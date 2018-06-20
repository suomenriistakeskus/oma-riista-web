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
                    controller: function (event) {
                        this.event = event;
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

                                    return _.sortByAll(participants, sortCriterias);
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

                                    var completed = _.sortByAll(partitionByCompleted[0], ['lastName', 'firstName', 'hunterNumber']);

                                    var attemptExistenceSort = function (participant) {
                                        return participant.attempts.length === 0 ? 0 : 1;
                                    };
                                    var uncompletedSortCriteria = [attemptExistenceSort, 'registrationTime', 'lastName', 'firstName', 'hunterNumber'];
                                    var uncompleted = _.sortByAll(partitionByCompleted[1], uncompletedSortCriteria);

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

            var repository = $resource('/api/v1/shootingtest/:rhyId/calendarevents', {'rhyId': '@rhyId'}, {
                'query': {
                    method: 'GET',
                    isArray: true
                },
                'get': {
                    method: 'GET',
                    url: '/api/v1/shootingtest/calendarevent/:calendarEventId',
                    params: {calendarEventId: '@calendarEventId'}
                },
                'open': {
                    method: 'POST',
                    url: '/api/v1/shootingtest/calendarevent/:calendarEventId/open',
                    params: {calendarEventId: '@calendarEventId'}
                }
            });

            return decorateRepository(repository);
        })

        .factory('ShootingTestEvents', function ($resource) {
            return $resource('/api/v1/shootingtest/:rhyId/events', {'rhyId': '@rhyId'}, {
                'close': {
                    method: 'POST',
                    url: '/api/v1/shootingtest/event/:eventId/close',
                    params: {eventId: '@eventId'}
                },
                'reopen': {
                    method: 'POST',
                    url: '/api/v1/shootingtest/event/:eventId/reopen',
                    params: {eventId: '@eventId'}
                }
            });
        })

        .factory('ShootingTestStatistics', function ($resource) {
            return $resource('/api/v1/shootingtest/:rhyId/statistics/:calendarYear', {
                'rhyId': '@rhyId',
                'calendarYear': '@calendarYear'
            }, {
                'get': {
                    method: 'GET'
                }
            });
        })

        .factory('ShootingTestOfficials', function ($resource) {
            return $resource('/api/v1/shootingtest/:rhyId/officials', {'rhyId': '@rhyId'}, {
                'listAvailable': {
                    method: 'GET',
                    isArray: true
                },
                'listQualifying': {
                    method: 'GET',
                    url: '/api/v1/shootingtest/event/:shootingTestEventId/qualifyingofficials',
                    params: {
                        shootingTestEventId: '@shootingTestEventId'
                    },
                    isArray: true
                },
                'listAssigned': {
                    method: 'GET',
                    url: '/api/v1/shootingtest/event/:shootingTestEventId/assignedofficials',
                    params: {
                        shootingTestEventId: '@shootingTestEventId'
                    },
                    isArray: true
                },
                'update': {
                    method: 'PUT',
                    url: '/api/v1/shootingtest/event/:shootingTestEventId/officials',
                    params: {
                        shootingTestEventId: '@shootingTestEventId'
                    }
                }
            });
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
                    url: '/api/v1/shootingtest/event/:eventId/participant/person/:personId',
                    params: {eventId: '@eventId', personId: '@personId'}
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
        })

        .component('shootingTestTabSelection', {
            templateUrl: 'shootingtest/tab-selection.html',
            bindings: {
                event: '<'
            },
            controller: function ($state) {
                var $ctrl = this;
                var states = [ '^.overview', '^.registration', '^.participants', '^.payments' ];

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
            }
        })

        .controller('ShootingTestEventOverviewController',
            function ($state, $uibModal, Helpers, NotificationService, ShootingTestCalendarEvents, ShootingTestEvents,
                      ShootingTestOfficials, event, hasUpdatePermission, rhyId) {

                var calendarEventId = event.calendarEventId;
                var eventId = event.shootingTestEventId;

                var $ctrl = this;
                $ctrl.event = event;

                var onSuccess = function () {
                    NotificationService.showDefaultSuccess();
                };
                var onFailure = function () {
                    NotificationService.showDefaultFailure();
                };
                var reload = function () {
                    $state.reload();
                };

                $ctrl.canOpenEvent = function () {
                    var eventDate = Helpers.toMoment($ctrl.event.date);
                    var today = Helpers.toMoment(new Date());

                    return !$ctrl.event.isOpened() && !eventDate.isAfter(today, 'day');
                };

                $ctrl.canCloseEvent = function () {
                    return $ctrl.event.isOpen()
                        && $ctrl.event.numberOfCompletedParticipants === $ctrl.event.numberOfAllParticipants
                        && hasUpdatePermission;
                };

                $ctrl.canReopenEvent = function () {
                    return hasUpdatePermission && $ctrl.event.isClosed();
                };

                $ctrl.isAnyLifecycleButtonVisible = function () {
                    return $ctrl.canOpenEvent() || $ctrl.canCloseEvent() || $ctrl.canReopenEvent();
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
                            existingOfficials: _.constant(angular.copy($ctrl.event.officials)),
                            availableOfficials: function () {
                                return ShootingTestOfficials.listAvailable({rhyId: rhyId}).$promise;
                            }
                        }
                    });

                    modalInstance.result.then(function (officials) {
                        var params = {calendarEventId: calendarEventId};
                        var e = {
                            calendarEventId: calendarEventId,
                            occupationIds: _.map(officials, 'occupationId')
                        };
                        ShootingTestCalendarEvents.open(params, e).$promise.then(onSuccess, onFailure).finally(reload);
                    });
                };

                $ctrl.closeEvent = function () {
                    ShootingTestEvents.close({eventId: eventId}).$promise
                        .then(onSuccess, onFailure)
                        .finally(reload);
                };

                $ctrl.reopenEvent = function () {
                    ShootingTestEvents.reopen({eventId: eventId}).$promise
                        .then(onSuccess, onFailure)
                        .finally(reload);
                };

                $ctrl.canAssignOfficials = function () {
                    return $ctrl.event.isOpen();
                };

                $ctrl.assignOfficials = function () {
                    var modalInstance = $uibModal.open({
                        templateUrl: 'shootingtest/shooting-test-officials.html',
                        size: 'lg',
                        controllerAs: '$ctrl',
                        controller: 'ShootingTestOfficialEditController',
                        resolve: {
                            titleLocalisationKey: _.constant('shootingTest.overview.assignOfficials'),
                            saveLocalisationKey: _.constant('global.button.save'),
                            existingOfficials: _.constant(angular.copy($ctrl.event.officials)),
                            availableOfficials: function () {
                                return ShootingTestOfficials.listQualifying({shootingTestEventId: eventId}).$promise;
                            }
                        }
                    });

                    modalInstance.result.then(function (officials) {
                        var params = {calendarEventId: calendarEventId};
                        var e = {
                            shootingTestEventId: eventId,
                            occupationIds: _.map(officials, 'occupationId')
                        };

                        ShootingTestOfficials.update(params, e).$promise
                            .then(onSuccess, onFailure)
                            .finally(reload);
                    });
                };
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
                        return !_.find($ctrl.officials, function (existing) {
                            return existing.personId === availableOfficial.personId;
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
                    $ctrl.officials.push($ctrl.newOfficial);
                    $ctrl.newOfficial = null;
                    updateAvailableOfficials();
                };

                $ctrl.removeOfficial = function (official) {
                    _.remove($ctrl.officials, function (o) {
                        return o.personId === official.personId;
                    });
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
            }, 500, true);

            $ctrl.getStatusNotificationClass = function () {
                var status = _.get($ctrl, 'searchResult.registrationStatus');

                switch (status) {
                    case 'COMPLETED':
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

            $ctrl.isRegistrationDisallowed = function () {
                if (!hasUpdatePermission || $ctrl.event.isClosed()) {
                    return true;
                }

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
                        return false;
                }
            };

            $ctrl.addParticipant = function () {
                if ($ctrl.searchResult) {
                    var urlParams = {
                        eventId: $ctrl.event.shootingTestEventId,
                        personId: $ctrl.searchResult.id
                    };

                    ShootingTestParticipant.register(urlParams, $ctrl.selectedShootingTestTypes).$promise
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
                return _.unique([isQualified() ? 'QUALIFIED' : 'UNQUALIFIED', $ctrl.attempt.result, 'REBATED', 'TIMED_OUT']);
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
                                                                participants) {
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
                .pluck('attemptCount')
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
                                                             initialStatistics) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.availableYears = availableYears;
                $ctrl.calendarYear = _.last(availableYears);
                $ctrl.statistics = initialStatistics;
            };

            $ctrl.joinOfficials = function (officials) {
                return _(officials).map(function (o) {
                    return o.lastName + ' ' + o.firstName;
                }).join(', ');
            };

            $ctrl.onSelectedYearChanged = function () {
                fetchStatistics($ctrl.calendarYear).then(function (statistics) {
                    $ctrl.statistics = statistics;
                });
            };

            $ctrl.exportToExcel = function () {
                exportToExcel($ctrl.calendarYear);
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
