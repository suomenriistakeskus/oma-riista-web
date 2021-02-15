'use strict';

angular.module('app.harvestpermit.application.dogunleash.application', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.dogunleash', {
                url: '/dogunleash',
                templateUrl: 'harvestpermit/applications/dogunleash/layout.html',
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
                                $state.go('profile.permitwizard.dogunleash.' + childState,
                                          _.extend({applicationId: applicationId}, params));
                            }
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.dogunleash', {
                url: '/dogunleash',
                templateUrl: 'harvestpermit/applications/dogunleash/layout.html',
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
                                $state.go('jht.decision.application.wizard.dogunleash.' + childState,
                                          _.extend({applicationId: applicationId}, params));
                            }
                        };
                    }
                }
            });
    })
    .factory('DogUnleashApplication', function ($resource) {
        var apiPrefix = 'api/v1/harvestpermit/application/dogevent/:id';

        function getMethod(suffix, isArray) {
            return {method: 'GET', url: apiPrefix + suffix, isArray: !!isArray};
        }

        function postMethod(suffix) {
            return {method: 'POST', url: apiPrefix + suffix};
        }

        function deleteMethod(suffix) {
            return {method: 'DELETE', url: apiPrefix + suffix};
        }

        return $resource(apiPrefix, {id: '@id', eventId: '@eventId'}, {

            // Permit holder
            getCurrentpermitHolder: getMethod('/permit-holder'),
            updatePermitHolder: postMethod('/permit-holder'),

            // Dog unleash permit application
            getEventDetails: getMethod('/unleash', true),
            updateEvent: postMethod('/unleash'),
            deleteEvent: deleteMethod('/unleash/:eventId'),

            getFullDetails: getMethod('/unleash/full')
        });
    });
