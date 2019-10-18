'use strict';

angular.module('app.harvestpermit.application.carnivore', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.carnivore', {
                url: '/carnivore',
                templateUrl: 'harvestpermit/applications/carnivore/layout.html',
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
                                $state.go('profile.permitwizard.carnivore.' + childState, _.extend({
                                    applicationId: applicationId
                                }, params));
                            }
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.carnivore', {
                url: '/carnivore',
                templateUrl: 'harvestpermit/applications/carnivore/layout.html',
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
                                $state.go('jht.decision.application.wizard.carnivore.' + childState, _.extend({
                                    applicationId: applicationId
                                }, params));
                            }
                        };
                    }
                }
            });
    })
    .factory('CarnivorePermitApplication', function ($resource) {
        var apiPrefix = 'api/v1/harvestpermit/application/carnivore/:id';

        function getMethod(suffix, isArray) {
            return {method: 'GET', url: apiPrefix + suffix, isArray: !!isArray};
        }

        function postMethod(suffix, isArray) {
            return {method: 'POST', url: apiPrefix + suffix, isArray: !!isArray};
        }

        return $resource(apiPrefix, {id: '@id'}, {
            getFullDetails: getMethod('/full'),

            // Send
            // sendApplication: postMethod('/send'),
            //
            // Species amounts
            listSpeciesAmounts: getMethod('/species'),
            saveSpeciesAmounts: postMethod('/species'),

            // Permit holder
            getCurrentpermitHolder: getMethod('/permit-holder'),
            updatePermitHolder: postMethod('/permit-holder'),

            // Area
            getArea: getMethod('/area'),
            updateArea: postMethod('/area'),
            listAvailableClubs: getMethod('/area/clubs', true),
            addAreaAttachment: postMethod('/area-attachment'),

            // Justification
            getJustification: getMethod('/justification'),
            updateJustification: postMethod('/justification', true),

            // Attachments
            listAttachments: getMethod('/attachment', true),
            updateAttachmentAdditionalInfo: postMethod('/attachment', true)
        });
    })
;
