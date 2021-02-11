'use strict';

angular.module('app.club.permits', [])
    .factory('ClubPermits', function ($resource) {
        return $resource('/api/v1/club/:clubId/permit/:permitId', {clubId: '@clubId', permitId: '@permitId'}, {
            huntingYears: {
                method: 'GET',
                url: '/api/v1/club/:clubId/permit/huntingyears',
                isArray: true
            },
            todos: {
                method: 'GET',
                url: '/api/v1/club/:clubId/permit/todo'
            }
        });
    })

    .config(function ($stateProvider) {
        $stateProvider
            .state('club.moosepermit', {
                url: '/permit?huntingYear&species',
                wideLayout: true,
                params: {
                    huntingYear: null,
                    species: null
                },
                resolve: {
                    stateBase: _.constant('club'),
                    huntingYears: function (ClubPermits, clubId) {
                        return ClubPermits.huntingYears({clubId: clubId}).$promise;
                    },
                    selectedYearAndSpecies: function (MoosePermitListSelectedHuntingYearService, $stateParams, huntingYears) {
                        return MoosePermitListSelectedHuntingYearService.resolve($stateParams, huntingYears);
                    }
                },
                views: {
                    '@club': {
                        templateUrl: 'harvestpermit/moosepermit/layout.html'
                    },
                    'left@club.moosepermit': {
                        templateUrl: 'harvestpermit/moosepermit/list/list.html',
                        controller: 'MoosePermitListController',
                        controllerAs: '$ctrl',
                        resolve: {
                            permits: function ($q, ClubPermits, selectedYearAndSpecies, clubId) {
                                if (!selectedYearAndSpecies.species || !selectedYearAndSpecies.huntingYear) {
                                    return $q.when([]);
                                }

                                return ClubPermits.query({
                                    clubId: clubId,
                                    year: selectedYearAndSpecies.huntingYear,
                                    species: selectedYearAndSpecies.species
                                }).$promise.then(function (permits) {
                                    return _.sortBy(permits, ['permitNumber', 'id']);
                                });
                            }
                        }
                    }
                }
            })
            .state('club.moosepermit.table', {
                url: '/{permitId:[0-9]{1,8}}/table',
                wideLayout: true,
                templateUrl: 'harvestpermit/moosepermit/table/permit-tables.html',
                controller: 'MoosePermitTableController',
                controllerAs: '$ctrl',
                resolve: {
                    permitId: function ($stateParams, MoosePermitSelection) {
                        return MoosePermitSelection.updateSelectedPermitId($stateParams);
                    },
                    permit: function (ClubPermits, clubId, permitId, selectedYearAndSpecies) {
                        return ClubPermits.get({
                            clubId: clubId,
                            permitId: permitId,
                            species: selectedYearAndSpecies.species
                        }).$promise;
                    }
                }
            })
            .state('club.moosepermit.map', {
                url: '/{permitId:[0-9]{1,8}}/map',
                templateUrl: 'harvestpermit/moosepermit/map/permit-map.html',
                controller: 'MoosePermitMapController',
                controllerAs: '$ctrl',
                bindToController: true,
                wideLayout: true,
                resolve: {
                    permitId: function ($stateParams, MoosePermitSelection) {
                        return MoosePermitSelection.updateSelectedPermitId($stateParams);
                    },
                    harvests: function (MoosePermitHarvest, permitId, selectedYearAndSpecies) {
                        return MoosePermitHarvest.query({
                            permitId: permitId,
                            huntingYear: selectedYearAndSpecies.huntingYear,
                            gameSpeciesCode: selectedYearAndSpecies.species
                        }).$promise;
                    },
                    featureCollection: function (MoosePermits, permitId, selectedYearAndSpecies) {
                        return MoosePermits.partnerAreaFeatures({
                            permitId: permitId,
                            huntingYear: selectedYearAndSpecies.huntingYear,
                            gameSpeciesCode: selectedYearAndSpecies.species
                        }).$promise;
                    },
                    mapBounds: function (MapBounds, club, featureCollection) {
                        var bounds = MapBounds.getBoundsFromGeoJsonFeatureCollection(featureCollection);
                        return bounds || MapBounds.getRhyBounds(club.rhy.officialCode);
                    },
                    goBackFn: function () {
                        return null;
                    }
                }
            })
            .state('club.moosepermit.rhystats', {
                url: '/{permitId:[0-9]{1,8}}/rhy-stats',
                template: '<moose-permit-statistics-simple-table statistics="$ctrl.statistics"></moose-permit-statistics-simple-table>',
                controller: function (statistics) {
                    this.statistics = statistics;
                },
                controllerAs: '$ctrl',
                bindToController: true,
                wideLayout: true,
                resolve: {
                    permitId: function ($stateParams, MoosePermitSelection) {
                        return MoosePermitSelection.updateSelectedPermitId($stateParams);
                    },
                    statistics: function (MoosePermits, permitId, selectedYearAndSpecies) {
                        return MoosePermits.rhyStatistics({
                            permitId: permitId,
                            speciesCode: selectedYearAndSpecies.species
                        }).$promise;
                    }
                }
            });
    })
    .service('MoosePermitPartnerDownloadModal', function ($uibModal, FormPostService, HarvestPermits,
                                                          HarvestPermitPdfUrl, HarvestPermitAttachmentUrl) {

        this.showModal = function (moosePermit) {
            return $uibModal.open({
                controller: ModalController,
                controllerAs: '$ctrl',
                templateUrl: 'club/permit/club-permit-download-modal.html',
                resolve: {
                    moosePermit: _.constant(moosePermit),
                    attachmentList: function () {
                        return HarvestPermits.getAttachmentList({id: moosePermit.id}).$promise;
                    }
                }
            }).result;
        };

        function ModalController($uibModalInstance, $translate, moosePermit, attachmentList) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.harvestPermitList = collectPermitNumbers(moosePermit);
                $ctrl.attachmentList = attachmentList;
            };

            $ctrl.close = function () {
                $uibModalInstance.dismiss('close');
            };

            $ctrl.downloadPermit = function (p) {
                FormPostService.submitFormUsingBlankTarget(HarvestPermitPdfUrl.get(p.permitNumber));
            };

            $ctrl.downloadAttachment = function (a) {
                FormPostService.submitFormUsingBlankTarget(HarvestPermitAttachmentUrl.get(moosePermit.id, a.id));
            };

            function collectPermitNumbers(permit) {
                var permitPrefix = $translate.instant('club.permit.permitName');
                var amendmentPermitPrefix = $translate.instant('club.permit.amendmentPermitName');

                var keys = _(permit.amendmentPermits)
                    .sort()
                    .map(function (amendmentPermitNumber) {
                        return {
                            text: amendmentPermitPrefix + ' ' + amendmentPermitNumber,
                            permitNumber: amendmentPermitNumber
                        };
                    }).value();

                keys.unshift({
                    text: permitPrefix + ' ' + permit.permitNumber,
                    permitNumber: permit.permitNumber
                });

                return keys;
            }
        }
    });
