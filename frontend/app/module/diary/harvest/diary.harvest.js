'use strict';

angular.module('app.diary.harvest', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.diary.addHarvest', {
                url: '/add_harvest?permitNumber&gameSpeciesCode',
                wideLayout: true,
                views: {
                    '@profile': {
                        templateUrl: 'diary/harvest/edit-harvest.html',
                        controller: 'OpenDiaryEntryFormController'
                    }
                },
                params: {
                    id: 'me',
                    permitNumber: null,
                    gameSpeciesCode: null
                },
                resolve: {
                    entry: function ($stateParams, DiaryListViewState, Harvest) {
                        var gameSpeciesCode = $stateParams.gameSpeciesCode
                            ? _.parseInt($stateParams.gameSpeciesCode) : null;

                        return Harvest.createTransient({
                            gameSpeciesCode: gameSpeciesCode,
                            permitNumber: $stateParams.permitNumber,
                            permitNumberRequired: !!$stateParams.permitNumber
                        });
                    }
                }
            })

            .state('profile.diary.editHarvest', {
                url: '/edit_harvest?entryId&copy',
                wideLayout: true,
                views: {
                    '@profile': {
                        templateUrl: 'diary/harvest/edit-harvest.html',
                        controller: 'OpenDiaryEntryFormController'
                    }
                },
                params: {
                    id: 'me',
                    entryId: undefined,
                    copy: null
                },
                resolve: {
                    entry: function ($stateParams, MapState, Harvest) {
                        return Harvest.get({id: $stateParams.entryId}).$promise.then(function (harvest) {
                            var zoom = MapState.getZoom();

                            if (zoom) {
                                harvest.geoLocation.zoom = zoom;
                            }

                            if ($stateParams.copy === 'true') {
                                return harvest.createCopyForModeratorMassInsertion();
                            }

                            return harvest;
                        });
                    }
                }
            });
    })
    .factory('Harvest', function (DiaryEntryRepositoryFactory, DiaryEntryType, DiaryEntrySpecimenFormService) {
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
    .service('HarvestFieldsService', function ($q, $http) {
        this.getForPersistedHarvest = function (id) {
            return $http.get('/api/v1/gamediary/harvest/fields/' + id)
                .then(function (response) {
                    return response.data;
                });
        };

        this.getForHarvest = function (params) {
            if (!params.gameSpeciesCode || !params.geoLocation || !params.harvestDate ||
                !params.geoLocation.latitude ||
                !params.geoLocation.longitude) {
                return $q.when(null);
            }

            var gameSpeciesCode = params.gameSpeciesCode;

            return $http.get('/api/v1/gamediary/harvest/fields', {
                params: {
                    gameSpeciesCode: gameSpeciesCode,
                    withPermit: params.withPermit,
                    harvestDate: params.harvestDate,
                    longitude: params.geoLocation.longitude,
                    latitude: params.geoLocation.latitude
                }
            }).then(function (response) {
                return response.data;
            });
        };

        this.removeForbiddenFields = function (requiredFields, harvest) {
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

            if (harvest.isPermitBasedMooselike()) {
                var isAlonePossible = harvest.isAlonePossible();

                _.each(harvest.specimens, function (specimen) {
                    delete specimen.weight;

                    if (!_.isBoolean(specimen.notEdible)) {
                        specimen.notEdible = false;
                    }
                    if (isAlonePossible && !_.isBoolean(specimen.alone)) {
                        specimen.alone = false;
                    }
                });

                if (!harvest.isAntlersPossible()) {
                    _.each(harvest.specimens, function (specimen) {
                        delete specimen.antlersType;
                        delete specimen.antlersWidth;
                        delete specimen.antlerPointsLeft;
                        delete specimen.antlerPointsRight;
                    });
                }
            } else {
                var weightNotAllowed = harvest.isGreySeal() && harvest.huntingMethod === 'SHOT_BUT_LOST';

                _.each(harvest.specimens, function (specimen) {
                    delete specimen.alone;
                    delete specimen.weightEstimated;
                    delete specimen.weightMeasured;
                    delete specimen.fitnessClass;
                    delete specimen.antlersType;
                    delete specimen.antlersWidth;
                    delete specimen.antlerPointsLeft;
                    delete specimen.antlerPointsRight;
                    delete specimen.notEdible;
                    delete specimen.additionalInfo;

                    if (weightNotAllowed) {
                        delete specimen.weight;
                    }
                });
            }

            if (requiredFields) {
                var fieldRemover = createFieldRemover(requiredFields, harvest);

                _.each([
                    'huntingMethod', 'reportedWithPhoneCall', 'feedingPlace',
                    'huntingAreaType', 'huntingAreaSize', 'huntingParty', 'lukeStatus'
                ], function (fieldName) {
                    fieldRemover(harvest, fieldName);
                });

                _.each([
                    //'weight', 'age', 'gender', 'additionalInfo', 'weighEstimated', 'weightMeasured',
                    //'fitnessClass', 'antlersType', 'antersWidth', 'antlerPointsLeft', 'antlerPointsRight',
                    'taigaBeanGoose', 'feedingPlace', 'lukeStatus'
                ], function (fieldName) {
                    _.each(harvest.specimens, function (specimen) {
                        fieldRemover(specimen, fieldName);
                    });
                });

            }
        };

        function createFieldRemover(requiredFields) {
            return function (entity, fieldName) {
                if (requiredFields[fieldName] === 'NO' && fieldName in entity) {
                    delete entity[fieldName];
                }
            };
        }

    })
    .component('diaryHarvestAuthorShooterModerator', {
        templateUrl: 'diary/harvest/author-shooter-moderator.html',
        require: {
            form: '^form'
        },
        bindings: {
            harvest: '<'
        },
        controller: function ($scope, PersonSearchModal) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $scope.$watch(function () {
                    $ctrl.form.$setValidity('authorAndActor', !authorOrActorMissing());
                });
            };

            function authorOrActorMissing() {
                return !$ctrl.harvest.authorInfo || !$ctrl.harvest.authorInfo.id
                    || !$ctrl.harvest.actorInfo || !$ctrl.harvest.actorInfo.id;
            }

            // Moderator only
            $ctrl.findAuthor = function () {
                PersonSearchModal.searchPerson(true, true).then(function (personInfo) {
                    $ctrl.harvest.authorInfo = personInfo;

                    if (!$ctrl.harvest.actorInfo) {
                        $ctrl.harvest.actorInfo = personInfo;
                    }
                });
            };

            // Moderator only
            $ctrl.findHunter = function () {
                PersonSearchModal.searchPerson(true, true).then(function (personInfo) {
                    $ctrl.harvest.actorInfo = personInfo;
                });
            };
        }
    })
    .component('diaryHarvestAuthorShooter', {
        templateUrl: 'diary/harvest/author-shooter.html',
        require: {
            form: '^form'
        },
        bindings: {
            harvest: '<'
        },
        controller: function ($scope, AuthenticationService, ActiveRoleService, PersonSearchService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.hunterByNumberNotFound = false;
                $ctrl.searchHunterNumber = null;

                var authorId = _.get($ctrl.harvest, 'authorInfo.id');
                var actorId = _.get($ctrl.harvest, 'actorInfo.id');

                $ctrl.authorIsMe = !authorId || AuthenticationService.isCurrentPersonId(authorId);
                $ctrl.actorIsMe = !actorId || AuthenticationService.isCurrentPersonId(actorId);
                $ctrl.searchHunterNumber = _.get($ctrl.harvest, 'actorInfo.hunterNumber') || '';

                $scope.$watch(function () {
                    $ctrl.form.$setValidity('actor', actorValid());
                });
            };

            function actorValid() {
                if ($ctrl.hunterByNumberNotFound && !$ctrl.actorIsMe) {
                    return false;
                }

                return $ctrl.actorIsMe || $ctrl.harvest.actorInfo && $ctrl.harvest.actorInfo.id;
            }

            $ctrl.actorIsMeChanged = function () {
                $ctrl.harvest.actorInfo = null;
                $ctrl.searchHunterNumber = null;
                $ctrl.hunterByNumberNotFound = false;
            };

            $ctrl.searchHunterNumberChanged = function (hunterNumber) {
                if (!hunterNumber) {
                    $ctrl.hunterByNumberNotFound = false;
                    return;
                }

                PersonSearchService.findByHunterNumber(hunterNumber).then(function (response) {
                    $ctrl.harvest.actorInfo = response.data;
                    $ctrl.hunterByNumberNotFound = false;
                }, function () {
                    $ctrl.harvest.actorInfo = null;
                    $ctrl.hunterByNumberNotFound = true;
                });
            };
        }
    })
    .component('diaryHarvestSelectSpecies', {
        templateUrl: 'diary/harvest/select-species.html',
        require: {
            form: '^form'
        },
        bindings: {
            diaryParameters: '<',
            gameSpeciesCode: '=',
            harvestDate: '<',
            harvestPermit: '<',
            readOnly: '<'
        },
        controller: function ($scope, $filter, HarvestPermitSpeciesAmountService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                var allSpecies = $ctrl.diaryParameters.species;
                $ctrl.availableSpecies = getAvailableSpecies(allSpecies, $ctrl.harvestPermit, $ctrl.harvestDate);

                // clear currently selected species if it is not in the permits species
                if ($ctrl.harvestPermit && _.find($ctrl.availableSpecies, 'code', $ctrl.gameSpeciesCode) === -1) {
                    $ctrl.gameSpeciesCode = null;
                }

                if ($ctrl.availableSpecies.length === 1) {
                    $ctrl.gameSpeciesCode = $ctrl.availableSpecies[0].code;
                }

                $ctrl.form.$setValidity('validSpeciesForDate', !invalidSpeciesForDate());
            };

            function invalidSpeciesForDate() {
                var gameSpeciesCode = $ctrl.gameSpeciesCode;
                var validOnDate = $ctrl.harvestDate;
                var permit = $ctrl.harvestPermit;

                return permit && validOnDate && gameSpeciesCode && !HarvestPermitSpeciesAmountService
                    .findMatchingAmount(permit.speciesAmounts, gameSpeciesCode, validOnDate);
            }

            $ctrl.$onChanges = function (changes) {
                if (changes.harvestPermit || changes.harvestDate || changes.gameSpeciesCode) {
                    $ctrl.$onInit();
                }
            };

            function getAvailableSpecies(allSpecies, harvestPermit, harvestDate) {
                var getSpeciesName = $filter('rI18nNameFilter');
                var getCategoryName = $ctrl.diaryParameters.$getCategoryName;
                var permitSpeciesCodes = harvestPermit ? _.map(harvestPermit.speciesAmounts, 'gameSpecies.code') : null;

                return _.chain(allSpecies)
                    .filter(function (s) {
                        return permitSpeciesCodes === null || permitSpeciesCodes.indexOf(s.code) !== -1;
                    })
                    .map(function (s) {
                        s.categoryName = getCategoryName(s.categoryId);
                        s.speciesName = getSpeciesName(s.name);

                        if (harvestPermit && harvestPermit.speciesAmounts) {
                            var intervalText = HarvestPermitSpeciesAmountService
                                .findMatchingAmountIntervalAsText(harvestPermit.speciesAmounts, s.code, harvestDate);

                            if (intervalText) {
                                s.speciesName += ' ' + intervalText;
                            }
                        }
                        return s;
                    })
                    .sortByAll(['categoryId', 'speciesName'])
                    .value();
            }
        }
    })
    .component('diaryHarvestPermittedMethod', {
        templateUrl: 'diary/harvest/permitted-method.html',
        bindings: {
            harvestPermit: '<',
            harvest: '<'
        },
        controller: function () {
            var $ctrl = this;

            function emptyPermittedMethod() {
                return {
                    tapeRecorders: false,
                    traps: false,
                    other: false,
                    description: null
                };
            }

            $ctrl.$onInit = function () {
                $ctrl.harvest.permittedMethod = $ctrl.harvest.permittedMethod || emptyPermittedMethod();
                $ctrl.withPermittedMethod = isPermittedMethodSelected($ctrl.harvest.permittedMethod);
            };

            $ctrl.$onChanges = function (changes) {
                if (changes.harvestPermit) {
                    var harvestPermit = changes.harvestPermit.currentValue;

                    if (harvestPermit && !harvestPermit.permittedMethodAllowed) {
                        $ctrl.withPermittedMethod = false;
                        $ctrl.harvest.permittedMethod = emptyPermittedMethod();
                    }
                }
            };

            $ctrl.isPermittedMethodAllowed = function () {
                return $ctrl.harvestPermit && $ctrl.harvestPermit.permittedMethodAllowed;
            };

            $ctrl.isPermittedMethodRequired = function () {
                return $ctrl.withPermittedMethod && !isPermittedMethodSelected($ctrl.harvest.permittedMethod);
            };

            function isPermittedMethodSelected(methods) {
                return methods && (methods.tapeRecorders === true || methods.traps === true || methods.other === true);
            }
        }
    })
    .component('diaryHarvestPermit', {
        templateUrl: 'diary/harvest/permit.html',
        require: {
            form: '^form'
        },
        bindings: {
            harvest: '<',
            harvestPermit: '=',
            authorOrActor: '<',
            fields: '<'
        },
        controller: function ($scope, CheckPermitNumber) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                var permitNumberRequired = !!$ctrl.harvest.permitNumberRequired;

                $ctrl.searchPermitNumber = $ctrl.harvest.permitNumber;
                $ctrl.searchPermitNumberReadOnly = $ctrl.harvest.id && !$ctrl.authorOrActor;

                $ctrl.withPermit = permitNumberRequired || !!$ctrl.harvest.permitNumber;
                $ctrl.withPermitReadOnly = permitNumberRequired || $ctrl.harvest.id && !$ctrl.authorOrActor;

                $ctrl.permitByNumberNotFound = false;
                $ctrl.permitUnusable = false;

                if ($ctrl.harvest.permitNumber) {
                    searchPermit($ctrl.harvest.permitNumber);
                }

                $scope.$watch(function () {
                    $ctrl.form.$setValidity('permitNumberFound', permitRequiredAndNotMissing());
                });

                function permitRequiredAndNotMissing() {
                    return $ctrl.harvest.permitNumber || !$ctrl.isPermitRequired() && !$ctrl.withPermit;
                }
            };

            $ctrl.isPermitRequired = function () {
                return $ctrl.harvest.permitNumberRequired || $ctrl.fields && $ctrl.fields.permitNumber === 'YES';
            };

            $ctrl.isPermitMissing = function () {
                return $ctrl.isPermitRequired() && (!$ctrl.harvest.permitNumber || !$ctrl.harvestPermit);
            };

            $ctrl.onSearchPermitNumberChange = function (permitNumber) {
                if (permitNumber) {
                    searchPermit(permitNumber);
                } else {
                    $ctrl.harvestPermit = null;
                    $ctrl.permitByNumberNotFound = false;
                    $ctrl.permitUnusable = false;
                    $ctrl.form.$setValidity('permitAvailable', true);
                }
            };

            $ctrl.onWithPermitChange = function (withPermit) {
                if (!withPermit) {
                    $ctrl.harvestPermit = null;
                    $ctrl.harvest.permitNumber = null;
                    $ctrl.searchPermitNumber = null;
                    $ctrl.permitByNumberNotFound = false;
                }
            };

            function searchPermit(permitNumber) {
                CheckPermitNumber.check(permitNumber).then(function (response) {
                    $ctrl.harvestPermit = response.data;
                    $ctrl.harvest.permitNumber = permitNumber;
                    $ctrl.permitByNumberNotFound = false;
                    $ctrl.permitUnusable =
                        $ctrl.harvestPermit.unavailable === true &&
                        $ctrl.harvest.stateAcceptedToHarvestPermit !== 'ACCEPTED';
                    $ctrl.form.$setValidity('permitAvailable', !$ctrl.permitUnusable);

                }, function () {
                    $ctrl.harvestPermit = null;
                    $ctrl.harvest.permitNumber = null;
                    $ctrl.permitByNumberNotFound = true;
                    $ctrl.permitUnusable = false;
                    $ctrl.form.$setValidity('permitAvailable', true);
                });
            }
        }
    })
    .component('diaryHarvestSpecimen', {
        templateUrl: 'diary/harvest/inline-specimen.html',
        require: {
            form: '^form'
        },
        bindings: {
            harvest: '<',
            diaryParameters: '<',
            fields: '<'
        },
        controller: function ($scope, $filter, DiaryEntrySpecimenFormService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.maxSpecimensForSelectedSpecies = 1;

                if ($ctrl.harvest.gameSpeciesCode) {
                    updateSpecimen($ctrl.harvest.gameSpeciesCode);
                } else {
                    DiaryEntrySpecimenFormService.initAmountAndSpecimens($ctrl.harvest);
                }

                $scope.$watch('$ctrl.harvest.specimens', function () {
                    $ctrl.form.$setValidity('specimen', allSpecimensValid());
                }, true);

                $scope.$watch('$ctrl.harvest.gameSpeciesCode', function (gameSpeciesCode, oldValue) {
                    var gameSpeciesChanged = oldValue !== gameSpeciesCode;

                    if (gameSpeciesChanged && gameSpeciesCode) {
                        updateSpecimen(gameSpeciesCode);
                    }
                });
            };

            $ctrl.showInlineSpecimenEdit = function () {
                return $ctrl.harvest.gameSpeciesCode && $ctrl.harvest.totalSpecimenAmount === 1;
            };

            $ctrl.showSpecimenEditButton = function () {
                return $ctrl.harvest.gameSpeciesCode && $ctrl.harvest.totalSpecimenAmount !== 1;
            };

            $ctrl.isSpecimenEditDisabled = function () {
                return $ctrl.harvest.totalSpecimenAmount < 1
                    || $ctrl.harvest.totalSpecimenAmount > DiaryEntrySpecimenFormService.MAX_VISIBLE_AMOUNT;
            };

            function updateSpecimen(gameSpeciesCode) {
                $ctrl.maxSpecimensForSelectedSpecies =
                    DiaryEntrySpecimenFormService.getMaxSpecimenCountForHarvest($ctrl.diaryParameters, gameSpeciesCode);
                $ctrl.harvest.totalSpecimenAmount = Math.min($ctrl.harvest.totalSpecimenAmount, $ctrl.maxSpecimensForSelectedSpecies);

                DiaryEntrySpecimenFormService.setSpecimenCount($ctrl.harvest, $ctrl.harvest.totalSpecimenAmount);
            }

            var filterUnknownOut = $filter('stripUnknown');

            $ctrl.getAvailableGameGenders = function () {
                return $ctrl.harvest.isMooselike() || $ctrl.harvest.isWildBoar()
                    ? filterUnknownOut($ctrl.diaryParameters.genders)
                    : $ctrl.diaryParameters.genders;
            };

            $ctrl.getAvailableGameAges = function () {
                return $ctrl.harvest.isMooselike() || $ctrl.harvest.isWildBoar()
                    ? filterUnknownOut($ctrl.diaryParameters.ages)
                    : $ctrl.diaryParameters.ages;
            };

            $ctrl.getAvailableGameFitnessClasses = function () {
                return $ctrl.diaryParameters.fitnessClasses;
            };

            $ctrl.getAvailableGameAntlersTypes = function () {
                return $ctrl.diaryParameters.antlersTypes;
            };

            $ctrl.editSpecimen = function () {
                DiaryEntrySpecimenFormService.setSpecimenCount($ctrl.harvest, $ctrl.harvest.totalSpecimenAmount);

                DiaryEntrySpecimenFormService.editSpecimen($ctrl.harvest, $ctrl.diaryParameters, {
                    age: $ctrl.isFieldRequired('age'),
                    gender: $ctrl.isFieldRequired('gender'),
                    weight: $ctrl.isFieldRequired('weight')
                });
            };

            // Validation

            $ctrl.isWeightVisible = function () {
                return !$ctrl.isFieldVisible('weightEstimated') &&
                    !$ctrl.isFieldVisible('weightMeasured') &&
                    $ctrl.harvest.huntingMethod !== 'SHOT_BUT_LOST';
            };

            $ctrl.isWeightRequired = function () {
                return $ctrl.isFieldRequired('weight') && $ctrl.harvest.huntingMethod !== 'SHOT_BUT_LOST';
            };

            $ctrl.isFieldVisible = function (fieldName) {
                return _getFieldRequired(fieldName) !== 'NO';
            };

            $ctrl.isFieldRequired = function (fieldName) {
                return _getFieldRequired(fieldName) === 'YES';
            };

            function _getFieldRequired(fieldName) {
                return _.get($ctrl.fields, fieldName, 'NO');
            }

            $ctrl.hasRequiredSpecimenFields = function () {
                return $ctrl.isFieldRequired('age')
                    || $ctrl.isFieldRequired('gender')
                    || $ctrl.isFieldRequired('weight');
            };

            function allSpecimensValid() {
                var age = $ctrl.isFieldRequired('age');
                var gender = $ctrl.isFieldRequired('gender');
                var weight = $ctrl.isWeightRequired();

                return _.all($ctrl.harvest.specimens, function (specimen) {
                    return (!age || specimen.age) && (!gender || specimen.gender) && (!weight || specimen.weight);
                });
            }
        }
    })
    .component('diaryHarvestImages', {
        templateUrl: 'diary/harvest/images.html',
        bindings: {
            harvest: '<',
            authorOrActor: '<'
        },
        controller: function (DiaryImageService) {
            var $ctrl = this;

            $ctrl.getImageUrl = DiaryImageService.getUrl;

            $ctrl.uploadImage = function () {
                DiaryImageService.openUploadDialog($ctrl.harvest, null, true);
            };

            $ctrl.changeImage = function (uuid) {
                DiaryImageService.openUploadDialog($ctrl.harvest, uuid, true);
            };

            $ctrl.removeImage = function (uuid) {
                $ctrl.harvest.imageIds = _.pull($ctrl.harvest.imageIds, uuid);
            };
        }
    })
    .component('diaryHarvestSeasonFields', {
        templateUrl: 'diary/harvest/season-fields.html',
        require: {
            form: '^form'
        },
        bindings: {
            harvest: '<',
            diaryParameters: '<',
            fields: '<'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.getAvailableHuntingMethods = function () {
                return $ctrl.diaryParameters.huntingMethods;
            };

            $ctrl.getAvailableHuntingAreaType = function () {
                return $ctrl.diaryParameters.huntingAreaTypes;
            };

            $ctrl.hasExtraFields = function () {
                return $ctrl.isFieldVisible('huntingMethod') ||
                    $ctrl.isFieldVisible('reportedWithPhoneCall') ||
                    $ctrl.isFieldVisible('feedingPlace') ||
                    $ctrl.isFieldVisible('taigaBeanGoose') ||
                    $ctrl.isFieldVisible('huntingAreaType') ||
                    $ctrl.isFieldVisible('huntingAreaSize') ||
                    $ctrl.isFieldVisible('huntingParty');
            };

            $ctrl.isFieldVisible = function (fieldName) {
                return _getFieldRequired(fieldName) !== 'NO';
            };

            $ctrl.isFieldRequired = function (fieldName) {
                return _getFieldRequired(fieldName) === 'YES';
            };

            function _getFieldRequired(fieldName) {
                return _.get($ctrl.fields, fieldName, 'NO');
            }

            $ctrl.isHuntingPartyRequired = function () {
                return $ctrl.isFieldRequired('huntingParty')
                    && $ctrl.harvest.huntingAreaType === 'HUNTING_SOCIETY';
            };
        }
    })
    .controller('HarvestFormController',
        function ($filter, $scope, $translate, NotificationService,
                  AuthenticationService, ActiveRoleService, TranslatedBlockUI,
                  HarvestPermits, HarvestReportReasonAsker, HarvestFieldsService, Harvest,
                  DiaryEntryService, DiaryEntrySpecimenFormService, entry, parameters) {

            $scope.$onInit = function () {
                var dateFilter = $filter('date');
                var authorId = _.get(entry, 'authorInfo.id');
                var actorId = _.get(entry, 'actorInfo.id');
                var authorIsMe = !authorId || AuthenticationService.isCurrentPersonId(authorId);
                var actorIsMe = !actorId || AuthenticationService.isCurrentPersonId(actorId);

                $scope.entry = entry;

                $scope.viewState = {
                    parameters: parameters,
                    moderatorView: ActiveRoleService.isModerator(),
                    authorOrActor: !ActiveRoleService.isModerator() && (authorIsMe || actorIsMe),
                    speciesReadOnly: ActiveRoleService.isModerator() && entry.id,
                    date: entry.pointOfTime ? dateFilter(entry.pointOfTime, 'yyyy-MM-dd') : null,
                    time: entry.pointOfTime ? dateFilter(entry.pointOfTime, 'HH:mm') : null,
                    // For harvestReport
                    reportingType: 'BASIC',
                    originalReportingType: null,
                    fields: null,
                    season: null,
                    harvestArea: null,
                    rhy: null,
                    propertyIdentifier: null,
                    municipalityName: null
                };

                $scope.$watchGroup([
                    'entry.geoLocation.latitude',
                    'entry.geoLocation.longitude',
                    'entry.gameSpeciesCode',
                    'entry.permitNumber',
                    'viewState.date'
                ], function (newValues, oldValues) {
                    var locationChanged = oldValues[0] !== newValues[0] || oldValues[1] !== newValues[1];
                    var gameSpeciesChanged = oldValues[2] !== newValues[2];
                    var permitNumberChanged = oldValues[3] !== newValues[3];
                    var dateChanged = oldValues[4] !== newValues[4];

                    if (locationChanged || gameSpeciesChanged || permitNumberChanged || dateChanged) {
                        _updateFields();
                    }
                });

                _updateFields().then(function (reportingType) {
                    $scope.viewState.originalReportingType = reportingType;
                });
            };

            $scope.getAvailableLukeStatuses = function () {
                return parameters.lukeStatuses;
            };

            $scope.isLukeStatusVisible = function () {
                return $scope.viewState.fields && $scope.viewState.fields.lukeStatus !== 'NO';
            };

            $scope.isHarvestAreaVisible = function () {
                return $scope.viewState.fields && $scope.viewState.fields.harvestArea !== 'NO';
            };

            $scope.isHarvestAreaRequired = function () {
                return $scope.viewState.fields && $scope.viewState.fields.harvestArea === 'YES';
            };

            $scope.isValid = function () {
                if (!$scope.viewState.fields || !$scope.viewState.reportingType) {
                    return false;
                }

                var reportingType = $scope.viewState.reportingType;
                var originalReportingType = $scope.viewState.originalReportingType;

                // Only author or actor can change reporting type
                if (!$scope.viewState.authorOrActor && $scope.entry.id && originalReportingType !== reportingType) {
                    return false;
                }

                // Moderator can not create or change reporting type to basic diary entry
                if ($scope.viewState.moderatorView && reportingType === 'BASIC') {
                    return false;
                }

                // Permit requires RHY (location must be in Finland)
                if (!!$scope.entry.permitNumber && !$scope.viewState.rhy) {
                    return false;
                }

                return $scope.entry.gameSpeciesCode && $scope.entry.geoLocation.latitude &&
                    ($scope.viewState.harvestArea || !$scope.isHarvestAreaRequired());
            };

            $scope.cancel = function () {
                $scope.$dismiss('cancel');
            };

            $scope.save = function () {
                HarvestFieldsService.removeForbiddenFields($scope.viewState.fields, $scope.entry);
                DiaryEntrySpecimenFormService.setSpecimenCount($scope.entry, $scope.entry.totalSpecimenAmount);

                $scope.entry.setDateAndTime($scope.viewState.date, $scope.viewState.time);

                HarvestReportReasonAsker.promptForReason().then(function (reason) {
                    $scope.entry.moderatorReasonForChange = reason;

                    TranslatedBlockUI.start("global.block.wait");

                    $scope.entry.saveOrUpdate().then(onSaveSuccess).finally(function () {
                        TranslatedBlockUI.stop();
                    });
                });
            };

            function onSaveSuccess(diaryEntry) {
                $scope.$close(diaryEntry);

                if ($scope.viewState.moderatorView) {
                    return;
                }

                if ($scope.entry.permitNumber &&
                    $scope.viewState.harvestPermit &&
                    $scope.viewState.harvestPermit.harvestsAsList) {

                    HarvestPermits.query().$promise.then(function (myPermits) {
                        // Check if current user is contactPerson for used permit
                        var isContactPerson = _.any(myPermits, function (p) {
                            return p.permitNumber === $scope.viewState.harvestPermit.permitNumber;
                        });

                        var body = $translate.instant("harvestreport.wizard.harvestCreated");

                        if (isContactPerson) {
                            body = $translate.instant("harvestreport.wizard.harvestCreatedContactPerson", {
                                permitNumber: $scope.entry.permitNumber
                            });
                        }

                        NotificationService.showMessage(body, "success", {ttl: -1});
                    });
                }
            }

            function clearComputedFields() {
                $scope.viewState.reportingType = null;
                $scope.viewState.fields = null;
                $scope.viewState.municipalityName = null;
                $scope.viewState.propertyIdentifier = null;
                $scope.viewState.rhy = null;
                $scope.viewState.season = null;
                $scope.viewState.harvestArea = null;
            }

            function _updateFields() {
                TranslatedBlockUI.start("global.block.wait");

                return HarvestFieldsService.getForHarvest({
                    harvestDate: $scope.viewState.date,
                    withPermit: !!$scope.entry.permitNumber,
                    gameSpeciesCode: $scope.entry.gameSpeciesCode,
                    geoLocation: $scope.entry.geoLocation

                }).then(function (computedFields) {
                    if (computedFields && computedFields.reportingType) {
                        $scope.viewState.reportingType = computedFields.reportingType;
                        $scope.viewState.fields = computedFields.fields;
                        $scope.viewState.municipalityName = computedFields.municipalityName;
                        $scope.viewState.propertyIdentifier = computedFields.propertyIdentifier;
                        $scope.viewState.rhy = computedFields.rhy;
                        $scope.viewState.season = computedFields.season;
                        $scope.viewState.harvestArea = computedFields.harvestArea;

                        return computedFields.reportingType;
                    }

                    clearComputedFields();
                    return null;

                }, function () {
                    clearComputedFields();

                    NotificationService.showDefaultFailure();

                }).finally(TranslatedBlockUI.stop);
            }

            $scope.$onInit();
        });
