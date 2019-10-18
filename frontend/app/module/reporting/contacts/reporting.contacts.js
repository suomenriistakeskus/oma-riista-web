'use strict';

angular.module('app.reporting.contacts', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('reporting.contacts', {
                abstract: true,
                url: '/contacts',
                templateUrl: 'reporting/contacts/layout.html'
            })
            .state('reporting.contacts.occupation', {
                url: '/occupation',
                templateUrl: 'reporting/contacts/occupations.html',
                controller: 'OccupationContactsSearchController',
                resolve: {
                    allOccupationTypes: function (OccupationTypes) {
                        return OccupationTypes.query().$promise;
                    },
                    areas: function (Areas) {
                        return Areas.query().$promise;
                    }
                }
            })
            .state('reporting.contacts.rhy', {
                url: '/rhy',
                templateUrl: 'reporting/contacts/organisations.html',
                controller: 'RhyContactsSearchController',
                resolve: {
                    allOccupationTypes: function (OccupationTypes) {
                        return OccupationTypes.query().$promise;
                    },
                    areas: function (Areas) {
                        return Areas.query().$promise;
                    }
                }
            });
    })

    .factory('OccupationContactSearch', function ($resource) {
        return $resource('api/v1/contactsearch/occupation', {}, {
            search: {
                method: 'POST',
                isArray: true
            }
        });
    })
    .factory('RhyContactSearch', function ($resource) {
        return $resource('api/v1/contactsearch/rhy', {}, {
            search: {
                method: 'POST',
                isArray: true
            }
        });
    })

    .controller('OccupationContactsSearchController', function ($scope, Areas, OccupationContactSearch,
                                                                areas, allOccupationTypes) {
        var orgs = [];

        var addOrgOccupations = function (orgs, orgType, areas, rhySelection) {
            if (allOccupationTypes[orgType] && allOccupationTypes[orgType].length > 0) {
                orgs.push({
                    type: orgType,
                    areas: areas,
                    occupationTypes: allOccupationTypes[orgType],
                    rhySelection: rhySelection
                });
            }
        };

        addOrgOccupations(orgs, 'RK', undefined);
        addOrgOccupations(orgs, 'VRN', undefined);
        addOrgOccupations(orgs, 'ARN', areas);
        addOrgOccupations(orgs, 'RKA', areas);
        addOrgOccupations(orgs, 'RHY', areas, true);

        $scope.orgs = orgs;
        $scope.opts = [
            {org: orgs[0], area: undefined, rhy: undefined, occupationType: undefined}
        ];

        $scope.addOpt = function (opt) {
            var newOpt = {org: opt.org, area: opt.area, rhy: opt.rhy, occupationType: undefined};
            var i = $scope.opts.indexOf(opt);
            $scope.opts.splice(i + 1, 0, newOpt);
        };
        var remove = function (from, item) {
            return _.filter(from, function (p) {
                return p !== item;
            });
        };
        $scope.removeOpt = function (opt) {
            if ($scope.opts.length > 1) {
                $scope.opts = remove($scope.opts, opt);
            }
        };
        $scope.clearOpt = function (opts) {
            opts.area = undefined;
            opts.rhy = undefined;
            opts.occupationType = undefined;
        };

        $scope.getCount = function () {
            if ($scope.pager) {
                return {count: $scope.pager.total};
            }
        };

        var updatePager = function () {
            if (!$scope.pager) {
                return;
            }
            var page = $scope.pager.currentPage - 1;
            var begin = page * $scope.pager.pageSize;
            var end = begin + $scope.pager.pageSize;
            $scope.page = $scope.pager.data.slice(begin, end);
        };
        $scope.$watch('pager.currentPage', function () {
            updatePager();
        });

        $scope.submit = function () {
            var data = [];
            _.forEach($scope.opts, function (opt) {
                var areaCode = opt.area ? opt.area.officialCode : undefined;
                var rhyCode = opt.rhy ? opt.rhy.officialCode : undefined;
                var v = {
                    organisationType: opt.org.type,
                    areaCode: areaCode,
                    rhyCode: rhyCode,
                    occupationType: opt.occupationType
                };
                data.push(v);
            });

            // Fill-in form submit data for Excel export file generation
            $scope.postData = angular.toJson(data);

            OccupationContactSearch.search(data).$promise.then(function (data) {
                $scope.pager = {
                    currentPage: 1,
                    pageSize: 100,
                    total: data.length,
                    data: data
                };
                updatePager();

            }).catch(function (data, status, headers, config) {
                console.log('error', data);
            });
        };
    })

    .controller('RhyContactsSearchController', function ($scope, Areas, RhyContactSearch, areas) {
        var orgs = [];

        var addOrgOccupations = function (orgs, orgType, areas, rhySelection) {
            orgs.push({type: orgType, areas: areas, rhySelection: rhySelection});
        };

        addOrgOccupations(orgs, 'RHY', areas, true);

        $scope.orgs = orgs;
        $scope.opts = [
            {org: orgs[0], area: undefined, rhy: undefined}
        ];

        $scope.addOpt = function (opt) {
            var newOpt = {org: opt.org, area: opt.area, rhy: opt.rhy};
            var i = $scope.opts.indexOf(opt);
            $scope.opts.splice(i + 1, 0, newOpt);
        };
        var remove = function (from, item) {
            return _.filter(from, function (p) {
                return p !== item;
            });
        };
        $scope.removeOpt = function (opt) {
            if ($scope.opts.length > 1) {
                $scope.opts = remove($scope.opts, opt);
            }
        };
        $scope.clearOpt = function (opts) {
            opts.area = undefined;
            opts.rhy = undefined;
        };

        $scope.getCount = function () {
            if ($scope.pager) {
                return {count: $scope.pager.total};
            }
        };

        var updatePager = function () {
            if (!$scope.pager) {
                return;
            }
            var page = $scope.pager.currentPage - 1;
            var begin = page * $scope.pager.pageSize;
            var end = begin + $scope.pager.pageSize;
            $scope.page = $scope.pager.data.slice(begin, end);
        };
        $scope.$watch('pager.currentPage', function () {
            updatePager();
        });

        $scope.submit = function () {
            var data = [];
            _.forEach($scope.opts, function (opt) {
                var areaCode = opt.area ? opt.area.officialCode : undefined;
                var rhyCode = opt.rhy ? opt.rhy.officialCode : undefined;
                var v = {areaCode: areaCode, rhyCode: rhyCode};
                data.push(v);
            });

            // Fill-in form submit data for Excel export file generation
            $scope.postData = angular.toJson(data);

            RhyContactSearch.search(data).$promise.then(function (data) {
                $scope.pager = {
                    currentPage: 1,
                    pageSize: 100,
                    total: data.length,
                    data: data
                };
                updatePager();

            }).catch(function (data, status, headers, config) {
                console.log('error', data);
            });
        };
    })

    .controller('CopyEmailsController', function ($uibModal) {
        var $ctrl = this;

        var nonEmptyStr = function (s) {
            return s && s.length > 0;
        };
        var extractEmails = function (data, emailAttributeName) {
            if (!angular.isString(emailAttributeName)) {
                emailAttributeName = 'email';
            }
            return _(data).map(emailAttributeName).filter(nonEmptyStr).uniq().value().join('; ');
        };
        var showEmails = function (data, emailAttributeName) {
            var emails = extractEmails(data, emailAttributeName);

            $uibModal.open({
                templateUrl: 'reporting/contacts/copyemails.html',
                controller: function ($scope) {
                    $scope.emails = emails;
                }
            });
        };
        $ctrl.copyEmails = function (data, emailAttributeName) {
            showEmails(data, emailAttributeName);
        };
    });
