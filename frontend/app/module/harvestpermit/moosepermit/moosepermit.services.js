'use strict';

angular.module('app.moosepermit.services', [])

    .service('MoosePermitSelection', function () {
        var self = this;

        self.permitId = null;

        this.updateSelectedPermitId = function (stateParams) {
            self.permitId = _.parseInt(stateParams.permitId);
            return self.permitId;
        };
    })

    .factory('MoosePermits', function ($http, $resource) {
        var apiPrefix = '/api/v1/moosepermit';

        return $resource(apiPrefix + '/:permitId', {permitId: '@permitId'}, {
            lukeReportParams: {
                method: 'GET',
                url: apiPrefix + '/lukereportparams'
            },
            query: {
                method: 'GET',
                isArray: true,
                params: {year: '@year', personId: '@personId', species: '@species'}
            },
            get: {
                method: 'GET',
                url: apiPrefix + '/:permitId',
                params: {permitId: '@permitId'}
            },
            getRhyCode: {
                method: 'GET',
                url: apiPrefix + '/:permitId/rhy',
                params: {permitId: '@permitId'}
            },
            listTodos: {
                method: 'GET',
                url: apiPrefix + '/:permitId/todo/:speciesCode',
                params: {permitId: '@permitId', speciesCode: '@speciesCode'}
            },
            partnerAreaFeatures: {
                method: 'GET',
                url: apiPrefix + '/:permitId/map',
                params: {'permitId': '@permitId'}
            },
            clubHuntingLeaders: {
                method: 'GET',
                isArray: true,
                url: apiPrefix + '/:permitId/leaders'
            },
            rhyStatistics: {
                method: 'GET',
                isArray: true,
                url: apiPrefix + '/:permitId/rhystatistics/:speciesCode',
                params: {permitId: '@permitId', speciesCode: '@speciesCode'}
            },
            updateAllocations: {
                method: 'POST',
                url: apiPrefix + '/:permitId/allocation/:gameSpeciesCode',
                params: {permitId: '@permitId', gameSpeciesCode: '@gameSpeciesCode'}
            },
            getClubHuntingSummariesForModeration: {
                method: 'GET',
                isArray: true,
                url: apiPrefix + '/:permitId/override/:speciesCode',
                params: {permitId: '@permitId', speciesCode: '@speciesCode'}
            },
            massOverrideClubHuntingSummaries: {
                method: 'POST',
                url: apiPrefix + '/:permitId/override/:speciesCode/:complete',
                params: {
                    permitId: '@permitId',
                    speciesCode: '@speciesCode',
                    complete: '@complete'
                }
            },
            deleteModeratorOverriddenClubHuntingSummaries: {
                method: 'DELETE',
                url: apiPrefix + '/:permitId/override/:speciesCode',
                params: {
                    permitId: '@permitId',
                    speciesCode: '@speciesCode'
                }
            }
        });
    })

    .factory('MoosePermitEndOfHuntingReport', function ($resource) {
        var apiPrefix = '/api/v1/harvestreport/moosepermit/:permitId/:speciesCode';

        return $resource(apiPrefix, {permitId: "@permitId", speciesCode: "@speciesCode"}, {
            noHarvests: {
                method: 'POST',
                url: apiPrefix + '/noharvests'
            }
        });
    })

    .service('MoosePermitPdfUrl', function () {
        this.get = function (permitNumber) {
            return '/api/v1/moosepermit/pdf?permitNumber=' + permitNumber;
        };
    })
    .service('LukeUrlService', function ($httpParamSerializer) {
        this.get = function (permitId, clubId, lukeOrg, lukePresentation, file) {
            var url = '/api/v1/moosepermit/' + permitId + '/luke-reports';

            return url + '?' + $httpParamSerializer({
                clubId: clubId,
                org: lukeOrg,
                presentation: lukePresentation,
                fileName: file
            });
        };
    })
    .service('MoosePermitLeadersService', function (MoosePermits, $uibModal) {
        this.showLeaders = function (params) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/moosepermit/permit-leaders.html',
                resolve: {
                    leaders: function () {
                        return MoosePermits.clubHuntingLeaders({
                            permitId: params.id,
                            huntingYear: params.huntingYear,
                            gameSpeciesCode: params.gameSpeciesCode
                        }).$promise;
                    }
                },
                size: 'lg',
                controllerAs: '$ctrl',
                controller: ModalController,
                bindToController: true
            }).result;
        };

        function ModalController($uibModalInstance, leaders) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.leaders = leaders;

                var previousClub = null;

                // Hide repetitive club names
                for (var i = 0; i < leaders.length; i++) {
                    var club = leaders[i].club;
                    if (previousClub && angular.equals(previousClub, club)) {
                        leaders[i].club = null;
                    }
                    previousClub = club;
                }
            };

            $ctrl.close = function () {
                $uibModalInstance.close();
            };
        }
    })
    .service('MoosePermitCounterService', function () {
        var countHarvestsBy = function (permit, key) {
            if (key === 'adult') {
                return _.sum(permit.harvestCounts, 'adultMales') + _.sum(permit.harvestCounts, 'adultFemales');
            }
            if (key === 'young') {
                return _.sum(permit.harvestCounts, 'youngMales') + _.sum(permit.harvestCounts, 'youngFemales');
            }
            return _.sum(permit.harvestCounts, key);
        };

        var countAllocateBy = function (allocations, key) {
            return _.sum(allocations, key);
        };

        var countSummaryForPartnersTable = function (permit, key) {
            return _.sum(permit.summaryForPartnersTable, key);
        };

        var countMaleAdultPercentage = function (func) {
            var m = func('adultMales');
            var f = func('adultFemales');
            return _.round(100 * m / (m + f)) || 0;
        };

        var countYoungPercentage = function (func) {
            var m = func('adultMales');
            var f = func('adultFemales');
            var y = func('young');
            return _.round(100 * y / (m + f + y)) || 0;
        };

        this.create = function (permit, allocations) {
            var harvestsBy = _.partial(countHarvestsBy, permit);
            var allocatedBy = _.partial(countAllocateBy, allocations);
            return {
                harvestsBy: harvestsBy,
                allocatedBy: allocatedBy,
                maleAdultPercentage: _.partial(countMaleAdultPercentage, allocatedBy),
                youngPercentage: _.partial(countYoungPercentage, allocatedBy),
                maleAdultHarvestPercentage: _.partial(countMaleAdultPercentage, harvestsBy),
                youngHarvestPercentage: _.partial(countYoungPercentage, harvestsBy),
                summaryForPartnersTable: _.partial(countSummaryForPartnersTable, permit)
            };
        };
    });
