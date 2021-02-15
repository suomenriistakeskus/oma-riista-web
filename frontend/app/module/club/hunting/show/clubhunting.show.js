'use strict';

angular.module('app.clubhunting.show', [])

    .controller('ClubDiaryEntryShowController', function ($scope, $translate, dialogs, ActiveRoleService,
                                                          DiaryImageService, AuthenticationService,
                                                          diaryEntry, groupStatus, isRejected, parameters) {

        $scope.diaryEntry = diaryEntry;
        $scope.getGameNameWithAmount = parameters.$getGameNameWithAmount;
        $scope.getUrl = DiaryImageService.getUrl;

        var canEdit = groupStatus.canEditDiaryEntry;
        var isModerator = ActiveRoleService.isModerator();
        var isObservationWithinDeerHunting = diaryEntry.isObservationWithinDeerHunting();

        $scope.showEdit = canEdit
            && diaryEntry.huntingDayId
            && (!diaryEntry.updateableOnlyByCarnivoreAuthority || AuthenticationService.isCarnivoreAuthority())
            && !isObservationWithinDeerHunting;
        $scope.showAccept = canEdit && !diaryEntry.huntingDayId && !isObservationWithinDeerHunting;
        $scope.showReject = canEdit && !isRejected;
        $scope.showGeolocationEditButton = isModerator && groupStatus.fromMooseDataCard;
        $scope.showAcceptDeerObservation = isObservationWithinDeerHunting && isRejected;

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

        $scope.fixGeoLocation = function () {
            $scope.$close('fixGeoLocation');
        };

        $scope.accept = function () {
            $scope.$close('accept');
        };

        $scope.acceptDeerObservation = function () {
            $scope.$close('acceptDeerObservation');
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

    .service('ClubHuntingEntryShowService', function ($q, $state, ActiveRoleService, ClubGroupDiary, ClubGroups,
                                                      ClubHuntingActiveEntry, DiaryEntryRemoveModal,
                                                      FormSidebarService, GameDiaryParameters) {
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
            var clubId = parameters.clubId;
            var groupId = parameters.groupId;

            return {
                diaryEntry: _.constant(entry),
                groupStatus: function () {
                    return ClubGroups.status({
                        id: groupId,
                        clubId: clubId
                    }).$promise;
                },
                isRejected: function () {
                    return ClubGroupDiary.listRejected({
                        id: groupId
                    }).$promise.then(function (rejected) {
                        return rejected[entry.type].indexOf(entry.id) >= 0;
                    });
                }
            };
        }

        var formSidebar = FormSidebarService.create(modalOptions, null, parametersToResolve);

        this.showDiaryEntry = function (clubId, groupId, diaryEntry) {
            return formSidebar.show({
                // ID parameter is used to prevent opening same entry twice
                id: diaryEntry.type + ':' + diaryEntry.id,
                diaryEntry: diaryEntry,
                clubId: clubId,
                groupId: groupId

            }).then(function (resultCode) {
                switch (resultCode) {
                    case 'accept':
                        ClubHuntingActiveEntry.acceptDiaryEntry(clubId, groupId, diaryEntry);
                        $state.go('club.hunting.add');
                        return $q.reject('ignore');

                    case 'acceptDeerObservation':
                        return ClubGroupDiary.acceptDeerObservation({
                            id: groupId,
                            observationId: diaryEntry.id
                        }).$promise;

                    case 'edit':
                        ClubHuntingActiveEntry.editDiaryEntry(clubId, groupId, diaryEntry);
                        $state.go('club.hunting.add');
                        return $q.reject('ignore');

                    case 'fixGeoLocation':
                        ClubHuntingActiveEntry.fixGeoLocationOfDiaryEntry(clubId, groupId, diaryEntry);
                        $state.go('club.hunting.fixgeolocation');
                        return $q.reject('ignore');

                    case 'reject':
                        ClubHuntingActiveEntry.clearSelectedItem();

                        return ClubGroupDiary.rejectEntry({
                            id: groupId,
                            entryId: diaryEntry.id,
                            type: diaryEntry.type
                        }).$promise;

                    case 'delete':
                        ClubHuntingActiveEntry.clearSelectedItem();

                        return DiaryEntryRemoveModal.openModal(diaryEntry);

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
                        ClubHuntingActiveEntry.clearSelectedItem();
                }

                return $q.reject(resultCode);
            });
        };
    });
