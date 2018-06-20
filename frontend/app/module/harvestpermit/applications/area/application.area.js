'use strict';

angular.module('app.harvestpermit.application.area', [])
    .component('permitApplicationAreaList', {
        templateUrl: 'harvestpermit/applications/area/permit-area-list.html',
        bindings: {
            applicationId: '<',
            partners: '<',
            onRefresh: '&'
        },
        controller: function ($translate, dialogs,
                              HarvestPermitApplicationAreaPartners) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.partners = $ctrl.partners || [];
            };

            $ctrl.deletePartner = function (id) {
                var dialogTitle = $translate.instant('harvestpermit.wizard.partners.deleteConfirmation.title');
                var dialogMessage = $translate.instant('harvestpermit.wizard.partners.deleteConfirmation.body');
                var dialog = dialogs.confirm(dialogTitle, dialogMessage);

                dialog.result.then(function () {
                    HarvestPermitApplicationAreaPartners.remove({
                        applicationId: $ctrl.applicationId,
                        partnerId: id
                    }).$promise.then($ctrl.onRefresh);
                });
            };

            $ctrl.updateGeometry = function (id) {
                HarvestPermitApplicationAreaPartners.save({
                    applicationId: $ctrl.applicationId,
                    partnerId: id
                }).$promise.then($ctrl.onRefresh);
            };
        }
    })

    .service('HarvestPermitAreaErrorModal', function ($uibModal) {
        this.showNotFound = function () {
            showErrorModal('harvestpermit.wizard.partners.addClubArea.notFound');
        };

        this.showHuntingYearMismatch = function () {
            showErrorModal('harvestpermit.wizard.partners.addClubArea.huntingYearMismatch');
        };

        this.showMhYearMismatch = function () {
            showErrorModal('harvestpermit.wizard.partners.addClubArea.mhYearMismatch');
        };

        this.showProcessingFailed = function () {
            showErrorModal('harvestpermit.wizard.partners.addClubArea.processingFailed');
        };

        function showErrorModal(localisationKey) {
            $uibModal.open({
                templateUrl: 'harvestpermit/applications/area/error-modal.html',
                controllerAs: '$ctrl',
                controller: function () {
                    this.titleKey = 'harvestpermit.wizard.partners.addClubArea.areaError';
                    this.key = localisationKey;
                }
            });
        }
    })

    .service('AddClubAreaToHarvestPermitApplicationModal', function ($uibModal, Account, NotificationService,
                                                                     HarvestPermitAreaErrorModal,
                                                                     HarvestPermitApplicationAreaPartners) {
        this.open = function (applicationId, huntingYear) {
            var modalInstance = $uibModal.open({
                templateUrl: 'harvestpermit/applications/area/add-club-area.html',
                controller: ModalController,
                controllerAs: '$ctrl',
                resolve: {
                    huntingYear: _.constant(huntingYear),
                    availableHuntingClubs: function (HarvestPermitApplicationAreaPartners, $filter) {
                        var i18n = $filter('rI18nNameFilter');

                        return HarvestPermitApplicationAreaPartners.listAvailable({
                            applicationId: applicationId
                        }).$promise.then(function (result) {
                            return _.map(result, function (club) {
                                return {
                                    id: club.id,
                                    name: i18n(club)
                                };
                            });
                        });
                    }
                }
            });

            return modalInstance.result.then(function (areaExternalId) {
                return HarvestPermitApplicationAreaPartners.save({
                    applicationId: applicationId,
                    externalId: areaExternalId
                }).$promise.then(function () {
                    NotificationService.showDefaultSuccess();
                }, function (response) {
                    if (response.status === 404) {
                        HarvestPermitAreaErrorModal.showNotFound();
                    } else if (response.status === 400) {
                        if (response.data.exception === 'HarvestPermitAreaHuntingYearException') {
                            HarvestPermitAreaErrorModal.showHuntingYearMismatch();
                        }
                        if (response.data.exception === 'MetsahallitusYearMismatchException') {
                            HarvestPermitAreaErrorModal.showMhYearMismatch();
                        }
                    } else {
                        NotificationService.showDefaultFailure();
                    }
                });
            });
        };

        function ModalController($filter, $uibModalInstance, ClubAreas, HuntingYearService,
                                 huntingYear, availableHuntingClubs) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.availableHuntingClubs = availableHuntingClubs;
                $ctrl.availableHuntingClubAreas = [];
                $ctrl.huntingYear = huntingYear;
                $ctrl.huntingClubId = _($ctrl.availableHuntingClubs).map('id').first();
                $ctrl.huntingClubAreaId = null;
                $ctrl.areaExternalId = null;

                fetchHuntingClubAreas($ctrl.huntingClubId);
            };

            $ctrl.onReloadAreas = function () {
                fetchHuntingClubAreas($ctrl.huntingClubId);
            };

            $ctrl.onAreaSelected = function () {
                $ctrl.areaExternalId = _($ctrl.availableHuntingClubAreas)
                    .filter('id', $ctrl.huntingClubAreaId)
                    .map('externalId')
                    .filter()
                    .first();
            };

            function fetchHuntingClubAreas(huntingClubId) {
                var i18n = $filter('rI18nNameFilter');

                $ctrl.availableHuntingClubAreas = [];
                $ctrl.huntingClubAreaId = null;
                $ctrl.areaExternalId = null;

                if (!_.isFinite(huntingYear) || !_.isFinite(huntingClubId)) {
                    return;
                }

                return ClubAreas.query({
                    year: huntingYear,
                    clubId: huntingClubId,
                    activeOnly: true,
                    includeEmpty: false

                }).$promise.then(function (result) {
                    $ctrl.availableHuntingClubAreas = _.map(result, function (area) {
                        return {
                            id: area.id,
                            name: i18n(area),
                            externalId: area.externalId
                        };
                    });
                });
            }

            $ctrl.ok = function () {
                $uibModalInstance.close($ctrl.areaExternalId);
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }
    })

    .component('permitApplicationAreaDetails', {
        templateUrl: 'harvestpermit/applications/area/area-details.html',
        bindings: {
            permitArea: '<'
        }
    })

    .component('permitApplicationUnionMap', {
        templateUrl: 'harvestpermit/applications/area/application-map.html',
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
        templateUrl: 'harvestpermit/applications/area/application-map.html',
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
    })
    .service('HarvestPermitAreaProcessingModal', function ($uibModal, $q, $interval, HarvestPermitApplications) {
        this.open = function (applicationId) {
            var modalInstance = $uibModal.open({
                templateUrl: 'harvestpermit/applications/area/processing.html',
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
            return HarvestPermitApplications.getAreaStatus({id: applicationId}).$promise.then(function (res) {
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
    });
