'use strict';

angular.module('app.public.decision', [])
    .factory('DecisionPublicAccess', function ($resource) {
        var apiPrefix = 'api/v1/anon/decision/receiver/download/:uuid';

        function getMethod(suffix, isArray) {
            return {method: 'GET', url: apiPrefix + suffix, isArray: !!isArray};
        }

        function postMethod(suffix, isArray) {
            return {method: 'POST', url: apiPrefix + suffix, isArray: !!isArray};
        }

        return $resource(apiPrefix, {uuid: '@uuid'});
    })
    .config(function ($stateProvider) {
            $stateProvider
                .state('public', {
                    abstract: true,
                    templateUrl: 'public/layout.html',
                    url: '/public',
                    authenticate: false
                })
                .state('public.decision', {
                    templateUrl: 'public/decision.html',
                    url: '/decision/{uuid:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}}',
                    controllerAs: '$ctrl',
                    controller: 'PublicDecisionDownloadController',
                    authenticate: false,
                    resolve: {
                        downloadLinks: function ($stateParams, DecisionPublicAccess) {
                            return DecisionPublicAccess.get({uuid: $stateParams.uuid}).$promise;
                        }
                    }
                });
        }
    )
    .controller('PublicDecisionDownloadController', function (FetchAndSaveBlob, downloadLinks) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.downloadLinks = downloadLinks;
        };

        $ctrl.downloadDecision = function () {
            FetchAndSaveBlob.get($ctrl.downloadLinks.decisionLink.url);
        };

        $ctrl.downloadAttachment = function (url) {
            FetchAndSaveBlob.get(url);
        };
    });
