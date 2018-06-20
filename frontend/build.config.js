'use strict';

var path = require('path');

var targetBase = './_public/frontend/';

module.exports = {
    target: {
        js: path.join(targetBase, 'js'),
        lib: path.join(targetBase, 'js', 'lib'),
        css: path.join(targetBase, 'css'),
        fonts: path.join(targetBase, 'fonts'),
        partials: path.join(targetBase, 'partials'),
        assets: targetBase
    },

    buildProfile: process.env.BUILD_PROFILE || 'dev',

    e2e: {
        profile: process.env.E2E_PROFILE || 'local',
        testFiles: ['./e2e/**/*.spec.js'],
        config: {
            local: './protractor.conf.js',
            ci: './protractor.ci.conf.js'
        },
        hub: {
            local: 'http://localhost:4444/wd/hub',
            ci: 'http://hub.selenium.vincit.intranet/wd/hub'
        },
        baseUrl: {
            local: 'http://localhost:9494/',
            ci: 'http://ci.vincit.intranet:9494/'
        }
    },

    vendorFiles: {
        fonts: [
            '../node_modules/bootstrap/dist/fonts/*',
            '../node_modules/font-awesome/fonts/*'
        ],
        other: [
            '../node_modules/lodash/index.js',
            '../node_modules/moment/min/moment.min.js',
            '../node_modules/leaflet/dist/leaflet.js',
            '../node_modules/leaflet.markercluster/dist/leaflet.markercluster.js',
            '../node_modules/drmonty-leaflet-awesome-markers/js/leaflet.awesome-markers.min.js',
            './vendor/Leaflet.VectorGrid.bundled.min.js',
            '../node_modules/leaflet-fullscreen/dist/Leaflet.fullscreen.min.js',
            '../node_modules/proj4/dist/proj4.js',
            '../node_modules/proj4leaflet/src/proj4leaflet.js',
            './app/leaflet/leaflet-projection.js',
            './app/leaflet/leaflet-lasso.js',
            './app/leaflet/leaflet-marquee.js',
            './app/leaflet/leaflet-polyline.js',
            './app/leaflet/leaflet-geojson-layers.js',
            './app/leaflet/leaflet-coordinates.js',
            './app/leaflet/leaflet-simple-legend.js',
            '../node_modules/greiner-hormann/dist/greiner-hormann.min.js',
            '../node_modules/qrcodejs/qrcode.min.js'
        ],
        angular: [
            '../node_modules/es6-promise-polyfill/promise.min.js',
            // Must load jQuery here for angular auto-detection
            '../node_modules/jquery/dist/jquery.min.js',
            '../node_modules/dropzone/dist/min/dropzone.min.js',
            '../node_modules/angular/angular.min.js',
            '../node_modules/angular-i18n/angular-locale_fi-fi.js',
            '../node_modules/angular-cookies/angular-cookies.min.js',
            '../node_modules/angular-resource/angular-resource.min.js',
            '../node_modules/angular-sanitize/angular-sanitize.min.js',
            '../node_modules/angular-messages/angular-messages.min.js',
            '../node_modules/angular-cache/dist/angular-cache.min.js',
            '../node_modules/angular-loading-bar/build/loading-bar.min.js',
            '../node_modules/angular-translate/dist/angular-translate.min.js',
            '../node_modules/angular-translate-loader-static-files/angular-translate-loader-static-files.min.js',
            '../node_modules/angular-translate-storage-cookie/angular-translate-storage-cookie.min.js',
            '../node_modules/angular-translate-storage-local/angular-translate-storage-local.min.js',
            '../node_modules/angular-translate-handler-log/angular-translate-handler-log.min.js',
            '../node_modules/angular-ui-router/release/angular-ui-router.min.js',
            './vendor/ui-router-history.js',
            '../node_modules/angular-ui-mask/dist/mask.min.js',
            '../node_modules/angular-ui-bootstrap/dist/ui-bootstrap-tpls.js',
            '../node_modules/angular-ui-select2/src/select2.js',
            '../node_modules/ui-select/dist/select.js',
            //'../node_modules/ui-leaflet/dist/ui-leaflet.js',
            './vendor/ui-leaflet.min.js',
            '../node_modules/angular-bootstrap-show-errors/src/showErrors.min.js',
            '../node_modules/angular-growl-v2/build/angular-growl.min.js',
            '../node_modules/angular-dialog-service/dist/dialogs.min.js',
            '../node_modules/angular-simple-logger/dist/angular-simple-logger.light.min.js',
            '../node_modules/angular-vs-repeat/src/angular-vs-repeat.min.js',
            '../node_modules/angular-block-ui/dist/angular-block-ui.min.js',
            '../node_modules/angular-upload/angular-upload.min.js',
            '../node_modules/angular-http-auth/src/http-auth-interceptor.js',
            '../node_modules/angular-dropzone/lib/angular-dropzone.js',
            '../node_modules/ng-idle/angular-idle.min.js',
            '../node_modules/rangy/lib/rangy-core.js',
            '../node_modules/rangy/lib/rangy-selectionsaverestore.js',
            '../node_modules/textangular/dist/textAngular.min.js',
            '../node_modules/diff/dist/diff.min.js',
            '../node_modules/angular-file-saver/dist/angular-file-saver.bundle.min.js',
            // This must be the last dependency due to packaging issues
            '../node_modules/select2/select2.js'
        ]
    },

    appFiles: {
        code: [
            './app/**/*.js',
            '!./app/leaflet/**',
            '!./app/**/*.spec.js'
        ],
        style: './app/app.less',
        styleBase: './app/',
        styleWatch: './app/**/*.less',
        partials: [
            "./app/module/**/*.html"
        ],
        assetsBase: './app/assets/',
        assets: [
            './app/assets/**'
        ],
        shim: [
            '../node_modules/angular-loader/angular-loader.min.js',
            // Old async loader is still required by cached scripts
            '../node_modules/scriptjs/dist/script.min.js',
            '../node_modules/loadjs/dist/loadjs.min.js'
        ]
    }
};
