'use strict';

angular.module('app.harvestpermit.search.application', [])
    .service('ModeratorPermitApplicationSearchFilters', function (LocalStorageService, AuthenticationService) {
        var initialFilters = {
            status: 'DRAFT',
            harvestPermitCategory: null,
            huntingYear: null,
            rkaOfficialCode: null,
            rhyOfficialCode: null,
            decisionLocale: null,
            pageNumber: 0
        };

        var self = this;

        self.currentFilters = null;

        this.load = function () {
            if (this.currentFilters === null) {
                this.currentFilters = createOrRestore();
            }

            return self.currentFilters;
        };

        this.save = function (value) {
            self.currentFilters = value;
            LocalStorageService.setKey('moderator-application-search-filters', JSON.stringify(value));
        };

        this.reset = function () {
            self.currentFilters = angular.copy(initialFilters);
            return self.currentFilters;
        };

        function createOrRestore() {
            var filters = angular.copy(initialFilters);
            var storedFiltersString = LocalStorageService.getKey('moderator-application-search-filters');

            if (_.isString(storedFiltersString) && storedFiltersString.length > 0) {
                var storedFilters = JSON.parse(storedFiltersString);

                return _.assign(filters, _.pick(storedFilters, [
                    'rhyOfficialCode', 'rkaOfficialCode', 'gameSpeciesCode',
                    'huntingYear', 'harvestPermitCategory', 'status',
                    'decisionType', 'grantStatus', 'appealStatus', 'handlerId',
                    'protectedAreaType', 'derogationReason', 'forbiddenMethod',
                    'validityYears', 'decisionLocale', 'pageNumber'
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
    .constant('ApplicationStatusList', {
        all: function () {
            return ['ACTIVE', 'DRAFT', 'AMENDING', 'LOCKED', 'PUBLISHED'];
        },
        decision: function () {
            return ['DRAFT', 'LOCKED', 'PUBLISHED'];
        }
    })
    .component('moderatorApplicationStatusTable', {
        templateUrl: 'harvestpermit/search/application-status-table.html',
        controller: function ($translate, $http, FormPostService) {
            var $ctrl = this;
            var PAGE_SIZE = 5;

            $ctrl.$onInit = function () {
                $ctrl.year = new Date().getFullYear();
                $ctrl.yearOptions = _.range(2018, $ctrl.year + 1);
                $ctrl.loadStatistics($ctrl.year);
                $ctrl.pageInfo = {
                    hasNext: true,
                    pageable: {
                        page: 0
                    }
                };
            };

            $ctrl.selectPage = function (page) {
                var start = PAGE_SIZE * page;
                var end = start + PAGE_SIZE;
                $ctrl.stats = _.map($ctrl.allStats, function (stat) {
                    return Object.assign({},
                        {rka: stat.rka},
                        {categoryStatuses: stat.categoryStatuses.slice(start, end)});
                });

                $ctrl.pageInfo.pageable.page = page;
                $ctrl.pageInfo.hasNext = end < $ctrl.allStats[0].categoryStatuses.length;
            };

            $ctrl.loadStatistics = function () {
                $http({
                    url: 'api/v1/harvestpermit/application/moderator/statustable/' + $ctrl.year
                }).then(function (response) {
                    $ctrl.allStats = response.data;
                    $ctrl.stats = _.map($ctrl.allStats, function (stat) {
                        return Object.assign({},
                            {rka: stat.rka},
                            {categoryStatuses: stat.categoryStatuses.slice(0, PAGE_SIZE)});
                    });
                });
            };

            $ctrl.exportToExcel = function () {
                FormPostService.submitFormUsingBlankTarget('api/v1/harvestpermit/application/moderator/excel/' + $ctrl.year);
            };

            $ctrl.getClass = function (amount) {
                if (amount >= 10) {
                    return 'danger';
                }
                if (amount >= 3) {
                    return 'warning';
                }
            };
        }
    })
    .component('moderatorApplicationSearch', {
        templateUrl: 'harvestpermit/search/search-applications.html',
        bindings: {
            handlers: '<',
            tab: '<'
        },
        controller: function (HarvestPermitApplications, HuntingYearService, TranslatedBlockUI,
                              ModeratorPermitApplicationSearchFilters, FormPostService, AuthenticationService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.filters = ModeratorPermitApplicationSearchFilters.load();
                $ctrl.results = null;
                $ctrl.mineAnnual = true;
                $ctrl.setShowMineAnnual = function (value) {
                    $ctrl.mineAnnual = value;
                    doSearch(0);
                };

                if ($ctrl.tab) {
                    doSearch($ctrl.filters.pageNumber);
                }

                $ctrl.$onChanges = function (changes) {
                    if (changes.tab) {
                        doSearch(0);
                    }
                };
            };

            $ctrl.reset = function () {
                var filters = ModeratorPermitApplicationSearchFilters.reset();
                $ctrl.filters = filters;
                return filters;
            };

            $ctrl.loadPage = function (page) {
                doSearch(page);
            };

            $ctrl.exportResultsToExcel = function () {
                var searchParams = createSearchParams($ctrl.filters);
                FormPostService.submitFormUsingBlankTarget(
                    'api/v1/harvestpermit/application/search/excel',
                    {json: angular.toJson(searchParams)});
            };

            function doSearch(page) {
                TranslatedBlockUI.start('global.block.wait');

                $ctrl.results = null;

                search(page).then(function (res) {
                    $ctrl.results = res;
                    $ctrl.filters.pageNumber = page;
                    ModeratorPermitApplicationSearchFilters.save($ctrl.filters);

                }).finally(function () {
                    TranslatedBlockUI.stop();
                });
            }

            function search(page) {

                if ($ctrl.tab === 'postalQueue') {
                    return HarvestPermitApplications.postalQueue().$promise.then(function (resultList) {
                        return {
                            content: resultList,
                            hasNext: false
                        };
                    });
                }

                if ($ctrl.tab === 'annualRenewals') {
                    var handlerId = $ctrl.mineAnnual ? _.get(AuthenticationService.getAuthentication(), 'id') : null;
                    return HarvestPermitApplications.annualRenewals({handlerId: handlerId}).$promise.then(function (resultList) {
                        return {
                            content: resultList,
                            hasNext: false
                        };
                    });
                }

                var searchParams = createSearchParams($ctrl.filters, page);
                return HarvestPermitApplications.search(searchParams).$promise;
            }

            function createSearchParams(f, page) {
                var params = {
                    // required
                    huntingYear: f.huntingYear,
                    status: f.status !== 'ALL' ? [f.status] : null,

                    // optional
                    gameSpeciesCode: f.gameSpeciesCode,
                    // Deprecated value 'ALL' might be in local storage, so, it needs to be handled too.
                    harvestPermitCategory: f.harvestPermitCategory === 'ALL' ? null : f.harvestPermitCategory,
                    validityYears: f.validityYears,
                    rhyOfficialCode: f.rhyOfficialCode,
                    rkaOfficialCode: f.rkaOfficialCode,
                    applicationNumber: f.applicationNumber,
                    handlerId: f.handlerId,
                    decisionLocale: f.decisionLocale,
                    decisionType: f.decisionType ? [f.decisionType] : null,
                    grantStatus: f.grantStatus ? [f.grantStatus] : null,
                    appealStatus: f.appealStatus ? [f.appealStatus] : null,
                    protectedArea: f.protectedArea ? [f.protectedArea] : null,
                    derogationReason: f.derogationReason ? [f.derogationReason] : null,
                    forbiddenMethod: f.forbiddenMethod ? [f.forbiddenMethod] : null
                };

                if (page || page === 0) {
                    params = _.assign(params, {page: page, size: 10});
                }

                return params;
            }
        }
    })
    .component('moderatorApplicationSearchFilters', {
        templateUrl: 'harvestpermit/search/search-application-filters.html',
        bindings: {
            filters: '<',
            handlers: '<',
            search: '&',
            reset: '&'
        },
        controller: function ($scope, AuthenticationService,
                              PermitCategories, DecisionTypes, AppealStatus, DecisionGrantStatus,
                              ProtectedAreaTypes, DerogationReasonType, PermitDecisionForbiddenMethodType,
                              HuntingYearService, Species, TranslatedSpecies, ApplicationStatusList) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.filterMode = getCurrentFilterMode();
                $ctrl.collapseAdditionalFilters = !($ctrl.filters.gameSpeciesCode || $ctrl.filters.decisionType
                    || $ctrl.filters.grantStatus || $ctrl.filters.appealStatus || $ctrl.filters.protectedArea
                    || $ctrl.filters.derogationReason || $ctrl.filters.forbiddenMethod
                    || !_.isNil($ctrl.filters.validityYears));
                $ctrl.statusList = ApplicationStatusList.all();
                $ctrl.statusList.unshift('ALL');
                $ctrl.availableSpecies = buildAvailableSpecies();
                $ctrl.validityYearsList = _.range(0, 5 + 1); // End exclusive -> +1
                $ctrl.permitCategoryList = PermitCategories;
                $ctrl.decisionTypeList = DecisionTypes;
                $ctrl.grantStatusList = DecisionGrantStatus;
                $ctrl.appealStatusList = AppealStatus;
                $ctrl.forbiddenMethodList = PermitDecisionForbiddenMethodType;
                $ctrl.protectedAreaTypeList = ProtectedAreaTypes;
                $ctrl.derogationReasonList = DerogationReasonType;
                $ctrl.languages = ['fi', 'sv'];

                var endYear = new Date().getFullYear();
                var beginYear = Math.max(endYear - 5, 2018);

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
                    $ctrl.reloadValues();
                }
            };

            $ctrl.showMine = function () {
                var filters = $ctrl.reset();
                filters.handlerId = getActiveUserId();
                filters.status = 'DRAFT';
            };

            $ctrl.showOther = function () {
                var filters = $ctrl.reset();
                filters.handlerId = null;
                filters.status = 'ACTIVE';
            };

            $ctrl.onFilterChange = function () {
                $ctrl.filters.pageNumber = 0;
                $ctrl.reloadValues();
            };

            $ctrl.reloadValues = function () {
                $ctrl.filterMode = getCurrentFilterMode();
                $ctrl.search();
            };

            function getCurrentFilterMode() {
                return $ctrl.filters.handlerId === getActiveUserId() ? 'mine' : 'other';
            }

            function getActiveUserId() {
                return _.get(AuthenticationService.getAuthentication(), 'id');
            }

            function buildAvailableSpecies() {
                return _.chain(Species.getSpeciesMapping())
                    .map(function (species) {
                        return TranslatedSpecies.translateSpecies(species);
                    })
                    .sortBy('name')
                    .value();
            }
        }
    })
    .component('moderatorApplicationResultsList', {
        templateUrl: 'harvestpermit/search/search-application-list.html',
        bindings: {
            results: '<'
        },
        controller: function ($state, $translate, PermitDecision, HarvestPermitCategoryType, MooselikeApplicationLate) {
            var $ctrl = this;

            $ctrl.openApplication = function (application) {
                PermitDecision.save({
                    applicationId: application.id
                }).$promise.then(function (res) {
                    $state.go('jht.decision.application.overview', {decisionId: res.id});
                });
            };

            $ctrl.getApplicationName = function (application) {
                return $translate.instant('harvestpermit.wizard.summary.permitCategory.' + application.harvestPermitCategory);
            };

            $ctrl.isLateApplication = function (application) {
                return application.harvestPermitCategory === 'MOOSELIKE' &&
                    MooselikeApplicationLate.isLate(application.huntingYear, application.submitDate);
            };

            $ctrl.resolveUnifiedStatus = function (application) {
                if (application.status === 'AMENDING') {
                    return application.status;
                }

                if (application.status === 'ACTIVE' && !application.handler) {
                    return 'ACTIVE';
                }

                return application.decisionStatus;
            };

            $ctrl.hasPermission = function (application) {
                return HarvestPermitCategoryType.hasPermission(application.harvestPermitCategory);
            };
        }
    });
