'use strict';

angular.module('app.common.filters', ['app.metadata'])

    .filter('withVersion', function (versionUrlPrefix) {
        return function (url) {
            if (url.charAt(0) !== '/') {
                url = '/' + url;
            }
            return versionUrlPrefix + url;
        };
    })

    .filter('paginate', function () {
        return function (input, pager) {
            pager.total = input.length;
            return input.slice((pager.currentPage - 1) * pager.pageSize,
                pager.currentPage * pager.pageSize);
        };
    })

    .filter('formatPropertyIdentifier', function () {
        var validPattern = new RegExp("^\\d{14}$");

        return function (input) {
            if (angular.isString(input) && validPattern.test(input)) {
                var p = _.bind(String.prototype.substring, input);
                var parts = [p(0, 3), p(3, 6), p(6, 10), p(10, 14)];
                return _.map(parts, _.parseInt).join('-');
            }

            return input;
        };
    })

    .filter('languageCodeName', function ($translate) {
        return function (countryCode) {
            if (!countryCode) {
                return '';
            } else if (countryCode === 'fi' || countryCode === 'sv' || countryCode === 'en') {
                return $translate.instant('global.languageName.' + countryCode);
            }
            return countryCode;
        };
    })

    .filter('htmlSplitLines', function ($sce, $filter) {
        var linky = $filter('linky');

        function insideParagraph(input) {
            return _(input.split(/\n/g)).map(function (line) {
                return linky(line, '_blank');
            }).join('<br>');
        }

        return function (input) {
            if (!angular.isString(input)) {
                return '';
            }

            var result = _(input.split(/\n{2,}/)).map(function (text) {
                return '<p>' + insideParagraph(text) + '</p>';
            }).join('');

            return $sce.trustAsHtml(result);
        };
    })

    .filter('capitalize', function () {
        return function (input) {
            return _.capitalize(input);
        };
    })

    .filter('truncateCharacters', function () {
        return function (input, chars, breakOnWord) {
            if (isNaN(chars)) {
                return input;
            }
            if (chars <= 0) {
                return '';
            }
            if (input && input.length > chars) {
                input = input.substring(0, chars);

                if (!breakOnWord) {
                    var lastspace = input.lastIndexOf(' ');
                    //get last space
                    if (lastspace !== -1) {
                        input = input.substr(0, lastspace);
                    }
                } else {
                    while (input.charAt(input.length - 1) === ' ') {
                        input = input.substr(0, input.length - 1);
                    }
                }
                return input + '...';
            }
            return input;
        };
    })

    .filter('range', function () {
        return function (input, start, end, step) {
            step = step || 1;
            start = parseInt(start);
            end = parseInt(end);
            var direction = (start <= end) ? step : -step;

            while (start !== end) {
                input.push(start);
                start += direction;
            }
            input.push(start);

            return input;
        };
    })

    .filter('rI18nNameFilter', function ($translate) {
        return function (input, propertyName) {
            var lang = $translate.use();
            var result;

            if (input) {
                if (input.fi || input.sv) {
                    result = input[lang] || input.fi;
                } else if (input.nameFI || input.nameSV) {
                    result = (lang === 'sv' && input.nameSV) ? input.nameSV : input.nameFI;
                } else if (input.finnish || input.swedish) {
                    result = (lang === 'sv' && input.swedish) ? input.swedish : input.finnish;
                }
            }

            if (_.isString(propertyName) && _.size(propertyName) > 0) {
                input[propertyName] = result;
            }

            return result;
        };
    })

    .filter('translateWithPrefix', function ($translate) {
        var filter = function (input, localizationKeyPrefix) {
            return !input ? '' : $translate.instant(localizationKeyPrefix + input);
        };

        // Since AngularJS 1.3, filters which are not stateless (depending on the scope)
        // have to explicitly define this behavior.
        filter.$stateful = true;

        return filter;
    })

    .filter('rHuntingYear', function (HuntingYearService) {
        return function (year) {
            return HuntingYearService.toStr(year);
        };
    })

    .filter('prettyMinutes', function () {
        return function (input) {
            var value = _.parseInt(input);

            if (!_.isNaN(value)) {
                var hours = Math.floor(value / 60.0);
                var minutes = value - hours * 60;

                return (hours || 0) + ':' + _.padStart(minutes, 2, '0');
            }

            return '';
        };
    })

    .filter('sumFinite', function () {
        return function (arr) {
            if (!_.isArray(arr)) {
                return null;
            }
            var finite = _.filter(arr, _.isFinite);
            return _.isEmpty(finite) ? null : _.sum(finite);
        };
    })

    .filter('translateSpeciesCode', function (SpeciesNameService) {
        return function (speciesCode) {
            return SpeciesNameService.translateSpeciesCode(speciesCode) || '?';
        };
    });
