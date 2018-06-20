'use strict';

angular.module('app.harvestpermit.application.conflict', [])
    .component('harvestPermitApplicationConflicts', {
        templateUrl: 'harvestpermit/applications/conflict/conflict-list.html',
        bindings: {
            selectedApplication: '<',
            conflicts: '<'
        },
        controller: function ($state) {
            var $ctrl = this;

            $ctrl.filter = 'all';

            $ctrl.filterConflicts = function () {
                if ($ctrl.filter === 'private') {
                    return _.filter($ctrl.conflicts, function (c) {
                        return c.onlyPrivateConflicts;
                    });
                }
                if ($ctrl.filter === 'mh') {
                    return _.filter($ctrl.conflicts, function (c) {
                        return c.onlyMhConflicts;
                    });
                }
                return $ctrl.conflicts;
            };

            $ctrl.openConflictResolution = function (otherApplication) {
                $state.go('jht.decision.conflicts', {
                    firstApplicationId: $ctrl.selectedApplication.id,
                    secondApplicationId: otherApplication.id
                });
            };
        }
    })

    .controller('HarvestPermitApplicationConflictResolutionController',
        function ($window, GIS, WGS84, HarvestPermitApplications, firstApplicationId, secondApplicationId) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.scrollToIndex = null;
                $ctrl.focusSelected = true;
                $ctrl.firstApplicationId = firstApplicationId;
                $ctrl.secondApplicationId = secondApplicationId;

                HarvestPermitApplications.listPairwiseConflicts({
                    id: firstApplicationId,
                    otherId: secondApplicationId
                }).$promise.then(function (result) {
                    $ctrl.pairwiseConflicts = _.sortByOrder(result, ['metsahallitus', 'areaSize'], [true, false]);
                });

                HarvestPermitApplications.get({id: firstApplicationId}).$promise.then(function (r) {
                    $ctrl.firstApplication = r;
                });

                HarvestPermitApplications.get({id: secondApplicationId}).$promise.then(function (r) {
                    $ctrl.secondApplication = r;
                });
            };

            $ctrl.goBack = function () {
                $window.history.back();
            };

            var selectedPalsta = null;
            $ctrl.showPalsta = function (row) {
                if (row === selectedPalsta) {
                    return;
                }
                selectedPalsta = row;
                updateListSelection(row.palstaId);

                GIS.getPropertyPolygonById(row.palstaId).then(function (response) {
                    $ctrl.palstaFeatureCollection = response.data;
                    $ctrl.focusSelected = true;
                });
            };

            $ctrl.onMapClick = function (latlng) {
                // Add area geometry by clicking map
                GIS.getPropertyByCoordinates(WGS84.toETRS(latlng.lat, latlng.lng)).then(function (response) {
                    $ctrl.palstaFeatureCollection = response.data;
                    $ctrl.focusSelected = false;

                    var firstFeature = _.first($ctrl.palstaFeatureCollection.features);

                    if (firstFeature) {
                        updateListSelection(_.parseInt(firstFeature.id));
                    }
                });
            };

            $ctrl.onFeatureClick = function (feature) {
                updateListSelection(_.parseInt(feature.id));
            };

            function updateListSelection(featureId) {
                _.forEach($ctrl.pairwiseConflicts, function (c) {
                    c.selected = false;
                });

                var selectedIndex = _.findIndex($ctrl.pairwiseConflicts, 'palstaId', featureId);

                if (selectedIndex >= 0) {
                    $ctrl.scrollToIndex = selectedIndex;
                    $ctrl.pairwiseConflicts[selectedIndex].selected = true;
                } else {
                    $ctrl.scrollToIndex = null;
                }
            }
        })

    .component('harvestPermitApplicationConflictMap', {
        templateUrl: 'harvestpermit/applications/conflict/conflict-map.html',
        bindings: {
            focusSelected: '<',
            firstApplicationId: '<',
            secondApplicationId: '<',
            palstaFeatureCollection: '<',
            onMapClick: '&',
            onFeatureClick: '&'
        },
        controller: function (HarvestPermitApplications, PropertyIdentifierService,
                              $filter, $translate, MapState, MapDefaults, MapBounds, leafletData) {
            var $ctrl = this;
            var featureAreaSize = $filter('featureAreaSize');
            var formatPropertyIdentifier = $filter('formatPropertyIdentifier');

            $ctrl.mapId = 'harvest-permit-application-conflict-map';
            $ctrl.mapDefaults = MapDefaults.create();
            $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents(['click']);
            $ctrl.mapState = MapState.get();

            var boundsOfFinland = MapBounds.getBoundsOfFinland();
            MapState.updateMapBounds(null, boundsOfFinland, false);

            leafletData.getMap($ctrl.mapId).then(function (map) {
                map.on('click', function (event) {
                    $ctrl.onMapClick({'latlng': event.latlng});
                });

                L.control.simpleLegend({
                    legend: {
                        '#EB7870': $translate.instant('harvestpermit.application.conflictResolution.firstApplication'),
                        '#666': $translate.instant('harvestpermit.application.conflictResolution.secondApplication'),
                        '#B53C38': $translate.instant('harvestpermit.application.conflictResolution.conflictArea')
                    }
                }).addTo(map);
            });

            var vectorLayerTemplate = _.template('/api/v1/vector/application/<%= id %>/{z}/{x}/{y}');

            function createVectorLayer(applicationId, color) {
                var url = vectorLayerTemplate({id: applicationId});

                return L.vectorGrid.protobuf(url, {
                    minZoom: 6,
                    pane: 'overlayPane',
                    rendererFactory: L.canvas.tile,
                    fetchOptions: {
                        credentials: 'include'
                    },
                    keepBuffer: 10,
                    maxZoom: 16,
                    //bounds: L.latLngBounds(maxBounds.southWest, maxBounds.northEast),
                    vectorTileLayerStyles: {
                        all: {
                            fill: true,
                            fillColor: color,
                            fillOpacity: 0.3,
                            weight: 0.75,
                            color: 'black'
                        }
                    }
                });
            }

            var firstApplicationVectorLayer = null;
            var secondApplicationVectorLayer = null;

            $ctrl.$onChanges = function (c) {
                if ($ctrl.palstaFeatureCollection) {
                    updateGeoJson();
                }

                if (!c.firstApplicationId && !c.secondApplicationId) {
                    return;
                }

                var firstApplicationId = $ctrl.firstApplicationId;
                var secondApplicationId = $ctrl.secondApplicationId;

                leafletData.getMap($ctrl.mapId).then(function (map) {
                    if (firstApplicationVectorLayer) {
                        map.removeLayer(firstApplicationVectorLayer);
                        firstApplicationVectorLayer = null;
                    }

                    if (secondApplicationVectorLayer) {
                        map.removeLayer(secondApplicationVectorLayer);
                        secondApplicationVectorLayer = null;
                    }

                    if (secondApplicationId) {
                        secondApplicationVectorLayer = createVectorLayer(secondApplicationId, 'black').addTo(map);
                    }

                    if (firstApplicationId) {
                        firstApplicationVectorLayer = createVectorLayer(firstApplicationId, 'red').addTo(map);

                        loadApplicationBounds(firstApplicationId).then(function (bounds) {
                            MapState.updateMapBounds(bounds, boundsOfFinland, true);
                        });
                    }
                });
            };

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

            function updateGeoJson() {
                $ctrl.geojson = {
                    data: $ctrl.palstaFeatureCollection,
                    style: function (feature) {
                        return {
                            color: 'red',
                            weight: 5,
                            fill: false
                        };
                    },
                    onEachFeature: function (feature, layer) {
                        var code = _.get(feature, 'properties.number');
                        var name = _.get(feature, 'properties.name');
                        var codeText = '<strong>' + formatPropertyIdentifier(code) + '</strong>';
                        var nameText = name ? '<br/>' + name : '';
                        var areaText = '<br/>' + featureAreaSize(feature);

                        var popup = layer.bindPopup(codeText + nameText + areaText);

                        layer.on('add', function () {
                            // Popup cannot be added before layer has been added
                            popup.openPopup();
                        });

                        layer.on('click', function (event) {
                            // Focus palsta in sidebar
                            $ctrl.onFeatureClick({'feature': event.target.feature});
                        });
                    }
                };

                if ($ctrl.focusSelected && $ctrl.palstaFeatureCollection) {
                    var bounds = MapBounds.getBoundsFromGeoJsonFeatureCollection($ctrl.palstaFeatureCollection);
                    MapState.updateMapBounds(bounds, MapBounds.getBoundsOfFinland(), true);
                }
            }
        }
    });
