'use strict';

angular.module('app.jht.otherwisedeceased-filters', [])
    .component('rOtherwiseDeceasedFilters', {
        templateUrl: 'jht/otherwisedeceased/otherwisedeceased-filters.html',
        bindings: {
            filter: '<',
            onFilterChange: '&',
            onYearChange: '&'
        },
        controller: function ($scope, Helpers, OtherwiseDeceasedCauses) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.causeFilters = OtherwiseDeceasedCauses;
                $ctrl.availableYears = listYearsUntilNow();
                $ctrl.filter.year = _.last($ctrl.availableYears).toString();
                updateDateFilters();
                updateDateOptions();
                $ctrl.filter.rka = null;
                $ctrl.filter.rhy = null;
                $ctrl.filter.cause = '';
                $ctrl.filter.showRejected = false;
                $ctrl.onFilterChange();
            };

            $ctrl.changeYear = function () {
                updateDateFilters();
                updateDateOptions();
                $ctrl.onYearChange();
            };

            $ctrl.changeDate = function () {
                updateDateOptions();
                $ctrl.onFilterChange();
            };

            function updateDateFilters() {
                $ctrl.filter.beginDate = Helpers.dateToString(new Date($ctrl.filter.year, 0, 1)); // Jan 1
                $ctrl.filter.endDate = Helpers.dateToString(new Date($ctrl.filter.year, 11, 31)); // Dec 31
                $ctrl.beginOfYear = new Date($ctrl.filter.year, 0, 1);
                $ctrl.endOfYear = new Date($ctrl.filter.year, 11, 31);
            }

            function updateDateOptions() {
                $ctrl.beginDateOptions = {
                    minDate: $ctrl.beginOfYear,
                    maxDate: moment($ctrl.filter.endDate).toDate()
                };

                $ctrl.endDateOptions = {
                    minDate: moment($ctrl.filter.beginDate).toDate(),
                    maxDate: $ctrl.endOfYear
                };
            }

            function currentYear() {
                return new Date().getFullYear();
            }

            function listYearsUntilNow() {
                return _.range(2021, currentYear() + 1); // Upper limit is included.
            }

            $scope.$watchGroup(['$ctrl.filter.rka', '$ctrl.filter.rhy'], function(newVal, oldVal) {
                if (newVal[0] !== oldVal[0] || newVal[1] !== oldVal[1]) {
                    $ctrl.onFilterChange();
                }
            });
        }
    });
