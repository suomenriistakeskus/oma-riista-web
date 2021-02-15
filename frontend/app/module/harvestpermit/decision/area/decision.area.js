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
                activeTab: function ($stateParams) {
                    return $stateParams.tab || 'normal';
                }
            },
            controllerAs: '$ctrl',
            controller: function ($state, TranslatedBlockUI, FormPostService,
                                  MooselikePermitApplication, MapPdfModal,
                                  decision, activeTab) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.decision = decision;
                    $ctrl.applicationId = decision.applicationId;
                    $ctrl.activeTab = activeTab;
                    $ctrl.featureCollection = {crs: null, features: [], type: 'FeatureCollection'};
                    $ctrl.conflicts = [];

                    if ($ctrl.activeTab === 'normal') {
                        return;
                    }

                    if ($ctrl.activeTab === 'conflicts') {
                        TranslatedBlockUI.start("global.block.wait");

                        MooselikePermitApplication.listConflicts({
                            id: $ctrl.applicationId

                        }).$promise.then(function (conflicts) {
                            $ctrl.conflicts = conflicts;
                        }).finally(function () {
                            TranslatedBlockUI.stop();
                        });

                    } else {
                        TranslatedBlockUI.start("global.block.wait");

                        MooselikePermitApplication.getGeometry({
                            id: $ctrl.applicationId,
                            outputStyle: $ctrl.activeTab === 'partner' ? 'partner' : 'union'

                        }).$promise.then(function (featureCollection) {
                            $ctrl.featureCollection = featureCollection;

                        }).finally(function () {
                            TranslatedBlockUI.stop();
                        });
                    }
                };

                $ctrl.isActiveTab = function (tab) {
                    return $ctrl.activeTab === tab;
                };

                $ctrl.showTab = function (tab) {
                    $state.go('jht.decision.area', {tab: tab});
                };
            }
        });
    })
    .component('decisionApplicationNormalMap', {
        templateUrl: 'harvestpermit/decision/area/map-vector.html',
        bindings: {
            applicationId: '<'
        },
        controller: function (MapDefaults, MapState, MapBounds, MapPdfModal, MooselikePermitApplication) {
            var vectorLayerTemplate = _.template('/api/v1/vector/application/<%= id %>/{z}/{x}/{y}');
            var boundsOfFinland = MapBounds.getBoundsOfFinland();

            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.mapDefaults = MapDefaults.create({fullscreen: true});
                $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents();
                $ctrl.mapState = MapState.get();
                $ctrl.vectorLayer = null;

                MapState.updateMapBounds(null, boundsOfFinland, false);
            };

            $ctrl.exportMapPdf = function () {
                MapPdfModal.printArea('/api/v1/harvestpermit/application/'
                    + $ctrl.applicationId + '/area/pdf');
            };

            $ctrl.$onChanges = function (c) {
                if (c.applicationId) {
                    updateMap(c.applicationId.currentValue);
                }
            };

            function updateMap(applicationId) {
                if (!applicationId) {
                    $ctrl.vectorLayer = null;
                    return;
                }

                loadApplicationBounds(applicationId).then(function (bounds) {
                    MapState.updateMapBounds(bounds, boundsOfFinland, true);

                    $ctrl.vectorLayer = {
                        url: vectorLayerTemplate({id: applicationId}),
                        bounds: bounds
                    };
                });
            }

            function loadApplicationBounds(applicationId) {
                return MooselikePermitApplication.getBounds({id: applicationId}).$promise.then(function (bounds) {
                    return {
                        southWest: {lat: bounds.minLat, lng: bounds.minLng},
                        northEast: {lat: bounds.maxLat, lng: bounds.maxLng}
                    };
                });
            }
        }
    })

    .component('decisionApplicationPartnerMap', {
        templateUrl: 'harvestpermit/decision/area/map-geojson.html',
        bindings: {
            applicationId: '<',
            featureCollection: '<'
        },
        controller: function ($filter, $translate, MapDefaults, MapState, MapBounds,
                              MoosePermitMapService, leafletData) {
            var prettyAreaSize = $filter('prettyAreaSize');

            var $ctrl = this;

            $ctrl.mapId = 'harvest-permit-application-partner-map';
            $ctrl.mapDefaults = MapDefaults.create({fullscreen: true});
            $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents();
            $ctrl.mapState = MapState.get();

            $ctrl.$onChanges = function (c) {
                if (c.featureCollection) {
                    var featureCollection = c.featureCollection.currentValue;

                    leafletData.getMap($ctrl.mapId).then(function (map) {
                        updateComponent(map, featureCollection);
                    });
                }
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

            function createGeoJsonLayer(featureCollection) {
                var palette = MoosePermitMapService.createPalette(featureCollection);

                return L.geoJSON(featureCollection, {
                    style: function (feature) {
                        return {
                            fillColor: palette.resolve(feature),
                            weight: 1,
                            color: '#000',
                            fillOpacity: 0.3
                        };
                    },
                    onEachFeature: function (feature, layer) {
                        layer.on('click', function (e) {
                            layer.bindPopup(legendTitleFunction(feature));
                        });
                    }
                });
            }

            var layerControl = null;
            var geoJsonLayer = null;

            function updateComponent(map, featureCollection) {
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

                var featureCollectionNotEmpty = featureCollection && _.size(featureCollection.features) > 0;

                if (featureCollectionNotEmpty) {
                    var featureBounds = MapBounds.getBoundsFromGeoJsonFeatureCollection(featureCollection);
                    MapState.updateMapBounds(featureBounds, null, true);

                    geoJsonLayer = createGeoJsonLayer(featureCollection);
                    layerControl = createLayerControl(geoJsonLayer);
                    layerControl.addTo(map);

                } else {
                    MapState.updateMapBounds({}, null, true);
                }
            }
        }
    });
