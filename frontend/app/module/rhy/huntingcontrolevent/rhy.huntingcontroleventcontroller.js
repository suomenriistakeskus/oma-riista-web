"use strict";

angular.module('app.rhy.huntingcontrolevent', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('rhy.huntingcontrolevent', {
                url: '/huntingcontrolevent',
                templateUrl: 'rhy/huntingcontrolevent/list.html',
                controller: 'HuntingControlEventListController',
                controllerAs: '$ctrl',
                resolve: {
                    rhy: function (Rhys, rhyId) {
                        return Rhys.get({id: rhyId}).$promise;
                    },
                    availableYears: function(rhyId, RhyExistenceYears) {
                        return RhyExistenceYears.get({rhyId: rhyId}).$promise;
                    },
                    events: function (rhyId, HuntingControlEvents, availableYears) {
                        return HuntingControlEvents.list({rhyId: rhyId, year: _.last(availableYears)}).$promise;
                    }
                }
            });
    })
    .controller('HuntingControlEventListController',
        function ($scope, $uibModal, Helpers, NotificationService, TranslatedSpecies, Species,
                  ActiveRoleService, HuntingControlEvents, rhy, events, availableYears) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.rhy = rhy;
                $ctrl.events = events;

                $ctrl.availableYears = availableYears;
                $ctrl.calendarYear = _.last($ctrl.availableYears);

                $ctrl.isModerator = ActiveRoleService.isModerator();
            };

            $ctrl.addEvent = function () {
                $uibModal.open({
                    templateUrl: 'rhy/huntingcontrolevent/form.html',
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
                    controller: 'HuntingControlEventFormController',
                    controllerAs: '$ctrl',
                    size: 'lg'
                }).result.then($ctrl.onSuccess, $ctrl.onFailure);
            };

            var refreshList = function () {
                HuntingControlEvents.list({rhyId: $ctrl.rhy.id, year: $ctrl.calendarYear}).$promise
                    .then(function (events) {
                        $ctrl.events = events;
                    });
            };

            $ctrl.onSuccess = function () {
                refreshList();
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
                } else if (reason === 'attachmentsDeleted') {
                    refreshList();
                }
            };

            $ctrl.edit = function (event) {
                $uibModal.open({
                    templateUrl: 'rhy/huntingcontrolevent/form.html',
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
                    controller: 'HuntingControlEventFormController',
                    controllerAs: '$ctrl',
                    size: 'lg'
                }).result.then($ctrl.onSuccess, $ctrl.onFailure);
            };

            $ctrl.remove = function (event) {
                $uibModal.open({
                    templateUrl: 'rhy/huntingcontrolevent/remove.html',
                    resolve: {
                        event: Helpers.wrapToFunction(event)
                    },
                    controller: 'HuntingControlEventRemoveController',
                    controllerAs: '$ctrl'
                }).result.then($ctrl.onSuccess, $ctrl.onFailure);
            };
    })
    .controller('HuntingControlEventFormController',
        function ($scope, $uibModalInstance, $timeout, $translate,
                  Helpers, MapBounds, MapDefaults, GIS, MapUtil, dialogs,
                  Species, TranslatedSpecies, ActiveRoleService, FetchAndSaveBlob,
                  HuntingControlEvents, HuntingControlCooperationTypes, WolfTerritory,
                  event, rhy, rhyBounds, rhyGeoJSON) {
            var $ctrl = this;

            $ctrl.dropzone = null;

            $ctrl.$onInit = function () {
                $ctrl.event = event;
                $ctrl.rhy = rhy;

                $ctrl.cooperationTypes = HuntingControlCooperationTypes;
                $ctrl.wolfTerritory = WolfTerritory;
                if ($ctrl.event.id) {
                    $ctrl.onDurationChanged();
                } else {
                    $ctrl.duration = 0;
                    $ctrl.durationText = '0:00';
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

                $ctrl.dropzoneConfig = {
                    autoProcessQueue: false,
                    addRemoveLinks: true,
                    maxFiles: 10,
                    maxFilesize: 50, // MiB
                    uploadMultiple: true,
                    parallelUploads: 10,
                    url: 'api/v1/riistanhoitoyhdistys/' + $ctrl.rhy.id + '/huntingcontroleventswithattachments/',
                    paramName: function () {
                        return "file";
                    }
                };

                $ctrl.dropzoneEventHandlers = {
                    addedfile: function (file) {
                        $timeout(function () {
                            // trigger digest cycle
                            $ctrl.errors = {};
                        });
                    },
                    successmultiple: function (file) {
                        $ctrl.dropzone.removeFile(file);

                        $timeout(function () {
                            $ctrl.errors = {};
                            $uibModalInstance.close();
                        });
                    },
                    error: function (file, response, xhr) {
                        $ctrl.dropzone.removeFile(file);
                        $timeout(function () {
                            $ctrl.errors = {
                                incompatibleFileType: true
                            };
                        });
                    },
                    sendingmultiple: function (file, xhr, formData) {
                        $ctrl.event.attachments = null;
                        formData.append('dto', JSON.stringify($ctrl.event));
                    }
                };

                $ctrl.attachmentsDeleted = false;
            };

            $ctrl.onDurationChanged = function () {
                if ($ctrl.isBeginTimeBeforeEndTime() && $ctrl.event.beginTime && $ctrl.event.endTime) {
                    var begin = Helpers.toMoment($ctrl.event.beginTime, 'HH:mm');
                    var end = Helpers.toMoment($ctrl.event.endTime, 'HH:mm');

                    $ctrl.duration = moment.duration(end.diff(begin));
                    $ctrl.durationText = moment.utc($ctrl.duration.asMilliseconds()).format("HH:mm");
                } else {
                    $ctrl.duration = 0;
                    $ctrl.durationText = '0:00';
                }
            };

            $ctrl.isDateTooFarInThePast = function () {
                if (ActiveRoleService.isModerator()) {
                    return false;
                }

                var e = $ctrl.event;

                if (e.date) {
                    var eventYear = Helpers.toMoment(e.date).year();
                    if (eventYear < moment().subtract(15, 'days').year()) {
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

            $ctrl.cancel = function () {
                if (!$ctrl.attachmentsDeleted) {
                    $uibModalInstance.dismiss('cancel');
                } else {
                    $uibModalInstance.dismiss('attachmentsDeleted');
                }
            };

            $ctrl.save = function () {
                if ($ctrl.dropzone.getQueuedFiles().length > 0) {
                    $ctrl.dropzone.processQueue();
                } else {
                    var saveOrUpdate = !$ctrl.event.id ? HuntingControlEvents.save : HuntingControlEvents.update;
                    saveOrUpdate({rhyId: $ctrl.rhy.id, id: $ctrl.event.id}, $ctrl.event).$promise
                        .then(function() {
                            $uibModalInstance.close();
                        }, function() {
                            $uibModalInstance.dismiss('error');
                        });
                }
            };

            function reloadAttachments() {
                HuntingControlEvents.listAttachments({id: $ctrl.event.id}).$promise
                    .then(function (attachments) {
                        $ctrl.event.attachments = attachments;
                    });
            }

            $ctrl.removeAttachment = function(id) {
                var dialogTitle = $translate.instant('global.dialog.confirmation.title');
                var dialogMessage = $translate.instant('global.dialog.confirmation.text');

                dialogs.confirm(dialogTitle, dialogMessage).result.then(function () {
                    HuntingControlEvents.deleteAttachment({id: id}).$promise
                        .then(function () {
                            $ctrl.attachmentsDeleted = true;
                            reloadAttachments();
                        });
                });
            };

            $ctrl.downloadAttachment = function(id) {
                FetchAndSaveBlob.post('/api/v1/riistanhoitoyhdistys/huntingcontrolevents/attachment/' + id);
            };

            $scope.$watch("$ctrl.event.geoLocation", function (geoLocation) {
                if (!geoLocation) {
                    return;
                }

                GIS.getRhyForGeoLocation(geoLocation).then(function (rhyData) {
                    $ctrl.inRhyArea = rhyData.data.id === $ctrl.rhy.id;
                });
            }, true);
    })
    .controller('HuntingControlEventRemoveController',
        function (event, $uibModalInstance, HuntingControlEvents) {
            var $ctrl = this;

            $ctrl.remove = function () {
                HuntingControlEvents.delete({id: event.id}).$promise
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
    .factory('HuntingControlEvents', function ($resource) {
        return $resource('api/v1/riistanhoitoyhdistys/:rhyId/huntingcontrolevents/:year', {rhyId: '@rhyId', year: '@calendarYear'}, {
            'list': {method: 'GET', isArray: true},
            'update': {
                method: 'PUT',
                params: {id: '@id'},
                url: 'api/v1/riistanhoitoyhdistys/huntingcontrolevents/:id'
            },
            'delete': {
                method: 'DELETE',
                params: {id: '@id'},
                url: 'api/v1/riistanhoitoyhdistys/huntingcontrolevents/:id'
            },
            'deleteAttachment': {
                method: 'DELETE',
                params: {id: '@id'},
                url: 'api/v1/riistanhoitoyhdistys/huntingcontrolevents/attachment/:id'
            },
            'listAttachments': {
                method: 'GET',
                isArray: true,
                params: {id: '@id'},
                url: 'api/v1/riistanhoitoyhdistys/huntingcontrolevents/:id/attachments'
            }
        });
    })
    .constant('HuntingControlCooperationTypes', [
        'POLIISI',
        'RAJAVARTIOSTO',
        'MH',
        'OMA'
    ])
    .constant('WolfTerritory', [
        true,
        false
    ]);
