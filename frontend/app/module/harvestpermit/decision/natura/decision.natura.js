'use strict';

angular.module('app.harvestpermit.decision.natura', [])
    .config(function ($stateProvider) {

        function createMarker(latlng, markerColor, message) {
            return {
                lat: latlng.lat,
                lng: latlng.lng,
                draggable: false,
                message: message,
                focus: false,
                icon: {
                    prefix: 'fa',
                    type: 'awesomeMarker',
                    icon: '',
                    markerColor: markerColor
                },
                group: 'applicationLocationGroup'
            };
        }

        function createApplicationLocationMarker($translate, WGS84, geoLocation) {
            var latlng = WGS84.fromETRS(geoLocation.latitude, geoLocation.longitude),
                markerColor = 'red',
                message = $translate.instant('harvestpermit.wizard.map.applicationAreaMap');
            return createMarker(latlng, markerColor, message);
        }

        $stateProvider.state('jht.decision.natura', {
            url: '/natura',
            templateUrl: 'harvestpermit/decision/natura/natura.html',
            resolve: {
                decision: function (PermitDecision, decisionId) {
                    return PermitDecision.get({id: decisionId}).$promise;
                },
                applicationId: function (PermitDecision, decisionId) {
                    return PermitDecision.getApplication({id: decisionId}).$promise.then(function (res) {
                        return res.id;
                    });
                },
                category: function (HarvestPermitApplications, applicationId) {
                    return HarvestPermitApplications.get({id: applicationId}).$promise.then(function (app) {
                        return app.harvestPermitCategory;
                    });
                },
                applicationLocations: function ($translate, WGS84, Helpers, DogUnleashApplication,
                                                DogDisturbanceApplication, BirdPermitApplication, applicationId,
                                                category) {
                    switch (category) {
                        case 'DOG_UNLEASH':
                            return DogUnleashApplication.getFullDetails({id: applicationId}).$promise.then(function (app) {
                                return [createApplicationLocationMarker($translate, WGS84, app.derogationPermitApplicationAreaDTO.geoLocation)].concat(app.events.map(
                                    function (event) {
                                        var latlng = WGS84.fromETRS(event.geoLocation.latitude, event.geoLocation.longitude);
                                        var markerColor = 'green';
                                        var message = $translate.instant('harvestpermit.wizard.dogunleash.eventDetails.eventTypes.' + event.eventType)
                                            + ' ' + Helpers.dateIntervalToString(event.beginDate, event.endDate);

                                        return createMarker(latlng, markerColor, message);
                                    }));
                            });
                        case 'DOG_DISTURBANCE':
                            return DogDisturbanceApplication.getFullDetails({id: applicationId}).$promise.then(function (app) {
                                return [createApplicationLocationMarker($translate, WGS84, app.derogationPermitApplicationAreaDTO.geoLocation)];
                            });
                        case 'BIRD':
                            return BirdPermitApplication.getFullDetails({id: applicationId}).$promise.then(function (app) {
                                return [createApplicationLocationMarker($translate, WGS84, app.protectedArea.geoLocation)];
                            });
                        default:
                            return null;
                    }
                }
            },
            controllerAs: '$ctrl',
            controller: 'DecisionNaturaController'
        });
    })

    .controller('DecisionNaturaController', function ($filter, $scope, $translate, MapDefaults, MapState, MapUtil,
                                                      Markers, GIS, WGS84, NaturaAreaMarkerService, NotificationService,
                                                      MeasureDistanceTool, MeasureAreaTool, leafletData, decision,
                                                      applicationId, applicationLocations) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.decision = decision;
            $ctrl.applicationId = applicationId;
            $ctrl.currentTool = 'selectNaturaArea';
            $ctrl.eventMarkers = applicationLocations;
            $ctrl.markers = [];
            // For selectNaturaArea
            $ctrl.selectedAreaId = '';
            $ctrl.selectedAreas = NaturaAreaMarkerService.getMarkers(applicationId);
            // For measurements
            $ctrl.measurements = [];

            updateMarkers();

            // Zoom to markers
            var markerBounds = Markers.getMarkerBounds($ctrl.markers);
            MapState.updateMapBounds(markerBounds, null, true);

            $ctrl.mapDefaults = MapDefaults.create({fullscreen: true});
            $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents(['click']);
            $ctrl.mapState = MapState.get();

            $ctrl.onClickMapButton = function (toolName) {
                return function (btn, map) {
                    if (!L.DomUtil.hasClass(btn.button, 'disabled')) {
                        if (toolName === 'clearMeasurements') {
                            $ctrl.clearMeasurements();
                        } else {
                            $ctrl.selectTool(toolName);
                        }
                    }
                };
            };

            function toolButtonHtml(toolIcon, toolName) {
                return '<span class="fa fa-fw ' + toolIcon + '" ></span>&nbsp;' +
                    '<span>' + $translate.instant('decision.natura.buttons.' + toolName) + '</span>';
            }

            function newToolButton(toolIcon, toolName) {
                var newButton =  L.easyButton(
                    toolButtonHtml(toolIcon, toolName),
                    $ctrl.onClickMapButton(toolName),
                    { leafletClasses: false }
                );
                L.DomUtil.addClass(newButton.button, 'btn btn-default');
                return newButton;
            }

            $ctrl.mapButtons = {
                'measureDistance': newToolButton('fa-arrows-h', 'measureDistance'),
                'measureArea': newToolButton('fa-arrows', 'measureArea'),
                'clearMeasurements': newToolButton('fa-trash', 'clearMeasurements'),
                'measureDistanceForArea': newToolButton('fa-map-marker', 'measureDistanceForArea')};
            $ctrl.mapButtons.measureDistanceForArea.disable();

            leafletData.getMap('naturaAreaMap').then(function (map) {

                findNaturaAreasForEventLocations(map);

                var buttons = L.easyBar(Object.values($ctrl.mapButtons), { leafletClasses: false });
                L.DomUtil.setClass(buttons.container, 'easy-button-container');

                buttons.addTo(map);

                map.on('click', function (event) {
                    switch ($ctrl.currentTool) {
                        case 'measureDistance':
                            L.DomEvent.stopPropagation(event);
                            doMeasureDistanceClick(map, event);
                            break;
                        case 'measureDistanceForArea':
                            L.DomEvent.stopPropagation(event);
                            doMeasureDistanceForAreaClick(map, event);
                            break;
                        case 'measureArea':
                            L.DomEvent.stopPropagation(event);
                            doMeasureAreaClick(map, event);
                            break;
                        case 'selectNaturaArea':
                            doSelectNaturaAreaClick(map, event);
                            break;
                        default:
                            break;
                    }
                });

                map.on('dblclick', function (event) {
                    switch ($ctrl.currentTool) {
                        case 'measureArea':
                            L.DomEvent.stopPropagation(event);
                            $ctrl.measurements.push(MeasureAreaTool.finishMeasurement());
                            break;
                        default:
                            break;
                    }
                });

                map.on('mousemove', function (event) {
                    switch ($ctrl.currentTool) {
                        case 'measureDistanceForArea':
                        case 'measureDistance':
                            MeasureDistanceTool.updateEndpoint(event.latlng);
                            break;
                        case 'measureArea':
                            MeasureAreaTool.updateEndpoint(event.latlng);
                            break;
                        default:
                            break;
                    }
                });

            });
        };

        $ctrl.selectArea = function (naturaId) {
            $ctrl.selectedAreaId = naturaId;
            NaturaAreaMarkerService.select(applicationId, $ctrl.selectedAreaId);
            updateMarkers();
        };

        $ctrl.removeArea = function (naturaId) {
             NaturaAreaMarkerService.remove(applicationId, naturaId);
             updateMarkers();
             $ctrl.selectedAreaId = $ctrl.selectedAreaId === naturaId ? '' : $ctrl.selectedAreaId;
        };

        $ctrl.selectTool = function (newTool) {
            MeasureDistanceTool.reset();
            MeasureAreaTool.reset();
            $ctrl.currentTool = $ctrl.currentTool === newTool ? 'selectNaturaArea' : newTool;
            updateMapButtonStates();
        };

        $ctrl.buttonStyle = function (tool) {
            return $ctrl.currentTool === tool ? 'btn-primary' : 'btn-default';
        };

        $ctrl.clearMeasurements = function () {
            $ctrl.measurements.forEach(function (mea) {
                mea.remove();
            });
            $ctrl.measurements = [];
        };

        function updateMapButtonStates() {
            Object.keys($ctrl.mapButtons).forEach(function (tool) {
                var button = $ctrl.mapButtons[tool];
                if(tool === $ctrl.currentTool) {
                    L.DomUtil.addClass(button.button, 'btn-primary');
                } else {
                    L.DomUtil.removeClass(button.button, 'btn-primary');
                }
            });
        }

        function updateMarkers() {
            $ctrl.markers = $ctrl.eventMarkers.concat(NaturaAreaMarkerService.getMarkers(applicationId));
        }

        function doMeasureDistanceClick(map, event) {
            if (MeasureDistanceTool.isMeasuring()) {
                $ctrl.measurements.push(MeasureDistanceTool.finishMeasurement());
            } else {
                MeasureDistanceTool.startMeasurement(map, event.latlng);
            }
        }

        function doMeasureDistanceForAreaClick(map, event) {
            if (MeasureDistanceTool.isMeasuring()) {
                NaturaAreaMarkerService.updateDistance(applicationId, $ctrl.selectedAreaId, MeasureDistanceTool.getDistance());
                $ctrl.measurements.push(MeasureDistanceTool.finishMeasurement());
                $ctrl.selectTool('selectNaturaArea');
                updateMarkers();
            } else {
                MeasureDistanceTool.startMeasurement(map, event.latlng);
            }
        }

        function doMeasureAreaClick(map, event) {
            if (MeasureAreaTool.isMeasuring()) {
                MeasureAreaTool.addLatlng(event.latlng);
            } else {
                MeasureAreaTool.startMeasurement(map, event.latlng);
            }
        }

        function naturaInfoOk(event) {
            return function (rsp) {
                if (isNaturaAreaSelected(rsp)) {
                    // Clicked on Natura area
                    $ctrl.selectedAreaId = rsp.data.naturaId;
                    if (isNewAreaSelected(rsp)) {
                        NaturaAreaMarkerService.newMarker(applicationId, event.latlng, rsp.data);
                    }
                } else {
                    // Clicked outside Natura areas
                    $ctrl.selectedAreaId = "";
                }
                NaturaAreaMarkerService.select(applicationId, $ctrl.selectedAreaId);
                updateMarkers();
            };
        }

        function naturaInfoFailed() {
            return function () {
                NotificationService.showMessage('decision.natura.naturaInfoServiceError', 'error');
            };
        }

        function doSelectNaturaAreaClick(map, event) {
            if (isValidEvent(event)) {
                GIS.getNaturaInfoLatlng(map, event.latlng).then(naturaInfoOk(event), naturaInfoFailed());
            }
        }

        function isNaturaAreaSelected(rsp) {
            return _.isObject(rsp.data);
        }

        function isNewAreaSelected(rsp) {
            return _.findIndex($ctrl.selectedAreas, function (area) {
                return area.info.naturaId === rsp.data.naturaId;
            }) === -1;
        }

        function isValidEvent(event) {
            return !_.isNil(event) && !_.isNil(event.latlng);
        }

        function findNaturaAreasForEventLocations(map) {
            $ctrl.eventMarkers.forEach(function (eventMarker) {
                GIS.getNaturaInfoLatlng(map, eventMarker).then(function (rsp) {
                    if (_.isObject(rsp.data)) {
                        rsp.data.distance = '0 m'; // Marker is inside Natura-area
                        markApplicationLocationInNaturaArea(eventMarker, rsp.data);
                        NaturaAreaMarkerService.newMarker(applicationId, eventMarker, rsp.data);
                    }
                    updateMarkers();
                    NaturaAreaMarkerService.select(applicationId, $ctrl.selectedAreaId);
                }, naturaInfoFailed());
            });
        }

        function markApplicationLocationInNaturaArea(marker, info) {
            marker.icon.icon = 'exclamation';
            marker.group = 'naturaAreaGroup' + info.naturaId;  /* group with area marker */
        }

        $scope.$watch('$ctrl.selectedAreaId', function () {
            if ($ctrl.selectedAreaId === '') {
                $ctrl.mapButtons.measureDistanceForArea.disable();
            } else {
                $ctrl.mapButtons.measureDistanceForArea.enable();
            }
        });
    })

    /**
     *  MeasureDistanceTool
     */

    .service('MeasureDistanceTool', function () {

        var line = null;
        var self = this;

        self.startMeasurement = function (map, latlng) {
            line = L.polyline([[latlng.lat, latlng.lng], [latlng.lat, latlng.lng]], {
                showMeasurements: true,
                measurementOptions: { minPixelDistance: 70 }
            });

            line.on('click', function(e) {
                if (!self.isMeasuring()) {
                    L.DomEvent.stopPropagation(e);
                    e.target.remove();
                }
            });

            line.addTo(map);
        };

        self.updateEndpoint = function (latlng) {
            if (self.isMeasuring()) {
                var latlngs = line.getLatLngs();
                latlngs[1] = [latlng.lat, latlng.lng];
                line.setLatLngs(latlngs);
            }
        };

        self.finishMeasurement = function () {
            var completedMeasurement = line;
            line = null;
            return completedMeasurement;
        };

        self.isMeasuring = function () {
            return !_.isNil(line);
        };

        self.getDistance = function () {
            var latlngs = line.getLatLngs();
            return line.formatDistance(latlngs[0].distanceTo(latlngs[1]));
        };

        self.reset = function () {
            if (line) {
                line.remove();
                line = null;
            }
        };

    })

    /**
     *  MeasureAreaTool
     */

    .service('MeasureAreaTool', function () {

        var self = this;
        var area = null;
        var line = null;

        var formatArea = function (area) {
            area = area / 10000;
            if (area < 100) {
                return area.toFixed(2) + ' ha';
            } else {
                return Math.round(area) + ' ha';
            }
        };

        self.startMeasurement = function (map, latlng) {
            line = L.polyline([[latlng.lat, latlng.lng], [latlng.lat, latlng.lng]]);
            area = L.polygon([[latlng.lat, latlng.lng]], {
                showMeasurements: true,
                measurementOptions: {
                    minPixelDistance: 70,
                    formatArea: formatArea,
                    showDistances: false
                }});

            area.on('click', function(e) {
                if (!self.isMeasuring()) {
                    L.DomEvent.stopPropagation(e);
                    e.target.remove();
                }
            });

            area.addTo(map);
            line.addTo(map);
        };

        self.addLatlng = function (latlng) {
            area.addLatLng(latlng);
            line.setLatLngs([[latlng.lat, latlng.lng], [latlng.lat, latlng.lng]]);
        };

        self.updateEndpoint = function (latlng) {
            if (self.isMeasuring()) {
                var latlngs = line.getLatLngs();
                latlngs[1] = [latlng.lat, latlng.lng];
                line.setLatLngs(latlngs);
            }
        };

        self.finishMeasurement = function () {
            var completedMeasurement = area;
            line.remove();
            area = null;
            line = null;
            return completedMeasurement;
        };

        self.isMeasuring = function () {
            return !_.isNil(area);
        };

        self.reset = function () {
            if (area) {
                area.remove();
                area = null;
            }
            if (line) {
                line.remove();
                line = null;
            }
        };

    })

    /**
     *  NaturaAreaMarkerService
     */

    .service('NaturaAreaMarkerService', function ($filter, $translate) {

        var selectedAreasByApplicationId = {};
        var i18n = $filter('rI18nNameFilter');
        var self = this;

        self.getMarkers = function (applicationId) {
            return getMarkers(applicationId);
        };

        self.newMarker = function (applicationId, latlng, info) {
            self.add(applicationId, createNaturaMarker(latlng, info));
        };

        self.add = function (applicationId, newArea) {
            if (_.findIndex(getMarkers(applicationId), function (area) {
                return area.info.naturaId === newArea.info.naturaId;
            }) === -1) {
                selectedAreasByApplicationId[applicationId].push(newArea);
            }
        };

        self.remove = function (applicationId, naturaId) {
            _.remove(getMarkers(applicationId), function (area) {
                return area.info.naturaId === naturaId;
            });
        };

        self.updateDistance = function (applicationId, naturaId, distance) {
            getMarkers(applicationId).forEach(
                function (area) {
                    if (area.info.naturaId === naturaId) {
                        area.info.distance = distance;
                    }
                });
        };

        self.select = function (applicationId, focusedAreaId) {
            getMarkers(applicationId).forEach(
                function (area) {
                    area.icon.icon = area.info.naturaId === focusedAreaId ? 'circle' : '';
                });
        };

        self.copyToClipboard = function (applicationId) {
            var tempInput = document.createElement("textarea");
            tempInput.value = asText(applicationId);
            document.body.appendChild(tempInput);
            tempInput.select();
            document.execCommand("copy");
            document.body.removeChild(tempInput);
        };

        function getMarkers(applicationId) {
            if (_.isNil(selectedAreasByApplicationId[applicationId])) {
                selectedAreasByApplicationId[applicationId] = [];
            }
            return selectedAreasByApplicationId[applicationId];
        }

        function asText(applicationId) {
            var text = '';
            getMarkers(applicationId).forEach(function (area) {
                text += i18n(area.info) + '\n'
                    + area.info.naturaId + ', '
                    + area.info.conservationId + ', '
                    + area.info.areaHectares + ' ' + $translate.instant('global.hectares') + ', '
                    + (_.isNil(area.info.distance) ? $translate.instant('decision.natura.areaInfo.notMeasured') : area.info.distance) + '\n\n';

            });
            return text;
        }

        function createNaturaMarker(latlng, info) {
            return {
                lat: latlng.lat,
                lng: latlng.lng,
                draggable: false,
                message: i18n(info), /* localised name as popup */
                focus: true,
                icon: {
                    prefix: 'fa',
                    type: 'awesomeMarker',
                    icon: 'circle'
                },
                info: angular.copy(info),
                group: 'naturaAreaGroup' + info.naturaId  /* for no grouping */
            };
        }

    })

    /**
     *  <r-natura-area-info>
     */
    .component('rNaturaAreaInfo', {
        templateUrl: 'harvestpermit/decision/natura/area-info.html',
        bindings: {
            areaInfo: '<',
            onRemove: '&',
            selectedAreaId: '<'
        },
        controller: function ($filter) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                var i18n = $filter('rI18nNameFilter');
                $ctrl.name = i18n($ctrl.areaInfo);
            };

            $ctrl.isSelected = function() {
                return $ctrl.selectedAreaId === $ctrl.areaInfo.naturaId;
            };
        }
    })

    /**
     *  <r-natura-property-selection>
     */
    .component('rNaturaPropertySelection', {
        templateUrl: 'harvestpermit/decision/natura/property-selection.html',
        bindings: {
            mapState: '<'
        },
        controller: function ($filter, GIS, leafletData, PropertyIdentifierService, NotificationService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.properties = {};
                $ctrl.propertyCode = '';
            };

            $ctrl.remove = function (id) {
                $ctrl.properties[id].remove();
                delete $ctrl.properties[id];
            };

            $ctrl.view = function (id) {
                leafletData.getMap('naturaAreaMap').then(function (map) {
                    map.fitBounds($ctrl.properties[id].getBounds());
                });
            };

            var handleResponse = function (code) {
                return function (rsp) {
                    if (_.isEmpty(rsp.data.features)) {
                        NotificationService.showMessage('decision.natura.propertyNotFound', 'error');
                        return;
                    }

                    leafletData.getMap('naturaAreaMap').then(function (map) {
                        var popup = L.popup({closeButton: false, closeOnClick: true});

                        $ctrl.properties[code] = L.geoJson(rsp.data)
                            .on('mouseover', function (e) {
                                var name = $filter('formatPropertyIdentifier')(code);
                                L.DomEvent.stopPropagation(e);
                                popup.setContent(name).setLatLng(e.latlng).openOn(map);
                            })
                            .addTo(map);
                    });
                };
            };

            $ctrl.searchProperty = function (propertyCode) {
                var code = PropertyIdentifierService.parseFromString(propertyCode);
                if (code && _.isNil($ctrl.properties[code])) {
                    GIS.getPropertyPolygonByCode(code).then(handleResponse(code), NotificationService.showDefaultFailure);
                }
                $ctrl.propertyCode = '';
            };
        }
    })

    /**
     * <r-natura-area-list>
     */
    .component('rNaturaAreaList', {
        templateUrl: 'harvestpermit/decision/natura/area-list.html',
        bindings: {
            onRemove: '&',
            onSelect: '&',
            areaList: '<',
            selectedAreaId: '<',
            applicationId: '<',
            title: '@'
        },
        controller: function (NaturaAreaMarkerService, NotificationService) {
            var $ctrl = this;

            $ctrl.copyToClipboard = function () {
                NaturaAreaMarkerService.copyToClipboard($ctrl.applicationId);
                NotificationService.showMessage('decision.natura.copyToClipboardOk', 'success');
            };

        }
    })

    /**
     *  r-validate-property-code
     */
    .directive('rValidatePropertyCode', function (PropertyIdentifierService) {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, elem, attrs, ctrl) {
                scope.$watch(attrs.ngModel, function (newVal) {
                    ctrl.$setValidity('propertyCode', _.isEmpty(newVal) || !!PropertyIdentifierService.parseFromString(newVal));
                });
            }
        };
    });