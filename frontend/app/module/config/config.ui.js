'use strict';

angular.module('app.config.ui', [])

    .config(function (uibDatepickerConfig) {
        uibDatepickerConfig.showWeeks = false;
        uibDatepickerConfig.startingDay = 1;
    })

    .config(function (uibDatepickerPopupConfig) {
        uibDatepickerPopupConfig.showButtonBar = false;
        uibDatepickerPopupConfig.datepickerPopup = 'd.M.yyyy';
        uibDatepickerPopupConfig.altInputFormats = ['dd.MM.yyyy', 'yyyy-MM-dd'];
    })

    .config(function ($animateProvider) {
        $animateProvider.classNameFilter(/enable-ng-animate/);
    })

    .config(function ($uibModalProvider) {
        $uibModalProvider.options.backdrop = 'static';
    })

    .config(function (growlProvider) {
        // Default notification timeout
        growlProvider.globalTimeToLive(5000);
    })

    .run(function (blockUIConfig) {
        blockUIConfig.delay = 1;
        blockUIConfig.autoBlock = false;
    })

    .config(function (KeepaliveProvider, IdleProvider) {
        KeepaliveProvider.http('/api/ping');
        KeepaliveProvider.interval(29 * 60);
        IdleProvider.idle(25 * 60);
        IdleProvider.timeout(5 * 60);
        IdleProvider.windowInterrupt('focus');
        IdleProvider.keepalive(true);
    });
