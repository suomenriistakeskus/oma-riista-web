'use strict';

angular.module('app.diary.srva', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.diary.addSrva', {
                url: '/add_srva?gameSpeciesCode',
                wideLayout: true,
                views: {
                    '@profile': {
                        templateUrl: 'diary/srva/edit-srva.html',
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
                            gameSpeciesCode: $stateParams.gameSpeciesCode
                        });
                    }
                }
            })

            .state('profile.diary.editSrva', {
                url: '/edit_srva?entryId',
                wideLayout: true,
                views: {
                    '@profile': {
                        templateUrl: 'diary/srva/edit-srva.html',
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

    .factory('Srva', function (DiaryEntryRepositoryFactory, DiaryEntryType, WGS84) {
        var Srva = DiaryEntryRepositoryFactory.create('api/v1/srva/srvaevent');

        /**
         * Create a new Srva object (not yet persisted).
         *
         * @param {{
         *   gameSpeciesCode : preselected official code of game species
         *   geoLocation: predefined geolocation
         * }} opts Options to populate Observation object with
         */
        Srva.createTransient = function (opts) {
            var geoLocation = opts.geoLocation;

            if (geoLocation) {
                var etrs = WGS84.toETRS(geoLocation.lat, geoLocation.lng);

                geoLocation = {
                    latitude: etrs.lat,
                    longitude: etrs.lng,
                    zoom: geoLocation.zoom
                };
            }

            var gameSpeciesCode = opts.gameSpeciesCode ? _.parseInt(opts.gameSpeciesCode) : null;

            return new Srva({
                id: null,
                type: DiaryEntryType.srva,
                gameSpeciesCode: gameSpeciesCode,
                geoLocation: geoLocation || {},
                canEdit: true
            });
        };

        return Srva;
    })

    .factory('GameDiarySrvaParameters', function ($resource, $http, CacheFactory,
                                                  SpeciesNameService, SrvaOtherSpeciesService) {
        function appendTransform(defaults, transform) {
            defaults = angular.isArray(defaults) ? defaults : [defaults];
            return defaults.concat(transform);
        }

        return $resource('api/v1/srva/parameters', null, {
            query: {
                method: 'GET',
                isArray: false,
                cache: CacheFactory.get('diarySrvaParameterCache'),
                transformResponse: appendTransform($http.defaults.transformResponse, function (data, headersGetter, status) {
                    if (status === 200 && angular.isObject(data)) {
                        SpeciesNameService.addSpeciesNameFunctions(data);
                        SrvaOtherSpeciesService.addOtherSpecies(data.species);
                        return data;
                    } else {
                        return data || {};
                    }
                })
            }
        });
    })

    .service('SrvaOtherSpeciesService', function (DiaryEntryType) {
        var otherSpeciesCode = 0;
        var otherSpecies = {"code": otherSpeciesCode, "name": {"fi": "muu", "sv": "andra", "en": "other"}};

        this.getOtherSpeciesCode = function () {
            return otherSpeciesCode;
        };

        this.addOtherSpecies = function (species) {
            if (_.isArray(species) && !_.find(species, {code: otherSpeciesCode})) {
                species.push(otherSpecies);
            }
            return species;
        };

        this.replaceNullsWithOtherSpeciesCodeInEntries = function (entries) {
            entries.forEach(function (entry) {
                if (!entry.gameSpeciesCode && entry.type === DiaryEntryType.srva) {
                    entry.gameSpeciesCode = otherSpeciesCode;
                }
            });
            return entries;
        };

        this.replaceNullWithOtherSpeciesCode = function (entry) {
            if (!entry.gameSpeciesCode && entry.type === DiaryEntryType.srva) {
                entry.gameSpeciesCode = otherSpeciesCode;
            }
            return entry;
        };

        this.replaceOtherSpeciesCodeWithNull = function (entry) {
            if (entry.gameSpeciesCode === otherSpeciesCode && entry.type === DiaryEntryType.srva) {
                entry.gameSpeciesCode = null;
            }
            return entry;
        };
    })

    .controller('SrvaFormController', function ($filter, $scope, $translate, ActiveRoleService,
                                                DiaryEntryService, DiaryImageService, DiaryEntrySpecimenFormService,
                                                entry, parameters, SrvaOtherSpeciesService) {
        $scope.srvaEntry = entry;

        $scope.parameters = parameters;
        $scope.srvaSpecies = parameters.species;
        $scope.srvaEvents = parameters.events;
        $scope.getGameName = parameters.$getGameName;

        $scope.getUrl = DiaryImageService.getUrl;

        $scope.maxSpecimenCount = DiaryEntrySpecimenFormService.getMaxSpecimenCountForObservation();

        $scope.viewState = {
            date: null,
            time: null
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
            DiaryEntrySpecimenFormService.editSpecimen($scope.srvaEntry, parameters, {
                age: false,
                gender: false
            });
        };

        $scope.image = function (uuid) {
            DiaryImageService.openUploadDialog($scope.srvaEntry, uuid, true);
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
    });
