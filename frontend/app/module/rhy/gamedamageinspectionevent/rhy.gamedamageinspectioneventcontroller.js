(function () {
    "use strict";

    angular.module('app.rhy.gamedamageinspectionevent', [])
        .config(function ($stateProvider) {
            $stateProvider
                .state('rhy.gamedamageinspectionevent', {
                    url: '/gamedamageinspectionevent',
                    templateUrl: 'rhy/gamedamageinspectionevent/list.html',
                    controller: 'GameDamageInspectionEventListController',
                    controllerAs: '$ctrl',
                    resolve: {
                        rhy: function (Rhys, rhyId) {
                            return Rhys.get({id: rhyId}).$promise;
                        },
                        availableYears: function(rhyId, RhyExistenceYears) {
                            return RhyExistenceYears.get({rhyId: rhyId}).$promise;
                        },
                        events: function (rhyId, GameDamageInspectionEvents, availableYears) {
                            return GameDamageInspectionEvents.list({rhyId: rhyId, year: _.last(availableYears)}).$promise;
                        }
                    }
                });
        })
        .controller('GameDamageInspectionEventListController',
            function ($scope, $uibModal, Helpers, NotificationService, TranslatedSpecies, Species,
                      FetchAndSaveBlob, GameDamageInspectionEvents, GameDamageType, ActiveRoleService,
                      rhy, events, availableYears) {
                var $ctrl = this;

                function populateGameDamageTypes() {
                    $ctrl.eventIdToGameDamageType = {};
                    $ctrl.events.forEach(function (event) {
                        $ctrl.eventIdToGameDamageType[event.id] = GameDamageType.getGameDamageType(event);
                    });
                }

                $ctrl.$onInit = function () {
                    $ctrl.rhy = rhy;
                    $ctrl.events = events;

                    $ctrl.availableYears = availableYears;
                    $ctrl.calendarYear = _.last($ctrl.availableYears);

                    $ctrl.isModerator = ActiveRoleService.isModerator();

                    populateGameDamageTypes();
                };

                $ctrl.addEvent = function () {
                    $uibModal.open({
                        templateUrl: 'rhy/gamedamageinspectionevent/form.html',
                        resolve: {
                            event: Helpers.wrapToFunction({}),
                            rhy: Helpers.wrapToFunction($ctrl.rhy),
                            rhyBounds: function (MapBounds) {
                                return MapBounds.getRhyBounds($ctrl.rhy.officialCode);
                            },
                            rhyGeoJSON: function (GIS) {
                                return GIS.getInvertedRhyGeoJSON($ctrl.rhy.officialCode, $ctrl.rhy.id, {
                                    name: $ctrl.rhy.nameFI
                                });
                            },
                        },
                        controller: 'GameDamageInspectionEventFormController',
                        controllerAs: '$ctrl',
                        size: 'lg'
                    }).result.then($ctrl.onSuccess, $ctrl.onFailure);
                };

                $ctrl.exportToExcel = function (gameDamageType) {
                    FetchAndSaveBlob.post('/api/v1/riistanhoitoyhdistys/' + $ctrl.rhy.id +
                        '/gamedamageinspectionevents/' + $ctrl.calendarYear + '/excel/' + gameDamageType);
                };

                $ctrl.exportToExcelSummary = function (gameDamageType) {
                    FetchAndSaveBlob.post('/api/v1/riistanhoitoyhdistys/gamedamageinspectionevents/' +
                        $ctrl.calendarYear + '/excel/' + gameDamageType + '/summary');
                };

                $ctrl.translateSpecies = function (speciesCode) {
                    var species = Species.getSpeciesMapping()[speciesCode];
                    return TranslatedSpecies.translateSpecies(species).name;
                };

                var refreshList = function () {
                    GameDamageInspectionEvents.list({rhyId: $ctrl.rhy.id, year: $ctrl.calendarYear}).$promise
                        .then(function (events) {
                            $ctrl.events = events;
                            populateGameDamageTypes();
                        });
                };

                $ctrl.onSuccess = function () {
                    GameDamageInspectionEvents.list({rhyId: $ctrl.rhy.id}).$promise.then(function (events) {
                        $ctrl.events = events;
                    });
                };

                $ctrl.onSelectedYearChanged = function () {
                    refreshList();
                };

                $ctrl.onSuccess = function () {
                    refreshList();
                    NotificationService.showDefaultSuccess();
                };

                $ctrl.onFailure = function (reason) {
                    if (reason === 'error') {
                        NotificationService.showDefaultFailure();
                    }
                };

                $ctrl.edit = function (event) {
                    $uibModal.open({
                        templateUrl: 'rhy/gamedamageinspectionevent/form.html',
                        resolve: {
                            event: Helpers.wrapToFunction(angular.copy(event)),
                            rhy: Helpers.wrapToFunction($ctrl.rhy),
                            rhyBounds: function (MapBounds) {
                                return MapBounds.getRhyBounds($ctrl.rhy.officialCode);
                            },
                            rhyGeoJSON: function (GIS) {
                                return GIS.getInvertedRhyGeoJSON($ctrl.rhy.officialCode, $ctrl.rhy.id, {
                                    name: $ctrl.rhy.nameFI
                                });
                            },
                        },
                        controller: 'GameDamageInspectionEventFormController',
                        controllerAs: '$ctrl',
                        size: 'lg'
                    }).result.then($ctrl.onSuccess, $ctrl.onFailure);
                };

                $ctrl.remove = function (event) {
                    $uibModal.open({
                        templateUrl: 'rhy/gamedamageinspectionevent/remove.html',
                        resolve: {
                            event: Helpers.wrapToFunction(event)
                        },
                        controller: 'GameDamageInspectionEventRemoveController',
                        controllerAs: '$ctrl'
                    }).result.then($ctrl.onSuccess, $ctrl.onFailure);
                };

                $ctrl.getInspectorName = function (inspector) {
                    return inspector.firstName + " " + inspector.lastName;
                };
        })
        .controller('GameDamageInspectionEventFormController',
            function ($scope, $uibModalInstance, Helpers,
                      MapBounds, MapDefaults, GIS, MapUtil,
                      Species, TranslatedSpecies, ActiveRoleService,
                      GameDamageInspectionEvents, GameDamageTypes, GameDamageType,
                      event, rhy, rhyBounds, rhyGeoJSON, GameDamageInspectionExpenseTypes) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.activeTabIndex = 0;
                    $ctrl.event = event;
                    $ctrl.gameDamageInspectionExpenseTypes = GameDamageInspectionExpenseTypes;
                    $ctrl.rhy = rhy;

                    $ctrl.damageTypes = GameDamageTypes;
                    $ctrl.largeCarnivore = Species.getCarnivoreSpecies();
                    $ctrl.mooselike = Species.getMooselikeSpecies();
                    if ($ctrl.event.id) {
                        $ctrl.gameDamageType = GameDamageType.getGameDamageType($ctrl.event);
                        $ctrl.event.gameDamageInspectionKmExpenses =  $ctrl.event.gameDamageInspectionKmExpenses || [{expenseType: null}];

                        $ctrl.onDateChange();

                        $ctrl.onGameDamageTypeChanged();

                        $ctrl.onDurationChanged();
                        $ctrl.onKilometerExpensesChanged();
                    } else {
                        $ctrl.speciesList = [];
                        $ctrl.event = {
                            gameDamageInspectionKmExpenses: [{expenseType: null}],
                            expensesIncluded: true
                        };

                        $ctrl.duration = 0;
                        $ctrl.durationText = '0:00';
                        $ctrl.rhyExpenses = 0;
                        $ctrl.kilometerExpenses = 0;
                        $ctrl.travelExpenses = 0;
                        $ctrl.totalExpenses = 0;
                    }

                    $ctrl.leaflet = {};

                    $ctrl.leaflet.mapDefaults = MapDefaults.create({
                        dragging: true,
                        minZoom: 5,
                        scrollWheelZoom: false
                    });
                    $ctrl.leaflet.bounds = rhyBounds;

                    $ctrl.leaflet.mapFeatures = {
                        data: rhyGeoJSON,
                        style: {
                            fillColor: "#A080B0",
                            weight: 2,
                            opacity: 0,
                            color: 'none',
                            fillOpacity: 0.45
                        }
                    };

                    $ctrl.inRhyArea = true;
                };

                $ctrl.isDateTooFarInThePast = function () {
                    if (ActiveRoleService.isModerator()) {
                        return false;
                    }

                    var e = $ctrl.event;

                    if (e.date) {
                        var eventYear = Helpers.toMoment(e.date).year();
                        var lastModificationDate = moment({month: 3, day: 5});
                        if (eventYear < moment().subtract(lastModificationDate.dayOfYear(), 'days').year()) {
                            return true;
                        }
                    }

                    return false;
                };

                $ctrl.isDateInTheFuture = function () {
                    var e = $ctrl.event;

                    if (e.date) {
                        var eventDate = Helpers.toMoment(e.date);
                        if (eventDate.isAfter(moment(new Date()))) {
                            return true;
                        }
                    }

                    return false;
                };

                $ctrl.isBeginTimeBeforeEndTime = function () {
                    if ($ctrl.event.beginTime && $ctrl.event.endTime) {
                        var begin =  Helpers.toMoment($ctrl.event.beginTime, 'HH:mm');
                        var end = Helpers.toMoment($ctrl.event.endTime, 'HH:mm');

                        if ( moment.duration(end.diff(begin)) < 0) {
                            return false;
                        }
                    }

                    return true;
                };

                $ctrl.isFormValid = function () {
                    return $ctrl.isBeginTimeBeforeEndTime() &&
                        !$ctrl.isDateTooFarInThePast() &&
                        !$ctrl.isDateInTheFuture() &&
                        !!$ctrl.event.geoLocation;
                };

                $ctrl.onGameDamageTypeChanged = function () {
                    if ($ctrl.gameDamageType === 'MOOSELIKE') {
                        $ctrl.speciesList = $ctrl.mooselike;
                    }

                    if ($ctrl.gameDamageType === 'LARGE_CARNIVORE') {
                        $ctrl.speciesList = $ctrl.largeCarnivore;
                    }
                };

                $ctrl.translateSpecies = function (species) {
                    return TranslatedSpecies.translateSpecies(species).name;
                };

                $ctrl.selectTab = function () {
                    MapUtil.forceRefreshMapArea("map", $ctrl.leaflet.bounds);
                };

                $ctrl.previous = function () {
                    $ctrl.activeTabIndex = 0;
                };

                $ctrl.next = function () {
                    $ctrl.activeTabIndex = 1;
                };

                $ctrl.onDurationChanged = function () {
                    if ($ctrl.isBeginTimeBeforeEndTime() && $ctrl.event.beginTime && $ctrl.event.endTime) {
                        var begin = Helpers.toMoment($ctrl.event.beginTime, 'HH:mm');
                        var end = Helpers.toMoment($ctrl.event.endTime, 'HH:mm');

                        $ctrl.duration = Math.floor(moment.duration(end.diff(begin)).asHours());
                        $ctrl.durationText = $ctrl.duration + ':00';
                    } else {
                        $ctrl.duration = 0;
                        $ctrl.durationText = '0:00';
                    }

                    $ctrl.onRhyExpensesChanged();
                };

                $ctrl.onRhyExpensesChanged = function () {
                    if ($ctrl.event.hourlyExpensesUnit) {
                        $ctrl.rhyExpenses = $ctrl.event.hourlyExpensesUnit * $ctrl.duration;
                    } else {
                        $ctrl.rhyExpenses = 0;
                    }

                    $ctrl.onTotalExpensesChanged();
                };

                $ctrl.onKilometerExpensesChanged = function () {
                    $ctrl.kilometerExpenses = $ctrl.event.gameDamageInspectionKmExpenses
                        .map(function (kmExpense) {
                            if (kmExpense.kilometers && kmExpense.expenseUnit) {
                                return kmExpense.kilometers * kmExpense.expenseUnit;
                            } else {
                                return 0;
                            }
                        })
                        .reduce(function (acc, expense) {
                            return acc + expense;
                        });

                    $ctrl.onTravelExpensesChanged();
                };

                $ctrl.addKmExpenses = function () {
                    $ctrl.event.gameDamageInspectionKmExpenses.push({expenseType: null});
                };

                $ctrl.removeKmExpenses = function (index) {
                    $ctrl.event.gameDamageInspectionKmExpenses.splice(index, 1);
                    $ctrl.onKilometerExpensesChanged();
                };

                $ctrl.onTravelExpensesChanged = function () {
                    $ctrl.travelExpenses =
                        ($ctrl.event.dailyAllowance ? $ctrl.event.dailyAllowance : 0) +
                            $ctrl.kilometerExpenses;
                    $ctrl.onTotalExpensesChanged();
                };

                $ctrl.onTotalExpensesChanged = function () {
                    $ctrl.totalExpenses = $ctrl.travelExpenses + $ctrl.rhyExpenses;
                };

                $ctrl.cancel = function () {
                    $uibModalInstance.dismiss('cancel');
                };

                $ctrl.save = function () {
                    if (!$ctrl.event.expensesIncluded) {
                        $ctrl.event.dailyAllowance = null;
                        $ctrl.event.hourlyExpensesUnit = null;
                        $ctrl.event.gameDamageInspectionKmExpenses = null;
                    }

                    if ($ctrl.event.inspector) {
                        $ctrl.event.inspectorName = null;
                    }

                    var saveOrUpdate = !$ctrl.event.id ? GameDamageInspectionEvents.save : GameDamageInspectionEvents.update;
                    saveOrUpdate({rhyId: $ctrl.rhy.id, id: $ctrl.event.id}, $ctrl.event).$promise
                        .then(function() {
                            $uibModalInstance.close();
                        }, function() {
                            $uibModalInstance.dismiss('error');
                        });
                };

                $scope.$watch("$ctrl.event.geoLocation", function (geoLocation) {
                    if (!geoLocation) {
                        return;
                    }
                    GIS.getRhyForGeoLocation(geoLocation).then(function (rhyData) {
                        $ctrl.inRhyArea = rhyData.data.id === $ctrl.rhy.id;
                    });
                });

                $ctrl.onDateChange = function () {
                    if (!$ctrl.event.date) {
                        $ctrl.event.inspector = null;
                        return;
                    }

                    GameDamageInspectionEvents.listAvailableInspectors({rhyId: $ctrl.rhy.id, date: $ctrl.event.date}).$promise
                        .then(function (inspectors) {
                            $ctrl.availableInspectors = inspectors;

                            if ($ctrl.event.inspector) {
                                $ctrl.event.inspector = _.find($ctrl.availableInspectors, function (inspector) {
                                    return $ctrl.event.inspector.id === inspector.id;
                                });
                            }
                        });
                };

                $ctrl.getInspectorName = function (person) {
                    return person.firstName + " " + person.lastName;
                };
        })
        .controller('GameDamageInspectionEventRemoveController',
            function (event, $uibModalInstance, GameDamageInspectionEvents) {
                var $ctrl = this;

                $ctrl.remove = function () {
                    GameDamageInspectionEvents.delete({id: event.id}).$promise
                        .then(function() {
                            $uibModalInstance.close();
                        }, function() {
                            $uibModalInstance.dismiss('error');
                        });
                };

                $ctrl.cancel = function () {
                    $uibModalInstance.dismiss('cancel');
                };
        })
        .service('GameDamageType', function (Species) {
            this.getGameDamageType = function (event) {
                var carnivoreIdx = _.findIndex(Species.getCarnivoreSpecies(), function (carnivore) {
                    return carnivore.code === event.gameSpeciesCode;
                });
                var mooselikeIdx = _.findIndex(Species.getMooselikeSpecies(), function (mooselike) {
                    return mooselike.code === event.gameSpeciesCode;
                });
                if (carnivoreIdx !== -1) {
                    return 'LARGE_CARNIVORE';
                } else if (mooselikeIdx !== -1) {
                    return 'MOOSELIKE';
                }

                return null;
            };
        })
        .factory('GameDamageInspectionEvents', function ($resource) {
            return $resource('api/v1/riistanhoitoyhdistys/:rhyId/gamedamageinspectionevents/:year', {rhyId: '@rhyId', year: '@calendarYear'}, {
                'list': {method: 'GET', isArray: true},
                'update': {
                    method: 'PUT',
                    params: {id: '@id'},
                    url: 'api/v1/riistanhoitoyhdistys/gamedamageinspectionevents/:id'
                },
                'delete': {
                    method: 'DELETE',
                    params: {id: '@id'},
                    url: 'api/v1/riistanhoitoyhdistys/gamedamageinspectionevents/:id'
                },
                'listAvailableInspectors': {
                    method: 'GET',
                    params: {rhyId: '@rhyId', date: '@date'},
                    url: 'api/v1/riistanhoitoyhdistys/:rhyId/gamedamageinspectionevents/inspectors/:date',
                    isArray: true
                }
            });
        })
        .constant('GameDamageTypes', [
            'MOOSELIKE',
            'LARGE_CARNIVORE'
        ])
        .constant('GameDamageInspectionExpenseTypes', [
            'AUTO',
            'MOOTTORIKELKKA',
            'MONKIJA',
            'MUU'
        ]);
})();