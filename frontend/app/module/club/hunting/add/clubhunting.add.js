'use strict';

angular.module('app.clubhunting.add', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('club.hunting.add', {
                abstract: true,
                parent: 'club',
                url: '/hunting/{groupId:[0-9]{1,8}}/{entryId}',
                template: '<div ui-view></div>',
                params: {
                    entryId: {
                        value: 'new',
                        squash: false
                    }
                },
                resolve: {
                    groupId: function ($stateParams) {
                        return $stateParams.groupId;
                    },
                    huntingDays: function (ClubGroups, clubId, groupId) {
                        return ClubGroups.huntingDays({id: groupId, clubId: clubId}).$promise;
                    },
                    huntingArea: function (ClubHuntingViewData, ClubGroups, clubId, groupId) {
                        var huntingData = ClubHuntingViewData.get();

                        return huntingData.huntingArea ? huntingData.huntingArea : ClubGroups.huntingArea({
                            id: groupId,
                            clubId: clubId
                        }).$promise;
                    },
                    memberCandidates: function (ClubGroupMemberService, clubId, groupId) {
                        return ClubGroupMemberService.listShooterCandidates(clubId, groupId);
                    },
                    huntingFinished: function (ClubGroups, clubId, groupId) {
                        return ClubGroups.get({clubId: clubId, id: groupId}).$promise.then(function (group) {
                            return group.huntingFinished;
                        });
                    }
                }
            })
            .state('club.hunting.add.harvest', {
                url: '/harvest?gameSpeciesCode',
                templateUrl: 'club/hunting/add/layout.html',
                controller: 'OpenClubHuntingEntryFormController',
                wideLayout: true,
                resolve: {
                    diaryEntry: function ($stateParams, Harvest) {
                        if ($stateParams.entryId === 'new') {
                            return Harvest.createTransient({
                                gameSpeciesCode: $stateParams.gameSpeciesCode
                            });
                        } else {
                            return Harvest.get({id: $stateParams.entryId}).$promise;
                        }
                    }
                }
            })

            .state('club.hunting.add.observation', {
                url: '/observation?gameSpeciesCode',
                templateUrl: 'club/hunting/add/layout.html',
                controller: 'OpenClubHuntingEntryFormController',
                wideLayout: true,
                resolve: {
                    diaryEntry: function ($stateParams, Observation) {
                        if ($stateParams.entryId === 'new') {
                            return Observation.createTransient({
                                gameSpeciesCode: $stateParams.gameSpeciesCode
                            });
                        } else {
                            return Observation.get({id: $stateParams.entryId}).$promise;
                        }
                    }
                }
            });
    })

    .controller('OpenClubHuntingEntryFormController', function ($scope, $state, $q, offCanvasStack,
                                                                Helpers, GIS, NotificationService,
                                                                MapState, MapDefaults, ActiveRoleService,
                                                                GameDiaryParameters, ClubGroups,
                                                                DiaryEntrySpecimenFormService,
                                                                ClubHuntingDayService, ClubHuntingEntryService,
                                                                ActivePermitsFields, ObservationFieldsMetadata,
                                                                clubId, groupId, huntingArea, huntingFinished,
                                                                diaryEntry, huntingDays, memberCandidates) {
        $scope.diaryEntry = diaryEntry;
        $scope.mapEvents = MapDefaults.getMapBroadcastEvents(['click']);
        $scope.mapDefaults = MapDefaults.create();
        $scope.mapGeoJSON = huntingArea ? {
            data: huntingArea,
            onEachFeature: _.noop,
            style: MapDefaults.getGeoJsonOptions({clickable: false})
        } : null;

        var mapGeoLocation = MapState.toGeoLocation();

        if (mapGeoLocation) {
            $scope.geoCenter = mapGeoLocation;
        } else if (diaryEntry.geoLocation && diaryEntry.geoLocation.latitude) {
            $scope.geoCenter = diaryEntry.geoLocation;
        } else {
            var bbox = _.get(huntingArea, 'features[0].bbox');
            $scope.geoCenter = GIS.getGeolocationFromGeoJsonBbox(bbox, 10);
        }

        // This is required to enable form fields normally disabled in gameDiary form
        $scope.diaryEntry.canEdit = true;

        if (diaryEntry.isObservation()) {
            diaryEntry.withinMooseHunting = true;
        }

        // Initialize specimen array to match totalAmount
        DiaryEntrySpecimenFormService.initAmountAndSpecimens($scope.diaryEntry);

        if (ActiveRoleService.isModerator() && !$scope.diaryEntry.authorInfo) {
            // Moderator: select primary hunting leader as author if not set
            diaryEntry.authorInfo = _(memberCandidates)
                .filter({occupationType: 'RYHMAN_METSASTYKSENJOHTAJA', callOrder: 0})
                .map('person')
                .map(_.partialRight(_.pick, ['id', 'hunterNumber']))
                .first();
        }

        function openDiaryEntryForm(diaryEntry) {
            var templateUrl;
            var controller;
            var resolve = {
                entry: _.constant(diaryEntry),
                huntingDays: _.constant(huntingDays),
                memberCandidates: _.constant(memberCandidates),
                createHuntingDayForEntry: function() {
                    var pointOfTimeAsDate = moment(diaryEntry.pointOfTime, 'YYYY-MM-DD[T]HH:mm');
                    var startDateAsString = Helpers.dateToString(pointOfTimeAsDate, 'YYYY-MM-DD');

                    return function () {
                        return ClubHuntingDayService.createHuntingDay(clubId, groupId, startDateAsString);
                    };
                },
                huntingFinished: _.constant(huntingFinished),
                parameters: function () {
                    return GameDiaryParameters.query().$promise;
                },
                permitSpeciesAmount: function () {
                    return ClubGroups.permitSpeciesAmount({clubId: clubId, id: groupId}).$promise;
                }
        };

            if (diaryEntry.isHarvest()) {
                templateUrl = 'club/hunting/add/harvest-form.html';
                controller = 'ClubHarvestFormController';

                angular.extend(resolve, {
                    fields: function () {
                        if (diaryEntry.gameSpeciesCode && (diaryEntry.permitNumber || diaryEntry.isMoose() || diaryEntry.isPermitBasedDeer())) {
                            var fieldsQuery = {
                                date: null,
                                gameSpeciesCode: diaryEntry.gameSpeciesCode
                            };

                            return ActivePermitsFields.query(fieldsQuery).$promise.then(function (res) {
                                return angular.isArray(res) ? _.first(res) : null;
                            });
                        }

                        return null;
                    }
                });
            } else if (diaryEntry.isObservation()) {
                templateUrl = 'club/hunting/add/observation-form.html';
                controller = 'ClubObservationFormController';

                angular.extend(resolve, {
                    availableSpecies: function () {
                        return GameDiaryParameters.getObservationSpeciesWithinMooseHunting().$promise;
                    },
                    fieldMetadataForObservationSpecies: function () {
                        if (diaryEntry.gameSpeciesCode) {
                            return ObservationFieldsMetadata.forSpecies({gameSpeciesCode: diaryEntry.gameSpeciesCode}).$promise;
                        }
                        return null;
                    }
                });
            }

            return offCanvasStack.open({
                controller: controller,
                templateUrl: templateUrl,
                resolve: resolve,
                largeDialog: false
            }).result;
        }

        function onSaveSuccessful(diaryEntry) {
            ClubHuntingEntryService.setSelectedDiaryEntry(clubId, groupId, diaryEntry);

            NotificationService.showDefaultSuccess();
        }

        openDiaryEntryForm(diaryEntry)
            .then(function (diaryEntry) {
                var huntingDayPromise = $q.resolve();
                if (diaryEntry.isHarvest() && diaryEntry.isPermitBasedDeer()) {
                    huntingDayPromise = ClubHuntingDayService.getOrCreate(groupId, diaryEntry.pointOfTime)
                        .then(function (huntingDay) {
                            diaryEntry.huntingDayId = huntingDay.id;
                            return diaryEntry;
                        });
                }
                return huntingDayPromise.then(function () {
                    return diaryEntry.saveOrUpdate().then(
                        onSaveSuccessful,
                        NotificationService.showDefaultFailure);
                });

            }, function (err) {
                var errorsToIgnore = ['cancel', 'escape', 'delete', 'back'];

                if (!angular.isString(err) || errorsToIgnore.indexOf(err) < 0) {
                    NotificationService.showDefaultFailure();
                }

                return $q.reject(err);
            })
            .finally(function () {
                $state.go('club.hunting');
            });
    })

    .directive('speciesSelect', function () {
        return {
            restrict: 'E',
            templateUrl: 'club/hunting/add/select-species.html',
            scope: {
                diaryParameters: '=',
                diaryEntry: '=',
                availableSpecies: '='
            },
            controllerAs: 'ctrl',
            bindToController: true,
            controller: function () {
                this.availableSpecies = this.availableSpecies || this.diaryParameters.species;
                this.getCategoryName = this.diaryParameters.$getCategoryName;
                this.getGameName = this.diaryParameters.$getGameName;
                this.isDisabled = function () {
                    return this.diaryEntry.isHarvest() || !this.diaryEntry.canEdit;
                };
            }
        };
    })

    .directive('clubPersonSelect', function (CheckHunterNumber) {
        return {
            restrict: 'E',
            templateUrl: 'club/hunting/add/select-person.html',
            scope: {
                memberList: '=',
                modelValue: '=person'
            },
            bindToController: true,
            controllerAs: '$ctrl',
            controller: function () {
                var $ctrl = this;
                $ctrl.selectedMember = null;
                $ctrl.searchHunterNumber = null;
                $ctrl.searchResultNotFound = false;

                $ctrl.formatPersonName = function (person) {
                    return person ? person.lastName + ', ' + person.byName : '';
                };

                $ctrl.isHunterSet = function () {
                    return isValidPerson($ctrl.modelValue);
                };

                $ctrl.onMemberSelected = function () {
                    $ctrl.searchHunterNumber = null;
                    $ctrl.searchResultNotFound = false;

                    if ($ctrl.selectedMember) {
                        var person = $ctrl.selectedMember.person;

                        $ctrl.searchHunterNumber = person.hunterNumber;
                        $ctrl.modelValue = _.pick(person, ['id', 'hunterNumber']);
                    } else {
                        $ctrl.modelValue = null;
                    }
                };

                $ctrl.onHunterNumberSearch = function () {
                    $ctrl.modelValue = null;

                    if (!$ctrl.searchHunterNumber) {
                        $ctrl.searchResultNotFound = false;
                        return;
                    }

                    CheckHunterNumber.check($ctrl.searchHunterNumber)
                        .success(function (person) {
                            if (isValidPerson(person)) {
                                $ctrl.searchResultNotFound = false;
                                $ctrl.modelValue = person;
                            } else {
                                $ctrl.searchResultNotFound = true;
                            }
                        })
                        .error(function () {
                            $ctrl.searchResultNotFound = true;
                        });
                };

                function isValidPerson(person) {
                    return person && (person.id || person.hunterNumber);
                }

                if (isValidPerson($ctrl.modelValue)) {
                    $ctrl.searchHunterNumber = $ctrl.modelValue.hunterNumber;

                    $ctrl.selectedMember = _.find($ctrl.memberList, function (m) {
                        var p1 = $ctrl.modelValue;
                        var p2 = m.person;

                        return p2.id === p1.id || p2.hunterNumber === p1.hunterNumber;
                    });
                }
            }
        };
    })

    .directive('timeInsideHuntingDay', function ($parse, Helpers) {
        return {
            restrict: 'A',
            require: 'ngModel',
            scope: false,
            link: function (scope, element, attrs, ngModelController) {
                var getHuntingDay = _.partial($parse(attrs.timeInsideHuntingDay), scope);
                var getIsNextDay = _.partial($parse(attrs.isNextDay), scope);

                ngModelController.$validators.timeInsideHuntingDay = function (modelValue, viewValue) {
                    var value = modelValue || viewValue;

                    if (!value) {
                        return true;
                    }

                    var h = getHuntingDay();
                    var isNextDay = getIsNextDay();

                    if (h && h.startDate && h.endDate && h.startTime && h.endTime) {
                        var start = Helpers.parseDateAndTime(h.startDate, h.startTime, 'YYYY-MM-DD');
                        var end = Helpers.parseDateAndTime(h.endDate, h.endTime, 'YYYY-MM-DD');
                        var t = Helpers.parseDateAndTime(isNextDay ? h.endDate : h.startDate, value, 'YYYY-MM-DD');

                        if (start && end) {
                            return t.isBetween(start, end) || t.isSame(start) || t.isSame(end);
                        }
                    }

                    return false;
                };

                var listener = function (newValue, oldValue) {
                    if (newValue !== oldValue) {
                        ngModelController.$validate();
                    }
                };

                scope.$watch(getHuntingDay, listener);
                scope.$watch(getIsNextDay, listener);
            }
        };
    })

    .directive('nonMooseDateTimeSelect', function (Helpers) {
        return {
            restrict: 'E',
            require: '^form',
            templateUrl: 'club/hunting/add/select-date-and-time.html',
            scope: {diaryEntry: '=', speciesAmount: '='},
            link: function ($scope, element, attrs, formController) {
                $scope.parentForm = formController;
            },
            controller: function ($scope, HarvestPermitSpeciesAmountService) {
                $scope.viewState = {
                    nonMooseDate: null,
                    time: null
                };

                if ($scope.diaryEntry.pointOfTime) {
                    var t = moment($scope.diaryEntry.pointOfTime, 'YYYY-MM-DD[T]HH:mm');
                    $scope.viewState.time = Helpers.dateToString(t, 'HH:mm');
                    $scope.viewState.nonMooseDate = Helpers.dateToString(t, 'YYYY-MM-DD');
                }

                $scope.isPermitValidOnDate = function () {
                    var isValid = !$scope.viewState.nonMooseDate || HarvestPermitSpeciesAmountService.isValidDateForSpeciesAmount(
                            $scope.speciesAmount, $scope.viewState.nonMooseDate);
                    $scope.parentForm.$setValidity('nonMooseDate', isValid);
                    return isValid;
                };


                $scope.$watchGroup(['viewState.nonMooseDate', 'viewState.time'], function (newValues) {
                    var nonMooseDate = newValues[0];
                    var time = newValues[1];
                    if (nonMooseDate && time) {
                        $scope.diaryEntry.setDateAndTime(nonMooseDate, time);
                    }
                });
            }
        };
    })

    .directive('huntingDayTimeSelect', function (Helpers) {
        return {
            restrict: 'E',
            require: '^form',
            templateUrl: 'club/hunting/add/select-day-and-time.html',
            scope: {
                huntingDays: '=',
                diaryEntry: '=',
                huntingFinished: '=',
                create: '&'
            },
            link: function ($scope, element, attrs, formController) {
                $scope.parentForm = formController;
            },
            controller: function ($scope) {
                $scope.viewState = {
                    huntingDay: null,
                    time: null,
                    nextDayHunting: false,
                    huntingFinished: $scope.huntingFinished
                };

                function parseDay(str) {
                    return moment(str, 'YYYY-MM-DD');
                }

                if ($scope.diaryEntry.pointOfTime) {
                    var t = moment($scope.diaryEntry.pointOfTime, 'YYYY-MM-DD[T]HH:mm');

                    $scope.viewState.time = Helpers.dateToString(t, 'HH:mm');
                    var entryDay = Helpers.dateToString(t, 'YYYY-MM-DD');

                    if ($scope.diaryEntry.huntingDayId) {
                        $scope.viewState.huntingDay = _.find($scope.huntingDays, function (d) {
                            return d.id === $scope.diaryEntry.huntingDayId;
                        });

                        if ($scope.viewState.huntingDay && entryDay === $scope.viewState.huntingDay.endDate) {
                            $scope.viewState.nextDayHunting = true;
                        }
                    } else {
                        $scope.viewState.huntingDay = _.find($scope.huntingDays, function (d) {
                            return d.startDate === entryDay;
                        });
                    }
                }

                $scope.getHuntingDayName = function (huntingDay) {
                    var day = parseDay(huntingDay.startDate);
                    var suffix = (huntingDay.startDate === huntingDay.endDate) ? '' : ' ( +1 )';
                    return Helpers.dateToString(day, 'D.M.YYYY') + suffix;
                };

                $scope.addHuntingDay = function () {
                    $scope.create().then(function (huntingDay) {
                        $scope.huntingDays.push(huntingDay);
                        $scope.viewState.huntingDay = huntingDay;
                    });
                };

                $scope.showNextDayHunting = function () {
                    var h = $scope.viewState.huntingDay;
                    return h && h.startDate && h.startDate !== h.endDate;
                };

                $scope.$watchGroup(['viewState.huntingDay', 'viewState.time', 'viewState.nextDayHunting'], function (newValues) {
                    var huntingDay = newValues[0];
                    var time = newValues[1];
                    var nextDayHunting = newValues[2];

                    if (huntingDay && time) {
                        $scope.diaryEntry.huntingDayId = huntingDay.id;

                        if (huntingDay.startDate !== huntingDay.endDate && nextDayHunting) {
                            var nextDay = parseDay(huntingDay.startDate).add(1, 'day');
                            $scope.diaryEntry.setDateAndTime(nextDay, time);
                        } else {
                            $scope.diaryEntry.setDateAndTime(huntingDay.startDate, time);
                        }
                    } else {
                        $scope.diaryEntry.huntingDayId = null;
                        $scope.diaryEntry.pointOfTime = null;
                    }
                });
            }
        };
    })

    .controller('ClubHarvestFormController', function ($scope, $filter, Helpers, ActiveRoleService,
                                                       Harvest, ClubHuntingEntryService,
                                                       entry, parameters, fields, huntingDays,
                                                       memberCandidates, createHuntingDayForEntry,
                                                       huntingFinished, permitSpeciesAmount) {
        $scope.entry = entry;
        $scope.isModerator = ActiveRoleService.isModerator();
        $scope.parameters = parameters;
        $scope.availableSpecies = parameters.species;
        $scope.huntingDays = huntingDays;
        $scope.memberCandidates = memberCandidates;
        $scope.huntingFinished = huntingFinished;
        $scope.showAccept = entry.id && !entry.huntingDayId && !$scope.huntingFinished;
        $scope.showSave = !$scope.huntingFinished && (!entry.id || entry.huntingDayId);
        $scope.createHuntingDayForHarvest = createHuntingDayForEntry;
        $scope.permitSpeciesAmount = permitSpeciesAmount;

        $scope.availableGenders = $filter('stripUnknown')(parameters.genders);
        $scope.availableAges = $filter('stripUnknown')(parameters.ages);

        $scope.mooseGroupSelected = $scope.entry.isMoose();

        $scope.isValid = function () {
            var permitBasedDeer = $scope.entry.isPermitBasedDeer();

            return $scope.entry.gameSpeciesCode &&
                $scope.entry.geoLocation.latitude &&
                $scope.entry.actorInfo && $scope.entry.actorInfo.hunterNumber &&
                (permitBasedDeer || $scope.entry.huntingDayId) &&
                $scope.entry.pointOfTime &&
                (permitBasedDeer || $scope.entry.specimens[0].weightEstimated || $scope.entry.specimens[0].weightMeasured);
        };

        $scope.save = function () {
            $scope.$close($scope.entry);
        };

        $scope.cancel = function () {
            $scope.$dismiss('cancel');
        };

        function _getFieldRequired(fieldName) {
            return fields ? fields[fieldName] : 'NO';
        }

        $scope.isFieldVisible = function (fieldName) {
            return _getFieldRequired(fieldName) !== 'NO';
        };

        $scope.isFieldRequired = function (fieldName) {
            return _getFieldRequired(fieldName) === 'YES';
        };

        $scope.isEstimatedWeightVisible = function () {
            return $scope.isFieldVisible('weightEstimated');
        };

        $scope.isMeasuredWeightVisible = function () {
            return $scope.isFieldVisible('weightMeasured');
        };

        var isEstimatedAndMeasuredWeightVisible = function () {
            return $scope.isEstimatedWeightVisible() && $scope.isMeasuredWeightVisible();
        };

        $scope.isEstimatedWeightRequired = function () {
            return $scope.mooseGroupSelected &&
                isEstimatedAndMeasuredWeightVisible() &&
                !_.get($scope.entry, 'specimens[0].weightMeasured', false);
        };

        $scope.isMeasuredWeightRequired = function () {
            return $scope.mooseGroupSelected &&
                   isEstimatedAndMeasuredWeightVisible() &&
                   !_.get($scope.entry, 'specimens[0].weightEstimated', false);
        };
    })

    .controller('ClubObservationFormController', function ($scope, DiaryEntryService, DiaryEntrySpecimenFormService,
                                                           ActiveRoleService, Helpers,
                                                           DiaryEntryType, ObservationFieldsMetadata,
                                                           fieldMetadataForObservationSpecies, availableSpecies, entry,
                                                           huntingDays, memberCandidates, parameters,
                                                           createHuntingDayForEntry,
                                                           huntingFinished) {
        $scope.entry = entry;
        $scope.isModerator = ActiveRoleService.isModerator();
        $scope.parameters = parameters;
        $scope.availableSpecies = availableSpecies;
        $scope.huntingDays = huntingDays;
        $scope.memberCandidates = memberCandidates;
        $scope.maxSpecimenCount = DiaryEntrySpecimenFormService.getMaxSpecimenCountForObservation();
        $scope.fieldMetadata = fieldMetadataForObservationSpecies;
        $scope.fieldRequirements = null;
        $scope.huntingFinished = huntingFinished;
        $scope.showAccept = entry.id && !entry.huntingDayId && !$scope.huntingFinished;
        $scope.showSave = !$scope.huntingFinished && (!entry.id || entry.huntingDayId);
        $scope.createHuntingDayForObservation = createHuntingDayForEntry;

        $scope.getAvailableObservationTypes = function () {
            return $scope.fieldMetadata ? $scope.fieldMetadata.getAvailableObservationTypes(true) : [];
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

        $scope.isFieldRequired = function (fieldName) {
            return $scope.fieldRequirements && $scope.fieldRequirements.isFieldRequired(fieldName);
        };

        $scope.isFieldVisible = function (fieldName) {
            return $scope.fieldRequirements && $scope.fieldRequirements.isFieldLegal(fieldName);
        };

        function isSumOfAmountFieldsValid() {
            return $scope.fieldRequirements && $scope.fieldRequirements.isSumOfAmountFieldsValid($scope.entry);
        }

        function isValidPerson(person) {
            return _.isObject(person) && (person.id || person.hunterNumber);
        }

        function isValidGeoLocation(geoLocation) {
            return _.isObject(geoLocation) && _.isFinite(geoLocation.latitude) && _.isFinite(geoLocation.longitude);
        }

        $scope.isValid = function () {
            return entry.gameSpeciesCode &&
                entry.observationType &&
                isValidGeoLocation(entry.geoLocation) &&
                isValidPerson(entry.actorInfo) &&
                (isValidPerson(entry.authorInfo) || !$scope.isModerator) &&
                entry.huntingDayId &&
                entry.pointOfTime &&
                isSumOfAmountFieldsValid();
        };

        $scope.editSpecimen = function () {
            var availableFields = $scope.fieldRequirements ? $scope.fieldRequirements.getSpecimenFields() : {};

            _.forEach(availableFields, function (fieldName) {
                if ($scope.isFieldVisible(fieldName)) {
                    availableFields[fieldName] = $scope.isFieldRequired(fieldName);
                }
            });

            DiaryEntryService.editSpecimen($scope.entry, parameters, availableFields, $scope.fieldRequirements);
        };

        $scope.save = function () {
            $scope.$close($scope.entry);
        };

        $scope.cancel = function () {
            $scope.$dismiss('cancel');
        };

        $scope.showInlineSpecimenEdit = function () {
            return $scope.entry.gameSpeciesCode && $scope.entry.totalSpecimenAmount === 1;
        };

        $scope.showEditSpecimenButton = function () {
            return $scope.entry.totalSpecimenAmount > 1 &&
                $scope.fieldRequirements && !_.isEmpty($scope.fieldRequirements.getSpecimenFields());
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

        $scope.$watch('fieldMetadata', function (newValue, oldValue) {
            if (newValue) {
                newValue.resetIllegalObservationFields($scope.entry);
                $scope.fieldRequirements = newValue.getFieldRequirements(true, $scope.entry.observationType);
                $scope.fieldMetadata.resetIllegalObservationFields($scope.entry);
            } else {
                $scope.fieldRequirements = null;
            }
        });

        $scope.$watch('entry.observationType', function (newValue, oldValue) {
            if (newValue !== oldValue) {
                if ($scope.fieldMetadata) {
                    $scope.fieldRequirements = $scope.fieldMetadata.getFieldRequirements($scope.entry.withinMooseHunting, $scope.entry.observationType);
                }
            }
        });

        $scope.$watch('fieldRequirements', function (newValue, oldValue) {
            if (newValue) {
                newValue.resetIllegalObservationFields($scope.entry);
            }
        });

        $scope.$watchCollection(
            function () {
                return $scope.getAvailableObservationTypes();
            },
            function (newValue, oldValue) {
                if (newValue && newValue.length === 1) {
                    $scope.entry.observationType = newValue[0];
                }
            });

        $scope.$watch('entry.totalSpecimenAmount', function (newValue, oldValue) {
            if (newValue) {
                $scope.entry.totalSpecimenAmount = Math.min(newValue, $scope.maxSpecimenCount);
                DiaryEntrySpecimenFormService.setSpecimenCount($scope.entry, newValue);
            }
        });
    });
