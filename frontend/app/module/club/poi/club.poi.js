"use strict";

angular.module('app.clubpoi', [])
    .constant('ClubPoiTypes', {
        SIGHTING_PLACE: 'SIGHTING_PLACE',
        MINERAL_LICK: 'MINERAL_LICK',
        FEEDING_PLACE: 'FEEDING_PLACE',
        OTHER: 'OTHER'
    })
    .config(function ($stateProvider) {
        $stateProvider
            .state('club.area.poi', {
                abstract: true,
                template: '<ui-view autoscroll="false"/>',
                resolve: {
                    canEdit: function ($http, club) {
                        // Use $http to get boolean instead of object
                        return $http.get('api/v1/club/' + club.id + '/poi/editable').then(function (result) {
                            return result.data;
                        });
                    }
                }
            })
            .state('club.area.poi.list', {
                url: '/area/poi/list?poiId',
                controllerAs: '$ctrl',
                wideLayout: true,
                reloadOnSearch: false,
                controller: 'ClubPoiController',
                templateUrl: 'club/poi/club-poi.html',
                resolve: {
                    pois: function ($q, ClubPois, club) {
                        return ClubPois.listPois({id: club.id}).$promise;
                    },
                    selectedPoiId: function ($stateParams) {
                        return _.parseInt($stateParams.poiId);
                    }
                }
            })
            .state('club.area.poi.edit', {
                url: '/area/poi/edit/{poiId}',
                controllerAs: '$ctrl',
                wideLayout: true,
                controller: 'ClubPoiEditController',
                templateUrl: 'club/poi/poi-edit.html',
                resolve: {
                    poi: function ($stateParams, ClubPois, club) {
                        var poiId = _.parseInt($stateParams.poiId);
                        return ClubPois.getPoi({id: club.id}, {poiId: poiId}).$promise;
                    }
                }
            });
    })
    .controller('ClubPoiController', function ($location, $scope, $state, $stateParams, $translate,
                                               ActiveRoleService, ClubPois, ClubPoiMarkerColors, ClubPoiMarkerBounds,
                                               ClubPoiTypes, FetchAndSaveBlob, club, canEdit, dialogs, pois, rhyBounds) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.filterMode = null;
            $ctrl.canEdit = canEdit;
            $ctrl.clubId = club.id;
            $ctrl.rhyBounds = rhyBounds;
            $ctrl.typeOptions = _.values(ClubPoiTypes);
            $ctrl.pois = pois;
            $ctrl.selectedPoiId = null;
            $ctrl.selectedPoi = null;
            $ctrl.visibleMarkers = null;
            $ctrl.filterChanged();

            var poiId = _.parseInt($stateParams.poiId) || _.get(_.first($ctrl.pois), 'id');
            $ctrl.selectPoi(_.find($ctrl.pois, ['id', poiId]));
        };

        $ctrl.isPoiSelected = function (poi) {
            return !!poi && poi.id === $ctrl.selectedPoiId;
        };

        $ctrl.getAreaToggleClasses = function (poi) {
            var selected = poi && poi.isOpen;
            return {
                'glyphicon': true,
                'glyphicon-chevron-down': selected,
                'glyphicon-chevron-right': !selected
            };
        };

        $ctrl.createNewPoi = function (type) {
            ClubPois.createPoi({id: club.id}, {
                type: type,
                locations: []
            }).$promise.then(function (res) {
                goToEditMode(res.id);
            });
        };

        $ctrl.editPoi = function (id) {
            goToEditMode(id);
        };

        function goToEditMode(poiId) {
            $state.go('club.area.poi.edit', {id: $ctrl.clubId, poiId: poiId}, {reload: true});
        }

        $ctrl.deletePoi = function (id) {
            var title = $translate.instant('club.area.poi.deleteDialog.title');
            var message = $translate.instant('club.area.poi.deleteDialog.message');
            dialogs.confirm(title, message).result.then(function (res) {
                if (!!res) {
                    ClubPois.deletePoi({id: club.id}, {poiId: id}).$promise.then(function (deleteRes) {
                        $state.go('club.area.poi.list', {poiId: null}, {reload: true});
                    });
                }
            });
        };

        $ctrl.selectPoi = function (poi) {
            if (!poi) {
                return;
            }

            var id = _.get(poi, 'id');
            if (id !== $ctrl.selectedPoiId) {
                if ($ctrl.selectedPoi) {
                    $ctrl.selectedPoi.isOpen = false;
                }
                $ctrl.selectedPoi = poi;
                $ctrl.selectedPoi.isOpen = true;
                $ctrl.selectedPoiId = poi.id;
                $location.search({poiId: poi.id});
            }
        };

        $ctrl.onMarkerClick = function (markerId) {
            var poi = _.find($ctrl.pois, ['id', _.parseInt(markerId)]);
            if (!!poi) {
                $ctrl.selectPoi(poi);
            }
            $scope.$digest();
        };

        $ctrl.filterChanged = function () {
            $ctrl.visibleMarkers =
                _.chain(pois)
                    .filter(function (poi) {
                        return !$ctrl.filterMode || poi.type === $ctrl.filterMode;
                    })
                    .map(function (poi) {
                        return {
                            locations: poi.locations,
                            color: ClubPoiMarkerColors.deduceColor(poi)
                        };
                    })
                    .flatMap(function (data) {
                        return ClubPoiMarkerColors.assignColor(data.locations, data.color);
                    })
                    .value();
        };

        $ctrl.zoomTo = function (poi) {
            ClubPoiMarkerBounds.zoomToContain(_.filter($ctrl.visibleMarkers, ['poiId', poi.id]));
        };

        $ctrl.exportExcel = function () {
            FetchAndSaveBlob.post('api/v1/club/' + club.id + '/poi/excel');
        };

        $ctrl.exportGpx = function () {
            FetchAndSaveBlob.post('api/v1/club/' + club.id + '/poi/gpx');
        };
    })
    .controller('ClubPoiEditController', function ($location, $state, $stateParams, ActiveRoleService, ClubPois,
                                                   ClubPoiMarkerColors, ClubPoiTypes, NotificationService,
                                                   UnsavedChangesConfirmationService, PoiImportModal,
                                                   club, dialogs, poi, rhyBounds) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.poi = poi;
            $ctrl.clubId = club.id;
            $ctrl.rhyBounds = rhyBounds;
            $ctrl.markerColor = ClubPoiMarkerColors.deduceColor(poi);
            $ctrl.visibleMarkers = ClubPoiMarkerColors.assignColor(poi.locations, $ctrl.markerColor);
        };

        $ctrl.onMapClicked = function (geoLocation) {
            $ctrl.addLocation(geoLocation, null);
        };

        $ctrl.addLocation = function (geoLocation, description) {
            var location = {
                poiId: $ctrl.poi.id,
                description: description,
                visibleId: $ctrl.nextVisibleId(),
                geoLocation: geoLocation,
                color: $ctrl.markerColor
            };

            $ctrl.poi.locations.push(location);
            $ctrl.visibleMarkers = $ctrl.poi.locations;
            UnsavedChangesConfirmationService.setChanges(true);
        };

        $ctrl.deleteLocation = function (visibleId) {
            _.remove($ctrl.poi.locations, ['visibleId', visibleId]);
            $ctrl.visibleMarkers = $ctrl.poi.locations;
            UnsavedChangesConfirmationService.setChanges(true);
        };

        $ctrl.save = function () {
            _.forEach($ctrl.poi.locations, function (location) {
                delete location.color;
            });

            ClubPois.updatePoi({id: $ctrl.clubId}, $ctrl.poi).$promise.then(function (res) {
                $ctrl.poi = res;
                $ctrl.visibleMarkers = ClubPoiMarkerColors.assignColor($ctrl.poi.locations, $ctrl.markerColor);
                UnsavedChangesConfirmationService.setChanges(false);
                NotificationService.showDefaultSuccess();
            });
        };

        $ctrl.close = function () {
            returnToListMode();
        };

        $ctrl.onPoiTextChanged = function () {
            UnsavedChangesConfirmationService.setChanges(true);
        };

        $ctrl.onDragEnded = function (marker) {
            var poiLocation = _.find($ctrl.poi.locations, ['visibleId', marker.id]);
            if (poiLocation) {
                poiLocation.geoLocation = marker.geoLocation;
                UnsavedChangesConfirmationService.setChanges(true);
            }
        };

        $ctrl.importPoiFromGpxFile = function () {
            PoiImportModal.locationsFromGpxFile($ctrl.clubId)
                .then(function (newPoints) {
                    if (_.isArray(newPoints)) {
                        newPoints.forEach(function (point) {
                            $ctrl.addLocation(point.geoLocation, point.description);
                        });
                    }
                });
        };

        $ctrl.nextVisibleId = function () {
            return (_.max(_.map($ctrl.poi.locations, 'visibleId')) || 0) + 1;
        };

        function returnToListMode() {
            $state.go('club.area.poi.list', {id: $ctrl.clubId, poiId: $ctrl.poi.id}, {reload: true});
        }
    })

    .service('ClubPoiMarkerColors', function (ClubPoiTypes) {
        var self = this;

        self.deduceColor = function (poi) {
            switch (poi.type) {
                case ClubPoiTypes.FEEDING_PLACE:
                    return 'orange';
                case ClubPoiTypes.MINERAL_LICK:
                    return 'cadetblue';
                case ClubPoiTypes.SIGHTING_PLACE:
                    return 'darkblue';
                case ClubPoiTypes.OTHER:
                    return 'purple';
                default:
                    throw 'Unknown poi group type: ' + poi.type;
            }
        };

        self.assignColor = function (locations, color) {
            return _.chain(locations)
                .map(function (location) {
                    location.color = color;
                    return location;
                })
                .value();
        };
    })

    .factory('ClubPois', function ($resource) {
        var apiPrefix = 'api/v1/club/:id/poi';

        return $resource(apiPrefix, {"id": "@id"}, {

            // Actual POIs
            listPois: {
                method: 'GET',
                url: apiPrefix + '/pois/list/:clubPoiId',
                params: {clubPoiId: '@clubPoiId'},
                isArray: true
            },
            getPoi: {
                method: 'GET',
                url: apiPrefix + '/pois/get/:poiId',
                params: {poiId: '@poiId'}
            },
            createPoi: {
                method: 'POST',
                url: apiPrefix + '/pois/create'
            },
            updatePoi: {
                method: 'PUT',
                url: apiPrefix + '/pois/update'
            },
            deletePoi: {
                method: 'DELETE',
                url: apiPrefix + '/pois/delete/:poiId',
                params: {poiId: '@poiId'}
            }
        });
    })
    .component('clubPoiMap', {
        templateUrl: 'club/poi/poi-map.html',
        bindings: {
            poiLocations: '<',
            huntingArea: '<',
            defaultBounds: '<',
            selectedPoi: '<',
            editable: '<',
            featureCollection: '<',
            onDragEnded: '&',
            onMarkerClicked: '&',
            onMapClicked: '&'
        },
        controller: function ($scope, MapState, MapDefaults, MapBounds, MapUtil, Markers, ClubPoiViewMarkers, WGS84) {
            var $ctrl = this;

            $ctrl.mapState = MapState.get();
            $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents(['click'], []);
            $ctrl.mapDefaults = MapDefaults.create();
            $ctrl.clickLocation = {latitude: 0, longitude: 0, accuracy: 0, source: "MANUAL"};
            $ctrl.markerWatchOptions = {
                individual: {
                    doWatch: true
                }
            };
            // Club area
            $ctrl.vectorLayer = null;
            $ctrl.mapBounds = null;

            $ctrl.$onInit = function () {
                // Markers
                $ctrl.markers = null;
                setPois($ctrl.poiLocations);
                $ctrl.markerBounds = Markers.getMarkerBounds($ctrl.markers, $ctrl.defaultBounds);
                setFeatureCollection();
            };


            $scope.$watch('$ctrl.clickLocation', function (c) {
                if (MapUtil.isValidGeoLocation(c)) {
                    $ctrl.onMapClicked({geoLocation: c});
                }
            });

            $scope.$watchCollection('$ctrl.poiLocations', function (c) {
                setPois(c);
            });


            $ctrl.$onChanges = function (c) {
                if (c.huntingArea) {
                    updateMap(c.huntingArea.currentValue);
                }

                if (c.featureCollection) {
                    setFeatureCollection();
                }

                if (c.selectedPoi) {
                    setPois($ctrl.poiLocations);
                }

                if (c.poiLocations) {
                    setPois(c.poiLocations.currentValue);
                }

                var combinedBounds = MapBounds.combineBoundsArray([$ctrl.mapBounds, $ctrl.markerBounds]);
                MapState.updateMapBounds(combinedBounds, $ctrl.defaultBounds, $ctrl.forceBoundsCalculation);
            };

            function markerClicked(markerId) {
                var split = markerId.split(':');
                $ctrl.onMarkerClicked({markerId: split[0]});
            }

            function dragEnded(markerId, latlng) {
                var split = markerId.split(':');

                var geoLocation = WGS84.toETRS(latlng.lat, latlng.lng);
                var lat = Math.round(geoLocation.lat);
                var lng = Math.round(geoLocation.lng);
                if (lat && lng) {
                    $ctrl.onDragEnded({
                        marker: {
                            id: _.parseInt(split[1]),
                            geoLocation: {
                                latitude: lat,
                                longitude: lng,
                                accuracy: 0,
                                source: 'MANUAL'
                            }
                        }
                    });
                }
            }

            function setPois(pois) {
                if (pois) {
                    $ctrl.markers = ClubPoiViewMarkers.createMarkers(
                        pois, $ctrl.selectedPoi, $ctrl.editable, markerClicked, dragEnded, $scope);
                    $ctrl.markerBounds = _.size($ctrl.markers) > 0 ? Markers.getMarkerBounds($ctrl.markers) : null;
                }
            }

            function updateMap(huntingArea) {
                if (huntingArea && huntingArea.bounds && huntingArea.areaId) {
                    var bounds = huntingArea.bounds;
                    var vectorLayerTemplate = _.template('/api/v1/vector/hunting-club-area/<%= id %>/{z}/{x}/{y}');

                    $ctrl.vectorLayer = {
                        url: vectorLayerTemplate({id: huntingArea.areaId}),
                        bounds: {
                            southWest: {lat: bounds.minLat, lng: bounds.minLng},
                            northEast: {lat: bounds.maxLat, lng: bounds.maxLng}
                        }
                    };
                    $ctrl.mapBounds = $ctrl.vectorLayer.bounds;

                } else {
                    $ctrl.vectorLayer = null;
                    $ctrl.mapBounds = null;
                }
            }

            function setFeatureCollection() {
                var geometriesIncluded = $ctrl.featureCollection &&
                    _($ctrl.featureCollection.features).map('geometry').some(_.isObject);

                if (geometriesIncluded) {
                    $ctrl.geojson = {
                        data: $ctrl.featureCollection,
                        style: MapDefaults.getGeoJsonOptions()
                    };
                    var bounds = MapBounds.getBoundsFromGeoJsonFeatureCollection($ctrl.featureCollection);
                    MapState.updateMapBounds(bounds, $ctrl.markerBounds, true);

                } else {
                    $ctrl.geojson = null;
                    MapState.updateMapBounds(null, $ctrl.initialViewBounds, true);
                }
            }
        }
    })

    .service('ClubPoiViewMarkers', function (Markers) {

        var markerDefaults = {
            icon: {
                type: 'awesomeMarker',
                prefix: 'fa', // font-awesome
            },
            groupOption: {
                disableClusteringAtZoom: 0 // Disable fully
            },
            tooltipOptions: {
                permanent: true,
                direction: 'bottom'
            }
        };

        this.createMarkers = function (locations, selectedPoi, draggable, clickHandler, dragHandler, $scope) {
            function createMarkerData(location) {
                var isSelected = selectedPoi && selectedPoi.id === location.poiId;

                var msg = isSelected
                    ? selectedPoi.visibleId + '-' + location.visibleId
                    : null;

                return [{
                    id: location.poiId + ':' + location.visibleId,
                    etrsCoordinates: location.geoLocation,
                    getMessageScope: function () {
                        return $scope;
                    },
                    draggable: draggable,
                    icon: {
                        markerColor: location.color,
                        icon: location.icon || 'circle',
                    },
                    tooltipMessage: msg,
                    group: 'poi',
                    dragHandler: dragHandler ? dragHandler : _.noop,
                    clickHandler: clickHandler ? clickHandler : _.noop // sanity check
                }];
            }

            return Markers.transformToLeafletMarkerData(locations, markerDefaults, createMarkerData);
        };
    })

    .component('clubPoiDetails', {
        templateUrl: 'club/poi/poi-details.html',
        bindings: {
            editable: '<',
            poi: '<',
            onDeletePoi: '&',
            onEditPoi: '&',
            zoomTo: '&'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.editPoi = function () {
                $ctrl.onEditPoi({id: $ctrl.poi.id});
            };

            $ctrl.deletePoi = function () {
                $ctrl.onDeletePoi({id: $ctrl.poi.id});
            };

        }
    })

    .component('clubPoiLocationDetails', {
        templateUrl: 'club/poi/poi-location-details.html',
        bindings: {
            poiId: '<',
            poiLocations: '<'
        }
    })

    .component('clubPoiMapMarkerHideSwitch', {
        templateUrl: 'club/poi/poi-marker-hide-tool.html',
        bindings: {
            hideMarkers: '<',
            onChange: '&'
        }
    })

    .service('ClubPoiMarkerBounds', function (MapState, Markers, WGS84) {
        var self = this;

        self.zoomToContain = function (locations) {
            var latlngs = _.chain(locations)
                .map(function (loc) {
                    return WGS84.fromETRS(loc.geoLocation.latitude, loc.geoLocation.longitude);
                })
                .value();

            var bounds = Markers.getMarkerBounds(latlngs, MapState.get().viewBounds);
            MapState.updateMapBounds(bounds, null, true);
        };
    })

    .service('PoiImportModal', function($uibModal, NotificationService) {
        this.locationsFromGpxFile = function (clubId, poiId) {
            return $uibModal.open({
                templateUrl: 'club/poi/poi-import-modal.html',
                size: 'md',
                controller: ModalController,
                controllerAs: '$ctrl',
                bindToController: true,
                resolve: {
                    clubId: _.constant(clubId),
                    poiId: _.constant(poiId)
                }
            }).result.then(function (convertedPoints) {
                return convertedPoints;
            });
        };

        function ModalController($uibModalInstance, clubId) {
            var $ctrl = this;

            $ctrl.url = 'api/v1/club/' + clubId + '/poi/gpx/convert';
            $ctrl.uploadInProgress = false;

            $ctrl.onUpload = function (files) {
                $ctrl.uploadInProgress = true;
            };

            $ctrl.onSuccess = function (response) {
                $uibModalInstance.close(response.data);
                NotificationService.showDefaultSuccess();
            };

            $ctrl.onError = function (response) {
                $uibModalInstance.dismiss();
            };

            $ctrl.close = function () {
                $uibModalInstance.dismiss();
            };
        }
    })
;


