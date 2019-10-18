'use strict';

angular.module('app.diary.sidebar', [])
    .service('DiaryEntrySidebar', function ($q, offCanvasStack, Harvest, Observation, Srva,
                                            HarvestFieldsService) {
        this.showSidebar = function (diaryEntry, largeDialog) {
            return offCanvasStack.open({
                controller: 'DiaryEntrySidebarController',
                templateUrl: 'diary/sidebar/diary-sidebar.html',
                largeDialog: !!largeDialog,
                resolve: {
                    entry: function () {
                        if (diaryEntry.isHarvest()) {
                            return Harvest.get({id: diaryEntry.id}).$promise;
                        } else if (diaryEntry.isObservation()) {
                            return Observation.get({id: diaryEntry.id}).$promise;
                        } else if (diaryEntry.isSrva()) {
                            return Srva.get({id: diaryEntry.id}).$promise;
                        }

                        return $q.reject('invalid diaryEntry.type');
                    },
                    computedFields: function () {
                        return diaryEntry.isHarvest()
                            ? HarvestFieldsService.getForPersistedHarvest(diaryEntry.id)
                            : $q.resolve(null);
                    }
                }
            });
        };
    })

    .controller('DiaryEntrySidebarController', function ($scope, $state,
                                                         ActiveRoleService,
                                                         DiaryEntryRemoveModal, DiaryEntryService, DiaryImageService,
                                                         entry, computedFields) {
        $scope.entry = entry;
        $scope.getUrl = DiaryImageService.getUrl;
        $scope.moderator = ActiveRoleService.isModerator();

        if (computedFields) {
            $scope.season = computedFields.season;
            $scope.harvestArea = computedFields.harvestArea;
            $scope.rhy = computedFields.rhy;
            $scope.municipalityName = computedFields.municipalityName;
        }

        $scope.edit = function () {
            $scope.$dismiss('ignore');
            DiaryEntryService.edit($scope.entry);
        };

        $scope.remove = function () {
            DiaryEntryRemoveModal.openModal($scope.entry).then(function () {
                $scope.$close();
                $state.reload();
            });
        };

        $scope.cancel = function () {
            $scope.$dismiss('cancel');
        };

        $scope.carnivoreObserverPresent = function () {
            var e = $scope.entry;
            return e.observerName || e.observerPhoneNumber;
        };

        $scope.getSrvaMethodsForSidebar = function (methods) {
            return _.chain(methods)
                .filter({isChecked: true})
                .map('name')
                .value();
        };

        $scope.showSrvaMethodsInSidebar = function (methods) {
            return _.some(methods, 'isChecked');
        };

        function isDefined(fieldName) {
            return angular.isDefined(entry[fieldName]) && entry[fieldName] !== null;
        }

        $scope.hasExtraFields = function () {
            return isDefined('huntingMethod') ||
                isDefined('reportedWithPhoneCall') ||
                isDefined('feedingPlace') ||
                isDefined('taigaBeanGoose') ||
                isDefined('huntingAreaType') ||
                isDefined('huntingAreaSize') ||
                isDefined('huntingParty');
        };
    });
