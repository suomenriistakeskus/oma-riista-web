'use strict';

angular.module('app.common.download', [])
    .component('rDownloadPdf', {
        templateUrl: 'common/download/download-pdf.html',
        bindings: {
            url: '<'
        }
    })
    .component('rDownloadXml', {
        templateUrl: 'common/download/download-xml.html',
        bindings: {
            url: '<'
        }
    })
    .component('rDownloadFile', {
        templateUrl: 'common/download/download-file.html',
        bindings: {
            url: '<',
            translateKey: '<',
            filename: '<',
            showIcon: '<',
            forceSplit: '<'
        },
        controller: function () {
            var $ctrl = this;
            $ctrl.iconVisible = function () {
                return $ctrl.showIcon === undefined || $ctrl.showIcon;
            };
            $ctrl.formatFilename = function () {
                if ($ctrl.forceSplit && _.isString($ctrl.filename)) {
                    return $ctrl.filename.replace(/[-_]/g, ' ');
                }
                return $ctrl.filename;
            };
        }
    });
