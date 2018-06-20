var gulp = require("gulp");
var plugins = require('gulp-load-plugins')();
var del = require('del');
var pump = require('pump');
var minifier = require('gulp-uglify/minifier');
var uglifyjs = require('uglify-js');
var runSequence = require('run-sequence');
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

gulp.task('tdd', ['watch'], function (done) {
    new karma.Server({
        configFile: karmaConfig,
        singleRun: false,
        autoWatch: true
    }, function (exitStatus) {
        done();
    }).start();
});

gulp.task('webdriver_update', plugins.protractor.webdriver_update);

gulp.task('e2e', ['webdriver_update'], function (done) {
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
});

gulp.task('appCode', function (cb) {
    pump([
        gulp.src(config.appFiles.code),
        plugins.cached('scripts'),
        plugins.if(!isProduction, plugins.sourcemaps.init()),
        plugins.ngAnnotate(),
        minifier({
            compress: {
                screw_ie8: true
            },
            mangle: {
                screw_ie8: true
            }
        }, uglifyjs),
        plugins.remember('scripts'),
        plugins.concat('app.min.js'),
        plugins.if(!isProduction, plugins.sourcemaps.write('.')),
        gulp.dest(config.target.js),
        plugins.size({showFiles: true}),
        plugins.livereload({auto: false})
    ], cb);
});

gulp.task('appPartials', function (cb) {
    pump([
        gulp.src(config.appFiles.partials),
        plugins.cached('partials'),
        plugins.htmlmin({
            collapseWhitespace: true,
            removeComments: true
        }),
        plugins.remember('partials'),
        plugins.angularTemplatecache({
            standalone: true
        }),
        gulp.dest(config.target.js),
        plugins.size({showFiles: true}),
        plugins.livereload({auto: false})
    ], cb);
});

gulp.task('appStyle', function (cb) {
    pump([
        gulp.src(config.appFiles.style),
        //plugins.if(!isProduction, plugins.sourcemaps.init())
        plugins.less({
            paths: [config.appFiles.styleBase]
        }),
        plugins.concat('app.css'),
        plugins.cleanCss(),
        //plugins.if(!isProduction, plugins.sourcemaps.write('.'))
        gulp.dest(config.target.css),
        plugins.size({showFiles: true}),
        plugins.livereload({auto: false})
    ], cb);
});

gulp.task('appAssets', function (cb) {
    pump([
        gulp.src(config.appFiles.assets, {base: config.appFiles.assetsBase}),
        plugins.if(function (file) {
            return /\.json$/.test(file.path);
        }, plugins.jsonminify()),
        gulp.dest(config.target.assets)
    ], cb);
});

gulp.task('appShim', function (cb) {
    pump([
        gulp.src(config.appFiles.shim),
        minifier({
            mangle: false,
            compress: false,
            output: {
                comments: false
            }
        }, uglifyjs),
        gulp.dest(config.target.lib)
    ], cb);
});

gulp.task('vendorFonts', function (cb) {
    pump([
        gulp.src(config.vendorFiles.fonts),
        plugins.flatten(),
        gulp.dest(config.target.fonts)
    ], cb);
});

var notMinified = function (file) {
    return !/\.min\.js/.test(file.path);
};

function createVendorCodePipeline(sourceFiles, targetFile) {
    return function (cb) {
        pump([
            gulp.src(sourceFiles),
            plugins.if(!isProduction, plugins.sourcemaps.init({loadMaps: true})),
            plugins.cached(targetFile),
            // Skip compression on minified files
            plugins.if(notMinified, minifier({
                preserveComments: 'license',
                compress: {
                    screw_ie8: true
                },
                mangle: false
            }, uglifyjs)),
            plugins.remember(targetFile),
            plugins.concat(targetFile),
            plugins.if(!isProduction, plugins.sourcemaps.write('.')),
            gulp.dest(config.target.js),
            plugins.size({showFiles: true}),
            plugins.livereload({auto: false})
        ], cb);
    };
}

gulp.task('vendorCodeAngular', createVendorCodePipeline(config.vendorFiles.angular, 'vendor.angular.min.js'));
gulp.task('vendorCodeOther', createVendorCodePipeline(config.vendorFiles.other, 'vendor.other.min.js'));

gulp.task('watch', function () {
    plugins.livereload.listen(35729, {auto: true});

    gulp.watch(config.appFiles.code, ['appCode', 'jshint'])
        .on('change', function (event) {
            if (event.type === 'deleted') {
                delete plugins.cached.caches['scripts'][event.path];
                plugins.remember.forget('scripts', event.path);
            }
        });

    gulp.watch(config.appFiles.partials, {interval: 500}, ['appPartials']);
    gulp.watch(config.appFiles.styleWatch, {interval: 100}, ['appStyle']);
    gulp.watch(config.appFiles.assets, {interval: 1000}, ['appAssets']);
    gulp.watch(config.vendorFiles.other, {}, ['vendorCodeOther']);
    // gulp.watch(config.vendorFiles.angular, { interval: 1000 }, ['vendorCodeAngular']);
});

gulp.task('watch:tdd', function (cb) {
    runSequence('clean', 'build', 'tdd', cb);
});

gulp.task('build', [
    'appStyle',
    'appShim',
    'appAssets',
    'appPartials',
    'appCode',
    'vendorFonts',
    'vendorCodeOther',
    'vendorCodeAngular'
]);

gulp.task('build:dist', function (cb) {
    runSequence('clean', 'build', 'test:unit', cb);
});

gulp.task('default', function (cb) {
    runSequence('clean', 'build', 'watch', cb);
});
