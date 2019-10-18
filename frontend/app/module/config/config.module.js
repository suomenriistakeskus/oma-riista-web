"use strict";

angular.module('app.config', [
    'app.metadata',
    'app.config.error',
    'app.config.http',
    'app.config.router',
    'app.config.cache',
    'app.config.translate',
    'app.config.ui'

]).run(function ($log, appRevision) {
    $log.info("Application revision is", appRevision);

}).config(function ($compileProvider, isProductionEnvironment) {
    // https://docs.angularjs.org/guide/production
    $compileProvider.debugInfoEnabled(!isProductionEnvironment);
});
