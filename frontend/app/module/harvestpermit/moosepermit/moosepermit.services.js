'use strict';

angular.module('app.moosepermit.services', [])

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
    });
