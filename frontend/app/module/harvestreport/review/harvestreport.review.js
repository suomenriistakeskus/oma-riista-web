'use strict';

angular.module('app.harvestreportreview.search', [])

    .config(function ($stateProvider) {
        $stateProvider
            .state('jht.harvestreportreview.list', {
                url: '/list',
                templateUrl: 'harvestreport/review/harvestreport-review-search.html'
            });
    });
