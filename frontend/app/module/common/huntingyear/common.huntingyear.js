'use strict';

angular.module('app.common.huntingyear', ['app.common.services'])

    .service('HuntingYearService', function (Helpers) {
        var self = this;

        this.dateToHuntingYear = function (date) {
            var year = date.getFullYear();
            return date.getMonth() < 7 ? year - 1 : year;
        };

        this.getCurrent = function () {
            return self.dateToHuntingYear(new Date());
        };

        var _getHuntingYear = function (dateOrYear) {
            if (dateOrYear === null || _.isUndefined(dateOrYear)) {
                return self.getCurrent();
            } else if (_.isDate(dateOrYear)) {
                return self.dateToHuntingYear(dateOrYear);
            } else if (_.isFinite(dateOrYear)) {
                return dateOrYear;
            }

            throw new TypeError("Invalid argument for dateOrYear: " + dateOrYear);
        };

        this.getBeginDateStr = function (dateOrYear) {
            return Helpers.dateToString(new Date(_getHuntingYear(dateOrYear), 7, 1));
        };

        this.getEndDateStr = function (dateOrYear) {
            return Helpers.dateToString(new Date(_getHuntingYear(dateOrYear) + 1, 6, 31));
        };

        this.createHuntingYearChoices = function (start, includeNextYear) {
            var rangeStart = start || 2014;
            var rangeEnd = self.getCurrent();

            if (includeNextYear === true) {
                rangeEnd += 1;
            }

            return _.map(_.range(rangeStart, rangeEnd + 1), self.toObj);
        };

        // 2014 -> "2014-15"
        this.toStr = function (y) {
            var year = '' + y;
            var next = '' + (y + 1);
            return year + '-' + next.slice(2, 4);
        };

        this.toObj = function (year) {
            return {
                year: year,
                name: self.toStr(year)
            };
        };

        this.currentAndNextObj = function () {
            var year = self.dateToHuntingYear(new Date());

            return [self.toObj(year), self.toObj(year + 1)];
        };
    })

    .component('rHuntingYearSelection', {
        templateUrl: 'common/huntingyear/select-hunting-year.html',
        bindings: {
            availableHuntingYears: '<',
            preselectCurrentHuntingYear: '<',
            onHuntingYearChanged: '&'
        },
        controller: function (HuntingYearService) {
            var $ctrl = this;
            $ctrl.selectedHuntingYear = null;

            // Decorate with name parameter.
            $ctrl.$onInit = function () {
                $ctrl.decoratedYears = _($ctrl.availableHuntingYears)
                    .map(function (param) {
                        var obj;

                        if (_.isObject(param)) {
                            obj = param;
                        } else if (_.isFinite(param)) {
                            obj = {year: param};
                        }

                        if (_.isFinite(obj.year)) {
                            obj.name = HuntingYearService.toObj(obj.year).name;
                        }

                        return obj;
                    })
                    .value();

                if ($ctrl.preselectCurrentHuntingYear) {
                    var huntingYear = HuntingYearService.getCurrent();

                    $ctrl.selectedHuntingYear = _.find($ctrl.decoratedYears, function (obj) {
                        return obj.year === huntingYear;
                    });
                } else {
                    $ctrl.selectedHuntingYear = _($ctrl.decoratedYears).last();
                }
            };

            $ctrl.updateHuntingYear = function () {
                $ctrl.onHuntingYearChanged({
                    huntingYear: _.get($ctrl.selectedHuntingYear, 'year', null),
                });
            };
        }
    });
