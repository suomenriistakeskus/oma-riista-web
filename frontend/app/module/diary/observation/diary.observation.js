'use strict';

angular.module('app.diary.observation', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.diary.addObservation', {
                url: '/add_observation?gameSpeciesCode',
                wideLayout: true,
                views: {
                    '@profile': {
                        templateUrl: 'diary/observation/edit-observation.html',
                        controller: 'OpenDiaryEntryFormController'
                    }
                },
                params: {
                    id: 'me',
                    gameSpeciesCode: null
                },
                resolve: {
                    entry: function ($stateParams, DiaryListViewState, Observation) {
                        return Observation.createTransient({
                            gameSpeciesCode: $stateParams.gameSpeciesCode
                                ? _.parseInt($stateParams.gameSpeciesCode) : null
                        });
                    }
                }
            })

            .state('profile.diary.editObservation', {
                url: '/edit_observation?entryId',
                wideLayout: true,
                views: {
                    '@profile': {
                        templateUrl: 'diary/observation/edit-observation.html',
                        controller: 'OpenDiaryEntryFormController'
                    }
                },
                params: {
                    id: 'me',
                    entryId: undefined
                },
                resolve: {
                    entry: function ($stateParams, MapState, Observation) {
                        return Observation.get({id: $stateParams.entryId}).$promise.then(function (observation) {
                            var zoom = MapState.getZoom();

                            if (zoom) {
                                observation.geoLocation.zoom = zoom;
                            }

                            return observation;
                        });
                    }
                }
            });
    })

    .factory('Observation', function (DiaryEntryRepositoryFactory, DiaryEntryType) {
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
            return new Observation({
                id: null,
                type: DiaryEntryType.observation,
                gameSpeciesCode: opts.gameSpeciesCode,
                geoLocation: opts.geoLocation || {},
                canEdit: true
            });
        };

        Observation.createObservationForMooseHarvest = function (harvest) {
            var firstSpecimen = _.size(harvest.specimens) > 0 ? harvest.specimens[0] : {};
            var isAdult = firstSpecimen.age === 'ADULT';
            var isMale = firstSpecimen.gender === 'MALE';
            var isFemale = firstSpecimen.gender === 'FEMALE';

            return new Observation({
                canEdit: true,
                type: DiaryEntryType.observation,
                observationType: 'NAKO',
                gameSpeciesCode: harvest.gameSpeciesCode,
                geoLocation: harvest.geoLocation,
                pointOfTime: harvest.pointOfTime,
                authorInfo: harvest.authorInfo,
                actorInfo: harvest.actorInfo,
                huntingDayId: harvest.huntingDayId,
                totalSpecimenAmount: harvest.totalSpecimenAmount,
                withinMooseHunting: true,
                mooselikeMaleAmount: isAdult && isMale ? 1 : 0,
                mooselikeFemaleAmount: isAdult && isFemale ? 1 : 0,
                specimens: [],
                imageIds: []
            });
        };

        return Observation;
    })

    .service('ObservationFieldsMetadata', function ($filter, $resource, $http, $rootScope,
                                                    ObservationFieldRequirements) {
        var rangeFilter = $filter('range');

        function appendTransform(defaults, transform) {
            defaults = angular.isArray(defaults) ? defaults : [defaults];
            return defaults.concat(transform);
        }

        var ObservationFieldsMetadata = $resource('api/v1/gamediary/observation/metadata', {}, {
            query: {method: 'GET', isArray: true},
            forSpecies: {
                url: 'api/v1/gamediary/observation/metadata/:gameSpeciesCode',
                method: 'GET',
                params: {account: '@account', gameSpeciesCode: '@gameSpeciesCode'},
                transformResponse: appendTransform($http.defaults.transformResponse, function (data, headersGetter, status) {
                    if (status === 200 && angular.isObject(data)) {
                        data.isCarnivoreAuthority = _.some($rootScope.account.occupations, function (occupation) {
                            return occupation.occupationType === 'PETOYHDYSHENKILO';
                        });
                        return data;
                    } else {
                        return data || {};
                    }
                })
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

        var halfStepRange = function (min, max) {
            if (!_.isFinite(min) || !_.isFinite(max) || min > max) {
                return null;
            }

            return rangeFilter([], min, max, 0.5);
        };

        ObservationFieldsMetadata.prototype.getWidthOfPawOptions = function () {
            return halfStepRange(this.minWidthOfPaw, this.maxWidthOfPaw);
        };

        ObservationFieldsMetadata.prototype.getLengthOfPawOptions = function () {
            return halfStepRange(this.minLengthOfPaw, this.maxLengthOfPaw);
        };

        ObservationFieldsMetadata.prototype.getFieldRequirements = function (withinMooseHunting, observationType) {
            if (!this.gameSpeciesCode || !_.isBoolean(this.isCarnivoreAuthority)) {
                return null;
            }

            var requirements = {
                fields: angular.copy(this.baseFields || {}),
                specimenFields: angular.copy(this.specimenFields || {}),
                isCarnivoreAuthority: !!this.isCarnivoreAuthority
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

                    requirements.widthOfPawOptions = this.getWidthOfPawOptions() || null;
                    requirements.lengthOfPawOptions = this.getLengthOfPawOptions() || null;
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

    .factory('ObservationFieldRequirements', function () {
        var ObservationFieldRequirements = function (opts) {
            angular.extend(this, opts);
        };

        var allMooseAmountFields =
            ['mooselikeMaleAmount', 'mooselikeFemaleAmount', 'mooselikeCalfAmount', 'mooselikeFemale1CalfAmount',
                'mooselikeFemale2CalfsAmount', 'mooselikeFemale3CalfsAmount', 'mooselikeUnknownSpecimenAmount'];
        var allMooselikeAmountFields = _.union(allMooseAmountFields, ['mooselikeFemale4CalfsAmount']);
        var allAmountFields = _.union(['amount'], allMooselikeAmountFields);

        var allPossibleBaseFields = _.union(allAmountFields,
            ['verifiedByCarnivoreAuthority', 'observerName', 'observerPhoneNumber', 'officialAdditionalInfo']);

        var allPossibleSpecimenFields = ['age', 'gender', 'widthOfPaw', 'lengthOfPaw', 'state', 'marking'];

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

        ObservationFieldRequirements.prototype.getWidthOfPawOptions = function () {
            return this.widthOfPawOptions || [];
        };

        ObservationFieldRequirements.prototype.getLengthOfPawOptions = function () {
            return this.lengthOfPawOptions || [];
        };

        ObservationFieldRequirements.prototype.isFieldRequired = function (fieldName) {
            return this.fields[fieldName] === 'YES' || this.specimenFields[fieldName] === 'YES';
        };

        ObservationFieldRequirements.prototype.isFieldLegal = function (fieldName) {
            var requirement = this.fields[fieldName] || this.specimenFields[fieldName];
            return requirement === 'YES'
                || requirement === 'VOLUNTARY'
                || requirement === 'VOLUNTARY_CARNIVORE_AUTHORITY' && !!this.isCarnivoreAuthority;
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

            _.forEach(allPossibleBaseFields, function (fieldName) {
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

            if (self.isFieldLegal('verifiedByCarnivoreAuthority') && !_.isBoolean(observation.verifiedByCarnivoreAuthority)) {
                observation.verifiedByCarnivoreAuthority = false;
            }

            if (!_.isEmpty(observation.specimens)) {
                _.forEach(observation.specimens, function (specimen) {

                    _.forEach(allPossibleSpecimenFields, function (fieldName) {
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

    .controller('ObservationFormController', function ($filter, $scope, DiaryEntryService,
                                                       DiaryImageService, DiaryEntrySpecimenFormService,
                                                       Helpers, ObservationFieldRequirements, ObservationFieldsMetadata,
                                                       entry, fieldMetadataForObservationSpecies, parameters) {

        $scope.entry = entry;
        $scope.species = parameters.species;
        $scope.getCategoryName = parameters.$getCategoryName;
        $scope.getUrl = DiaryImageService.getUrl;
        $scope.maxSpecimenCount = DiaryEntrySpecimenFormService.getMaxSpecimenCountForObservation();
        $scope.fieldMetadata = fieldMetadataForObservationSpecies;
        $scope.fieldRequirements = null;

        if (entry.huntingDayId) {
            $scope.entry.canEdit = false;
        }

        $scope.observationSpecimenTitleVisible = function () {
            return _.any(ObservationFieldRequirements.getAllAmountFields(), $scope.isFieldVisible);
        };

        $scope.getAvailableObservationTypes = function () {
            return $scope.fieldMetadata ? $scope.fieldMetadata.getAvailableObservationTypes($scope.entry.withinMooseHunting) : [];
        };

        $scope.isFieldRequired = function (fieldName) {
            return $scope.fieldRequirements && $scope.fieldRequirements.isFieldRequired(fieldName);
        };

        $scope.isFieldVisible = function (fieldName) {
            return $scope.fieldRequirements && $scope.fieldRequirements.isFieldLegal(fieldName);
        };

        $scope.getAvailableGameGenders = function () {
            return parameters.genders;
        };

        $scope.getAvailableGameAges = function () {
            return _.result($scope.fieldRequirements, 'getAvailableGameAges', []);
        };

        $scope.getAvailableGameStates = function () {
            return _.result($scope.fieldRequirements, 'getAvailableGameStates', []);
        };

        $scope.getAvailableGameMarkings = function () {
            return _.result($scope.fieldRequirements, 'getAvailableGameMarkings', []);
        };

        $scope.getWidthOfPawOptions = function () {
            return _.result($scope.fieldRequirements, 'getWidthOfPawOptions', []);
        };

        $scope.getLengthOfPawOptions = function () {
            return _.result($scope.fieldRequirements, 'getLengthOfPawOptions', []);
        };

        $scope.getGameName = function (speciesCode, species) {
            return parameters.$getGameName(speciesCode, species);
        };

        function isSumOfAmountFieldsValid() {
            return $scope.fieldRequirements && $scope.fieldRequirements.isSumOfAmountFieldsValid($scope.entry);
        }

        $scope.viewState = {
            anyLargeCarnivoreFieldsPresent: false
        };

        $scope.isValid = function () {
            return $scope.entry.gameSpeciesCode &&
                $scope.entry.observationType &&
                $scope.entry.geoLocation.latitude &&
                isSumOfAmountFieldsValid();
        };

        // Convert timestamp
        if (entry.pointOfTime) {
            var dateFilter = $filter('date');
            $scope.viewState.date = dateFilter(entry.pointOfTime, 'yyyy-MM-dd');
            $scope.viewState.time = dateFilter(entry.pointOfTime, 'HH:mm');
        }

        $scope.editSpecimen = function () {
            var availableFields = $scope.fieldRequirements ? $scope.fieldRequirements.getSpecimenFields() : {};

            _.forEach(availableFields, function (fieldName) {
                if ($scope.isFieldVisible(fieldName)) {
                    availableFields[fieldName] = $scope.isFieldRequired(fieldName);
                }
            });

            DiaryEntrySpecimenFormService.editSpecimen($scope.entry, parameters, availableFields, $scope.fieldRequirements);
        };

        var onSaveSuccess = function (entry) {
            $scope.$close(entry);
        };

        $scope.save = function () {
            $scope.entry.setDateAndTime($scope.viewState.date, $scope.viewState.time);

            delete $scope.entry.actorInfo;

            $scope.entry.saveOrUpdate().then(onSaveSuccess);
        };

        $scope.cancel = function () {
            $scope.$dismiss('cancel');
        };

        $scope.image = function (uuid) {
            DiaryImageService.openUploadDialog($scope.entry, uuid, true);
        };

        $scope.removeImage = function (uuid) {
            $scope.entry.imageIds = _.pull($scope.entry.imageIds, uuid);
        };

        $scope.showInlineSpecimenEdit = function () {
            return $scope.entry.gameSpeciesCode && $scope.entry.totalSpecimenAmount === 1;
        };

        $scope.showEditSpecimenButton = function () {
            return $scope.entry.totalSpecimenAmount > 1 &&
                $scope.fieldRequirements &&
                !_.isEmpty($scope.fieldRequirements.getSpecimenFields());
        };

        $scope.isSpecimenEditDisabled = function () {
            return $scope.entry.totalSpecimenAmount > DiaryEntrySpecimenFormService.MAX_VISIBLE_AMOUNT;
        };

        $scope.$watch('entry.gameSpeciesCode', function (newValue, oldValue) {
            if (newValue !== oldValue) {
                if (newValue) {
                    ObservationFieldsMetadata.forSpecies({gameSpeciesCode: newValue}).$promise.then(function (metadata) {
                        $scope.fieldMetadata = metadata;
                    });
                } else {
                    $scope.fieldMetadata = null;
                }
            }
        });

        $scope.$watch('entry.withinMooseHunting', function (newValue, oldValue) {
            if (newValue !== oldValue) {
                if ($scope.fieldMetadata) {
                    $scope.fieldMetadata.resetIllegalObservationFields($scope.entry);
                    $scope.fieldRequirements = $scope.fieldMetadata.getFieldRequirements(newValue, $scope.entry.observationType);
                }
            }
        });

        $scope.$watch('entry.observationType', function (newValue, oldValue) {
            if (newValue !== oldValue) {
                if ($scope.fieldMetadata) {
                    $scope.fieldRequirements = $scope.fieldMetadata.getFieldRequirements($scope.entry.withinMooseHunting, $scope.entry.observationType);
                }
            }
        });

        $scope.$watch('fieldMetadata', function (newValue, oldValue) {
            if (newValue) {
                newValue.resetIllegalObservationFields($scope.entry);
                $scope.fieldRequirements = newValue.getFieldRequirements($scope.entry.withinMooseHunting, $scope.entry.observationType);
            } else {
                $scope.fieldRequirements = null;
            }
        });

        $scope.$watchCollection(
            function () {
                return $scope.getAvailableObservationTypes();
            },
            function (newValue, oldValue) {
                if (newValue !== oldValue && newValue) {
                    if (newValue.length === 1) {
                        $scope.entry.observationType = newValue[0];
                    }
                }
            });

        $scope.$watch('fieldRequirements', function (newValue, oldValue) {
            var anyLargeCarnivoreFieldsPresent = false;

            if (newValue) {
                newValue.resetIllegalObservationFields($scope.entry);

                anyLargeCarnivoreFieldsPresent = newValue.isFieldLegal('verifiedByCarnivoreAuthority')
                    || newValue.isFieldLegal('observerName')
                    || newValue.isFieldLegal('observerPhoneNumber')
                    || newValue.isFieldLegal('officialAdditionalInfo');
            }
            $scope.viewState.anyLargeCarnivoreFieldsPresent = anyLargeCarnivoreFieldsPresent;
        });

        $scope.$watch('entry.totalSpecimenAmount', function (newValue, oldValue) {
            if (newValue) {
                $scope.entry.totalSpecimenAmount = Math.min(newValue, $scope.maxSpecimenCount);
                DiaryEntrySpecimenFormService.setSpecimenCount($scope.entry, newValue);
            }
        });
    });
