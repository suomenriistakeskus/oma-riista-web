'use strict';

angular.module('app.diary.harvest.model', [])
    .factory('Harvest', function (DiaryEntryRepositoryFactory, DiaryEntrySpecimenFormService, DiaryEntryType) {
        var Harvest = DiaryEntryRepositoryFactory.create('api/v1/gamediary/harvest');

        /**
         * Create a new (not-yet-persisted) Harvest object.
         *
         * @param {{
         *   gameSpeciesCode : preselected official code of game species
         *   geoLocation: predefined geolocation
         *   permitNumber : predefined permit number
         * }} opts Options to populate Harvest object with
         */
        Harvest.createTransient = function (opts) {
            var harvest = new Harvest({
                id: null,
                type: DiaryEntryType.harvest,
                gameSpeciesCode: opts.gameSpeciesCode,
                geoLocation: opts.geoLocation || {},
                canEdit: true,
                permitNumber: opts.permitNumber,
                permitNumberRequired: opts.permitNumberRequired
            });

            DiaryEntrySpecimenFormService.initAmountAndSpecimens(harvest);

            return harvest;
        };

        Harvest.prototype.createCopyForModeratorMassInsertion = function () {
            var copy = angular.copy(this);

            delete copy.id;
            delete copy.rev;
            delete copy.stateAcceptedToHarvestPermit;
            delete copy.rhyId;
            delete copy.totalSpecimenAmount;

            copy.canEdit = true;
            copy.imageIds = [];
            copy.description = null;
            copy.specimens = [];

            return copy;
        };

        return Harvest;
    })

    .factory('RequiredHarvestReportFields', function () {
        // Field is always mandatory.
        var YES = 'YES';

        // Field is voluntary; either null or non-null value is allowed.
        var VOLUNTARY = 'VOLUNTARY';

        // Field is illegal.
        var NO = 'NO';

        function RequiredHarvestReportFields(fields) {
            angular.extend(this, fields);
        }

        function _getField(self, fieldName) {
            return _.get(self, fieldName, NO);
        }

        RequiredHarvestReportFields.prototype.isIllegal = function (fieldName) {
            var requirement = _getField(this, fieldName);

            switch (requirement) {
                case YES:
                case VOLUNTARY:
                    return false;

                case NO:
                    return true;

                default:
                    return true;
            }
        };

        RequiredHarvestReportFields.prototype.isVisible = function (fieldName) {
            return !this.isIllegal(fieldName);
        };

        RequiredHarvestReportFields.prototype.isRequired = function (fieldName) {
            var requirement = _getField(this, fieldName);

            switch (requirement) {
                case YES:
                    return true;

                case VOLUNTARY:
                case NO:
                    return false;

                default:
                    return false;
            }
        };

        RequiredHarvestReportFields.prototype.removeIllegalFields = function (harvest) {
            var self = this;

            _.forOwn(self, function (value, fieldName) {
                if (self.isIllegal(fieldName) && fieldName in harvest) {
                    delete harvest[fieldName];
                }
            });
        };

        return {
            create: function (fields) {
                return new RequiredHarvestReportFields(fields);
            }
        };
    })

    .factory('RequiredHarvestSpecimenFields', function () {
        // Field is always mandatory.
        var YES = 'YES';

        // Field is mandatory if age is young; otherwise illegal.
        var YES_IF_YOUNG = 'YES_IF_YOUNG';

        // Field is mandatory in case of adult male; otherwise illegal.
        var YES_IF_ADULT_MALE = 'YES_IF_ADULT_MALE';

        // Field is mandatory in case of adult male having antlers (not lost); otherwise illegal.
        var YES_IF_ANTLERS_PRESENT = 'YES_IF_ANTLERS_PRESENT';

        // Field is voluntary. Either null or non-null is allowed.
        var VOLUNTARY = 'VOLUNTARY';

        // Field is voluntary if age is young; otherwise illegal.
        var VOLUNTARY_IF_YOUNG = 'VOLUNTARY_IF_YOUNG';

        // Field is voluntary in case of adult male; otherwise illegal.
        var VOLUNTARY_IF_ADULT_MALE = 'VOLUNTARY_IF_ADULT_MALE';

        // Field is voluntary in case of adult male having antlers (not lost); otherwise illegal.
        var VOLUNTARY_IF_ANTLERS_PRESENT = 'VOLUNTARY_IF_ANTLERS_PRESENT';

        // Field is illegal.
        var NO = 'NO';

        // Field may exist in case of adult male having antlers (not lost); however, the field should not
        // be displayed or be editable in client.
        var DEPRECATED_ANTLER_DETAIL = 'DEPRECATED_ANTLER_DETAIL';

        // Field is allowed to exist; however, the field should not be displayed or be editable in client.
        var ALLOWED_BUT_HIDDEN = 'ALLOWED_BUT_HIDDEN';

        function RequiredHarvestSpecimenFields(fields) {
            angular.extend(this, fields);
        }

        function _getField(self, fieldName) {
            return _.get(self, fieldName, NO);
        }

        RequiredHarvestSpecimenFields.prototype.isIllegal = function (fieldName, obj) {
            var requirement = _getField(this, fieldName);

            switch (requirement) {
                case YES:
                case VOLUNTARY:
                    return false;

                case YES_IF_YOUNG:
                case VOLUNTARY_IF_YOUNG:
                    return !obj || obj.age !== 'YOUNG';

                case YES_IF_ADULT_MALE:
                case VOLUNTARY_IF_ADULT_MALE:
                    return !obj || obj.age !== 'ADULT' || obj.gender !== 'MALE';

                case YES_IF_ANTLERS_PRESENT:
                case VOLUNTARY_IF_ANTLERS_PRESENT:
                    return !obj || obj.age !== 'ADULT' || obj.gender !== 'MALE' || obj.antlersLost === true;

                case NO:
                case DEPRECATED_ANTLER_DETAIL:
                case ALLOWED_BUT_HIDDEN:
                    return true;

                default:
                    return true;
            }
        };

        RequiredHarvestSpecimenFields.prototype.isVisible = function (fieldName, obj) {
            return !this.isIllegal(fieldName, obj);
        };

        RequiredHarvestSpecimenFields.prototype.isRequired = function (fieldName, obj) {
            var requirement = _getField(this, fieldName);

            switch (requirement) {
                case YES:
                    return true;

                case YES_IF_YOUNG:
                    return !!obj && obj.age === 'YOUNG';

                case YES_IF_ADULT_MALE:
                    return !!obj && obj.age === 'ADULT' && obj.gender === 'MALE';

                case YES_IF_ANTLERS_PRESENT:
                    return !!obj && obj.age === 'ADULT' && obj.gender === 'MALE' && obj.antlersLost !== true;

                case VOLUNTARY:
                case VOLUNTARY_IF_YOUNG:
                case VOLUNTARY_IF_ADULT_MALE:
                case VOLUNTARY_IF_ANTLERS_PRESENT:
                case DEPRECATED_ANTLER_DETAIL:
                case NO:
                case ALLOWED_BUT_HIDDEN:
                    return false;

                default:
                    return false;
            }
        };

        RequiredHarvestSpecimenFields.prototype.removeIllegalFields = function (specimen) {
            var self = this;

            _.forOwn(self, function (value, fieldName) {
                if (self.isIllegal(fieldName, specimen) && fieldName in specimen) {
                    delete specimen[fieldName];
                }
            });
        };

        return {
            create: function (fields) {
                return new RequiredHarvestSpecimenFields(fields);
            }
        };
    })

    .factory('RequiredHarvestFields', function (RequiredHarvestReportFields, RequiredHarvestSpecimenFields) {
        function RequiredHarvestFields(requiredFields) {
            var fields = requiredFields || {};
            var reportFields = fields.report || {};
            var specimenFields = fields.specimen || {};

            // Replace field requirements objects with enhanced ones having custom API.
            this.report = RequiredHarvestReportFields.create(reportFields);
            this.specimen = RequiredHarvestSpecimenFields.create(specimenFields);
        }

        RequiredHarvestFields.prototype.isIllegalReportField = function (fieldName) {
            return this.report.isIllegal(fieldName);
        };

        RequiredHarvestFields.prototype.isVisibleReportField = function (fieldName) {
            return this.report.isVisible(fieldName);
        };

        RequiredHarvestFields.prototype.isRequiredReportField = function (fieldName) {
            return this.report.isRequired(fieldName);
        };

        RequiredHarvestFields.prototype.isIllegalSpecimenField = function (fieldName, obj) {
            return this.specimen.isIllegal(fieldName, obj);
        };

        RequiredHarvestFields.prototype.isVisibleSpecimenField = function (fieldName, obj) {
            return this.specimen.isVisible(fieldName, obj);
        };

        RequiredHarvestFields.prototype.isRequiredSpecimenField = function (fieldName, obj) {
            return this.specimen.isRequired(fieldName, obj);
        };

        RequiredHarvestFields.prototype.removeIllegalFields = function (harvest) {
            var self = this;

            self.report.removeIllegalFields(harvest);

            _.forEach(harvest.specimens, function (specimen) {
                self.specimen.removeIllegalFields(specimen);
            });
        };

        return {
            create: function (fields) {
                return new RequiredHarvestFields(fields);
            }
        };
    })

    .service('HarvestFieldsService', function ($http, $q, RequiredHarvestFields) {
        this.getForPersistedHarvest = function (id) {
            return $http.get('/api/v1/gamediary/harvest/fields/' + id)
                .then(function (response) {
                    return response.data;
                });
        };

        this.getForHarvest = function (params) {
            var geoLocation = _.get(params, 'geoLocation', {});

            if (!params
                || !params.gameSpeciesCode
                || !params.harvestDate
                || !_.isBoolean(params.withPermit)
                || !geoLocation.latitude || !geoLocation.longitude) {

                return $q.when(null);
            }

            return $http.get('/api/v1/gamediary/harvest/fields', {
                    params: {
                        gameSpeciesCode: params.gameSpeciesCode,
                        withPermit: params.withPermit,
                        harvestDate: params.harvestDate,
                        longitude: geoLocation.longitude,
                        latitude: geoLocation.latitude
                    }
                })
                .then(function (response) {
                    var ret = response.data;

                    // Replace field requirements object with enhanced one having custom API.
                    ret.fields = RequiredHarvestFields.create(ret.fields);

                    return ret;
                });
        };

        this.fixStateBeforeSaving = function (requiredFields, harvest) {
            delete harvest.permitNumberRequired;

            if (harvest.permittedMethod) {
                if (!harvest.permittedMethod.other) {
                    delete harvest.permittedMethod.description;
                }

                if (!harvest.permittedMethod.tapeRecorders &&
                    !harvest.permittedMethod.traps &&
                    !harvest.permittedMethod.other) {

                    delete harvest.permittedMethod;
                }
            }

            if (!!requiredFields) {
                requiredFields.removeIllegalFields(harvest);

                _.forEach(harvest.specimens, function (specimen) {
                    var notEdibleAllowed = !requiredFields.isIllegalSpecimenField('notEdible', specimen);
                    var antlersLostAllowed = !requiredFields.isIllegalSpecimenField('antlersLost', specimen);
                    var aloneAllowed = !requiredFields.isIllegalSpecimenField('alone', specimen);

                    if (!_.isBoolean(specimen.notEdible) && notEdibleAllowed) {
                        specimen.notEdible = false;
                    }
                    if (!_.isBoolean(specimen.antlersLost) && antlersLostAllowed) {
                        specimen.antlersLost = false;
                    }
                    if (!_.isBoolean(specimen.alone) && aloneAllowed) {
                        specimen.alone = false;
                    }
                });
            }

            if (!harvest.isPermitBasedMooselike()) {
                var weightNotAllowed = harvest.isGreySeal() && harvest.huntingMethod === 'SHOT_BUT_LOST';

                _.forEach(harvest.specimens, function (specimen) {
                    if (weightNotAllowed) {
                        delete specimen.weight;
                    }
                });
            }
        };
    });
