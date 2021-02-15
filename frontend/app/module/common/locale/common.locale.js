'use strict';

angular.module('app.common.locale', [])
    .component('documentLocaleSelection', {
        templateUrl: 'common/locale/document-locale-selection.html',
        bindings: {
            locale: '='
        },
        controllerAs: '$ctrl',
        controller: function () {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.localeOptions = [{
                    code: 'fi_FI', localisationKey: 'global.languageName.fi'
                }, {
                    code: 'sv_FI', localisationKey: 'global.languageName.sv'
                }];
            };
        }});
