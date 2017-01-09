'use strict';

angular.module('app.clubhunting.show', [])

    .controller('ClubDiaryEntryShowController', function ($scope, $state, $translate, dialogs, ActiveRoleService,
                                                          CheckPermitNumber, ClubHuntingEntryService, DiaryEntryService,
                                                          diaryEntry, groupStatus, parameters, isRejected) {

        $scope.diaryEntry = diaryEntry;
        $scope.getGameNameWithAmount = parameters.$getGameNameWithAmount;
        $scope.getUrl = DiaryEntryService.getUrl;

        var canEdit = groupStatus.canEditDiaryEntry;

        $scope.showEdit = canEdit && diaryEntry.huntingDayId;
        $scope.showAccept = canEdit && !diaryEntry.huntingDayId;
        $scope.showReject = canEdit && !isRejected;

        if (!diaryEntry.huntingDayId) {
            if (diaryEntry.isHarvest()) {
                if (isRejected) {
                    $scope.header = $translate.instant('club.hunting.harvestRejectedDialog.title');
                    $scope.msg = $translate.instant('club.hunting.harvestRejectedDialog.message');
                } else {
                    $scope.header = $translate.instant('club.hunting.harvestProposalDialog.title');
                    $scope.msg = $translate.instant('club.hunting.harvestProposalDialog.message');
                }

            } else if (diaryEntry.isObservation()) {
                if (isRejected) {
                    $scope.header = $translate.instant('club.hunting.observationRejectedDialog.title');
                    $scope.msg = $translate.instant('club.hunting.observationRejectedDialog.message');
                } else {
                    $scope.header = $translate.instant('club.hunting.observationProposalDialog.title');
                    $scope.msg = $translate.instant('club.hunting.observationProposalDialog.message');
                }
            }
        } else {
            if (diaryEntry.isHarvest()) {
                $scope.header = $translate.instant('gamediary.harvest');

            } else if (diaryEntry.isObservation()) {
                $scope.header = $translate.instant('gamediary.observation');
            }
            $scope.msg = '';
        }

        $scope.edit = function () {
            $scope.$close('edit');
        };

        $scope.accept = function () {
            $scope.$close('accept');
        };

        $scope.reject = function () {
            var dialogTitle = $translate.instant('global.dialog.confirmation.title');
            var dialogMessage = $translate.instant('global.dialog.confirmation.text');
            var dialog = dialogs.confirm(dialogTitle, dialogMessage);

            dialog.result.then(function () {
                $scope.$close('reject');
            });
        };

        $scope.remove = function () {
            $scope.$close('remove');
        };

        $scope.cancel = function () {
            $scope.$dismiss('cancel');
        };
    })

    .service('ClubHuntingEntryService', function ($q, $state, $translate,
                                                  Harvest, Observation,
                                                  FormSidebarService,
                                                  ClubGroups,
                                                  GameDiaryParameters,
                                                  DiaryEntryService) {
        var self = this;

        this.createHarvest = function (clubId, groupId, gameSpeciesCode) {
            $state.go('club.hunting.add.harvest', {
                gameSpeciesCode: gameSpeciesCode,
                clubId: clubId,
                groupId: groupId
            });
        };

        this.createObservation = function (clubId, groupId, gameSpeciesCode) {
            $state.go('club.hunting.add.observation', {
                gameSpeciesCode: gameSpeciesCode,
                clubId: clubId,
                groupId: groupId
            });
        };

        this.editHarvest = function (clubId, groupId, entryId) {
            $state.go('club.hunting.add.harvest', {
                clubId: clubId,
                groupId: groupId,
                entryId: entryId
            });
        };

        this.editObservation = function (clubId, groupId, entryId) {
            $state.go('club.hunting.add.observation', {
                clubId: clubId,
                groupId: groupId,
                entryId: entryId
            });
        };

        function editDiaryEntry(clubId, groupId, diaryEntry) {
            if (diaryEntry.isHarvest()) {
                self.editHarvest(clubId, groupId, diaryEntry.id);
            } else if (diaryEntry.isObservation()) {
                self.editObservation(clubId, groupId, diaryEntry.id);
            } else {
                console.log("Unknown diaryEntry", diaryEntry);
            }
        }

        var selectedItem = null;

        this.isItemSelected = function () {
            return selectedItem !== null;
        };

        this.showSelectedDiaryEntry = function (onSidebarOpen) {
            if (selectedItem) {
                self.showDiaryEntry(selectedItem.clubId, selectedItem.groupId, selectedItem.diaryEntry, onSidebarOpen)
                    .then(function () {
                        $state.reload();
                    });
                selectedItem = null;
            }
        };

        this.setSelectedDiaryEntry = function (clubId, groupId, diaryEntry) {
            selectedItem = {
                clubId: clubId,
                groupId: groupId,
                diaryEntry: diaryEntry
            };
        };

        var modalOptions = {
            controller: 'ClubDiaryEntryShowController',
            templateUrl: 'club/hunting/show/sidebar.html',
            largeDialog: false,
            resolve: {
                parameters: function () {
                    return GameDiaryParameters.query().$promise;
                }
            }
        };

        function parametersToResolve(parameters) {
            var entry = parameters.diaryEntry;
            var diaryEntryId = entry.id;
            var repository = entry.isHarvest() ? Harvest : entry.isObservation ? Observation : null;
            var clubId = parameters.clubId;
            var groupId = parameters.groupId;

            if (!diaryEntryId || !repository) {
                return $q.reject('missing diaryEntry');
            }

            return {
                diaryEntry: function () {
                    return repository.get({id: diaryEntryId}).$promise.then(parameters.onDiaryEntryLoaded);
                },
                groupStatus: function () {
                    return ClubGroups.status({
                        id: groupId,
                        clubId: clubId
                    }).$promise;
                },
                isRejected: function () {
                    return ClubGroups.listRejected({ clubId: clubId, id: groupId}).$promise.then(function(rejected) {
                        return rejected[entry.type].indexOf(diaryEntryId) >= 0;
                    });
                }
            };
        }

        var formSidebar = FormSidebarService.create(modalOptions, null, parametersToResolve);

        this.showDiaryEntry = function (clubId, groupId, diaryEntry, onSidebarOpen) {
            function onDiaryEntryLoaded(reloadedEntry) {
                selectedItem = {
                    clubId: clubId,
                    groupId: groupId,
                    diaryEntry: reloadedEntry
                };

                if (onSidebarOpen) {
                    onSidebarOpen(reloadedEntry);
                }

                return reloadedEntry;
            }

            return formSidebar.show({
                id: diaryEntry.type + ':' + diaryEntry.id,
                diaryEntry: diaryEntry,
                onDiaryEntryLoaded: onDiaryEntryLoaded,
                clubId: clubId,
                groupId: groupId

            }).then(function (resultCode) {
                switch (resultCode) {
                    case 'edit':
                        editDiaryEntry(clubId, groupId, diaryEntry);

                        return $q.reject('ignore');

                    case 'reject':
                        selectedItem = null;
                        return ClubGroups.rejectEntry({
                            clubId: clubId,
                            id: groupId,
                            entryId: diaryEntry.id,
                            type: diaryEntry.type
                        }).$promise;

                    case 'delete':
                        selectedItem = null;
                        return DiaryEntryService.openRemoveForm(diaryEntry);

                    default:
                        return $q.reject(resultCode);
                }
            }, function (resultCode) {
                switch (resultCode) {
                    case 'back':
                        break;

                    default:
                    case 'escape':
                    case 'cancel':
                        selectedItem = null;
                }

                return $q.reject(resultCode);
            });
        };
    });
