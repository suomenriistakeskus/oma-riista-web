'use strict';

angular.module('app.common.validation', [])
    .directive('validatePhonenumber', function ($http, $q) {
        var BASIC_PATTERN = /^[+]?[0-9 -]{6,}$/;

        function _simpleValidityCheck(phoneNumber) {
            return angular.isString(phoneNumber) && BASIC_PATTERN.test(phoneNumber);
        }

        function _checkOverHttp(phoneNumber) {
            // HTTP API returns 200 OK always. For invalid phone numbers API returns string 'invalid'.
            // When using $http directly on asyncValidators then API should return 4xx (which we don't want),
            // therefore wrap $http to promise instead of returning $http directly.
            return $http({
                method: 'GET',
                url: '/api/v1/validation/phonenumber',
                params: {
                    phoneNumber: phoneNumber
                }
            }).then(function (res) {
                return res.data !== 'invalid' ? res : $q.reject(false);
            });
        }

        return {
            require: 'ngModel',
            link: function ($scope, element, attrs, ngModel) {
                // NOTE: empty string is considered valid value. Must use ng-required to enforce value.
                ngModel.$asyncValidators.validPhoneNumber = function (modelValue, viewValue) {
                    var phoneNumber = modelValue || viewValue;

                    if (ngModel.$isEmpty(phoneNumber)) {
                        return $q.when(true);
                    } else if (!_simpleValidityCheck(phoneNumber)) {
                        // Skip backend validation if basic format is wrong
                        return $q.reject(false);
                    } else {
                        return _checkOverHttp(phoneNumber);
                    }
                };
            }
        };
    })

    .directive('validateFinnishIban', function () {
        var IBAN_PATTERN = /^FI\d{2}\s?\d{4}\s?\d{4}\s?\d{4}\s?\d{2}$/;

        return {
            require: 'ngModel',
            link: function ($scope, element, attrs, ngModel) {

                ngModel.$validators.validIban = function (modelValue, viewValue) {
                    var iban = modelValue || viewValue;

                    return ngModel.$isEmpty(iban) || IBAN_PATTERN.test(iban);

                };
            }
        };
    })

    .directive('uniqueClubName', function ($http, $q) {
        function _checkOverHttp(clubId, name) {
            // HTTP API returns 200 OK always. When club name is not duplicate, API returns 'ok'
            // When using $http directly on asyncValidators then API should return 4xx (which we don't want),
            // therefore wrap $http to promise instead of returning $http directly.
            return $http({
                method: 'GET',
                url: '/api/v1/validation/clubname',
                params: {
                    name: name,
                    clubId: clubId
                }
            }).then(function (res) {
                return res.data === 'ok' ? res : $q.reject(false);
            });
        }

        return {
            require: 'ngModel',
            scope: {clubId: '='},
            link: function ($scope, element, attrs, ngModel) {
                // NOTE: empty string is considered valid value. Must use ng-required to enforce value.
                ngModel.$asyncValidators.validClubName = function (modelValue, viewValue) {
                    var clubName = modelValue || viewValue;

                    if (ngModel.$isEmpty(clubName)) {
                        return $q.when(true);
                    } else {
                        return _checkOverHttp($scope.clubId, clubName);
                    }
                };
            }
        };
    })

    .directive('finnishTime', function () {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, element, attrs, ngModelCtrl) {
                function _parseDate(inputValue) {
                    var parsedMoment = window.moment(inputValue, ['H:mm', 'H.mm', 'H mm', 'Hmm', 'H'], true);

                    return parsedMoment.isValid() ? parsedMoment.format("HH:mm") : undefined;
                }

                ngModelCtrl.$parsers.push(function (value) {
                    if (ngModelCtrl.$isEmpty(value)) {
                        return null;
                    } else if (angular.isString(value)) {
                        return _parseDate(value);
                    } else {
                        return undefined;
                    }
                });
            }
        };
    })

    .directive('finnishDateTimeInPast', function (Helpers) {

        function _withTime(dateMoment, time) {
            return dateMoment
                .hour(time.slice(0, 2))
                .minute(time.slice(3))
                .second(0)
                .millisecond(0);
        }

        function _parseTime(inputValue) {
            var parsedMoment = moment(inputValue, ['H:mm', 'H.mm', 'H mm', 'Hmm', 'H'], true);
            return parsedMoment.isValid() ? parsedMoment.format("HH:mm") : undefined;
        }

        function _isToday(dateMoment) {
            return dateMoment.startOf('day').isSame(moment().startOf('day'));
        }

        return {
            restrict: 'A',
            require: 'ngModel',
            scope: {finnishDateTimeInPast: '&'},
            link: function (scope, element, attrs, ngModelCtrl) {

                function _checkNotInFutureToday(date, time) {
                    var dateMoment = Helpers.toMoment(date, 'YYYY-MM-DD');
                    if (date && time && _isToday(dateMoment)) {
                        var isInPast = _withTime(dateMoment, time).isBefore();
                        ngModelCtrl.$setValidity('finnishDateTimeValidity', isInPast);
                    } else {
                        ngModelCtrl.$setValidity('finnishDateTimeValidity', true);
                    }
                }

                scope.$watch(scope.finnishDateTimeInPast, function (newDate) {
                    var time = _parseTime(ngModelCtrl.$viewValue);
                    _checkNotInFutureToday(newDate, time);
                    if (time) {
                        ngModelCtrl.$modelValue = time;
                        ngModelCtrl.$viewValue = time;
                    }
                }, true);

                function _parseDateTimeInPast(inputValue) {
                    var time = _parseTime(inputValue);
                    _checkNotInFutureToday(scope.finnishDateTimeInPast(), time);
                    return time;
                }

                ngModelCtrl.$parsers.push(function (value) {
                    if (ngModelCtrl.$isEmpty(value)) {
                        return null;
                    } else if (angular.isString(value)) {
                        return _parseDateTimeInPast(value);
                    } else {
                        return undefined;
                    }
                });
            }
        };
    })

    .directive('dateBetweenMinMax', function (Helpers) {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, element, attrs, ngModelCtrl) {
                ngModelCtrl.$validators.dateBetweenMinMax = function (modelValue, viewValue) {
                    var dateString = modelValue || viewValue;

                    if (ngModelCtrl.$isEmpty(dateString)) {
                        return true;
                    }

                    var min = attrs.minDate ? scope.$eval(attrs.minDate) : null;
                    var max = attrs.maxDate ? scope.$eval(attrs.maxDate) : null;
                    var min2 = attrs.minDate2 ? scope.$eval(attrs.minDate2) : null;
                    var max2 = attrs.maxDate2 ? scope.$eval(attrs.maxDate2) : null;

                    var range1 = !!min || !!max;
                    var range2 = !!min2 || !!max2;
                    var noLimits = !range1 && !range2;

                    var range1Valid = range1 && Helpers.dateWithinRange(dateString, min, max);
                    var range2Valid = range2 && Helpers.dateWithinRange(dateString, min2, max2);

                    return noLimits || range1 && range1Valid || range2 && range2Valid;
                };
            }
        };
    })

    .directive('validPersonEmail', function ($q, $http) {

        function _checkOverHttp(email) {
            return $http({
                method: 'GET',
                url: '/api/v1/validation/email',
                params: {
                    email: email
                }
            }).then(function (res) {
                return res.data !== 'invalid' ? res : $q.reject(false);
            });
        }

        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, element, attrs, ngModelCtrl) {
                var illegalSuffixes = ['.riista.fi', '@riista.fi'];

                function _endsWithAnySuffix(str, suffixes) {
                    return _.some(suffixes, function (suffix) {
                        return _.endsWith(str, suffix);
                    });
                }

                // NOTE: empty string is considered valid value. Must use ng-required to enforce value.
                ngModelCtrl.$asyncValidators.personEmail = function (modelValue, viewValue) {
                    var email = modelValue || viewValue;

                    if (ngModelCtrl.$isEmpty(email)) {
                        return $q.when(true);
                    } else if (email.indexOf('@') === -1) {
                        return $q.reject(false);
                    } else {
                        return _checkOverHttp(email);
                    }
                };

                ngModelCtrl.$validators.notRiistaEmail = function (modelValue, viewValue) {
                    var email = modelValue || viewValue;

                    if (ngModelCtrl.$isEmpty(email)) {
                        return true;
                    } else {
                        return !_endsWithAnySuffix(email, illegalSuffixes);
                    }
                };

                ngModelCtrl.$parsers.push(function (value) {
                    if (ngModelCtrl.$isEmpty(value)) {
                        return null;
                    } else if (angular.isString(value)) {
                        // Convert to lower-case
                        return value.toLowerCase();
                    } else {
                        return undefined;
                    }
                });
            }
        };
    })

    .directive('validPersonName', function () {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, element, attrs, ngModelCtrl) {
                var validNamePattern = /^[a-zA-ZäöåÄÖÅ .-]{2,}$/;

                ngModelCtrl.$validators.personName = function (modelValue, viewValue) {
                    var name = modelValue || viewValue;

                    if (ngModelCtrl.$isEmpty(name)) {
                        return true;
                    } else {
                        return validNamePattern.test(name);
                    }
                };
            }
        };
    })
    .directive('validHunterNumber', function (HunterNumberValidatorService) {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, element, attrs, ngModelCtrl) {
                ngModelCtrl.$validators.hunterNumber = function (modelValue, viewValue) {
                    var hunterNumber = modelValue || viewValue;

                    if (ngModelCtrl.$isEmpty(hunterNumber)) {
                        return true;
                    } else if (angular.isString(hunterNumber)) {
                        return HunterNumberValidatorService.validate(hunterNumber);
                    } else {
                        return false;
                    }
                };
            }
        };
    })

    .directive('validPermitNumber', function () {
        var permitNumberRegEx = /^20[1-9][0-9]-[0-9]-[0-9]{3}-[0-9]{5}-[0-9]$/;
        var checksumWeights = [7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1, 3, 7, 1];

        function _calculateChecksum(s) {
            var onlyDigits = s.replace(/-/g, '');
            var sum = 0;
            //there are 14 numbers, but last is checksum
            for (var i = 0; i < 13; i++) {
                sum += checksumWeights[i] * parseInt(onlyDigits.charAt(i));
            }
            var remainder = sum % 10;
            return remainder === 0 ? '0' : '' + (10 - remainder);
        }

        function _validatePermitNumber(permitNumber) {
            return permitNumberRegEx.test(permitNumber) &&
                permitNumber.charAt(17) === _calculateChecksum(permitNumber);
        }

        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, element, attrs, ngModelCtrl) {
                ngModelCtrl.$validators.permitNumber = function (modelValue, viewValue) {
                    var permitNumber = modelValue || viewValue;

                    if (ngModelCtrl.$isEmpty(permitNumber)) {
                        return true;
                    } else if (angular.isString(permitNumber)) {
                        return _validatePermitNumber(permitNumber);
                    } else {
                        return false;
                    }
                };
            }
        };
    })

    .directive('validFinnishBusinessId', function () {
        var BUSINESS_ID_PATTERN = /^[0-9]{7}-[0-9]$/;
        var BUSINESS_ID_WEIGHTS = [7, 9, 10, 5, 8, 4, 2];

        function _calculateChecksum(input) {
            var sum = 0;
            for (var i = 0; i < 7; i++) {
                sum += BUSINESS_ID_WEIGHTS[i] * (input.charAt(i) - '0');
            }

            var remainder = sum % 11;

            if (remainder === 0) {
                return '0';
            } else if (remainder === 1) {
                return 'x';
            } else {
                return '' + (11 - remainder);
            }
        }

        function _validateBusinessId(businessId) {
            return BUSINESS_ID_PATTERN.test(businessId) &&
                businessId.charAt(8) === _calculateChecksum(businessId);
        }

        return {
            restrict: 'A',
            require: 'ngModel',
            link: function ($scope, element, attrs, ngModel) {
                ngModel.$validators.finnishBusinessId = function (modelValue, viewValue) {
                    var businessId = modelValue || viewValue;

                    if (ngModel.$isEmpty(businessId)) {
                        return true;
                    } else {
                        return angular.isString(businessId) && _validateBusinessId(businessId);
                    }
                };
            }
        };
    })

    .directive('validSsn', function () {
        var PERSONAL_IDENTITY_NUMBER_PATTERN = /^(0[1-9]|[12][0-9]|3[01])(0[1-9]|1[012])([0-9]{2})[+-A][0-9]{3}[0-9A-FHJKLMNPR-Y]$/;
        var CHECK_CHARACTERS = "0123456789ABCDEFHJKLMNPRSTUVWXY";

        function _calculateCheckCharacter(input) {
            var birthDate = input.substring(0, 6);
            var identityNumber = input.substring(7, 10);
            var checkSum = parseInt(birthDate + identityNumber) % 31;

            return CHECK_CHARACTERS[checkSum];
        }

        function _validateFinnishPersonalIdentity(ssn) {
            return ssn.length === 11 &&
                PERSONAL_IDENTITY_NUMBER_PATTERN.test(ssn) &&
                ssn.charAt(10) === _calculateCheckCharacter(ssn);
        }

        return {
            restrict: 'A',
            require: 'ngModel',
            link: function ($scope, element, attrs, ngModelCtrl) {
                ngModelCtrl.$parsers.push(function (value) {
                    if (ngModelCtrl.$isEmpty(value)) {
                        return null;
                    } else if (angular.isString(value)) {
                        // Trim and convert to upper-case
                        return value.toUpperCase().trim();
                    } else {
                        return undefined;
                    }
                });

                ngModelCtrl.$validators.finnishPersonalIdentity = function (modelValue, viewValue) {
                    var ssn = modelValue || viewValue;

                    if (ngModelCtrl.$isEmpty(ssn)) {
                        return true;

                    } else {
                        return angular.isString(ssn) &&
                            _validateFinnishPersonalIdentity(ssn);
                    }
                };
            }
        };
    })

    .directive('rDecimalSeparatorFix', function () {
        // at least IE9 does not support both ',' and '.' as decimal separator
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, element, attrs, ngModelCtrl) {
                function _parseWeight(inputValue) {
                    return parseFloat(inputValue.replace(',', '.'));
                }

                // this parser needs to be first in order to use the normal type='number'
                ngModelCtrl.$parsers.unshift(function (value) {
                    if (ngModelCtrl.$isEmpty(value)) {
                        return null;
                    } else if (angular.isString(value)) {
                        return _parseWeight(value);
                    } else {
                        return undefined;
                    }
                });
            }
        };
    })
;
