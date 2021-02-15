'use strict';

angular.module('app.diary.sidebar', [])

    .component('rShowDiarySpeciesTitle', {
        templateUrl: 'diary/sidebar/show-diary-species-title.html',
        bindings: {
            entry: '<'
        }
    })

    .component('rShowDiaryDescriptionAndPictures', {
        templateUrl: 'diary/sidebar/show-diary-description-and-pictures.html',
        bindings: {
            entry: '<'
        },
        controller: function (DiaryImageService) {
            var $ctrl = this;
            $ctrl.getUrl =  DiaryImageService.getUrl;
        }
    })

    .component('rShowHarvestEntry', {
        templateUrl: 'diary/sidebar/show-harvest-entry.html',
        bindings: {
            entry: '<',
            computedFields: '<'
        },
        controller: function() {
            var $ctrl = this;

            $ctrl.isDefined = function(fieldName) {
                return !_.isNil($ctrl.entry[fieldName]);
            };

            $ctrl.hasExtraFields = function () {
                return $ctrl.isDefined('huntingMethod') ||
                    $ctrl.isDefined('reportedWithPhoneCall') ||
                    $ctrl.isDefined('feedingPlace') ||
                    $ctrl.isDefined('taigaBeanGoose') ||
                    $ctrl.isDefined('huntingAreaType') ||
                    $ctrl.isDefined('huntingAreaSize') ||
                    $ctrl.isDefined('huntingParty');
            };

        }
    })

    .component('rShowObservationEntry', {
        templateUrl: 'diary/sidebar/show-observation-entry.html',
        bindings: {
            entry: '<'
        },
        controller: function (ObservationFieldsMetadata, ObservationCategory) {
            var $ctrl = this;

            $ctrl.isDefined = function(fieldName) {
                return !_.isNil($ctrl.entry[fieldName]);
            };

            $ctrl.isWithinDeerHunting = function () {
                return ObservationCategory.isWithinDeerHunting($ctrl.entry.observationCategory);
            };

            $ctrl.isWithinMooseHunting = function () {
                return ObservationCategory.isWithinMooseHunting($ctrl.entry.observationCategory);
            };

            $ctrl.carnivoreObserverPresent = function () {
                var e = $ctrl.entry;
                return e.observerName || e.observerPhoneNumber;
            };

        }
    })

    .component('rShowSrvaEntry', {
        templateUrl: 'diary/sidebar/show-srva-entry.html',
        bindings: {
            entry: '<'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.showSrvaMethodsInSidebar = function (methods) {
                return _.some(methods, 'isChecked');
            };

            $ctrl.getSrvaMethodsForSidebar = function (methods) {
                return _.chain(methods)
                    .filter({isChecked: true})
                    .map('name')
                    .value();
            };

        }
    })

    .component('rHarvestDeerHuntingType', {
        templateUrl: 'diary/sidebar/show-deer-hunting-type.html',
        bindings: {
            entry: '<'
        }
    })

    .service('DiaryEntrySidebar', function ($q, Harvest, HarvestFieldsService, Observation, Srva, offCanvasStack) {
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

    .controller('DiaryEntrySidebarController', function ($scope, $state, ActiveRoleService,
                                                         DiaryEntryRemoveModal, DiaryEntryService,
                                                         computedFields, entry) {
        $scope.entry = entry;
        $scope.computedFields = computedFields;
        $scope.moderator = ActiveRoleService.isModerator();

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

    });
