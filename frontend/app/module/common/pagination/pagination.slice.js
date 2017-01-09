(function () {
    'use strict';

    angular.module('app.common.pagination.slice', [])
        .component('slicePager', {
            templateUrl: 'common/pagination/slice.html',
            bindings: {
                slice: '<',
                loadPage: '&'
            },
            controller: function () {
                var $ctrl = this;

                $ctrl.nextPage = function () {
                    if ($ctrl.hasNextPage()) {
                        loadPage($ctrl.slice.pageable.page + 1);
                    }
                };

                $ctrl.previousPage = function () {
                    if ($ctrl.hasPreviousPage()) {
                        loadPage($ctrl.slice.pageable.page - 1);
                    }
                };

                $ctrl.hasNextPage = function () {
                    return $ctrl.slice && $ctrl.slice.hasNext;
                };

                $ctrl.hasPreviousPage = function () {
                    return $ctrl.slice && $ctrl.slice.pageable &&
                        $ctrl.slice.pageable.page > 0;
                };

                function loadPage(page) {
                    $ctrl.loadPage({page: page});
                }

                $ctrl.$onInit = function () {
                    loadPage(0);
                };
            }
        });
})();
