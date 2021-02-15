'use strict';

angular.module('app.harvestpermit.application.gamemanagement', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.gamemanagement', {
                url: '/gamemanagement',
                templateUrl: 'harvestpermit/applications/gamemanagement/layout.html',
                abstract: true,
                controllerAs: '$ctrl',
                controller: function (applicationBasicDetails) {
                    this.applicationBasicDetails = applicationBasicDetails;
                },
                resolve: {
                    wizard: function ($state, applicationId, applicationBasicDetails) {
                        return {
                            isAmending: _.constant(applicationBasicDetails.status === 'AMENDING'),
                            reload: $state.reload,
                            exit: function () {
                                $state.go('profile.permits');
                            },
                            goto: function (childState, params) {
                                $state.go('profile.permitwizard.gamemanagement.' + childState, _.extend({
                                    applicationId: applicationId
                                }, params));
                            }
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.gamemanagement', {
                url: '/gamemanagement',
                templateUrl: 'harvestpermit/applications/gamemanagement/layout.html',
                abstract: true,
                controllerAs: '$ctrl',
                controller: function (applicationBasicDetails) {
                    this.applicationBasicDetails = applicationBasicDetails;
                },
                resolve: {
                    wizard: function ($state, applicationId, applicationBasicDetails) {
                        return {
                            isAmending: _.constant(applicationBasicDetails.status === 'AMENDING'),
                            reload: $state.reload,
                            exit: function () {
                                $state.go('jht.decision.application.overview');
                            },
                            goto: function (childState, params) {
                                $state.go('jht.decision.application.wizard.gamemanagement.' + childState, _.extend({
                                    applicationId: applicationId
                                }, params));
                            }
                        };
                    }
                }
            });
    })
    .factory('GameManagementPermitApplication', function ($resource) {
        var apiPrefix = 'api/v1/harvestpermit/application/gamemanagement/:id';

        function getMethod(suffix, isArray) {
            return {method: 'GET', url: apiPrefix + suffix, isArray: !!isArray};
        }

        function postMethod(suffix, isArray) {
            return {method: 'POST', url: apiPrefix + suffix, isArray: !!isArray};
        }

        return $resource(apiPrefix, {id: '@id'}, {
            getFullDetails: getMethod('/full'),

            // Permit holder
            getCurrentpermitHolder: getMethod('/permit-holder'),
            updatePermitHolder: postMethod('/permit-holder'),

            // Species amount
            getSpeciesAmount: getMethod('/species'),
            saveSpeciesAmount: postMethod('/species'),

            // Period
            getSpeciesPeriod: getMethod('/period'),
            saveSpeciesPeriod: postMethod('/period'),

            // Justification
            getJustification: getMethod('/justification'),
            updateJustification: postMethod('/justification'),

            // Forbidden methods
            getCurrentDeviationJustification: getMethod('/method'),
            updateDeviationJustification: postMethod('/method')

        });
    });
