'use strict';

angular.module('app.clubhunting.add', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('club.hunting.add', {
                parent: 'club',
                url: '/hunting/entry',
                templateUrl: 'club/hunting/add/layout.html',
                controller: 'OpenClubHuntingEntryFormController',
                controllerAs: '$ctrl',
                bindToController: true,
                wideLayout: true,
                resolve: {
                    selectedItem: function (ClubHuntingActiveEntry) {
                        return ClubHuntingActiveEntry.reloadSelectedItem();
                    },
                    groupId: function (selectedItem) {
                        return selectedItem.groupId;
                    },
                    huntingDays: function (ClubGroupDiary, groupId) {
                        return ClubGroupDiary.huntingDays({id: groupId}).$promise;
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
            });
    })

    .service('ClubHuntingHarvestFields', function ($http, RequiredHarvestFields) {
        function responseTransformer (response) {
            var requiredFields = response.data;

            // Replace field requirements object with enhanced one having custom API.
            return RequiredHarvestFields.create(requiredFields);
        }

        this.get = function (huntingGroupId) {
            return $http.get('/api/v1/club/group/harvest/fields', {
                params: {
                    huntingGroupId: huntingGroupId
                }
            }).then(responseTransformer);
        };

        this.getLegallyMandatory = function (huntingGroupId) {
            return $http.get('/api/v1/club/group/harvest/legally-mandatory-fields', {
                params: {
                    huntingGroupId: huntingGroupId
                }
            }).then(responseTransformer);
        };
    })

    .service('ClubHuntingEntryFormService', function (ActiveRoleService, ClubGroups, ClubHuntingDayService,
                                                      ClubHuntingHarvestFields, DiaryEntrySpecimenFormService,
                                                      GameDiaryParameters, Helpers, ObservationCategory,
                                                      ObservationFieldsMetadata, offCanvasStack) {

        this.openDiaryEntryForm = function (item, huntingDays, memberCandidates, huntingFinished) {
            var diaryEntry = item.diaryEntry;
            var clubId = item.clubId;
            var groupId = item.groupId;
            var templateUrl;
            var controller;
            var resolve = {
                entry: _.constant(diaryEntry),
                huntingDays: _.constant(huntingDays),
                memberCandidates: _.constant(memberCandidates),
                createHuntingDayForEntry: function () {
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

            // This is required to enable form fields normally disabled in game diary form
            diaryEntry.canEdit = true;

            if (diaryEntry.isObservation()) {
                diaryEntry.observationCategory = ObservationCategory.MOOSE_HUNTING;
            }

            // Initialize specimen array to match totalAmount
            DiaryEntrySpecimenFormService.initAmountAndSpecimens(diaryEntry);

            if (ActiveRoleService.isModerator() && !diaryEntry.authorInfo) {
                // Moderator: select primary hunting leader as author if not set
                diaryEntry.authorInfo = _(memberCandidates)
                    .filter({occupationType: 'RYHMAN_METSASTYKSENJOHTAJA', callOrder: 0})
                    .map('person')
                    .map(_.partialRight(_.pick, ['id', 'hunterNumber']))
                    .head();
            }

            if (diaryEntry.isHarvest()) {
                templateUrl = 'club/hunting/add/harvest-form.html';
                controller = 'ClubHarvestFormController';

                angular.extend(resolve, {
                    fields: function () {
                        return ClubHuntingHarvestFields.get(groupId);
                    },
                    groupId: _.constant(groupId)
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
                    },
                    isModerator: function () {
                        return ActiveRoleService.isModerator();
                    }
                });
            }

            return offCanvasStack.open({
                controller: controller,
                templateUrl: templateUrl,
                resolve: resolve,
                largeDialog: false
            }).result;
        };
    })

    .controller('OpenClubHuntingEntryFormController', function ($q, $state, ClubHuntingActiveEntry,
                                                                ClubHuntingDayService, ClubHuntingEntryFormService,
                                                                NotificationService, groupId, huntingArea,
                                                                huntingDays, huntingFinished, memberCandidates,
                                                                selectedItem) {
        var $ctrl = this;

        $ctrl.diaryEntry = selectedItem.diaryEntry;
        $ctrl.huntingArea = huntingArea;

        ClubHuntingEntryFormService.openDiaryEntryForm(
            selectedItem, huntingDays, memberCandidates, huntingFinished).then(function (returnValue) {
            var huntingDayPromise = $q.resolve();
            var diaryEntry = returnValue.entry;
            var createHuntingDay = returnValue.createDay;

            if (diaryEntry.isHarvest() && createHuntingDay) {
                huntingDayPromise = ClubHuntingDayService.getOrCreate(groupId, diaryEntry.pointOfTime)
                    .then(function (huntingDay) {
                        diaryEntry.huntingDayId = huntingDay.id;
                        return diaryEntry;
                    });
            }

            return huntingDayPromise.then(function () {
                return diaryEntry.saveOrUpdate().then(function (savedDiaryEntry) {
                    selectedItem.diaryEntry = savedDiaryEntry;
                    NotificationService.showDefaultSuccess();

                }, function (err) {
                    return $q.reject(err);
                });
            }, function (err) {
                return $q.reject(err);
            });

        }, function (err) {
            ClubHuntingActiveEntry.clearSelectedItem();

            var errorsToIgnore = ['cancel', 'escape', 'delete', 'back'];

            if (!angular.isString(err) || errorsToIgnore.indexOf(err) < 0) {
                NotificationService.showDefaultFailure();
            }

            return $q.reject(err);

        }).finally(function () {
            $state.go('club.hunting');
        });
    })

    .component('speciesSelect', {
        templateUrl: 'club/hunting/add/select-species.html',
        bindings: {
            diaryParameters: '<',
            diaryEntry: '<',
            availableSpecies: '<',
            readOnly: '<'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.availableSpecies = $ctrl.availableSpecies || $ctrl.diaryParameters.species;
                $ctrl.getCategoryName = $ctrl.diaryParameters.$getCategoryName;
                $ctrl.getGameName = $ctrl.diaryParameters.$getGameName;
                $ctrl.readOnly = !!$ctrl.readOnly;
            };

            $ctrl.isReadOnly = function () {
                return !!$ctrl.readOnly || $ctrl.diaryEntry.isHarvest() || !$ctrl.diaryEntry.canEdit;
            };
        }
    })

    .component('clubPersonSelect', {
        templateUrl: 'club/hunting/add/select-person.html',
        bindings: {
            memberList: '<',
            person: '=',
            readOnly: '<'
        },
        controller: function (PersonSearchService) {
            var $ctrl = this;

            $ctrl.selectedMember = null;
            $ctrl.searchHunterNumber = null;
            $ctrl.searchResultNotFound = false;

            function isValidPerson(person) {
                return person && (person.id || person.hunterNumber);
            }

            $ctrl.$onInit = function () {
                $ctrl.readOnly = !!$ctrl.readOnly;

                if (isValidPerson($ctrl.person)) {
                    $ctrl.searchHunterNumber = $ctrl.person.hunterNumber;

                    $ctrl.selectedMember = _.find($ctrl.memberList, function (m) {
                        var p1 = $ctrl.person;
                        var p2 = m.person;

                        return p2.id === p1.id || p2.hunterNumber === p1.hunterNumber;
                    });
                }
            };

            $ctrl.formatPersonName = function (person) {
                return person ? person.lastName + ', ' + person.byName : '';
            };

            $ctrl.isHunterSet = function () {
                return isValidPerson($ctrl.person);
            };

            $ctrl.onMemberSelected = function () {
                $ctrl.searchHunterNumber = null;
                $ctrl.searchResultNotFound = false;

                if ($ctrl.selectedMember) {
                    var person = $ctrl.selectedMember.person;

                    $ctrl.searchHunterNumber = person.hunterNumber;
                    $ctrl.person = _.pick(person, ['id', 'hunterNumber']);
                } else {
                    $ctrl.person = null;
                }
            };

            $ctrl.onHunterNumberSearch = function () {
                $ctrl.person = null;

                if (!$ctrl.searchHunterNumber) {
                    $ctrl.searchResultNotFound = false;
                    return;
                }

                PersonSearchService.findByHunterNumber($ctrl.searchHunterNumber)
                    .then(function (response) {
                        var person = response.data;

                        if (isValidPerson(person)) {
                            $ctrl.searchResultNotFound = false;
                            $ctrl.person = person;
                        } else {
                            $ctrl.searchResultNotFound = true;
                        }
                    }, function () {
                        $ctrl.searchResultNotFound = true;
                    });
            };
        }
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

    .component('nonMooseDateTimeSelect', {
        require: {
            parentForm: '^form'
        },
        bindings: {
            diaryEntry: '<',
            speciesAmount: '<'
        },
        templateUrl: 'club/hunting/add/select-date-and-time.html',
        controller: function ($scope, HarvestPermitSpeciesAmountService, Helpers) {
            var $ctrl = this;
            $ctrl.nonMooseDate = null;
            $ctrl.time = null;

            $ctrl.$onInit = function () {
                if ($ctrl.diaryEntry.pointOfTime) {
                    var t = moment($ctrl.diaryEntry.pointOfTime, 'YYYY-MM-DD[T]HH:mm');
                    $ctrl.time = Helpers.dateToString(t, 'HH:mm');
                    $ctrl.nonMooseDate = Helpers.dateToString(t, 'YYYY-MM-DD');
                }
            };

            $ctrl.isPermitValidOnDate = function () {
                var isValid = !$ctrl.nonMooseDate || HarvestPermitSpeciesAmountService.isValidDateForSpeciesAmount(
                    $ctrl.speciesAmount, $ctrl.nonMooseDate);
                $ctrl.parentForm.$setValidity('nonMooseDate', isValid);
                return isValid;
            };

            $scope.$watchGroup(['$ctrl.nonMooseDate', '$ctrl.time'], function (newValues) {
                var nonMooseDate = newValues[0];
                var time = newValues[1];

                if (nonMooseDate && time) {
                    $ctrl.diaryEntry.setDateAndTime(nonMooseDate, time);
                }
            });
        }
    })

    .component('huntingDayTimeSelect', {
        require: {
            parentForm: '^form'
        },
        bindings: {
            huntingDays: '<',
            diaryEntry: '<',
            huntingFinished: '<',
            create: '&',
            readOnly: '<'
        },
        templateUrl: 'club/hunting/add/select-day-and-time.html',
        controller: function ($scope, Helpers) {
            var $ctrl = this;

            $ctrl.huntingDay = null;
            $ctrl.time = null;
            $ctrl.nextDayHunting = false;

            function parseDay(str) {
                return moment(str, 'YYYY-MM-DD');
            }

            $ctrl.$onInit = function () {
                $ctrl.readOnly = !!$ctrl.readOnly;

                if ($ctrl.diaryEntry.pointOfTime) {
                    var t = moment($ctrl.diaryEntry.pointOfTime, 'YYYY-MM-DD[T]HH:mm');

                    $ctrl.time = Helpers.dateToString(t, 'HH:mm');
                    var entryDay = Helpers.dateToString(t, 'YYYY-MM-DD');

                    if ($ctrl.diaryEntry.huntingDayId) {
                        $ctrl.huntingDay = _.find($ctrl.huntingDays, function (d) {
                            return d.id === $ctrl.diaryEntry.huntingDayId;
                        });

                        if ($ctrl.huntingDay && entryDay === $ctrl.huntingDay.endDate) {
                            $ctrl.nextDayHunting = true;
                        }
                    } else {
                        $ctrl.huntingDay = _.find($ctrl.huntingDays, function (d) {
                            return d.startDate === entryDay;
                        });
                    }
                }
            };

            $ctrl.getHuntingDayName = function (huntingDay) {
                var day = parseDay(huntingDay.startDate);
                var suffix = (huntingDay.startDate === huntingDay.endDate) ? '' : ' ( +1 )';
                return Helpers.dateToString(day, 'D.M.YYYY') + suffix;
            };

            $ctrl.addHuntingDay = function () {
                $ctrl.create().then(function (huntingDay) {
                    $ctrl.huntingDays.push(huntingDay);
                    $ctrl.huntingDay = huntingDay;
                });
            };

            $ctrl.showNextDayHunting = function () {
                var h = $ctrl.huntingDay;
                return h && h.startDate && h.startDate !== h.endDate;
            };

            $scope.$watchGroup(['$ctrl.huntingDay', '$ctrl.time', '$ctrl.nextDayHunting'], function (newValues) {
                var huntingDay = newValues[0];
                var time = newValues[1];
                var nextDayHunting = newValues[2];

                if (huntingDay && time) {
                    $ctrl.diaryEntry.huntingDayId = huntingDay.id;

                    if (huntingDay.startDate !== huntingDay.endDate && nextDayHunting) {
                        var nextDay = parseDay(huntingDay.startDate).add(1, 'day');
                        $ctrl.diaryEntry.setDateAndTime(nextDay, time);
                    } else {
                        $ctrl.diaryEntry.setDateAndTime(huntingDay.startDate, time);
                    }
                } else {
                    $ctrl.diaryEntry.huntingDayId = null;
                    $ctrl.diaryEntry.pointOfTime = null;
                }
            });
        }
    })

    .controller('ClubHarvestFormController', function ($filter, $scope, ActiveRoleService, ClubHuntingHarvestFields,
                                                       HarvestFieldsService, ModeratorPrivileges,
                                                       createHuntingDayForEntry, entry, fields, groupId, huntingDays,
                                                       huntingFinished, memberCandidates, parameters,
                                                       permitSpeciesAmount) {
        $scope.entry = entry;
        $scope.fields = fields;

        $scope.isModerator = ActiveRoleService.isModerator();
        $scope.canSaveWithIncompleteData =
            ActiveRoleService.isPrivilegedModerator(ModeratorPrivileges.saveHarvestWithIncompleteData);
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
        $scope.saveWithLegalFieldsOnly = false;

        $scope.isValid = function () {
            return $scope.entry.gameSpeciesCode &&
                $scope.entry.geoLocation.latitude &&
                $scope.entry.actorInfo && $scope.entry.actorInfo.hunterNumber &&
                isHuntingDayInfoValid() &&
                $scope.entry.pointOfTime &&
                !$scope.isWeightInfoInadequate();
        };

        function isHuntingDayInfoValid() {
            return ($scope.saveWithLegalFieldsOnly || $scope.entry.isPermitBasedDeer() || $scope.entry.huntingDayId);
        }

        $scope.save = function () {
            HarvestFieldsService.fixStateBeforeSaving($scope.fields, $scope.entry);

            $scope.$close({
                entry: $scope.entry,
                createDay: $scope.saveWithLegalFieldsOnly || $scope.entry.isPermitBasedDeer()
            });
        };

        $scope.cancel = function () {
            $scope.$dismiss('cancel');
        };

        function updateFieldRequirements () {
            function updateRequiredFields (requiredFields) {
                $scope.fields = requiredFields;
            }

            if ($scope.saveWithLegalFieldsOnly) {
                ClubHuntingHarvestFields.getLegallyMandatory(groupId).then(updateRequiredFields);
            } else {
                ClubHuntingHarvestFields.get(groupId).then(updateRequiredFields);
            }
        }

        $scope.toggleLegalFieldsOnly = function () {
            $scope.saveWithLegalFieldsOnly = !$scope.saveWithLegalFieldsOnly;
            updateFieldRequirements();
        };

        $scope.isReportFieldVisible = function (fieldName) {
            return !!$scope.fields && $scope.fields.isVisibleReportField(fieldName);
        };

        $scope.isSpecimenFieldVisible = function (fieldName) {
            var specimen = $scope.entry.specimens[0];
            return !!$scope.fields && $scope.fields.isVisibleSpecimenField(fieldName, specimen);
        };

        $scope.isSpecimenFieldRequired = function (fieldName) {
            var specimen = $scope.entry.specimens[0];
            return !!$scope.fields && $scope.fields.isRequiredSpecimenField(fieldName, specimen);
        };

        $scope.dateSelectionType = function () {
            if ($scope.saveWithLegalFieldsOnly || !$scope.mooseGroupSelected) {
                return 'BASIC';
            }
            return 'MOOSE_DAY';
        };

        $scope.isWeightInfoInadequate = function () {
            return !$scope.saveWithLegalFieldsOnly &&
                $scope.mooseGroupSelected &&
                !$scope.entry.specimens[0].weightMeasured &&
                !$scope.entry.specimens[0].weightEstimated;
        };

    })

    .controller('ClubObservationFormController', function ($scope, DiaryEntrySpecimenFormService, MapUtil,
                                                           ObservationFieldsMetadata, availableSpecies,
                                                           createHuntingDayForEntry, entry,
                                                           fieldMetadataForObservationSpecies, huntingDays,
                                                           huntingFinished, isModerator, memberCandidates, parameters) {
        $scope.entry = entry;
        $scope.isModerator = isModerator;
        $scope.parameters = parameters;
        $scope.availableSpecies = availableSpecies;
        $scope.huntingDays = huntingDays;
        $scope.memberCandidates = memberCandidates;
        $scope.maxSpecimenCount = DiaryEntrySpecimenFormService.getMaxSpecimenCountForObservation();
        $scope.fieldMetadata = fieldMetadataForObservationSpecies;
        $scope.fieldRequirements = null;
        $scope.createHuntingDayForObservation = createHuntingDayForEntry;
        $scope.huntingFinished = huntingFinished;
        $scope.readOnly = !!entry.updateableOnlyByCarnivoreAuthority;

        var isAlreadyExistingAndNotYetLinked = entry.id && !entry.huntingDayId;
        $scope.showAccept = !huntingFinished && isAlreadyExistingAndNotYetLinked;
        $scope.showSave = !huntingFinished && !isAlreadyExistingAndNotYetLinked;

        $scope.getAvailableObservationTypes = function () {
            return $scope.fieldMetadata
                ? $scope.fieldMetadata.getAvailableObservationTypes($scope.entry.observationCategory)
                : [];
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

        $scope.isValid = function () {
            return entry.gameSpeciesCode &&
                entry.observationType &&
                MapUtil.isValidGeoLocation(entry.geoLocation) &&
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

            DiaryEntrySpecimenFormService.editSpecimen($scope.entry, parameters, availableFields, $scope.fieldRequirements);
        };

        $scope.save = function () {
            $scope.$close({entry: $scope.entry, createDay: false});
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
            return $scope.readOnly || $scope.entry.totalSpecimenAmount > DiaryEntrySpecimenFormService.MAX_VISIBLE_AMOUNT;
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
                $scope.fieldRequirements = newValue.getFieldRequirements(
                    $scope.entry.observationCategory,
                    $scope.entry.observationType);
                $scope.fieldMetadata.resetIllegalObservationFields($scope.entry);
            } else {
                $scope.fieldRequirements = null;
            }
        });

        $scope.$watch('entry.observationType', function (newValue, oldValue) {
            if (newValue !== oldValue) {
                if ($scope.fieldMetadata) {
                    $scope.fieldRequirements = $scope.fieldMetadata.getFieldRequirements(
                        $scope.entry.observationCategory,
                        $scope.entry.observationType);
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
