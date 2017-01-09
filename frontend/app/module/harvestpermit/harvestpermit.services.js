'use strict';

angular.module('app.harvestpermit.services', ['ngResource'])

    .factory('CheckPermitNumber', function (HttpPost) {
        return {
            check: function (permitNumber) {
                return HttpPost.post('api/v1/harvestpermit/checkPermitNumber', {permitNumber: permitNumber});
            }
        };
    })
    .factory('PermitAcceptHarvest', function (HttpPost) {
        return {
            accept: function (harvestId, harvestRev, toState) {
                return HttpPost.post('api/v1/harvestpermit/acceptHarvest', {
                    harvestId: harvestId,
                    harvestRev: harvestRev,
                    toState: toState
                });
            }
        };
    })
    .factory('HarvestPermits', function ($resource) {
        return $resource('api/v1/harvestpermit/:id', {id: '@id'}, {
            get: {method: 'GET'},
            query: {method: 'GET', isArray: true, url: 'api/v1/harvestpermit/mypermits'},
            listByRhy: {method: 'GET', url: 'api/v1/harvestpermit/rhy/:rhyId/list'},
            updateContactPersons: {method: 'PUT', url: 'api/v1/harvestpermit/:id/contactpersons'},
            species: {method: 'GET', isArray: true, url: 'api/v1/harvestpermit/species'},
            search: {method: 'POST', isArray: true, url: 'api/v1/harvestpermit/admin/search'},
            rhySearch: {method: 'POST', isArray: true, url: 'api/v1/harvestpermit/rhy/search'},
            moosePermitRhyCode: {
                method: 'GET',
                url: 'api/v1/harvestpermit/moosepermit/rhy/:permitId',
                params: {permitId: '@permitId'}
            },
            moosePermitHuntingYears: {
                method: 'GET',
                url: 'api/v1/harvestpermit/moosepermit/huntingyears',
                params: {personId: '@personId'},
                isArray: true
            },
            listMoosePermits: {
                method: 'GET',
                url: 'api/v1/harvestpermit/moosepermit',
                params: {year: '@year', personId: '@personId', species: '@species'},
                isArray: true
            },
            moosePermit: {
                method: 'GET',
                url: 'api/v1/harvestpermit/moosepermit/:permitId',
                params: {permitId: '@permitId'}
            },
            permitMapFeatures: {
                method: 'GET',
                url: 'api/v1/harvestpermit/moosepermit/:permitId/map',
                params: {'permitId': '@permitId'}
            },
            listTodos: {
                method: 'GET',
                url: 'api/v1/harvestpermit/moosepermit/todo/permit/:permitId/species/:speciesCode',
                params: {'permitId': '@permitId', 'speciesCode': '@speciesCode'}
            },
            listClubTodos: {
                method: 'GET',
                url: 'api/v1/harvestpermit/moosepermit/todo/club/:clubId',
                params: {'clubId': '@clubId', 'year': '@year'}
            },
            lukeReportParams: {method: 'GET', url: 'api/v1/harvestpermit/moosepermit/lukereportparams'},
            getClubHuntingSummariesForModeration: {
                method: 'GET',
                url: 'api/v1/harvestpermit/:permitId/huntingsummariesformoderation/species/:speciesCode',
                params: {
                    'permitId': '@permitId',
                    'speciesCode': '@speciesCode'
                },
                isArray: true
            },
            massOverrideClubHuntingSummaries: {
                method: 'POST',
                url: 'api/v1/harvestpermit/:permitId/massoverrideclubhuntingsummaries/species/:speciesCode/:complete',
                params: {
                    'permitId': '@permitId',
                    'speciesCode': '@speciesCode',
                    'complete': '@complete'
                }
            },
            deleteModeratorOverriddenClubHuntingSummaries: {
                method: 'DELETE',
                url: 'api/v1/harvestpermit/:permitId/deleteoverriddenclubhuntingsummaries/species/:speciesCode',
                params: {
                    'permitId': '@permitId',
                    'speciesCode': '@speciesCode'
                }
            }
        });
    })

    .factory('speciesAmountIntervalTextFunc', function(Helpers) {
        return function (wrapToParentheses) {
            return function (speciesAmount) {
                if (speciesAmount) {
                    var parts = [];

                    if (speciesAmount.beginDate) {
                        parts.push(Helpers.dateIntervalToString(speciesAmount.beginDate, speciesAmount.endDate));
                    }

                    if (speciesAmount.beginDate2) {
                        parts.push(Helpers.dateIntervalToString(speciesAmount.beginDate2, speciesAmount.endDate2));
                    }

                    if (parts.length > 0) {
                        var str = parts.join(', ');
                        if (wrapToParentheses) {
                            return '(' + str + ')';
                        }
                        return str;
                    }
                }
                return '';
            };
        };
    })

    .filter('speciesAmountIntervalText', function (speciesAmountIntervalTextFunc) {
        return speciesAmountIntervalTextFunc(true);
    })
    .filter('speciesAmountIntervalTextPlain', function (speciesAmountIntervalTextFunc) {
        return speciesAmountIntervalTextFunc(false);
    })

    .service('HarvestPermitSpeciesAmountService',
        function(Helpers, $filter) {
            var self = this;
            var speciesAmountIntervalTextFilter = $filter('speciesAmountIntervalText');

            /**
             * Find first valid HarvestPermit option which is valid on given date for species.
             *
             * @param speciesAmounts {array}
             * @param speciesCode {int}
             * @param validOnDate {Date} optional
             * @returns {*} single result or null
             */
            this.findMatchingAmount = function(speciesAmounts, speciesCode, validOnDate) {
                return _.find(speciesAmounts, function (speciesAmount) {
                    if (validOnDate) {
                        return speciesAmount.gameSpecies.code === speciesCode &&
                            self.isValidDateForSpeciesAmount(speciesAmount, validOnDate);
                    }

                    return speciesAmount.gameSpecies.code === speciesCode;
                });
            };

            /**
             * Find specific date interval as text matching given species and/or date.
             *
             * @param speciesAmounts {array}
             * @param speciesCode {int}
             * @param validOnDate {Date} optional
             * @returns {*} string or empty string
             */
            this.findMatchingAmountIntervalAsText = function(speciesAmounts, speciesCode, validOnDate) {
                var spa = this.findMatchingAmount(speciesAmounts, speciesCode, validOnDate);

                if (!spa) {
                    // Find any matching to species
                    spa = this.findMatchingAmount(speciesAmounts, speciesCode, null);
                }

                return spa ? speciesAmountIntervalTextFilter(spa) : '';
            };

            this.isValidDateForSpeciesAmount = function (speciesAmount, validOnDate) {
                if (speciesAmount && validOnDate) {
                    if (Helpers.dateWithinRange(validOnDate, speciesAmount.beginDate, speciesAmount.endDate)) {
                        return true;
                    }

                    if (speciesAmount.beginDate2 || speciesAmount.endDate2) {
                        return Helpers.dateWithinRange(validOnDate, speciesAmount.beginDate2, speciesAmount.endDate2);
                    }
                }

                return false;
            };
        }
    )
    .factory('PermitEndOfHuntingReportService',
        function ($uibModal, HarvestPermits) {

            var showModal = function (permit, report) {
                return $uibModal.open({
                    templateUrl: 'harvestpermit/form-end-of-hunting-report.html',
                    resolve: {
                        permit: _.constant(permit),
                        report: _.constant(report)
                    },
                    controller: 'PermitEndOfHuntingReportEditController',
                    size: 'lg'
                }).result;
            };

            var viewById = function (permitId) {
                return HarvestPermits.get({id: permitId}).$promise.then(function (permit) {
                    return showModal(permit, permit.endOfHuntingReport);
                });
            };

            var create = function (permit) {
                return showModal(permit);
            };

            return {
                create: create,
                viewById: viewById
            };
        })
;
