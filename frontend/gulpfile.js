var gulp = require("gulp");
var plugins = require('gulp-load-plugins')();
var del = require('del');
var uglifyjs = require('uglify-js');
var composer = require('gulp-uglify/composer');
var minifier = composer(uglifyjs, console);
var saveLicense = require('uglify-save-license');
var karma = require('karma');
var karmaConfig = __dirname + '/karma.conf.js';
var config = require('./build.config.js');
var isProduction = (config.buildProfile === 'prod');

console.log("BUILD PROFILE", config.buildProfile);

gulp.task('clean', function (cb) {
    return del(['_public'], cb)
});

gulp.task('jshint', function () {
    return gulp.src(config.appFiles.code)
        .pipe(plugins.cached('jshint'))
        .pipe(plugins.jshint('.jshintrc'))
        .pipe(plugins.jshint.reporter('jshint-stylish'))
        .pipe(plugins.notify(function (file) {
            if (file.jshint.success) {
                return false;
            }

            var errors = file.jshint.results.map(function (data) {
                if (data.error) {
                    return "(" + data.error.line + ':' + data.error.character + ') ' + data.error.reason;
                }
            }).join("\n");

            return file.relative + " (" + file.jshint.results.length + " errors)\n" + errors;
        }));
});

gulp.task('test:unit', function (done) {
    new karma.Server({
        configFile: karmaConfig,
        reporters: ['junit', 'spec'],
        singleRun: true
    }, done).start();
});

gulp.task('webdriver_update', plugins.protractor.webdriver_update);

gulp.task('e2e', gulp.series('webdriver_update', function (done) {
    var configFile = config.e2e.config[config.e2e.profile];
    var baseUrl = config.e2e.baseUrl[config.e2e.profile];
    var hub = config.e2e.hub[config.e2e.profile];

    gulp.src(config.e2e.testFiles)
        .pipe(plugins.protractor.protractor({
            'configFile': configFile,
            'args': ['--baseUrl', baseUrl, '--seleniumAddress', hub],
        }))
        .on('error', function (e) {
            throw e;
        })
        .on('end', done);
}));

gulp.task('appCode', function () {
    return gulp.src(config.appFiles.code)
        .pipe(plugins.cached('scripts'))
        .pipe(plugins.if(!isProduction, plugins.sourcemaps.init()))
        .pipe(plugins.ngAnnotate())
        .pipe(minifier({
            compress: true,
            mangle: true
        }, uglifyjs))
        .pipe(plugins.remember('scripts'))
        .pipe(plugins.concat('app.min.js'))
        .pipe(plugins.if(!isProduction, plugins.sourcemaps.write('.')))
        .pipe(gulp.dest(config.target.js))
        .pipe(plugins.size({showFiles: true}))
        .pipe(plugins.livereload());
});

gulp.task('appPartials', function () {
    return gulp.src(config.appFiles.partials)
        .pipe(plugins.cached('partials'))
        .pipe(plugins.htmlmin({
            collapseWhitespace: true,
            removeComments: true
        }))
        .pipe(plugins.remember('partials'))
        .pipe(plugins.angularTemplatecache({
            standalone: true,
            transformUrl: function (url) {
                // Remove leading slash
                return url.substring(1);
            }
        }))
        .pipe(gulp.dest(config.target.js))
        .pipe(plugins.size({showFiles: true}))
        .pipe(plugins.livereload());
});

gulp.task('appStyle', function () {
    return gulp.src(config.appFiles.style)
        .pipe(plugins.less({
            paths: [config.appFiles.styleBase]
        }))
        .pipe(plugins.concat('app.css'))
        .pipe(plugins.cleanCss())
        .pipe(gulp.dest(config.target.css))
        .pipe(plugins.size({showFiles: true}))
        .pipe(plugins.livereload());
});

gulp.task('appAssets', function () {
    return gulp.src(config.appFiles.assets, {base: config.appFiles.assetsBase})
        .pipe(plugins.if(function (file) {
            return /\.json$/.test(file.path);
        }, plugins.jsonminify()))
        .pipe(gulp.dest(config.target.assets));
});

gulp.task('appShim', function () {
    return gulp.src(config.appFiles.shim)
        .pipe(minifier({
            mangle: false,
            compress: false,
            output: {
                comments: false
            }
        }, uglifyjs))
        .pipe(gulp.dest(config.target.lib));
});

gulp.task('vendorFonts', function () {
    return gulp.src(config.vendorFiles.fonts)
        .pipe(plugins.flatten())
        .pipe(gulp.dest(config.target.fonts));
});

var notMinified = function (file) {
    return !/\.min\.js/.test(file.path);
};

function createVendorCodePipeline(sourceFiles, targetFile) {
    return function () {
        return gulp.src(sourceFiles)
            .pipe(plugins.if(!isProduction, plugins.sourcemaps.init({loadMaps: true})))
            .pipe(plugins.cached(targetFile))
            // Skip compression on minified files
            .pipe(plugins.if(notMinified, minifier({
                output: {
                    comments: saveLicense
                },
                compress: true,
                mangle: false
            }, uglifyjs)))
            .pipe(plugins.remember(targetFile))
            .pipe(plugins.concat(targetFile))
            .pipe(plugins.if(!isProduction, plugins.sourcemaps.write('.')))
            .pipe(gulp.dest(config.target.js))
            .pipe(plugins.size({showFiles: true}))
            .pipe(plugins.livereload());
    };
}

gulp.task('vendorCodeAngular', createVendorCodePipeline(config.vendorFiles.angular, 'vendor.angular.min.js'));
gulp.task('vendorCodeOther', createVendorCodePipeline(config.vendorFiles.other, 'vendor.other.min.js'));

gulp.task('watch', function () {
    plugins.livereload.listen();

    function reload(done) {
        done();
    }

    gulp.watch(config.appFiles.code)
        .on('unlink', function (path) {
            delete plugins.cached.caches['scripts'][path];
            plugins.remember.forget('scripts', path);
        })
        .on('change', gulp.series('appCode', gulp.parallel(reload, 'jshint')));

    gulp.watch(config.appFiles.partials, {delay: 500}, gulp.series('appPartials', reload));
    gulp.watch(config.appFiles.styleWatch, {delay: 100}, gulp.series('appStyle', reload));
    gulp.watch(config.appFiles.assets, {delay: 1000}, gulp.series('appAssets', reload));
    gulp.watch(config.vendorFiles.other, {}, gulp.series('vendorCodeOther', reload));
    // gulp.watch(config.vendorFiles.angular, {delay: 1000}, gulp.series('vendorCodeAngular', reload));
});

gulp.task('build', gulp.parallel(
    'appStyle',
    'appShim',
    'appAssets',
    'appPartials',
    'appCode',
    'vendorFonts',
    'vendorCodeOther',
    'vendorCodeAngular'
));

gulp.task('tdd', gulp.parallel('watch', function (done) {
    new karma.Server({
        configFile: karmaConfig,
        singleRun: false,
        autoWatch: true
    }, function (exitStatus) {
        done();
    }).start();
}));

gulp.task('watch:tdd', gulp.series('clean', 'build', 'tdd'));

gulp.task('build:dist', gulp.series('clean', 'build', 'test:unit'));

gulp.task('default', gulp.series('clean', 'build', 'watch'));
