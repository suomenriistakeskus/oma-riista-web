'use strict';

angular.module('app.harvestpermit.services', ['ngResource'])

    .constant('PermitCategories', ['MOOSELIKE', 'MOOSELIKE_NEW', 'BIRD', 'LARGE_CARNIVORE_BEAR', 'LARGE_CARNIVORE_LYNX', 'LARGE_CARNIVORE_LYNX_PORONHOITO'])
    .constant('DecisionTypes', ['HARVEST_PERMIT', 'CANCEL_APPLICATION', 'IGNORE_APPLICATION'])
    .constant('DecisionGrantStatus', ['UNCHANGED', 'RESTRICTED', 'REJECTED'])
    .constant('AppealStatus', ['INITIATED', 'IGNORED', 'UNCHANGED', 'REPEALED', 'PARTIALLY_REPEALED', 'RETREATMENT'])
    .constant('ProtectedAreaTypes', [
        'OTHER',
        'AIRPORT',
        'FOOD_PREMISES',
        'WASTE_DISPOSAL',
        'BERRY_FARM',
        'FUR_FARM',
        'FISHERY',
        'ANIMAL_SHELTER'
    ])

    .constant('DerogationReasonType', [
        'REASON_PUBLIC_HEALTH',
        'REASON_PUBLIC_SAFETY',
        'REASON_AVIATION_SAFETY',
        'REASON_CROPS_DAMAMGE',
        'REASON_DOMESTIC_PETS',
        'REASON_FOREST_DAMAGE',
        'REASON_FISHING',
        'REASON_WATER_SYSTEM',
        'REASON_FLORA',
        'REASON_FAUNA',
        'REASON_RESEARCH'
    ])

    .constant('PermitDecisionForbiddenMethodType', [
        'SNARES',
        'LIVE_ANIMAL_DECOY',
        'TAPE_RECORDERS',
        'ELECTRICAL_DEVICE',
        'ARTIFICIAL_LIGHT',
        'MIRRORS',
        'ILLUMINATION_DEVICE',
        'NIGHT_SHOOTING_DEVICE',
        'EXPLOSIVES',
        'NETS',
        'TRAPS',
        'POISON',
        'GASSING',
        'AUTOMATIC_WEAPON',
        'LIMES',
        'HOOKS',
        'CROSSBOWS',
        'SPEAR',
        'BLOWPIPE',
        'LEGHOLD_TRAP',
        'CONCEALED_WEAPON',
        'OTHER_SELECTIVE',
        'OTHER_NON_SELECTIVE'
    ])

    .factory('CheckPermitNumber', function (HttpPost) {
        return {
            check: function (permitNumber) {
                return HttpPost.post('api/v1/harvestpermit/checkPermitNumber', {permitNumber: permitNumber});
            }
        };
    })
    .service('PermitAcceptHarvest', function ($http) {
        this.accept = function (harvestId, harvestRev, toState) {
            return $http.post('api/v1/harvestpermit/acceptHarvest', {
                harvestId: harvestId,
                harvestRev: harvestRev,
                toState: toState
            }).then(function (response) {
                return response.data;
            });
        };
    })

    .service('HarvestPermitPdfUrl', function () {
        this.get = function (permitNumber) {
            return '/api/v1/harvestpermit/pdf/' + permitNumber;
        };
    })

    .service('HarvestPermitAttachmentUrl', function () {
        this.get = function (permitId, attachmentId) {
            return '/api/v1/harvestpermit/' + permitId + '/attachment/' + attachmentId;
        };
    })

    .factory('HarvestPermits', function ($http, $resource, CacheFactory) {
        return $resource('api/v1/harvestpermit/:id', {id: '@id'}, {
            permitTypes: {
                method: 'GET',
                isArray: true,
                url: 'api/v1/harvestpermit/permittypes',
                cache: CacheFactory.get('harvestPermitPermitTypesCache')
            },
            query: {
                method: 'GET',
                isArray: true,
                url: 'api/v1/harvestpermit/mypermits'
            },
            listMetsahallitusPermits: {
                method: 'GET',
                isArray: true,
                url: 'api/v1/harvestpermit/metsahallitus'
            },
            get: {
                method: 'GET'
            },
            getHarvestList: {
                method: 'GET',
                url: 'api/v1/harvestpermit/:id/harvests',
                isArray: true
            },
            getSpeciesUsage: {
                method: 'GET',
                url: 'api/v1/harvestpermit/:id/usage',
                isArray: true
            },
            getAttachmentList: {
                method: 'GET',
                url: 'api/v1/harvestpermit/:id/attachment',
                isArray: true
            },
            search: {method: 'POST', isArray: true, url: 'api/v1/harvestpermit/admin/search'},
            rhySearch: {method: 'POST', isArray: true, url: 'api/v1/harvestpermit/rhy/search'}
        });
    })

    .factory('speciesAmountIntervalTextFunc', function (Helpers) {
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
        function (Helpers, $filter) {
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
            this.findMatchingAmount = function (speciesAmounts, speciesCode, validOnDate) {
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
            this.findMatchingAmountIntervalAsText = function (speciesAmounts, speciesCode, validOnDate) {
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
    );
