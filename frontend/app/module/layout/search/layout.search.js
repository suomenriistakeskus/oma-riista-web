(function () {
    'use strict';

    angular.module('app.layout.search', [])
        .service('SiteSearchService', SiteSearchService)
        .directive('navSearch', function () {
            return {
                restrict: 'E',
                replace: true,
                scope: true,
                bindToController: true,
                controllerAs: '$ctrl',
                templateUrl: 'layout/search/nav-search.html',
                controller: SiteSearchController
            };
        });

    function SiteSearchService($state, LocalStorageService) {
        var self = this;
        var activeSearch = null;

        this.getActiveSearch = function () {
            if (activeSearch) {
                return activeSearch;
            } else if (LocalStorageService.getKey('activeSearch')) {
                self.selectActiveSearch(angular.fromJson(LocalStorageService.getKey('activeSearch')));
                return activeSearch;
            } else {
                return {};
            }
        };

        this.selectActiveSearch = function (search, doNotStore) {
            if (!search || !search.state) {
                return;
            }

            activeSearch = search;

            if (search && !doNotStore) {
                LocalStorageService.setKey('activeSearch', angular.toJson(search));
            }

            $state.go(search.state, search.stateParams);

            return search;
        };

        this.handleSelection = function (model) {
            var type = model.category;
            var id = model.id;
            var search = {title: model.description, id: id};
            if (type === 'PERSON') {
                search.state = 'profile.account';
                search.stateParams = {id: id};
            } else if (type === 'RHY') {
                search.state = 'rhy.show';
                search.stateParams = {id: id};
            } else if (type === 'ORG') {
                search.state = 'organisation.show';
                search.stateParams = {id: id};
            } else if (type === 'CLUB') {
                search.state = 'club.main';
                search.stateParams = {id: id};
            } else if (type === 'PERMIT') {
                search.state = 'permitmanagement.dashboard';
                search.stateParams = {permitId: id};
            }
            return self.selectActiveSearch(search);
        };

        this.clearActiveSearch = function () {
            LocalStorageService.setKey('activeSearch', null);
            activeSearch = null;
        };
    }

    function SiteSearchController($scope, $translate, $http, SiteSearchService) {
        var $ctrl = this;

        $ctrl.activeSearch = SiteSearchService.getActiveSearch();
        $ctrl.searchModel = null;
        $ctrl.searchResults = [];
        $ctrl.searching = false;

        $ctrl.activeSearchClicked = function () {
            SiteSearchService.selectActiveSearch(SiteSearchService.getActiveSearch());
        };

        $scope.$on('event:auth-loginCancelled', function () {
            $ctrl.searchModel = null;
            $ctrl.activeSearch = {};
        });

        $scope.$on('event:auth-loginConfirmed', function () {
            $ctrl.searchModel = null;
            $ctrl.activeSearch = {};
        });

        $ctrl.inputTooShort = function (search) {
            return search.length < 3;
        };

        $ctrl.doSearch = function (term) {
            $ctrl.searching = true;
            $ctrl.searchResults = [];

            return $http.get('api/v1/search', {
                params: {
                    term: term,
                    locale: $translate.use()
                }
            }).then(function (response) {
                var results = [];
                _.forOwn(response.data.results, function (arr, key) {
                    var categoryTranslated = $translate.instant('search.' + key);
                    _.each(arr, function (item) {
                        item.category = key;
                        item.categoryTranslated = categoryTranslated;
                        results.push(item);
                    });
                });
                $ctrl.searchResults = results;

            }).finally(function () {
                $ctrl.searching = false;
            });
        };

        $ctrl.doSelect = function () {
            $ctrl.activeSearch = SiteSearchService.handleSelection($ctrl.searchModel);
            $ctrl.searchModel = null;
        };
    }

})();
