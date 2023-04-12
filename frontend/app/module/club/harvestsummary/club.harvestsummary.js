'use strict';

angular.module('app.club.harvestsummary', [])
    .constant('GameCategory', {
        'FOWL': 1,
        'GAME_MAMMAL': 2,
        'UNPROTECTED': 3
    })

    .controller('ClubHarvestSummaryController',
        function ($state, $stateParams, $translate,
                  Helpers, GameCategory, Clubs, club) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.searchPerformed = false;
                $ctrl.club = club;
                var year = new Date().getFullYear().toString();
                $ctrl.beginDate = year + '-01-01';
                $ctrl.endDate = year + '-12-31';

                $ctrl.mammalSummary = [];
                $ctrl.fowlSummary = [];
                $ctrl.unprotectedSummary = [];

                $ctrl.searchSummary = function () {
                    Clubs.harvestSummary({clubId: club.id, begin: $ctrl.beginDate, end: $ctrl.endDate}).$promise
                        .then(function (summary) {
                            $ctrl.searchPerformed = true;

                            $ctrl.mammalSummary =
                                _.filter(summary.items, ['species.categoryId', GameCategory.GAME_MAMMAL]);
                            $ctrl.fowlSummary =
                                _.filter(summary.items, ['species.categoryId', GameCategory.FOWL]);
                            $ctrl.unprotectedSummary =
                                _.filter(summary.items, ['species.categoryId', GameCategory.UNPROTECTED]);
                        });

                };

                $ctrl.isIntervalIllegal = function () {
                    var b = Helpers.toMoment($ctrl.beginDate, 'YYYY-MM-DD');
                    var e = Helpers.toMoment($ctrl.endDate, 'YYYY-MM-DD');

                    if (b && e) {
                        // Maximum period is 1 year
                        return e.diff(b, 'years') >= 1;
                    }
                    return false;
                };
            };
        })

    .component('clubHarvestSummaryCategory', {
        templateUrl: 'club/harvestsummary/harvestsummary-category.html',
        controllerAs: '$ctrl',
        bindings: {
            items: '<'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.showAll = false;
            };

            $ctrl.toggleShowAll = function () {
                $ctrl.showAll = !$ctrl.showAll;
            };
        }
    })

    .component('clubHarvestSummarySpecies', {
        templateUrl: 'club/harvestsummary/harvestsummary-species.html',
        controllerAs: '$ctrl',
        bindings: {
            speciesCode: '<',
            amount: '<'
        }
    })

    .component('clubHarvestSummaryTable', {
        templateUrl: 'club/harvestsummary/harvestsummary-table.html',
        controllerAs: '$ctrl',
        bindings: {
            items: '<'
        }
    });
