'use strict';

angular.module('app.harvestpermit.application.mooselike', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.mooselike', {
                url: '/mooselike',
                templateUrl: 'harvestpermit/applications/mooselike/layout.html',
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
                                $state.go('profile.permitwizard.mooselike.' + childState, _.extend({
                                    applicationId: applicationId
                                }, params));
                            }
                        };
                    }
                }
            })

            .state('jht.decision.application.wizard.mooselike', {
                url: '/mooselike',
                templateUrl: 'harvestpermit/applications/mooselike/layout.html',
                abstract: true,
                controllerAs: '$ctrl',
                controller: function (applicationBasicDetails) {
                    this.applicationBasicDetails = applicationBasicDetails;
                },
                resolve: {
                    wizard: function ($state, decisionId) {
                        return {
                            isAmending: _.constant(true),
                            reload: $state.reload,
                            exit: function () {
                                $state.go('jht.decision.application.overview');
                            },
                            goto: function (childState, params) {
                                $state.go('jht.decision.application.wizard.mooselike.' + childState, _.extend({
                                    decisionId: decisionId
                                }, params));
                            }
                        };
                    }
                }
            });
    })
    .factory('MooselikePermitApplication', function ($resource) {
        var apiPrefix = 'api/v1/harvestpermit/application/mooselike/:id';

        function getMethod(suffix, isArray) {
            return {method: 'GET', url: apiPrefix + suffix, isArray: !!isArray};
        }

        function postMethod(suffix, isArray) {
            return {method: 'POST', url: apiPrefix + suffix, isArray: !!isArray};
        }

        return $resource(apiPrefix, {id: '@id'}, {
            getFullDetails: getMethod('/full'),

            // Validate and send
            validate: postMethod('/validate'),
            sendApplication: postMethod('/send'),

            // Species amounts
            listSpeciesAmounts: getMethod('/species', true),
            saveSpeciesAmounts: postMethod('/species', true),

            // Permit holder
            listAvailablePermitHolders: getMethod('/permit-holder', true),
            searchPermitHolder: getMethod('/permit-holder-search'),
            updatePermitHolder: postMethod('/permit-holder-club'),

            // Partners
            listPartnerClubs: getMethod('/partner/club', true),

            // Shooter count
            getShooterCounts: getMethod('/shooters'),
            updateShooterCounts: postMethod('/shooters'),

            // Attachments
            importMh: postMethod('/mh'),

            // Permit area
            getArea: getMethod('/area'),
            getAreaStatus: getMethod('/area/status'),
            setAreaReady: postMethod('/area/ready'),
            setAreaIncomplete: postMethod('/area/incomplete'),
            getBounds: getMethod('/area/bounds'),
            getGeometry: getMethod('/area/geometry'),

            // Conflicts & fragments
            listConflicts: getMethod('/conflicts', true),
            listPairwiseConflicts: getMethod('/conflicts/:otherId', true),
            getGeometryFragmentInfo: postMethod('/area/fragmentinfo', true)
        });
    })
    .factory('MooselikePermitApplicationAreaPartners', function ($resource) {
        var apiPrefix = '/api/v1/harvestpermit/application/mooselike/:applicationId/partner/:partnerId';

        return $resource(apiPrefix, {'applicationId': '@applicationId', 'partnerId': '@partnerId'}, {
            listAvailable: {
                method: 'GET',
                url: apiPrefix + '/available',
                isArray: true
            }
        });
    });
