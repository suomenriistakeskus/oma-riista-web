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
        var dateIntervalToString = function (begin, end) {
            begin = begin ? dateToString(toMoment(begin), 'D.M.YYYY') : ' ';
            end = end ? dateToString(toMoment(end), 'D.M.YYYY') : ' ';

            return begin + ' - ' + end;
        };

        var dateWithinRange = function (d, begin, end) {
            begin = begin ? toMoment(begin, 'YYYY-MM-DD') : null;
            end = end ? toMoment(end, 'YYYY-MM-DD') : null;
            d = d ? toMoment(d, 'YYYY-MM-DD') : null;

            if (!d) {
                return false;
            }
            if (begin && end) {
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

        function localStorageAvailable() {
            var key = 'LocalStorageService_testStorage';

            try {
                window.localStorage.setItem(key, key);
                window.localStorage.removeItem(key);
                return true;

            } catch (e) {
                return false;
            }
        }

        function resolveStorage() {
            return localStorageAvailable() ? window.localStorage : noopStorage;
        }

        var _storage = resolveStorage();

        this.getKey = function (key) {
            return _storage.getItem(key);
        };

        this.setKey = function (key, value) {
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

    .service('HttpGetBlob', function ($http) {
        function appendTransform(defaults, transform) {
            defaults = angular.isArray(defaults) ? defaults : [defaults];
            return defaults.concat(transform);
        }

        function doTransform(data, headersGetter, status) {
            var headers = headersGetter();
            var contentType = headers['content-type'] || null;

            if (status === 200) {
                if (!contentType) {
                    console.log('Content-Type not set in response');
                }

                var blob = new Blob([data], {type: contentType});

                var filenameRegexp = /.*filename="(.+)"/i;
                var filenameMatch = filenameRegexp.exec(headers['content-disposition']);

                if (angular.isObject(filenameMatch)) {
                    blob.name = filenameMatch['1'];
                }

                return blob;
            }

            if (data && contentType.indexOf("application/json") !== -1) {
                var jsonStr = String.fromCharCode.apply(null, new Uint8Array(data));
                return JSON.parse(jsonStr);
            }

            return null;
        }

        this.get = function (url, responseType) {
            return $http({
                method: 'GET',
                url: url,
                responseType: responseType || 'text',
                transformResponse: appendTransform($http.defaults.transformResponse, doTransform)
            });
        };

        this.post = function (url, responseType, data) {
            return $http({
                method: 'POST',
                url: url,
                responseType: responseType || 'text',
                transformResponse: appendTransform($http.defaults.transformResponse, doTransform),
                data: data
            });
        };
    })

    .service('FetchAndSaveBlob', function (HttpGetBlob, FileSaver) {

        function save(response) {
            var blob = response.data;
            var filename = blob.name;

            FileSaver.saveAs(blob, filename, true);
        }

        this.get = function (url) {
            return HttpGetBlob.get(url, 'arraybuffer').then(save);
        };

        this.post = function (url, data) {
            return HttpGetBlob.post(url, 'arraybuffer', data).then(save);
        };
    })

    .service('FormPostService', function ($cookies) {
        function submitForm(action, params, target, excludeCsrf) {
            var form = document.createElement('form');
            form.setAttribute('method', 'post');
            form.setAttribute('action', action);
            form.setAttribute('target', target);
            form.style.visibility = 'hidden';

            if (!excludeCsrf) {
                var csrfField = document.createElement('input');
                csrfField.setAttribute("type", "hidden");
                csrfField.setAttribute('name', '_csrf');
                csrfField.setAttribute('value', $cookies.get('XSRF-TOKEN'));
                csrfField.style.visibility = 'hidden';
                form.appendChild(csrfField);
            }

            _.forOwn(params, function (value, key) {
                if (value === null || value === '' || angular.isUndefined(value)) {
                    return;
                }
                var fieldElement = document.createElement('input');
                fieldElement.setAttribute("type", "hidden");
                fieldElement.setAttribute('name', key);
                fieldElement.setAttribute('value', value);
                fieldElement.style.visibility = 'hidden';
                form.appendChild(fieldElement);
            });

            document.body.appendChild(form);
            form.submit();
        }

        this.submitFormUsingBlankTarget = function (action, params, excludeCsrf) {
            submitForm(action, params, '_blank', excludeCsrf);
        };

        this.submitFormUsingSelfTarget = function (action, params, excludeCsrf) {
            submitForm(action, params, '_self', excludeCsrf);
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
    .service('ConfirmationDialogService', function ($q, $uibModal) {
        this.showConfimationDialogWithPrimaryAccept = function (titleKey, bodyKey) {
            return doShow(titleKey, bodyKey, 'common/confirm-default-accept.html');
        };

        this.showConfimationDialogWithPrimaryReject = function (titleKey, bodyKey) {
            return doShow(titleKey, bodyKey, 'common/confirm-default-reject.html');
        };

        function doShow(titleKey, bodyKey, template) {
            return $uibModal.open({
                templateUrl: template,
                controller: ModalController,
                controllerAs: '$ctrl',
                resolve: {
                    titleKey: _.constant(titleKey),
                    bodyKey: _.constant(bodyKey)
                }
            }).result;
        }

        function ModalController($uibModalInstance, $translate, titleKey, bodyKey) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.reason = '';
                $ctrl.modalTitle = $translate.instant(titleKey);
                $ctrl.message = $translate.instant(bodyKey);
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };

            $ctrl.ok = function () {
                $uibModalInstance.close($ctrl.reason);
            };
        }
    })
;
