'use strict';

angular.module('app.harvestpermit.decision.area.fragment', [])

    .component('decisionApplicationFragments', {
        templateUrl: 'harvestpermit/decision/area/fragment/fragment-list.html',
        bindings: {
            applicationId: '<',
            featureCollection: '<'
        },
        controller: function ($state, $q, $filter, $translate, dialogs, FormPostService, TranslatedBlockUI,
                              PermitAreaFragmentStatus, FetchAndSaveBlob,
                              MooselikePermitApplication, MoosePermitMapService, PermitAreaFragmentInfoModal,
                              PermitAreaFragmentLoadingService, MapPdfModal, LocalStorageService,
                              WGS84, MapDefaults, MapState, MapBounds, leafletData) {
            var $ctrl = this;

            $ctrl.mooseFragmentSizeLimitHa = 1000;
            $ctrl.deerFragmentSizeLimitHa = 500;
            $ctrl.fragmentSizeLimitHa = $ctrl.mooseFragmentSizeLimitHa;
            $ctrl.fragmentList = [];
            $ctrl.shownFragments = [];
            $ctrl.fragmentStatus = null;
            $ctrl.filterModes = ['ALL', 'UNCONFIRMED', 'CONFIRMED'];
            $ctrl.filterMode = 'ALL';
            $ctrl.mapId = 'harvest-permit-application-fragment-map';
            $ctrl.mapDefaults = MapDefaults.create({fullscreen: true, doubleClickZoom: false, hideOverlays: true});
            $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents();
            $ctrl.mapState = MapState.get();

            $ctrl.selectedFragment = null;
            $ctrl.selectedFeature = null;
            $ctrl.layers = [];
            $ctrl.$onInit = function () {
                updateFragmentList();
            };

            $ctrl.$onChanges = function (c) {
                if (c.fragmentSizeLimitHa || c.featureCollection) {
                    $ctrl.initializeMap();
                }
            };

            $ctrl.isSelectedFragment = function (fragment) {
                if (fragment && $ctrl.selectedFragment) {
                    return fragment.hash === $ctrl.selectedFragment.hash;
                }
                return false;
            };

            $ctrl.isSelectedFeature = function (feature) {
                if (feature && $ctrl.selectedFeature) {
                    return feature.id === $ctrl.selectedFeature.id;
                }
                return false;
            };

            $ctrl.updateShownFragments = function () {
                switch ($ctrl.filterMode) {
                    case 'ALL': {
                        $ctrl.shownFragments = $ctrl.fragmentList;
                        break;
                    }
                    case 'UNCONFIRMED': {
                        $ctrl.shownFragments = _.filter($ctrl.fragmentList, function (fragment) {
                            return _.isNil($ctrl.fragmentStatus.isFragment(fragment));
                        });
                        break;
                    }
                    case 'CONFIRMED': {
                        $ctrl.shownFragments = _.filter($ctrl.fragmentList, function (fragment) {
                            return $ctrl.fragmentStatus.isFragment(fragment);
                        });
                        break;
                    }
                    default:
                        throw 'Unknown filter mode ' + $ctrl.filterMode;
                }
            };

            $ctrl.verifyFragment = function (f) {
                $ctrl.fragmentStatus.setFragmentStatus(f, true);
                updateFragmentFeature(f);
            };

            $ctrl.clearFragment = function (f) {
                $ctrl.fragmentStatus.setFragmentStatus(f, false);
                updateFragmentFeature(f);
            };

            $ctrl.fragmentSizeChanged = function () {
                updateFragmentList();
            };

            $ctrl.selectFragment = function (fragment) {
                if (!$ctrl.selectedFragment || (fragment && fragment.hash !== $ctrl.selectedFragment.hash)) {
                    var oldFeature = $ctrl.selectedFeature;
                    $ctrl.selectedFragment = fragment || null;
                    $ctrl.selectedFeature = findFeature(fragment);
                    highlightSelected($ctrl.selectedFeature, oldFeature);
                }
            };

            function findFeature(fragment) {
                if (fragment) {
                    return _.find($ctrl.featureCollection.features, function (feature) {
                        return feature.properties.hash && feature.properties.hash === fragment.hash;
                    }) || null;
                }
                return null;
            }

            $ctrl.zoomToFragment = function (fragment) {
                $ctrl.selectFragment(fragment);

                var feature = findFeature(fragment);
                if (feature) {
                    var bounds = MapBounds.getBoundsFromGeoJsonBbox(feature.bbox);
                    MapState.updateMapBounds(bounds, null, true);
                }
            };

            $ctrl.showDialog = function (fragment) {
                if (fragment) {
                    var feature = _.find($ctrl.featureCollection.features, function (feature) {
                        return feature.properties.hash && feature.properties.hash === fragment.hash;
                    });

                    var fragmentStatus =
                        PermitAreaFragmentStatus.create($ctrl.applicationId, $ctrl.fragmentSizeLimitHa * 10000);

                    PermitAreaFragmentInfoModal.showPopup(
                        $ctrl.applicationId, fragment, fragmentStatus, feature
                    ).then(function () {
                        $ctrl.updateShownFragments();
                        $ctrl.initializeMap();
                    });
                }

            };

            function doShowNoFragmentsDialog() {
                var dialogTitleNoFragments = $translate.instant('harvestpermit.application.fragment.pdfNotifyTitle');
                var dialogMessageNoFragments = $translate.instant('harvestpermit.application.fragment.pdfNotifyNoFragments');
                dialogs.notify(dialogTitleNoFragments, dialogMessageNoFragments);
            }

            function isSomeFragmentConfirmed() {
                return _.findIndex($ctrl.fragmentList, function (f) {
                    return !!$ctrl.fragmentStatus.isFragment(f);
                }) > -1;
            }

            $ctrl.printFragments = function () {

                if (!isSomeFragmentConfirmed()) {
                    doShowNoFragmentsDialog();
                    return;
                }

                // Check if some fragment candidate is still waiting processing
                var nullIndex = _.findIndex($ctrl.fragmentList, function (f) {
                    return _.isNil($ctrl.fragmentStatus.isFragment(f));
                });

                if (nullIndex !== -1) {
                    var dialogTitle = $translate.instant('harvestpermit.application.fragment.pdfNotifyTitle');
                    var dialogMessage = $translate.instant('harvestpermit.application.fragment.pdfUnconfirmedExistNotifyText');
                    dialogs.confirm(dialogTitle, dialogMessage).result.then(function () {
                        doPrintFragments();
                    });
                    return;
                }

                doPrintFragments();
            };

            function doPrintFragments() {
                var uri =
                    '/api/v1/harvestpermit/application/mooselike/' + $ctrl.applicationId + '/area/fragments/print';

                var hashes = _.chain($ctrl.fragmentList)
                    .filter(function (f) {
                        return $ctrl.fragmentStatus.isFragment(f);
                    })
                    .map('hash')
                    .value();

                MapPdfModal.showModal().then(function (pdfParameters) {
                    TranslatedBlockUI.start('global.block.wait');
                    FetchAndSaveBlob.post(uri, {
                        fragmentIds: hashes,
                        pdfParameters: pdfParameters
                    }).finally(TranslatedBlockUI.stop);
                });
            }

            $ctrl.exportFragmentExcel = function () {

                if (!isSomeFragmentConfirmed()) {
                    doShowNoFragmentsDialog();
                    return;
                }

                var url = '/api/v1/harvestpermit/application/mooselike/' + $ctrl.applicationId + '/area/fragments/excel';
                var hashes = _.chain($ctrl.fragmentList)
                    .filter(function (f) {
                        return $ctrl.fragmentStatus.isFragment(f);
                    })
                    .map('hash')
                    .value();

                FetchAndSaveBlob.post(url, {
                    fragmentSizeLimitSquareMeters: $ctrl.fragmentSizeLimitHa * 10000,
                    fragmentIds: hashes
                });
            };

            $ctrl.isFragment = function (fragment) {
                return $ctrl.fragmentStatus.isFragment(fragment);
            };

            $ctrl.initializeMap = function () {
                leafletData.getMap($ctrl.mapId).then(function (map) {
                    updateComponent(map, $ctrl.featureCollection, $ctrl.fragmentSizeLimitHa);
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
                        'blue': $translate.instant('harvestpermit.application.mapLegend.fragmentProposal', i18nParams),
                        'red': $translate.instant('harvestpermit.application.mapLegend.fragment', i18nParams),
                        'green': $translate.instant('harvestpermit.application.mapLegend.notFragment', i18nParams)
                    }
                });
            }

            function createGeoJsonLayer(featureCollection, fragmentSizeLimitHa) {
                var fragmentSizeLimit = fragmentSizeLimitHa * 10000;
                var fragmentStatus = PermitAreaFragmentStatus.create($ctrl.applicationId, fragmentSizeLimit);
                $ctrl.layers = [];

                return L.geoJSON(featureCollection, {
                    style: function (feature) {
                        return getFeatureStyle(fragmentStatus.isFeatureFragment(feature), $ctrl.isSelectedFeature(feature));
                    },
                    onEachFeature: function (feature, layer) {
                        $ctrl.layers.push({layer: layer, feature: feature});
                        layer.on('click', function (e) {
                            PermitAreaFragmentLoadingService.getFragmentInfo({
                                applicationId: $ctrl.applicationId,
                                fragmentSizeLimit: fragmentSizeLimit,
                                location: {
                                    latitude: e.latlng.lat,
                                    longitude: e.latlng.lng
                                }

                            }).then(function (fragmentInfo) {
                                $ctrl.selectFragment(fragmentInfo);
                            }, function (error) {
                                // Clear selection when large feature is clicked
                                $ctrl.selectFragment(null);
                            });
                        });
                    }
                });
            }

            function updateFragmentFeature(fragment) {
                if (geoJsonLayer) {
                    geoJsonLayer.eachLayer(function (layer) {
                        if (layer.feature.properties.hash === fragment.hash) {
                            layer.setStyle(getFeatureStyle($ctrl.fragmentStatus.isFragment(fragment), $ctrl.isSelectedFeature(layer.feature)));
                        }
                    });
                }
            }

            function findFeatureLayer(feature) {
                if (feature) {
                    var obj = _.find($ctrl.layers, ['feature.properties.hash', feature.properties.hash]);
                    if (obj) {
                        return obj.layer;
                    }
                }
                return null;
            }

            function highlightSelected(newFeature, oldFeature) {
                var layer = findFeatureLayer(newFeature);
                if (layer) {
                    layer.setStyle({
                        fillColor: layer.options.fillColor,
                        weight: 3,
                        color: '#000',
                        fillOpacity: 0.8
                    });
                }
                clearHighlight(oldFeature);

            }

            function clearHighlight(oldFeature) {
                var layer = findFeatureLayer(oldFeature);
                if (layer) {
                    layer.setStyle({
                        fillColor: layer.options.fillColor,
                        weight: 1,
                        color: '#000',
                        fillOpacity: 0.3
                    });
                }
            }

            function getFeatureStyle(isFragment, isHighlighted) {
                return {
                    fillColor: _.isNil(isFragment)
                        ? 'blue'
                        : isFragment ? 'red' : 'green',
                    weight: isHighlighted ? 3 : 1,
                    color: '#000',
                    fillOpacity: isHighlighted ? 0.8 : 0.3
                };
            }

            function updateFragmentList() {
                $ctrl.fragmentStatus =
                    PermitAreaFragmentStatus.create($ctrl.applicationId, $ctrl.fragmentSizeLimitHa * 10000);

                PermitAreaFragmentLoadingService.getFragmentInfoes({
                    applicationId: $ctrl.applicationId,
                    fragmentSizeLimit: $ctrl.fragmentSizeLimitHa * 10000
                }).then(function (fragments) {
                    $ctrl.fragmentList = fragments;
                    $ctrl.updateShownFragments();
                });
            }

        }
    })

    .factory('PermitAreaFragmentStatus', function (LocalStorageService) {
        function Service(applicationId, fragmentSizeLimit) {
            this.applicationId = applicationId;
            this.fragmentSizeLimit = fragmentSizeLimit;
            this.areaSizeGetter = _.property('properties.areaSize');
            this.hashGetter = _.property('properties.hash');
        }

        var proto = Service.prototype;

        function cacheKey(id, hash) {
            return 'fragment-' + id + '-' + hash;
        }

        proto.isFeatureFragment = function (feature) {
            var hash = this.hashGetter(feature);


            if (this.areaSizeGetter(feature) < this.fragmentSizeLimit) {
                if (hash && LocalStorageService.getKey(cacheKey(this.applicationId, hash)) === '0') {
                    return false;
                }
                if (hash && LocalStorageService.getKey(cacheKey(this.applicationId, hash)) === '1') {
                    return true;
                }
                return null;
            }
            return false;

        };

        proto.isFragment = function (fragment) {
            var hash = fragment.hash;
            if (fragment.bothSize.land < this.fragmentSizeLimit) {

                if (hash && LocalStorageService.getKey(cacheKey(this.applicationId, hash)) === '0') {
                    return false;
                }
                if (hash && LocalStorageService.getKey(cacheKey(this.applicationId, hash)) === '1') {
                    return true;
                }
                return null;
            }
            return false;
        };

        proto.setFragmentStatus = function (fragment, value) {
            var hash = fragment.hash;

            if (hash) {
                LocalStorageService.setKey(cacheKey(this.applicationId, hash), value ? '1' : '0');
            }
        };

        return {
            create: function (applicationId, fragmentSizeLimit) {
                return new Service(applicationId, fragmentSizeLimit);
            }
        };
    })
    .service('PermitAreaFragmentLoadingService', function ($q, TranslatedBlockUI, MooselikePermitApplication) {
        this.getFragmentInfo = function (params) {

            TranslatedBlockUI.start("global.block.wait");

            return MooselikePermitApplication.getGeometryFragmentInfo({
                id: params.applicationId

            }, params).$promise.then(function (data) {
                return _.isEmpty(data) ? $q.reject() : data[0];

            }).finally(function () {
                TranslatedBlockUI.stop();
            });

        };

        this.getFragmentInfoes = function (params) {
            TranslatedBlockUI.start("global.block.wait");

            return MooselikePermitApplication.getGeometryFragmentInfo({
                id: params.applicationId

            }, params).$promise.then(function (data) {
                return _.isEmpty(data) ? $q.reject() : data;

            }).finally(function () {
                TranslatedBlockUI.stop();
            });
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
                $ctrl.fragmentStatus = fragmentStatus.isFragment(fragmentInfo);
                $ctrl.viewBounds = MapBounds.getBoundsFromGeoJsonBbox(fragmentFeature.bbox);
            };

            $ctrl.close = function () {
                $uibModalInstance.close();
            };

            $ctrl.setFragmentStatus = function (value) {
                fragmentStatus.setFragmentStatus(fragmentInfo, value);
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
