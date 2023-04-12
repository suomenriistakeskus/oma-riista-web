'use strict';

angular.module('app.rhy.huntingcontrolevent.form', [])
    .controller('HuntingControlEventFormController', function ($q, $scope, $uibModalInstance, $timeout, $translate,
                                                               Helpers, MapBounds, MapDefaults, GIS, MapUtil, dialogs,
                                                               Species, TranslatedSpecies, ActiveRoleService, FetchAndSaveBlob,
                                                               HuntingControlEvents, HuntingControlCooperationTypes,
                                                               WolfTerritory, HuntingControlEventTypes, AvailableRoleService,
                                                               RhyAnnualStatisticsService, event, rhy, rhyBounds, rhyGeoJSON) {
        var $ctrl = this;

        $ctrl.dropzone = null;

        $ctrl.$onInit = function () {
            $ctrl.event = event;
            $ctrl.rhy = rhy;

            $ctrl.cooperationTypes = HuntingControlCooperationTypes;
            $ctrl.cooperationType = null;
            updateCooperationTypes();

            resetAllAvailableGameWardens();
            $ctrl.availableGameWardens = [];
            $ctrl.gameWarden = null;
            updateAllAvailableGameWardens();

            updateInspectorCount();

            $ctrl.wolfTerritory = WolfTerritory;
            $ctrl.eventTypes = HuntingControlEventTypes;
            if ($ctrl.event.id) {
                $ctrl.onDurationChanged();
            } else {
                $ctrl.duration = 0;
                $ctrl.durationText = '0:00';
            }

            $ctrl.leaflet = {};

            $ctrl.leaflet.mapDefaults = MapDefaults.create({dragging: true, minZoom: 5, scrollWheelZoom: false});
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
                },
                clickable: '.add-with-click',
                dictDefaultMessage: '',
                dictFileTooBig: $translate.instant('global.dropzone.dropzoneError.fileTooBig', {o: '{{', c: '}}'}),
                dictMaxFilesExceeded: $translate.instant('global.dropzone.dropzoneError.maxFilesExceeded', {o: '{{', c: '}}'}),
                dictRemoveFile: $translate.instant('global.button.delete')
            };

            $ctrl.dropzoneEventHandlers = {
                addedfile: function (file) {
                    $timeout(function () {
                        // trigger digest cycle
                        $ctrl.errors = {};
                    });
                },
                removedfile: function (file) {
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
                            incompatibleFileType: true,
                            text: response
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

        function resetAllAvailableGameWardens() {
            $ctrl.allAvailableGameWardens = {
                gameWardens: [],
                activeNomination: true
            };
        }

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
            return e.date && RhyAnnualStatisticsService.hasDeadlinePassed(e.date);
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

        $ctrl.isReasonForChangeMandatory = function () {
            return ActiveRoleService.isGameWarden() ? false : true;
        };

        $ctrl.isFormValid = function () {
            return $ctrl.isBeginTimeBeforeEndTime() &&
                !$ctrl.isDateTooFarInThePast() &&
                !$ctrl.isDateInTheFuture() &&
                !!$ctrl.event.geoLocation &&
                !_.isEmpty($ctrl.event.inspectors) &&
                !_.isEmpty($ctrl.event.cooperationTypes) &&
                !!$ctrl.allAvailableGameWardens.activeNomination;
        };

        $ctrl.selectCooperation = function () {
            $ctrl.event.cooperationTypes.push($ctrl.cooperationType);
            $ctrl.cooperationType = null;
            updateCooperationTypes();
        };

        $ctrl.deselectCooperation = function (cooperationType) {
            _.pull($ctrl.event.cooperationTypes, cooperationType);
            updateCooperationTypes();
        };

        function updateCooperationTypes() {
            $ctrl.cooperationTypes = _.filter(
                HuntingControlCooperationTypes,
                function (o) {
                    return $ctrl.event.cooperationTypes.indexOf(o) < 0;
                });
        }

        $ctrl.selectGameWarden = function () {
            $ctrl.event.inspectors.push($ctrl.gameWarden);
            $ctrl.gameWarden = null;
            updateAvailableGameWardens();
        };

        $ctrl.deselectGameWarden = function (id) {
            _.remove($ctrl.event.inspectors, function (o) { return o.id === id; });
            updateAvailableGameWardens();
        };

        function updateAvailableGameWardens() {
            $ctrl.availableGameWardens = _.filter(
                $ctrl.allAvailableGameWardens.gameWardens,
                function (o) {
                    return _.findIndex($ctrl.event.inspectors, {'id': o.id}) < 0;
                });
            updateInspectorCount();
        }

        $ctrl.selectGameWardenText = function () {
            if (!!$ctrl.event.date) {
                return 'global.button.select';
            } else {
                return 'rhy.huntingControlEvent.selectDateFirst';
            }
        };

        $ctrl.dateAndNominationOk = function () {
            return !!$ctrl.event.date;
        };

        function updateSelectedGameWardens() {
            $ctrl.event.inspectors = _.filter(
                $ctrl.event.inspectors,
                function (o) {
                    return _.findIndex($ctrl.allAvailableGameWardens.gameWardens, {'id': o.id}) >= 0;
                });
        }

        $ctrl.selfNotSelectedAsInspector = function () {
            var personId = _.get(AvailableRoleService.findUserRole(), 'context.personId');
            return ActiveRoleService.isGameWarden() &&
                !_.isEmpty($ctrl.event.inspectors) &&
                _.isNil(_.find($ctrl.event.inspectors, {'id': personId}));
        };

        function updateInspectorCount() {
            $ctrl.event.inspectorCount = _.isArray($ctrl.event.inspectors) ? $ctrl.event.inspectors.length : 0;
        }

        $ctrl.dateChanged = function () {
            updateAllAvailableGameWardens();
        };

        function updateAllAvailableGameWardens() {
            if (!!$ctrl.event.date) {
                // Don't search too old or in the future
                if (!$ctrl.isDateTooFarInThePast() && !$ctrl.isDateInTheFuture()) {
                    HuntingControlEvents.listInspectors({rhyId: rhy.id, date: $ctrl.event.date}).$promise
                        .then(function (rsp) {
                            $ctrl.allAvailableGameWardens = rsp;
                            updateAvailableGameWardens();
                            updateSelectedGameWardens();
                        });
                }
            } else {
                // If date is undefined, clear results
                resetAllAvailableGameWardens();
                updateAvailableGameWardens();
            }
        }

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

        $ctrl.reloadAttachments = function () {
            HuntingControlEvents.listAttachments({id: $ctrl.event.id}).$promise
                .then(function (attachments) {
                    $ctrl.event.attachments = attachments;
                    $ctrl.attachmentsDeleted = true;
                });
        };

        $scope.$watch("$ctrl.event.geoLocation", function (geoLocation) {
            if (!geoLocation) {
                return;
            }

            GIS.getRhyForGeoLocation(geoLocation).then(function (rhyData) {
                $ctrl.inRhyArea = rhyData.data.id === $ctrl.rhy.id;
            });
        }, true);
    });