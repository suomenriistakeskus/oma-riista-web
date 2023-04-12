'use strict';

angular.module('app.harvestpermit.application.mooselike.map', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.mooselike.map', {
                url: '/map',
                templateUrl: 'harvestpermit/applications/mooselike/map/map.html',
                controller: 'MooselikePermitWizardMapController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    permitArea: function (MooselikePermitApplication, applicationId) {
                        return MooselikePermitApplication.getArea({
                            id: applicationId
                        }).$promise;
                    }
                }
            })

            .state('jht.decision.application.wizard.mooselike.map', {
                url: '/map',
                templateUrl: 'harvestpermit/applications/mooselike/map/map.html',
                controller: 'MooselikePermitWizardMapController',
                controllerAs: '$ctrl',
                resolve: {
                    permitArea: function (MooselikePermitApplication, applicationId) {
                        return MooselikePermitApplication.getArea({
                            id: applicationId
                        }).$promise;
                    }
                }
            });
    })

    .controller('MooselikePermitWizardMapController', function (MooselikePermitApplication, FormPostService,
                                                                wizard, applicationId, permitArea) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.permitArea = permitArea;

            if (permitArea.status !== 'READY') {
                wizard.goto('partners');
            }
        };

        $ctrl.selectTab = function (tabIndex) {
            $ctrl.featureCollection = null;

            if (tabIndex === 0) {
                loadGeometry('union');
            } else if (tabIndex === 1) {
                loadGeometry('partner');
            }
        };

        $ctrl.exportMmlExcel = function () {
            FormPostService.submitFormUsingBlankTarget('/api/v1/application/area/mml/'
                + applicationId + '/print/pdf');
        };

        function loadGeometry(outputStyle) {
            $ctrl.featureCollection = null;

            MooselikePermitApplication.getGeometry({
                id: applicationId,
                outputStyle: outputStyle

            }).$promise.then(function (result) {
                $ctrl.featureCollection = result;
            });
        }

        $ctrl.exit = wizard.exit;

        $ctrl.previous = function () {
            MooselikePermitApplication.setAreaIncomplete({
                id: applicationId
            }).$promise.then(function () {
                wizard.goto('partners');
            });
        };

        $ctrl.next = function () {
            wizard.goto('attachments');
        };
    })

    .service('MooselikePermitWizardAreaProcessingModal', function ($uibModal, $q, $interval,
                                                                   MooselikePermitApplication) {
        this.open = function (applicationId) {
            var modalInstance = $uibModal.open({
                templateUrl: 'harvestpermit/applications/mooselike/map/processing.html',
                size: 'md',
                resolve: {
                    applicationId: _.constant(applicationId)
                },
                controllerAs: '$ctrl',
                controller: ModalController
            });
            return modalInstance.result;
        };

        function checkStatus(applicationId) {
            return MooselikePermitApplication.getAreaStatus({id: applicationId}).$promise.then(function (res) {
                if (res.status === 'PENDING' || res.status === 'PROCESSING') {
                    return $q.reject(res.status);
                }

                return res.status === 'READY' ? $q.when(true) : $q.when(false);
            });
        }

        function ModalController($uibModalInstance, applicationId) {
            var $ctrl = this;
            $ctrl.status = 'PENDING';
            $ctrl.progress = 0;
            $ctrl.progressBarWidth = 7;

            var intervalPromise = $interval(updateProgress, 1000);

            updateProgress();

            function updateProgress() {
                checkStatus(applicationId).then(function (success) {
                    $interval.cancel(intervalPromise);

                    if (success) {
                        $uibModalInstance.close();
                    } else {
                        $uibModalInstance.dismiss();
                    }

                }, function (status) {
                    $ctrl.status = status;

                    if (status === 'PROCESSING') {
                        $ctrl.progressBarWidth = 7 + Math.round(93.0 *
                            (1.0 - 1.0 / Math.exp(++$ctrl.progress / 100.0)));
                    }
                });
            }

        }
    })

    .component('mooselikePermitApplicationAreaDetails', {
        templateUrl: 'harvestpermit/applications/mooselike/map/area-details.html',
        bindings: {
            permitArea: '<',
            onExportMmlExcel: '&'
        }
    })

    .component('permitApplicationUnionMap', {
        templateUrl: 'harvestpermit/applications/mooselike/map/application-map.html',
        bindings: {
            featureCollection: '<'
        },
        controller: function (MapDefaults, MapState, MapBounds, $translate,
                              MoosePermitMapService, leafletData) {
            var $ctrl = this;

            $ctrl.mapId = 'permit-application-partner-map';
            $ctrl.mapDefaults = MapDefaults.create({
                scrollWheelZoom: false
            });
            $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents();
            $ctrl.mapState = MapState.get();

            MapState.updateMapBounds(null, MapBounds.getBoundsOfFinland(), true);

            $ctrl.$onInit = function () {
                setFeatureCollection($ctrl.featureCollection);
            };

            $ctrl.$onChanges = function (c) {
                if (c.featureCollection) {
                    setFeatureCollection(c.featureCollection.currentValue);
                }
            };

            function setFeatureCollection(featureCollection) {
                if (!featureCollection || $ctrl.initDone) {
                    return;
                }

                $ctrl.initDone = true;

                leafletData.getMap($ctrl.mapId).then(function (map) {
                    featureCollection = featureCollection || {features: []};

                    var featureBounds = MapBounds.getBoundsFromGeoJsonFeatureCollection(featureCollection);
                    MapState.updateMapBounds(featureBounds, MapBounds.getBoundsOfFinland(), true);

                    L.geoJson(featureCollection, {
                        style: {
                            fillColor: 'green',
                            weight: 1,
                            opacity: 1,
                            color: '#000',
                            fillOpacity: 0.3
                        }
                    }).addTo(map);

                    map.invalidateSize({reset: true});
                });
            }
        }
    })

    .component('permitApplicationPartnerMap', {
        templateUrl: 'harvestpermit/applications/mooselike/map/application-map.html',
        bindings: {
            featureCollection: '<'
        },
        controller: function (MapDefaults, MapState, MapBounds, $translate,
                              MoosePermitMapService, leafletData) {
            var $ctrl = this;

            $ctrl.mapId = 'permit-application-union-map';
            $ctrl.mapDefaults = MapDefaults.create({
                scrollWheelZoom: false
            });
            $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents();
            $ctrl.mapState = MapState.get();

            MapState.updateMapBounds(null, MapBounds.getBoundsOfFinland(), true);

            $ctrl.$onInit = function () {
                setFeatureCollection($ctrl.featureCollection);
            };

            $ctrl.$onChanges = function (c) {
                if (c.featureCollection) {
                    setFeatureCollection(c.featureCollection.currentValue);
                }
            };

            function setFeatureCollection(featureCollection) {
                if (!featureCollection || $ctrl.initDone) {
                    return;
                }

                $ctrl.initDone = true;

                leafletData.getMap($ctrl.mapId).then(function (map) {
                    var tooltipFunction = createTooltipFunction();

                    featureCollection = featureCollection || {features: []};
                    featureCollection.features = _.sortBy(featureCollection.features, tooltipFunction);

                    var geoJsonLayer = createGeoJsonLayer(featureCollection, tooltipFunction);
                    var layerControl = createLayerControl(geoJsonLayer, tooltipFunction);
                    layerControl.addTo(map);

                    var featureBounds = MapBounds.getBoundsFromGeoJsonFeatureCollection(featureCollection);
                    MapState.updateMapBounds(featureBounds, MapBounds.getBoundsOfFinland(), true);

                    map.invalidateSize({reset: true});
                });
            }

            function createTooltipFunction() {
                var userLanguage = $translate.use() === 'sv' ? 'sv' : 'fi';
                var clubNameGetter = _.property('properties.clubName.' + userLanguage);
                var areaNameGetter = _.property('properties.areaName.' + userLanguage);

                return function (feature) {
                    return clubNameGetter(feature) + ' - ' + areaNameGetter(feature);
                };
            }

            function createLayerControl(geoJsonLayer, tooltipFunction) {
                return L.control.geoJsonLayerControl(geoJsonLayer, {
                    textToggleAll: $translate.instant('global.map.toggleLayers'),
                    layerToLegendTitle: function (layer) {
                        return tooltipFunction(layer.feature);
                    }
                });
            }

            function createGeoJsonLayer(featureCollection, tooltipFunction) {
                var palette = MoosePermitMapService.createPalette(featureCollection);

                return L.geoJson(featureCollection, {
                    style: function (feature) {
                        return {
                            fillColor: palette.resolve(feature),
                            weight: 1,
                            opacity: 1,
                            color: '#000',
                            fillOpacity: 0.3
                        };
                    },
                    onEachFeature: function (feature, layer) {
                        layer.bindPopup(tooltipFunction(feature));
                    }
                });
            }
        }
    });
