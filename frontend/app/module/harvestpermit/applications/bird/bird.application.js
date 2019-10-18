'use strict';

angular.module('app.harvestpermit.application.bird', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.bird', {
                url: '/bird',
                templateUrl: 'harvestpermit/applications/bird/layout.html',
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
                                $state.go('profile.permitwizard.bird.' + childState, _.extend({
                                    applicationId: applicationId
                                }, params));
                            }
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.bird', {
                url: '/bird',
                templateUrl: 'harvestpermit/applications/bird/layout.html',
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
                                $state.go('jht.decision.application.wizard.bird.' + childState, _.extend({
                                    applicationId: applicationId
                                }, params));
                            }
                        };
                    }
                }
            });
    })
    .component('birdPermitApplicationWizardApplicantType', {
        templateUrl: 'harvestpermit/applications/bird/bird-applicant-type.html',
        bindings: {
            subtype: '<'
        }
    })
    .factory('BirdPermitApplication', function ($resource) {
        var apiPrefix = 'api/v1/harvestpermit/application/bird/:id';

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
            saveSpeciesAmounts: postMethod('/species', true),

            // Species periods
            listSpeciesPeriods: getMethod('/period'),
            saveSpeciesPeriods: postMethod('/period'),

            // Permit holder
            getCurrentpermitHolder: getMethod('/permit-holder'),
            updatePermitHolder: postMethod('/permit-holder'),

            // Protected area
            getCurrentProtectedArea: getMethod('/area'),
            updateProtectedArea: postMethod('/area'),

            // Deviate
            getCurrentDeviationJustification: getMethod('/method'),
            updateDeviationJustification: postMethod('/method'),

            // Permit cause
            getCurrentPermitCause: getMethod('/cause'),
            updatePermitCause: postMethod('/cause'),

            // Damage
            getSpeciesDamage: getMethod('/damage', true),
            updateSpeciesDamage: postMethod('/damage', true),

            // Population
            getSpeciesPopulation: getMethod('/population', true),
            updateSpeciesPopulation: postMethod('/population', true),

            // Attachments
            listAttachments: getMethod('/attachment', true),
            updateAttachmentAdditionalInfo: postMethod('/attachment', true),
        });
    });
