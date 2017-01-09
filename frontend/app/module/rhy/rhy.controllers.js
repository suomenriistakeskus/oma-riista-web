'use strict';

angular.module('app.rhy.controllers', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('rhy', {
                abstract: true,
                templateUrl: 'rhy/layout.html',
                url: '/rhy/{id:[0-9]{1,8}}',
                resolve: {
                    orgId: function ($stateParams) {
                        return _.parseInt($stateParams.id);
                    }
                }
            })
            .state('rhy.show', {
                url: '/show',
                templateUrl: 'rhy/show.html',
                controller: 'RhyShowController',
                resolve: {
                    rhy: function (Rhys, orgId) {
                        return Rhys.get({id: orgId}).$promise;
                    }
                }
            })
            .state('rhy.occupations', {
                url: '/occupations',
                templateUrl: 'occupation/list.html',
                controller: 'OccupationListController',
                controllerAs: '$ctrl',
                resolve: {
                    allOccupations: function (Occupations, orgId) {
                        return Occupations.query({orgId: orgId}).$promise;
                    },
                    onlyBoard: function () {
                        return false;
                    },
                    occupationTypes: function (OccupationTypes, orgId) {
                        return OccupationTypes.query({orgId: orgId}).$promise;
                    }
                }
            })
            .state('rhy.announcements', {
                url: '/announcements',
                controllerAs: '$ctrl',
                templateUrl: 'rhy/announcements.html',
                resolve: {
                    rhy: function (Rhys, orgId) {
                        return Rhys.get({id: orgId}).$promise;
                    }
                },
                controller: function ($state, $stateParams, rhy) {
                    this.rhy = rhy;
                }
            })
            .state('rhy.nomination', {
                url: '/jht',
                templateUrl: 'occupation/nomination/list.html',
                controller: 'OccupationNominationListController',
                controllerAs: '$ctrl',
                resolve: {
                    activeRhy: function (Rhys, orgId) {
                        return Rhys.get({id: orgId}).$promise;
                    },
                    searchParams: function (OccupationNominationService, activeRhy) {
                        return OccupationNominationService.createSearchParameters(activeRhy);
                    },
                    resultList: function (OccupationNominationService, searchParams) {
                        return searchParams ? OccupationNominationService.search(searchParams) : [];
                    }
                }
            })
            .state('rhy.events', {
                url: '/events',
                templateUrl: 'event/event_list.html',
                controller: 'EventListController'
            })
            .state('rhy.locations', {
                url: '/locations',
                templateUrl: 'event/venue_list.html',
                controller: 'VenueListController'
            })
            .state('rhy.harvestreports', {
                url: '/harvestreports',
                templateUrl: 'rhy/harvestreports.html',
                controller: 'RhyHarvestReportListController',
                resolve: {
                    fieldsAndSeasons: function (HarvestReportFieldsAndSeasons) {
                        return HarvestReportFieldsAndSeasons.validsForAllSeasonsAndPermits();
                    },
                    rhy: function (Rhys, orgId) {
                        return Rhys.get({id: orgId}).$promise;
                    },
                    rhyBounds: function (rhy, GIS) {
                        return GIS.getRhyBounds(rhy.officialCode);
                    },
                    rhyGeometry: function (rhy, GIS) {
                        return GIS.getRhyGeom(rhy.officialCode).then(function (response) {
                            var rhyExteriorGeometry = {
                                type: "FeatureCollection",
                                features: [
                                    {
                                        type: "Feature",
                                        id: rhy.id,
                                        properties: {name: rhy.nameFI},
                                        geometry: {
                                            type: "MultiPolygon",
                                            coordinates: [[[[-180, -180], [180, 0], [180, 180], [0, 180], [-180, -180]], response.data.coordinates[0][0]]]
                                        }
                                    }
                                ]
                            };

                            return {
                                data: rhyExteriorGeometry,
                                style: {fillColor: "#A080B0", weight: 2, opacity: 0, color: 'none', fillOpacity: 0.45}
                            };
                        });
                    },
                    gameDiaryParameters: function (GameDiaryParameters) {
                        return GameDiaryParameters.query().$promise;
                    },
                    harvestReportLocalityResolver: function (HarvestReportLocalityResolver) {
                        return HarvestReportLocalityResolver.get();
                    }
                }
            })
            .state('rhy.permits', {
                url: '/permits',
                templateUrl: 'rhy/permits.html',
                controller: 'RhyPermitListController',
                resolve: {
                    species: function (HarvestPermits) {
                        return HarvestPermits.species().$promise;
                    }
                }
            })
            .state('rhy.srva', {
                abstract: true,
                url: '/srva',
                template: '<div ui-view autoscroll="false"></div>',
                resolve: {
                    parameters: function (GameDiarySrvaParameters) {
                        return GameDiarySrvaParameters.query().$promise;
                    }
                }
            })
            .state('rhy.srva.list', {
                url: '/list',
                templateUrl: 'srva/srva-events.html',
                controller: 'SrvaEventListController',
                controllerAs: '$ctrl'
            })
            .state('rhy.srva.map', {
                url: '/map',
                wideLayout: true,
                templateUrl: 'srva/srva-map.html',
                controller: 'SrvaEventMapController',
                controllerAs: '$ctrl',
                resolve: {
                    areas: function (Areas) {
                        return Areas.query().$promise;
                    }
                }
            })
            .state('rhy.moosepermit', {
                url: '/moosepermit?huntingYear&species',
                wideLayout: true,
                params: {
                    huntingYear: null,
                    species: null
                },
                resolve: {
                    initialState: _.constant('rhy.moosepermit'),
                    huntingYears: function (Rhys, orgId) {
                        return Rhys.moosePermitHuntingYears({id: orgId}).$promise;
                    },
                    selectedYearAndSpecies: function (MoosePermitListSelectedHuntingYearService, $stateParams, huntingYears) {
                        return MoosePermitListSelectedHuntingYearService.resolve($stateParams, huntingYears);
                    }
                },
                views: {
                    '@rhy': {
                        templateUrl: 'harvestpermit/moosepermit/layout.html'
                    },
                    'left@rhy.moosepermit': {
                        templateUrl: 'harvestpermit/moosepermit/list.html',
                        controller: 'MoosePermitListController',
                        controllerAs: '$ctrl',
                        resolve: {
                            permits: function (Rhys, selectedYearAndSpecies, orgId) {
                                var year = selectedYearAndSpecies.huntingYear;
                                var species = selectedYearAndSpecies.species;
                                if (!species) {
                                    return _.constant([]);
                                }
                                return Rhys.listMoosePermits({id: orgId}, {year: year, species: species}).$promise;
                            }
                        }
                    }
                }
            })
            .state('rhy.moosepermit.show', {
                url: '/{permitId:[0-9]{1,8}}/show',
                wideLayout: true,
                templateUrl: 'harvestpermit/moosepermit/show.html',
                controller: 'MoosePermitShowController',
                resolve: {
                    permitId: function ($stateParams, MoosePermitSelection) {
                        return MoosePermitSelection.updateSelectedPermitId($stateParams);
                    },
                    permit: function (HarvestPermits, permitId, selectedYearAndSpecies) {
                        return HarvestPermits.moosePermit({permitId: permitId, species: selectedYearAndSpecies.species}).$promise;
                    },
                    todos: function (HarvestPermits, permitId, selectedYearAndSpecies) {
                        return HarvestPermits.listTodos({permitId: permitId, speciesCode: selectedYearAndSpecies.species}).$promise;
                    },
                    edit: _.constant(false)
                }
            })
            .state('rhy.moosepermit.edit', {
                url: '/{permitId:[0-9]{1,8}}/edit',
                wideLayout: true,
                templateUrl: 'harvestpermit/moosepermit/show.html',
                controller: 'MoosePermitShowController',
                resolve: {
                    permitId: function ($stateParams, MoosePermitSelection) {
                        return MoosePermitSelection.updateSelectedPermitId($stateParams);
                    },
                    permit: function (HarvestPermits, permitId, selectedYearAndSpecies) {
                        return HarvestPermits.moosePermit({permitId: permitId, species: selectedYearAndSpecies.species}).$promise;
                    },
                    todos: function (HarvestPermits, permitId, selectedYearAndSpecies) {
                        return HarvestPermits.listTodos({permitId: permitId, speciesCode: selectedYearAndSpecies.species}).$promise;
                    },
                    edit: _.constant(true)
                }
            })
            .state('rhy.moosepermit.lukereports', {
                url: '/{permitId:[0-9]{1,8}}/luke-reports',
                wideLayout: true,
                templateUrl: 'harvestpermit/moosepermit/luke-reports.html',
                controller: 'MoosePermitLukeReportsController',
                resolve: {
                    permitId: function ($stateParams, MoosePermitSelection) {
                        return MoosePermitSelection.updateSelectedPermitId($stateParams);
                    },
                    clubId: _.constant(null),
                    lukeReportParams: function (HarvestPermits, permitId) {
                        return HarvestPermits.lukeReportParams({permitId: permitId}).$promise;
                    }
                }
            })
            .state('rhy.moosepermit.map', {
                url: '/{permitId:[0-9]{1,8}}/map',
                templateUrl: 'harvestpermit/moosepermit/map/permit-map.html',
                controller: 'MoosePermitMapController',
                controllerAs: 'ctrl',
                wideLayout: true,
                resolve: {
                    rhy: function (Rhys, orgId) {
                        return Rhys.get({id: orgId}).$promise;
                    },
                    permitId: function ($stateParams, MoosePermitSelection) {
                        return MoosePermitSelection.updateSelectedPermitId($stateParams);
                    },
                    harvests: function (MoosePermitHarvest, permitId, selectedYearAndSpecies) {
                        return MoosePermitHarvest.query({
                            permitId: permitId,
                            huntingYear: selectedYearAndSpecies.huntingYear,
                            gameSpeciesCode: selectedYearAndSpecies.species
                        }).$promise;
                    },
                    featureCollection: function (HarvestPermits, permitId, selectedYearAndSpecies) {
                        return HarvestPermits.permitMapFeatures({
                            permitId: permitId,
                            huntingYear: selectedYearAndSpecies.huntingYear,
                            gameSpeciesCode: selectedYearAndSpecies.species
                        }).$promise;
                    },
                    mapBounds: function (GIS, featureCollection, rhy) {
                        var bounds = GIS.getBoundsFromGeoJsonFeatureCollection(featureCollection);
                        return bounds || GIS.getRhyBounds(rhy.officialCode);
                    }
                }
            });
    })

    .controller('RhyShowController',
        function ($scope, $state, $uibModal, rhy, NotificationService, MapDefaults, GIS) {
            $scope.data = rhy;
            $scope.mapDefaults = MapDefaults.create();
            $scope.center = {};

            GIS.getRhyBounds(rhy.officialCode).then(function (bounds) {
                $scope.bounds = bounds;
            });

            GIS.getRhyGeom(rhy.officialCode).then(function (response) {
                var geojson = {
                    type: "FeatureCollection",
                    features: [
                        {
                            type: "Feature",
                            id: rhy.id,
                            properties: {name: rhy.nameFI},
                            geometry: {
                                type: "MultiPolygon",
                                coordinates: response.data.coordinates
                            }
                        }
                    ]
                };
                $scope.geojson = {
                    data: geojson,
                    style: {fillColor: "green", weight: 2, opacity: 0, color: 'none', fillOpacity: 0.6}
                };
            });


            $scope.editRHY = function () {
                $uibModal.open({
                    templateUrl: 'rhy/edit_rhy.html',
                    controller: 'RHYEditController',
                    resolve: {
                        rhy: function (Rhys) {
                            return Rhys.get({id: rhy.id}).$promise;
                        }
                    }
                }).result.then(function () {
                    NotificationService.showDefaultSuccess();

                    $state.reload();
                });
            };
        })
    .controller('RHYEditController',
        function ($scope, $uibModalInstance, Rhys, rhy) {
            $scope.rhy = rhy;

            var originalAddress = rhy.hasOwnAddress ? rhy.address : null;
            if (!rhy.hasOwnAddress) {
                // address is from coordinator, address might not be valid, so set it null to have valid form
                rhy.address = null;
            }
            $scope.$watch('rhy.hasOwnAddress', function (newValue, oldValue) {
                if (newValue !== oldValue) {
                    rhy.address = newValue ? originalAddress : null;
                }
            });

            var originalEmail = rhy.hasOwnEmail ? rhy.email : null;
            $scope.$watch('rhy.hasOwnEmail', function (newValue, oldValue) {
                if (newValue !== oldValue) {
                    rhy.email = newValue ? originalEmail : null;
                }
            });

            var originalPhoneNumber = rhy.hasOwnPhoneNumber ? rhy.phoneNumber : null;
            $scope.$watch('rhy.hasOwnPhoneNumber', function (newValue, oldValue) {
                if (newValue !== oldValue) {
                    rhy.phoneNumber = newValue ? originalPhoneNumber : null;
                }
            });

            $scope.save = function (form) {
                var promise = Rhys.update(rhy).$promise;

                promise.then(function () {
                    $uibModalInstance.close();
                }, function () {
                    $scope.error = "ERROR";
                });
            };
        })
    .controller('ValidRhyEmailController', function ($scope) {
        var validDomain = '@rhy.riista.fi';
        var getUser = function (email) {
            return email.substring(0, email.indexOf('@'));
        };
        var getDomain = function (email) {
            return email.substring(email.indexOf('@'));
        };
        var startsWith = function (str, toStartWith) {
            return str.indexOf(toStartWith) === 0;
        };
        var isValidDomain = function (email) {
            return startsWith(validDomain, getDomain(email));
        };
        $scope.getRhyEmail = function (email) {
            if (isValidDomain(email)) {
                return [getUser(email) + validDomain];
            }
            if (email.indexOf('@') !== -1) {
                return [''];
            }
            return [email + validDomain];
        };
    })
    .controller('RhyHarvestReportListController',
        function ($scope, $translate,
                  HarvestReports, HarvestReportService, HarvestReportSearch,
                  GIS, MapDefaults, Helpers, HuntingYearService, Markers,
                  rhyBounds, rhyGeometry,
                  fieldsAndSeasons, orgId, gameDiaryParameters, harvestReportLocalityResolver) {
            $scope.fieldsAndSeasons = fieldsAndSeasons;
            $scope.getHuntingArea = harvestReportLocalityResolver.getHuntingArea;
            $scope.getAreaName = harvestReportLocalityResolver.getAreaName;
            $scope.getRhyName = harvestReportLocalityResolver.getRhyName;

            $scope.states = {'PROPOSED': false, 'SENT_FOR_APPROVAL': false, 'REJECTED': false, 'APPROVED': true};

            $scope.dates = {
                beginDate: HuntingYearService.getBeginDateStr(),
                endDate: HuntingYearService.getEndDateStr()
            };

            $scope.mapDefaults = MapDefaults.create({
                dragging: true,
                scrollWheelZoom: false
            });
            $scope.mapEvents = MapDefaults.getMapBroadcastEvents();
            $scope.mapCenter = {};
            $scope.bounds = $scope.rhyBounds = rhyBounds;
            $scope.rhyGeometry = rhyGeometry;
            $scope.harvestReports = {};
            $scope.markers = {};

            var getSelectedStates = function () {
                return _.filter(_.keys($scope.states), function (key) {
                    return $scope.states[key];
                });
            };
            var canSearch = function () {
                return getSelectedStates().length > 0;
            };
            $scope.canSearch = canSearch;

            $scope.search = function () {
                if (!canSearch()) {
                    return;
                }
                var getNullableId = function (v) {
                    return v ? v.id : null;
                };
                var s = $scope.selectedFieldOrSeason || {};
                var params = {
                    beginDate: Helpers.dateToString($scope.dates.beginDate),
                    endDate: Helpers.dateToString($scope.dates.endDate),
                    fieldsId: getNullableId(s.fields),
                    rhyId: orgId,
                    states: getSelectedStates(),
                    permitNumber: $scope.permitNumberSearch
                };

                // Fill-in form submit data for Excel export file generation
                $scope.postData = angular.toJson(params);

                HarvestReportSearch.findAllForRhy(params).then(function (response) {
                    $scope.harvestReports = response.data;
                });
            };

            var markerDefaults = {
                draggable: false,
                icon: {
                    type: 'awesomeMarker',
                    prefix: 'fa', // font-awesome
                    icon: 'crosshairs'
                },
                compileMessage: true,
                groupOption: {
                    // Options to pass for leaflet.markercluster plugin

                    //disableClusteringAtZoom: 13,
                    showCoverageOnHover: true
                },
                group: 'HarvestReports',
                popupOptions: {
                    // Options to pass for Leaflet popup message (L.popup)

                    //keepInView: true,
                    maxWidth: 400
                }
            };

            $scope.show = function (harvestReport) {
                HarvestReportService.edit(angular.copy(harvestReport));
            };

            var findByFieldValue = function (objects, fieldName, value) {
                return _.find(objects, function (obj) {
                    return obj[fieldName] === value;
                });
            };

            $scope.showHarvestReportById = function (id) {
                $scope.show(findByFieldValue($scope.harvestReports, 'id', id));
            };

            var popupMessageFieldsFunction = function (harvestReports) {
                return function (harvestReportId) {
                    var harvestReport, jsDate, localizedGeolocation, localizedGameName;

                    if (harvestReports && harvestReportId) {
                        harvestReport = findByFieldValue(harvestReports, 'id', harvestReportId);

                        if (harvestReport) {
                            if (harvestReport.pointOfTime) {
                                jsDate = moment(harvestReport.pointOfTime).toDate();
                            }

                            if (harvestReport.geoLocation && harvestReport.geoLocation.longitude) {
                                localizedGeolocation = $translate.instant('global.geoLocation.coordinatesText', harvestReport.geoLocation);
                            }

                            localizedGameName = gameDiaryParameters.$getGameName(harvestReport.gameSpeciesCode);
                        }
                    }

                    return [
                        {name: 'species', value: localizedGameName},
                        {name: 'date', value: Helpers.dateToString(jsDate, 'D.M.YYYY')},
                        {name: 'time', value: Helpers.dateToString(jsDate, 'HH:mm')},
                        {name: 'coordinates', value: localizedGeolocation}
                    ];
                };
            };

            $scope.$watch('harvestReports', function (newReports, oldReports) {
                var getPopupMessageFields = popupMessageFieldsFunction(newReports);

                var getPopupMessageButtons = function (harvestReportId) {
                    return [
                        {name: 'doOpen', handlerExpr: 'showHarvestReportById(' + harvestReportId + ')'}
                    ];
                };

                var createMarkerData = function (harvestReport) {
                    if (harvestReport.harvestsAsList) {
                        return _.map(harvestReport.harvests, function (harvest) {
                            return {
                                id: harvestReport.id,
                                etrsCoordinates: harvest.geoLocation,
                                icon: {
                                    markerColor: Markers.getColorForHarvestReportState(harvestReport.state)
                                },
                                popupMessageFields: function () {
                                    var jsDate = moment(harvest.pointOfTime).toDate();
                                    return [
                                        {
                                            name: 'species',
                                            value: gameDiaryParameters.$getGameName(harvest.gameSpeciesCode)
                                        },
                                        {name: 'date', value: Helpers.dateToString(jsDate, 'D.M.YYYY')},
                                        {name: 'time', value: Helpers.dateToString(jsDate, 'HH:mm')},
                                        {
                                            name: 'coordinates',
                                            value: $translate.instant('global.geoLocation.coordinatesText', harvest.geoLocation)
                                        }
                                    ];
                                },
                                popupMessageButtons: getPopupMessageButtons,
                                getMessageScope: function () {
                                    return $scope;
                                }
                            };
                        });
                    }
                    return [{
                        id: harvestReport.id,
                        etrsCoordinates: harvestReport.geoLocation,
                        icon: {
                            markerColor: Markers.getColorForHarvestReportState(harvestReport.state)
                        },
                        popupMessageFields: getPopupMessageFields,
                        popupMessageButtons: getPopupMessageButtons,
                        getMessageScope: function () {
                            return $scope;
                        }
                    }];
                };

                var markers = Markers.transformToLeafletMarkerData(newReports, markerDefaults, createMarkerData);

                var latLngFunc = function (marker) {
                    return {
                        lat: marker.lat,
                        lng: marker.lng
                    };
                };
                $scope.bounds = GIS.getBounds(markers, latLngFunc, $scope.rhyBounds);

                $scope.markers = markers;
            });
        })

    .controller('RhyPermitListController',
        function ($scope, $translate, HarvestPermits, orgId, species) {
            $scope.allSpecies = species;

            $scope.speciesSortProperty = 'name.' + $translate.use();

            $scope.search = function () {
                var params = {};
                params.permitNumber = $scope.permitNumber;
                params.year = $scope.year;
                params.rhyId = orgId;
                if ($scope.species) {
                    params.speciesCode = $scope.species.code;
                }
                HarvestPermits.rhySearch(params).$promise.then(function (data) {
                    $scope.permits = data;
                });
            };

            $scope.canSearch = function () {
                return $scope.species || $scope.permitNumber || $scope.year;
            };
        }
    )
;
