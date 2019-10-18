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

            // show current as default
            $ctrl.showTense($ctrl.tenses.current);

            function onSuccess() {
                NotificationService.showDefaultSuccess();

                Occupations.query({orgId: orgId}).$promise.then(function (data) {
                    $ctrl.allOccupations = data;
                    $ctrl.showTense($ctrl.selectedTense);
                });
            }

            function onFailure(reason) {
                if (reason === 'error') {
                    NotificationService.showDefaultFailure();
                }
            }

            $ctrl.addOccupation = function () {
                OccupationDialogService.addOccupation(orgId, occupationTypes, onlyBoard)
                    .then(onSuccess, onFailure);
            };

            $ctrl.showSelected = function (selected) {
                OccupationDialogService.showSelected(selected, orgId, occupationTypes, onlyBoard)
                    .then(onSuccess, onFailure);
            };

            $ctrl.removeSelected = function (selected) {
                OccupationDialogService.removeSelected(selected, orgId).then(onSuccess, onFailure);
            };

            $ctrl.exportToExcel = function (params) {
                FormPostService.submitFormUsingBlankTarget('/api/v1/organisation/excel/occupations', {
                    'orgId': $ctrl.orgId
                });
            };
        })

    .controller('OccupationFormController',
        function ($scope, $uibModalInstance,
                  Helpers, CallOrderConfig, TranslatedBlockUI,
                  Occupations, OccupationFindPerson,
                  orgId, occupation, occupationTypes, existingPersons) {

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
            $scope.occupation = occupation;
            $scope.occupationTypes = occupationTypes;

            $scope.searchPerson = {
                error: false,
                ssn: occupation && occupation.person ? occupation.person.ssn : null,
                hunterNumber: occupation && occupation.person ? occupation.person.hunterNumber : null
            };

            $scope.addPersonBy = {value: 'existing'};
            $scope.$watch('addPersonBy.value', function (newVal, oldVal) {
                if (newVal !== oldVal) {
                    $scope.occupation.person = null;
                    $scope.searchPerson.error = false;
                }
            });

            function canEditAddress(person) {
                return person && !person.registered && (!person.address || person.address.editable);
            }

            function decorateSearch(searchMethod) {
                var ok = function (response) {
                    $scope.occupation.person = response.data;
                    $scope.searchPerson.error = false;
                };

                var nok = function () {
                    $scope.occupation.person = null;
                    $scope.searchPerson.error = true;
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
            });

            $scope.findPersonByHunterNumber = decorateSearch(function () {
                return OccupationFindPerson.findByHunterNumber($scope.searchPerson.hunterNumber);
            });

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
                if (CallOrderConfig.isCallOrderType(occupation.occupationType)) {
                    if (_.indexOf(CallOrderConfig.callOrderValues, occupation.callOrder) === -1) {
                        return false;
                    }
                    if (exists(occupation.person) && !exists(occupation.person.phoneNumber)) {
                        return false;
                    }
                }
                return exists(occupation.occupationType) && exists(occupation.person);
            };
            $scope.save = function () {
                var newOccupation = angular.copy(occupation);// prevent ui showing updated object properties on save
                newOccupation.beginDate = Helpers.dateToString(occupation.beginDate);
                newOccupation.endDate = Helpers.dateToString(occupation.endDate);

                if (!$scope.editAddress) {
                    // address is not needed, but it might have invalid data
                    delete newOccupation.person.address;
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
        })
    .controller('OccupationRemoveController',
        function ($scope, $uibModalInstance, Occupations, CallOrderConfig, orgId, occupation) {
            $scope.callOrderConfig = CallOrderConfig;
            $scope.occupation = occupation;
            $scope.remove = function () {
                if (occupation.id) {
                    Occupations.delete({orgId: orgId}, occupation).$promise
                        .then(function () {
                            $uibModalInstance.close();
                        }, function () {
                            $uibModalInstance.dismiss('error');
                        });

                } else {
                    $uibModalInstance.dismiss('error');
                }
            };
            $scope.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        });
