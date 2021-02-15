(function () {
    "use strict";

    angular.module('app.rhy.annualstats', [])
        .config(function ($stateProvider) {
            $stateProvider
                .state('rhy.annualstats', {
                    url: '/annualstatistics?{year:20[0-9]{2}}&activeTab',
                    reloadOnSearch: false,
                    params: {
                        year: null,
                        activeTab: null
                    },
                    templateUrl: 'rhy/annualstats/layout.html',
                    controller: 'RhyAnnualStatisticsController',
                    controllerAs: '$ctrl',
                    wideLayout: false,
                    resolve: {
                        rhy: function (RhyAnnualStatisticsViewState, Rhys, rhyId) {
                            if (_.isFinite(rhyId)) {
                                RhyAnnualStatisticsViewState.setRhyId(rhyId);
                            }

                            return Rhys.get({id: rhyId}).$promise;
                        },
                        availableYears: function (AnnualStatisticsYears, rhyId) {
                            return AnnualStatisticsYears.get({rhyId: rhyId}).$promise;
                        },
                        year: function ($stateParams, RhyAnnualStatisticsViewState, availableYears) {
                            var yearParam = $stateParams.year;
                            var year;

                            if (angular.isString(yearParam)) {
                                year = _.parseInt(yearParam);

                                if (!_.includes(availableYears, year)) {
                                    year = _.last(availableYears);
                                }
                            } else {
                                year = _.last(availableYears);
                            }

                            RhyAnnualStatisticsViewState.setYear(year);
                            return year;
                        },
                        statistics: function (RhyAnnualStatisticsService, year) {
                            return RhyAnnualStatisticsService.getStatistics(year);
                        },
                        activeTab: function ($stateParams, RhyAnnualStatisticsViewState) {
                            var activeTabParam = $stateParams.activeTab;
                            var activeTab = null;

                            if (angular.isString(activeTabParam)) {
                                var parsedTab = _.parseInt(activeTabParam);

                                if (_.isFinite(parsedTab) && parsedTab >= 0 && parsedTab <= 4) {
                                    activeTab = parsedTab;
                                } else {
                                    activeTab = 0;
                                }
                                RhyAnnualStatisticsViewState.setActiveTab(activeTab);

                            } else {
                                activeTab = RhyAnnualStatisticsViewState.getActiveTab();
                            }

                            return activeTab;
                        },
                        allSpeciesNames: function (GameDiaryParameters) {
                            return GameDiaryParameters.query().$promise.then(function (params) {
                                var ret = {};
                                _.forEach(params.species, function (species) {
                                    ret[species.code] = species.name;
                                });

                                return ret;
                            });
                        },
                        permission: function (ActiveRoleService, AvailableRoleService, ModeratorPrivileges,
                                              RhyAnnualStatisticsPermission) {

                            if (ActiveRoleService.isAdmin()) {
                                return RhyAnnualStatisticsPermission.MODERATE;

                            } else if (ActiveRoleService.isModerator()) {
                                if (AvailableRoleService.hasPrivilege(ModeratorPrivileges.moderateRhyAnnualStatistics)) {
                                    return RhyAnnualStatisticsPermission.MODERATE;
                                } else {
                                    return RhyAnnualStatisticsPermission.VIEW;
                                }
                            }

                            return RhyAnnualStatisticsPermission.EDIT;
                        },
                        isModerator: function (RhyAnnualStatisticsPermission, permission) {
                            return permission === RhyAnnualStatisticsPermission.MODERATE;
                        }
                    }
                });
        })

        .constant('RhyAnnualStatisticsState', {
            NOT_STARTED: 'NOT_STARTED',
            IN_PROGRESS: 'IN_PROGRESS',
            UNDER_INSPECTION: 'UNDER_INSPECTION',
            APPROVED: 'APPROVED'
        })

        .service('RhyAnnualStatisticsStates', function (RhyAnnualStatisticsState) {
            var self = this;
            var states = [
                RhyAnnualStatisticsState.NOT_STARTED,
                RhyAnnualStatisticsState.IN_PROGRESS,
                RhyAnnualStatisticsState.UNDER_INSPECTION,
                RhyAnnualStatisticsState.APPROVED
            ];

            self.list = function () {
                // Clone
                return states.slice();
            };

            self.isCompletedByCoordinator = function (state) {
                return state !== RhyAnnualStatisticsState.NOT_STARTED &&
                       state !== RhyAnnualStatisticsState.IN_PROGRESS;
            };

            self.indexOf = function (state) {
                return _.indexOf(states, state);
            };

            self.get = function (index) {
                if (index >= 0 && index < states.length) {
                    return states[index];
                }
                return null;
            };
        })

        .constant('RhyAnnualStatisticsPermission', {
            VIEW: 'VIEW',
            EDIT: 'EDIT',
            MODERATE: 'MODERATE'
        })

        .constant('AnnualStatisticsLastAvailableYear', {
            LAST_YEAR: 2020
        })

        .factory('AnnualStatisticsYears', function ($resource) {
            return $resource('/api/v1/riistanhoitoyhdistys/:rhyId/annualstatisticsyears', {rhyId: '@rhyId'}, {
                'get': {method: 'GET', isArray: true}
            });
        })

        .service('RhyAnnualStatisticsViewState', function ($state) {
            var self = this;

            self.rhyId = null;
            self.year = null;
            self.activeTab = 0;

            this.getRhyId = function () {
                return self.rhyId;
            };

            this.setRhyId = function (rhyId) {
                self.rhyId = rhyId;
            };

            this.getYear = function () {
                return self.year;
            };

            this.setYear = function (year) {
                self.year = year;
            };

            this.getActiveTab = function () {
                return self.activeTab;
            };

            this.setActiveTab = function (activeTab) {
                self.activeTab = activeTab;
            };

            this.reload = function () {
                $state.go($state.current, {year: self.year, activeTab: self.activeTab}, {reload: true});
            };
        })

        .factory('RhyAnnualStatistics', function ($resource) {
            var getParams = {
                'rhyId': '@rhyId',
                'calendarYear': '@calendarYear'
            };

            return $resource('/api/v1/riistanhoitoyhdistys/:rhyId/annualstatistics/:calendarYear', getParams, {
                getOrCreate: {method: 'GET'},
                submitForInspection: {
                    method: 'POST',
                    url: '/api/v1/riistanhoitoyhdistys/annualstatistics/:statisticsId/submitforinspection',
                    params: {statisticsId: '@statisticsId'}
                },
                approve: {
                    method: 'POST',
                    url: '/api/v1/riistanhoitoyhdistys/annualstatistics/:statisticsId/approve',
                    params: {statisticsId: '@statisticsId'}
                },
                cancelApproval: {
                    method: 'POST',
                    url: '/api/v1/riistanhoitoyhdistys/annualstatistics/:statisticsId/cancelapproval',
                    params: {statisticsId: '@statisticsId'}
                },
                update: {
                    method: 'PUT',
                    url: '/api/v1/riistanhoitoyhdistys/annualstatistics/:statisticsId/:urlPostfix',
                    params: {
                        statisticsId: '@statisticsId',
                        urlPostfix: '@urlPostfix'
                    }
                }
            });
        })

        .service('RhyAnnualStatisticsService', function (NotificationService, RhyAnnualStatistics,
                                                         RhyAnnualStatisticsPermission, RhyAnnualStatisticsState,
                                                         RhyAnnualStatisticsViewState) {
            var self = this;

            this.getStatistics = function (year) {
                var rhyId = RhyAnnualStatisticsViewState.getRhyId();
                return RhyAnnualStatistics.getOrCreate({rhyId: rhyId, calendarYear: year}).$promise;
            };

            this.reloadStatistics = function () {
                return self.getStatistics(RhyAnnualStatisticsViewState.getYear());
            };

            this.isStatisticsEditable = function (state, year, permission) {
                if (permission === RhyAnnualStatisticsPermission.VIEW ||
                    state === RhyAnnualStatisticsState.APPROVED) {

                    return false;

                } else if (permission === RhyAnnualStatisticsPermission.MODERATE) {
                    return true;

                } else if (permission === RhyAnnualStatisticsPermission.EDIT &&
                           state !== RhyAnnualStatisticsState.UNDER_INSPECTION) {

                    // Editing is blocked from coordinator after 15.1. next year.

                    var coordinatorEditableEndDate = moment().year(year + 1).month(0).date(15);

                    if (coordinatorEditableEndDate.isSameOrAfter(new Date(), 'day')) {
                        return true;
                    }
                }

                return false;
            };

            this.submitStatisticsForInspection = function (statistics) {
                return RhyAnnualStatistics.submitForInspection({statisticsId: statistics.id}, {
                    id: statistics.id,
                    rev: statistics.rev
                }).$promise.then(function (response) {
                    NotificationService.flashMessage('rhy.annualStats.message.submitSucceeded', 'success');
                    return response;
                });
            };

            this.approve = function (statistics) {
                return RhyAnnualStatistics.approve({statisticsId: statistics.id}, {
                    id: statistics.id,
                    rev: statistics.rev
                }).$promise.then(function (response) {
                    NotificationService.flashMessage('global.messages.success', 'success');
                    return response;
                });
            };

            this.cancelApproval = function (statistics) {
                return RhyAnnualStatistics.cancelApproval({statisticsId: statistics.id}, {
                    id: statistics.id,
                    rev: statistics.rev
                }).$promise.then(function (response) {
                    NotificationService.flashMessage('rhy.annualStats.message.approvalCancelled', 'success');
                    return response;
                });
            };
        })

        .service('RhyAnnualStatisticsDialogHelper', function ($uibModal, NotificationService,
                                                              RhyAnnualStatisticsViewState) {

            this.createOpenEditDialogFunction = function (data, templateUrl, saveDataFn) {

                function showModal(data, modalCtrlOpts) {
                    var modalPromise = $uibModal.open({
                        templateUrl: templateUrl,
                        controllerAs: '$ctrl',
                        controller: ModalController,
                        resolve: {
                            data: _.constant(data),
                            modalCtrlOpts: _.constant(modalCtrlOpts)
                        }
                    }).result.then(saveDataFn);

                    NotificationService.handleModalPromise(modalPromise).then(function () {
                        RhyAnnualStatisticsViewState.reload();
                    });
                }

                function ModalController($uibModalInstance, data, modalCtrlOpts) {
                    var $modalCtrl = this;
                    angular.extend($modalCtrl, modalCtrlOpts);
                    $modalCtrl.data = data || {};

                    $modalCtrl.save = function () {
                        $uibModalInstance.close($modalCtrl.data);
                    };

                    $modalCtrl.cancel = function () {
                        $uibModalInstance.dismiss('cancel');
                    };
                }

                return function (modalCtrlOpts) {
                    showModal(data, modalCtrlOpts);
                };
            };
        })

        .service('Tuple2ListFormDialog', function (RhyAnnualStatisticsDialogHelper) {
            /*
             * The following localisation keys need to exist with the given prefix:
             *  - title
             *  - input1Key
             *  - input2Key
             */
            this.create = function (data, localisationPrefix, updateFn) {
                var upperLimit = 99999;

                var defaults = {
                    input1UnitKey: 'global.pcs',
                    input2UnitKey: 'global.pcs',
                    getValue1Min: function (group) {
                        return 0;
                    },
                    getValue1Max: function (group) {
                        return upperLimit;
                    },
                    getValue2Min: function (group) {
                        return 0;
                    },
                    getValue2Max: function (group) {
                        return upperLimit;
                    }
                };

                function createModalCtrlOpts (groups, modalCtrlOpts) {
                    var opts = angular.copy(defaults);

                    if (angular.isObject(modalCtrlOpts)) {
                        angular.extend(opts, modalCtrlOpts);
                    }

                    angular.extend(opts, {
                        groups: groups,
                        titleKey: localisationPrefix + 'title',
                        header0: localisationPrefix + 'header0',
                        header1: localisationPrefix + 'header1',
                        header2: localisationPrefix + 'header2',
                        getGroupTitle: function (group) {
                            return localisationPrefix + group.name + 'Title';
                        }
                    });

                    return opts;
                }

                var openEditDialogFn = RhyAnnualStatisticsDialogHelper.createOpenEditDialogFunction(
                    data, 'rhy/annualstats/tuple2-list-form.html', updateFn);

                return {
                    /*
                     * Each group must have the following properties:
                     *  - name
                     *  - input1Key
                     *  - input2Key
                     *
                     * The following properties are supported in 'modalCtrlOpts' parameter:
                     *  - input1UnitKey (localisation key for unit of the first value)
                     *  - input2UnitKey (localisation key for unit of the second value)
                     *
                     * Additionally, 'modalCtrlOtps' may contain the following functions overriding the default implementations:
                     *  - getValue1Min(group)
                     *  - getValue1Max(group)
                     *  - getValue2Min(group)
                     *  - getValue2Max(group)
                     */
                    open: function (groups, modalCtrlOpts) {
                        return openEditDialogFn(createModalCtrlOpts(groups, modalCtrlOpts));
                    }
                };
            };
        })

        .service('RhyAnnualStatisticsEditDialog', function (RhyAnnualStatistics, RhyAnnualStatisticsDialogHelper,
                                                            Tuple2ListFormDialog) {

            var update = function (statisticsId, urlPostfix, data) {
                var pathParams = {statisticsId: statisticsId, urlPostfix: urlPostfix};

                return RhyAnnualStatistics.update(pathParams, data).$promise;
            };

            this.openBasicInfo = function (statistics) {
                var updateFn = function (data) {
                    delete data.rhyMembers;
                    return update(statistics.id, 'basicinfo', data);
                };

                return RhyAnnualStatisticsDialogHelper.createOpenEditDialogFunction(
                    statistics.basicInfo, 'rhy/annualstats/overview/edit-rhy-basic-info.html', updateFn);
            };

            this.openHunterExams = function (statistics, isModerator) {
                var updateFn = function (data) {
                    var urlPostfix;

                    if (isModerator) {
                        urlPostfix = 'moderatedhunterexams';
                    } else {
                        urlPostfix = 'hunterexams';
                        delete data.moderatorOverriddenHunterExamEvents;
                    }

                    delete data.hunterExamEvents;
                    delete data.hunterExamOfficials;

                    return update(statistics.id, urlPostfix, data);
                };

                var openDialog = RhyAnnualStatisticsDialogHelper.createOpenEditDialogFunction(
                    statistics.hunterExams, 'rhy/annualstats/administration/edit-hunter-exams.html', updateFn);

                return function () {
                    return openDialog({
                        isModerator: isModerator,
                        hunterExamEventsOverridden: _.isFinite(statistics.hunterExams.moderatorOverriddenHunterExamEvents)
                    });
                };
            };

            this.openShootingTestsModeratorOverride = function (statistics, isModerator) {
                var updateFn = function (data) {
                    var urlPostfix = 'moderatedshootingtests';

                    delete data.allShootingTestEvents;
                    delete data.firearmTestEvents;
                    delete data.bowTestEvents;
                    delete data.shootingTestOfficials;

                    return update(statistics.id, urlPostfix, data);
                };

                var openDialog = RhyAnnualStatisticsDialogHelper.createOpenEditDialogFunction(
                    statistics.shootingTests, 'rhy/annualstats/administration/edit-shooting-tests.html', updateFn);

                var modalCtrlOpts = {
                    firearmTestEventsOverridden: _.isFinite(statistics.shootingTests.moderatorOverriddenFirearmTestEvents),
                    bowTestEventsOverridden: _.isFinite(statistics.shootingTests.moderatorOverriddenBowTestEvents),
                };

                return function () {
                    return openDialog(modalCtrlOpts);
                };
            };

            this.openHuntingControl = function (statistics) {
                var updateFn = function (data) {
                    delete data.huntingControllers;
                    return update(statistics.id, 'huntingcontrol', data);
                };

                return RhyAnnualStatisticsDialogHelper.createOpenEditDialogFunction(
                    statistics.huntingControl, 'rhy/annualstats/administration/edit-hunting-control.html', updateFn);
            };

            this.openGameDamage = function (statistics) {
                var updateFn = function (data) {
                    delete data.gameDamageInspectors;
                    delete data.totalDamageInspectionLocations;
                    delete data.totalDamageInspectionExpenses;

                    return update(statistics.id, 'gamedamage', data);
                };

                return RhyAnnualStatisticsDialogHelper.createOpenEditDialogFunction(
                    statistics.gameDamage, 'rhy/annualstats/administration/edit-game-damage.html', updateFn);
            };

            this.openOtherPublicAdmin = function (statistics) {
                var updateFn = function (data) {
                    return update(statistics.id, 'otherpublicadmin', data);
                };

                return RhyAnnualStatisticsDialogHelper.createOpenEditDialogFunction(
                    statistics.otherPublicAdmin, 'rhy/annualstats/administration/edit-other-admin-data.html', updateFn);
            };

            this.openHunterExamTraining = function (statistics, isModerator) {
                var updateFn = function (data) {
                    var urlPostfix;

                    if (isModerator) {
                        urlPostfix = 'moderatedhunterexamtraining';
                    } else {
                        urlPostfix = 'hunterexamtraining';
                        delete data.moderatorOverriddenHunterExamTrainingEvents;
                    }

                    delete data.hunterExamTrainingEvents;

                    return update(statistics.id, urlPostfix, data);
                };

                var openDialog = RhyAnnualStatisticsDialogHelper.createOpenEditDialogFunction(
                    statistics.hunterExamTraining, 'rhy/annualstats/trainings/edit-hunter-exam-training.html', updateFn);

                return function () {
                    return openDialog({
                        isModerator: isModerator,
                        hunterExamTrainingEventsOverridden: _.isFinite(statistics.hunterExamTraining.moderatorOverriddenHunterExamTrainingEvents)
                    });
                };
            };

            var createTrainingFormGroups = function (trainingTypes) {
                return _.map(trainingTypes, function (trainingType) {
                    return {
                        name: trainingType,
                        input1Key: trainingType + 'TrainingEvents',
                        input2Key: trainingType + 'TrainingParticipants'
                    };
                });
            };

            var trainingModalCtrlOpts = {
                input2UnitKey: 'global.personUnit',
                getValue2Max: function (group) {
                    if (this.data[group.input1Key] === 0) {
                        return 0;
                    }
                    return 99999;
                }
            };

            this.openJhtTraining = function (statistics, trainingTypes) {
                var updateFn = function (data) {
                    return update(statistics.id, 'jhttraining', data);
                };

                var dialog = Tuple2ListFormDialog.create(statistics.jhtTraining, 'rhy.annualStats.trainings.jht.form.', updateFn);

                return dialog.open(createTrainingFormGroups(trainingTypes), trainingModalCtrlOpts);
            };

            this.openHunterTraining = function (statistics, trainingTypes) {
                var updateFn = function (data) {
                    return update(statistics.id, 'huntertraining', data);
                };

                var dialog = Tuple2ListFormDialog.create(statistics.hunterTraining, 'rhy.annualStats.trainings.hunter.form.', updateFn);

                return dialog.open(createTrainingFormGroups(trainingTypes), trainingModalCtrlOpts);
            };

            this.openYouthTraining = function (statistics, trainingTypes) {
                var updateFn = function (data) {
                    return update(statistics.id, 'youthtraining', data);
                };

                var dialog = Tuple2ListFormDialog.create(statistics.youthTraining, 'rhy.annualStats.trainings.youth.form.', updateFn);

                return dialog.open(createTrainingFormGroups(trainingTypes), trainingModalCtrlOpts);
            };

            this.openOtherHunterTraining = function (statistics, trainingTypes) {
                var updateFn = function (data) {
                    return update(statistics.id, 'otherhuntertraining', data);
                };

                var dialog = Tuple2ListFormDialog.create(statistics.otherHunterTraining, 'rhy.annualStats.trainings.otherHunter.form.', updateFn);

                return dialog.open(createTrainingFormGroups(trainingTypes), trainingModalCtrlOpts);
            };

            this.openPublicEvents = function (statistics) {
                var updateFn = function (data) {
                    return update(statistics.id, 'publicevents', data);
                };

                var formGroups = [{
                    name: 'publicEvents',
                    input1Key: 'publicEvents',
                    input2Key: 'publicEventParticipants'
                }];

                var dialog = Tuple2ListFormDialog.create(statistics.publicEvents, 'rhy.annualStats.trainings.publicEvents.form.', updateFn);

                return dialog.open(formGroups, trainingModalCtrlOpts);
            };

            this.openOtherHuntingRelated = function (statistics, isModerator) {
                var updateFn = function (data) {
                    var urlPostfix;

                    if (isModerator) {
                        urlPostfix = 'moderatedotherhuntingrelated';
                    } else {
                        urlPostfix = 'otherhuntingrelated';
                        delete data.wolfTerritoryWorkgroups;
                    }

                    delete data.harvestPermitApplicationPartners;

                    return update(statistics.id, urlPostfix, data);
                };

                var openDialog = RhyAnnualStatisticsDialogHelper.createOpenEditDialogFunction(
                    statistics.otherHuntingRelated, 'rhy/annualstats/misc/edit-other-hunting-related.html', updateFn);

                return function () {
                    return openDialog({isModerator: isModerator});
                };
            };

            this.openCommunication = function (statistics) {
                var updateFn = function (data) {
                    delete data.omariistaAnnouncements;
                    return update(statistics.id, 'communication', data);
                };

                return RhyAnnualStatisticsDialogHelper.createOpenEditDialogFunction(
                    statistics.communication, 'rhy/annualstats/misc/edit-communication.html', updateFn);
            };

            this.openShootingRanges = function (statistics) {
                var updateFn = function (data) {
                    return update(statistics.id, 'shootingranges', data);
                };

                return RhyAnnualStatisticsDialogHelper.createOpenEditDialogFunction(
                    statistics.shootingRanges, 'rhy/annualstats/misc/edit-shooting-ranges.html', updateFn);
            };

            this.openLuke = function (statistics) {
                var updateFn = function (data) {
                    delete data.sum;
                    delete data.isWillowGrouseAndCarnivoreDnaEditable;
                    return update(statistics.id, 'luke', data);
                };

                // Some of the fields are editable only after 2019
                var lukeStatistics = angular.extend({}, statistics.luke, {isWillowGrouseAndCarnivoreDnaEditable: statistics.year > 2019});

                return RhyAnnualStatisticsDialogHelper.createOpenEditDialogFunction(
                    lukeStatistics, 'rhy/annualstats/misc/edit-game-calculations.html', updateFn);
            };

            this.openMetsahallitus = function (statistics) {
                var updateFn = function (data) {
                    return update(statistics.id, 'metsahallitus', data);
                };

                return RhyAnnualStatisticsDialogHelper.createOpenEditDialogFunction(
                    statistics.metsahallitus, 'rhy/annualstats/misc/edit-metsahallitus.html', updateFn);
            };
        })

        .controller('RhyAnnualStatisticsController', function ($location, FormPostService, RhyAnnualStatisticsService,
                                                               RhyAnnualStatisticsState, RhyAnnualStatisticsStates,
                                                               RhyAnnualStatisticsViewState, activeTab,
                                                               allSpeciesNames, availableYears, isModerator,
                                                               permission, rhy, statistics, year) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.rhy = rhy;
                $ctrl.availableYears = availableYears;
                $ctrl.calendarYear = year;
                $ctrl.activeTab = activeTab;

                $ctrl.statistics = statistics;
                $ctrl.allSpeciesNames = allSpeciesNames;

                $ctrl.isModerator = isModerator;

                var state = $ctrl.statistics.state;
                $ctrl.isEditable = RhyAnnualStatisticsService.isStatisticsEditable(state, year, permission);
            };

            $ctrl.onSelectedYearChanged = function () {
                RhyAnnualStatisticsViewState.setYear($ctrl.calendarYear);
                RhyAnnualStatisticsViewState.reload();
            };

            $ctrl.onActiveTabChanged = function (activeTab) {
                RhyAnnualStatisticsViewState.setActiveTab(activeTab);
                $ctrl.activeTab = activeTab;
                $location.search({year: $ctrl.calendarYear, activeTab: activeTab});
            };

            var invokeExport = function (url, groupByRka) {
                var params = {};

                if (_.isBoolean(groupByRka)) {
                    params.groupByRka = groupByRka;
                }

                FormPostService.submitFormUsingBlankTarget(url, params);
            };

            $ctrl.exportToExcel = function () {
                var url = '/api/v1/riistanhoitoyhdistys/annualstatistics/' + $ctrl.statistics.id + '/excel';
                invokeExport(url);
            };

            $ctrl.exportAllAnnualStatisticsToExcel = function (groupByRka) {
                var url = '/api/v1/riistanhoitoyhdistys/annualstatistics/year/' + year + '/excel';
                invokeExport(url, groupByRka);
            };

            $ctrl.exportToPdf = function () {
                var url = '/api/v1/riistanhoitoyhdistys/annualstatistics/' + $ctrl.statistics.id + '/pdf';
                invokeExport(url);
            };

            $ctrl.isSubmitForInspectionButtonVisible = function () {
                var state = _.get($ctrl, 'statistics.state');
                return !RhyAnnualStatisticsStates.isCompletedByCoordinator(state);
            };

            $ctrl.isSubmittingForInspectionDisabled = function () {
                return !$ctrl.isEditable || !$ctrl.statistics.readyForInspection;
            };

            $ctrl.submitForInspection = function () {
                RhyAnnualStatisticsService.submitStatisticsForInspection($ctrl.statistics)
                    .then(RhyAnnualStatisticsViewState.reload);
            };

            $ctrl.isApproveButtonVisible = function () {
                return $ctrl.isModerator
                    && $ctrl.statistics.year >= 2018
                    && $ctrl.statistics.state === RhyAnnualStatisticsState.UNDER_INSPECTION;
            };

            $ctrl.isCancelApprovalButtonVisible = function () {
                return $ctrl.isModerator
                    && $ctrl.statistics.year >= 2018
                    && $ctrl.statistics.state === RhyAnnualStatisticsState.APPROVED;
            };

            $ctrl.approve = function () {
                RhyAnnualStatisticsService.approve($ctrl.statistics)
                    .then(RhyAnnualStatisticsViewState.reload);
            };

            $ctrl.cancelApproval = function () {
                RhyAnnualStatisticsService.cancelApproval($ctrl.statistics)
                    .then(RhyAnnualStatisticsViewState.reload);
            };
        })

        .component('rhyAnnualStatisticsState', {
            templateUrl: 'rhy/annualstats/annual-statistics-state.html',
            bindings: {
                state: '<',
                rhy: '<',
                calendarYear: '<',
                submitEvent: '<'
            },
            controller: function ($filter, $state, ActiveRoleService, RhyAnnualStatisticsState,
                                  RhyAnnualStatisticsStates) {
                var $ctrl = this;
                var toLowerCase = $filter('lowercase');

                var kebabcase = function (input) {
                    if (!_.isString(input) || input.length === 0) {
                        return input;
                    }

                    return toLowerCase(input).replace(/_/i, '-');
                };

                var getIconCssClass = function () {
                    switch ($ctrl.state) {
                        case RhyAnnualStatisticsState.NOT_STARTED:
                            return 'fa-ban';
                        case RhyAnnualStatisticsState.IN_PROGRESS:
                            return 'fa-edit';
                        case RhyAnnualStatisticsState.UNDER_INSPECTION:
                            return 'fa-envelope';
                        case RhyAnnualStatisticsState.APPROVED:
                            return 'fa-check';
                        default:
                            return '';
                    }
                };

                var getTooltipLocalisationKey = function () {
                    switch ($ctrl.state) {
                        case RhyAnnualStatisticsState.UNDER_INSPECTION:
                            return 'rhy.annualStats.tooltip.forState.' + $ctrl.state;
                        default:
                            return null;
                    }
                };

                $ctrl.$onInit = function () {
                    $ctrl.isModerator = ActiveRoleService.isModerator();
                    $ctrl.colorCssClass = kebabcase($ctrl.state);
                    $ctrl.iconCssClass = getIconCssClass();

                    $ctrl.isTooltipEnabled = $ctrl.state === RhyAnnualStatisticsState.UNDER_INSPECTION;
                    $ctrl.tooltipLocalisationKey = getTooltipLocalisationKey();
                };

                $ctrl.openAnnualStatisticsDashboard = function () {
                    var tabIndex = RhyAnnualStatisticsStates.indexOf($ctrl.state);
                    $state.go('jht.annualstats', {year: $ctrl.calendarYear, activeTab: tabIndex});
                };
            }
        })

        .component('rhyAnnualStatisticsTabset', {
            templateUrl: 'rhy/annualstats/tabs.html',
            bindings: {
                activeTab: '<',
                onActiveTabChanged: '&'
            },
            controller: function () {
                var $ctrl = this;

                $ctrl.onTabSelected = function (activeTab) {
                    $ctrl.onActiveTabChanged({activeTab: activeTab});
                };
            }
        })

        .component('timestampedPanelHeading', {
            templateUrl: 'rhy/annualstats/timestamped-panel-heading.html',
            bindings: {
                titleKey: '<',
                lastModified: '<'
            }
        })

        .directive('computedMarkerTitle', function () {
            return {
                restrict: 'A',
                replace: false,
                scope: true,
                link: function (scope, element, attrs) {
                    scope.i18nKey = attrs.computedMarkerTitle;
                },
                template: '<span translate="{{::i18nKey}}"></span>&nbsp;&nbsp;<span class="fa fa-bar-chart"></span>'
            };
        })

        .component('annualStatisticsOverview', {
            templateUrl: 'rhy/annualstats/overview/annual-statistics-overview.html',
            bindings: {
                statistics: '<',
                isEditable: '<'
            },
            controller: function (RhyAnnualStatisticsEditDialog) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.editBasicInfo = RhyAnnualStatisticsEditDialog.openBasicInfo($ctrl.statistics);
                };
            }
        })

        .component('showRhyBasicInfo', {
            templateUrl: 'rhy/annualstats/overview/show-rhy-basic-info.html',
            bindings: {
                basicInfo: '<',
                editable: '<',
                openEditDialog: '&'
            }
        })

        .component('showSubsidySummary', {
            templateUrl: 'rhy/annualstats/overview/show-subsidy-summary.html',
            bindings: {
                statistics: '<'
            },
            controller: function () {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.hunterExamTrainingEventsOverridden = _.isFinite($ctrl.statistics.hunterExamTraining.moderatorOverriddenHunterExamTrainingEvents);
                };
            }
        })

        .component('showOtherSummary', {
            templateUrl: 'rhy/annualstats/overview/show-other-summary.html',
            bindings: {
                statistics: '<'
            },
            controller: function () {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.hunterExamEventsOverridden = _.isFinite($ctrl.statistics.hunterExams.moderatorOverriddenHunterExamEvents);
                };
            }
        })

        .component('annualAdministrationStatistics', {
            templateUrl: 'rhy/annualstats/administration/annual-administration-statistics.html',
            bindings: {
                statistics: '<',
                isEditable: '<',
                isModerator: '<'
            },
            controller: function (RhyAnnualStatisticsEditDialog) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.editHunterExams = RhyAnnualStatisticsEditDialog.openHunterExams($ctrl.statistics, $ctrl.isModerator);
                    $ctrl.editShootingTests = RhyAnnualStatisticsEditDialog.openShootingTestsModeratorOverride($ctrl.statistics, $ctrl.isModerator);
                    $ctrl.editHuntingControl = RhyAnnualStatisticsEditDialog.openHuntingControl($ctrl.statistics);
                    $ctrl.editGameDamage = RhyAnnualStatisticsEditDialog.openGameDamage($ctrl.statistics);
                    $ctrl.editOtherPublicAdminData = RhyAnnualStatisticsEditDialog.openOtherPublicAdmin($ctrl.statistics);
                };
            }
        })

        .component('showHunterExamStatistics', {
            templateUrl: 'rhy/annualstats/administration/show-hunter-exams.html',
            bindings: {
                hunterExams: '<',
                editable: '<',
                openEditDialog: '&'
            },
            controller: function () {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.hunterExamEventsOverridden = _.isFinite($ctrl.hunterExams.moderatorOverriddenHunterExamEvents);
                };
            }
        })

        .component('showShootingTestStatistics', {
            templateUrl: 'rhy/annualstats/administration/show-shooting-tests.html',
            bindings: {
                shootingTests: '<',
                editable: '<',
                openEditDialog: '&',
                isModerator: '<'
            },
            controller: function () {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    var tests = $ctrl.shootingTests;

                    $ctrl.firearmTestEventsOverridden = _.isFinite(tests.moderatorOverriddenFirearmTestEvents);
                    $ctrl.bowTestEventsOverridden = _.isFinite(tests.moderatorOverriddenBowTestEvents);

                    $ctrl.countAllShootingTestAttempts = function () {
                        var mooseAttempts = Math.max(tests.allMooseAttempts, tests.qualifiedMooseAttempts);
                        var bearAttempts = Math.max(tests.allBearAttempts, tests.qualifiedBearAttempts);
                        var roeDeerAttempts = Math.max(tests.allRoeDeerAttempts, tests.qualifiedRoeDeerAttempts);
                        var bowAttempts = Math.max(tests.allBowAttempts, tests.qualifiedBowAttempts);

                        return mooseAttempts + bearAttempts + roeDeerAttempts + bowAttempts;
                    };
                };
            }
        })

        .component('showHuntingControlStatistics', {
            templateUrl: 'rhy/annualstats/administration/show-hunting-control.html',
            bindings: {
                huntingControl: '<',
                editable: '<',
                openEditDialog: '&'
            }
        })

        .component('showGameDamageStatistics', {
            templateUrl: 'rhy/annualstats/administration/show-game-damage.html',
            bindings: {
                gameDamage: '<',
                editable: '<',
                openEditDialog: '&'
            }
        })

        .component('showOtherAdminStatistics', {
            templateUrl: 'rhy/annualstats/administration/show-other-admin.html',
            bindings: {
                otherPublicAdmin: '<',
                editable: '<',
                openEditDialog: '&'
            }
        })

        .component('annualSrvaStatistics', {
            templateUrl: 'rhy/annualstats/srva/annual-srva-statistics.html',
            bindings: {
                statistics: '<',
                allSpeciesNames: '<'
            },
            controller: function (GameSpeciesCodes, $translate) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.srva = $ctrl.statistics.srva;

                    var speciesNameListOrdered = [
                        'MOOSE', 'WHITE_TAILED_DEER', 'ROE_DEER', 'WILD_FOREST_REINDEER', 'FALLOW_DEER', 'WILD_BOAR',
                        'LYNX', 'BEAR', 'WOLF', 'WOLVERINE'
                    ];

                    $ctrl.speciesCodeNamePairs = _.map(speciesNameListOrdered, function (speciesName) {
                        var speciesCode = GameSpeciesCodes[speciesName];

                        return {
                            code: speciesCode,
                            name: $ctrl.allSpeciesNames[speciesCode]
                        };
                    });
                    $ctrl.speciesCodeNamePairs.push({code: -1, name: $translate.instant("rhy.annualStats.srva.otherSpecies")});
                };
            }
        })

        .component('showSrvaTotalStatistics', {
            templateUrl: 'rhy/annualstats/srva/show-srva-totals.html',
            bindings: {
                srva: '<'
            }
        })

        .component('showSrvaAccidentStatistics', {
            templateUrl: 'rhy/annualstats/srva/show-srva-accidents.html',
            bindings: {
                srva: '<',
                speciesCodeNamePairs: '<'
            },
            controller: function () {
                var $ctrl = this;

                this.$onInit = function () {
                    $ctrl.speciesList = _.map($ctrl.speciesCodeNamePairs, function (pair) {
                        return {
                            code: pair.code,
                            name: pair.name,
                            amount: $ctrl.srva.accident[pair.code]
                        };
                    });
                };
            }
        })

        .component('showSrvaEventStatistics', {
            templateUrl: 'rhy/annualstats/srva/show-srva-events.html',
            bindings: {
                events: '<',
                speciesCodeNamePairs: '<',
                localisationPrefix: '<'
            },
            controller: function () {
                var $ctrl = this;

                this.$onInit = function () {
                    $ctrl.speciesList = _.map($ctrl.speciesCodeNamePairs, function (pair) {
                        return {
                            code: pair.code,
                            name: pair.name,
                            amount: $ctrl.events[pair.code]
                        };
                    });
                };
            }
        })

        .component('annualTrainingStatistics', {
            templateUrl: 'rhy/annualstats/trainings/annual-training-statistics.html',
            bindings: {
                statistics: '<',
                isEditable: '<',
                isModerator: '<'
            },
            controller: function (RhyAnnualStatisticsEditDialog) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.editHunterExamTraining = RhyAnnualStatisticsEditDialog.openHunterExamTraining($ctrl.statistics, $ctrl.isModerator);

                    $ctrl.editJhtTraining = function (trainingTypes) {
                        return RhyAnnualStatisticsEditDialog.openJhtTraining($ctrl.statistics, trainingTypes);
                    };

                    $ctrl.editHunterTraining = function (trainingTypes) {
                        RhyAnnualStatisticsEditDialog.openHunterTraining($ctrl.statistics, trainingTypes);
                    };

                    $ctrl.editYouthTraining = function (trainingTypes) {
                        RhyAnnualStatisticsEditDialog.openYouthTraining($ctrl.statistics, trainingTypes);
                    };

                    $ctrl.editOtherHunterTraining = function (trainingTypes) {
                        return RhyAnnualStatisticsEditDialog.openOtherHunterTraining($ctrl.statistics, trainingTypes);
                    };
                };
            }
        })

        .component('showTrainingStatisticsOverview', {
            templateUrl: 'rhy/annualstats/trainings/show-training-overview.html',
            bindings: {
                statistics: '<'
            }
        })

        .component('showHunterExamTrainingStatistics', {
            templateUrl: 'rhy/annualstats/trainings/show-hunter-exam-training.html',
            bindings: {
                hunterExamTraining: '<',
                editable: '<',
                openEditDialog: '&'
            },
            controller: function () {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.hunterExamTrainingEventsOverridden = _.isFinite($ctrl.hunterExamTraining.moderatorOverriddenHunterExamTrainingEvents);
                };
            }
        })

        .component('showJhtTrainingStatistics', {
            templateUrl: 'rhy/annualstats/trainings/show-jht-training.html',
            bindings: {
                jhtTraining: '<',
                editable: '<',
                openEditDialog: '&'
            },
            controller: function () {
                var $ctrl = this;

                $ctrl.trainingTypes = ['shootingTest', 'hunterExamOfficial', 'gameDamage', 'huntingControl'];

                $ctrl.editJhtTraining = function () {
                    $ctrl.openEditDialog({trainingTypes: $ctrl.trainingTypes});
                };
            }
        })

        .component('showHunterTrainingStatistics', {
            templateUrl: 'rhy/annualstats/trainings/show-hunter-training.html',
            bindings: {
                hunterTraining: '<',
                editable: '<',
                openEditDialog: '&'
            },
            controller: function () {
                var $ctrl = this;

                $ctrl.trainingTypes = [
                    'mooselikeHunting', 'mooselikeHuntingLeader', 'carnivoreHunting', 'carnivoreHuntingLeader', 'srva',
                    'carnivoreContactPerson', 'accidentPrevention'];

                $ctrl.editHunterTraining = function () {
                    $ctrl.openEditDialog({trainingTypes: $ctrl.trainingTypes});
                };
            }
        })

        .component('showYouthTrainingStatistics', {
            templateUrl: 'rhy/annualstats/trainings/show-youth-training.html',
            bindings: {
                youthTraining: '<',
                editable: '<',
                openEditDialog: '&'
            },
            controller: function () {
                var $ctrl = this;

                $ctrl.trainingTypes = ['school', 'college', 'otherYouthTargeted'];

                $ctrl.editYouthTraining = function () {
                    $ctrl.openEditDialog({trainingTypes: $ctrl.trainingTypes});
                };
            }
        })

        .component('showOtherHunterTrainingStatistics', {
            templateUrl: 'rhy/annualstats/trainings/show-other-hunter-training.html',
            bindings: {
                otherHunterTraining: '<',
                editable: '<',
                openEditDialog: '&'
            },
            controller: function () {
                var $ctrl = this;

                $ctrl.trainingTypes = [
                    'smallCarnivoreHunting', 'gameCounting', 'gamePopulationManagement', 'gameEnvironmentalCare',
                    'otherGamekeeping', 'shooting', 'tracker'
                ];

                $ctrl.editHunterTraining = function () {
                    $ctrl.openEditDialog({trainingTypes: $ctrl.trainingTypes});
                };
            }
        })

        .component('showPublicEventStatistics', {
            templateUrl: 'rhy/annualstats/misc/show-public-events.html',
            bindings: {
                publicEvents: '<',
                editable: '<',
                openEditDialog: '&'
            },
            controller: function () {
                var $ctrl = this;

                $ctrl.editPublicEvents = function () {
                    return $ctrl.openEditDialog();
                };
            }
        })

        .component('annualMiscStatistics', {
            templateUrl: 'rhy/annualstats/misc/annual-misc-statistics.html',
            bindings: {
                statistics: '<',
                isEditable: '<',
                isModerator: '<'
            },
            controller: function (RhyAnnualStatisticsEditDialog) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.editOtherHuntingRelated = RhyAnnualStatisticsEditDialog.openOtherHuntingRelated($ctrl.statistics, $ctrl.isModerator);
                    $ctrl.editCommunication = RhyAnnualStatisticsEditDialog.openCommunication($ctrl.statistics);
                    $ctrl.editShootingRanges = RhyAnnualStatisticsEditDialog.openShootingRanges($ctrl.statistics);
                    $ctrl.editGameCalculations = RhyAnnualStatisticsEditDialog.openLuke($ctrl.statistics);
                    $ctrl.editMetsahallitusData = RhyAnnualStatisticsEditDialog.openMetsahallitus($ctrl.statistics);
                    $ctrl.editPublicEvents = function () {
                        return RhyAnnualStatisticsEditDialog.openPublicEvents($ctrl.statistics);
                    };

                };
            }
        })

        .component('showOtherHuntingRelatedStatistics', {
            templateUrl: 'rhy/annualstats/misc/show-other-hunting-related.html',
            bindings: {
                otherHuntingRelated: '<',
                editable: '<',
                openEditDialog: '&'
            }
        })

        .component('showCommunicationStatistics', {
            templateUrl: 'rhy/annualstats/misc/show-communication.html',
            bindings: {
                communication: '<',
                editable: '<',
                openEditDialog: '&'
            }
        })

        .component('showShootingRangeStatistics', {
            templateUrl: 'rhy/annualstats/misc/show-shooting-ranges.html',
            bindings: {
                shootingRanges: '<',
                editable: '<',
                openEditDialog: '&'
            }
        })

        .component('showGameCalculationStatistics', {
            templateUrl: 'rhy/annualstats/misc/show-game-calculations.html',
            bindings: {
                luke: '<',
                editable: '<',
                openEditDialog: '&'
            }
        })

        .component('showMetsahallitusStatistics', {
            templateUrl: 'rhy/annualstats/misc/show-metsahallitus.html',
            bindings: {
                metsahallitus: '<',
                editable: '<',
                openEditDialog: '&'
            }
        });

})();
