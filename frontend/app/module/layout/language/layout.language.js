'use strict';

angular.module('app.layout.language', [])
    .service('LanguageService', function ($translate, $http) {
        this.isSelectedLanguage = function (value) {
            return $translate.use() === value;
        };

        this.changeLanguage = function (languageKey) {
            $translate.use(languageKey);

            $http({
                method: 'POST',
                url: '/api/v1/language',
                params: {lang: languageKey}
            });
        };
    })
    .directive('navLanguage', function (LanguageService) {
        return {
            restrict: 'E',
            replace: true,
            scope: true,
            bindToController: true,
            controllerAs: '$ctrl',
            templateUrl: 'layout/language/nav-language.html',
            controller: function () {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.isSelectedLanguage = LanguageService.isSelectedLanguage;
                    $ctrl.changeLanguage = LanguageService.changeLanguage;
                };
            }
        };
    });
