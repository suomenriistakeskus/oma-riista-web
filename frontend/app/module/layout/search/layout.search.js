'use strict';

angular.module('app.layout.search', [])
    .service('SiteSearchService', function () {
            var activeSearch = null;

            this.getActiveSearch = function () {
                return activeSearch;
            };

            this.setActiveSearch = function (search) {
                activeSearch = search;
            };

            this.clearActiveSearch = function () {
                activeSearch = null;
            };

            this.createSearch = function (id, type, title) {
                var search = {
                    title: title,
                    id: id
                };

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

                return search;
            };
        }
    )
    .directive('navSearch', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: true,
            bindToController: true,
            templateUrl: 'layout/search/nav-search.html',
            controllerAs: '$ctrl',
            controller: function ($state, $translate, $http, SiteSearchService) {
                var $ctrl = this;

                $ctrl.uiSelectModel = null;
                $ctrl.searchResults = [];
                $ctrl.searching = false;

                var emptyActiveSearchTitle = $translate.use() === 'sv' ? 'Sök...' : 'Hae...';

                $ctrl.getActiveSearchTitle = function () {
                    var activeSearch = SiteSearchService.getActiveSearch();
                    return activeSearch ? activeSearch.title : emptyActiveSearchTitle;
                };

                $ctrl.onActiveSearchClick = function () {
                    var activeSearch = SiteSearchService.getActiveSearch();

                    if (activeSearch) {
                        navigateToSearch(activeSearch);
                    }
                };

                $ctrl.onSearchResultSelect = function () {
                    var id = $ctrl.uiSelectModel.id;
                    var type = $ctrl.uiSelectModel.category;
                    var title = $ctrl.uiSelectModel.description;

                    $ctrl.uiSelectModel = null;

                    var search = SiteSearchService.createSearch(id, type, title);
                    SiteSearchService.setActiveSearch(search);
                    navigateToSearch(search);
                };

                function navigateToSearch(search) {
                    $state.go(search.state, search.stateParams);
                }

                $ctrl.doSearch = function (term) {
                    $ctrl.searching = true;
                    $ctrl.searchResults = [];

                    return $http.post('api/v1/search', {
                            term: term,
                            locale: $translate.use()
                    }).then(function (response) {
                        $ctrl.searchResults = transformHttpSearchResults(response.data);

                    }).finally(function () {
                        $ctrl.searching = false;
                    });
                };

                function transformHttpSearchResults(data) {
                    var results = [];

                    _.forOwn(data.results, function (arr, key) {
                        var categoryTranslated = $translate.instant('search.' + key);

                        _.forEach(arr, function (item) {
                            item.category = key;
                            item.categoryTranslated = categoryTranslated;
                            results.push(item);
                        });
                    });

                    return results;
                }
            }
        };
    });
