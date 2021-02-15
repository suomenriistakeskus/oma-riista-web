'use strict';

angular.module('app.harvestpermit.application.mammal', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.mammal', {
                url: '/mammal',
                templateUrl: 'harvestpermit/applications/mammal/layout.html',
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
                                $state.go('profile.permitwizard.mammal.' + childState, _.extend({
                                    applicationId: applicationId
                                }, params));
                            }
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.mammal', {
                url: '/mammal',
                templateUrl: 'harvestpermit/applications/mammal/layout.html',
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
                                $state.go('jht.decision.application.wizard.mammal.' + childState, _.extend({
                                    applicationId: applicationId
                                }, params));
                            }
                        };
                    }
                }
            });
    })
    .factory('MammalPermitApplication', function ($resource) {
        var apiPrefix = 'api/v1/harvestpermit/application/mammal/:id';

        function getMethod(suffix, isArray) {
            return {method: 'GET', url: apiPrefix + suffix, isArray: !!isArray};
        }

        function postMethod(suffix, isArray) {
            return {method: 'POST', url: apiPrefix + suffix, isArray: !!isArray};
        }

        return $resource(apiPrefix, {id: '@id'}, {
            getFullDetails: getMethod('/full'),

            // Species amounts
            listSpeciesAmounts: getMethod('/species', true),
            saveSpeciesAmounts: postMethod('/species'),

            // Permit holder
            getCurrentpermitHolder: getMethod('/permit-holder'),
            updatePermitHolder: postMethod('/permit-holder'),

            // Species periods
            listSpeciesPeriods: getMethod('/period'),
            saveSpeciesPeriods: postMethod('/period'),

            // Area
            getArea: getMethod('/area'),
            updateArea: postMethod('/area'),
            listAvailableClubs: getMethod('/area/clubs', true),
            addAreaAttachment: postMethod('/area-attachment'),

            // Justification
            getJustification: getMethod('/justification'),
            updateJustification: postMethod('/justification', true),


            // Deviate
            getCurrentDeviationJustification: getMethod('/method'),
            updateDeviationJustification: postMethod('/method'),

            // Attachments
            listAttachments: getMethod('/attachment', true),
            updateAttachmentAdditionalInfo: postMethod('/attachment', true),

            // Permit reasons
            getCurrentPermitCause: getMethod('/reasons'),
            updatePermitCause: postMethod('/reasons')
        });
    })
;
