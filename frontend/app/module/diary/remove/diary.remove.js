'use strict';

angular.module('app.diary.remove', [])
    .service('DiaryEntryRemoveModal', function ($uibModal) {
        this.openModal = function (diaryEntry) {
            return $uibModal.open({
                controller: 'DiaryEntryRemoveModalController',
                controllerAs: '$ctrl',
                bindToController: true,
                templateUrl: 'diary/remove/diary-remove.html',
                resolve: {
                    diaryEntry: _.constant(diaryEntry)
                }
            }).result;
        };
    })

    .controller('DiaryEntryRemoveModalController', function ($q, $uibModalInstance, $state,
                                                             Harvest, Observation, Srva,
                                                             DiaryListViewState, diaryEntry) {
        var $ctrl = this;

        $ctrl.diaryEntryType = diaryEntry.type.toLowerCase();

        $ctrl.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };

        $ctrl.remove = function () {
            removeDiaryEntry().then(function () {
                DiaryListViewState.selectedDiaryEntry = null;

                $uibModalInstance.close();

                $state.reload();
            });
        };

        function removeDiaryEntry() {
            var id = diaryEntry.id;

            if (diaryEntry.isHarvest()) {
                return Harvest.delete({id: id}).$promise;
            } else if (diaryEntry.isObservation()) {
                return Observation.delete({id: id}).$promise;
            } else if (diaryEntry.isSrva()) {
                return Srva.delete({id: id}).$promise;
            }

            return $q.reject('unknown diary type');
        }
    });
