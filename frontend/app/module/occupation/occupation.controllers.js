'use strict';

angular.module('app.occupation.controllers', [])
    .controller('OccupationListController',
        function ($scope, $uibModal,
                  NotificationService, FormPostService, CallOrderConfig,
                  Occupations, OccupationPermissionService,
                  OccupationsFilterSorterService, OccupationDialogService,
                  orgId, onlyBoard, allOccupations, occupationTypes) {
            var $ctrl = this;

            $ctrl.orgId = orgId;
            $ctrl.callOrderConfig = CallOrderConfig;
            $ctrl.allOccupations = allOccupations;
            $ctrl.occupations = [];
            $ctrl.board = [];
            $ctrl.tenses = {
                current: OccupationsFilterSorterService.current,
                past: OccupationsFilterSorterService.past,
                future: OccupationsFilterSorterService.future
            };
            $ctrl.occupationTypes = occupationTypes.all;
            $ctrl.occupationTypeFilter = null;

            $ctrl.canModify = OccupationPermissionService.canModify;

            $ctrl.isPersonRegistered = function (item) {
                return item && item.person && !!item.person.registered;
            };

            $ctrl.onOccupationTypeChange = function() {
                $ctrl.showTense($ctrl.selectedTense);
            };

            $ctrl.showTense = function (tenseFn) {
                $ctrl.selectedTense = tenseFn;
                var v = tenseFn(occupationTypes.board, $ctrl.allOccupations, $ctrl.occupationTypeFilter);

                $ctrl.occupations = v.occupations;
                $ctrl.board = v.board;
                $ctrl.showOccupations = v.occupations !== null && v.occupations.length !== 0;
                $ctrl.showBoard = v.board !== null && v.board.length !== 0;
            };

            var updateCurrentOccupations = function() {
                var currentOccupations = $ctrl.tenses.current(occupationTypes.board, $ctrl.allOccupations);
                $ctrl.currentBoardRoles = _(currentOccupations.board).filter(function (occ) {
                    return !!occ.boardRepresentation;
                }).map(function (occ) {
                    return occ.boardRepresentation;
                }).value();
            };

            // show current as default
            $ctrl.showTense($ctrl.tenses.current);
            updateCurrentOccupations();

            function onSuccess() {
                NotificationService.showDefaultSuccess();

                Occupations.query({orgId: orgId}).$promise.then(function (data) {
                    $ctrl.allOccupations = data;
                    $ctrl.showTense($ctrl.selectedTense);
                    updateCurrentOccupations();
                });
            }

            function onFailure(reason) {
                if (reason === 'error') {
                    NotificationService.showDefaultFailure();
                }
            }

            $ctrl.addOccupation = function () {
                OccupationDialogService.addOccupation(orgId, occupationTypes, onlyBoard, $ctrl.currentBoardRoles)
                    .then(onSuccess, onFailure);
            };

            $ctrl.showSelected = function (selected) {
                OccupationDialogService.showSelected(selected, orgId, occupationTypes, onlyBoard, $ctrl.currentBoardRoles)
                    .then(onSuccess, onFailure);
            };

            $ctrl.exportToExcel = function (params) {
                FormPostService.submitFormUsingBlankTarget('/api/v1/organisation/excel/occupations', {
                    'orgId': $ctrl.orgId,
                    'occupationType': $ctrl.occupationTypeFilter
                });
            };
        })

    .controller('OccupationFormController',
        function ($scope, $uibModalInstance,
                  Helpers, CallOrderConfig, TranslatedBlockUI,
                  Occupations, OccupationFindPerson,
                  orgId, occupation, occupationTypes, existingPersons, boardTypes, currentBoardRoles,
                  boardRepresentationRoles, organisation) {

            $scope.existingPersons = existingPersons;
            $scope.personDisplayName = function (person) {
                if (person.address && person.address.city) {
                    return person.lastName + ' ' + person.firstName + ', ' + person.address.city;
                }
                return person.lastName + ' ' + person.firstName;
            };
            $scope.editPersonInformation = false;
            $scope.editAddress = false;
            $scope.canEditAddress = canEditAddress(occupation.person);
            $scope.callOrderConfig = CallOrderConfig;
            $scope.organisation = organisation;
            $scope.occupation = occupation;
            $scope.occupationTypes = occupationTypes;
            $scope.boardTypes = boardTypes;
            $scope.boardRepresentationRoles = boardRepresentationRoles;
            $scope.substitute = {};
            if (occupation.substitute) {
                $scope.substitute.person = occupation.substitute;
            }

            $scope.currentBoardRoles = _.filter(currentBoardRoles, function (role) {
                return role !== $scope.occupation.boardRepresentation;
            });

            $scope.isBoardRoleDisabled = function (o) {
                if ($scope.organisation.organisationType !== 'RHY') {
                    return false;
                }

                return $scope.currentBoardRoles.indexOf(o) !== -1;
            };

            $scope.searchPerson = {
                error: false,
                ssn: occupation && occupation.person ? occupation.person.ssn : null,
                hunterNumber: occupation && occupation.person ? occupation.person.hunterNumber : null
            };

            $scope.searchSubstitute = {
                error: false,
                ssn: $scope.substitute ? $scope.substitute.ssn : null,
                hunterNumber: $scope.substitute ? $scope.substitute.hunterNumber : null
            };

            $scope.addPersonBy = {value: 'existing'};
            $scope.$watch('addPersonBy.value', function (newVal, oldVal) {
                if (newVal !== oldVal) {
                    $scope.occupation.person = null;
                    $scope.searchPerson.error = false;
                }
            });

            $scope.addSubstituteBy = {value: 'existing'};
            $scope.onAddSubstituteChanged = function () {
                $scope.substitute.person = null;
                $scope.searchSubstitute.error = false;
            };

            $scope.isBoardType = function () {
                if ($scope.boardTypes) {
                    return $scope.boardTypes.indexOf($scope.occupation.occupationType) !== -1;
                }

                return false;
            };

            $scope.isSubstituteRequired = function () {
                return $scope.isBoardType() && $scope.occupation.occupationType !== 'HALLITUKSEN_VARAJASEN'
                    || $scope.occupation.occupationType === 'ALUEKOKOUKSEN_EDUSTAJA';
            };

            function canEditAddress(person) {
                return person && !person.registered && (!person.address || person.address.editable);
            }

            function decorateSearch(searchMethod, personData, searchParams) {
                var ok = function (response) {
                    personData.person = response.data;
                    searchParams.error = false;
                };

                var nok = function () {
                    personData.person = null;
                    searchParams.error = true;
                };

                var done = function () {
                    TranslatedBlockUI.stop();
                };

                // prevent double clicking
                return _.debounce(function (search) {
                    TranslatedBlockUI.start("global.block.wait");

                    return searchMethod(search)
                        .then(ok, nok).finally(done);
                }, 500);
            }

            $scope.findPersonBySsn = decorateSearch(function () {
                return OccupationFindPerson.findBySsn($scope.searchPerson.ssn);
            }, $scope.occupation, $scope.searchPerson);

            $scope.findPersonByHunterNumber = decorateSearch(function () {
                return OccupationFindPerson.findByHunterNumber($scope.searchPerson.hunterNumber);
            }, $scope.occupation, $scope.searchPerson);

            $scope.findSubstituteBySsn = decorateSearch(function () {
                return OccupationFindPerson.findBySsn($scope.searchSubstitute.ssn);
            }, $scope.substitute, $scope.searchSubstitute);

            $scope.findSubstituteByHunterNumber = decorateSearch(function () {
                return OccupationFindPerson.findByHunterNumber($scope.searchSubstitute.hunterNumber);
            }, $scope.substitute, $scope.searchSubstitute);

            $scope.startEditPersonInformation = function () {
                $scope.editPersonInformation = true;
            };

            $scope.startEditAddress = function () {
                $scope.editAddress = true;
            };

            var checkIsPhoneNumberRequired = function () {
                $scope.editPersonInformation = false;
                $scope.phoneNumberRequired = occupation.person && !occupation.person.phoneNumber &&
                    CallOrderConfig.isCallOrderType(occupation.occupationType);
                if ($scope.phoneNumberRequired) {
                    $scope.editPersonInformation = true;
                }
            };
            $scope.$watch('occupation.occupationType', function () {
                checkIsPhoneNumberRequired();
            });
            $scope.$watch('occupation.person', function () {
                checkIsPhoneNumberRequired();
                $scope.canEditAddress = canEditAddress(occupation.person);
            });
            $scope.isValid = function (occupation, editEmailAndPhone, phoneNumberInvalid) {
                if (editEmailAndPhone && phoneNumberInvalid) {
                    return false;
                }
                var exists = function (value) {
                    return !_.isNull(value) && !_.isUndefined(value);
                };

                var isSubstituteForBoardMember = function () {
                    if ($scope.isSubstituteRequired()) {
                        return exists($scope.substitute.person);
                    }

                    return true;
                };

                if (CallOrderConfig.isCallOrderType(occupation.occupationType)) {
                    if (_.indexOf(CallOrderConfig.callOrderValues, occupation.callOrder) === -1) {
                        return false;
                    }
                    if (exists(occupation.person) && !exists(occupation.person.phoneNumber)) {
                        return false;
                    }
                }
                return exists(occupation.occupationType) && exists(occupation.person) && isSubstituteForBoardMember();
            };
            $scope.save = function () {
                var newOccupation = angular.copy(occupation);// prevent ui showing updated object properties on save
                newOccupation.beginDate = Helpers.dateToString(occupation.beginDate);
                newOccupation.endDate = Helpers.dateToString(occupation.endDate);

                if (!$scope.editAddress) {
                    // address is not needed, but it might have invalid data
                    delete newOccupation.person.address;
                }

                if ($scope.isSubstituteRequired() && $scope.substitute.person) {
                    newOccupation.substitute = $scope.substitute.person;
                }

                var saveMethod = !newOccupation.id ? Occupations.save : Occupations.update;

                saveMethod({orgId: orgId}, newOccupation).$promise
                    .then(function () {
                        $uibModalInstance.close();
                    }, function () {
                        $uibModalInstance.dismiss('error');
                    });
            };
            $scope.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        });
