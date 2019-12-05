(function () {
    "use strict";

    angular.module('app.shootingtest', ['ngResource'])
        .config(function ($stateProvider) {
            $stateProvider
                .state('rhy.shootingtest', {
                    abstract: true,
                    url: '/shootingtest',
                    template: '<div ui-view autoscroll="false"></div>'
                })
                .state('rhy.shootingtest.stats', {
                    url: '/stats',
                    templateUrl: 'shootingtest/stats.html',
                    controller: 'ShootingTestStatsController',
                    controllerAs: '$ctrl',
                    resolve: {
                        availableYears: function () {
                            return _.range(2017, new Date().getUTCFullYear() + 1);
                        },
                        fetchStatistics: function (ShootingTestStatistics, rhyId) {
                            return function (calendarYear) {
                                return ShootingTestStatistics.get({
                                    rhyId: rhyId,
                                    calendarYear: calendarYear
                                }).$promise;
                            };
                        },
                        initialStatistics: function (availableYears, fetchStatistics) {
                            return fetchStatistics(_.last(availableYears));
                        },
                        exportToExcel: function (FormPostService, rhyId) {
                            return function (year) {
                                FormPostService.submitFormUsingBlankTarget('/api/v1/shootingtest/statistics/excel', {
                                    rhyId: rhyId, calendarYear: year
                                });
                            };
                        }
                    }
                })
                .state('rhy.shootingtest.events', {
                    url: '/events',
                    templateUrl: 'shootingtest/shooting-test-events.html',
                    controller: 'ShootingTestEventListController',
                    controllerAs: '$ctrl',
                    resolve: {
                        events: function (ShootingTestCalendarEvents, rhyId) {
                            return ShootingTestCalendarEvents.query({rhyId: rhyId}).$promise;
                        }
                    }
                })
                .state('rhy.shootingtest.event', {
                    abstract: true,
                    url: '/event/{calendarEventId:[0-9]+}',
                    templateUrl: 'shootingtest/shooting-test-event-common.html',
                    resolve: {
                        calendarEventId: function ($stateParams) {
                            return $stateParams.calendarEventId;
                        },
                        event: function (ShootingTestCalendarEvents, calendarEventId) {
                            return ShootingTestCalendarEvents.get({calendarEventId: calendarEventId}).$promise;
                        },
                        hasUpdatePermission: function (ShootingTestEventService, event) {
                            return ShootingTestEventService.isUserGrantedUpdatePermission(event.officials);
                        }
                    },
                    controller: function (event, hasUpdatePermission) {
                        this.event = event;
                        this.hasUpdatePermission = hasUpdatePermission;
                    },
                    controllerAs: '$ctrl'
                })
                .state('rhy.shootingtest.event.overview', {
                    url: '/overview',
                    templateUrl: 'shootingtest/shooting-test-event-overview.html',
                    controller: 'ShootingTestEventOverviewController',
                    controllerAs: '$ctrl',
                })
                .state('rhy.shootingtest.event.registration', {
                    url: '/registration',
                    templateUrl: 'shootingtest/shooting-test-event-registration.html',
                    controller: 'ShootingTestRegistrationController',
                    controllerAs: '$ctrl',
                })
                .state('rhy.shootingtest.event.participants', {
                    url: '/participants',
                    templateUrl: 'shootingtest/shooting-test-event-participants.html',
                    controller: 'ShootingTestParticipantsController',
                    controllerAs: '$ctrl',
                    resolve: {
                        participants: function (ShootingTestParticipant, event) {
                            var params = {
                                eventId: event.shootingTestEventId,
                                unfinishedOnly: true
                            };

                            return ShootingTestParticipant.list(params).$promise
                                .then(function (participants) {
                                    var attemptExistenceSort = function (participant) {
                                        return participant.attempts.length === 0 ? 0 : 1;
                                    };
                                    var sortCriterias = [attemptExistenceSort, 'registrationTime', 'lastName', 'firstName', 'hunterNumber'];

                                    return _.sortBy(participants, sortCriterias);
                                });
                        }
                    }
                })
                .state('rhy.shootingtest.event.participant', {
                    url: '/participant/{participantId:[0-9]+}',
                    templateUrl: 'shootingtest/participant-attempts.html',
                    controller: 'ShootingTestAttemptsController',
                    controllerAs: '$ctrl',
                    resolve: {
                        participantId: function ($stateParams) {
                            return $stateParams.participantId;
                        },
                        participant: function (ShootingTestParticipant, participantId) {
                            var params = {participantId: participantId};
                            return ShootingTestParticipant.getParticipantWithDetailedAttempts(params).$promise;
                        }
                    }
                })
                .state('rhy.shootingtest.event.payments', {
                    url: '/payments',
                    templateUrl: 'shootingtest/shooting-test-event-payments.html',
                    controller: 'ShootingTestPaymentsController',
                    controllerAs: '$ctrl',
                    resolve: {
                        participants: function (ShootingTestParticipant, event) {
                            return ShootingTestParticipant.list({eventId: event.shootingTestEventId}).$promise
                                .then(function (participants) {
                                    var partitionByCompleted = _.partition(participants, 'completed');

                                    var completed = _.sortBy(partitionByCompleted[0], ['lastName', 'firstName', 'hunterNumber']);

                                    var attemptExistenceSort = function (participant) {
                                        return participant.attempts.length === 0 ? 0 : 1;
                                    };
                                    var uncompletedSortCriteria = [attemptExistenceSort, 'registrationTime', 'lastName', 'firstName', 'hunterNumber'];
                                    var uncompleted = _.sortBy(partitionByCompleted[1], uncompletedSortCriteria);

                                    return uncompleted.concat(completed);
                                });
                        }
                    }
                });
        })

        .constant('SHOOTING_TEST_ATTEMPT_PRICE', 20.0)

        .factory('ShootingTestCalendarEvents', function ($resource) {
            function decorateRepository(repository) {
                angular.extend(repository.prototype, {
                    isOpened: function () {
                        return _.isFinite(this.shootingTestEventId);
                    },
                    isClosed: function () {
                        return !!this.lockedTime;
                    },
                    isOpen: function () {
                        return this.isOpened() && !this.isClosed();
                    }
                });
                return repository;
            }

            var defaultParams = {
                calendarEventId: '@calendarEventId'
            };

            var repository = $resource('/api/v1/shootingtest/calendarevent/:calendarEventId', defaultParams, {
                'query': {
                    method: 'GET',
                    url: '/api/v1/shootingtest/rhy/:rhyId/calendarevents',
                    params: {rhyId: '@rhyId'},
                    isArray: true
                },
                'get': {
                    method: 'GET'
                },
                'open': {
                    method: 'POST',
                    url: '/api/v1/shootingtest/calendarevent/:calendarEventId/open'
                }
            });

            return decorateRepository(repository);
        })

        .service('ShootingTestCalendarEventsService', function (FetchAndSaveBlob, $translate) {
            this.loadPaymentSummaryPdf = function (calendarEventId) {
                var lang = $translate.use();
                return FetchAndSaveBlob.post('/api/v1/shootingtest/summary/event/' +
                    calendarEventId + '/event-summary.pdf' + '?lang=' + lang);
            };
        })

        .factory('ShootingTestEvent', function ($resource) {
            return $resource('/api/v1/shootingtest/event/:eventId', {eventId: '@eventId'}, {
                'close': {
                    method: 'POST',
                    url: '/api/v1/shootingtest/event/:eventId/close'
                },
                'reopen': {
                    method: 'POST',
                    url: '/api/v1/shootingtest/event/:eventId/reopen'
                }
            });
        })

        .factory('ShootingTestStatistics', function ($resource) {
            var defaultParams = {
                rhyId: '@rhyId',
                calendarYear: '@calendarYear'
            };

            return $resource('/api/v1/shootingtest/rhy/:rhyId/statistics/:calendarYear', defaultParams, {
                'get': {
                    method: 'GET'
                }
            });
        })

        .factory('ShootingTestOfficials', function ($resource) {
            var defaultParams = {
                shootingTestEventId: '@shootingTestEventId'
            };

            return $resource('/api/v1/shootingtest/event/:shootingTestEventId/officials', defaultParams, {
                'listAvailable': {
                    method: 'GET',
                    url: '/api/v1/shootingtest/rhy/:rhyId/officials',
                    params: {
                        rhyId: '@rhyId',
                        eventDate: '@eventDate'
                    },
                    isArray: true
                },
                'listQualifying': {
                    method: 'GET',
                    url: '/api/v1/shootingtest/event/:shootingTestEventId/qualifyingofficials',
                    isArray: true
                },
                'listAssigned': {
                    method: 'GET',
                    url: '/api/v1/shootingtest/event/:shootingTestEventId/assignedofficials',
                    isArray: true
                },
                'update': {
                    method: 'PUT'
                }
            });
        })

        .service('ShootingTestOfficialService', function (NotificationService, ShootingTestOfficials) {
            var self = this;

            self.listAvailable = function (rhyId, date) {
                return ShootingTestOfficials.listAvailable({rhyId: rhyId, eventDate: date}).$promise;
            };

            self.listQualifying = function (shootingTestEventId) {
                return ShootingTestOfficials.listQualifying({shootingTestEventId: shootingTestEventId}).$promise;
            };

            self.listAssigned = function (shootingTestEventId) {
                return ShootingTestOfficials.listAssigned({shootingTestEventId: shootingTestEventId}).$promise;
            };

            self.update = function (shootingTestEventId, shootingTestOfficialOccupationIds, shootingTestResponsibleOccupationId) {
                var pathParams = {shootingTestEventId: shootingTestEventId};
                var bodyParams = {
                    shootingTestEventId: shootingTestEventId,
                    occupationIds: shootingTestOfficialOccupationIds,
                    responsibleOccupationId: shootingTestResponsibleOccupationId
                };

                return ShootingTestOfficials.update(pathParams, bodyParams).$promise
                    .then(function (response) {
                        NotificationService.showDefaultSuccess();
                        return response;
                    });
            };
        })

        .factory('ShootingTestPersonSearch', function (HttpPost) {
            return {
                findByHunterNumber: function (shootingTestEventId, hunterNumber) {
                    var url = '/api/v1/shootingtest/event/' + shootingTestEventId + '/findperson/hunternumber';
                    return HttpPost.post(url, {hunterNumber: hunterNumber});
                }
            };
        })

        .factory('ShootingTestParticipant', function ($resource) {
            return $resource('/api/v1/shootingtest/event/:eventId/participants', {'eventId': '@eventId'}, {
                'list': {
                    method: 'GET',
                    params: {unfinishedOnly: "@unfinishedOnly"},
                    isArray: true
                },
                'register': {
                    method: 'POST',
                    url: '/api/v1/shootingtest/event/:eventId/participant',
                    params: {eventId: '@eventId'}
                },
                'get': {
                    method: 'GET',
                    url: '/api/v1/shootingtest/participant/:participantId',
                    params: {'participantId': '@participantId'}
                },
                'getParticipantWithDetailedAttempts': {
                    method: 'GET',
                    url: '/api/v1/shootingtest/participant/:participantId/attempts',
                    params: {'participantId': '@participantId'}
                },
                'completePayment': {
                    method: 'PUT',
                    url: '/api/v1/shootingtest/participant/:participantId/payment',
                    params: {'participantId': '@participantId'}
                },
                'updatePayment': {
                    method: 'POST',
                    url: '/api/v1/shootingtest/participant/:participantId/payment',
                    params: {'participantId': '@participantId'}
                }
            });
        })

        .factory('ShootingTestAttempt', function ($resource) {
            return $resource('/api/v1/shootingtest/attempt/:attemptId', {'attemptId': '@attemptId'}, {
                'get': {
                    method: 'GET'
                },
                'create': {
                    method: 'PUT',
                    url: '/api/v1/shootingtest/participant/:participantId/attempt',
                    params: {'participantId': '@participantId'}
                },
                'update': {
                    method: 'POST'
                },
                'remove': {
                    method: 'DELETE'
                }
            });
        })

        .service('ShootingTestEventService', function (ActiveRoleService) {
            var self = this;

            this.findOfficialMatchingActiveOccupationId = function (officials) {
                var activeOccupationId = ActiveRoleService.getActiveOccupationId();

                return _.find(officials, function (official) {
                    return official.occupationId === activeOccupationId;
                });
            };

            this.isUserGrantedUpdatePermission = function (officials) {
                if (ActiveRoleService.isModerator() || ActiveRoleService.isCoordinator()) {
                    return true;
                }

                return ActiveRoleService.isShootingTestOfficial()
                    && !!self.findOfficialMatchingActiveOccupationId(officials);
            };
        })

        .controller('ShootingTestEventListController', function ($state, events) {
            var $ctrl = this;

            $ctrl.events = events;

            $ctrl.selectEvent = function (calendarEventId) {
                $state.go('rhy.shootingtest.event.overview', {calendarEventId: calendarEventId});
            };

            var isEventInPast = function (event) {
                var eventDateMoment = moment(event.date);
                return moment().diff(eventDateMoment, 'days') > 0;
            };

            $ctrl.isEventNotClosedOnTime = function (event) {
                return !!event.shootingTestEventId && !event.lockedTime && isEventInPast(event);
            };

            $ctrl.isEventUnpopulated = function (event) {
                return isEventInPast(event) && !event.shootingTestEventId;
            };

            $ctrl.isEventIncomplete = function (event) {
                return $ctrl.isEventNotClosedOnTime(event) || $ctrl.isEventUnpopulated(event);
            };
        })

        .component('shootingTestTabSelection', {
            templateUrl: 'shootingtest/tab-selection.html',
            bindings: {
                event: '<',
                hasUpdatePermission: '<'
            },
            controller: function ($state) {
                var $ctrl = this;
                var states = ['^.overview', '^.registration', '^.participants', '^.payments'];

                $ctrl.$onInit = function () {
                    $ctrl.activeTabIndex = _.findIndex(states, function (state) {
                        return $state.is(state);
                    });

                    $ctrl.params = {
                        rhyId: $ctrl.event.rhyId,
                        calendarEventId: $ctrl.event.calendarEventId
                    };
                };

                $ctrl.selectTab = function (index) {
                    if ($ctrl.activeTabIndex !== index) {
                        $state.go(states[index], $ctrl.params, {reload: true});
                    }
                };

                $ctrl.isRegistrationEnabled = function () {
                    return $ctrl.event.isOpen() && $ctrl.hasUpdatePermission;
                };

                $ctrl.isParticipantsViewEnabled = function () {
                    return $ctrl.event.isOpen() && $ctrl.hasUpdatePermission;
                };

                $ctrl.isPaymentsViewEnabled = function () {
                    return $ctrl.event.isOpened() && $ctrl.hasUpdatePermission;
                };
            }
        })

        .controller('ShootingTestEventOverviewController',
            function ($state, $uibModal, NotificationService, ShootingTestCalendarEvents, ShootingTestOfficialService,
                      event, hasUpdatePermission, rhyId) {

                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.event = event;
                    $ctrl.hasUpdatePermission = hasUpdatePermission;

                    $ctrl.isEventOpened = $ctrl.event.isOpened();
                    $ctrl.canUpdateOfficials = hasUpdatePermission && $ctrl.event.isOpen();
                };

                $ctrl.reloadState = function () {
                    $state.reload();
                };

                $ctrl.openEvent = function () {
                    var modalInstance = $uibModal.open({
                        templateUrl: 'shootingtest/shooting-test-officials.html',
                        size: 'lg',
                        controllerAs: '$ctrl',
                        controller: 'ShootingTestOfficialEditController',
                        resolve: {
                            titleLocalisationKey: _.constant('shootingTest.overview.open'),
                            saveLocalisationKey: _.constant('shootingTest.overview.open'),
                            existingOfficials: _.constant([]),
                            availableOfficials: function () {
                                return ShootingTestOfficialService.listAvailable(rhyId, $ctrl.event.date);
                            }
                        }
                    });

                    modalInstance.result.then(function (officials) {
                        var calendarEventId = event.calendarEventId;
                        var params = {calendarEventId: calendarEventId};
                        var e = {
                            calendarEventId: calendarEventId,
                            occupationIds: _.map(officials, 'occupationId')
                        };

                        ShootingTestCalendarEvents.open(params, e).$promise
                            .then(NotificationService.showDefaultSuccess)
                            .finally($ctrl.reloadState);
                    });
                };
            })

        .component('showShootingTestEventInfo', {
            templateUrl: 'shootingtest/show-shooting-test-event-info.html',
            bindings: {
                event: '<',
                hasUpdatePermission: '<',
                openOfficialAssignDialog: '&',
                reload: '&'
            },
            controller: function ($filter, Helpers, NotificationService, ShootingTestEvent) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    var eventDate = Helpers.toMoment($ctrl.event.date);
                    var today = Helpers.toMoment(new Date());

                    $ctrl.canOpenEvent = !$ctrl.event.isOpened() && !eventDate.isAfter(today, 'day');

                    $ctrl.canCloseEvent = $ctrl.hasUpdatePermission
                        && $ctrl.event.isOpen()
                        && $ctrl.event.numberOfCompletedParticipants === $ctrl.event.numberOfAllParticipants;

                    $ctrl.canReopenEvent = $ctrl.hasUpdatePermission && $ctrl.event.isClosed();

                    $ctrl.isAnyLifecycleButtonVisible = $ctrl.canOpenEvent || $ctrl.canCloseEvent || $ctrl.canReopenEvent;

                    var $translate = $filter('translate');
                    $ctrl.confirmMessage = $translate('DIALOGS_CONFIRMATION_MSG');
                    $ctrl.confirmUnpopulatedWarning = $translate('shootingTest.overview.unpopulatedEventWarningMsg');
                };

                $ctrl.getCloseConfirmationMsg = function () {
                    if ($ctrl.event.numberOfAllParticipants > 0) {
                        return $ctrl.confirmMessage;
                    }

                    return "<p class='shooting-test-event-alert-box'>" +
                        "<span class='fa fa-fw fa-exclamation-triangle'></span> " +
                        "<span>" +
                        $ctrl.confirmUnpopulatedWarning +
                        "</span>" +
                        "</p><span>" +
                        $ctrl.confirmMessage +
                        "</span>";
                };

                $ctrl.closeEvent = function () {
                    ShootingTestEvent.close({eventId: $ctrl.event.shootingTestEventId}).$promise
                        .then(NotificationService.showDefaultSuccess)
                        .finally($ctrl.reload);
                };

                $ctrl.reopenEvent = function () {
                    ShootingTestEvent.reopen({eventId: $ctrl.event.shootingTestEventId}).$promise
                        .then(NotificationService.showDefaultSuccess)
                        .finally($ctrl.reload);
                };
            }
        })

        .component('showShootingTestEventOfficials', {
            templateUrl: 'shootingtest/show-shooting-test-event-officials.html',
            bindings: {
                shootingTestEventId: '<',
                officials: '<',
                canUpdateOfficials: '<',
                reload: '&'
            },
            controller: function ($uibModal, ShootingTestOfficialService) {
                var $ctrl = this;

                var sortOfficials = function (officials) {
                    return _.orderBy(officials, [function (official) {
                        return !!official.shootingTestResponsible;
                    }, 'lastName', 'firstName'], ['desc', 'asc', 'asc']);
                };

                $ctrl.$onInit = function () {
                    $ctrl.sortedOfficials = sortOfficials($ctrl.officials);
                };

                var updateOfficials = function (occupationIds, responsibleOccupationId) {
                    ShootingTestOfficialService.update($ctrl.shootingTestEventId, occupationIds, responsibleOccupationId)
                        .finally($ctrl.reload);
                };

                $ctrl.openOfficialUpdateDialog = function () {
                    if (!$ctrl.canUpdateOfficials) {
                        return;
                    }

                    var modalInstance = $uibModal.open({
                        templateUrl: 'shootingtest/shooting-test-officials.html',
                        size: 'lg',
                        controllerAs: '$ctrl',
                        controller: 'ShootingTestOfficialEditController',
                        resolve: {
                            titleLocalisationKey: _.constant('shootingTest.overview.assignOfficials'),
                            saveLocalisationKey: _.constant('global.button.save'),
                            existingOfficials: _.constant(angular.copy($ctrl.sortedOfficials)),
                            availableOfficials: function () {
                                return ShootingTestOfficialService.listQualifying($ctrl.shootingTestEventId);
                            }
                        }
                    });

                    modalInstance.result.then(function (officials) {
                        var responsible = _.find(officials, function (official) {
                            return !!official.shootingTestResponsible;
                        });

                        var occupationIds = _.map(sortOfficials(officials), 'occupationId');
                        var responsibleOccupationId = responsible ? responsible.occupationId : null;

                        updateOfficials(occupationIds, responsibleOccupationId);
                    });
                };

                $ctrl.updateResponsibleOfficial = function (responsibleOfficial) {
                    if (!$ctrl.canUpdateOfficials) {
                        return;
                    }

                    var updatedOfficials = _.chain($ctrl.sortedOfficials)
                        .map(function (official) {
                            var responsible = official.occupationId === responsibleOfficial.occupationId;
                            return angular.extend({}, official, {shootingTestResponsible: responsible});
                        })
                        .value();

                    $ctrl.sortedOfficials = sortOfficials(updatedOfficials);

                    var occupationIds = _.map($ctrl.sortedOfficials, 'occupationId');

                    updateOfficials(occupationIds, responsibleOfficial.occupationId);
                };
            }
        })

        .component('showShootingTestEventWorkflowSummary', {
            templateUrl: 'shootingtest/show-shooting-test-event-summary.html',
            bindings: {
                totalPaidAmount: '<',
                lastModifier: '<'
            }
        })

        .controller('ShootingTestOfficialEditController',
            function ($uibModalInstance, ActiveRoleService, ShootingTestEventService,
                      availableOfficials, existingOfficials, saveLocalisationKey, titleLocalisationKey) {

                var $ctrl = this;

                $ctrl.titleLocalisationKey = titleLocalisationKey;
                $ctrl.saveLocalisationKey = saveLocalisationKey;
                $ctrl.officials = existingOfficials;
                $ctrl.newOfficial = null;
                $ctrl.availableOfficials = [];

                var updateAvailableOfficials = function () {
                    $ctrl.availableOfficials = _.filter(availableOfficials, function (availableOfficial) {
                        return _.every($ctrl.officials, function (existing) {
                            return existing.personId !== availableOfficial.personId;
                        });
                    });
                };

                var addCurrentRoleToOfficials = function () {
                    if (_.isEmpty($ctrl.officials) && ActiveRoleService.isShootingTestOfficial()) {
                        var toAdd = ShootingTestEventService.findOfficialMatchingActiveOccupationId(availableOfficials);

                        if (toAdd) {
                            $ctrl.officials.push(toAdd);
                        }
                    }
                };

                $ctrl.$onInit = function () {
                    addCurrentRoleToOfficials();
                    updateAvailableOfficials();
                };

                $ctrl.getNameOfOfficial = function (o) {
                    return o.lastName + ', ' + o.firstName;
                };

                $ctrl.addOfficial = function () {
                    $ctrl.newOfficial.shootingTestResponsible = null;
                    if ($ctrl.officials.length === 0) {
                        $ctrl.newOfficial.shootingTestResponsible = true;
                    }
                    $ctrl.officials.push($ctrl.newOfficial);
                    $ctrl.newOfficial = null;
                    updateAvailableOfficials();
                };

                $ctrl.removeOfficial = function (official) {
                    _.remove($ctrl.officials, function (o) {
                        return o.personId === official.personId;
                    });
                    if (official.shootingTestResponsible && $ctrl.officials.length > 0) {
                        $ctrl.officials[0].shootingTestResponsible = true;
                    }
                    updateAvailableOfficials();
                };

                $ctrl.canSave = function () {
                    return _.size($ctrl.officials) >= 2;
                };

                $ctrl.save = function () {
                    $uibModalInstance.close($ctrl.officials);
                };

                $ctrl.cancel = function () {
                    $uibModalInstance.dismiss('cancel');
                };
            })

        .controller('ShootingTestRegistrationController', function ($state, NotificationService,
                                                                    ShootingTestParticipant, ShootingTestPersonSearch,
                                                                    TranslatedBlockUI, event, hasUpdatePermission) {
            var $ctrl = this;

            function clearState() {
                $ctrl.hunterNumber = null;
                $ctrl.error = false;
                $ctrl.searchResult = null;
                $ctrl.selectedShootingTestTypes = {};
            }

            $ctrl.$onInit = function () {
                $ctrl.event = event;
                clearState();
            };

            $ctrl.findPersonByHunterNumber = _.debounce(function () {
                TranslatedBlockUI.start("global.block.wait");

                ShootingTestPersonSearch.findByHunterNumber($ctrl.event.shootingTestEventId, $ctrl.hunterNumber)
                    .then(function (response) {
                            $ctrl.error = false;
                            $ctrl.searchResult = response.data;
                            $ctrl.selectedShootingTestTypes = $ctrl.searchResult.selectedShootingTestTypes;
                        },
                        function () {
                            $ctrl.error = true;
                            $ctrl.searchResult = null;
                            $ctrl.selectedShootingTestTypes = {};
                        })
                    .finally(TranslatedBlockUI.stop);
            }, 500);

            $ctrl.getStatusNotificationClass = function () {
                var status = _.get($ctrl, 'searchResult.registrationStatus');

                switch (status) {
                    case 'COMPLETED':
                    case 'FOREIGN_HUNTER':
                        return 'info';
                    case 'HUNTING_PAYMENT_DONE':
                        return 'success';
                    case 'IN_PROGRESS':
                        return 'warning';
                    default:
                    case 'DISQUALIFIED_AS_OFFICIAL':
                    case 'NO_HUNTER_NUMBER':
                    case 'HUNTING_BAN':
                        return 'danger';
                }
            };

            $ctrl.isRegistrationDisabled = function () {
                return !hasUpdatePermission || $ctrl.event.isClosed();
            };

            var isRegistrationDisallowedByCheckStatus = function () {
                var registrationCheckStatus = _.get($ctrl, 'searchResult.registrationStatus');

                switch (registrationCheckStatus) {
                    case 'IN_PROGRESS':
                    case 'DISQUALIFIED_AS_OFFICIAL':
                    case 'NO_HUNTER_NUMBER':
                    case 'HUNTING_BAN':
                        return true;
                    default:
                    case 'COMPLETED':
                    case 'HUNTING_PAYMENT_DONE':
                    case 'HUNTING_PAYMENT_NOT_DONE':
                    case 'FOREIGN_HUNTER':
                        return false;
                }
            };

            $ctrl.isTogglingTestTypesDisabled = function () {
                return $ctrl.isRegistrationDisabled() || isRegistrationDisallowedByCheckStatus();
            };

            $ctrl.isRegistrationDisallowed = function () {
                var isAnyTestTypeSelected = _.some($ctrl.selectedShootingTestTypes, function (isSelected) {
                    return isSelected;
                });

                return !isAnyTestTypeSelected ||
                    $ctrl.isRegistrationDisabled() ||
                    isRegistrationDisallowedByCheckStatus();
            };

            $ctrl.addParticipant = function () {
                if ($ctrl.searchResult) {
                    var urlParams = {
                        eventId: $ctrl.event.shootingTestEventId
                    };

                    var requestBody = {
                        hunterNumber: $ctrl.searchResult.hunterNumber,
                        selectedTypes: $ctrl.selectedShootingTestTypes
                    };

                    ShootingTestParticipant.register(urlParams, requestBody).$promise
                        .then(NotificationService.showDefaultSuccess, NotificationService.showDefaultFailure)
                        .finally(function () {
                            $state.reload();
                        });
                }
            };

            $ctrl.cancel = function () {
                clearState();
            };
        })

        .controller('ShootingTestParticipantsController', function ($state, calendarEventId, event,
                                                                    hasUpdatePermission, participants, rhyId) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.event = event;
                $ctrl.participants = participants;
                $ctrl.hasUpdatePermission = hasUpdatePermission;

                var numberOfParticipantsWithNoAttempts = _.size(_.filter($ctrl.participants, function (participant) {
                    return participant.attempts.length === 0;
                }));

                $ctrl.havingAttemptsBeginIndex = numberOfParticipantsWithNoAttempts === 0 ? -1 : numberOfParticipantsWithNoAttempts;
            };

            $ctrl.viewParticipant = function (participant) {
                $state.go('rhy.shootingtest.event.participant', {
                    rhyId: rhyId,
                    calendarEventId: calendarEventId,
                    participantId: participant.id
                });
            };
        })

        .controller('ShootingTestAttemptsController', function ($state, $uibModal, NotificationService,
                                                                ShootingTestAttempt, ShootingTestParticipant,
                                                                calendarEventId, event, hasUpdatePermission,
                                                                participant, rhyId) {
            var $ctrl = this;
            var reload = function () {
                $state.reload();
            };

            $ctrl.participant = participant;

            $ctrl.canEditAttempts = function () {
                return hasUpdatePermission && !$ctrl.participant.completed && event.isOpen();
            };

            var openFormDialog = function (getAttempt) {
                ShootingTestParticipant.get({participantId: participant.id}).$promise
                    .then(function (reloadedParticipant) {
                        $uibModal.open({
                            templateUrl: 'shootingtest/participant-attempt-form.html',
                            controllerAs: '$ctrl',
                            bindToController: true,
                            size: 'lg',
                            controller: 'ShootingTestAttemptFormController',
                            resolve: {
                                disabledTestTypes: function () {
                                    return _.chain(reloadedParticipant.attempts)
                                        .filter(function (attempt) {
                                            return attempt.attemptCount >= 5;
                                        })
                                        .map('type')
                                        .value();
                                },
                                attempt: function () {
                                    return getAttempt(reloadedParticipant);
                                }
                            }
                        }).result.then(reload);
                    });
            };

            $ctrl.addAttempt = function () {
                openFormDialog(function (participant) {
                    return {
                        participantId: participant.id,
                        participantRev: participant.rev,
                        type: null,
                        result: null,
                        hits: null,
                        note: null
                    };
                });
            };

            $ctrl.editAttempt = function (attempt) {
                var id = attempt.id;

                function getAttempt(participant) {
                    return ShootingTestAttempt.get({attemptId: id}).$promise
                        .then(function (reloadedAttempt) {
                            return {
                                id: id,
                                rev: reloadedAttempt.rev,
                                participantId: participant.id,
                                participantRev: participant.rev,
                                type: reloadedAttempt.type,
                                result: reloadedAttempt.result,
                                hits: reloadedAttempt.hits,
                                note: reloadedAttempt.note
                            };
                        });
                }

                openFormDialog(getAttempt);
            };

            $ctrl.removeAttempt = function (attempt) {
                function showFailureMessage() {
                    NotificationService.flashMessage('shootingTest.attempt.paidAttemptsCannotBeRemoved', 'error');
                }

                ShootingTestAttempt.remove({attemptId: attempt.id}).$promise
                    .then(NotificationService.showDefaultSuccess, showFailureMessage)
                    .finally(reload);
            };

            $ctrl.done = function () {
                $state.go('rhy.shootingtest.event.participants', {
                    rhyId: rhyId,
                    calendarEventId: calendarEventId
                });
            };
        })

        .controller('ShootingTestAttemptFormController', function ($translate, $uibModalInstance, ShootingTestAttempt,
                                                                   attempt, disabledTestTypes) {

            var $ctrl = this;

            $ctrl.attempt = attempt;
            $ctrl.listTypes = ['BEAR', 'MOOSE', 'ROE_DEER', 'BOW'];
            $ctrl.listHits = [4, 3, 2, 1, 0];

            function isQualified() {
                var hits = $ctrl.attempt.hits;
                return hits === 4 || $ctrl.attempt.type === 'BOW' && hits === 3;
            }

            function updateResult() {
                if (!_.isFinite($ctrl.attempt.type) && !_.isFinite($ctrl.attempt.hits)) {
                    $ctrl.attempt.result = null;
                } else {
                    $ctrl.attempt.result = isQualified() ? 'QUALIFIED' : 'UNQUALIFIED';
                }
            }

            $ctrl.typeChanged = function () {
                if ($ctrl.attempt.type === 'BOW' && $ctrl.attempt.hits === 4) {
                    $ctrl.attempt.hits = null;
                }
                updateResult();
            };

            $ctrl.hitsChanged = function () {
                updateResult();
            };

            $ctrl.isTypeDisabled = function (type) {
                return _.includes(disabledTestTypes, type);
            };

            $ctrl.isHitsDisabled = function (hits) {
                var type = $ctrl.attempt.type;
                return !type || type === 'BOW' && hits === 4;
            };

            $ctrl.translateResult = function (result) {
                return result ? $translate.instant('shootingTest.result.' + result) : '';
            };

            $ctrl.selectableResults = function () {
                if (!$ctrl.attempt.result) {
                    return [];
                }
                return _.uniq([isQualified() ? 'QUALIFIED' : 'UNQUALIFIED', $ctrl.attempt.result, 'REBATED', 'TIMED_OUT']);
            };

            $ctrl.showNote = function () {
                return $ctrl.attempt.result === 'REBATED';
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };

            $ctrl.save = function () {
                var attempt = $ctrl.attempt;
                var persistInvocation;

                if (attempt.type && _.isFinite(attempt.hits)) {
                    if (_.isFinite(attempt.id)) {
                        persistInvocation = ShootingTestAttempt.update({attemptId: attempt.id}, attempt);
                    } else {
                        persistInvocation = ShootingTestAttempt.create({participantId: attempt.participantId}, attempt);
                    }

                    persistInvocation.$promise.then($uibModalInstance.close);
                }
            };
        })

        .controller('ShootingTestPaymentsController', function ($state, $translate, $uibModal, NotificationService,
                                                                ShootingTestParticipant, event, hasUpdatePermission,
                                                                participants, ShootingTestCalendarEventsService) {
            var $ctrl = this;

            var numberOfUncompleted = event.numberOfAllParticipants - event.numberOfCompletedParticipants;
            $ctrl.completedBeginIndex = numberOfUncompleted === 0 ? -1 : numberOfUncompleted;

            $ctrl.event = event;
            $ctrl.participants = participants;
            $ctrl.hasUpdatePermission = hasUpdatePermission;

            function showSuccessMessage(localisationKey, participant) {
                var translation = $translate.instant(localisationKey, {
                    'firstName': participant.firstName,
                    'lastName': participant.lastName,
                    'hunterNumber': participant.hunterNumber
                });
                NotificationService.flashMessage(translation, 'success', {ttl: -1});
            }

            $ctrl.hasIncompletePayment = function (participant) {
                return participant.completed && participant.paidAmount < participant.totalDueAmount;
            };

            $ctrl.getCompleteButtonClass = function (participant) {
                if (!participant.completed) {
                    return 'btn-default';
                }
                return $ctrl.hasIncompletePayment(participant) ? 'r-btn-incomplete-payment' : 'btn-primary';
            };

            $ctrl.completePayment = function (participant) {
                var id = participant.id;
                var urlParams = {participantId: id};
                var requestBody = {id: id, rev: participant.rev};

                var onSuccess = function () {
                    showSuccessMessage('shootingTest.payment.messages.completeSuccess', participant);
                };

                ShootingTestParticipant.completePayment(urlParams, requestBody).$promise
                    .then(onSuccess, NotificationService.showDefaultFailure)
                    .finally(function () {
                        $state.reload();
                    });
            };

            $ctrl.updatePayment = function (participant) {
                $uibModal.open({
                    templateUrl: 'shootingtest/update-payment-form.html',
                    controller: 'ShootingTestUpdatePaymentFormController',
                    controllerAs: '$ctrl',
                    bindToController: true,
                    size: 'lg',
                    resolve: {
                        participant: function () {
                            return ShootingTestParticipant.get({participantId: participant.id}).$promise;
                        },
                        onSuccessCallback: _.constant(function () {
                            showSuccessMessage('shootingTest.payment.messages.updateSuccess', participant);
                        }),
                        onFailureCallback: _.constant(NotificationService.showDefaultFailure)
                    }
                }).result.then(function () {
                    $state.reload();
                });
            };

            $ctrl.loadPaymentSummaryPdf = function () {
                ShootingTestCalendarEventsService.loadPaymentSummaryPdf($ctrl.event.calendarEventId);
            };
        })

        .controller('ShootingTestUpdatePaymentFormController', function ($uibModalInstance,
                                                                         SHOOTING_TEST_ATTEMPT_PRICE,
                                                                         ShootingTestParticipant, onFailureCallback,
                                                                         onSuccessCallback, participant) {
            var $ctrl = this;
            $ctrl.participant = participant;

            var totalAttempts = participant.totalDueAmount / SHOOTING_TEST_ATTEMPT_PRICE;

            $ctrl.paidAttempts = participant.paidAmount / SHOOTING_TEST_ATTEMPT_PRICE;
            $ctrl.completed = participant.completed;

            $ctrl.paidAmountOptions = _.chain(participant.attempts)
                .map('attemptCount')
                .sum()
                .add(1)
                .range()
                .map(function (attemptNumber) {
                    return {
                        attempts: attemptNumber,
                        amount: attemptNumber * SHOOTING_TEST_ATTEMPT_PRICE
                    };
                })
                .value();

            $ctrl.calculateRemainingAmount = function () {
                return participant.totalDueAmount - $ctrl.paidAttempts * SHOOTING_TEST_ATTEMPT_PRICE;
            };

            $ctrl.paidAttemptsChanged = function () {
                $ctrl.completed = $ctrl.paidAttempts === totalAttempts;
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };

            $ctrl.save = function () {
                var urlParams = {participantId: participant.id};
                var requestBody = {
                    id: participant.id,
                    rev: participant.rev,
                    paidAttempts: $ctrl.paidAttempts,
                    completed: $ctrl.completed
                };

                ShootingTestParticipant.updatePayment(urlParams, requestBody).$promise
                    .then(onSuccessCallback, onFailureCallback)
                    .finally($uibModalInstance.close);
            };
        })

        .component('shootingTestPaymentSummary', {
            templateUrl: 'shootingtest/shooting-test-payment-summary.html',
            bindings: {
                totalPaidAmount: '<'
            }
        })

        .controller('ShootingTestStatsController', function (availableYears, exportToExcel, fetchStatistics,
                                                             initialStatistics, ShootingTestCalendarEventsService,
                                                             ActiveRoleService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.availableYears = availableYears;
                $ctrl.calendarYear = _.last(availableYears);
                $ctrl.statistics = initialStatistics;
                $ctrl.isModeratorOrCoordinator = ActiveRoleService.isModerator() || ActiveRoleService.isCoordinator();
            };

            $ctrl.getResponsibleOfficialName = function (officials) {
                var responsibleOfficial = _.find(officials, function (official) {
                    return official.shootingTestResponsible === true;
                });

                var responsibleOfficialName = null;
                if (responsibleOfficial) {
                    responsibleOfficialName = responsibleOfficial.lastName + ' ' + responsibleOfficial.firstName;
                }

                return responsibleOfficialName;
            };

            $ctrl.joinNonResponsibleOfficials = function (officials) {
                return _.chain(officials)
                    .filter(function (official) {
                        return official.shootingTestResponsible !== true;
                    })
                    .map(function (official) {
                        return official.lastName + ' ' + official.firstName;
                    })
                    .join(', ')
                    .value();
            };

            $ctrl.onSelectedYearChanged = function () {
                fetchStatistics($ctrl.calendarYear).then(function (statistics) {
                    $ctrl.statistics = statistics;
                });
            };

            $ctrl.exportToExcel = function () {
                exportToExcel($ctrl.calendarYear);
            };

            $ctrl.loadPaymentSummaryPdf = function (calendarEventId) {
                ShootingTestCalendarEventsService.loadPaymentSummaryPdf(calendarEventId);
            };
        })

        .component('shootingTestStatisticsTable', {
            templateUrl: 'shootingtest/stats-table.html',
            bindings: {
                statistic: '<'
            }
        })
    ;
})();
