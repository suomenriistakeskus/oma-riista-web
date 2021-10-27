'use strict';

angular.module('app.jht.nominationdecision', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('jht.nominationdecisions', {
                url: '/nominationdecision?tab',
                reloadOnSearch: false,
                templateUrl: 'jht/nominationdecision/nominationdecisions.html',
                resolve: {
                    handlers: function (NominationDecisionSearch) {
                        return NominationDecisionSearch.listHandlers().$promise;
                    }
                },
                controllerAs: '$ctrl',
                controller: function ($location, $state, CreateNominationDecisionModal, NominationDecision, handlers) {
                    var $ctrl = this;

                    $ctrl.$onInit = function () {
                        $ctrl.handlers = handlers;
                        $ctrl.showTab('filter');
                    };

                    $ctrl.createDecision = function () {
                        CreateNominationDecisionModal.open().then(function (createParams) {
                            NominationDecision.create(createParams).$promise.then(function (returnResult) {
                                $state.go('jht.nominationdecision.overview', {decisionId: returnResult.id});
                            });
                        }, function (error) {
                            // Do nothing
                        });
                    };

                    $ctrl.showTab = function (tab) {
                        $ctrl.tab = tab;
                        $location.search({tab: tab});
                    };
                }
            })

            .state('jht.nominationdecision', {
                url: '/nominationdecision/{decisionId:[0-9]{1,8}}',
                templateUrl: 'jht/nominationdecision/layout.html',
                abstract: true,
                resolve: {
                    decisionId: function ($stateParams) {
                        return _.parseInt($stateParams.decisionId);
                    },
                }
            });
    })

    .factory('NominationDecision', function ($http, $resource) {
        var apiPrefix = 'api/v1/nominationdecision/:id';
        return $resource(apiPrefix, {id: '@id', spaId: '@spaId'}, {
            create: {
                method: 'POST'
            },
            getDocument: {method: 'GET', url: apiPrefix + '/document'},
            updateDocument: {method: 'PUT', url: apiPrefix + '/document'},
            generateText: {
                method: 'GET',
                url: apiPrefix + '/generate/:sectionId',
                params: {id: '@id', sectionId: '@sectionId'}
            },
            generateAndPersistText: {
                method: 'POST',
                url: apiPrefix + '/generate/:sectionId',
                params: {id: '@id', sectionId: '@sectionId'}
            },
            getCompleteStatus: {method: 'GET', url: apiPrefix + '/complete'},
            updateCompleteStatus: {method: 'PUT', url: apiPrefix + '/complete'},
            getPaymentOptions: {method: 'GET', url: apiPrefix + '/payment', isArray: true},
            updatePayment: {method: 'PUT', url: apiPrefix + '/payment'},
            getReference: {method: 'GET', url: apiPrefix + '/reference'},
            updateReference: {method: 'PUT', url: apiPrefix + '/reference'},
            searchReferences: {method: 'POST', url: apiPrefix + '/search/references'},
            getAuthorities: {method: 'GET', url: apiPrefix + '/authorities'},
            updateAuthorities: {method: 'POST', url: apiPrefix + '/authorities'},
            getAttachments: {method: 'GET', url: apiPrefix + '/attachment', isArray: true},
            updateAttachmentOrder: {method: 'PUT', url: apiPrefix + '/attachment-order'},
            getDeliveries: {method: 'GET', url: apiPrefix + '/delivery', isArray: true},
            updateDeliveries: {method: 'POST', url: apiPrefix + '/delivery'},
            getDocumentSettings: {method: 'GET', url: apiPrefix + '/document-settings'},
            updateDocumentSettings: {method: 'PUT', url: apiPrefix + '/document-settings'},
            getPublishSettings: {method: 'GET', url: apiPrefix + '/publish-settings'},
            updatePublishSettings: {method: 'PUT', url: apiPrefix + '/publish-settings'},
            getAppealSettings: {method: 'GET', url: apiPrefix + '/appeal-settings'},
            updateAppealSettings: {method: 'PUT', url: apiPrefix + '/appeal-settings'},
            lock: {method: 'POST', url: apiPrefix + '/lock'},
            unlock: {method: 'POST', url: apiPrefix + '/unlock'},
            getRevisions: {method: 'GET', url: apiPrefix + '/revisions', isArray: true},
            updateProposalDate: {method: 'POST', url: apiPrefix + '/proposal-date', isArray: true},
            updatePosted: {
                method: 'POST',
                url: apiPrefix + '/revisions/:revisionId/posted',
                params: {id: '@id', revisionId: '@revisionId'}
            },
            updateNotPosted: {
                method: 'POST',
                url: apiPrefix + '/revisions/:revisionId/notposted',
                params: {id: '@id', revisionId: '@revisionId'}
            },
            assign: {method: 'POST', url: apiPrefix + '/assign'},
            unassign: {method: 'POST', url: apiPrefix + '/unassign'}
        });
    })

    .factory('NominationDecisionSearch', function ($http, $resource) {
        var apiPrefix = 'api/v1/nominationdecision/search';
        return $resource(apiPrefix, {}, {
            search: {
                method: 'POST'
            },
            create: {
                method: 'POST'
            },
            listHandlers: {
                method: 'GET',
                isArray: true,
                url: apiPrefix + '/handlers'
            },
            getStatistics: {
                method: 'GET',
                isArray: true,
                url: apiPrefix + '/statistics/:year',
                params: {year: '@year'}}
        });
    })

    .factory('NominationDecisionRkaAuthority', function ($resource) {
        var apiPrefix = 'api/v1/nominationdecision/rkaauthority/:id';

        return $resource(apiPrefix, {id: '@id'}, {
            listByNominationDecision: {method: 'GET', url: apiPrefix + '/nominationdecision/:decisionId', isArray: true}
        });
    })

    .service('CreateNominationDecisionModal', function ($uibModal) {
        this.open = function () {
            return $uibModal.open({
                templateUrl: 'jht/nominationdecision/create-modal.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'md',
                resolve: {
                    organisations: function (OrganisationsByArea) {
                        return OrganisationsByArea.queryActive().$promise;
                    }
                }
            }).result;
        };

        function ModalController($uibModalInstance, $translate, NominationDecisionTypes, JHTOccupationTypes, organisations) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.DECISION_TYPES = NominationDecisionTypes;
                $ctrl.OCCUPATION_TYPES = JHTOccupationTypes;
                $ctrl.selectedDecisionType = _.first($ctrl.DECISION_TYPES);
                $ctrl.selectedOccupationType = _.first($ctrl.OCCUPATION_TYPES);

                $ctrl.areas = organisations;
                $ctrl.selectedArea = _.first($ctrl.areas);
                $ctrl.selectedRhy = null;
                $ctrl.selectedLocale = 'fi_FI';
            };

            $ctrl.isInvalid = function (form) {
                return form.$invalid || $ctrl.selectedRhy === null;
            };

            $ctrl.save = function () {
                $uibModalInstance.close({
                    nominationDecisionType: $ctrl.selectedDecisionType,
                    occupationType: $ctrl.selectedOccupationType,
                    rhyCode: $ctrl.selectedRhy.officialCode,
                    locale: $ctrl.selectedLocale
                });
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss();
            };
        }
    })

    .service('ModeratorNominationDecisionSearchFilters', function (LocalStorageService, AuthenticationService) {
        var initialFilters = {
            status: 'DRAFT',
            decisionYear: null,
            rkaOfficialCode: null,
            rhyOfficialCode: null
        };

        var self = this;

        self.currentFilters = null;

        self.load = function () {
            if (self.currentFilters === null) {
                self.currentFilters = createOrRestore();
            }

            return self.currentFilters;
        };

        self.save = function (value) {
            self.currentFilters = value;
            LocalStorageService.setKey('moderator-nominationdecision-search-filters', JSON.stringify(value));
        };

        self.reset = function () {
            self.currentFilters = angular.copy(initialFilters);
            return self.currentFilters;
        };

        function createOrRestore() {
            var filters = angular.copy(initialFilters);
            var storedFiltersString = LocalStorageService.getKey('moderator-nominationdecision-search-filters');

            if (_.isString(storedFiltersString) && storedFiltersString.length > 0) {
                var storedFilters = JSON.parse(storedFiltersString);

                return _.assign(filters, _.pick(storedFilters, [
                    'rhyOfficialCode', 'rkaOfficialCode',
                    'decisionYear', 'status',
                    'decisionType', 'occupationType', 'appealStatus', 'handlerId'
                ]));

            } else {
                filters.handlerId = getActiveUserId();

                return filters;
            }
        }

        function getActiveUserId() {
            return _.get(AuthenticationService.getAuthentication(), 'id');
        }
    })
    .constant('NominationDecisionStatuses',
        ['ALL', 'DRAFT', 'LOCKED', 'PUBLISHED'])

    .constant('NominationDecisionTypes',
        ['NOMINATION', 'NOMINATION_CANCELLATION'])

    .component('moderatorNominationdecisionSearch', {
        templateUrl: 'jht/nominationdecision/search-decisions.html',
        bindings: {
            handlers: '<',
            tab: '<'
        },
        controller: function (FetchAndSaveBlob, NominationDecision, NominationDecisionSearch,
                              ModeratorNominationDecisionSearchFilters, TranslatedBlockUI) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.filters = ModeratorNominationDecisionSearchFilters.load();
                $ctrl.results = null;
                $ctrl.loadFirstPage();

                $ctrl.$onChanges = function (changes) {
                    if (changes.tab) {
                        $ctrl.loadFirstPage();
                    }
                };
            };

            $ctrl.reset = function () {
                var filters = ModeratorNominationDecisionSearchFilters.reset();
                $ctrl.filters = filters;
                return filters;
            };

            $ctrl.loadFirstPage = function () {
                doSearch(0);
            };

            $ctrl.loadPage = function (page) {
                doSearch(page);
            };

            $ctrl.exportResultsToExcel = function () {
                var searchParams = createSearchParams($ctrl.filters);
                FetchAndSaveBlob.post(
                    '/api/v1/nominationdecision/search/excel', searchParams);
            };

            function doSearch(page) {
                TranslatedBlockUI.start('global.block.wait');

                $ctrl.results = null;

                search(page).then(function (res) {
                    $ctrl.results = res;
                    ModeratorNominationDecisionSearchFilters.save($ctrl.filters);

                }).finally(function () {
                    TranslatedBlockUI.stop();
                });
            }

            function search(page) {
                var searchParams = createSearchParams($ctrl.filters, page);
                return NominationDecisionSearch.search(searchParams).$promise;
            }

            function createSearchParams(f, page) {
                var params = {
                    // required
                    year: f.decisionYear,
                    statuses: f.status !== 'ALL' ? [f.status] : null,

                    rhyOfficialCode: f.rhyOfficialCode,
                    rkaOfficialCode: f.rkaOfficialCode,
                    decisionNumber: f.decisionNumber,
                    handlerId: f.handlerId,
                    decisionTypes: f.decisionType ? [f.decisionType] : null,
                    occupationTypes: f.occupationType ? [f.occupationType] : null,
                    appealStatuses: f.appealStatus ? [f.appealStatus] : null
                };

                // Apply paging parameters if page number defined
                if (_.isFinite(page)) {
                    params = _.assign(params, {page: page, size: 10});
                }

                return params;
            }
        }
    })
    .component('moderatorNominationDecisionSearchFilters', {
        templateUrl: 'jht/nominationdecision/search-decision-filters.html',
        bindings: {
            filters: '<',
            handlers: '<',
            search: '&',
            reset: '&'
        },
        controller: function ($scope, AuthenticationService,
                              NominationDecisionTypes, JHTOccupationTypes,
                              AppealStatus, NominationDecisionStatuses) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.filterMode = getCurrentFilterMode();
                $ctrl.collapseAdditionalFilters = !($ctrl.filters.decisionType || $ctrl.filters.appealStatus);

                $ctrl.statusList = NominationDecisionStatuses;
                $ctrl.decisionTypeList = NominationDecisionTypes;
                $ctrl.occupationTypes = JHTOccupationTypes;
                $ctrl.appealStatusList = AppealStatus;
                $ctrl.activeUserId = _.get(AuthenticationService.getAuthentication(), 'id');

                var endYear = new Date().getFullYear();
                var beginYear = Math.max(endYear - 5, 2020);

                $ctrl.availableYears = _.range(beginYear, endYear + 1);

                $scope.$watchGroup(['$ctrl.filters.rhyOfficialCode', '$ctrl.filters.rkaOfficialCode'], function (newValues, oldValues) {
                    if (newValues[0] !== oldValues[0] ||
                        newValues[1] !== oldValues[1]) {
                        $ctrl.onFilterChange();
                    }
                });
            };

            $ctrl.$onChanges = function (changes) {
                if (changes.filters) {
                    $ctrl.onFilterChange();
                }
            };

            $ctrl.showMine = function () {
                var filters = $ctrl.reset();
                filters.handlerId = $ctrl.activeUserId;
                filters.status = 'DRAFT';
            };

            $ctrl.showOther = function () {
                var filters = $ctrl.reset();
                filters.handlerId = null;
                filters.status = 'DRAFT';
            };

            $ctrl.onFilterChange = function () {
                $ctrl.filterMode = getCurrentFilterMode();
                $ctrl.search();
            };

            function getCurrentFilterMode() {
                return $ctrl.filters.handlerId === $ctrl.activeUserId ? 'mine' : 'other';
            }
        }
    })
    .component('moderatorNominationdecisionResultsList', {
        templateUrl: 'jht/nominationdecision/search-decisions-list.html',
        bindings: {
            results: '<'
        },
        controllerAs: '$ctrl',
        controller: function ($state) {
            var $ctrl = this;

            $ctrl.openDecision = function (decision) {
                $state.go('jht.nominationdecision.overview', {decisionId: decision.id});
            };
        }
    })
    .component('moderatorNominationDecisionStatusTable', {
        templateUrl:'jht/nominationdecision/status-table.html',
        controller: function (NominationDecisionSearch) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.year = new Date().getFullYear();
                $ctrl.yearOptions = _.range(2020, $ctrl.year + 1);
                $ctrl.data = [];
                $ctrl.loadStatistics($ctrl.year);
            };

            $ctrl.loadStatistics = function () {
                NominationDecisionSearch.getStatistics({year: $ctrl.year}).$promise.then(function (response) {
                    $ctrl.data = response;
                });

            };
        }
    });
