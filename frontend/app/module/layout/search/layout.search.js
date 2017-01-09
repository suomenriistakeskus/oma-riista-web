(function () {
    'use strict';

    angular.module('app.layout.search', [])
        .service('SiteSearchService', SiteSearchService)
        .directive('navSearch',function () {
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

    function SiteSearchService($state, $translate, LocalStorageService) {
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

        this.handleSelectionFromSelect2Model = function (model) {
            var v = model.id.split(':');
            if (v.length !== 2) {
                console.log('Unable to parse search selection from select2 model:', model);
                return;
            }
            var type = v[0];
            var id = v[1];
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
            }
            return self.selectActiveSearch(search);
        };

        this.clearActiveSearch = function () {
            LocalStorageService.setKey('activeSearch', null);
            activeSearch = null;
        };

        this.createOptions = function () {
            var formatInputTooShort = function (term, minLength) {
                var count = minLength - term.length;
                return $translate.instant('search.formatInputTooShort', {count: count});
            };

            function formatSearchResults(input, page) {
                var output = [];

                // Iterate search result categories
                var categories = _.keys(input);
                for (var i = 0; i < categories.length; i++) {
                    // Iterate results by category
                    var category = categories[i];

                    for (var j = 0; j < input[category].length; j++) {
                        // Encode item id as category:id
                        input[category][j].id = category + ":" + input[category][j].id;
                    }

                    if (page > 1) {
                        output[i] = {
                            children: input[category]
                        };

                    } else {
                        // Show parent description only for the first page
                        output[i] = {
                            children: input[category],
                            description: $translate.instant('search.' + category)
                        };
                    }
                }

                return output;
            }

            return {
                minimumInputLength: 3,
                allowClear: false,
                dropdownCssClass: 'r-main-search-dropdown',
                dropdownAutoWidth: true,
                ajax: {
                    quietMillis: 150,
                    url: 'api/v1/search',
                    dataType: 'json',
                    data: function (term, page) {
                        return {
                            'term': term,
                            'page': page,
                            'locale': $translate.use()
                        };
                    },
                    results: function (data, page) {
                        return {
                            results: formatSearchResults(data.results, page),
                            more: false
                        };
                    }
                },
                formatNoMatches: _.partial($translate.instant, 'search.formatNoMatches'),
                formatInputTooShort: formatInputTooShort,
                formatSearching: _.partial($translate.instant, 'search.formatSearching'),
                formatLoadMore: _.partial($translate.instant, 'search.formatSearching'),
                formatResult: function (item) {
                    return item.description;
                },

                formatSelection: function (item) {
                    return item.description;
                }
            };
        };
    }

    function SiteSearchController($scope, $translate, SiteSearchService) {
        var $ctrl = this;

        $ctrl.select2Options = SiteSearchService.createOptions($translate);
        $ctrl.searchModel = null;
        $ctrl.activeSearch = SiteSearchService.getActiveSearch();

        $scope.$watch('$ctrl.searchModel', function () {
            if ($ctrl.searchModel === null) {
                return;
            }
            $ctrl.activeSearch = SiteSearchService.handleSelectionFromSelect2Model($ctrl.searchModel);
            $ctrl.searchModel = null;
        });

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
    }

})();
