'use strict';

angular.module('app.common.services', [])

    .service('Helpers', function () {
        var _dateToString = function (date, momentFormat) {
            if (date === null) {
                return null;
            }
            if (moment.isDate(date)) {
                return moment(date).format(momentFormat);
            }
            if (moment.isMoment(date)) {
                return date.format(momentFormat);
            }
            if (_.isString(date)) {
                return date;
            }
            console.log('Helpers.dateToString: date is neither Date, moment or String', date);
            return date;
        };
        var dateToString = function (date, momentFormat) {
            return _dateToString(date, momentFormat || 'YYYY-MM-DD');
        };
        var dateTimeToString = function (date, momentFormat) {
            return _dateToString(date, momentFormat || 'YYYY-MM-DD[T]HH:mm');
        };
        var dateIntervalToString = function(begin, end) {
            begin = begin ? dateToString(moment(begin), 'D.M.YYYY') : ' ';
            end = end ? dateToString(moment(end), 'D.M.YYYY') : ' ';

            return begin + ' - ' + end;
        };

        var dateWithinRange = function (d, begin, end) {
            begin = begin ? moment(begin) : null;
            end = end ? moment(end) : null;
            d = d ? moment(d) : null;

            if (!d) {
                return false;
            } if (begin && end) {
                return d.isBetween(begin, end, 'day') || d.isSame(begin, 'day') || d.isSame(end, 'day');
            } else if (begin) {
                return d.isAfter(begin, 'day') || d.isSame(begin, 'day');
            } else if (end) {
                return d.isBefore(end, 'day') || d.isSame(end, 'day');
            } else {
                return true;
            }
        };

        function toMoment(date, dateFormat) {
            if (date === null) {
                return null;
            }
            if (angular.isString(date)) {
                return moment(date, dateFormat);
            }
            if (moment.isDate(date)) {
                return moment(date);
            }
            if (moment.isMoment(date)) {
                return date;
            }
            return date;
        }

        function parseDateAndTime(date, time, dateFormat) {
            var day = toMoment(date, dateFormat);

            if (day && angular.isString(time)) {
                day.hour(time.slice(0, 2));
                day.minute(time.slice(3));

                return day;
            }

            return null;
        }

        var wrapToFunction = function (value) {
            return function () {
                return value;
            };
        };

        return {
            dateToString: dateToString,
            dateTimeToString: dateTimeToString,
            dateIntervalToString: dateIntervalToString,
            dateWithinRange: dateWithinRange,
            toMoment: toMoment,
            parseDateAndTime: parseDateAndTime,
            wrapToFunction: wrapToFunction
        };
    })

    .service('LocalStorageService', function () {
        // no op Storage interface
        var noopStorage = {
            length: 0,
            key: _.noop,
            getItem: _.noop,
            setItem: _.noop,
            removeItem: _.noop,
            clear: _.noop
        };

        function resolveStorage() {
            if (!window.localStorage) {
                console.log('LocalStorageService no localStorage available, returning no-op storage object');
                return noopStorage;
            }
            try {
                window.localStorage.setItem('LocalStorageService_testStorage', 1);
                window.localStorage.removeItem('LocalStorageService_testStorage');
            } catch (e) {
                console.log('LocalStorageService localStorage not usable, returning no-op storage object', e);
                return noopStorage;
            }
            return window.localStorage;
        }

        var _storage = resolveStorage();

        this.getKey = function(key, value) {
            return _storage.getItem(key);
        };

        this.setKey = function(key, value) {
            if (value) {
                _storage.setItem(key, value);
            } else {
                _storage.removeItem(key);
            }
        };
    })

    .factory('HttpPost', function ($http) {
        var transformRequest = function (obj) {
            var str = [];

            _.forOwn(obj, function (value, key) {
                str.push(encodeURIComponent(key) + "=" + encodeURIComponent(value));
            });

            return str.join("&");
        };
        return {
            post: function (url, data) {
                return $http({
                    url: url,
                    method: 'POST',
                    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                    transformRequest: transformRequest,
                    data: data
                });
            }
        };
    })

    .service('FormPostService', function ($cookies) {
        this.submitFormUsingBlankTarget = function (action, params) {
            var form = document.createElement('form');
            form.setAttribute('method', 'post');
            form.setAttribute('action', action);
            form.setAttribute('target', '_blank');
            form.style.visibility = 'hidden';

            var csrfField = document.createElement('input');
            csrfField.setAttribute("type", "hidden");
            csrfField.setAttribute('name', '_csrf');
            csrfField.setAttribute('value', $cookies.get('XSRF-TOKEN'));
            csrfField.style.visibility = 'hidden';
            form.appendChild(csrfField);

            _.forOwn(params, function (value, key) {
                var fieldElement = document.createElement('input');
                fieldElement.setAttribute("type", "hidden");
                fieldElement.setAttribute('name', key);
                fieldElement.setAttribute('value', value);
                fieldElement.style.visibility = 'hidden';
                form.appendChild(fieldElement);
            });

            document.body.appendChild(form);
            form.submit();
        };
    })

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

        this.createHuntingYearChoices = function (start) {
            var rangeStart = start || 2014;
            var rangeEnd = self.getCurrent();

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
    .service('UnsavedChangesConfirmationService', function ($window, $translate) {
        var hasUnsavedChanges = false;

        this.setChanges = function (b) {
            hasUnsavedChanges = b;
        };

        this.checkEvent = function (event) {
            if (hasUnsavedChanges) {
                var confirmed = $window.confirm($translate.instant('global.dialog.unsavedChangesConfirmation'));
                if (confirmed) {
                    hasUnsavedChanges = false;
                } else {
                    event.preventDefault();
                }
            }
        };
    })
    .service('HunterNumberValidatorService', function () {
        var hunterNumberRegEx = /^[1-9][0-9]{7}$/;
        var checksumWeights = [7, 1, 3, 7, 1, 3, 7];

        function _calculateChecksum(s) {
            var sum = 0;
            for (var i = 0; i < 7; i++) {
                sum += checksumWeights[i] * parseInt(s.charAt(i));
            }
            var remainder = sum % 10;
            return remainder === 0 ? '0' : '' + (10 - remainder);
        }

        this.validate = function (hunterNumber) {
            return hunterNumberRegEx.test(hunterNumber) &&
                hunterNumber.charAt(7) === _calculateChecksum(hunterNumber);
        };
    })
    .service('SpeciesSortByName', function ($filter) {
        var i18n = $filter('rI18nNameFilter');
        this.sort = function (species) {
            return _.sortBy(species, function (s) {
                var name = i18n(s.name);
                return name ? name.toLowerCase() : null;
            });
        };
    })
;
