'use strict';

angular.module('app.config.translate', [])

    .config(function ($translateProvider, versionUrlPrefix) {
        // Initialize angular-translate
        $translateProvider.useStaticFilesLoader({
            prefix: versionUrlPrefix + '/i18n/',
            suffix: '.json'
        });

        $translateProvider.preferredLanguage('fi');
        $translateProvider.useLocalStorage();
        $translateProvider.useMissingTranslationHandlerLog();
        $translateProvider.useSanitizeValueStrategy('sanitizeParameters');
    });
