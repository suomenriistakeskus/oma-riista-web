'use strict';

angular.module('app.diary.services', ['ngResource'])

    .constant('DiaryEntryType', {
        harvest: 'HARVEST',
        observation: 'OBSERVATION',
        srva: 'SRVA'
    })

    .service('DiaryEntryUrl', function () {
        this.getUrl = function (imgId, w, h, keepDimensions) {
            return '/api/v1/gamediary/image/' + imgId + '/resize/' + w +
                'x' + h +
                'x' + (keepDimensions ? '1' : '0');
        };
    })

    .factory('DiaryEntryRepositoryFactory', function ($resource, Helpers, DiaryEntryType, DiaryEntryUrl,
                                                      GameSpeciesCodes, SrvaOtherSpeciesService) {

        function decorateRepository(repository) {
            angular.extend(repository.prototype, {
                setDateAndTime: function (date, time) {
                    var dateTime = moment(date).toDate();

                    dateTime.setHours(time.slice(0, 2));
                    dateTime.setMinutes(time.slice(3));

                    this.pointOfTime = Helpers.dateTimeToString(dateTime);
                },
                saveOrUpdate: function () {
                    if (this.fields) {
                        delete this.fields.species; //gameSpeciesDTO needs constructor etc
                    }

                    if (this.permittedMethod && !this.permittedMethod.other) {
                        delete this.permittedMethod.description;
                    }

                    if (!_.isUndefined(this.permitNumberRequired)) {
                        delete this.permitNumberRequired;
                    }

                    if (_.has(this, 'geoLocation.zoom')) {
                        delete this.geoLocation.zoom;
                    }

                    if (this.isHarvest()) {
                        if (this.isMoose() || this.isPermitBasedDeer()) {
                            _.each(this.specimens, function (specimen) {
                                delete specimen.weight;

                                if (!_.isBoolean(specimen.notEdible)) {
                                    specimen.notEdible = false;
                                }
                            });
                        } else {
                            _.each(this.specimens, function (specimen) {
                                delete specimen.weightEstimated;
                                delete specimen.weightMeasured;
                                delete specimen.fitnessClass;
                                delete specimen.antlersType;
                                delete specimen.antlersWidth;
                                delete specimen.antlerPointsLeft;
                                delete specimen.antlerPointsRight;
                                delete specimen.notEdible;
                                delete specimen.additionalInfo;
                            });
                        }
                        if (!this.isAntlersPossible()) {
                            _.each(this.specimens, function (specimen) {
                                delete specimen.antlersType;
                                delete specimen.antlersWidth;
                                delete specimen.antlerPointsLeft;
                                delete specimen.antlerPointsRight;
                            });
                        }
                    }

                    return this.id ? this.$update() : this.$save();
                },
                isHarvest: function () {
                    return this.type === DiaryEntryType.harvest;
                },
                isObservation: function () {
                    return this.type === DiaryEntryType.observation;
                },
                isSrva: function () {
                    return this.type === DiaryEntryType.srva;
                },
                isOtherSpecies: function () {
                    return this.gameSpeciesCode === SrvaOtherSpeciesService.getOtherSpeciesCode();
                },
                isMoose: function () {
                    return GameSpeciesCodes.isMoose(this.gameSpeciesCode);
                },
                isMooselike: function () {
                    return GameSpeciesCodes.isDeer(this.gameSpeciesCode);
                },
                isPermitBasedDeer: function () {
                    return GameSpeciesCodes.isPermitBasedDeer(this.gameSpeciesCode);
                },
                isAntlersPossible: function () {
                    return (this.isMoose() || this.isPermitBasedDeer()) && !_.isEmpty(this.specimens) &&
                        this.specimens[0].age === 'ADULT' && this.specimens[0].gender === 'MALE';
                },
                getRepository: function () {
                    return repository;
                },
                getImageUrl: DiaryEntryUrl.getUrl
            });

            return repository;
        }


        function createWithBaseUrlAndType(baseUrl) {
            var repository = $resource(baseUrl + '/:id', {"id": "@id"}, {
                get: {
                    method: 'GET',
                    transformResponse: function (data, headers, status) {
                        var result = angular.fromJson(data);

                        if (status >= 400) {
                            return result;
                        }

                        return SrvaOtherSpeciesService.replaceNullWithOtherSpeciesCode(result);
                    }
                },
                save: {
                    method: 'POST',
                    transformRequest: function (data, header) {
                        SrvaOtherSpeciesService.replaceOtherSpeciesCodeWithNull(data);
                        return angular.toJson(data);
                    }
                },
                update: {
                    method: 'PUT',
                    transformRequest: function (data, header) {
                        SrvaOtherSpeciesService.replaceOtherSpeciesCodeWithNull(data);
                        return angular.toJson(data);
                    }
                },
                delete: {method: 'DELETE'},
                getRelationship: {
                    url: baseUrl + '/:id/relationship',
                    method: 'GET'
                }
            });

            // Add methods
            return decorateRepository(repository);
        }

        return {
            create: createWithBaseUrlAndType,
            decorateRepository: decorateRepository
        };
    })

    .factory('Harvest', function (DiaryEntryRepositoryFactory, DiaryEntryType, DiaryEntrySpecimenFormService, WGS84) {
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
            var geoLocation = opts.geoLocation;

            if (geoLocation) {
                var etrs = WGS84.toETRS(geoLocation.lat, geoLocation.lng);

                geoLocation = {
                    latitude: etrs.lat,
                    longitude: etrs.lng,
                    zoom: geoLocation.zoom
                };
            }

            var gameSpeciesCode = opts.gameSpeciesCode ? _.parseInt(opts.gameSpeciesCode) : null;

            var harvest = new Harvest({
                id: null,
                type: DiaryEntryType.harvest,
                gameSpeciesCode: gameSpeciesCode,
                geoLocation: geoLocation || {},
                authoredByMe: true,
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

    .factory('Observation', function (DiaryEntryRepositoryFactory, DiaryEntryType, WGS84) {
        var Observation = DiaryEntryRepositoryFactory.create('api/v1/gamediary/observation');

        /**
         * Create a new Observation object (not yet persisted).
         *
         * @param {{
         *   gameSpeciesCode : preselected official code of game species
         *   geoLocation: predefined geolocation
         * }} opts Options to populate Observation object with
         */
        Observation.createTransient = function (opts) {
            var geoLocation = opts.geoLocation;

            if (geoLocation) {
                var etrs = WGS84.toETRS(geoLocation.lat, geoLocation.lng);

                geoLocation = {
                    latitude: etrs.lat,
                    longitude: etrs.lng,
                    zoom: geoLocation.zoom
                };
            }

            var gameSpeciesCode = opts.gameSpeciesCode ? _.parseInt(opts.gameSpeciesCode) : null;

            return new Observation({
                id: null,
                type: DiaryEntryType.observation,
                gameSpeciesCode: gameSpeciesCode,
                geoLocation: geoLocation || {},
                canEdit: true
            });
        };

        return Observation;
    })

    .factory('DiaryEntries', function ($resource, DiaryEntryType, DiaryEntryRepositoryFactory, SrvaOtherSpeciesService) {
        var repository = $resource('api/v1/gamediary', {}, {
            mine: {
                method: 'GET',
                isArray: true,
                transformResponse: function (data, headers, status) {
                    var result = angular.fromJson(data);

                    if (status >= 400) {
                        return result;
                    }

                    return SrvaOtherSpeciesService.replaceNullsWithOtherSpeciesCodeInEntries(result);
                }
            },
            reports: {url: 'api/v1/gamediary/reported', method: 'GET', isArray: true},
            todo: {url: 'api/v1/gamediary/todo', method: 'GET'},
            acceptedToPermit: {url: 'api/v1/gamediary/accepted/permit/:permitId', method: 'GET', isArray: true}
        });

        return DiaryEntryRepositoryFactory.decorateRepository(repository);
    })

    .factory('CheckHunterNumber', function (HttpPost) {
        return {
            check: function (hunterNumber) {
                return HttpPost.post('api/v1/gamediary/checkHunterNumber', {hunterNumber: hunterNumber});
            }
        };
    })

    .factory('ObservationFieldRequirements', function () {
        var ObservationFieldRequirements = function (opts) {
            angular.extend(this, opts);
        };

        var allMooseAmountFields =
            ['mooselikeMaleAmount', 'mooselikeFemaleAmount', 'mooselikeFemale1CalfAmount',
                'mooselikeFemale2CalfsAmount', 'mooselikeFemale3CalfsAmount', 'mooselikeUnknownSpecimenAmount'];
        var allMooselikeAmountFields = _.union(allMooseAmountFields, ['mooselikeFemale4CalfsAmount']);
        var allAmountFields = _.union(['amount'], allMooselikeAmountFields);
        var allPossibleContextSensitiveBaseFields = allAmountFields;

        var allPossibleContextSensitiveSpecimenFields = ['age', 'gender', 'state', 'marking'];

        function translateMetadataFieldToObservationField(fieldName) {
            return fieldName === 'amount' ? 'totalSpecimenAmount' : fieldName;
        }

        ObservationFieldRequirements.create = function (opts) {
            return new ObservationFieldRequirements(opts);
        };

        ObservationFieldRequirements.getAllMooseAmountFields = function () {
            return angular.copy(allMooseAmountFields);
        };

        ObservationFieldRequirements.getAllAmountFields = function () {
            return angular.copy(allAmountFields);
        };

        ObservationFieldRequirements.prototype.getFields = function () {
            return _.keys(this.fields);
        };

        ObservationFieldRequirements.prototype.getSpecimenFields = function () {
            return _.keys(this.specimenFields);
        };

        ObservationFieldRequirements.prototype.getAvailableGameAges = function () {
            return this.allowedAges;
        };

        ObservationFieldRequirements.prototype.getAvailableGameStates = function () {
            return this.allowedStates;
        };

        ObservationFieldRequirements.prototype.getAvailableGameMarkings = function () {
            return this.allowedMarkings;
        };

        ObservationFieldRequirements.prototype.isFieldRequired = function (fieldName) {
            return this.fields[fieldName] === 'YES' || this.specimenFields[fieldName] === 'YES';
        };

        ObservationFieldRequirements.prototype.isFieldLegal = function (fieldName) {
            var requirement = this.fields[fieldName] || this.specimenFields[fieldName];
            return requirement === 'YES' || requirement === 'VOLUNTARY';
        };

        ObservationFieldRequirements.prototype.getAmountFields = function () {
            var self = this;

            return _.filter(allAmountFields, function (amountFieldName) {
                return self.isFieldLegal(amountFieldName);
            });
        };

        ObservationFieldRequirements.prototype.isSumOfAmountFieldsValid = function (observation) {
            var amountFields = this.getAmountFields();

            if (amountFields.length === 0) {
                return true;
            }

            var sum = _.chain(amountFields)
                .map(function (amountFieldName) {
                    var observationFieldName = translateMetadataFieldToObservationField(amountFieldName);
                    return observation[observationFieldName];
                })
                .filter(function (amountField) {
                    return _.isFinite(amountField);
                })
                .sum();

            return sum > 0;
        };

        ObservationFieldRequirements.prototype.resetIllegalObservationFields = function (observation) {
            if (!observation || !observation.isObservation()) {
                return;
            }

            var self = this;

            _.forEach(allPossibleContextSensitiveBaseFields, function (fieldName) {
                var isNonMooselikeAmountField = fieldName === 'amount';
                var observationFieldName = translateMetadataFieldToObservationField(fieldName);

                if (_.has(observation, observationFieldName) && !self.isFieldLegal(fieldName)) {
                    delete observation[observationFieldName];

                    if (isNonMooselikeAmountField) {
                        delete observation.specimens;
                    }
                } else if (_.includes(allAmountFields, fieldName) && !_.isFinite(observation[observationFieldName]) &&
                    self.isFieldRequired(fieldName)) {

                    if (isNonMooselikeAmountField) {
                        observation[observationFieldName] = 1;
                        observation.specimens = [];
                    } else {
                        observation[observationFieldName] = 0;
                    }
                }
            });

            if (!_.isEmpty(observation.specimens)) {
                _.forEach(observation.specimens, function (specimen) {

                    _.forEach(allPossibleContextSensitiveSpecimenFields, function (fieldName) {
                        var specimenFieldName = translateMetadataFieldToObservationField(fieldName);

                        if (specimen[specimenFieldName] && !self.isFieldLegal(fieldName)) {
                            delete specimen[specimenFieldName];
                        }
                    });

                    if (specimen.age && !_.includes(self.getAvailableGameAges(), specimen.age)) {
                        specimen.age = null;
                    }

                    if (specimen.state && !_.includes(self.getAvailableGameStates(), specimen.state)) {
                        specimen.state = null;
                    }

                    if (specimen.marking && !_.includes(self.getAvailableGameMarkings(), specimen.marking)) {
                        specimen.marking = null;
                    }
                });
            }
        };

        return ObservationFieldRequirements;
    })

    .service('ObservationFieldsMetadata', function ($resource, ObservationFieldRequirements) {
        var ObservationFieldsMetadata = $resource('api/v1/gamediary/observation/metadata', {}, {
            query: {method: 'GET', isArray: true},
            forSpecies: {
                url: 'api/v1/gamediary/observation/metadata/:gameSpeciesCode',
                method: 'GET',
                params: {gameSpeciesCode: '@gameSpeciesCode'}
            }
        });

        var _findContextSensitiveFieldSets = function (self, withinMooseHunting, observationType) {
            if (!self.contextSensitiveFieldSets) {
                return [];
            }

            withinMooseHunting = withinMooseHunting || false;

            return _.filter(self.contextSensitiveFieldSets, function (fieldSet) {
                return fieldSet.withinMooseHunting === withinMooseHunting &&
                    (!observationType || fieldSet.type === observationType);
            });
        };

        var _hasMooseHuntingContexts = function (self) {
            return _findContextSensitiveFieldSets(self, true).length > 0;
        };

        ObservationFieldsMetadata.prototype.getAvailableObservationTypes = function (withinMooseHunting) {
            return _.pluck(_findContextSensitiveFieldSets(this, withinMooseHunting), 'type');
        };

        ObservationFieldsMetadata.prototype.getFieldRequirements = function (withinMooseHunting, observationType) {
            if (!this.gameSpeciesCode) {
                return null;
            }

            var requirements = {
                fields: angular.copy(this.baseFields || {}),
                specimenFields: angular.copy(this.specimenFields || {})
            };

            if (observationType) {
                var ctxSensitiveFieldSets = _findContextSensitiveFieldSets(this, withinMooseHunting, observationType);
                var ctxSensitiveFieldSet = ctxSensitiveFieldSets.length === 1 ? ctxSensitiveFieldSets[0] : null;

                if (ctxSensitiveFieldSet) {
                    if (ctxSensitiveFieldSet.allowedAges) {
                        requirements.allowedAges = ctxSensitiveFieldSet.allowedAges;
                    }
                    if (ctxSensitiveFieldSet.allowedStates) {
                        requirements.allowedStates = ctxSensitiveFieldSet.allowedStates;
                    }
                    if (ctxSensitiveFieldSet.allowedMarkings) {
                        requirements.allowedMarkings = ctxSensitiveFieldSet.allowedMarkings;
                    }

                    _.extend(requirements.fields, ctxSensitiveFieldSet.baseFields);
                    _.extend(requirements.specimenFields, ctxSensitiveFieldSet.specimenFields);
                }
            }

            return ObservationFieldRequirements.create(requirements);
        };

        function _isWithinMooseHuntingSelectionRequired(self) {
            var fieldRequirements = self.getFieldRequirements();
            return fieldRequirements.fields.withinMooseHunting === 'YES';
        }

        function _isObservationTypeLegalForMooseHuntingSelection(self, observationType, withinMooseHunting) {
            return _.find(self.getAvailableObservationTypes(withinMooseHunting), function (obsTypeCandidate) {
                return obsTypeCandidate === observationType;
            });
        }

        ObservationFieldsMetadata.prototype.resetIllegalObservationFields = function (observation) {
            if (!observation || !observation.isObservation()) {
                return;
            }

            if (this.gameSpeciesCode) {
                if (_.isBoolean(observation.withinMooseHunting)) {
                    if (!_hasMooseHuntingContexts(this)) {
                        delete observation.withinMooseHunting;
                    }
                } else if (_isWithinMooseHuntingSelectionRequired(this)) {
                    observation.withinMooseHunting = false;
                }

                var withinMooseHunting = observation.withinMooseHunting || false;
                var type = observation.observationType;

                if (type && !_isObservationTypeLegalForMooseHuntingSelection(this, type, withinMooseHunting)) {
                    observation.observationType = null;
                }
            }
        };

        return ObservationFieldsMetadata;
    })

    .service('DiaryEntryService', function ($q, $uibModal, $state, offCanvasStack,
                                            GameDiaryParameters, GameDiarySrvaParameters, Harvest, Observation, Srva,
                                            ObservationFieldsMetadata, DiaryEntryUrl) {

        var self = this;

        this.getUrl = DiaryEntryUrl.getUrl;

        function _getGameDiaryParameters() {
            return GameDiaryParameters.query().$promise;
        }

        function _getGameDiarySrvaParameters() {
            return GameDiarySrvaParameters.query().$promise;
        }

        this.openDiaryEntryForm = function (diaryEntry) {
            var controller, templateUrl;
            var resolve = {
                entry: _.constant(diaryEntry),
                parameters: diaryEntry.isSrva() ? _getGameDiarySrvaParameters : _getGameDiaryParameters
            };

            if (diaryEntry.isHarvest()) {
                templateUrl = 'diary/harvest-form.html';
                controller = 'HarvestFormController';

                angular.extend(resolve, {
                    relationship: function () {
                        return diaryEntry.id ? Harvest.getRelationship({id: diaryEntry.id}).$promise : null;
                    }
                });

            } else if (diaryEntry.isObservation()) {
                templateUrl = 'diary/observation-form.html';
                controller = 'ObservationFormController';

                angular.extend(resolve, {
                    relationship: function () {
                        return diaryEntry.id ? Observation.getRelationship({id: diaryEntry.id}).$promise : null;
                    },
                    fieldMetadataForObservationSpecies: function () {
                        if (diaryEntry.gameSpeciesCode) {
                            return ObservationFieldsMetadata.forSpecies({gameSpeciesCode: diaryEntry.gameSpeciesCode}).$promise;
                        }
                        return null;
                    }
                });
            } else if (diaryEntry.isSrva()) {
                templateUrl = 'diary/srva-form.html';
                controller = 'SrvaFormController';

                angular.extend(resolve, {
                    relationship: function () {
                        return diaryEntry.id ? Srva.getRelationship({id: diaryEntry.id}).$promise : null;
                    }
                });

            } else {
                return $q.reject('unknown entry type');
            }

            return offCanvasStack.open({
                controller: controller,
                templateUrl: templateUrl,
                largeDialog: false,
                resolve: resolve
            }).result;
        };

        this.createHarvestForPermit = function (permit, userId) {
            return $state.go('profile.diary.addHarvest', {
                id: userId || 'me',
                permitNumber: permit.permitNumber
            });
        };

        this.createHarvestForPermitByFields = function (fields, harvest) {
            if (harvest) {
                return $state.go('profile.diary.editHarvest', {
                    id: 'none',
                    entryId: harvest.id,
                    permitNumberRequired: true
                });
            }
            return $state.go('profile.diary.addHarvest', {
                id: 'none',
                gameSpeciesCode: fields.species.code,
                permitNumberRequired: true
            });
        };

        this.showSidebar = function (diaryEntry) {
            return offCanvasStack.open({
                templateUrl: 'diary/show-sidebar.html',
                largeDialog: false,
                resolve: {
                    entry: function () {
                        if (diaryEntry.isHarvest()) {
                            return Harvest.get({id: diaryEntry.id}).$promise;
                        } else if (diaryEntry.isObservation()) {
                            return Observation.get({id: diaryEntry.id}).$promise;
                        } else if (diaryEntry.isSrva()) {
                            return Srva.get({id: diaryEntry.id}).$promise;
                        }

                        return $q.reject('invalid diaryEntry.type');
                    },
                    parameters: diaryEntry.isSrva() ? _getGameDiarySrvaParameters : _getGameDiaryParameters
                },
                controller: 'DiarySidebarShowController'
            });
        };

        function transitionToDiaryEntryEditState(diaryEntry, opts) {
            var state;

            if (diaryEntry.isHarvest()) {
                state = 'profile.diary.editHarvest';
            } else if (diaryEntry.isObservation()) {
                state = 'profile.diary.editObservation';
            } else if (diaryEntry.isSrva()) {
                state = 'profile.diary.editSrva';
            }

            return $state.go(state, opts);
        }

        this.edit = function (diaryEntry) {
            return transitionToDiaryEntryEditState(diaryEntry, {
                id: 'me',
                entryId: diaryEntry.id
            });
        };

        this.copy = function (diaryEntry) {
            return transitionToDiaryEntryEditState(diaryEntry, {
                id: 'me',
                entryId: diaryEntry.id,
                copy: 'true'
            });
        };

        this.editSpecimen = function (entry, parameters, availableFields, observationFieldRequirements) {
            var templateUrl;

            if (entry.isHarvest()) {
                templateUrl = 'diary/harvest-specimen.html';
            } else if (entry.isObservation()) {
                templateUrl = 'diary/observation-specimen.html';
            } else if (entry.isSrva()) {
                templateUrl = 'diary/srva-specimen.html';
            }

            return offCanvasStack.open({
                templateUrl: templateUrl,
                largeDialog: true,
                resolve: {
                    entry: _.constant(entry),
                    parameters: _.constant(parameters),
                    availableFields: _.constant(availableFields || {}),
                    observationFieldRequirements: _.constant(observationFieldRequirements || null)
                },
                controller: 'DiaryEntrySpecimenModalController'
            }).result;
        };

        this.openRemoveForm = function (diaryEntry) {
            return $uibModal.open({
                templateUrl: 'diary/remove.html',
                resolve: {
                    entry: _.constant(diaryEntry)
                },
                controller: 'DiaryRemoveController'
            }).result;
        };

        this.remove = function (diaryEntry) {
            var deleteFn;

            if (diaryEntry.isHarvest()) {
                deleteFn = Harvest.delete;
            } else if (diaryEntry.isObservation()) {
                deleteFn = Observation.delete;
            } else if (diaryEntry.isSrva()) {
                deleteFn = Srva.delete;
            }

            return deleteFn({id: diaryEntry.id}).$promise;
        };

        this.image = function (entry, uuid, tmp) {
            return $uibModal.open({
                templateUrl: 'diary/upload_image.html',
                size: 'sm',
                resolve: {
                    entry: _.constant(entry),
                    uuid: _.constant(uuid),
                    tmp: _.constant(tmp)
                },
                controller: 'DiaryImageController'
            }).result;
        };
    })

    .service('DiaryEntrySpecimenFormService', function () {
        var self = this;

        this.MAX_SPECIMEN_AMOUNT = 999;
        this.MAX_VISIBLE_AMOUNT = 25;

        this.getMaxSpecimenCountForHarvest = function (gameDiaryParameters, gameSpeciesCode) {
            return gameDiaryParameters.isMultipleSpecimensAllowedForHarvestSpecies(gameSpeciesCode) ? self.MAX_SPECIMEN_AMOUNT : 1;
        };

        this.getMaxSpecimenCountForObservation = function () {
            return self.MAX_SPECIMEN_AMOUNT;
        };

        this.initAmountAndSpecimens = function (entry) {
            // initialize invalid amount to 1.
            if (!_.isFinite(entry.totalSpecimenAmount)) {
                entry.totalSpecimenAmount = 1;
            }

            if (!entry.specimens) {
                entry.specimens = [];
            }

            self.setSpecimenCount(entry, entry.totalSpecimenAmount);
        };

        this.setSpecimenCount = function (entry, count) {
            if (count > 0) {
                var limit = Math.min(entry.totalSpecimenAmount, self.MAX_VISIBLE_AMOUNT);

                if (!_.isArray(entry.specimens)) {
                    entry.specimens = [];
                }
                var arrSize = entry.specimens.length;

                if (count > arrSize) {
                    for (var i = arrSize; i < limit; i++) {
                        self.addNewSpecimen(entry, 0);
                    }
                } else {
                    entry.specimens.length = count;
                }
            }
        };

        this.addNewSpecimen = function (entry, index) {
            var specimen = {};

            if (index) {
                entry.specimens.splice(index, 0, specimen);
            } else {
                entry.specimens.push(specimen);
            }
        };

        this.removeSpecimen = function (entry, index) {
            if (entry.totalSpecimenAmount > 1) {
                if (index >= 0) {
                    entry.specimens.splice(index, 1);
                }
                entry.totalSpecimenAmount--;
            }
        };
    });
