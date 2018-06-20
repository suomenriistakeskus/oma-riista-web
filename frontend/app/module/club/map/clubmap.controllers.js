'use strict';

angular.module('app.clubmap.controllers', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('club.map', {
                url: '/areamap/{areaId:[0-9]{1,8}}',
                templateUrl: 'club/map/edit.html',
                controller: 'ClubAreaEditMapController',
                controllerAs: '$ctrl',
                bindToController: true,
                wideLayout: true,
                resolve: {
                    rhyBounds: function (MapBounds, club) {
                        return MapBounds.getRhyBounds(club.rhy.officialCode);
                    },
                    huntingClubAreaId: function ($stateParams) {
                        return _.parseInt($stateParams.areaId);
                    },
                    huntingClubArea: function (ClubAreas, huntingClubAreaId) {
                        return ClubAreas.get({id: huntingClubAreaId}).$promise;
                    },
                    featureCollection: function (ClubAreas, huntingClubAreaId) {
                        return ClubAreas.getFeatures({id: huntingClubAreaId}).$promise;
                    },
                    metsahallitus: function (MHAreaService, featureCollection, huntingClubArea) {
                        return MHAreaService.create(featureCollection, huntingClubArea);
                    }
                }
            });
    })

    .controller('ClubAreaEditMapController',
        function ($state, $log, $filter, $q,
                  NotificationService, TranslatedBlockUI, UnsavedChangesConfirmationService,
                  GIS, WGS84, MapDefaults, MapState, MapBounds, ClubAreas,
                  clubId, huntingClubArea, huntingClubAreaId,
                  rhyBounds, metsahallitus, featureCollection) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                var areaBounds = MapBounds.getBoundsFromGeoJsonBbox(featureCollection.bbox);
                MapState.updateMapBounds(areaBounds, rhyBounds, true);

                $ctrl.huntingClubArea = huntingClubArea;
                $ctrl.metsahallitus = metsahallitus;
                $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents();
                $ctrl.mapDefaults = MapDefaults.create();
                $ctrl.mapState = MapState.get();
                $ctrl.selectedTab = 'a';

                $ctrl.editor = {
                    api: {
                        ready: false
                    },
                    geojson: featureCollection,
                    callbacks: {
                        add: function (latlng) {
                            // Add area geometry by clicking map
                            var etrs = WGS84.toETRS(latlng.lat, latlng.lng);
                            return GIS.getPropertyByCoordinates(etrs).then(_.property('data'));
                        },
                        marquee: function (bounds) {
                            return GIS.getPropertyByBounds(bounds).then(_.property('data'));
                        }
                    }
                };
            };

            $ctrl.close = function () {
                $state.go('club.area.list', {areaId: huntingClubAreaId});
            };

            $ctrl.saveFeatures = function () {
                var geoJson = $ctrl.editor.api.features.toGeoJSON();
                geoJson.id = huntingClubAreaId;

                var checkAreaCalculated = function () {
                    return ClubAreas.get({id: huntingClubAreaId}).$promise.then(function (a) {
                        return a.computedAreaSize >= 0 ? $q.resolve() : $q.reject();
                    });
                };

                var handleAreaCalculationRunning = function () {
                    NotificationService.showMessage('club.area.messages.areaCalculationRunning', 'warn');
                };

                var updateFeatures = function () {
                    var onFeatureUpdateSuccess = function () {
                        UnsavedChangesConfirmationService.setChanges(false);
                        NotificationService.showDefaultSuccess();
                    };
                    return ClubAreas.updateFeatures(geoJson).$promise.then(onFeatureUpdateSuccess, NotificationService.showDefaultFailure);
                };

                TranslatedBlockUI.start("global.block.wait");
                checkAreaCalculated()
                    .then(updateFeatures, handleAreaCalculationRunning)
                    .finally(function () {
                        TranslatedBlockUI.stop();
                    });
            };

            $ctrl.getActiveFeature = function () {
                return $ctrl.editor.api.ready ? $ctrl.editor.api.features.getActiveFeature() : null;
            };

            $ctrl.hasPalstaAreaChanges = function () {
                return $ctrl.editor.api.ready ? $ctrl.editor.api.features.hasChangedFeatures() : false;
            };

            $ctrl.hasMooseAreaChanges = function () {
                return $ctrl.metsahallitus.hasChangedFeatures();
            };
        }
    )

    .component('clubMapEditDetails', {
        templateUrl: 'club/map/edit_details.html',
        bindings: {
            feature: '<',
            updateFeature: '&'
        }
    })

    .component('clubMapEditPalstaSidebar', {
        templateUrl: 'club/map/edit_sidebar_palsta.html',
        bindings: {
            editorApi: '<'
        },
        controller: function (NotificationService) {
            var $ctrl = this;

            $ctrl.palstaFeatureList = function () {
                return $ctrl.editorApi.ready ? $ctrl.editorApi.features.palstaFeatureList() : [];
            };

            $ctrl.isEmptySelection = function () {
                return $ctrl.editorApi.ready && $ctrl.editorApi.features.isEmptySelection();
            };

            $ctrl.zoomFeature = function (feature) {
                $ctrl.editorApi.zoom(feature.id);
            };

            $ctrl.removeFeature = function (feature) {
                $ctrl.editorApi.removeFeature(feature.id);
            };

            $ctrl.setHighlight = function (feature) {
                $ctrl.editorApi.features.setHighlight(feature);
            };

            $ctrl.removeHighlight = function (feature) {
                $ctrl.editorApi.features.removeHighlight(feature);
            };

            $ctrl.updateChangedFeature = function (feature) {
                $ctrl.editorApi.features.updateChangedFeature(feature).catch(NotificationService.showDefaultFailure);
            };
        }
    })

    .component('clubMapEditMooseSidebar', {
        templateUrl: 'club/map/edit_sidebar_mh.html',
        bindings: {
            editorApi: '<',
            metsahallitus: '<'
        },
        controller: function (GIS, UnsavedChangesConfirmationService) {
            var $ctrl = this;

            $ctrl.mooseAreaSearchQuery = null;

            $ctrl.focusMooseArea = function (area) {
                $ctrl.editorApi.zoom(area.id);
            };

            $ctrl.onSelectMooseArea = function (area) {
                UnsavedChangesConfirmationService.setChanges(true);

                // De-select
                $ctrl.mooseAreaSearchQuery = null;

                // Show geometry
                loadMooseFeature(area);
            };

            $ctrl.removeMooseArea = function (area) {
                UnsavedChangesConfirmationService.setChanges(true);
                $ctrl.metsahallitus.removeSelectedArea(area);
                $ctrl.editorApi.features.removeMooseArea(area);
            };

            $ctrl.updateMooseArea = function (area) {
                var replacement = $ctrl.metsahallitus.findByCode(area.number);

                if (replacement) {
                    loadMooseFeature(replacement);
                }
            };

            function loadMooseFeature(area) {
                GIS.getMetsahallitusHirviById(area.gid).then(function (response) {
                    $ctrl.editorApi.features.removeMooseArea(area);
                    $ctrl.editorApi.addFeatures(response.data);

                    var mooseArea = $ctrl.metsahallitus.addSelectedArea(area);
                    $ctrl.focusMooseArea(mooseArea);
                });
            }
        }
    })

    .component('clubMapBulkInsertSidebar', {
        templateUrl: 'club/map/edit_sidebar_bulk.html',
        bindings: {
            editorApi: '<'
        },
        controller: function (GIS, NotificationService) {
            var $ctrl = this;

            $ctrl.propertyList = [];
            $ctrl.okPropertyCount = null;
            $ctrl.invalidPropertyList = null;

            // Add area geometry using property identifier
            $ctrl.addPropertiesAsText = function (form) {
                if (form.$invalid) {
                    return;
                }

                var propertyListCopy = $ctrl.propertyList || [];

                $ctrl.propertyList = [];
                $ctrl.okPropertyCount = 0;
                $ctrl.invalidPropertyList = [];

                var handleResponse = function (propertyIdentifier) {
                    return function (response) {
                        if (response.data.features.length) {
                            $ctrl.editorApi.addFeatures(response.data);
                            $ctrl.okPropertyCount++;
                        } else {
                            $ctrl.invalidPropertyList.push(propertyIdentifier);
                        }
                    };
                };

                for (var i = 0; i < propertyListCopy.length; i++) {
                    var value = propertyListCopy[i];

                    GIS.getPropertyPolygonByCode(value)
                        .then(handleResponse(value), NotificationService.showDefaultFailure);
                }
            };
        }
    });

