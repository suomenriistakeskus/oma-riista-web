'use strict';

angular.module('app.harvestpermit.decision.area.fragment', [])

    .component('decisionApplicationFragmentMap', {
        templateUrl: 'harvestpermit/decision/area/fragment/fragment-map.html',
        bindings: {
            applicationId: '<',
            featureCollection: '<'
        },
        controller: function ($q, $filter, $translate, FormPostService, TranslatedBlockUI,
                              WGS84, MapDefaults, MapState, MapBounds, leafletData, PermitAreaFragmentStatus,
                              MooselikePermitApplication, MoosePermitMapService, PermitAreaFragmentInfoModal) {
            var $ctrl = this;

            $ctrl.mapId = 'harvest-permit-application-fragment-map';
            $ctrl.mapDefaults = MapDefaults.create({fullscreen: true, doubleClickZoom: false, hideOverlays: true});
            $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents();
            $ctrl.mapState = MapState.get();
            $ctrl.mooseFragmentSizeLimitHa = 1000;
            $ctrl.deerFragmentSizeLimitHa = 500;
            $ctrl.fragmentSizeLimitHa = $ctrl.mooseFragmentSizeLimitHa;

            $ctrl.$onChanges = function (c) {
                if (c.featureCollection) {
                    $ctrl.initializeMap();
                }
            };

            $ctrl.initializeMap = function () {
                leafletData.getMap($ctrl.mapId).then(function (map) {
                    updateComponent(map, $ctrl.featureCollection, $ctrl.fragmentSizeLimitHa);
                });
            };

            $ctrl.exportFragmentExcel = function () {
                var url = '/api/v1/harvestpermit/application/mooselike/' + $ctrl.applicationId + '/area/fragments/excel';

                FormPostService.submitFormUsingBlankTarget(url, {
                    fragmentSizeLimit: $ctrl.fragmentSizeLimitHa * 10000
                });
            };

            var geoJsonLayer = null;
            var legendControl = null;

            function updateComponent(map, featureCollection, fragmentSizeLimitHa) {
                if (geoJsonLayer) {
                    geoJsonLayer.eachLayer(function (layer) {
                        map.removeLayer(layer);
                    });
                    geoJsonLayer = null;
                }

                if (legendControl) {
                    map.removeControl(legendControl);
                }

                legendControl = createLegendControl(fragmentSizeLimitHa);
                legendControl.addTo(map);

                var featureCollectionNotEmpty = featureCollection && _.size(featureCollection.features) > 0;

                if (featureCollectionNotEmpty) {
                    var featureBounds = MapBounds.getBoundsFromGeoJsonFeatureCollection(featureCollection);
                    MapState.updateMapBounds(featureBounds, null, true);

                    geoJsonLayer = createGeoJsonLayer(featureCollection, fragmentSizeLimitHa);
                    geoJsonLayer.addTo(map);

                } else {
                    MapState.updateMapBounds({}, null, true);
                }
            }

            function createLegendControl(fragmentSizeLimitHa) {
                var i18nParams = {limit: fragmentSizeLimitHa};

                return L.control.simpleLegend({
                    position: 'bottomright',
                    legend: {
                        'red': $translate.instant('harvestpermit.application.mapLegend.fragment', i18nParams),
                        'green': $translate.instant('harvestpermit.application.mapLegend.notFragment', i18nParams)
                    }
                });
            }

            function createGeoJsonLayer(featureCollection, fragmentSizeLimitHa) {
                var fragmentSizeLimit = fragmentSizeLimitHa * 10000;
                var fragmentStatus = PermitAreaFragmentStatus.create(fragmentSizeLimit);

                return L.geoJSON(featureCollection, {
                    style: function (feature) {
                        return getFeatureStyle(fragmentStatus.isFragment(feature));
                    },
                    onEachFeature: function (feature, layer) {
                        layer.on('click', function (e) {
                            getFragmentInfo({
                                applicationId: $ctrl.applicationId,
                                fragmentSizeLimit: fragmentSizeLimit,
                                location: {
                                    latitude: e.latlng.lat,
                                    longitude: e.latlng.lng
                                }

                            }).then(function (fragmentInfo) {
                                PermitAreaFragmentInfoModal.showPopup(
                                    $ctrl.applicationId, fragmentInfo, fragmentStatus, feature
                                ).then(function () {
                                    layer.setStyle(getFeatureStyle(fragmentStatus.isFragment(feature)));
                                });
                            });
                        });
                    }
                });
            }

            function getFeatureStyle(isFragment) {
                return {
                    fillColor: isFragment ? 'red' : 'green',
                    weight: 1,
                    color: '#000',
                    fillOpacity: 0.3
                };
            }

            function getFragmentInfo(params) {
                TranslatedBlockUI.start("global.block.wait");

                return MooselikePermitApplication.getGeometryFragmentInfo({
                    id: params.applicationId

                }, params).$promise.then(function (data) {
                    return _.isEmpty(data) ? $q.reject() : data[0];

                }).finally(function () {
                    TranslatedBlockUI.stop();
                });
            }
        }
    })

    .factory('PermitAreaFragmentStatus', function (LocalStorageService) {
        function Service(fragmentSizeLimit) {
            this.fragmentSizeLimit = fragmentSizeLimit;
            this.areaSizeGetter = _.property('properties.areaSize');
            this.hashGetter = _.property('properties.hash');
        }

        var proto = Service.prototype;

        proto.isFragment = function (feature) {
            var hash = this.hashGetter(feature);

            if (hash && LocalStorageService.getKey('fragment-' + hash) === '0') {
                return false;
            }

            return this.areaSizeGetter(feature) < this.fragmentSizeLimit;
        };

        proto.setFragmentStatus = function (feature, value) {
            var hash = this.hashGetter(feature);

            if (hash) {
                LocalStorageService.setKey('fragment-' + hash, value ? null : '0');
            }
        };

        return {
            create: function (fragmentSizeLimit) {
                return new Service(fragmentSizeLimit);
            }
        };
    })

    .service('PermitAreaFragmentInfoModal', function ($uibModal, MapDefaults, MapBounds, MapPdfModal, leafletData) {
        this.showPopup = function (applicationId, fragmentInfo, fragmentStatus, fragmentFeature) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/area/fragment/fragment-info.html',
                resolve: {
                    applicationId: _.constant(applicationId),
                    fragmentInfo: _.constant(fragmentInfo),
                    fragmentStatus: _.constant(fragmentStatus),
                    fragmentFeature: _.constant(fragmentFeature)
                },
                size: 'lg',
                controllerAs: '$ctrl',
                controller: ModalController
            }).result;
        };

        function ModalController($uibModalInstance, applicationId, fragmentInfo, fragmentStatus, fragmentFeature) {
            var $ctrl = this;

            $ctrl.mapId = 'harvest-permit-application-fragment-details-map';
            $ctrl.mapDefaults = MapDefaults.create({fullscreen: true, doubleClickZoom: false, hideOverlays: true});
            $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents();

            $ctrl.$onInit = function () {
                $ctrl.data = fragmentInfo;
                $ctrl.fragmentStatus = fragmentStatus.isFragment(fragmentFeature);
                $ctrl.viewBounds = MapBounds.getBoundsFromGeoJsonBbox(fragmentFeature.bbox);
            };

            $ctrl.close = function () {
                $uibModalInstance.close();
            };

            $ctrl.setFragmentStatus = function (value) {
                fragmentStatus.setFragmentStatus(fragmentFeature, value);
            };

            $ctrl.printMapPdf = function () {
                var uriTemplate = _.template('/api/v1/harvestpermit/application/mooselike/<%= applicationId %>/area/fragments/<%= fragmentId %>/print');

                MapPdfModal.printArea(uriTemplate({
                    applicationId: applicationId,
                    fragmentId: fragmentInfo.hash
                }));
            };

            leafletData.getMap($ctrl.mapId).then(function (map) {
                L.geoJSON(fragmentFeature, {
                    style: {
                        weight: 2.5,
                        color: 'red',
                        fillColor: 'red',
                        fillOpacity: 0.25
                    }
                }).addTo(map);
            });
        }
    });
