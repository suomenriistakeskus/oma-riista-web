'use strict';

angular.module('app.harvestpermit.application.weapontransportation', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.weapontransportation', {
                url: '/weapontransportation',
                templateUrl: 'harvestpermit/applications/weapontransportation/layout.html',
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
                                $state.go('profile.permitwizard.weapontransportation.' + childState, _.extend({
                                    applicationId: applicationId
                                }, params));
                            }
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.weapontransportation', {
                url: '/weapontransportation',
                templateUrl: 'harvestpermit/applications/weapontransportation/layout.html',
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
                                $state.go('jht.decision.application.wizard.weapontransportation.' + childState, _.extend({
                                    applicationId: applicationId
                                }, params));
                            }
                        };
                    }
                }
            });
    })
    .factory('WeaponTransportationPermitApplication', function ($resource) {
        var apiPrefix = 'api/v1/harvestpermit/application/weapontransportation/:id';

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

            // Reason
            getReason: getMethod("/reason"),
            updateReason: postMethod("/reason"),

            // Justification
            getJustification: getMethod("/justification"),
            updateJustification: postMethod("/justification")
        });
    });
