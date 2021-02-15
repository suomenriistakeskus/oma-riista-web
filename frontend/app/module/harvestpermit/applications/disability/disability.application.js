'use strict';

angular.module('app.harvestpermit.application.disability', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.disability', {
                url: '/disability',
                templateUrl: 'harvestpermit/applications/disability/layout.html',
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
                                $state.go('profile.permitwizard.disability.' + childState, _.extend({
                                    applicationId: applicationId
                                }, params));
                            }
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.disability', {
                url: '/disability',
                templateUrl: 'harvestpermit/applications/disability/layout.html',
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
                                $state.go('jht.decision.application.wizard.disability.' + childState, _.extend({
                                    applicationId: applicationId
                                }, params));
                            }
                        };
                    }
                }
            });
    })
    .factory('DisabilityPermitApplication', function ($resource) {
        var apiPrefix = 'api/v1/harvestpermit/application/disability/:id';

        function getMethod(suffix, isArray) {
            return {method: 'GET', url: apiPrefix + suffix, isArray: !!isArray};
        }

        function postMethod(suffix, isArray) {
            return {method: 'POST', url: apiPrefix + suffix, isArray: !!isArray};
        }

        function putMethod(suffix, isArray) {
            return {method: 'PUT', url: apiPrefix + suffix, isArray: !!isArray};
        }

        return $resource(apiPrefix, {id: '@id'}, {
            getFullDetails: getMethod('/full'),

            // Permit holder
            getCurrentpermitHolder: getMethod('/permit-holder'),
            updatePermitHolder: postMethod('/permit-holder'),

            // Basic info
            getBasicInfo: getMethod('/basicinfo'),
            updateBasicInfo: putMethod('/basicinfo'),

            // Justification
            getJustification: getMethod('/justification'),
            updateJustification: putMethod('/justification')
        });
    });
