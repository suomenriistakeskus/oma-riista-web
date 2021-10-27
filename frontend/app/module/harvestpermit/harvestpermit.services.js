'use strict';

angular.module('app.harvestpermit.services', ['ngResource'])

    .constant('PermitCategories', ['MOOSELIKE', 'MOOSELIKE_NEW', 'BIRD',
        'LARGE_CARNIVORE_BEAR', 'LARGE_CARNIVORE_LYNX', 'LARGE_CARNIVORE_LYNX_PORONHOITO', 'LARGE_CARNIVORE_WOLF',
        'MAMMAL', 'NEST_REMOVAL', 'LAW_SECTION_TEN', 'WEAPON_TRANSPORTATION',
        'DISABILITY', 'DOG_DISTURBANCE', 'DOG_UNLEASH', 'DEPORTATION', 'RESEARCH', 'IMPORTING', 'GAME_MANAGEMENT'])
    .constant('DecisionTypes', ['HARVEST_PERMIT', 'CANCEL_APPLICATION', 'IGNORE_APPLICATION', 'CANCEL_ANNUAL_RENEWAL'])
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
    .constant('DogEventType', {
        DOG_TRAINING: 'DOG_TRAINING',
        DOG_TEST: 'DOG_TEST'
    })

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
            omaRiistaPermitTypes: {
                method: 'GET',
                isArray: true,
                url: 'api/v1/harvestpermit/omariistapermittypes',
                cache: CacheFactory.get('harvestPermitOmaRiistaPermitTypesCache')
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

    .factory('NestRemovalPermitUsage', function ($http, $resource, CacheFactory) {
        return $resource('api/v1/nestremovalpermit/:id/usage', {id: '@id'}, {
            list: {
                method: 'GET',
                isArray: true
            }
        });
    })

    .factory('PermitUsage', function ($http, $resource, CacheFactory) {
        return $resource('api/v1/permit/:id/usage', {id: '@id'}, {
            list: {
                method: 'GET',
                isArray: true
            }
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
    )

    .service('HarvestPermitCategoryType',
        function (ActiveRoleService, ModeratorPrivileges) {
            var otherHarvestPermitTypes = ['LAW_SECTION_TEN'];
            var damageBasedDerogations = ['BIRD', 'MAMMAL', 'NEST_REMOVAL'];
            var otherDerogations = ['LARGE_CARNIVORE_BEAR', 'LARGE_CARNIVORE_LYNX', 'LARGE_CARNIVORE_LYNX_PORONHOITO',
                'LARGE_CARNIVORE_WOLF'];
            var otherPermitTypes = ['WEAPON_TRANSPORTATION', 'DISABILITY', 'DEPORTATION', 'RESEARCH', 'IMPORTING', 'GAME_MANAGEMENT'];
            var dogEventPermitTypes = ['DOG_UNLEASH', 'DOG_DISTURBANCE'];

            this.getMooseTypes = function(list) {
                return _.filter(list, _.matchesProperty('category', 'MOOSELIKE'));
            };

            this.getOtherHarvestPermitTypes = function (list) {
                return _.filter(list, function (item) {
                    return _.indexOf(otherHarvestPermitTypes, item.category) !== -1;
                });
            };

            this.getDamageBasedDerogationTypes = function (list) {
                return _.filter(list, function (item) {
                    return _.indexOf(damageBasedDerogations, item.category) !== -1;
                });
            };

            this.getOtherDerogationTypes = function (list) {
                return _.filter(list, function (item) {
                    return _.indexOf(otherDerogations, item.category) !== -1;
                });
            };

            this.getOtherPermitTypes = function (list) {
                return _.filter(list, function (item) {
                    return _.indexOf(otherPermitTypes, item.category) !== -1;
                });
            };

            this.getDogEventPermitTypes = function (list) {
                return _.filter(list, function (item) {
                    return _.indexOf(dogEventPermitTypes, item.category) !== -1;
                });
            };

            this.isDamageBasedDerogation = function (category) {
                return _.indexOf(damageBasedDerogations, category) !== -1;
            };

            this.isOtherDerogation = function (category) {
                return _.indexOf(otherDerogations, category) !== -1;
            };

            this.hasPermission = function (category) {
                return !(category === 'DISABILITY' &&
                    ActiveRoleService.isModerator() &&
                    !ActiveRoleService.isPrivilegedModerator(ModeratorPrivileges.moderateDisabilityPermitApplication));
            };
        }
    )

    .constant('PermitTypes', {
        MOOSELIKE: "100",
        MOOSELIKE_AMENDMENT: "190",
        FOWL_AND_UNPROTECTED_BIRD: "305",
        ANNUAL_UNPROTECTED_BIRD: "346",
        BEAR_DAMAGE_BASED: "202",
        BEAR_KANNAHOIDOLLINEN: "207",
        LYNX_DAMAGE_BASED: "203",
        LYNX_KANNANHOIDOLLINEN: "208",
        WOLF_DAMAGE_BASED: "204",
        WOLF_KANNANHOIDOLLINEN: "209",
        WOLVERINE_DAMAGE_BASED: "211",
        MAMMAL_DAMAGE_BASED: "215",
        NEST_REMOVAL_BASED: "615",
        LAW_SECTION_TEN_BASED: "255",
        WEAPON_TRANSPORTATION_BASED: "380",
        DISABILITY_BASED: "710",
        DOG_DISTURBANCE_BASED: "830",
        DOG_UNLEASH_BASED: "700",
        DEPORTATION: "395",
        RESEARCH: "396",
        IMPORTING: "360",
        GAME_MANAGEMENT: "512",
        FORBIDDEN_METHOD: "370"
    })

    .service('PermitTypeCode',
        function (ActiveRoleService, ModeratorPrivileges, PermitTypes) {
            this.hasSpeciesAmounts = function (permitTypeCode) {
                return !_.includes([PermitTypes.WEAPON_TRANSPORTATION_BASED,
                                       PermitTypes.DISABILITY_BASED,
                                       PermitTypes.DOG_DISTURBANCE_BASED,
                                       PermitTypes.DOG_UNLEASH_BASED],
                                   permitTypeCode);
            };

            this.hasPermission = function (permitTypeCode) {
                return !(permitTypeCode === PermitTypes.DISABILITY_BASED &&
                    ActiveRoleService.isModerator() &&
                    !ActiveRoleService.isPrivilegedModerator(ModeratorPrivileges.moderateDisabilityPermitApplication));
            };

            this.isRenewalPermitType = function (permitTypeCode) {
                return permitTypeCode === PermitTypes.ANNUAL_UNPROTECTED_BIRD;
            };
        }
    );
