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
                observationCategory: 'MOOSE_HUNTING',
                observationType: 'NAKO',
                gameSpeciesCode: harvest.gameSpeciesCode,
                geoLocation: harvest.geoLocation,
                pointOfTime: harvest.pointOfTime,
                authorInfo: harvest.authorInfo,
                actorInfo: harvest.actorInfo,
                huntingDayId: harvest.huntingDayId,
                totalSpecimenAmount: harvest.totalSpecimenAmount,
                mooselikeMaleAmount: isAdult && isMale ? 1 : 0,
                mooselikeFemaleAmount: isAdult && isFemale ? 1 : 0,
                specimens: [],
                imageIds: []
            });
        };

        return Observation;
    })

    .service('ObservationCategory', function () {
        var self = this;

        this.MOOSE_HUNTING = 'MOOSE_HUNTING';
        this.DEER_HUNTING = 'DEER_HUNTING';
        this.NORMAL = 'NORMAL';

        this.from = function (withinMooseHunting, withinDeerHunting) {
            if (withinMooseHunting) {
                return self.MOOSE_HUNTING;
            }
            if (withinDeerHunting) {
                return self.DEER_HUNTING;
            }
            return self.NORMAL;
        };

        this.isWithinMooseHunting = function (observationCategory) {
            return observationCategory === self.MOOSE_HUNTING;
        };

        this.isWithinDeerHunting = function (observationCategory) {
            return observationCategory === self.DEER_HUNTING;
        };

        this.isWithinHunting = function (observationCategory) {
            return observationCategory === self.MOOSE_HUNTING || observationCategory === self.DEER_HUNTING;
        };

    })

    .service('DeerHuntingType', function() {
        var self = this;

        this.STAND_HUNTING = 'STAND_HUNTING';
        this.DOG_HUNTING = 'DOG_HUNTING';
        this.OTHER = 'OTHER';

        this.getAll = function () {
            return [self.STAND_HUNTING, self.DOG_HUNTING, self.OTHER];
        };

    })

    .service('ObservationFieldsMetadata', function ($filter, $resource, $http, AuthenticationService,
                                                    ObservationFieldRequirements, ObservationCategory) {
        var rangeFilter = $filter('range');

        function appendTransform(defaults, transform) {
            defaults = angular.isArray(defaults) ? defaults : [defaults];
            return defaults.concat(transform);
        }

        var ObservationFieldsMetadata = $resource('api/v1/gamediary/observation/metadata', {}, {
            forSpecies: {
                url: 'api/v1/gamediary/observation/metadata/:gameSpeciesCode',
                method: 'GET',
                params: {account: '@account', gameSpeciesCode: '@gameSpeciesCode'},
                transformResponse: appendTransform($http.defaults.transformResponse, function (data, headersGetter, status) {
                    if (status === 200 && _.isObject(data)) {
                        data.isCarnivoreAuthority = AuthenticationService.isCarnivoreAuthority();
                        return data;
                    } else {
                        return data || {};
                    }
                })
            }
        });

        var _findContextSensitiveFieldSets = function (self, observationCategory, observationType) {
            if (!self.contextSensitiveFieldSets) {
                return [];
            }

            return _.filter(self.contextSensitiveFieldSets, function (fieldSet) {
                return fieldSet.category === observationCategory && (!observationType || fieldSet.type === observationType);
            });
        };

        var _hasMooseHuntingContexts = function (self) {
            return _findContextSensitiveFieldSets(self, ObservationCategory.MOOSE_HUNTING).length > 0;
        };

        var _hasDeerHuntingContexts = function (self) {
            return _findContextSensitiveFieldSets(self, ObservationCategory.DEER_HUNTING).length > 0;
        };

        ObservationFieldsMetadata.prototype.getAvailableObservationTypes = function (category) {
            return _.map(_findContextSensitiveFieldSets(this, category), 'type');
        };

        var halfStepRange = function (min, max) {
            if (!_.isFinite(min) || !_.isFinite(max) || min > max) {
                return null;
            }

            return rangeFilter([], min, max, 0.5);
        };

        ObservationFieldsMetadata.prototype.hasDeerHuntingFields = function () {
            return _hasDeerHuntingContexts(this);
        };

        ObservationFieldsMetadata.prototype.hasMooseHuntingFields = function () {
            return _hasMooseHuntingContexts(this);
        };

        ObservationFieldsMetadata.prototype.getWidthOfPawOptions = function () {
            return halfStepRange(this.minWidthOfPaw, this.maxWidthOfPaw);
        };

        ObservationFieldsMetadata.prototype.getLengthOfPawOptions = function () {
            return halfStepRange(this.minLengthOfPaw, this.maxLengthOfPaw);
        };

        ObservationFieldsMetadata.prototype.getFieldRequirements = function (observationCategory, observationType) {
            if (!this.gameSpeciesCode || !_.isBoolean(this.isCarnivoreAuthority)) {
                return null;
            }

            var requirements = {
                fields: angular.copy(this.baseFields || {}),
                specimenFields: angular.copy(this.specimenFields || {}),
                isCarnivoreAuthority: !!this.isCarnivoreAuthority
            };

            if (observationType) {
                var ctxSensitiveFieldSets = _findContextSensitiveFieldSets(this, observationCategory, observationType);
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

        function _isObservationTypeLegalForCategory(self, observationType, observationCategory) {
            return _.find(self.getAvailableObservationTypes(observationCategory), function (obsTypeCandidate) {
                return obsTypeCandidate === observationType;
            });
        }

        // TODO: Check what of these are really needed.
        ObservationFieldsMetadata.prototype.resetIllegalObservationFields = function (observation) {
            if (!observation || !observation.isObservation()) {
                return;
            }

            if (this.gameSpeciesCode) {
                if (!_hasMooseHuntingContexts(this) && observation.observationCategory === ObservationCategory.MOOSE_HUNTING) {
                    observation.observationCategory = ObservationCategory.NORMAL;
                }

                if (!_hasDeerHuntingContexts(this)) {
                    delete observation.deerHuntingType;
                    delete observation.deerHuntingTypeDescription;

                    if (observation.observationCategory === ObservationCategory.DEER_HUNTING) {
                        observation.observationCategory = ObservationCategory.NORMAL;
                    }
                }

                if (!observation.observationCategory) {
                    observation.observationCategory = ObservationCategory.NORMAL;
                }

                var type = observation.observationType;

                if (type && !_isObservationTypeLegalForCategory(this, type, observation.observationCategory)) {
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
        var allMooselikeAmountFields =
            ['mooselikeMaleAmount', 'mooselikeFemaleAmount', 'mooselikeCalfAmount', 'mooselikeFemale1CalfAmount',
            'mooselikeFemale2CalfsAmount', 'mooselikeFemale3CalfsAmount', 'mooselikeFemale4CalfsAmount',
                'mooselikeUnknownSpecimenAmount'];
        var allAmountFields = _.union(['amount'], allMooselikeAmountFields);

        var allPossibleBaseFields = _.union(allAmountFields,
            ['verifiedByCarnivoreAuthority', 'observerName', 'observerPhoneNumber', 'officialAdditionalInfo',
                'deerHuntingType', 'deerHuntingTypeDescription']);

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

        ObservationFieldRequirements.getAllMooselikeAmountFields = function () {
            return angular.copy(allMooselikeAmountFields);
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
            var requirement = this.fields[fieldName] || this.specimenFields[fieldName];
            return requirement === 'YES';
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

        ObservationFieldRequirements.prototype.areRequiredFieldsSet = function (observation) {
            if (!observation || !observation.isObservation()) {
                return false;
            }

            var self = this;

            return _.every(allPossibleBaseFields, function (fieldName) {
                var observationFieldName = translateMetadataFieldToObservationField(fieldName);
                return !self.isFieldRequired(fieldName) || !_.isNil(observation[observationFieldName]);
            });

        };

        ObservationFieldRequirements.prototype.isSumOfAmountFieldsValid = function (observation) {
            var self = this;

            var amountFields = this.getAmountFields();

            var hasRequiredAmountFields = _.some(amountFields, function(field) {
                return self.isFieldRequired(field);
            });

            if ( !hasRequiredAmountFields ) {
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
                } else if (_.includes(allAmountFields, fieldName)
                           && !_.isFinite(observation[observationFieldName])
                           && self.isFieldRequired(fieldName)) {

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

    .controller('ObservationFormController', function ($filter, $scope, DiaryImageService,
                                                       DiaryEntrySpecimenFormService, ObservationFieldRequirements,
                                                       ObservationFieldsMetadata, entry,
                                                       fieldMetadataForObservationSpecies, parameters,
                                                       ObservationCategory, DeerHuntingType) {

        $scope.entry = entry;

        // Hunting day cannot be altered from game diary view. In addition, `huntingDayId` property is not used in game
        // diary view. Hence, it can be safely deleted in order to avoid unnecessary validation errors and side-effects
        // in processing of create/update requests on the server-side.
        delete $scope.entry.huntingDayId;

        $scope.species = parameters.species;
        $scope.getCategoryName = parameters.$getCategoryName;
        $scope.getUrl = DiaryImageService.getUrl;
        $scope.maxSpecimenCount = DiaryEntrySpecimenFormService.getMaxSpecimenCountForObservation();
        $scope.minSpecimenCount = DiaryEntrySpecimenFormService.getMinSpecimenCountForObservation(entry.observationType);
        $scope.fieldMetadata = fieldMetadataForObservationSpecies;
        $scope.fieldRequirements = null;
        $scope.viewState = {
            anyLargeCarnivoreFieldsPresent: false,
            withinMooseHunting: entry.observationCategory === ObservationCategory.MOOSE_HUNTING,
            withinDeerHunting: entry.observationCategory === ObservationCategory.DEER_HUNTING
        };

        $scope.observationSpecimenTitleVisible = function () {
            return _.some(ObservationFieldRequirements.getAllAmountFields(), $scope.isFieldVisible);
        };

        $scope.getAvailableObservationTypes = function () {
            return $scope.fieldMetadata
                ? $scope.fieldMetadata.getAvailableObservationTypes($scope.entry.observationCategory)
                : [];
        };

        $scope.getAvailableDeerHuntingTypes = function () {
          return DeerHuntingType.getAll();
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

        function areRequiredFieldsSet() {
            return $scope.fieldRequirements && $scope.fieldRequirements.areRequiredFieldsSet($scope.entry);
        }

        $scope.isValid = function () {
            return $scope.entry.gameSpeciesCode &&
                $scope.entry.observationType &&
                $scope.entry.geoLocation.latitude &&
                isSumOfAmountFieldsValid() &&
                areRequiredFieldsSet();
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

        // Changed value of 'Laji' dropdown
        $scope.gameSpeciesChange = function () {
            if ($scope.entry.gameSpeciesCode) {
                ObservationFieldsMetadata.forSpecies({gameSpeciesCode: $scope.entry.gameSpeciesCode}).$promise.then(function (metadata) {
                    $scope.fieldMetadata = metadata;
                    $scope.updateCategory();
                });
            } else {
                $scope.fieldMetadata = null;
                $scope.fieldRequirements = null;
                // TODO: Should here reset entry or viewState values too?
                //       They are reset when another species is been selected, though.
            }
        };

        // Clicked 'Hirvenmetsästyksen yhteydessä' or 'Peuran metsästyksen yhteydessä' checkbox
        $scope.updateCategory = function () {
            if ($scope.fieldMetadata) {
                if (!$scope.fieldMetadata.hasMooseHuntingFields()) {
                    $scope.viewState.withinMooseHunting = false;
                }

                if (!$scope.fieldMetadata.hasDeerHuntingFields()) {
                    $scope.viewState.withinDeerHunting = false;
                }

                $scope.entry.observationCategory = ObservationCategory.from($scope.viewState.withinMooseHunting,
                                                                            $scope.viewState.withinDeerHunting);

                var availableObservationTypes = $scope.getAvailableObservationTypes();
                if (availableObservationTypes.length === 1) {
                    $scope.entry.observationType = availableObservationTypes[0];
                }

                $scope.updateRequirements();
            } else {
                $scope.entry.observationCategory = ObservationCategory.NORMAL;
            }
        };

        $scope.updateRequirements = function () {
            $scope.fieldRequirements = $scope.fieldMetadata.getFieldRequirements($scope.entry.observationCategory,
                                                                                 $scope.entry.observationType);

            $scope.fieldRequirements.resetIllegalObservationFields($scope.entry);

            var anyLargeCarnivoreFieldsPresent = $scope.fieldRequirements.isFieldLegal('verifiedByCarnivoreAuthority')
                || $scope.fieldRequirements.isFieldLegal('observerName')
                || $scope.fieldRequirements.isFieldLegal('observerPhoneNumber')
                || $scope.fieldRequirements.isFieldLegal('officialAdditionalInfo');
            $scope.viewState.anyLargeCarnivoreFieldsPresent = anyLargeCarnivoreFieldsPresent;
        };

        // If observation has been edited, the field requirements needs to be set..
        // Note! This must be after definition of updateRequirements function.
        if ($scope.fieldMetadata) {
            // This is needed for old deer observations that were originally observed 'within moose
            // hunting' but cannot anymore be saved as 'within moose hunting' according to updated
            // metadata. Without this, the observation type drop-down may initially be left empty.
            $scope.fieldMetadata.resetIllegalObservationFields($scope.entry);

            $scope.updateRequirements();
        }

        // Changed value of 'Havaintotyyppi' dropdown
        $scope.observationTypeChange = function () {
            if ($scope.fieldMetadata) {
                $scope.updateRequirements();
            }

            if ($scope.entry.observationType === 'PARI') {
                $scope.entry.totalSpecimenAmount = 2;
                DiaryEntrySpecimenFormService.setSpecimenCount($scope.entry, $scope.entry.totalSpecimenAmount);

                $scope.entry.specimens[0].gender = 'MALE';
                $scope.entry.specimens[0].age = 'ADULT';
                $scope.entry.specimens[1].gender = 'FEMALE';
                $scope.entry.specimens[1].age = 'ADULT';
            }

            $scope.minSpecimenCount = DiaryEntrySpecimenFormService.getMinSpecimenCountForObservation(entry.observationType);
            if ($scope.entry.totalSpecimenAmount < $scope.minSpecimenCount) {
                $scope.entry.totalSpecimenAmount = $scope.minSpecimenCount;
                DiaryEntrySpecimenFormService.setSpecimenCount($scope.entry, $scope.minSpecimenCount);
            }
        };

        $scope.isTotalSpecimenAmountDisabled = function () {
            return !$scope.entry.canEdit || !$scope.entry.gameSpeciesCode || $scope.entry.observationType === 'PARI';
        };

        // Changed value of 'Määrä' input field
        $scope.totalSpecimenAmountChange = function () {
            $scope.entry.totalSpecimenAmount = Math.min($scope.entry.totalSpecimenAmount, $scope.maxSpecimenCount);
            DiaryEntrySpecimenFormService.setSpecimenCount($scope.entry, $scope.entry.totalSpecimenAmount);
        };

    });
