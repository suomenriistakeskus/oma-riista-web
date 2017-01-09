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
    .factory('ClubPermits', function ($resource) {
        return $resource('/api/v1/club/:clubId/permit', {'clubId': '@clubId'}, {
            'query': {
                method: 'GET',
                params: {year: '@year', species: '@species'},
                isArray: true
            },
            'get': {
                method: 'GET',
                url: '/api/v1/club/:clubId/permit/:permitId',
                params: {'clubId': '@clubId', 'permitId': '@permitId'}
            },
            'huntingYears': {
                method: 'GET',
                url: '/api/v1/club/:clubId/permit/huntingyears',
                isArray: true
            },
            'clubLeaders': {
                method: 'GET',
                url: '/api/v1/harvestpermit/moosepermit/:permitId/leaders',
                isArray: true
            },
            'updateAllocations': {
                method: 'POST',
                url: '/api/v1/harvestpermit/moosepermit/allocation/:permitId/:gameSpeciesCode',
                params: {'permitId': '@permitId', 'gameSpeciesCode': '@gameSpeciesCode'}
            },
            'noHarvests': {
                method: 'POST',
                url: '/api/v1/harvestpermit/moosepermit/:permitId/species/:speciesCode/harvestreport/noharvests',
                params: {"permitId": "@permitId", "speciesCode": "@speciesCode"}
            },
            'removeHarvestReport': {
                method: 'DELETE',
                url: '/api/v1/harvestpermit/moosepermit/:permitId/species/:speciesCode/harvestreport',
                params: {'permitId': '@permitId', 'speciesCode': '@speciesCode'}
            }
        });
    })
    .service('MoosePermitPdfUrl', function () {
        this.get = function (permitNumber) {
            return '/api/v1/harvestpermit/moosepermit/pdf?permitNumber=' + permitNumber;
        };
    })
    .service('LukeUrlService', function ($httpParamSerializer) {
        this.get = function (permitId, clubId, lukeOrg, lukePresentation, file) {
            var url = '/api/v1/harvestpermit/moosepermit/' + permitId + '/luke-reports';
            return url + '?' + $httpParamSerializer({
                    clubId: clubId,
                    org: lukeOrg,
                    presentation: lukePresentation,
                    fileName: file
                });
        };
    })
    .service('MoosePermitLeadersService', function (ClubPermits, offCanvasStack) {
        this.showLeaders = function (params) {
            return offCanvasStack.open({
                templateUrl: 'harvestpermit/moosepermit/permit-leaders.html',
                largeDialog: true,
                resolve: {
                    leaders: function () {
                        return ClubPermits.clubLeaders({
                            permitId: params.id,
                            huntingYear: params.huntingYear,
                            gameSpeciesCode: params.gameSpeciesCode
                        }).$promise;
                    }
                },
                controller: 'MoosePermitLeadersController'
            }).resolve;
        };
    })
    .service('MoosePermitCounterService', function () {
        var countHarvestsBy = function (permit) {
            return function (key) {
                if (key === 'adult') {
                    return _.sum(permit.harvestCounts, 'adultMales') +
                        _.sum(permit.harvestCounts, 'adultFemales');
                }
                if (key === 'young') {
                    return _.sum(permit.harvestCounts, 'youngMales') +
                        _.sum(permit.harvestCounts, 'youngFemales');
                }
                return _.sum(permit.harvestCounts, key);
            };
        };
        this.createCountHarvestsBy = function (permit) {
            return countHarvestsBy(permit);
        };
    })
;
