'use strict';

angular.module('app.diary.controllers')
    .config(function ($stateProvider) {
        $stateProvider

            .state('profile.diary.addSrva', {
                url: '/add_srva?gameSpeciesCode',
                wideLayout: true,
                views: {
                    '@profile': {
                        templateUrl: 'diary/edit-srva.html',
                        controller: 'OpenDiaryEntryFormController'
                    }
                },
                params: {
                    id: 'me',
                    gameSpeciesCode: null
                },
                resolve: {
                    entry: function ($stateParams, DiaryListViewState, Srva) {
                        return Srva.createTransient({
                            gameSpeciesCode: $stateParams.gameSpeciesCode,
                            geoLocation: null
                        });
                    }
                }
            })
            .state('profile.diary.editSrva', {
                url: '/edit_srva?entryId',
                wideLayout: true,
                views: {
                    '@profile': {
                        templateUrl: 'diary/edit-srva.html',
                        controller: 'OpenDiaryEntryFormController'
                    }
                },
                params: {
                    id: 'me',
                    entryId: undefined
                },
                resolve: {
                    entry: function ($stateParams, MapState, Srva) {
                        return Srva.get({id: $stateParams.entryId}).$promise.then(function (srva) {
                            var zoom = MapState.getZoom();

                            if (zoom) {
                                srva.geoLocation.zoom = zoom;
                            }

                            return srva;
                        });
                    }
                }
            });

    })

    .controller('SrvaFormController', function ($filter, $scope, $translate, ActiveRoleService,
                                                DiaryEntryService, DiaryEntrySpecimenFormService,
                                                entry, relationship, parameters, SrvaOtherSpeciesService) {
        $scope.srvaEntry = entry;

        $scope.parameters = parameters;
        $scope.srvaSpecies = parameters.species;
        $scope.srvaEvents = parameters.events;
        $scope.getGameName = parameters.$getGameName;

        $scope.getUrl = DiaryEntryService.getUrl;

        $scope.maxSpecimenCount = DiaryEntrySpecimenFormService.getMaxSpecimenCountForObservation();

        $scope.isAuthor = !relationship || relationship.isAuthor;

        $scope.viewState = {
            fields: entry.fields
        };

        if (ActiveRoleService.isModerator() ||
            ActiveRoleService.isCoordinator() ||
            ActiveRoleService.isSrvaContactPerson()) {
            $scope.srvaEntry.canEdit = true;
        }

        $scope.getSrvaMethods = function () {
            if (!$scope.srvaEvents) {
                return [];
            }

            //Edit first time
            if ($scope.srvaEntry.methods) {
                return $scope.srvaEntry.methods;
            }

            var index = _.findIndex($scope.srvaEvents, function (event) {
                return event.name === $scope.srvaEntry.eventName;
            });

            $scope.srvaEntry.methods = $scope.srvaEvents[index] ? $scope.srvaEvents[index].methods : [];
            return $scope.srvaEntry.methods;
        };

        var findSrvaEvent = function (srvaEventName) {
            return _.find($scope.srvaEvents, function (event) {
                return event.name === srvaEventName;
            });
        };

        $scope.getSrvaEventTypes = function (srvaEventName) {
            var event = findSrvaEvent(srvaEventName);
            return event ? event.types : [];
        };


        $scope.getSrvaResults = function (srvaEventName) {
            var event = findSrvaEvent(srvaEventName);
            return event ? event.results : [];
        };

        $scope.showOtherMethodDescription = function () {
            var show = _.result(_.find($scope.srvaEntry.methods, {'name': "OTHER", 'isChecked': true}), 'isChecked');

            if (!show) {
                $scope.srvaEntry.otherMethodDescription = null;
            }

            return show;
        };

        $scope.showOtherTypeDescription = function () {
            var show = _.isEqual("OTHER", $scope.srvaEntry.eventType);

            if (!show) {
                $scope.srvaEntry.otherTypeDescription = null;
            }

            return show;
        };

        $scope.editSpecimen = function () {
            DiaryEntryService.editSpecimen($scope.srvaEntry, parameters, {
                age: false,
                gender: false
            });
        };

        $scope.image = function (uuid) {
            DiaryEntryService.image($scope.srvaEntry, uuid, true);
        };

        $scope.removeImage = function (uuid) {
            $scope.srvaEntry.imageIds = _.pull($scope.srvaEntry.imageIds, uuid);
        };

        $scope.cancel = function () {
            $scope.$dismiss('cancel');
        };

        var onSaveSuccess = function (entry) {
            $scope.$close(entry);
        };

        $scope.save = function () {
            $scope.srvaEntry.setDateAndTime($scope.viewState.date, $scope.viewState.time);
            $scope.srvaEntry.fields = angular.copy($scope.viewState.fields);

            $scope.srvaEntry.saveOrUpdate().then(onSaveSuccess);
        };

        // Convert timestamp
        if ($scope.srvaEntry.pointOfTime) {
            var dateFilter = $filter('date');
            $scope.viewState.date = dateFilter($scope.srvaEntry.pointOfTime, 'yyyy-MM-dd');
            $scope.viewState.time = dateFilter($scope.srvaEntry.pointOfTime, 'HH:mm');
        }

        $scope.isValidGameSpeciesCode = function (code) {
            return !!code || code === SrvaOtherSpeciesService.getOtherSpeciesCode();
        };

        $scope.isValidOtherSpeciesDescription = function () {
            return $scope.srvaEntry.gameSpeciesCode !== SrvaOtherSpeciesService.getOtherSpeciesCode() || $scope.srvaEntry.otherSpeciesDescription;
        };

        $scope.isValid = function () {
            return $scope.srvaEntry.eventName &&
                $scope.srvaEntry.eventType &&
                $scope.isValidGameSpeciesCode($scope.srvaEntry.gameSpeciesCode) &&
                $scope.isValidOtherSpeciesDescription() &&
                $scope.srvaEntry.geoLocation.latitude &&
                $scope.srvaEntry.totalSpecimenAmount;
        };

        $scope.resetEventSpecificFields = function () {
            $scope.srvaEntry.eventType = null;
            $scope.srvaEntry.eventResult = null;
            _.forEach($scope.srvaEntry.methods, function (method) {
                method.isChecked = false;
            });
            $scope.srvaEntry.methods = null;
        };

        $scope.resetOtherSpeciesDescription = function () {
            $scope.srvaEntry.otherSpeciesDescription = null;
        };

        $scope.$watch('srvaEntry.totalSpecimenAmount', function (newValue, oldValue) {
            if (newValue) {
                $scope.srvaEntry.totalSpecimenAmount = Math.min(newValue, $scope.maxSpecimenCount);
                DiaryEntrySpecimenFormService.setSpecimenCount($scope.srvaEntry, newValue);
            }
        });

    })
;
