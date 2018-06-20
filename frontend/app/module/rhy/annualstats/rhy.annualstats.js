(function () {
    "use strict";

    angular.module('app.rhy.annualstats', [])
        .config(function ($stateProvider) {
            $stateProvider
                .state('rhy.annualstats', {
                    url: '/annualstatistics?{year:2[0-9]{3}}&activeTab',
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
                        rhy: function (RhyAnnualStatisticsState, Rhys, rhyId) {
                            if (_.isFinite(rhyId)) {
                                RhyAnnualStatisticsState.setRhyId(rhyId);
                            }

                            return Rhys.get({id: rhyId}).$promise;
                        },
                        availableYears: function (AnnualStatisticsAvailableYears) {
                            return AnnualStatisticsAvailableYears.get();
                        },
                        year: function ($stateParams, RhyAnnualStatisticsState, availableYears) {
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

                            var activeTabParam = $stateParams.activeTab;
                            var activeTab = activeTabParam ? _.parseInt(activeTabParam) : 0;

                            RhyAnnualStatisticsState.setYear(year);
                            RhyAnnualStatisticsState.setActiveTab(activeTab);

                            return year;
                        },
                        statistics: function (RhyAnnualStatisticsService, year) {
                            return RhyAnnualStatisticsService.getStatistics(year);
                        },
                        allSpeciesNames: function (GameDiaryParameters) {
                            return GameDiaryParameters.query().$promise.then(function (params) {
                                var ret = {};

                                _.each(params.species, function (species) {
                                    ret[species.code] = species.name;
                                });

                                return ret;
                            });
                        }
                    }
                });
        })

        .constant('RhyAnnualStatisticsStates', [
            'NOT_CREATED',
            'IN_PROGRESS',
            'UNDER_INSPECTION',
            'APPROVED'
        ])

        .service('AnnualStatisticsAvailableYears', function () {
            this.get = function () {
                return [2017];

                // TODO uncomment after annual statistics locking logic is implemented.
                //return _.range(2017, new Date().getFullYear() + 1);
            };
        })

        .service('RhyAnnualStatisticsState', function ($state) {
            var self = this,
                rhyId,
                year,
                activeTab;

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
                update: {
                    method: 'PUT',
                    url: '/api/v1/riistanhoitoyhdistys/annualstatistics/:statisticsId',
                    params: {statisticsId: '@statisticsId'}
                },
                submitForInspection: {
                    method: 'POST',
                    url: '/api/v1/riistanhoitoyhdistys/annualstatistics/:statisticsId/submitforinspection',
                    params: {statisticsId: '@statisticsId'}
                },
                approve: {
                    method: 'POST',
                    url: '/api/v1/riistanhoitoyhdistys/annualstatistics/:statisticsId/approve',
                    params: {statisticsId: '@statisticsId'}
                }
            });
        })

        .service('RhyAnnualStatisticsService', function ($uibModal, NotificationService, RhyAnnualStatistics,
                                                         RhyAnnualStatisticsState) {
            var self = this;

            this.getStatistics = function (year) {
                var rhyId = RhyAnnualStatisticsState.getRhyId();
                return RhyAnnualStatistics.getOrCreate({rhyId: rhyId, calendarYear: year}).$promise;
            };

            this.reloadStatistics = function () {
                return self.getStatistics(RhyAnnualStatisticsState.getYear());
            };

            this.updateStatistics = function (statistics) {
                return RhyAnnualStatistics.update({statisticsId: statistics.id}, statistics).$promise;
            };

            this.isEditable = function (statistics, isModerator) {
                if (isModerator) {
                    return statistics.state !== 'APPROVED';
                }

                return statistics.state === 'IN_PROGRESS';
            };

            this.submitStatisticsForInspection = function (statistics) {
                return RhyAnnualStatistics.submitForInspection({statisticsId: statistics.id}, {
                    id: statistics.id,
                    rev: statistics.rev
                }).$promise;
            };

            this.approve = function (statistics) {
                return RhyAnnualStatistics.approve({statisticsId: statistics.id}, {
                    id: statistics.id,
                    rev: statistics.rev
                }).$promise;
            };

            // 'onBeforeSaveCallback' function is given the embedded object as the only parameter.
            this.createOpenEditDialogFunction = function (statistics, embeddedObjectName, templateUrl,
                                                          onBeforeSaveCallback) {

                function showModal(data, modalCtrlOpts) {
                    var modalPromise = $uibModal.open({
                        templateUrl: templateUrl,
                        controllerAs: '$ctrl',
                        controller: ModalController,
                        resolve: {
                            data: _.constant(data),
                            modalCtrlOpts: _.constant(modalCtrlOpts)
                        }
                    }).result.then(saveData);

                    NotificationService.handleModalPromise(modalPromise).then(function () {
                        NotificationService.flashMessage('global.messages.success', 'success');
                        RhyAnnualStatisticsState.reload();
                    });
                }

                function saveData(data) {
                    return self.reloadStatistics().then(function (reloaded) {
                        if (angular.isFunction(onBeforeSaveCallback)) {
                            onBeforeSaveCallback(data);
                        }

                        reloaded[embeddedObjectName] = data;

                        delete reloaded.state;
                        delete reloaded.allTrainingEvents;
                        delete reloaded.allTrainingParticipants;
                        delete reloaded.stateAidHunterTrainingEvents;
                        delete reloaded.schoolAndCollegeTrainingEvents;
                        delete reloaded.stateAidAffectingQuantitiesLastModified;
                        delete reloaded.publicAdministrationTasksLastModified;
                        delete reloaded.srva;
                        delete reloaded.shootingTests.allShootingTestEvents;
                        delete reloaded.gameDamage.totalDamageInspectionLocations;
                        delete reloaded.gameDamage.totalDamageInspectionExpenses;
                        delete reloaded.luke.sum;
                        delete reloaded.readyForInspection;
                        delete reloaded.completeForApproval;

                        return self.updateStatistics(reloaded);
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
                    showModal(statistics[embeddedObjectName], modalCtrlOpts);
                };
            };
        })

        .service('Tuple2ListFormDialog', function (RhyAnnualStatisticsService) {
            var self = this;

            /*
             * The following localisation keys need to exist with the given prefix:
             *  - title
             *  - value1Key
             *  - value2Key
             */
            this.create = function (statistics, embeddedObjectName, localisationPrefix) {
                var upperLimit = 99999;

                var defaults = {
                    value1UnitKey: 'global.pcs',
                    value2UnitKey: 'global.pcs',
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

                var openEditDialogFn = RhyAnnualStatisticsService.createOpenEditDialogFunction(
                    statistics, embeddedObjectName, 'rhy/annualstats/tuple2-list-form.html');

                return {
                    /*
                     * Each group must have the following properties:
                     *  - name
                     *  - value1Key
                     *  - value2Key
                     *
                     * The following properties are supported in 'modalCtrlOpts' parameter:
                     *  - value1UnitKey (localisation key for unit of the first value)
                     *  - value2UnitKey (localisation key for unit of the second value)
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

        .controller('RhyAnnualStatisticsController', function ($state, ActiveRoleService, FormPostService,
                                                               NotificationService, RhyAnnualStatisticsService,
                                                               RhyAnnualStatisticsState, RhyAnnualStatisticsStates,
                                                               allSpeciesNames, availableYears, rhy, statistics,
                                                               year) {
            var $ctrl = this;
            var rhyId = rhy.id;

            $ctrl.$onInit = function () {
                $ctrl.rhy = rhy;
                $ctrl.availableYears = availableYears;
                $ctrl.calendarYear = year;
                $ctrl.statistics = statistics;
                $ctrl.allSpeciesNames = allSpeciesNames;
                $ctrl.isModerator = ActiveRoleService.isModerator();
            };

            $ctrl.onSelectedYearChanged = function () {
                RhyAnnualStatisticsState.setYear($ctrl.calendarYear);
                RhyAnnualStatisticsState.reload();
            };

            $ctrl.getCssClassForStateLabel = function () {
                var state = _.get($ctrl, 'statistics.state');

                if (state === 'APPROVED') {
                    return 'r-annual-statistics-approved';
                } else if (state === 'UNDER_INSPECTION') {
                    return 'r-annual-statistics-under-inspection';
                }
                return 'r-annual-statistics-in-progress';
            };

            var invokeExport = function (url, groupByRka) {
                var params = {};

                if (_.isBoolean(groupByRka)) {
                    params.groupByRka = groupByRka;
                }

                FormPostService.submitFormUsingBlankTarget(url, params);
            };

            $ctrl.openAnnualStatisticsDashboard = function () {
                var tabIndex = _.indexOf(RhyAnnualStatisticsStates, $ctrl.statistics.state);
                $state.go('jht.annualstats', {year: $ctrl.calendarYear, activeTab: tabIndex});
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
                return $ctrl.statistics.state === 'IN_PROGRESS';
            };

            $ctrl.submitForInspection = function () {
                RhyAnnualStatisticsService.submitStatisticsForInspection($ctrl.statistics)
                    .then(function () {
                        NotificationService.flashMessage('global.messages.success', 'success');
                        RhyAnnualStatisticsState.reload();
                    });
            };

            $ctrl.isApproveButtonVisible = function () {
                return $ctrl.isModerator && $ctrl.statistics.year >= 2018 && $ctrl.statistics.state === 'UNDER_INSPECTION';
            };

            $ctrl.approve = function () {
                RhyAnnualStatisticsService.approve($ctrl.statistics)
                    .then(function () {
                        NotificationService.flashMessage('global.messages.success', 'success');
                        RhyAnnualStatisticsState.reload();
                    });
            };
        })

        .component('annualStatisticsTabset', {
            templateUrl: 'rhy/annualstats/tabs.html',
            bindings: {
                statistics: '<',
                allSpeciesNames: '<'
            },
            controller: function ($location, ActiveRoleService, RhyAnnualStatisticsState) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.activeTabIndex = RhyAnnualStatisticsState.getActiveTab();
                    $ctrl.isModerator = ActiveRoleService.isModerator();
                };

                $ctrl.onTabSelected = function (activeTab) {
                    RhyAnnualStatisticsState.setActiveTab(activeTab);
                    $location.search({year: RhyAnnualStatisticsState.getYear(), activeTab: activeTab});
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
                isModerator: '<'
            },
            controller: function (RhyAnnualStatisticsService) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.editable = $ctrl.isModerator && $ctrl.statistics.state !== 'APPROVED';

                    $ctrl.editBasicInfo = RhyAnnualStatisticsService.createOpenEditDialogFunction(
                        $ctrl.statistics, 'basicInfo', 'rhy/annualstats/overview/edit-rhy-basic-info.html');
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

        .component('showStateAidAffectingStatistics', {
            templateUrl: 'rhy/annualstats/overview/show-state-aid-statistics.html',
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

        .component('showJhtStatisticsOverview', {
            templateUrl: 'rhy/annualstats/overview/show-jht-statistics-overview.html',
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
                isModerator: '<'
            },
            controller: function (RhyAnnualStatisticsService, Tuple2ListFormDialog) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.editable = RhyAnnualStatisticsService.isEditable($ctrl.statistics, $ctrl.isModerator);

                    var openHunterExamsDialog = RhyAnnualStatisticsService.createOpenEditDialogFunction(
                        $ctrl.statistics, 'hunterExams', 'rhy/annualstats/administration/edit-hunter-exams.html', function (data) {
                            if (_.isFinite(data.moderatorOverriddenHunterExamEvents)) {
                                delete data.hunterExamEvents;
                            }
                        });

                    $ctrl.editHunterExams = function () {
                        return openHunterExamsDialog({
                            isModerator: $ctrl.isModerator,
                            hunterExamEventsOverridden: _.isFinite($ctrl.statistics.hunterExams.moderatorOverriddenHunterExamEvents)
                        });
                    };

                    $ctrl.editHuntingControl = RhyAnnualStatisticsService.createOpenEditDialogFunction(
                        $ctrl.statistics, 'huntingControl', 'rhy/annualstats/administration/edit-hunting-control.html');

                    $ctrl.editGameDamage = RhyAnnualStatisticsService.createOpenEditDialogFunction(
                        $ctrl.statistics, 'gameDamage', 'rhy/annualstats/administration/edit-game-damage.html');

                    $ctrl.editOtherPublicAdminData = RhyAnnualStatisticsService.createOpenEditDialogFunction(
                        $ctrl.statistics, 'otherPublicAdmin', 'rhy/annualstats/administration/edit-other-admin-data.html');

                    var openShootingTestDialog =  RhyAnnualStatisticsService.createOpenEditDialogFunction(
                        $ctrl.statistics, 'shootingTests', 'rhy/annualstats/administration/edit-shooting-tests.html', function (data) {
                            if (_.isFinite(data.moderatorOverriddenFirearmTestEvents)) {
                                delete data.firearmTestEvents;
                            }
                            if (_.isFinite(data.moderatorOverriddenBowTestEvents)) {
                                delete data.bowTestEvents;
                            }
                        });

                    var shootingTestTypes = _.map(['moose', 'bear', 'roeDeer', 'bow'], function (shootingTestType) {
                        var getKey = function (name) {
                            return name + _.capitalize(shootingTestType) + 'Attempts';
                        };

                        return {
                            name: shootingTestType,
                            value1Key: getKey('all'),
                            value2Key: getKey('qualified')
                        };
                    });

                    var shootingTestModalCtrlOpts = {
                        shootingTestTypes: shootingTestTypes,
                        isModerator: $ctrl.isModerator,
                        firearmTestEventsOverridden: _.isFinite($ctrl.statistics.shootingTests.moderatorOverriddenFirearmTestEvents),
                        bowTestEventsOverridden: _.isFinite($ctrl.statistics.shootingTests.moderatorOverriddenBowTestEvents),
                        getTitleKey: function (shootingTestType) {
                            return 'rhy.annualStats.administration.shootingTests.form.' + shootingTestType.name + 'Title';
                        },
                        getValue1Min: function (shootingTestType) {
                            var qualified = this.data[shootingTestType.value2Key];
                            return Math.max(0, qualified);
                        },
                        getValue2Max: function (shootingTestType) {
                            var limit = 99999;
                            var all = this.data[shootingTestType.value1Key];
                            return _.isFinite(all) ? Math.min(limit, all) : limit;
                        }
                    };

                    $ctrl.editShootingTests = function () {
                        return openShootingTestDialog(shootingTestModalCtrlOpts);
                    };
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
                openEditDialog: '&'
            },
            controller: function ($uibModal, RhyAnnualStatisticsService) {
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
            controller: function (GameSpeciesCodes) {
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
                isModerator: '<'
            },
            controller: function (RhyAnnualStatisticsService, Tuple2ListFormDialog) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.editable = RhyAnnualStatisticsService.isEditable($ctrl.statistics, $ctrl.isModerator);

                    var hunterExamTrainingDialog = RhyAnnualStatisticsService.createOpenEditDialogFunction(
                        $ctrl.statistics, 'hunterExamTraining', 'rhy/annualstats/trainings/edit-hunter-exam-training.html', function (data) {
                            if (_.isFinite(data.moderatorOverriddenHunterExamTrainingEvents)) {
                                delete data.hunterExamTrainingEvents;
                            }
                        });

                    var jhtTrainingDialog = Tuple2ListFormDialog.create(
                        $ctrl.statistics, 'jhtTraining', 'rhy.annualStats.trainings.jht.form.');

                    var stateAidTrainingDialog = Tuple2ListFormDialog.create(
                        $ctrl.statistics, 'stateAidTraining', 'rhy.annualStats.trainings.stateAid.form.');

                    var otherHunterTrainingDialog = Tuple2ListFormDialog.create(
                        $ctrl.statistics, 'otherHunterTraining', 'rhy.annualStats.trainings.otherHunterTraining.form.');

                    var otherTrainingDialog = Tuple2ListFormDialog.create(
                        $ctrl.statistics, 'otherTraining', 'rhy.annualStats.trainings.otherTraining.form.');

                    $ctrl.editHunterExamTraining = function () {
                        return hunterExamTrainingDialog({
                            isModerator: $ctrl.isModerator,
                            hunterExamTrainingEventsOverridden: _.isFinite($ctrl.statistics.hunterExamTraining.moderatorOverriddenHunterExamTrainingEvents)
                        });
                    };

                    function createFormGroups(trainingTypes) {
                        return _.map(trainingTypes, function (trainingType) {
                            return {
                                name: trainingType,
                                value1Key: trainingType + 'TrainingEvents',
                                value2Key: trainingType + 'TrainingParticipants'
                            };
                        });
                    }

                    var modalCtrlOpts = {
                        value2UnitKey: 'global.personUnit'
                    };

                    $ctrl.editJhtTraining = function (trainingTypes) {
                        return jhtTrainingDialog.open(createFormGroups(trainingTypes), modalCtrlOpts);
                    };

                    $ctrl.editStateAidTraining = function (trainingTypes) {
                        return stateAidTrainingDialog.open(createFormGroups(trainingTypes), modalCtrlOpts);
                    };

                    $ctrl.editOtherHunterTraining = function (trainingTypes) {
                        return otherHunterTrainingDialog.open(createFormGroups(trainingTypes), modalCtrlOpts);
                    };

                    $ctrl.editOtherTraining = function (trainingTypes) {
                        return otherTrainingDialog.open(createFormGroups(trainingTypes), modalCtrlOpts);
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

        .component('showStateAidTrainingStatistics', {
            templateUrl: 'rhy/annualstats/trainings/show-state-aid-training.html',
            bindings: {
                stateAidTraining: '<',
                editable: '<',
                openEditDialog: '&'
            },
            controller: function () {
                var $ctrl = this;

                $ctrl.trainingTypes = [
                    'mooselikeHunting', 'mooselikeHuntingLeader', 'carnivoreHunting', 'carnivoreHuntingLeader', 'srva',
                    'carnivoreContactPerson', 'accidentPrevention', 'school', 'college', 'otherYouthTargeted'];

                $ctrl.editStateAidTraining = function () {
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
                    'otherGamekeeping', 'otherShooting', 'tracker'
                ];

                $ctrl.editHunterTraining = function () {
                    $ctrl.openEditDialog({trainingTypes: $ctrl.trainingTypes});
                };
            }
        })

        .component('showOtherTrainingStatistics', {
            templateUrl: 'rhy/annualstats/trainings/show-other-training.html',
            bindings: {
                otherTraining: '<',
                editable: '<',
                openEditDialog: '&'
            },
            controller: function () {
                var $ctrl = this;

                $ctrl.trainingTypes = ['other'];

                $ctrl.editOtherTraining = function () {
                    $ctrl.openEditDialog({trainingTypes: $ctrl.trainingTypes});
                };
            }
        })

        .component('annualMiscStatistics', {
            templateUrl: 'rhy/annualstats/misc/annual-misc-statistics.html',
            bindings: {
                statistics: '<',
                isModerator: '<'
            },
            controller: function (RhyAnnualStatisticsService) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.editable = RhyAnnualStatisticsService.isEditable($ctrl.statistics, $ctrl.isModerator);
                    $ctrl.moderatorEditable = $ctrl.isModerator && $ctrl.editable;

                    $ctrl.editOtherHuntingRelated = RhyAnnualStatisticsService.createOpenEditDialogFunction(
                        $ctrl.statistics, 'otherHuntingRelated', 'rhy/annualstats/misc/edit-other-hunting-related.html');

                    $ctrl.editCommunication = RhyAnnualStatisticsService.createOpenEditDialogFunction(
                        $ctrl.statistics, 'communication', 'rhy/annualstats/misc/edit-communication.html');

                    $ctrl.editShootingRanges = RhyAnnualStatisticsService.createOpenEditDialogFunction(
                        $ctrl.statistics, 'shootingRanges', 'rhy/annualstats/misc/edit-shooting-ranges.html');

                    $ctrl.editGameCalculations = RhyAnnualStatisticsService.createOpenEditDialogFunction(
                        $ctrl.statistics, 'luke', 'rhy/annualstats/misc/edit-game-calculations.html');

                    $ctrl.editMetsahallitusData = RhyAnnualStatisticsService.createOpenEditDialogFunction(
                        $ctrl.statistics, 'metsahallitus', 'rhy/annualstats/misc/edit-metsahallitus.html');
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
