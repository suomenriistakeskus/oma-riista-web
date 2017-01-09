(function () {
    'use strict';

    angular.module('app.layout.language', [])
        .controller('LanguageController', LanguageController)
        .directive('navLanguage', function () {
            return {
                restrict: 'E',
                replace: true,
                scope: true,
                bindToController: true,
                controllerAs: '$ctrl',
                templateUrl: 'layout/language/nav-language.html',
                controller: LanguageController
            };
        });

    function LanguageController($translate, $http) {
        this.isSelected = function (value) {
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
    }
})();
