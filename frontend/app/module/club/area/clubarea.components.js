'use strict';

angular.module('app.clubarea.components', [])
    .component('rClubAreaCategorySelection', {
        templateUrl: 'club/area/r-club-area-category-selection.html',
        bindings: {
            activeTabIndex: '<'
        },
        controller: function ($state, $stateParams) {
            var $ctrl = this;

            var clubId = $stateParams.id;

            $ctrl.transitionToAreaList = function () {
                $state.go('club.area.list', { clubId: clubId });
            };

            $ctrl.transitionToAreaProposals = function () {
                $state.go('club.area.proposals', { clubId: clubId });
            };
        }
    });
