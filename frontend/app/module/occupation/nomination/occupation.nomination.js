(function () {
    "use strict";

    var requestTransformer = function (data) {
        if (angular.isObject(data)) {
            // Remove route path variable
            delete data.action;

            // Remove empty key values
            _.forOwn(data, function (value, key, object) {
                if (angular.isUndefined(value) || value === null) {
                    delete object[key];
                }
            });

            return angular.toJson(data);
        }
        return data;
    };

    angular.module('app.occupation.nomination', [])
        .service('OccupationNominationService', OccupationNominationService)
        .controller('OccupationNominationListController', OccupationNominationListController)
        .controller('AddTrainingDialogController', AddTrainingDialogController)
        .controller('OccupationNominationDialogController', OccupationNominationDialogController)
        .factory('JHTTrainings', function ($resource) {
            return $resource('api/v1/jht-training/:id', {id: "@id", action: "@action"}, {
                search: {method: 'POST', url: 'api/v1/jht-training/search'},
                mine: {method: 'GET', url: 'api/v1/jht-training/mine', isArray: true},
                forPerson: {
                    method: 'GET', 
                    url: 'api/v1/jht-training/person/:personId', 
                    isArray: true, 
                    params: {
                        personId: '@personId'
                    }
                },
                doAction: {
                    method: 'POST',
                    url: 'api/v1/jht-training/:id/:action',
                    transformRequest: requestTransformer
                }
            });
        })
        .factory('OccupationNominations', function ($resource) {
            return $resource('api/v1/organisation/occupation-nomination/:id', {id: "@id", action: "@action"}, {
                search: {method: 'POST', url: 'api/v1/occupation-nomination/search'},
                doAction: {
                    method: 'POST',
                    url: 'api/v1/occupation-nomination/:id/:action',
                    transformRequest: requestTransformer
                }
            });
        });

    function OccupationNominationService($q, $http, $uibModal,
                                         FormPostService,
                                         OccupationNominations,
                                         JHTOccupationTypes,
                                         JHTTrainings) {
        var self = this;

        this.getCounts = function (rhyOfficialCode, occupationType) {
            return $http({
                url: 'api/v1/occupation-nomination/counts',
                method: 'GET',
                params: {
                    rhyOfficialCode: rhyOfficialCode,
                    occupationType: occupationType
                }
            }).then(_.property('data'));
        };

        this.getOccupationPeriodForToday = function () {
            return $http({
                url: 'api/v1/occupation-nomination/occupationPeriod',
                method: 'GET'
            }).then(_.property('data'));
        };

        this.createSearchParameters = function (defaultRhy) {
            return {
                searchType: 'PREVIOUS_OCCUPATION',
                rhyCode: defaultRhy ? defaultRhy.officialCode : null,
                occupationType: _.first(JHTOccupationTypes),
                nominationStatus: 'KOULUTUS'
            };
        };

        this.resetSearchParameters = function (params, defaultRhy) {
            var result = _.pick(params, ['searchType', 'occupationType', 'nominationStatus']);

            if (params.nominationStatus === 'KOULUTUS' && (
                params.searchType === 'PERSON' ||
                params.searchType === 'TRAINING_LOCATION')) {
                return result;
            }

            result.rhyCode = defaultRhy ? defaultRhy.officialCode : params.rhyCode;
            result.areaCode = defaultRhy ? null : params.areaCode;
            return result;
        };

        function searchParamsToRequestData(params, defaultPageSize) {
            var commonSearchParams = ['occupationType', 'rhyCode', 'ssn', 'hunterNumber', 'beginDate', 'endDate'];

            var isTrainingSearch = params.nominationStatus === 'KOULUTUS';
            var data = _.pick(params, commonSearchParams.concat(isTrainingSearch
                ? ['searchType', 'trainingType', 'trainingLocation']
                : ['occupationType', 'nominationStatus']));

            data.pageSize = params.pageSize || defaultPageSize;
            data.page = params.page ? params.page - 1 : 0;

            return data;
        }

        this.search = function (params) {
            var repository = params.nominationStatus === 'KOULUTUS' ? JHTTrainings : OccupationNominations;
            var showAll = params.nominationStatus === 'EHDOLLA' || params.nominationStatus === 'ESITETTY';
            var requestData = searchParamsToRequestData(params, showAll ? 2000 : 50);

            if (params.nominationStatus !== 'KOULUTUS' && !params.rhyCode) {
                return $q.when({content: []});
            }

            return repository.search(requestData).$promise;
        };

        this.exportToExcel = function (params) {
            var formSubmitAction = params.nominationStatus === 'KOULUTUS'
                ? '/api/v1/jht-training/searchExcel'
                : '/api/v1/occupation-nomination/searchExcel';
            var requestData = searchParamsToRequestData(params, 2000);
            requestData.page = 0;

            FormPostService.submitFormUsingBlankTarget(formSubmitAction, {'json': angular.toJson(requestData)});
        };

        this.showNominationDialog = function (params) {
            return $uibModal.open({
                templateUrl: 'occupation/nomination/nomination-dialog.html',
                resolve: {
                    params: params,
                    occupationPeriod: self.getOccupationPeriodForToday()
                },
                controller: 'OccupationNominationDialogController',
                controllerAs: '$ctrl',
                bindToController: true
            }).result;
        };

        this.showAddTrainingDialog = function (params) {
            return $uibModal.open({
                templateUrl: 'occupation/nomination/training-dialog.html',
                resolve: {
                    params: params
                },
                controller: 'AddTrainingDialogController',
                controllerAs: '$ctrl',
                bindToController: true
            }).result.then(function (occupationNomination) {
                return $http({
                    url: 'api/v1/jht-training',
                    method: 'POST',
                    data: occupationNomination
                }).then(function () {
                    // Retain field values for mass insert
                    params.trainingDate = occupationNomination.trainingDate;
                    params.trainingLocation = occupationNomination.trainingLocation;

                    self.showAddTrainingDialog(params);
                });
            });
        };
    }

    function OccupationNominationListController($q, dialogs, $translate,
                                                OccupationNominationService,
                                                NotificationService,
                                                JHTOccupationTypes,
                                                JHTTrainings,
                                                OccupationNominations,
                                                ActiveRoleService,
                                                activeRhy,
                                                searchParams,
                                                resultList) {
        var $ctrl = this;

        $ctrl.activeRhy = activeRhy;
        $ctrl.searchParams = searchParams;
        $ctrl.resultList = resultList;
        $ctrl.isModerator = ActiveRoleService.isModerator();
        $ctrl.occupationTypes = JHTOccupationTypes;
        $ctrl.selectAllModel = false;
        $ctrl.counts = {};

        reloadCounts();

        function reloadCounts() {
            var occupationType = $ctrl.searchParams.occupationType;
            var rhyCode = $ctrl.isModerator ? $ctrl.searchParams.rhyCode
                : activeRhy ? activeRhy.officialCode
                : null;

            if (occupationType && rhyCode) {
                OccupationNominationService.getCounts(rhyCode, occupationType).then(function (result) {
                    $ctrl.counts = result;
                }, function () {
                    $ctrl.counts = {};
                });
            } else {
                $ctrl.counts = {};
            }
        }

        // LAYOUT

        $ctrl.showFilters = function () {
            var nominationStatus = $ctrl.searchParams.nominationStatus;

            return nominationStatus === 'KOULUTUS' ||
                nominationStatus === 'NIMITETTY' ||
                nominationStatus === 'HYLATTY' ||
                (nominationStatus === 'ESITETTY' && $ctrl.isModerator);
        };

        $ctrl.showPager = function () {
            var nominationStatus = $ctrl.searchParams.nominationStatus;

            return $ctrl.resultList && nominationStatus !== 'EHDOLLA' && nominationStatus !== 'ESITETTY';
        };

        $ctrl.showAddTraining = function () {
            return $ctrl.searchParams.nominationStatus === 'KOULUTUS';
        };

        $ctrl.showTrainingLocationFilter = function () {
            var nominationStatus = $ctrl.searchParams.nominationStatus;
            var searchType = $ctrl.searchParams.searchType;

            return nominationStatus === 'KOULUTUS' && searchType === 'TRAINING_LOCATION';
        };

        $ctrl.showOrganisationFilter = function () {
            var nominationStatus = $ctrl.searchParams.nominationStatus;
            var searchType = $ctrl.searchParams.searchType;

            if (nominationStatus === 'KOULUTUS') {
                return searchType === 'HOME_RHY' ||
                    ($ctrl.isModerator && searchType === 'PREVIOUS_OCCUPATION');
            }

            return nominationStatus === 'NIMITETTY' ||
                nominationStatus === 'HYLATTY' ||
                $ctrl.isModerator && nominationStatus === 'ESITETTY';
        };

        $ctrl.showPersonSearch = function () {
            var nominationStatus = $ctrl.searchParams.nominationStatus;
            var searchType = $ctrl.searchParams.searchType;

            return nominationStatus === 'KOULUTUS' && searchType === 'PERSON';
        };

        $ctrl.showDateFilter = function () {
            var nominationStatus = $ctrl.searchParams.nominationStatus;
            var searchType = $ctrl.searchParams.searchType;

            return nominationStatus === 'KOULUTUS' && searchType === 'HOME_RHY' ||
                nominationStatus === 'KOULUTUS' && searchType === 'TRAINING_LOCATION' ||
                $ctrl.isModerator;
        };

        $ctrl.getRowHeaderDate = function (row) {
            return row.trainingDate || row.decisionDate || row.nominationDate;
        };

        // FILTER

        $ctrl.search = function () {
            $ctrl.selectAllModel = false;

            OccupationNominationService.search($ctrl.searchParams).then(function (response) {
                $ctrl.resultList = response;
            });

            if ($ctrl.isModerator) {
                reloadCounts();
            }
        };

        $ctrl.reset = function () {
            $ctrl.searchParams = OccupationNominationService.resetSearchParameters($ctrl.searchParams, $ctrl.activeRhy);
        };

        $ctrl.onSearchTypeChange = function () {
            $ctrl.reset();
            $ctrl.resultList = null;
        };

        $ctrl.selectOccupationType = function (value) {
            $ctrl.searchParams.occupationType = value;
            $ctrl.resultList = null;

            if (!$ctrl.showFilters()) {
                $ctrl.search();
            }

            reloadCounts();
        };

        $ctrl.selectNominationStatus = function (value) {
            $ctrl.searchParams.nominationStatus = value;
            $ctrl.resultList = null;
            $ctrl.reset();
            $ctrl.search();
        };

        // SELECTION

        function getSelectedIds() {
            return $ctrl.resultList ? _.chain($ctrl.resultList.content)
                .filter('selected', true)
                .reject('person.underage')
                .reject('person.huntingBanActive')
                .uniq('person.id')
                .map('id')
                .value() : [];
        }

        $ctrl.selectAll = function (value) {
            _.each($ctrl.resultList.content, function (nomination) {
                nomination.selected = value;
            });
        };

        $ctrl.onRowSelected = function (value) {
            if (!value) {
                $ctrl.selectAllModel = false;
            }
        };

        // ACTIONS

        function createAction(repository, actionName, extraData) {
            return function (id) {
                return repository.doAction(_.assign({id: id, action: actionName}, extraData)).$promise;
            };
        }

        function confirmAction(requireConfirm) {
            if (!requireConfirm) {
                return $q.when();
            }

            var dialogTitle = $translate.instant('global.dialog.confirmation.title');
            var dialogMessage = $translate.instant('global.dialog.confirmation.text');
            var dialog = dialogs.confirm(dialogTitle, dialogMessage);

            return dialog.result;
        }

        function executeAction(requireConfirm, action, id) {
            var selectedIds = getSelectedIds();

            if (id === 'selected' && _.isEmpty(selectedIds)) {
                return;
            }

            confirmAction(requireConfirm).then(function () {
                if (id === 'selected') {
                    return $q.all(_.map(selectedIds, action));

                } else if (id) {
                    return action(id);

                } else {
                    return $q.reject('ignore');
                }
            }, function () {
                return $q.reject('ignore');

            }).then(
                NotificationService.showDefaultSuccess,
                function (result) {
                    if (result !== 'ignore')  {
                        NotificationService.showDefaultFailure();
                    }
                }
            ).finally(function () {
                $ctrl.search();
                reloadCounts();
            });
        }

        $ctrl.proposeTraining = !$ctrl.activeRhy ? _.noop
            : _.partial(executeAction, false, createAction(JHTTrainings, 'propose', {rhyId: $ctrl.activeRhy.id}));
        $ctrl.deleteTraining = _.partial(executeAction, true, function (id) {
            return JHTTrainings.delete({id: id}).$promise;
        });
        $ctrl.propose = _.partial(executeAction, false, createAction(OccupationNominations, 'propose'));
        $ctrl.cancel = _.partial(executeAction, true, createAction(OccupationNominations, 'cancel'));
        $ctrl.reject = _.partial(executeAction, true, createAction(OccupationNominations, 'reject'));

        $ctrl.accept = function (id) {
            if (id === 'selected' && _.isEmpty(getSelectedIds())) {
                return;
            }

            var modalPromise = OccupationNominationService.showNominationDialog({
                occupationType: $ctrl.searchParams.occupationType,
                // All result items have same RHY
                rhy: $ctrl.resultList.content[0].rhy
            });

            return NotificationService.handleModalPromise(modalPromise).then(function (result) {
                var acceptAction = createAction(OccupationNominations, 'accept', {
                    occupationPeriod: {
                        beginDate: result.beginDate,
                        endDate: result.endDate
                    }
                });

                executeAction(false, acceptAction, id);
            });
        };

        $ctrl.exportToExcel = function () {
            OccupationNominationService.exportToExcel($ctrl.searchParams);
        };

        $ctrl.addTraining = function () {
            var modalPromise = OccupationNominationService.showAddTrainingDialog({
                occupationType: $ctrl.searchParams.occupationType
            });

            return NotificationService.handleModalPromise(modalPromise);
        };
    }

    function OccupationNominationDialogController($uibModalInstance, Helpers,
                                                  params, occupationPeriod) {
        var $ctrl = this;

        $ctrl.occupationType = params.occupationType;
        $ctrl.beginDate = occupationPeriod.beginDate;
        $ctrl.endDate = occupationPeriod.endDate;
        $ctrl.rhy = params.rhy;

        $ctrl.beginDatePickerOptions = {
            minDate: new Date()
        };

        $ctrl.endDatePickerOptions = {
            minDate: new Date(),
            dateDisabled: isEndDateDisabled
        };

        function isHuntingYearEndDate(date) {
            return date && date.getMonth() === (7 - 1) && date.getDate() === 31;
        }

        function isEndDateDisabled(data) {
            return !(data.mode === 'day' && isHuntingYearEndDate(data.date));
        }

        $ctrl.isValidOccupationPeriod = function () {
            if ($ctrl.beginDate && $ctrl.endDate) {
                var beginDate = Helpers.toMoment($ctrl.beginDate);
                var endDate = Helpers.toMoment($ctrl.endDate);

                var minDuration = moment.duration('P4Y');
                var maxDuration = moment.duration('P5Y');

                var minDate = beginDate.clone().add(minDuration);
                var maxDate = beginDate.clone().add(maxDuration);

                return endDate.isAfter(minDate) && endDate.isBefore(maxDate);
            }

            return false;
        };

        $ctrl.submit = function () {
            $uibModalInstance.close({
                beginDate: $ctrl.beginDate,
                endDate: $ctrl.endDate
            });
        };

        $ctrl.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    }

    function AddTrainingDialogController($uibModalInstance, params,
                                         TranslatedBlockUI, OccupationFindPerson) {
        var $ctrl = this;

        $ctrl.occupationType = params.occupationType;
        $ctrl.trainingDate = params.trainingDate;
        $ctrl.trainingType = 'LAHI';
        $ctrl.trainingLocation = params.trainingLocation;

        $ctrl.searchPerson = {
            by: 'ssn',
            ssn: null,
            hunterNumber: null,
            error: false
        };

        $ctrl.datePickerOptions = {
            maxDate: new Date()
        };

        $ctrl.submit = function () {
            $uibModalInstance.close({
                occupationType: $ctrl.occupationType,
                person: {
                    id: $ctrl.person.id
                },
                trainingType: $ctrl.trainingType,
                trainingLocation: $ctrl.trainingLocation,
                trainingDate: $ctrl.trainingDate
            });
        };

        $ctrl.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };

        $ctrl.onAddPersonByChange = function () {
            $ctrl.person = null;
            $ctrl.searchPerson.error = false;
        };

        $ctrl.findPersonBySsn = decorateSearch(function () {
            return OccupationFindPerson.findBySsn($ctrl.searchPerson.ssn);
        });

        $ctrl.findPersonByHunterNumber = decorateSearch(function () {
            return OccupationFindPerson.findByHunterNumber($ctrl.searchPerson.hunterNumber);
        });

        function decorateSearch(searchMethod) {
            var ok = function (response) {
                $ctrl.person = response.data;
                $ctrl.searchPerson.error = false;
            };

            var nok = function () {
                $ctrl.person = null;
                $ctrl.searchPerson.error = true;
            };

            // prevent double clicking
            return _.debounce(function (search) {
                TranslatedBlockUI.start("global.block.wait");
                return searchMethod(search).then(ok, nok).finally(TranslatedBlockUI.stop);
            }, 500, true);
        }
    }
})();
