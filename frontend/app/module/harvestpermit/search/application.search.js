'use strict';

angular.module('app.harvestpermit.search.application', [])
    .constant('ModeratorPermitApplicationSearchFilters', {
        huntingYear: null,
        rkaOfficialCode: null,
        rhyOfficialCode: null,
        initialPageLoad: true
    })
    .constant('ApplicationStatusList', {
        all: function () {
            return ['ACTIVE', 'DRAFT', 'AMENDING', 'LOCKED', 'PUBLISHED'];
        },
        decision: function () {
            return ['DRAFT', 'LOCKED', 'PUBLISHED'];
        }
    })
    .component('moderatorApplicationSearch', {
        templateUrl: 'harvestpermit/search/search-applications.html',
        bindings: {
            availableSpecies: '<',
            handlers: '<'
        },
        controller: function (HarvestPermitApplications, HuntingYearService,
                              ModeratorPermitApplicationSearchFilters) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.tab = 'filter';
                $ctrl.filters = null;
                $ctrl.results = null;
                $ctrl.showFilters();
            };

            $ctrl.search = function () {
                if ($ctrl.tab === 'filter') {
                    var f = angular.copy($ctrl.filters);
                    if (f.statusText) {
                        f.status = [f.statusText];
                    } else {
                        f.status = null;
                    }
                    delete f.statusText;
                    HarvestPermitApplications.search(f).$promise.then(function (res) {
                        $ctrl.results = res;
                    });
                } else if ($ctrl.tab === 'myApplications') {
                    HarvestPermitApplications.assignedApplications().$promise.then(function (res) {
                        $ctrl.results = res;
                    });
                } else if ($ctrl.tab === 'myDecisions') {
                    HarvestPermitApplications.assignedDecisions().$promise.then(function (res) {
                        $ctrl.results = res;
                    });
                } else if ($ctrl.tab === 'postalQueue') {
                    HarvestPermitApplications.postalQueue().$promise.then(function (res) {
                        $ctrl.results = res;
                    });
                }
            };

            $ctrl.showFilters = function () {
                $ctrl.tab = 'filter';
                $ctrl.filters = $ctrl.filters || ModeratorPermitApplicationSearchFilters;
                if (!$ctrl.filters.huntingYear) {
                    $ctrl.filters.huntingYear = new Date().getFullYear();
                }

                if ($ctrl.filters.initialPageLoad) {
                    delete $ctrl.filters.initialPageLoad;
                } else {
                    $ctrl.search();
                }
            };

            $ctrl.showMyApplications = function () {
                $ctrl.tab = 'myApplications';
                $ctrl.search();
            };

            $ctrl.showMyDecisions = function () {
                $ctrl.tab = 'myDecisions';
                $ctrl.search();
            };

            $ctrl.showPostalQueue = function () {
                $ctrl.tab = 'postalQueue';
                $ctrl.search();
            };
        }
    })
    .component('moderatorApplicationSearchFilters', {
        templateUrl: 'harvestpermit/search/search-application-filters.html',
        bindings: {
            filters: '<',
            availableSpecies: '<',
            handlers: '<',
            search: '&'
        },
        controller: function (HuntingYearService, ApplicationStatusList) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.statusList = ApplicationStatusList.all();
                var beginYear = 2018;
                var endYear = new Date().getFullYear();
                var years = _.range(beginYear, endYear + 1);

                $ctrl.huntingYears = _.map(years, function (year) {
                    return HuntingYearService.toObj(year);
                });
            };
        }
    })
    .component('moderatorApplicationResultsList', {
        templateUrl: 'harvestpermit/search/search-application-list.html',
        bindings: {
            results: '<'
        },
        controller: function ($state, PermitDecision) {
            var $ctrl = this;

            $ctrl.openApplication = function (application) {
                PermitDecision.save({
                    applicationId: application.id
                }).$promise.then(function (res) {
                    $state.go('jht.decision.application.summary', {decisionId: res.id});
                });
            };

            $ctrl.getApplicationName = function (application) {
                // TODO:
                return application.permitTypeCode === '100'
                    ? 'Hirvieläinten lupahakemus'
                    : application.permitTypeCode;
            };

            $ctrl.isLateApplication = function (application) {
                if (application.submitDate) {
                    var submitDate = moment(application.submitDate, 'YYYY-MM-DD');

                    if (submitDate.isValid()) {
                        var month = submitDate.month() + 1; // zero indexed
                        return month > 4; // Submitted after 30.4
                    }
                }
                return false;
            };

            $ctrl.resolveUnifiedStatus = function (application) {
                if (application.status === 'AMENDING') {
                    return 'AMENDING';
                }

                if (application.status === 'ACTIVE' && !application.handler) {
                    return 'ACTIVE';
                }
                return application.decisionStatus;
            };
        }
    });
