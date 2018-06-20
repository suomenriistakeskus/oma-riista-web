'use strict';

angular.module('app.harvestpermit.decision.area', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.decision.area', {
            url: '/area?tab',
            params: {
                tab: null
            },
            templateUrl: 'harvestpermit/decision/area/area.html',
            resolve: {
                decision: function (PermitDecision, decisionId) {
                    return PermitDecision.get({id: decisionId}).$promise;
                },
                application: function (decisionId, PermitDecision) {
                    return PermitDecision.getApplication({id: decisionId}).$promise;
                }
            },
            controllerAs: '$ctrl',
            controller: function ($state, $stateParams, TranslatedBlockUI, FormPostService,
                                  HarvestPermitApplications, MapPdfModal, application, decision) {
                var $ctrl = this;

                $ctrl.decision = decision;
                $ctrl.activeTab = $stateParams.tab || 'vector';
                $ctrl.mapStyle = 'union';
                $ctrl.conflicts = [];
                $ctrl.featureCollection = {crs: null, features: [], type: 'FeatureCollection'};

                $ctrl.$onInit = function () {
                    $ctrl.application = application;
                    if ($ctrl.activeTab === 'conflicts') {
                        $ctrl.conflicts = [];

                        TranslatedBlockUI.start("global.block.wait");
                        HarvestPermitApplications.listConflicts({id: $ctrl.application.id}).$promise
                            .then(function (conflicts) {
                                $ctrl.conflicts = conflicts;
                            })
                            .finally(function () {
                                TranslatedBlockUI.stop();
                            });
                    } else {
                        $ctrl.mapStyle = $ctrl.activeTab === 'partner' ? 'partner' : 'union';
                        $ctrl.featureCollection = {crs: null, features: [], type: 'FeatureCollection'};

                        if ($ctrl.activeTab === 'vector') {
                            return;
                        }

                        TranslatedBlockUI.start("global.block.wait");
                        HarvestPermitApplications.getGeometry({
                            id: $ctrl.application.id,
                            outputStyle: $ctrl.mapStyle
                        }).$promise
                            .then(function (featureCollection) {
                                $ctrl.featureCollection = featureCollection;
                            })
                            .finally(function () {
                                TranslatedBlockUI.stop();
                            });
                    }
                };

                $ctrl.isMapActivated = function (map) {
                    return $ctrl.activeTab === map;
                };

                $ctrl.showMap = function (map) {
                    $state.go('jht.decision.area', {tab: map});
                };

                $ctrl.isConflictsViewActivated = function () {
                    return $ctrl.activeTab === 'conflicts';
                };

                $ctrl.showConflicts = function () {
                    $state.go('jht.decision.area', {tab: 'conflicts'});
                };

                $ctrl.loadMapPdf = function () {
                    MapPdfModal.printArea('/api/v1/harvestpermit/application/' + application.id + '/area/pdf');
                };

                $ctrl.exportFragmentExcel = function () {
                    FormPostService.submitFormUsingBlankTarget('/api/v1/harvestpermit/application/'
                        + $ctrl.application.id + '/area/fragments/excel');
                };

                $ctrl.exportConflictsExcel = function () {
                    FormPostService.submitFormUsingBlankTarget('/api/v1/harvestpermit/application/'
                        + $ctrl.application.id + '/conflicts/excel');
                };
            }
        });
    })
    .component('decisionApplicationVector', {
        templateUrl: 'harvestpermit/decision/area/map-vector.html',
        bindings: {
            selectedApplication: '<'
        },
        controller: function (MapDefaults, MapState, MapBounds,
                              HarvestPermitApplications) {
            var $ctrl = this;

            $ctrl.mapDefaults = MapDefaults.create();
            $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents();
            $ctrl.mapState = MapState.get();
            $ctrl.vectorLayer = null;

            var boundsOfFinland = MapBounds.getBoundsOfFinland();
            MapState.updateMapBounds(null, boundsOfFinland, false);

            $ctrl.$onChanges = function (c) {
                if (c.selectedApplication) {
                    var application = c.selectedApplication.currentValue;

                    if (application && application.id) {
                        loadApplicationBounds(application.id).then(function (bounds) {
                            MapState.updateMapBounds(bounds, boundsOfFinland, true);
                            updateMap(application, bounds);
                        });

                    } else {
                        $ctrl.vectorLayer = null;
                    }
                }
            };

            function updateMap(application, bounds) {
                var vectorLayerTemplate = _.template('/api/v1/vector/application/<%= id %>/{z}/{x}/{y}');

                $ctrl.vectorLayer = {
                    url: vectorLayerTemplate({id: application.id}),
                    bounds: bounds
                };
            }

            function loadApplicationBounds(applicationId) {
                return HarvestPermitApplications.getBounds({
                    id: applicationId

                }).$promise.then(function (bounds) {
                    return {
                        southWest: {lat: bounds.minLat, lng: bounds.minLng},
                        northEast: {lat: bounds.maxLat, lng: bounds.maxLng}
                    };
                });
            }
        }
    })
    .component('decisionApplicationGeojson', {
        templateUrl: 'harvestpermit/decision/area/map-geojson.html',
        bindings: {
            applicationId: '<',
            mapStyle: '<',
            featureCollection: '<'
        },
        controller: function ($filter, $translate, MapDefaults, MapState, MapBounds,
                              MoosePermitMapService, leafletData, WGS84, HarvestPermitApplicationFragmentInfoPopup) {
            var $ctrl = this;
            var prettyAreaSize = $filter('prettyAreaSize');

            $ctrl.mapId = 'harvest-permit-application-geojson-map';
            $ctrl.mapDefaults = MapDefaults.create({fullscreen: true});
            $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents();
            $ctrl.mapState = MapState.get();

            $ctrl.$onChanges = function (c) {
                if (c.featureCollection) {
                    setFeatureCollection(c.featureCollection.currentValue, $ctrl.mapStyle);
                }
            };

            $ctrl.$onInit = function () {
                if ($ctrl.mapStyle !== 'union') {
                    return;
                }

                leafletData.getMap($ctrl.mapId).then(function (map) {
                    L.control.simpleLegend({
                        legend: {
                            'red': $translate.instant('harvestpermit.application.mapLegend.fragment'),
                            'green': $translate.instant('harvestpermit.application.mapLegend.notFragment'),
                            'blue': $translate.instant('harvestpermit.application.mapLegend.mh')
                        }
                    }).addTo(map);
                });
            };

            function createLegendTitleFunction() {
                var userLanguage = $translate.use() === 'sv' ? 'sv' : 'fi';
                var areaSizeText = $translate.instant('global.map.areaSize');
                var clubNameGetter = _.property('properties.clubName.' + userLanguage);
                var areaNameGetter = _.property('properties.areaName.' + userLanguage);
                var areaSizeGetter = _.property('properties.areaSize');

                return function (feature) {
                    var areaSize = areaSizeGetter(feature);
                    var clubName = clubNameGetter(feature);
                    var areaName = areaNameGetter(feature);
                    clubName = clubName ? clubName : '';
                    areaName = areaName ? ' - ' + areaName : '';
                    areaSize = areaSize ? areaSizeText + ': ' + prettyAreaSize(areaSize) : '';
                    areaSize = clubName ? '<br/>' + areaSize : areaSize;

                    return clubName + areaName + areaSize;
                };
            }

            var legendTitleFunction = createLegendTitleFunction();

            function createLayerControl(geoJsonLayer) {
                var sortProperty = _.property('layer.feature.properties.areaSize');
                return L.control.geoJsonLayerControl(geoJsonLayer, {
                    textToggleAll: $translate.instant('global.map.toggleLayers'),
                    layerToLegendTitle: function (layer) {
                        return legendTitleFunction(layer.feature);
                    },
                    sortFunction: function (a, b) {
                        return sortProperty(b) - sortProperty(a);
                    }
                });
            }

            function createGeoJsonLayer(featureCollection, mapStyle) {
                var palette = MoosePermitMapService.createPalette(featureCollection);

                return L.geoJSON(featureCollection, {
                    style: function (feature) {
                        var fillColor;

                        if (mapStyle === 'union') {
                            var areaSize = _.get(feature, 'properties.areaSize');
                            // Mark area with less than 1000 ha
                            fillColor = areaSize < 1000 * 10000 ? 'red' : 'green';
                        } else {
                            fillColor = palette.resolve(feature);
                        }

                        return {
                            fillColor: fillColor,
                            weight: 1,
                            opacity: 1,
                            color: '#000',
                            fillOpacity: 0.3
                        };
                    },
                    onEachFeature: function (feature, layer) {
                        var areaSize = _.get(feature, 'properties.areaSize');
                        var showSimple = areaSize >= 1000 * 10000;
                        layer.on('click', function (e) {
                            if (!areaSize || showSimple) {
                                layer.bindPopup(legendTitleFunction(feature));
                            } else {
                                HarvestPermitApplicationFragmentInfoPopup.popup($ctrl.applicationId, e.latlng);
                            }
                        });
                    }
                });
            }

            var layerControl = null;
            var geoJsonLayer = null;

            function setFeatureCollection(featureCollection, mapStyle) {
                leafletData.getMap($ctrl.mapId).then(function (map) {
                    if (layerControl) {
                        map.removeControl(layerControl);
                        layerControl = null;
                    }

                    if (geoJsonLayer) {
                        geoJsonLayer.eachLayer(function (layer) {
                            map.removeLayer(layer);
                        });
                        geoJsonLayer = null;
                    }

                    var geometriesIncluded = featureCollection &&
                        _(featureCollection.features).map('geometry').some(_.isObject);

                    if (geometriesIncluded) {
                        geoJsonLayer = createGeoJsonLayer(featureCollection, mapStyle);
                        layerControl = createLayerControl(geoJsonLayer);
                        layerControl.addTo(map);

                        var featureBounds = MapBounds.getBoundsFromGeoJsonFeatureCollection(featureCollection);
                        MapState.updateMapBounds(featureBounds, $ctrl.initialViewBounds, true);

                    } else {
                        MapState.updateMapBounds({}, null, true);
                    }
                });
            }
        }
    });
