'use strict';

angular.module('app.harvestpermit.application.deportation', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.deportation', {
                url: '/deportation',
                templateUrl: 'harvestpermit/applications/deportation/layout.html',
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
                                $state.go('profile.permitwizard.deportation.' + childState, _.extend({
                                    applicationId: applicationId
                                }, params));
                            }
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.deportation', {
                url: '/deportation',
                templateUrl: 'harvestpermit/applications/deportation/layout.html',
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
                                $state.go('jht.decision.application.wizard.deportation.' + childState, _.extend({
                                    applicationId: applicationId
                                }, params));
                            }
                        };
                    }
                }
            });
    })
    .factory('DeportationPermitApplication', function ($resource) {
        var apiPrefix = 'api/v1/harvestpermit/application/deportation/:id';

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

            // Species amounts
            getSpeciesAmount: getMethod('/species'),
            saveSpeciesAmount: postMethod('/species'),

            // Reasons
            getCurrentPermitCause: getMethod('/reasons'),
            updatePermitCause: postMethod('/reasons'),

            // Periods
            getSpeciesPeriod: getMethod('/period'),
            saveSpeciesPeriod: postMethod('/period'),

            // Forbidden methods
            getCurrentDeviationJustification: getMethod('/method'),
            updateDeviationJustification: postMethod('/method')
        });
    });
