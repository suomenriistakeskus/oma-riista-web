'use strict';

angular.module('app.jht.otherwisedeceased-filters', [])
    .component('rOtherwiseDeceasedFilters', {
        templateUrl: 'jht/otherwisedeceased/otherwisedeceased-filters.html',
        bindings: {
            filter: '<',
            onFilterChange: '&'
        },
        controller: function ($scope, Helpers, OtherwiseDeceasedCauses) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.causeFilters = OtherwiseDeceasedCauses;
                $ctrl.filter.rka = null;
                $ctrl.filter.rhy = null;
                $ctrl.filter.cause = '';
                $ctrl.filter.showRejected = false;
                $ctrl.filter.beginDate = Helpers.dateToString(new Date(currentYear(), 0, 1)); // Jan 1
                $ctrl.filter.endDate = Helpers.dateToString(new Date(currentYear(), 11, 31)); // Dec 31
                $ctrl.onFilterChange();
            };

            function currentYear() {
                return new Date().getFullYear();
            }

            $scope.$watchGroup(['$ctrl.filter.rka', '$ctrl.filter.rhy'], function (newVal, oldVal) {
                if (newVal[0] !== oldVal[0] || newVal[1] !== oldVal[1]) {
                    $ctrl.onFilterChange();
                }
            });
        }
    });
