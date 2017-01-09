'use strict';

angular.module('app.diary.controllers', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.diary.addHarvest', {
                url: '/add_harvest?permitNumber&gameSpeciesCode&permitNumberRequired',
                wideLayout: true,
                views: {
                    '@profile': {
                        templateUrl: 'diary/edit-harvest.html',
                        controller: 'OpenDiaryEntryFormController'
                    }
                },
                params: {
                    id: 'me',
                    permitNumber: null,
                    gameSpeciesCode: null,
                    permitNumberRequired: null
                },
                resolve: {
                    entry: function ($stateParams, DiaryListViewState, Harvest) {
                        return Harvest.createTransient({
                            gameSpeciesCode: $stateParams.gameSpeciesCode,
                            geoLocation: null,
                            permitNumber: $stateParams.permitNumber,
                            permitNumberRequired: $stateParams.permitNumberRequired
                        });
                    }
                }
            })

            .state('profile.diary.addObservation', {
                url: '/add_observation?gameSpeciesCode',
                wideLayout: true,
                views: {
                    '@profile': {
                        templateUrl: 'diary/edit-observation.html',
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
                            gameSpeciesCode: $stateParams.gameSpeciesCode,
                            geoLocation: null
                        });
                    }
                }
            })

            .state('profile.diary.editHarvest', {
                url: '/edit_harvest?entryId&copy&permitNumberRequired',
                wideLayout: true,
                views: {
                    '@profile': {
                        templateUrl: 'diary/edit-harvest.html',
                        controller: 'OpenDiaryEntryFormController'
                    }
                },
                params: {
                    id: 'me',
                    entryId: undefined,
                    copy: null,
                    permitNumber: null,
                    permitNumberRequired: null
                },
                resolve: {
                    entry: function ($stateParams, MapState, Harvest) {
                        var doCopy = $stateParams.copy === 'true';

                        return Harvest.get({id: $stateParams.entryId}).$promise.then(function (harvest) {
                            var zoom = MapState.getZoom();

                            if (zoom) {
                                harvest.geoLocation.zoom = zoom;
                            }
                            if (doCopy) {
                                return harvest.createCopyForModeratorMassInsertion();
                            }
                            if ($stateParams.permitNumber) {
                                harvest.permitNumber = $stateParams.permitNumber || harvest.permitNumber;
                            }
                            if ($stateParams.permitNumberRequired) {
                                harvest.permitNumberRequired = $stateParams.permitNumberRequired;
                            }
                            return harvest;
                        });
                    }
                }
            })

            .state('profile.diary.editObservation', {
                url: '/edit_observation?entryId',
                wideLayout: true,
                views: {
                    '@profile': {
                        templateUrl: 'diary/edit-observation.html',
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

    .controller('OpenDiaryEntryFormController', function ($history, $scope, $state, ActiveRoleService,
                                                          DiaryEntryService, DiaryListViewState,
                                                          MapState, MapDefaults,
                                                          NotificationService, WGS84, entry) {

        $scope.entry = entry;
        $scope.mapEvents = MapDefaults.getMapBroadcastEvents(['click']);
        $scope.mapDefaults = MapDefaults.create();
        $scope.geoCenter = MapState.toGeoLocation();

        DiaryEntryService.openDiaryEntryForm(entry)
            .then(function (diaryEntry) {
                NotificationService.showDefaultSuccess();
                DiaryListViewState.selectedDiaryEntry = diaryEntry;

                // Return to previous active state
                $history.back().catch(function (err) {
                    $state.go('profile.diary');
                });
            })
            .catch(function (err) {
                if (angular.isString(err)) {
                    // Dialog was dismissed by browser navigation
                    if (err.indexOf('back') !== -1) {
                        return;
                    }

                    if (err.indexOf('escape') === -1 && err.indexOf('cancel') === -1) {
                        // Error not caused by dismissing the dialog
                        NotificationService.showDefaultFailure();
                    }
                } else {
                    NotificationService.showDefaultFailure();
                }

                $history.back().catch(function (error) {
                    $state.go(ActiveRoleService.isModerator() ? 'reporting.home' : 'profile.diary');
                });
            });
    })

    .controller('DiarySidebarShowController', function ($scope, $state, DiaryEntryService, HarvestReportService,
                                                        CheckPermitNumber, entry, parameters) {

        $scope.entry = entry;
        $scope.getGameNameWithAmount = parameters.$getGameNameWithAmount;
        $scope.getUrl = DiaryEntryService.getUrl;
        $scope.permit = null;
        $scope.allowDelete = !entry.huntingDayId && entry.canEdit;
        $scope.allowEdit = entry.canEdit;

        if (entry.permitNumber){
            CheckPermitNumber.check(entry.permitNumber).success(function(permit){
                $scope.permit = permit;
            });
        }

        $scope.edit = function () {
            $scope.$dismiss('ignore');
            DiaryEntryService.edit($scope.entry);
        };

        $scope.remove = function () {
            DiaryEntryService.openRemoveForm($scope.entry).then(function () {
                $scope.$close();
                $state.reload();
            });
        };

        $scope.cancel = function () {
            $scope.$dismiss('cancel');
        };

        $scope.editHarvestReport = function () {
            if ($scope.entry.harvestReportId !== null) {
                $scope.$close();
                HarvestReportService.editById($scope.entry.harvestReportId);
            }
        };

        $scope.getSrvaMethodsForSidebar = function (methods) {
            return _.pluck(_.filter(methods, { 'isChecked': true }), 'name');
        };

        $scope.showSrvaMethodsInSidebar = function (methods) {
            return _.result(_.find(methods, {'isChecked': true}), 'isChecked');
        };

        $scope.isBoolean = _.isBoolean;
    })

    .controller('DiaryRemoveController', function ($uibModalInstance, $scope, $state, DiaryEntryService,
                                                   DiaryListViewState, entry) {

        $scope.diaryEntryType = angular.lowercase(entry.type);

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };

        $scope.remove = function () {
            DiaryEntryService.remove(entry).then(function () {
                DiaryListViewState.selectedDiaryEntry = null;
                $uibModalInstance.close();
                $state.reload();
            });
        };
    })

    .controller('HarvestFormController',
        function ($filter, $uibModal, $rootScope, $scope, $state, $stateParams, $translate,
                  ActivePermitsFields, ActiveRoleService, CheckHunterNumber, CheckPermitNumber, DiaryEntryService,
                  DiaryEntrySpecimenFormService, DiaryEntryType, Harvest, HarvestPermitSpeciesAmountService,
                  HarvestPermits, Helpers, MapDefaults, NotificationService, entry, parameters, relationship) {

            $scope.entry = entry;
            $scope.species = parameters.species;
            $scope.getCategoryName = parameters.$getCategoryName;

            $scope.getGameName = function (speciesCode, species) {
                var speciesName = parameters.$getGameName(speciesCode, species);

                if ($scope.viewState.withPermit && $scope.viewState.searchPermit) {
                    var amounts = $scope.viewState.searchPermit.speciesAmounts;
                    var validOnDate = $scope.viewState.date;

                    var intervalText = HarvestPermitSpeciesAmountService
                        .findMatchingAmountIntervalAsText(amounts, speciesCode, validOnDate);

                    return speciesName + ' ' + intervalText;
                }

                return speciesName;
            };
            $scope.getUrl = DiaryEntryService.getUrl;

            var filterUnknownOut = $filter('stripUnknown');

            $scope.getAvailableGameGenders = function () {
                return entry.isMoose() ? filterUnknownOut(parameters.genders) : parameters.genders;
            };

            $scope.getAvailableGameAges = function () {
                return entry.isMoose() ? filterUnknownOut(parameters.ages) : parameters.ages;
            };

            $scope.getAvailableGameFitnessClasses = function () {
                return parameters.fitnessClasses;
            };

            $scope.getAvailableGameAntlersTypes = function () {
                return parameters.antlersTypes;
            };

            $scope.viewState = {
                iAmTheShooter: entry.actorInfo ? entry.actorInfo.id === entry.authorInfo.id : !ActiveRoleService.isModerator(),
                searchHunterNumber: entry.actorInfo ? entry.actorInfo.hunterNumber : '',
                hunterByNumberNotFound: false,
                withPermit: entry.permitNumberRequired || entry.permitNumber,
                withPermittedMethod: !!entry.permittedMethod,
                searchPermitNumber: entry.permitNumber,
                fields: entry.fields,
                moderatorView: ActiveRoleService.isModerator()
            };

            var allSpecimensValid = function () {
                var age = $scope.isFieldRequired('age');
                var gender = $scope.isFieldRequired('gender');
                var weight = $scope.isFieldRequired('weight');
                return _.all(entry.specimens, function (specimen) {
                    return (!age || specimen.age) && (!gender || specimen.gender) && (!weight || specimen.weight);
                });
            };

            $scope.isValid = function () {
                return $scope.entry.gameSpeciesCode &&
                    $scope.entry.geoLocation.latitude &&
                    !$scope.viewState.hunterByNumberNotFound &&
                    ($scope.viewState.iAmTheShooter || entry.actorInfo) &&
                    !$scope.permitUnusable() &&
                    !$scope.invalidSpeciesForDate() &&
                    entry.totalSpecimenAmount > 0 &&
                    allSpecimensValid();
            };

            //timestamp conversion
            if (entry.pointOfTime) {
                var dateFilter = $filter('date');
                $scope.viewState.date = dateFilter(entry.pointOfTime, 'yyyy-MM-dd');
                $scope.viewState.time = dateFilter(entry.pointOfTime, 'HH:mm');
            }

            if (entry.huntingDayId) {
                $scope.entry.canEdit = false;
            }

            $scope.editSpecimen = function () {
                DiaryEntryService.editSpecimen($scope.entry, parameters, {
                    age: $scope.isFieldRequired('age'),
                    gender: $scope.isFieldRequired('gender'),
                    weight: $scope.isFieldRequired('weight')
                });
            };

            var onSaveSuccess = function (diaryEntry) {
                $scope.$close(diaryEntry);

                if (ActiveRoleService.isModerator()) {
                    return;
                }

                if ($scope.viewState.withPermit &&
                    $scope.viewState.searchPermit  &&
                    $scope.viewState.searchPermit.harvestsAsList) {

                    HarvestPermits.query().$promise.then(function (myPermits) {
                        // Check if current user is contactPerson for used permit
                        var isContactPerson = _.any(myPermits, function (p) {
                            return p.permitNumber === $scope.viewState.searchPermit.permitNumber;
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
            };

            $scope.save = function () {
                $scope.entry.setDateAndTime($scope.viewState.date, $scope.viewState.time);
                $scope.entry.fields = angular.copy($scope.viewState.fields);

                $scope.entry.saveOrUpdate().then(onSaveSuccess);
            };

            $scope.cancel = function () {
                $scope.$dismiss('cancel');
            };

            $scope.image = function (uuid) {
                DiaryEntryService.image($scope.entry, uuid, true);
            };

            $scope.removeImage = function (uuid) {
                $scope.entry.imageIds = _.pull($scope.entry.imageIds, uuid);
            };

            $scope.iAmTheShooterChanged = function () {
                $scope.entry.actorInfo = null;
                $scope.viewState.searchHunterNumber = null;
                $scope.viewState.hunterByNumberNotFound = false;
            };

            $scope.findAuthor = function () {
                _openFindPersonDialog(true, true).then(function (personInfo) {
                    $scope.entry.authorInfo = personInfo;

                    if (!$scope.entry.actorInfo) {
                        $scope.entry.actorInfo = personInfo;
                    }
                });
            };

            $scope.findHunter = function (isModerator) {
                _openFindPersonDialog(isModerator, isModerator).then(function (personInfo) {
                    $scope.entry.actorInfo = personInfo;
                });
            };

            function _openFindPersonDialog(showSsnSearch, showPermitNumberSearch) {
                return $uibModal.open({
                    templateUrl: 'harvestreport/findperson.html',
                    resolve: {
                        showSsnSearch: _.constant(showSsnSearch),
                        showPermitNumberSearch: _.constant(showPermitNumberSearch)
                    },
                    controller: 'HarvestReportFindPersonController'
                }).result;
            }

            $scope.$watch('viewState.searchHunterNumber', function (hunterNumber) {
                if (hunterNumber) {
                    CheckHunterNumber.check(hunterNumber)
                        .success(function (data, status) {
                            $scope.entry.actorInfo = data;
                            $scope.viewState.hunterByNumberNotFound = false;
                        })
                        .error(function (data, status) {
                            $scope.entry.actorInfo = null;
                            $scope.viewState.hunterByNumberNotFound = true;
                        });
                }
            });

            function _searchPermit(permitNumber) {
                if (!permitNumber) {
                    return;
                }

                CheckPermitNumber
                    .check(permitNumber)
                    .then(function (data) {
                        $scope.entry.permitNumber = permitNumber;
                        $scope.viewState.searchPermit = data.data;
                        $scope.viewState.permitByNumberNotFound = false;
                        _updateSpecies();
                        _updateFields();
                    }, function () {
                        $scope.entry.permitNumber = null;
                        $scope.viewState.searchPermit = null;
                        $scope.viewState.permitByNumberNotFound = true;
                        _updateSpecies();
                        _updateFields();
                    });
            }

            $scope.$watch('viewState.searchPermitNumber', _searchPermit);

            $scope.$watch('viewState.withPermit', function (withPermit) {
                if (!withPermit) {
                    $scope.entry.permitNumber = null;
                    $scope.viewState.searchPermit = null;
                    $scope.viewState.searchPermitNumber = null;
                    $scope.viewState.permitByNumberNotFound = true;

                    $scope.viewState.withPermittedMethod = false;
                    $scope.entry.permittedMethod = null;
                }
                _updateSpecies();
                _updateFields();
            });

            $scope.$watch('viewState.withPermittedMethod', function (newVal, oldVal) {
                if (newVal === oldVal) {
                    return;
                }
                if (newVal) {
                    $scope.entry.permittedMethod = {tapeRecorders: false, traps: false, other: false, description: null};
                } else {
                    $scope.entry.permittedMethod = null;
                }
            });

            function _updateFields() {
                if ($scope.entry.gameSpeciesCode && ($scope.entry.permitNumber || $scope.entry.isMoose() || $scope.entry.isPermitBasedDeer())) {
                    ActivePermitsFields.query({date: null, gameSpeciesCode: $scope.entry.gameSpeciesCode})
                        .$promise.then(function (res) {
                            $scope.viewState.fields = res[0];
                        });
                } else {
                    $scope.viewState.fields = null;
                }
            }

            $scope.$watch('entry.gameSpeciesCode', _updateFields);

            function _getFieldRequired(fieldName) {
                var fs = $scope.viewState;
                return fs && fs.fields ? fs.fields[fieldName] : 'NO';
            }

            $scope.isFieldVisible = function (fieldName) {
                return _getFieldRequired(fieldName) !== 'NO';
            };

            $scope.isFieldRequired = function (fieldName) {
                return _getFieldRequired(fieldName) === 'YES';
            };

            $scope.hasRequiredFields = function () {
                return $scope.isFieldRequired('age') || $scope.isFieldRequired('gender') || $scope.isFieldRequired('weight');
            };

            $scope.isPermittedMethodRequired = function () {
                return $scope.viewState.withPermittedMethod &&
                       !entry.permittedMethod.tapeRecorders &&
                       !entry.permittedMethod.traps &&
                       !entry.permittedMethod.other;
            };

            $scope.isWeightVisible = function () {
                return !$scope.isFieldVisible('weightEstimated') && !$scope.isFieldVisible('weightMeasured');
            };

            function _updateSpecies() {
                $scope.species = _getSpecies();
            }

            function _getSpecies() {
                if (!$scope.viewState.withPermit) {
                    return parameters.species;
                }
                if (!$scope.viewState.searchPermit) {
                    return [];
                }
                var speciesCodes = _.map($scope.viewState.searchPermit.speciesAmounts, function (spa) {
                    return spa.gameSpecies.code;
                });
                var res = _.filter(parameters.species, function (species) {
                    return speciesCodes.indexOf(species.code) !== -1;
                });
                if (res.length === 1) {
                    entry.gameSpeciesCode = res[0].code;
                }
                // clear currently selected species if it is not in the permits species
                if (speciesCodes.indexOf(entry.gameSpeciesCode) === -1) {
                    entry.gameSpeciesCode = null;
                }
                return res;
            }

            function _findSpeciesAmount(speciesCode, validOnDate) {
                if ($scope.viewState.withPermit && $scope.viewState.searchPermit) {
                    return HarvestPermitSpeciesAmountService.findMatchingAmount(
                        $scope.viewState.searchPermit.speciesAmounts, speciesCode, validOnDate);
                }

                return null;
            }

            $scope.invalidSpeciesForDate = function () {
                if ($scope.viewState.withPermit && $scope.viewState.searchPermit)  {
                    var validOnDate = $scope.viewState.date;

                    if (!validOnDate) {
                        return false;
                    }

                    if (!entry.gameSpeciesCode) {
                        return false;
                    }

                    return !_findSpeciesAmount(entry.gameSpeciesCode, validOnDate);
                }

                return false;
            };

            $scope.permitUnusable = function () {
                return $scope.viewState.withPermit &&
                    $scope.viewState.searchPermit &&
                    $scope.viewState.searchPermit.unavailable === true &&
                    entry.stateAcceptedToHarvestPermit !== 'ACCEPTED';
            };

            // Specimen functionality

            DiaryEntrySpecimenFormService.initAmountAndSpecimens($scope.entry);

            $scope.maxSpecimensForSelectedSpecies = 1;

            $scope.showInlineSpecimenEdit = function () {
                return $scope.entry.gameSpeciesCode && $scope.entry.totalSpecimenAmount === 1;
            };

            $scope.showSpecimenEditButton = function () {
                return $scope.entry.gameSpeciesCode && $scope.entry.totalSpecimenAmount !== 1;
            };

            $scope.isSpecimenEditDisabled = function () {
                return $scope.entry.totalSpecimenAmount > DiaryEntrySpecimenFormService.MAX_VISIBLE_AMOUNT ||
                    entry.totalSpecimenAmount < 1;
            };

            $scope.$watch('entry.gameSpeciesCode', function (newValue, oldValue) {
                $scope.maxSpecimensForSelectedSpecies =
                    DiaryEntrySpecimenFormService.getMaxSpecimenCountForHarvest(parameters, newValue);
                $scope.entry.totalSpecimenAmount = Math.min($scope.entry.totalSpecimenAmount, $scope.maxSpecimensForSelectedSpecies);

                DiaryEntrySpecimenFormService.setSpecimenCount($scope.entry, $scope.entry.totalSpecimenAmount);
            });

            $scope.$watch('entry.totalSpecimenAmount', function (newValue, oldValue) {
                DiaryEntrySpecimenFormService.setSpecimenCount($scope.entry, newValue);
            });
        })

    .controller('ObservationFormController', function ($filter, $scope, ActiveRoleService, DiaryEntryService,
                                                       DiaryEntrySpecimenFormService, Helpers, ObservationFieldRequirements,
                                                       ObservationFieldsMetadata, entry, relationship,
                                                       fieldMetadataForObservationSpecies, parameters) {

        $scope.entry = entry;
        $scope.species = parameters.species;
        $scope.getCategoryName = parameters.$getCategoryName;
        $scope.getUrl = DiaryEntryService.getUrl;
        $scope.maxSpecimenCount = DiaryEntrySpecimenFormService.getMaxSpecimenCountForObservation();
        $scope.fieldMetadata = fieldMetadataForObservationSpecies;
        $scope.fieldRequirements = null;
        $scope.isAuthorOrObserver = !relationship || relationship.isAuthor || relationship.isActor;

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
            return $scope.fieldRequirements ? $scope.fieldRequirements.getAvailableGameAges() : [];
        };

        $scope.getAvailableGameStates = function () {
            return $scope.fieldRequirements ? $scope.fieldRequirements.getAvailableGameStates() : [];
        };

        $scope.getAvailableGameMarkings = function () {
            return $scope.fieldRequirements ? $scope.fieldRequirements.getAvailableGameMarkings() : [];
        };

        $scope.getGameName = function (speciesCode, species) {
            return parameters.$getGameName(speciesCode, species);
        };

        function isSumOfAmountFieldsValid() {
            return $scope.fieldRequirements && $scope.fieldRequirements.isSumOfAmountFieldsValid($scope.entry);
        }

        $scope.viewState = {
            iAmTheObserver: entry.actorInfo ? entry.actorInfo.id === entry.authorInfo.id : !ActiveRoleService.isModerator(),
            fields: entry.fields,
            moderatorView: ActiveRoleService.isModerator()
        };

        $scope.isValid = function () {
            return $scope.entry.gameSpeciesCode &&
                $scope.entry.observationType &&
                $scope.entry.geoLocation.latitude &&
                ($scope.viewState.iAmTheObserver || $scope.entry.actorInfo) &&
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

            DiaryEntryService.editSpecimen($scope.entry, parameters, availableFields, $scope.fieldRequirements);
        };

        $scope.iAmTheObserverChanged = function () {
            $scope.entry.actorInfo = null;
        };

        var onSaveSuccess = function (entry) {
            $scope.$close(entry);
        };

        $scope.save = function () {
            $scope.entry.setDateAndTime($scope.viewState.date, $scope.viewState.time);
            $scope.entry.fields = angular.copy($scope.viewState.fields);

            $scope.entry.saveOrUpdate().then(onSaveSuccess);
        };

        $scope.cancel = function () {
            $scope.$dismiss('cancel');
        };

        $scope.image = function (uuid) {
            DiaryEntryService.image($scope.entry, uuid, true);
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
                    ObservationFieldsMetadata.forSpecies({ gameSpeciesCode: newValue}).$promise.then(function (metadata) {
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
            if (newValue) {
                newValue.resetIllegalObservationFields($scope.entry);
            }
        });

        $scope.$watch('entry.totalSpecimenAmount', function (newValue, oldValue) {
            if (newValue) {
                $scope.entry.totalSpecimenAmount = Math.min(newValue, $scope.maxSpecimenCount);
                DiaryEntrySpecimenFormService.setSpecimenCount($scope.entry, newValue);
            }
        });
    })

    .controller('DiaryEntrySpecimenModalController', function ($scope, DiaryEntrySpecimenFormService, availableFields,
                                                               entry, parameters, observationFieldRequirements) {

        $scope.entry = entry;
        $scope.maxVisibleSpecimens = DiaryEntrySpecimenFormService.MAX_VISIBLE_AMOUNT;

        function isMultipleSpecimensAllowedForDiaryEntry() {
            return !$scope.entry.isHarvest() || parameters.isMultipleSpecimensAllowedForHarvestSpecies($scope.entry.gameSpeciesCode);
        }

        $scope.getAvailableGameGenders = function () {
            return parameters.genders;
        };

        $scope.getAvailableGameAges = function () {
            // Use observationFieldRequirements to resolve age values, if available;
            // otherwise default to using general game diary 'parameters' object.
            return observationFieldRequirements ? observationFieldRequirements.getAvailableGameAges() : parameters.ages;
        };

        $scope.getAvailableGameStates = function () {
            return observationFieldRequirements ? observationFieldRequirements.getAvailableGameStates() : [];
        };

        $scope.getAvailableGameMarkings = function () {
            return observationFieldRequirements ? observationFieldRequirements.getAvailableGameMarkings() : [];
        };

        $scope.isFieldRequired = function (fieldName) {
            return availableFields[fieldName] === true;
        };

        $scope.isFieldVisible = function (fieldName) {
            return availableFields[fieldName] === false;
        };

        $scope.isAddSpecimenButtonHidden = function () {
            return !entry.canEdit || entry.specimens.length > 0 && !isMultipleSpecimensAllowedForDiaryEntry();
        };

        $scope.addSpecimen = function (index) {
            if (entry.totalSpecimenAmount < $scope.maxVisibleSpecimens && isMultipleSpecimensAllowedForDiaryEntry()) {
                DiaryEntrySpecimenFormService.addNewSpecimen(entry, index);
                entry.totalSpecimenAmount++;
            }
        };
    })

    .controller('DiaryImageController', function ($scope, $uibModalInstance, entry, uuid, tmp) {
            var state = {upload: true, uploading: false, success: false, error: false};

            $scope.onUpload = function (response) {
                state.upload = false;
                state.uploading = true;
            };

            $scope.onSuccess = function (response) {
                var newUuid = response.data;
                if (tmp) {
                    if (_.isUndefined(entry.imageIds)) {
                        entry.imageIds = [];
                    }
                    entry.imageIds.push(newUuid);
                    if (uuid) {
                        var i = entry.imageIds.indexOf(uuid);
                        if (i !== -1) {
                            entry.imageIds.splice(i, 1);
                        }
                    }
                }
                state.success = true;
                state.uploading = false;
                $uibModalInstance.close();
            };

            $scope.onError = function (response) {
                state.error = true;
                state.uploading = false;
            };

            $scope.state = state;

            var formdata = { gameDiaryEntryId: entry.id };
            if (uuid) {
                formdata.replace = uuid;
            }

            $scope.acceptTypes = 'image/jpeg, image/pjpeg, image/png';
            $scope.formdata = formdata;
            $scope.replace = uuid;
            $scope.url = tmp ? '/api/v1/gamediary/image/uploadtmp' : '/api/v1/gamediary/image/uploadFor' + (entry.isHarvest() ? 'Harvest' : 'Observation');

            $scope.close = function () {
                $uibModalInstance.close();
            };
        })
;
