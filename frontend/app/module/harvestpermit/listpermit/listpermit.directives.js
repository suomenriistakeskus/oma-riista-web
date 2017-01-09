'use strict';

angular.module('app.listpermit.directives', [])
    .directive('rViewHarvestButtons', function ($state, Harvest, DiaryEntryService, DiaryListViewState, ActiveRoleService, NotificationService) {
        return {
            replace: false,
            restrict: 'A',
            scope: {harvest: '=rViewHarvestButtons', showModeratorButtons: '='},
            link: function (scope, element, attrs) {
                scope.moderator = ActiveRoleService.isModerator();

                scope.viewHarvest = function () {
                    scope.harvest._showThisHarvest = !scope.harvest._showThisHarvest;
                };

                scope.createCopy = function () {
                    // Harvests in scope are not the Harvest js-objects, but custom  objects returned with permit.
                    DiaryEntryService.copy(new Harvest(scope.harvest));
                };

                scope.delete = function () {
                    var harvestId = scope.harvest.id;
                    Harvest.delete({id: harvestId}).$promise.then(function () {
                        // Deleted harvest might be referenced by DiaryListViewState, so,
                        // make DiaryListViewState forget if deleted is selected
                        var selectedEntry = DiaryListViewState.selectedDiaryEntry;
                        if (selectedEntry && selectedEntry.id === harvestId) {
                            DiaryListViewState.selectedDiaryEntry = null;
                        }

                        NotificationService.showDefaultSuccess();
                        $state.reload();
                    }, NotificationService.showDefaultFailure);
                };
            },
            templateUrl: 'harvestpermit/listpermit/r-view-harvest-buttons.html'
        };
    })

    .directive('rViewHarvestInline', function (MapDefaults, Harvest, DiaryEntryService) {
        return {
            replace: false,
            restrict: 'A',
            scope: {harvestStub: '=rViewHarvestInline'},
            link: function (scope, element, attrs) {
                scope.mapDefaults = MapDefaults.create();

                scope.$watch('harvestStub._showThisHarvest', function (newVal, oldVal) {
                    if (newVal && newVal !== oldVal) {
                        Harvest.get({id: scope.harvestStub.id}).$promise
                            .then(function (data) {
                                scope.harvest = data;
                            });
                    }
                });

                scope.editHarvest = function () {
                    DiaryEntryService.edit(scope.harvest);
                };

            },
            templateUrl: 'harvestpermit/listpermit/r-view-harvest-inline.html'
        };
    })
;
