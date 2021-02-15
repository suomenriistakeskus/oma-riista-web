'use strict';

angular.module('app.club.harvestsummary', [])
    .constant('GameCategory', {
        'FOWL': 1,
        'GAME_MAMMAL': 2,
        'UNPROTECTED': 3
    })

    .controller('ClubHarvestSummaryController',
        function ($state, $stateParams, $translate,
                  GameCategory, club, summary, year) {
            var $ctrl = this;

            $ctrl.$onInit = function() {
                $ctrl.club = club;

                $ctrl.mammalSummary = _.filter(summary.items, ['species.categoryId', GameCategory.GAME_MAMMAL]);
                $ctrl.fowlSummary = _.filter(summary.items, ['species.categoryId', GameCategory.FOWL]);
                $ctrl.unprotectedSummary = _.filter(summary.items, ['species.categoryId', GameCategory.UNPROTECTED]);

                $ctrl.year= year;
                $ctrl.years = _.range(2014, new Date().getFullYear() + 1);

                $ctrl.yearChanged = function () {
                    var params = angular.copy($stateParams);
                    params.year = $ctrl.year;

                    $state.transitionTo($state.current, params, {reload: true, inherit: false, notify: true});
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
