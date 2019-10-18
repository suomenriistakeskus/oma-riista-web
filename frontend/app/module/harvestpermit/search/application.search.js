'use strict';

angular.module('app.harvestpermit.search.application', [])
    .service('ModeratorPermitApplicationSearchFilters', function (LocalStorageService, AuthenticationService) {
        var initialFilters = {
            status: 'DRAFT',
            harvestPermitCategory: 'ALL',
            huntingYear: null,
            rkaOfficialCode: null,
            rhyOfficialCode: null
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
                    'protectedAreaType', 'derogationReason', 'forbiddenMethod'
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

            $ctrl.$onInit = function () {
                $ctrl.year = new Date().getFullYear();
                $ctrl.yearOptions = _.range(2018, $ctrl.year + 1);
                $ctrl.stats = $ctrl.loadStatistics($ctrl.year);
            };

            $ctrl.loadStatistics = function () {
                $http({
                    url: 'api/v1/harvestpermit/application/moderator/statustable/' + $ctrl.year
                }).then(function (response) {
                    $ctrl.stats = response.data;
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
                              ModeratorPermitApplicationSearchFilters, FormPostService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.filters = ModeratorPermitApplicationSearchFilters.load();
                $ctrl.results = null;

                if ($ctrl.tab) {
                    doSearch(0);
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
                    ModeratorPermitApplicationSearchFilters.save($ctrl.filters);

                }).finally(function () {
                    TranslatedBlockUI.stop();
                });
            }

            function search(page) {
                var searchParams = createSearchParams($ctrl.filters, page);

                if ($ctrl.tab === 'postalQueue') {
                    return HarvestPermitApplications.postalQueue().$promise.then(function (resultList) {
                        return {
                            content: resultList,
                            hasNext: false
                        };
                    });
                }

                return HarvestPermitApplications.search(searchParams).$promise;
            }

            function createSearchParams(f, page) {
                var params = {
                    // required
                    huntingYear: f.huntingYear,
                    status: f.status !== 'ALL' ? [f.status] : null,

                    // optional
                    gameSpeciesCode: f.gameSpeciesCode,
                    harvestPermitCategory: f.harvestPermitCategory === 'ALL' ? null : f.harvestPermitCategory,
                    rhyOfficialCode: f.rhyOfficialCode,
                    rkaOfficialCode: f.rkaOfficialCode,
                    applicationNumber: f.applicationNumber,
                    handlerId: f.handlerId,
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
                    || $ctrl.filters.derogationReason || $ctrl.filters.forbiddenMethod);
                $ctrl.statusList = ApplicationStatusList.all();
                $ctrl.statusList.unshift('ALL');
                $ctrl.availableSpecies = buildAvailableSpecies();
                $ctrl.permitCategoryList = PermitCategories;
                $ctrl.decisionTypeList = DecisionTypes;
                $ctrl.grantStatusList = DecisionGrantStatus;
                $ctrl.appealStatusList = AppealStatus;
                $ctrl.forbiddenMethodList = PermitDecisionForbiddenMethodType;
                $ctrl.protectedAreaTypeList = ProtectedAreaTypes;
                $ctrl.derogationReasonList = DerogationReasonType;

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
                    $ctrl.onFilterChange();
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
                var mooseSpecies = Species.getPermitBasedMooselike();
                var birdSpecies = Species.getBirdPermitSpecies();
                var speciesList = _([]).concat(mooseSpecies, birdSpecies).value();

                return _.chain(speciesList)
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
        controller: function ($state, $translate, PermitDecision) {
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
                if (application.harvestPermitCategory === 'MOOSELIKE' && application.submitDate) {
                    var submitDate = moment(application.submitDate, 'YYYY-MM-DD');

                    // TODO: Fix hard coded application period
                    if (submitDate.isValid()) {
                        var month = submitDate.month() + 1; // zero indexed
                        return month > 4; // Submitted after 30.4.
                    }
                }
                return false;
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
        }
    });
