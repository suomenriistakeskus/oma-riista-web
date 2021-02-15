'use strict';

angular.module('app.diary.harvest.view', [])
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
                    entry: function ($stateParams, Harvest) {
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
                    entry: function ($stateParams, Harvest, MapState) {
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
        controller: function ($scope, AuthenticationService, PersonSearchService) {
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
        controller: function ($filter, HarvestPermitSpeciesAmountService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                var allSpecies = $ctrl.diaryParameters.species;
                $ctrl.availableSpecies = getAvailableSpecies(allSpecies, $ctrl.harvestPermit, $ctrl.harvestDate);

                // clear currently selected species if it is not in the permits species
                if ($ctrl.harvestPermit && _.find($ctrl.availableSpecies, {code: $ctrl.gameSpeciesCode}) === -1) {
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
                    .sortBy(['categoryId', 'speciesName'])
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

            function isFieldRequired (fieldName) {
                return !!$ctrl.fields && $ctrl.fields.isRequiredReportField(fieldName);
            }

            $ctrl.isPermitRequired = function () {
                return $ctrl.harvest.permitNumberRequired || isFieldRequired('permitNumber');
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
        controller: function ($scope, $filter, AntlerGuide, DiaryEntrySpecimenFormService) {
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

            $ctrl.isFieldVisible = function (fieldName) {
                var specimen = $ctrl.harvest.specimens[0];
                return !!$ctrl.fields && $ctrl.fields.isVisibleSpecimenField(fieldName, specimen);
            };

            $ctrl.isFieldRequired = function (fieldName) {
                var specimen = $ctrl.harvest.specimens[0];
                return !!$ctrl.fields && $ctrl.fields.isRequiredSpecimenField(fieldName, specimen);
            };

            $ctrl.isWeightVisible = function () {
                return !$ctrl.isFieldVisible('weightEstimated') &&
                    !$ctrl.isFieldVisible('weightMeasured') &&
                    $ctrl.harvest.huntingMethod !== 'SHOT_BUT_LOST';
            };

            $ctrl.isWeightRequired = function () {
                return $ctrl.isFieldRequired('weight') && $ctrl.harvest.huntingMethod !== 'SHOT_BUT_LOST';
            };

            $ctrl.hasRequiredSpecimenFields = function () {
                return $ctrl.isFieldRequired('age')
                    || $ctrl.isFieldRequired('gender')
                    || $ctrl.isFieldRequired('weight');
            };

            $ctrl.isAntlerGuideVisible = function () {
                return AntlerGuide.isVisible($ctrl.harvest, $ctrl.fields);
            };

            function allSpecimensValid() {
                var age = $ctrl.isFieldRequired('age');
                var gender = $ctrl.isFieldRequired('gender');
                var weight = $ctrl.isWeightRequired();

                return _.every($ctrl.harvest.specimens, function (specimen) {
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

            $ctrl.isFieldVisible = function (fieldName) {
                return !!$ctrl.fields && $ctrl.fields.isVisibleReportField(fieldName);
            };

            $ctrl.isFieldRequired = function (fieldName) {
                return !!$ctrl.fields && $ctrl.fields.isRequiredReportField(fieldName);
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

            $ctrl.isHuntingPartyRequired = function () {
                return $ctrl.isFieldRequired('huntingParty') && $ctrl.harvest.huntingAreaType === 'HUNTING_SOCIETY';
            };
        }
    })
    .component('diaryHarvestDeerHuntingTypeSelection', {
        templateUrl: 'diary/harvest/deer-hunting-type.html',
        require: {
            form: '^form'
        },
        bindings: {
            harvest: '<'
        },
        controller: function (DeerHuntingType) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.deerHuntingTypes = DeerHuntingType.getAll();
            };
        }
    })
    .controller('HarvestFormController',
        function ($filter, $scope, $translate, ActiveRoleService, AuthenticationService, DiaryEntrySpecimenFormService,
                  GameSpeciesCodes, HarvestFieldsService, HarvestPermits, HarvestReportReasonAsker,
                  NotificationService, TranslatedBlockUI, entry, parameters) {

            $scope.$onInit = function () {
                var dateFilter = $filter('date');
                var authorId = _.get(entry, 'authorInfo.id');
                var actorId = _.get(entry, 'actorInfo.id');
                var authorIsMe = !authorId || AuthenticationService.isCurrentPersonId(authorId);
                var actorIsMe = !actorId || AuthenticationService.isCurrentPersonId(actorId);
                var isModerator = ActiveRoleService.isModerator();

                $scope.entry = entry;

                $scope.viewState = {
                    parameters: parameters,
                    moderatorView: isModerator,
                    authorOrActor: !isModerator && (authorIsMe || actorIsMe),
                    speciesReadOnly: isModerator && entry.id,
                    date: entry.pointOfTime ? dateFilter(entry.pointOfTime, 'yyyy-MM-dd') : null,
                    time: entry.pointOfTime ? dateFilter(entry.pointOfTime, 'HH:mm') : null
                };

                clearHarvestReportInfoFields($scope.viewState);
                $scope.viewState.reportingType = 'BASIC';
                $scope.viewState.originalReportingType = null;

                $scope.$watchGroup([
                    'entry.geoLocation.latitude',
                    'entry.geoLocation.longitude',
                    'entry.gameSpeciesCode',
                    'entry.permitNumber',
                    'viewState.date',
                    'entry.authorInfo.id'
                ], function (newValues, oldValues) {
                    var locationChanged = oldValues[0] !== newValues[0] || oldValues[1] !== newValues[1];
                    var gameSpeciesChanged = oldValues[2] !== newValues[2];
                    var permitNumberChanged = oldValues[3] !== newValues[3];
                    var dateChanged = oldValues[4] !== newValues[4];
                    var authorChanged = oldValues[5] !== newValues[5];

                    if (locationChanged || gameSpeciesChanged || permitNumberChanged || dateChanged || authorChanged) {
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

            $scope.isReportFieldVisible = function (fieldName) {
                var fields = $scope.viewState.fields;
                return !!fields && fields.isVisibleReportField(fieldName);
            };

            $scope.isReportFieldRequired = function (fieldName) {
                var fields = $scope.viewState.fields;
                return !!fields && fields.isRequiredReportField(fieldName);
            };

            $scope.isHarvestAreaRequired = function () {
                return $scope.isReportFieldRequired('harvestArea');
            };

            $scope.isDeerHuntingTypeVisible = function () {
                var fields = $scope.viewState.fields;

                // TODO Can fields be null? This conditional structure can probably be simplified.
                if (!!fields) {
                    return fields.isVisibleReportField('deerHuntingType');
                } else {
                    return GameSpeciesCodes.isWhiteTailedDeer(entry.gameSpeciesCode) &&
                        AuthenticationService.isDeerPilotUser();
                }
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
                HarvestFieldsService.fixStateBeforeSaving($scope.viewState.fields, $scope.entry);
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
                        var isContactPerson = _.some(myPermits, function (p) {
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

            function clearHarvestReportInfoFields(obj) {
                obj.reportingType = null;
                obj.fields = null;
                obj.municipalityName = null;
                obj.propertyIdentifier = null;
                obj.rhy = null;
                obj.season = null;
                obj.harvestArea = null;
            }

            function _updateFields() {
                var harvest = $scope.entry;

                var params = {
                    harvestDate: $scope.viewState.date,
                    withPermit: !!harvest.permitNumber,
                    gameSpeciesCode: harvest.gameSpeciesCode,
                    geoLocation: harvest.geoLocation
                };

                if ($scope.viewState.moderatorView) {
                    params.personId = _.get(harvest.authorInfo, 'id', null);
                }

                TranslatedBlockUI.start("global.block.wait");

                return HarvestFieldsService.getForHarvest(params)
                    .then(function (response) {
                        if (response && response.reportingType) {
                            $scope.viewState.reportingType = response.reportingType;
                            $scope.viewState.fields = response.fields;
                            $scope.viewState.municipalityName = response.municipalityName;
                            $scope.viewState.propertyIdentifier = response.propertyIdentifier;
                            $scope.viewState.rhy = response.rhy;
                            $scope.viewState.season = response.season;
                            $scope.viewState.harvestArea = response.harvestArea;

                            return response.reportingType;
                        }

                        clearHarvestReportInfoFields($scope.viewState);
                        return null;

                    }, function () {
                        clearHarvestReportInfoFields($scope.viewState);
                        NotificationService.showDefaultFailure();
                    })
                    .finally(TranslatedBlockUI.stop);
            }

            $scope.$onInit();
        });
