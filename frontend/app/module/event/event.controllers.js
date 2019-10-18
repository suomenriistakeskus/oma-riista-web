'use strict';

angular.module('app.event.controllers', ['ui.router', 'app.event.services'])
    .controller('EventListController',
        function ($scope, $uibModal, Helpers, NotificationService, EventsByYear, EventTypes, Venues, orgId, AnnualStatisticsAvailableYears) {
            $scope.events = [];
            $scope.availableYears = AnnualStatisticsAvailableYears.get();
            $scope.calendarYear = _.last($scope.availableYears);
            $scope.eventTypeFilter = null;
            EventTypes.then(function (result) {
                $scope.eventTypes = result.data;
            });

            var filterByEventType = function (eventType) {
                if (eventType) {
                    $scope.events = _.filter($scope.allEvents, function (event) {
                        return event.calendarEventType === eventType;
                    });
                } else {
                    $scope.events = $scope.allEvents;
                }
            };

            var reloadPage = function () {
                EventsByYear.query({orgId: orgId, year: $scope.calendarYear}).$promise.then(function (data) {
                    $scope.allEvents = data.map(function(event) {
                        if (event.additionalCalendarEvents && event.additionalCalendarEvents.length > 0) {
                            var key = 1;
                            event.additionalCalendarEvents = event.additionalCalendarEvents.map(function(additionalEvent) {
                                return angular.extend({}, additionalEvent, {key: key++});
                            });
                        }
                        return event;
                    });

                    filterByEventType($scope.eventTypeFilter);
                });
            };
            reloadPage();

            var onSuccess = function() {
                reloadPage();

                NotificationService.showDefaultSuccess();
            };

            var onFailure = function(reason) {
                if (reason === 'error') {
                    NotificationService.showDefaultFailure();
                }
            };

            $scope.onSelectedYearChanged = function () {
                reloadPage();
            };

            $scope.onEventTypeChanged = function () {
                filterByEventType($scope.eventTypeFilter);
            };

            $scope.addEvent = function () {
                $uibModal.open({
                    templateUrl: 'event/event_form.html',
                    resolve: {
                        orgId: Helpers.wrapToFunction(orgId),
                        eventTypes: Helpers.wrapToFunction(EventTypes),
                        event: Helpers.wrapToFunction({publicVisibility: true, excludedFromStatistics:false}),
                        venues: function () {
                            return Venues.query({orgId: orgId});
                        }
                    },
                    controller: 'EventFormController',
                    size: 'lg'
                }).result.then(onSuccess, onFailure);

            };

            $scope.clone = function (event) {
                var newEvent = angular.copy(event);

                delete newEvent.id;
                delete newEvent.rev;
                delete newEvent.date;
                delete newEvent.lockedAsPastCalendarEvent;
                delete newEvent.lockedAsPastStatistics;

                newEvent.additionalCalendarEvents = [];

                $scope.show(newEvent);
            };

            $scope.show = function (event) {
                $uibModal.open({
                    templateUrl: 'event/event_form.html',
                    resolve: {
                        orgId: Helpers.wrapToFunction(orgId),
                        eventTypes: Helpers.wrapToFunction(EventTypes),
                        event: Helpers.wrapToFunction(angular.copy(event)),
                        venues: function () {
                            return Venues.query({orgId: orgId}).$promise;
                        }
                    },
                    controller: 'EventFormController',
                    size: 'lg'
                }).result.then(onSuccess, onFailure);

            };
            $scope.remove = function (event) {
                $uibModal.open({
                    templateUrl: 'event/event_remove.html',
                    resolve: {
                        orgId: Helpers.wrapToFunction(orgId),
                        event: Helpers.wrapToFunction(event)
                    },
                    controller: 'EventRemoveController'
                }).result.then(onSuccess, onFailure);
            };
        })

    .controller('EventFormController',
        function ($scope, $uibModalInstance, Events, venues, eventTypes, orgId, event, Helpers, ActiveRoleService) {
            $scope.venues = venues;

            var findVenue = function (venue) {
                for (var i = 0; i < venues.length; i++) {
                    if (venues[i].id === venue.id) {
                        return venues[i];
                    }
                }
            };

            var findAdditionalEvent = function (key) {
                var position = $scope.viewState.event.additionalCalendarEvents.findIndex(function(additionalEvent) {
                    return additionalEvent.key === key;
                });

                return position;
            };

            $scope.isAdditionalEventsAllowed = function () {
                return $scope.viewState.event.calendarEventType === 'METSASTAJAKURSSI';
            };

            $scope.isAdditionalEventsTableVisible = function () {
                return $scope.isAdditionalEventsAllowed() &&
                    $scope.viewState.event.additionalCalendarEvents.length > 0;
            };

            $scope.isEventTypeSelectionDisabled = function () {
                return $scope.viewState.event.lockedAsPastCalendarEvent ||
                    $scope.isInRowEditMode() ||
                    ($scope.viewState.event.additionalCalendarEvents && $scope.viewState.event.additionalCalendarEvents.length > 0);
            };

            $scope.viewState = {
                event: event,
                editedEvent: null,
                cancelEditedEventFunc: $scope.cancelEditEvent
            };
            $scope.viewState.event.venue = findVenue($scope.viewState.event.venue);

            var sortDateTime = function (e) {
                var dateStr = e.date + 'T' + e.beginTime;
                var date = Helpers.toMoment(dateStr, 'YYYY-MM-DDTHH:mm');
                return date;
            };
            $scope.viewState.event.additionalCalendarEvents =
                _.sortBy($scope.viewState.event.additionalCalendarEvents, sortDateTime);

            $scope.eventDefaultVisibility = {
                AMPUMAKOE: true,
                JOUSIAMPUMAKOE: true,
                METSASTAJAKURSSI: true,
                METSASTAJATUTKINTO: true,
                VUOSIKOKOUS: true,
                YLIMAARAINEN_KOKOUS: true,
                AMPUMAKILPAILU: true,
                RIISTAPOLKUKILPAILU: true,
                ERATAPAHTUMA: true,
                HARJOITUSAMMUNTA: true,
                METSASTYKSENJOHTAJA_HIRVIELAIMET: true,
                METSASTYKSENJOHTAJA_SUURPEDOT: true,
                METSASTAJAKOULUTUS_HIRVIELAIMET: true,
                METSASTAJAKOULUTUS_SUURPEDOT: true,
                SRVAKOULUTUS: true,
                PETOYHDYSHENKILO_KOULUTUS: true,
                VAHINKOKOULUTUS: true,
                TILAISUUS_KOULUILLLE: true,
                OPPILAITOSTILAISUUS: true,
                NUORISOTILAISUUS: true,
                AMPUMAKOKEENVASTAANOTTAJA_KOULUTUS: true,
                METSASTAJATUTKINNONVASTAANOTTAJA_KOULUTUS: true,
                RIISTAVAHINKOTARKASTAJA_KOULUTUS: true,
                METSASTYKSENVALVOJA_KOULUTUS: true,
                PIENPETOJEN_PYYNTI_KOULUTUS: true,
                RIISTALASKENTA_KOULUTUS: true,
                RIISTAKANTOJEN_HOITO_KOULUTUS: true,
                RIISTAN_ELINYMPARISTON_HOITO_KOULUTUS: true,
                MUU_RIISTANHOITOKOULUTUS: true,
                AMPUMAKOULUTUS: true,
                JALJESTAJAKOULUTUS: true,
                MUU_TAPAHTUMA: true,
                RHY_HALLITUKSEN_KOKOUS: false
            };

            $scope.eventTypes = eventTypes.data;

            $scope.removeAdditionalEvent = function(key) {
                var elemPos = findAdditionalEvent(key);
                $scope.viewState.event.additionalCalendarEvents.splice(elemPos, 1);

                $scope.viewState.event.additionalCalendarEvents =
                    _.sortBy($scope.viewState.event.additionalCalendarEvents, sortDateTime);

                for (var i = 1; i < $scope.viewState.event.additionalCalendarEvents; i++) {
                    $scope.viewState.event.additionalCalendarEvents[i].key = i;
                }

                $scope.cancelEditEvent();
            };

            $scope.addAdditionalEvent = function() {
                $scope.viewState.cancelEditedEventFunc = $scope.removeAdditionalEvent;

                if (!$scope.viewState.event.additionalCalendarEvents) {
                    $scope.viewState.event.additionalCalendarEvents = [];
                }

                var newEvent = {
                    date: null,
                    beginTime: null,
                    endTime: null,
                    venue: null,
                    key: $scope.viewState.event.additionalCalendarEvents.length + 1
                };

                $scope.viewState.event.additionalCalendarEvents.push(newEvent);

                $scope.viewState.editedEvent = newEvent;
            };

            $scope.isHunterExamTraining = function () {
                return $scope.viewState.event.calendarEventType === 'METSASTAJAKURSSI';
            };

            $scope.isInRowEditMode = function () {
                return $scope.viewState.editedEvent !== null;
            };

            $scope.cancelEditEvent = function () {
                $scope.viewState.editedEvent = null;
            };

            $scope.saveEditEvent = function (key) {
                var elemPos = findAdditionalEvent(key);
                $scope.viewState.event.additionalCalendarEvents[elemPos] = angular.copy($scope.viewState.editedEvent);
                $scope.viewState.event.additionalCalendarEvents =
                    _.sortBy($scope.viewState.event.additionalCalendarEvents, sortDateTime);

                $scope.viewState.event.additionalCalendarEvents[elemPos].venue =
                    findVenue($scope.viewState.event.additionalCalendarEvents[elemPos].venue);

                $scope.viewState.editedEvent = null;
            };

            $scope.editEvent = function (event) {
                $scope.viewState.cancelEditedEventFunc = $scope.cancelEditEvent;

                var elemPos = findAdditionalEvent(event.key);
                $scope.viewState.editedEvent = angular.copy($scope.viewState.event.additionalCalendarEvents[elemPos]);

                $scope.viewState.editedEvent.venue = findVenue($scope.viewState.editedEvent.venue);
            };

            $scope.isParticipantsShown = function() {
                var e = $scope.viewState.event;
                if (e) {
                    if (e.calendarEventType === 'AMPUMAKOE' ||
                    e.calendarEventType === 'JOUSIAMPUMAKOE' ||
                    e.calendarEventType === 'METSASTAJATUTKINTO') {
                        return false;
                    }
                }
                return true;
            };

            $scope.onSelectionChange = function() {
                $scope.viewState.event.publicVisibility = $scope.eventDefaultVisibility[$scope.viewState.event.calendarEventType];
            };

            $scope.isDateRequiredAtLeast7DaysIntoFuture = function () {
                var e = $scope.viewState.event;
                if ((e.calendarEventType === 'AMPUMAKOE' || e.calendarEventType === 'JOUSIAMPUMAKOE') && e.date) {
                    return Helpers.toMoment(e.date).subtract(6, 'days').isBefore(moment());
                }
                return false;
            };

            $scope.isDateTooFarInThePast = function() {
                if (ActiveRoleService.isModerator()) {
                    return false;
                }

                var e = $scope.viewState.event;

                if (e.date) {
                    var eventYear = Helpers.toMoment(e.date).year();
                    if (eventYear < moment().subtract(15, 'days').year()) {
                        return true;
                    }
                }

                return false;
            };

            $scope.isDateInvalid = function () {
                return !$scope.viewState.event.id && ($scope.isDateRequiredAtLeast7DaysIntoFuture() || $scope.isDateTooFarInThePast());
            };

            $scope.save = function (viewState) {
                var savedEvent = angular.copy(viewState.event);// prevent ui showing updated object properties on save

                savedEvent.date = Helpers.dateToString(savedEvent.date);
                if (savedEvent.additionalCalendarEvents) {
                    savedEvent.additionalCalendarEvents = savedEvent.additionalCalendarEvents.map(function (additionalCalendarEvent) {
                        delete additionalCalendarEvent.key;
                        return additionalCalendarEvent;
                    });
                }

                var saveMethod = !savedEvent.id ? Events.save : Events.update;

                saveMethod({orgId: orgId}, savedEvent).$promise
                    .then(function() {
                        $uibModalInstance.close();
                    }, function() {
                        $uibModalInstance.dismiss('error');
                    });
            };
            $scope.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        })
    .controller('EventRemoveController',
        function ($scope, $uibModalInstance, Events, orgId, event) {
            $scope.event = event;
            $scope.remove = function () {
                Events.delete({orgId: orgId}, event).$promise
                    .then(function() {
                        $uibModalInstance.close();
                    }, function() {
                        $uibModalInstance.dismiss('error');
                    });
            };
            $scope.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        })
    .controller('VenueListController',
        function ($scope, $uibModal, Helpers, NotificationService, Venues, orgId) {
            var reloadPage = function () {
                $scope.venues = Venues.query({orgId: orgId});
            };

            reloadPage();

            var onSuccess = function() {
                reloadPage();

                NotificationService.showDefaultSuccess();
            };

            var onFailure = function(reason) {
                if (reason === 'error') {
                    NotificationService.showDefaultFailure();
                }
            };

            $scope.addVenue = function () {
                $uibModal.open({
                    templateUrl: 'event/venue_add.html',
                    resolve: {
                        orgId: Helpers.wrapToFunction(orgId)
                    },
                    controller: 'VenueAddController'
                }).result.then(onSuccess, onFailure);

            };
            $scope.show = function (venue) {
                $uibModal.open({
                    templateUrl: 'event/venue_form.html',
                    resolve: {
                        orgId: Helpers.wrapToFunction(orgId),
                        venue: Helpers.wrapToFunction(angular.copy(venue))
                    },
                    controller: 'VenueFormController'
                }).result.then(onSuccess, onFailure);
            };
            $scope.remove = function (venue) {
                $uibModal.open({
                    templateUrl: 'event/venue_remove.html',
                    resolve: {
                        orgId: Helpers.wrapToFunction(orgId),
                        venue: Helpers.wrapToFunction(venue)
                    },
                    controller: 'VenueRemoveController'
                }).result.then(onSuccess, onFailure);
            };
        })
    .controller('VenueAddController',
        function ($scope, $uibModalInstance, $uibModal, Helpers, Venues, VenueSearchByName, orgId) {
            $scope.venue = {};
            var updatePager = function () {
                $scope.pager = {
                    currentPage: $scope.page.pageable.page ? $scope.page.pageable.page + 1 : 0,
                    pageSize: $scope.page.pageable.size,
                    total: $scope.page.total
                };
            };
            var noContent = function () {
                $scope.page = {content: [], pageable: {size: 5}, total: 0};
                updatePager();
            };
            noContent();
            var cb = function (page) {
                $scope.page = page;
                updatePager();
            };
            var reloadPage = function (keyTyped) {
                if ($scope.venue.name) {
                    var params = {
                        page: ($scope.pager.currentPage - 1),
                        size: ($scope.pager.pageSize),
                        sort: 'name',
                        term: $scope.venue.name
                    };
                    if (keyTyped) {
                        params.page = 0;
                        VenueSearchByName.search(params, cb);
                    } else {
                        VenueSearchByName.searchImmediately(params, cb);
                    }
                }
            };
            $scope.$watch('venue.name', function () {
                reloadPage(true);
            });
            $scope.$watch('pager.currentPage', function () {
                reloadPage(false);
            });

            $scope.use = function (venue) {
                Venues.attach({orgId: orgId, id: venue.id}).$promise
                    .then(function() {
                        $uibModalInstance.close();
                    }, function() {
                        $uibModalInstance.dismiss('error');
                    });
            };
            $scope.addNew = function () {
                $uibModal.open({
                    templateUrl: 'event/venue_form.html',
                    resolve: {
                        orgId: Helpers.wrapToFunction(orgId),
                        venue: Helpers.wrapToFunction($scope.venue)
                    },
                    controller: 'VenueFormController'
                }).result
                    .then(function() {
                        $uibModalInstance.close();
                    }, function() {
                        $uibModalInstance.dismiss('error');
                    });
            };
            $scope.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        })
    .controller('VenueFormController',
        function ($scope, $uibModalInstance, Venues, venue, orgId) {
            $scope.venue = venue;

            $scope.save = function (venue) {
                var saveMethod = !venue.id ? Venues.save : Venues.update;

                saveMethod({orgId: orgId}, venue).$promise
                    .then(function() {
                        $uibModalInstance.close();
                    }, function() {
                        $uibModalInstance.dismiss('error');
                    });
            };

            $scope.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        })
    .controller('VenueRemoveController',
        function ($scope, $uibModalInstance, Venues, orgId, venue) {
            $scope.venue = venue;
            $scope.remove = function () {
                Venues.detach({orgId: orgId, id: venue.id}).$promise
                    .then(function() {
                        $uibModalInstance.close();
                    }, function() {
                        $uibModalInstance.dismiss('error');
                    });
            };
            $scope.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        });

