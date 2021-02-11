'use strict';

angular.module('app.harvestpermit.application.dogunleash.eventdetails', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.dogunleash.eventdetails', {
                url: '/eventdetails',
                templateUrl: 'harvestpermit/applications/dogunleash/eventdetails/eventdetails.html',
                controller: 'DogUnleashEventDetailsController',
                controllerAs: '$ctrl',
                resolve: {
                    application: function (applicationId, HarvestPermitApplications) {
                        return HarvestPermitApplications.get({id: applicationId}).$promise;
                    },
                    eventList: function (applicationId, DogUnleashApplication) {
                        return DogUnleashApplication.getEventDetails({id: applicationId}).$promise;
                    },
                    isAmending: _.constant(false)
                }
            })
            .state('jht.decision.application.wizard.dogunleash.eventdetails', {
                url: '/eventdetails',
                templateUrl: 'harvestpermit/applications/dogunleash/eventdetails/eventdetails.html',

                controller: 'DogUnleashEventDetailsController',
                controllerAs: '$ctrl',
                resolve: {
                    application: function (applicationId, HarvestPermitApplications) {
                        return HarvestPermitApplications.get({id: applicationId}).$promise;
                    },
                    eventList: function (applicationId, DogUnleashApplication) {
                        return DogUnleashApplication.getEventDetails({id: applicationId}).$promise;
                    },
                    isAmending: _.constant(true)
                }
            });
    })
    .controller('DogUnleashEventDetailsController', function ($state, wizard, applicationId, eventList, isAmending,
                                                              DogUnleashApplication,
                                                              ApplicationWizardNavigationHelper,
                                                              DogEventDetailsModal) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.eventList = _.orderBy(eventList, ['beginDate'], ['asc']);
            $ctrl.selectedEvent = null;
            $ctrl.isAmending = isAmending;
        };

        $ctrl.exit = function () {
            wizard.exit();
        };

        $ctrl.previous = function () {
            wizard.goto('mapdetails');
        };

        $ctrl.isNextDisabled = function () {
            return $ctrl.eventList.length < 1;
        };

        $ctrl.next = function () {
            wizard.goto('attachments');
        };

        $ctrl.add = function () {
            DogEventDetailsModal.open(
                {}, // empty event object
                function (event) {
                    save(event.event);
                    },
                $ctrl.isAmending);
        };

        $ctrl.onCopy = function (event) {
            event.id = null;
            save(event);
        };

        $ctrl.onDelete = function (event) {
            remove(event.id);
        };

        $ctrl.onEdit = function (event) {
            save(event);
        };

        function remove(eventId) {
            return DogUnleashApplication.deleteEvent({id: applicationId, eventId: eventId}).$promise
                .then(function () {
                    refreshEventList();
                });
        }

        function save(eventDetails) {
            DogUnleashApplication.updateEvent({id: applicationId}, allowedFields(eventDetails)).$promise
                .then(function (savedEvent) {
                    $ctrl.selectedEvent = savedEvent.id;
                    refreshEventList();
                });
        }

        function allowedFields(event) {
            return  _.pick(event,[
                'id',
                'eventType',
                'beginDate',
                'endDate',
                'dogsAmount',
                'naturaArea',
                'eventDescription',
                'locationDescription',
                'contactName',
                'contactMail',
                'contactPhone',
                'additionalInfo',
                'geoLocation'
            ]);
        }

        function refreshEventList() {
            DogUnleashApplication.getEventDetails({id: applicationId}).$promise
                .then(function (events) {
                    $ctrl.eventList = _.orderBy(events, ['beginDate'], ['asc']);
                    $ctrl.eventList.forEach(function (event) {
                        event.selected = event.id === $ctrl.selectedEvent;
                    });
                });
        }

    })
    .service('DogEventDetailsModal', function ($uibModal, MapDefaults, MapUtil, MapState, DogEventType) {

        this.open = function (event, onSave, isAmending) {
            $uibModal.open({ templateUrl: 'harvestpermit/applications/dogunleash/eventdetails/edit.html',
                               controllerAs: '$modalCtrl',
                               resolve: { event: _.constant(event),
                                   onSave: _.constant(onSave),
                                   isAmending: _.constant(isAmending)},
                               size: 'lg',
                               controller: ModalController}).result
                .then(function (event) {
                    return onSave({event: event});
                });
        };

        function ModalController($scope, $uibModalInstance, $filter, event, isAmending) {
            var $modalCtrl = this;
            $modalCtrl.naturaAreaInfo = "";
            $modalCtrl.event = event;
            $modalCtrl.eventTypes = [DogEventType.DOG_TRAINING, DogEventType.DOG_TEST];
            $modalCtrl.canEditNaturaField = isAmending;

            $modalCtrl.beginDateOptions = {
                minDate: new Date()
            };

            $modalCtrl.endDateOptions = {
                minDate: $modalCtrl.event.beginDate ? new Date($modalCtrl.event.beginDate) : new Date()
            };

            $modalCtrl.updateDatePickerLimits = function () {
                $modalCtrl.endDateOptions.minDate = $modalCtrl.event.beginDate ? new Date($modalCtrl.event.beginDate) : new Date();

                // Manually set values are not validated by this
                // (This is not a bug, it's a feature https://github.com/angular-ui/bootstrap/issues/4664)
                //
                // Validation is done by date-between-min-max, min-date and r-validate-greater-or-equal.
            };

            var location = event.geoLocation;

            if (!isValidLocation(location)) {
                location = MapUtil.getDefaultGeoLocation();
            }

            MapState.updateMapCenter(angular.copy(location), 7);

            $modalCtrl.mapState = MapState.get();
            $modalCtrl.mapDefaults = MapDefaults.create({scrollWheelZoom: false});
            $modalCtrl.mapEvents = MapDefaults.getMapBroadcastEvents(['click']);

            $modalCtrl.isValidLocation = function () {
                return isValidLocation($modalCtrl.event.geoLocation);
            };

            $modalCtrl.ok = function () {
                $uibModalInstance.close($modalCtrl.event);
            };

            $modalCtrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };

            $scope.$watch('$modalCtrl.naturaAreaInfo', function(areaInfo) {
                if (_.isObject(areaInfo)
                    && _.isString(areaInfo.naturaId)
                    && _.isString(areaInfo.conservationType)
                    && (_.isString(areaInfo.nameFI) || _.isString(areaInfo.nameSV))) {

                    var i18n = $filter('rI18nNameFilter');

                    $modalCtrl.event.naturaArea = areaInfo.naturaId +
                        ", " + areaInfo.conservationType +
                        ", " + i18n(areaInfo);
                } else {
                    $modalCtrl.event.naturaArea = "";
                }

            });

            function isValidLocation(location) {
                return _.isObject(location)
                    && _.isNumber(location.latitude)
                    && _.isNumber(location.longitude);
            }

        }
    })
    .component('rDogEventDetails', {
        templateUrl: 'harvestpermit/applications/dogunleash/eventdetails/show.html',
        bindings: {
            eventDetails: '<',
            onCopy: '&',
            onDelete: '&',
            onEdit: '&',
            isAmending: '<'
        },
        controller: function (DogEventDetailsModal) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.event = angular.copy($ctrl.eventDetails);
            };

            $ctrl.copy = function () {
                var eventCopy = angular.copy($ctrl.event);
                DogEventDetailsModal.open(
                    eventCopy,
                    function (event) {
                        $ctrl.onCopy({event: event});
                    },
                    $ctrl.isAmending);
            };

            $ctrl.delete = function () {
                $ctrl.onDelete({event: {event: $ctrl.event}});
            };

            $ctrl.edit = function () {
                DogEventDetailsModal.open(
                    $ctrl.event,
                    function (event) {
                        $ctrl.onEdit({event: event});
                    },
                    $ctrl.isAmending);
            };
        }
    })
    .component('rDogEventList', {
        templateUrl: 'harvestpermit/applications/dogunleash/eventdetails/list.html',
        bindings: {
            eventList: '<',
            onCopy: '&',
            onDelete: '&',
            onEdit: '&',
            isAmending: '<'
        }
    })
    .component('rParagraphedText', {
        template: '<p ng-repeat="p in $ctrl.content.split(\'\\n\') track by $index" ng-bind="p"></p>',
        bindings: {
            content: '<'
        }
    });
