(function () {
    "use strict";

    angular.module('app.clubarea.list', [])
        .config(function ($stateProvider) {
            $stateProvider
                .state('club.area', {
                    abstract: true,
                    template: '<ui-view autoscroll="false"/>',
                    resolve: {
                        rhyBounds: function (MapBounds, club) {
                            return MapBounds.getRhyBounds(club.rhy.officialCode);
                        }
                    }
                })
                .state('club.area.list', {
                    url: '/area?{areaId:[0-9]{1,8}}',
                    templateUrl: 'club/area/club-areas.html',
                    controller: 'ClubAreaListController',
                    controllerAs: '$ctrl',
                    bindToController: true,
                    wideLayout: true,
                    reloadOnSearch: false,
                    params: {
                        areaId: {
                            value: null
                        }
                    },
                    resolve: {
                        selectedAreaId: function ($stateParams) {
                            return _.parseInt($stateParams.areaId);
                        },
                        preloadedHuntingYears: function (ClubAreaListService, clubId) {
                            return ClubAreaListService.listHuntingYears(clubId);
                        },
                        preselectedArea: function (ClubAreas, selectedAreaId) {
                            return selectedAreaId ? ClubAreas.get({id: selectedAreaId}).$promise : null;
                        },
                        showDeactive: function (preselectedArea) {
                            return preselectedArea && !preselectedArea.active;
                        },
                        huntingYear: function (preselectedArea, preloadedHuntingYears) {
                            if (preselectedArea) {
                                return preselectedArea.huntingYear;
                            }
                            var currentYear = new Date().getFullYear();
                            if (_.some(preloadedHuntingYears, {year: currentYear}) || _.isEmpty(preloadedHuntingYears)) {
                                return currentYear;
                            }

                            return _.max(_.map(preloadedHuntingYears, 'year'));
                        },
                        pois: function (ClubPois, club) {
                            return ClubPois.listPois({id: club.id}).$promise;
                        }
                    }
                })
                .state('club.area.areapoi', {
                    url: '/areapoi?{areaId:[0-9]{1,8}}',
                    templateUrl: 'club/area/club-area-pois.html',
                    controller: 'ClubAreaPoiController',
                    controllerAs: '$ctrl',
                    bindToController: true,
                    wideLayout: true,
                    reloadOnSearch: false,
                    params: {
                        areaId: {
                            value: null
                        }
                    },
                    resolve: {
                        selectedAreaId: function ($stateParams) {
                            return _.parseInt($stateParams.areaId);
                        },
                        area: function (ClubAreas, selectedAreaId) {
                            return selectedAreaId ? ClubAreas.get({id: selectedAreaId}).$promise : null;
                        },
                        pois: function (ClubPois, club) {
                            return ClubPois.listPois({id: club.id}).$promise;
                        },
                        featureCollection: function (ClubAreas, selectedAreaId) {
                            return ClubAreas.combinedFeatures({id: selectedAreaId}).$promise;
                        },
                        connectedPois: function (ClubAreas, selectedAreaId) {
                            return ClubAreas.listPois({id: selectedAreaId}).$promise;
                        }
                    }
                });
        })

        .factory('ClubAreas', function ($resource) {
            var apiPrefix = 'api/v1/clubarea/:id';

            return $resource(apiPrefix, {"id": "@id"}, {
                update: {method: 'PUT'},
                huntingYears: {
                    method: 'GET',
                    url: apiPrefix + '/huntingyears',
                    isArray: true
                },
                getFeatures: {
                    method: 'GET',
                    url: apiPrefix + '/features'
                },
                saveFeatures: {
                    method: 'PUT',
                    url: apiPrefix + '/features'
                },
                getZoneStatus: {
                    method: 'GET',
                    url: apiPrefix + '/zone/status'
                },
                combinedFeatures: {
                    method: 'GET',
                    url: apiPrefix + '/combinedFeatures'
                },
                activate: {
                    method: 'POST',
                    url: apiPrefix + '/activate'
                },
                deactivate: {
                    method: 'POST',
                    url: apiPrefix + '/deactivate'
                },
                copy: {
                    method: 'POST',
                    url: apiPrefix + '/copy'
                },
                importFromPersonalArea: {
                    method: 'POST',
                    url: apiPrefix + '/import-personal/:personalAreaId',
                    params: {personalAreaId: '@personalAreaId'},
                    isArray: false
                },
                listPois: {
                    method: 'GET',
                    url: apiPrefix + '/pois'
                },
                updatePois: {
                    method: 'PUT',
                    url: apiPrefix + '/pois'
                }
            });
        })

        .service('ClubAreaListService', function ($filter, ClubAreas, HuntingYearService) {
            this.listHuntingYears = function (clubId) {
                return ClubAreas.huntingYears({clubId: clubId}).$promise.then(function (result) {
                    return _.map(result, HuntingYearService.toObj);
                });
            };

            this.list = function (clubId, huntingYear, activeOnly) {
                return ClubAreas.query({
                    clubId: clubId,
                    activeOnly: activeOnly,
                    year: huntingYear || HuntingYearService.getCurrent(),
                    includeEmpty: true

                }).$promise.then(function (areas) {
                    var i18n = $filter('rI18nNameFilter');

                    return _.sortBy(areas, function (area) {
                        var name = i18n(area);
                        return name ? name.toLowerCase() : null;
                    });
                });
            };

            this.selectActiveArea = function (areas, selectedAreaId) {
                var area = _.find(areas, {
                    id: selectedAreaId
                });
                return area || _.chain(areas).filter('active').head().value();
            };
        })

        .controller('ClubAreaListController', function ($location, $scope, Helpers, HuntingYearService,
                                                        ClubAreas, ClubAreaFormSidebar, ClubAreaListService,
                                                        ClubPoiMarkerColors,
                                                        club, clubId, selectedAreaId, huntingYear,
                                                        showDeactive, rhyBounds, pois, preloadedHuntingYears) {
            var $ctrl = this;

            $ctrl.areas = [];
            $ctrl.selectedAreaId = selectedAreaId;
            $ctrl.canCreateArea = club.canEdit;
            $ctrl.canEditArea = club.canEdit;

            $ctrl.huntingYears = preloadedHuntingYears;
            $ctrl.selectedYear = huntingYear;

            $ctrl.rhyBounds = rhyBounds;
            $ctrl.showDeactive = showDeactive;

            $ctrl.hidePoiMarkers = false;
            $ctrl.visiblePoiMarkers = [];

            $ctrl.$onInit = function () {
                $ctrl.reloadAreas();
            };

            $ctrl.createArea = function () {
                ClubAreaFormSidebar.addClubArea(clubId, $ctrl.selectedYear).then(function (area) {
                    $scope.$emit('areaChanged', area);
                });
            };

            $ctrl.reloadAreas = function () {
                ClubAreaListService.list(clubId, $ctrl.selectedYear, !$ctrl.showDeactive).then(function (areas) {
                    $ctrl.areas = areas;
                    focusSelectedArea();
                });
            };

            var currentHuntingYear = HuntingYearService.getCurrent();

            $ctrl.showUpdateWarning = function () {
                return $ctrl.selectedYear && $ctrl.selectedYear < currentHuntingYear;
            };

            $scope.$on('areaChanged', function (e, area) {
                $ctrl.selectedAreaId = area.id;
                $ctrl.selectedYear = area.huntingYear;

                $ctrl.reloadAreas();

                if (!_.some($ctrl.huntingYears, {year: $ctrl.selectedYear})) {
                    ClubAreaListService.listHuntingYears(clubId).then(function (result) {
                        $ctrl.huntingYears = result;
                    });
                }
            });

            $ctrl.goToStore = function () {
                Helpers.goToStore();
            };

            $ctrl.showStoreButton = function () {
                var system = Helpers.getMobileOperatingSystem();
                return system === 'android' || system === 'ios';
            };

            $ctrl.onAreaSelect = function (area) {
                if (area && $ctrl.selectedAreaId !== area.id) {
                    showArea(area);
                }
            };

            $ctrl.hidePoiMarkersChanged = function () {
                $ctrl.hidePoiMarkers = !$ctrl.hidePoiMarkers;
            };

            function focusSelectedArea() {
                var selectedArea = ClubAreaListService.selectActiveArea($ctrl.areas, $ctrl.selectedAreaId);

                if (selectedArea) {
                    showArea(selectedArea);
                }
            }

            function showArea(area) {
                area.isOpen = true;

                $ctrl.selectedYear = area.huntingYear;
                $ctrl.selectedAreaId = area.id;

                $location.search({areaId: area.id});

                ClubAreas.combinedFeatures({id: area.id}).$promise.then(function (featureCollection) {
                    $ctrl.featureCollection = featureCollection;
                });

                ClubAreas.listPois({id: area.id}).$promise.then(function (result) {
                    var connectedPoiIds = result.poiIds;
                    $ctrl.visiblePoiMarkers =
                        _.chain(pois)
                            .filter(function (poi) {
                                return _.findIndex(connectedPoiIds, function (id) {
                                    return id === poi.id;
                                }) >= 0;
                            })
                            .map(function (poi) {
                                return {
                                    locations: poi.locations,
                                    color: ClubPoiMarkerColors.deduceColor(poi)
                                };
                            })
                            .flatMap(function (data) {
                                var locations = _.chain(data.locations)
                                    .map(function (l) {
                                        l.icon = data.icon;
                                        return l;
                                    })
                                    .value();
                                return ClubPoiMarkerColors.assignColor(locations, data.color);
                            })
                            .value();
                });
            }
        })

        .component('clubAreaList', {
            templateUrl: 'club/area/area-list.html',
            bindings: {
                areas: '<',
                canEdit: '<',
                onAreaSelect: '&'
            },
            controller: function () {
                var $ctrl = this;

                $ctrl.selectArea = function ($event, area) {
                    $event.preventDefault();
                    $ctrl.onAreaSelect({'area': area});
                };

                $ctrl.getAreaToggleClasses = function (area) {
                    return {
                        'glyphicon': true,
                        'glyphicon-chevron-down': area.isOpen,
                        'glyphicon-chevron-right': !area.isOpen
                    };
                };
            }
        })

        .component('clubAreaListDetails', {
            templateUrl: 'club/area/area-details.html',
            bindings: {
                area: '<'
            },
            controller: function ($state, ActiveRoleService) {
                var $ctrl = this;

                function isContactPerson() {
                    return ActiveRoleService.isClubContact() || ActiveRoleService.isModerator();
                }
                function isContactPersonOrGroupLeader() {
                    return ActiveRoleService.isClubGroupLeader() || isContactPerson();
                }
                $ctrl.canEditAreaGeometry = function () {
                    return isContactPerson() && $ctrl.area
                        && $ctrl.area.sourceType !== 'EXTERNAL';
                };

                $ctrl.canConnectPois = function () {
                    return isContactPersonOrGroupLeader() && $ctrl.area
                        && $ctrl.area.sourceType !== 'EXTERNAL';
                };

                $ctrl.editAreaGeometry = function () {
                    $state.go('club.map', {
                        areaId: $ctrl.area.id
                    });
                };

                $ctrl.selectPois = function () {
                    $state.go('club.area.areapoi', {
                        areaId: $ctrl.area.id
                    });
                };
            }
        })

        .controller('ClubAreaPoiController', function ($location, $scope, $state, HuntingYearService,
                                                       ClubAreas, ClubAreaFormSidebar, ClubAreaListService,
                                                       ClubPoiMarkerColors, ClubPoiTypes, ClubPoiMarkerBounds,
                                                       FetchAndSaveBlob, NotificationService,
                                                       club, clubId, area, pois, featureCollection, connectedPois) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.area = area;
                $ctrl.filterMode = null;
                $ctrl.pois = _.chain(pois)
                    .map(function (p) {
                        p.connected = _.findIndex(connectedPois.poiIds, function (c) {
                            return c === p.id;
                        }) >= 0;
                        return p;
                    })
                    .value();
                $ctrl.featureCollection = featureCollection;
                $ctrl.showOnlyConnected = false;
                $ctrl.typeOptions = _.values(ClubPoiTypes);
                $ctrl.connectedFilterOptions = ['ALL', 'CONNECTED', 'DETACHED'];
                $ctrl.connectedFilterMode = 'ALL';
                $ctrl.selectAll = false;
                $ctrl.filterChanged();
            };

            $ctrl.close = function () {
                $state.go('club.area.list', {
                    areaId: $ctrl.area.id
                });
            };

            $ctrl.save = function () {
                var ids = _.chain($ctrl.pois)
                    .filter('connected')
                    .map('id')
                    .value();
                ClubAreas.updatePois({id: area.id}, {poiIds: ids}).$promise.then(NotificationService.showDefaultSuccess);

            };

            $ctrl.filterChanged = function () {
                $ctrl.visibleMarkers =
                    _.chain($ctrl.pois)
                        .filter($ctrl.isPoiVisible)
                        .map(function (poi) {
                            return {
                                locations: poi.locations,
                                icon: poi.connected ? 'check' : 'none',
                                color: ClubPoiMarkerColors.deduceColor(poi)
                            };
                        })
                        .flatMap(function (data) {
                            var locations = _.chain(data.locations)
                                .map(function (l) {
                                    l.icon = data.icon;
                                    return l;
                                })
                                .value();
                            return ClubPoiMarkerColors.assignColor(locations, data.color);
                        })
                        .value();
            };

            $ctrl.selectAllChanged = function () {
                _.forEach($ctrl.pois, function (poi) {
                    if ($ctrl.isPoiVisible(poi)) {
                        poi.connected = $ctrl.selectAll;
                    }
                });
                $ctrl.filterChanged();
            };

            $ctrl.isPoiVisible = function (poi) {
                return isVisibleThroughPoiTypeFiltering(poi) &&
                    isVisibleThroughConnectedFiltering(poi);
            };

            function isVisibleThroughConnectedFiltering(poi) {
                return $ctrl.connectedFilterMode === 'ALL' ||
                    $ctrl.connectedFilterMode === 'CONNECTED' && poi.connected ||
                    $ctrl.connectedFilterMode === 'DETACHED' && !poi.connected;
            }

            function isVisibleThroughPoiTypeFiltering(poi) {
                return !$ctrl.filterMode || poi.type === $ctrl.filterMode;
            }

            $ctrl.selectPoi = function (poi) {
                $ctrl.selectedPoi = poi;
            };

            $ctrl.onMarkerClick = function (markerId) {
                var clickedId = _.parseInt(markerId);
                var poi = _.find($ctrl.pois, ['id', clickedId]);
                if (poi) {
                    $ctrl.selectPoi(poi);
                }
                $scope.$digest();
            };

            $ctrl.zoomTo = function (poi) {
                $ctrl.selectPoi(poi);
                ClubPoiMarkerBounds.zoomToContain(_.filter($ctrl.visibleMarkers, ['poiId', poi.id]));
            };

        })

        .component('clubAreaFunctionDropdown', {
            templateUrl: 'club/area/area-functions.html',
            bindings: {
                area: '<'
            },
            controller: function ($q, $scope, $stateParams, $window, ActiveRoleService, ClubAreas,
                                  FetchAndSaveBlob, TranslatedBlockUI,
                                  ClubAreaFormSidebar, ClubAreaCopyModal, ClubAreaImportModal,
                                  ClubAreaImportFromPersonalAreaModal, MapPdfModal, NotificationService) {
                var $ctrl = this;

                $ctrl.isContactPerson = function () {
                    return ActiveRoleService.isClubContact() || ActiveRoleService.isModerator();
                };

                function notifyAreaChange(area) {
                    $scope.$emit('areaChanged', area);
                }

                $ctrl.editArea = function () {
                    ClubAreaFormSidebar.editClubArea($ctrl.area).then(notifyAreaChange);
                };

                $ctrl.copyArea = function () {
                    ClubAreaCopyModal.copyClubArea($ctrl.area).then(notifyAreaChange);
                };

                $ctrl.importArea = function () {
                    checkAreaCalculated($ctrl.area).then(function () {
                        return ClubAreaImportModal.importArea($ctrl.area).then(function (area) {
                            notifyAreaChange(area);
                        });
                    }, function () {
                        NotificationService.showMessage('club.area.messages.areaCalculationRunning', 'warn');
                    });
                };

                function checkAreaCalculated(huntingClubArea) {
                    return ClubAreas.get({id: huntingClubArea.id}).$promise.then(function (a) {
                        return a.size.status !== 'PROCESSING' ? $q.resolve() : $q.reject();
                    });
                }

                $ctrl.importPersonalArea = function () {
                    ClubAreaImportFromPersonalAreaModal.importPersonalArea($ctrl.area).then(notifyAreaChange);
                };

                $ctrl.printArea = function () {
                    MapPdfModal.printArea('/api/v1/clubarea/' + $ctrl.area.id + '/print');
                };

                $ctrl.exportExcel = function (type) {
                    var url = exportBaseUri() + '/excel/' + type;

                    TranslatedBlockUI.start("club.area.excelExportMessage");

                    FetchAndSaveBlob.get(url)
                        .finally(TranslatedBlockUI.stop);

                };

                $ctrl.exportGeoJson = function () {
                    var url = exportBaseUri() + '/zip';

                    FetchAndSaveBlob.post(url, 'arraybuffer');
                };

                $ctrl.exportGarmin = function () {
                    FetchAndSaveBlob.post(exportBaseUri() + '/garmin');
                };

                $ctrl.exportArea = function () {
                    $window.open(exportBaseUri() + '/zip');
                };

                $ctrl.activate = function () {
                    ClubAreas.activate({id: $ctrl.area.id}).$promise.then(function () {
                        notifyAreaChange($ctrl.area);
                    });
                };

                $ctrl.deactivate = function () {
                    ClubAreas.deactivate({id: $ctrl.area.id}).$promise.then(function () {
                        notifyAreaChange($ctrl.area);
                    });
                };

                $ctrl.canActivate = function () {
                    return $ctrl.isContactPerson() && !$ctrl.area.active;
                };

                $ctrl.canDeactivate = function () {
                    return $ctrl.isContactPerson() && $ctrl.area.active && !$ctrl.area.attachedToGroup;
                };

                function exportBaseUri() {
                    return '/api/v1/clubarea/' + $ctrl.area.id;
                }

                $ctrl.isLocalArea = function () {
                    return $ctrl.area && $ctrl.area.sourceType !== 'EXTERNAL';
                };

                $ctrl.isAreaWithGeometry = function () {
                    return $ctrl.area && $ctrl.area.zoneId && !!$ctrl.area.size && $ctrl.area.size.all.total > 1;
                };

                $ctrl.isLocalAreaWithGeometry = function () {
                    return $ctrl.isAreaWithGeometry() && $ctrl.isLocalArea();
                };

                $ctrl.exportPoisToExcel = function () {
                    FetchAndSaveBlob.post('api/v1/club/' + $stateParams.id + '/poi/excel/' + $ctrl.area.id);
                };

                $ctrl.exportPoisToGpx = function () {
                    FetchAndSaveBlob.post('api/v1/club/' + $stateParams.id + '/poi/gpx/' + $ctrl.area.id);
                };
            }
        })

        .component('clubAreaListMap', {
            templateUrl: 'club/area/area-map.html',
            bindings: {
                initialViewBounds: '<',
                featureCollection: '<',
                poiLocations: '<',
                hideMarkers: '<'
            },
            controller: function ($filter, $translate, $scope, ClubPoiViewMarkers, MapDefaults, MapState, MapBounds) {
                var $ctrl = this;
                $ctrl.mapDefaults = MapDefaults.create();
                $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents();
                $ctrl.mapState = MapState.get();
                $ctrl.geojson = null;

                $ctrl.$onInit = function () {
                    setFeatureCollection($ctrl.featureCollection);
                };

                $ctrl.$onChanges = function (c) {
                    if (c.featureCollection) {
                        setFeatureCollection(c.featureCollection.currentValue);
                    }

                    if (c.hideMarkers) {
                        setPois($ctrl.poiLocations);
                    }

                    if (c.poiLocations) {
                        setPois($ctrl.poiLocations);
                    }
                };

                function setFeatureCollection(featureCollection) {
                    var prettyAreaSize = $filter('prettyAreaSize');
                    var geometriesIncluded = featureCollection &&
                        _(featureCollection.features).map('geometry').some(_.isObject);

                    if (geometriesIncluded) {
                        $ctrl.geojson = {
                            data: featureCollection,
                            style: MapDefaults.getGeoJsonOptions(),
                            onEachFeature: function (feature, layer) {
                                var areaSize = _.get(feature, 'properties.areaSize');

                                if (areaSize) {
                                    layer.bindPopup($translate.instant('global.map.areaSize') + ': ' + prettyAreaSize(areaSize));
                                }
                            }
                        };
                        var bounds = MapBounds.getBoundsFromGeoJsonFeatureCollection(featureCollection);
                        MapState.updateMapBounds(bounds, $ctrl.initialViewBounds, true);

                    } else {
                        $ctrl.geojson = null;
                        MapState.updateMapBounds(null, $ctrl.initialViewBounds, true);
                    }
                }

                function setPois(pois) {
                    if (pois) {
                        if ($ctrl.hideMarkers) {
                            $ctrl.markers = [];
                        } else {
                            $ctrl.markers = ClubPoiViewMarkers.createMarkers(pois, null, false, null, null, $scope);
                        }
                    }
                }
            }
        })

        .service('ClubAreaFormSidebar', function (FormSidebarService, GameDiaryParameters, ClubAreas) {
            var formSidebar = FormSidebarService.create({
                templateUrl: 'club/area/area-form.html',
                controller: 'ClubAreaFormSidebarController',
                largeDialog: false,
                resolve: {
                    diaryParameters: function () {
                        return GameDiaryParameters.query().$promise;
                    }
                }
            }, ClubAreas, function (parameters) {
                return {area: _.constant(parameters.area)};
            });

            this.addClubArea = function (clubId, huntingYear) {
                return formSidebar.show({
                    area: {
                        clubId: clubId,
                        huntingYear: huntingYear
                    }
                });
            };

            this.editClubArea = function (area) {
                return formSidebar.show({
                    id: area.id,
                    area: ClubAreas.get({id: area.id}).$promise
                });
            };
        })

        .controller('ClubAreaFormSidebarController', function (HuntingYearService, $scope, area, diaryParameters) {
            $scope.area = area;
            $scope.parameters = diaryParameters;
            $scope.getCategoryName = diaryParameters.$getCategoryName;
            $scope.getGameName = diaryParameters.$getGameName;

            var currentHuntingYear = HuntingYearService.getCurrent();
            var nextHuntingYear = currentHuntingYear + 1;
            var areaHuntingYear = area.huntingYear;

            $scope.huntingYears = _([currentHuntingYear, nextHuntingYear, areaHuntingYear])
                .filter()
                .uniq()
                .map(HuntingYearService.toObj)
                .value();

            $scope.save = function () {
                $scope.$close($scope.area);
            };

            $scope.cancel = $scope.$dismiss;
        })

        .service('ClubAreaCopyModal', function ($uibModal, ClubAreas, HuntingYearService) {
            this.copyClubArea = function (area) {
                return $uibModal.open({
                    templateUrl: 'club/area/area-copy.html',
                    controller: ModalController,
                    controllerAs: '$ctrl',
                    bindToController: true,
                    resolve: {
                        area: _.constant(area)
                    }
                }).result.then(function (res) {
                    return ClubAreas.copy({id: res.id}, res).$promise;
                });
            };

            function ModalController($uibModalInstance, area) {
                var $ctrl = this;

                $ctrl.huntingYears = HuntingYearService.currentAndNextObj();
                $ctrl.area = area;
                $ctrl.areaCopyData = {
                    id: area.id,
                    huntingYear: area.huntingYear,
                    copyGroups: true,
                    copyPOIs: true
                };

                $ctrl.save = function () {
                    $uibModalInstance.close($ctrl.areaCopyData);
                };

                $ctrl.cancel = function () {
                    $uibModalInstance.dismiss();
                };
            }
        })

        .service('ClubAreaImportModal', function ($q, $filter, $translate, $uibModal, dialogs, NotificationService,
                                                  ClubMapZoneProcessingModal) {
            this.importArea = function (area) {
                return confirmAreaImport(area).then(function () {
                    return $uibModal.open({
                        templateUrl: 'club/area/area-import.html',
                        size: 'sm',
                        controller: ModalController,
                        controllerAs: '$ctrl',
                        bindToController: true,
                        resolve: {
                            areaId: _.constant(area.id)
                        }
                    }).result.then(function () {
                        return ClubMapZoneProcessingModal.open(area.id).then(function () {
                            NotificationService.showMessage('club.area.import.success', 'success');
                            return area;
                        }, function () {
                            NotificationService.showDefaultFailure();
                        });
                    });
                });
            };

            function confirmAreaImport(area) {
                if (area.sourceType === 'EXTERNAL' || !area.zoneId) {
                    return $q.when(true);
                }

                var i18nFilter = $filter('rI18nNameFilter');
                var dialogTitle = $translate.instant('club.area.import.confirmTitle');
                var dialogMessage = $translate.instant('club.area.import.confirmBody', {
                    areaName: i18nFilter(area)
                });

                return dialogs.confirm(dialogTitle, dialogMessage).result;
            }

            function ModalController($uibModalInstance, areaId) {
                var $ctrl = this;

                $ctrl.url = '/api/v1/clubarea/' + areaId + '/import';
                $ctrl.uploadButtonVisible = true;
                $ctrl.uploadInProgress = false;

                $ctrl.onUpload = function (files) {
                    $ctrl.uploadButtonVisible = false;
                    $ctrl.uploadInProgress = true;
                };

                $ctrl.onSuccess = function (response) {
                    $uibModalInstance.close();
                    NotificationService.showDefaultSuccess();
                };

                $ctrl.onError = function (response) {
                    $uibModalInstance.dismiss();
                    NotificationService.showDefaultFailure();
                };

                $ctrl.close = function () {
                    $uibModalInstance.dismiss();
                };
            }
        })
        .service('ClubAreaImportFromPersonalAreaModal', function ($q, $filter, $translate, $uibModal, dialogs, NotificationService) {
            this.importPersonalArea = function (area) {
                return confirmAreaImport(area).then(function () {
                    return $uibModal.open({
                        templateUrl: 'club/area/personal-area-import.html',
                        size: 'md',
                        controller: ModalController,
                        controllerAs: '$ctrl',
                        bindToController: true,
                        resolve: {
                            areaId: _.constant(area.id)
                        }
                    }).result.then(function (area) {
                        NotificationService.showMessage('club.area.importPersonal.success', 'success');
                        return area;
                    });
                });
            };

            function confirmAreaImport(area) {
                if (!area.zoneId) {
                    return $q.when(true);
                }

                var i18nFilter = $filter('rI18nNameFilter');
                var dialogTitle = $translate.instant('club.area.import.confirmTitle');
                var dialogMessage = $translate.instant('club.area.importPersonal.confirmBody', {
                    areaName: i18nFilter(area)
                });

                return dialogs.confirm(dialogTitle, dialogMessage).result;
            }

            function ModalController($uibModalInstance, ClubAreas, AccountAreas, areaId) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.areaExternalId = null;
                    $ctrl.selectedArea = null;
                    $ctrl.error = null;
                };

                $ctrl.search = function () {
                    $ctrl.selectedArea = null;
                    $ctrl.error = null;
                    AccountAreas.findByExternalId({areaExternalId: $ctrl.areaExternalId}).$promise.then(
                        function (area) {
                            $ctrl.selectedArea = area;
                        }, function (error) {
                            $ctrl.error = error;
                        });
                };

                $ctrl.close = function () {
                    $uibModalInstance.dismiss();
                };

                $ctrl.removeSelection = function () {
                    $ctrl.selectedArea = null;
                };

                $ctrl.doImport = function () {
                    return ClubAreas.importFromPersonalArea({
                        id: areaId
                    }, {
                        personalAreaId: $ctrl.selectedArea.id
                    }).$promise.then(function (area) {
                        $uibModalInstance.close(area);
                    });
                };
            }
        });
})();

