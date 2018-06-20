'use strict';

angular.module('app.organisation.controllers', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('organisation', {
                abstract: true,
                templateUrl: 'organisation/layout.html',
                url: '/organisation/{id:[0-9]{1,8}}',
                resolve: {
                    orgId: function ($stateParams) {
                        return $stateParams.id;
                    },
                    organisation: function (Organisations, orgId) {
                        return Organisations.get({id: orgId}).$promise;
                    }
                },
                controller: function ($scope, organisation) {
                    $scope.showOccupationMenu = organisation.hasOccupations;
                    $scope.showDecisionrecipients = organisation.organisationType === 'RKA';
                    $scope.showDecisionauthorities = organisation.organisationType === 'RKA';
                }
            })
            .state('organisation.show', {
                url: '/show',
                templateUrl: 'organisation/show.html',
                controller: 'OrganisationShowController',
                controllerAs: '$ctrl'
            })
            .state('organisation.occupations', {
                url: '/occupations',
                templateUrl: 'occupation/list.html',
                controller: 'OccupationListController',
                controllerAs: '$ctrl',
                resolve: {
                    allOccupations: function (Occupations, orgId) {
                        return Occupations.query({orgId: orgId}).$promise;
                    },
                    onlyBoard: function () {
                        return true;
                    },
                    occupationTypes: function (OccupationTypes, orgId) {
                        return OccupationTypes.query({orgId: orgId}).$promise;
                    }
                }
            })
            .state('organisation.events', {
                url: '/events',
                templateUrl: 'event/event_list.html',
                controller: 'EventListController'
            })
            .state('organisation.locations', {
                url: '/locations',
                templateUrl: 'event/venue_list.html',
                controller: 'VenueListController'
            })
            .state('organisation.decisionrecipients', {
                url: '/decisionrecipients',
                templateUrl: 'harvestpermit/decision/rkarecipient/list.html',
                controller: 'DecisionRkaRecipientListControler',
                controllerAs: '$ctrl',
                resolve: {
                    rkaId: function (orgId) {
                        return orgId;
                    },
                    recipients: function (DecisionRkaRecipient, rkaId) {
                        return DecisionRkaRecipient.listByRka({rkaId: rkaId});
                    }
                }
            })
            .state('organisation.decisionauthorities', {
                url: '/decisionauuthorities',
                templateUrl: 'harvestpermit/decision/rkaauthority/list.html',
                controller: 'DecisionRkaAuthorityListControler',
                controllerAs: '$ctrl',
                resolve: {
                    rkaId: function (orgId) {
                        return orgId;
                    },
                    authorities: function (DecisionRkaAuthority, rkaId) {
                        return DecisionRkaAuthority.listByRka({rkaId: rkaId});
                    }
                }
            });
    })
    .controller('OrganisationShowController', function (organisation) {
        this.organisation = organisation;
    });
