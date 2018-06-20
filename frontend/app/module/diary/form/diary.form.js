'use strict';

angular.module('app.diary.form', [])
    .service('DiaryEntryService', function ($q, $state, ActiveRoleService) {

        this.createHarvest = function () {
            if (ActiveRoleService.isModerator()) {
                return $state.go('jht.harvestreport.create');

            } else {
                return $state.go('profile.diary.addHarvest');
            }
        };

        this.createObservation = function () {
            return $state.go('profile.diary.addObservation');
        };

        this.createSrvaEvent = function () {
            return $state.go('profile.diary.addSrva');
        };

        this.createHarvestForPermit = function (permitNumber, gameSpeciesCode) {
            return $state.go('profile.diary.addHarvest', {
                id: 'me',
                gameSpeciesCode: gameSpeciesCode,
                permitNumber: permitNumber
            });
        };

        function transitionToDiaryEntryEditState(diaryEntry, opts) {
            if (diaryEntry.isHarvest()) {
                if (ActiveRoleService.isModerator()) {
                    delete opts.id;
                    return $state.go('jht.harvestreport.edit', opts);

                } else {
                    return $state.go('profile.diary.editHarvest', opts);
                }

            } else if (diaryEntry.isObservation()) {
                return $state.go('profile.diary.editObservation', opts);

            } else if (diaryEntry.isSrva()) {
                return $state.go('profile.diary.editSrva', opts);
            }

            return $q.reject();
        }

        this.edit = function (diaryEntry) {
            return transitionToDiaryEntryEditState(diaryEntry, {
                id: 'me',
                entryId: diaryEntry.id
            });
        };

        this.copy = function (diaryEntry) {
            return transitionToDiaryEntryEditState(diaryEntry, {
                id: 'me',
                entryId: diaryEntry.id,
                copy: 'true'
            });
        };
    })

    .service('DiaryEntryFormSidebar', function ($q, offCanvasStack,
                                                ObservationFieldsMetadata,
                                                GameDiaryParameters, GameDiarySrvaParameters) {

        this.openDiaryEntryForm = function (diaryEntry) {
            var controller, templateUrl;
            var resolve = {
                entry: _.constant(diaryEntry),
                parameters: function () {
                    return diaryEntry.isSrva()
                        ? GameDiarySrvaParameters.query().$promise
                        : GameDiaryParameters.query().$promise;
                }
            };

            if (diaryEntry.isHarvest()) {
                templateUrl = 'diary/harvest/harvest-form.html';
                controller = 'HarvestFormController';

            } else if (diaryEntry.isObservation()) {
                templateUrl = 'diary/observation/observation-form.html';
                controller = 'ObservationFormController';

                angular.extend(resolve, {
                    fieldMetadataForObservationSpecies: function () {
                        if (diaryEntry.gameSpeciesCode) {
                            return ObservationFieldsMetadata.forSpecies({gameSpeciesCode: diaryEntry.gameSpeciesCode}).$promise;
                        }
                        return null;
                    }
                });
            } else if (diaryEntry.isSrva()) {
                templateUrl = 'diary/srva/srva-form.html';
                controller = 'SrvaFormController';

            } else {
                return $q.reject('unknown entry type');
            }

            return offCanvasStack.open({
                controller: controller,
                templateUrl: templateUrl,
                largeDialog: false,
                resolve: resolve
            }).result;
        };
    })

    .controller('OpenDiaryEntryFormController', function ($history, $scope, $state, ActiveRoleService,
                                                          DiaryEntryFormSidebar, DiaryListViewState,
                                                          MapState, MapDefaults,
                                                          NotificationService, WGS84, entry) {
        $scope.entry = entry;
        $scope.mapEvents = MapDefaults.getMapBroadcastEvents(['click']);
        $scope.mapDefaults = MapDefaults.create();
        $scope.geoCenter = MapState.toGeoLocationOrDefault(entry.geoLocation);

        var defaultReturnState = ActiveRoleService.isModerator() ? 'jht.home' : 'profile.diary';

        DiaryEntryFormSidebar.openDiaryEntryForm(entry)
            .then(function (diaryEntry) {
                NotificationService.showDefaultSuccess();
                DiaryListViewState.selectedDiaryEntry = diaryEntry;

                // Return to previous active state
                $history.back().catch(function (err) {
                    $state.go(defaultReturnState);
                });
            })
            .catch(function (err) {
                if (angular.isString(err)) {
                    // Dialog was dismissed by browser navigation
                    if (err.indexOf('back') !== -1) {
                        return;
                    }

                    if (err.indexOf('escape') === -1 && err.indexOf('cancel') === -1) {
                        // Error not caused by dismissing the dialog
                        NotificationService.showDefaultFailure();
                    }
                } else {
                    NotificationService.showDefaultFailure();
                }

                $history.back().catch(function (error) {
                    $state.go(defaultReturnState);
                });
            });
    });
