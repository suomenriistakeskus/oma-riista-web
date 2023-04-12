'use strict';

angular.module('app.rhy.taxation', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('rhy.taxation', {
                url: '/taxation',
                templateUrl: 'rhy/taxation/layout.html',
                controller: 'TaxationController',
                controllerAs: '$ctrl',
            });

    })
    .service('TaxationService', function ($q, GameSpeciesCodes, Rhys, AttachmentService,
                                          FetchAndSaveBlob) {
        var self = this;

        self.downloadAttachment = function (attachmentId) {
            return FetchAndSaveBlob.get('/api/v1/riistanhoitoyhdistys/taxation/attachment/' + attachmentId);
        };

        self.deleteAttachment = function (attachmentId) {
            return Rhys.deleteTaxationAttachment({id: attachmentId}).$promise;
        };
    })
    .controller('TaxationController', function ($scope, $stateParams, $uibModal, $filter, HuntingYearService,
                                                TaxationService, Species, Helpers, TranslatedSpecies, Rhys,
                                                NotificationService, ActiveRoleService, RoleService,
                                                FetchAndSaveBlob) {
        var $ctrl = this;

        $ctrl.getRoleDisplayName = RoleService.getRoleDisplayName;
        $ctrl.data = {};
        $ctrl.selectedYear = null;
        $ctrl.selectedSpecies = null;
        $ctrl.selectedMooseArea = null;
        $ctrl.availableSpecies = null;
        $ctrl.rhy = {};
        $ctrl.decoratedYears = [];
        $ctrl.mooseAreas = {};
        $ctrl.isAdminOrModerator = false;
        $ctrl.isCoordinator = false;

        $ctrl.downloadAttachment = TaxationService.downloadAttachment;

        $ctrl.editTaxation = function () {
            $uibModal.open({
                templateUrl: 'rhy/taxation/form.html',
                resolve: {
                    data: _.clone($ctrl.data),
                    selectedYear: $ctrl.selectedYear,
                    selectedSpecies: $ctrl.selectedSpecies,
                    selectedMooseArea: $ctrl.selectedMooseArea,
                    rhy: $ctrl.rhy
                },
                controller: 'TaxationFormController',
                controllerAs: '$ctrl',
                size: 'lg'
            }).result.then($ctrl.onSuccess, $ctrl.onFailure);
        };

        $ctrl.onSuccess = function () {
            NotificationService.showDefaultSuccess();
            $ctrl.onSpeciesChange();
        };

        $ctrl.onFailure = function (reason) {
            $ctrl.onSpeciesChange();
            if (reason === 'error') {
                NotificationService.showDefaultFailure();
            }
        };

        $ctrl.$onInit = function () {
            $ctrl.getRhy();
            $ctrl.availableSpecies = getSpeciesList();
            getMooseAreas();
            getHuntingYears();

            $ctrl.selectedYear = $stateParams.year;
            $ctrl.isAdminOrModerator = ActiveRoleService.isModerator();
            $ctrl.isCoordinator = ActiveRoleService.isCoordinator();
        };

        $ctrl.getRhy = function () {
            var i18n = $filter('rI18nNameFilter');
            Rhys.getPublicInfo({id: $stateParams.id}).$promise.then(function (data) {
                if (data === undefined || data.id === undefined) {
                    $ctrl.rhy = {
                        id: $stateParams.id,
                        name: ""
                    };
                } else {
                    $ctrl.rhy = data;
                    $ctrl.rhy.name = i18n(data);
                }
            });
        };

        $ctrl.isDraft = function () {
            return $ctrl.data.state === 'DRAFT';
        };

        $ctrl.clearData = function () {
            $ctrl.data = {};
        };

        $ctrl.showNewTaxationView = function () {
            return $ctrl.selectedSpecies && !$ctrl.data.id;
        };

        $ctrl.showFilledTaxationView = function () {
            return $ctrl.selectedSpecies && $ctrl.data.id;
        };

        $ctrl.onMooseAreaChange = function () {
            $ctrl.selectedYear = null;
            $ctrl.selectedSpecies = null;
            $ctrl.clearData();
        };

        $ctrl.onHuntingYearChange = function () {
            $ctrl.selectedSpecies = null;
            $ctrl.clearData();
        };

        $ctrl.onSpeciesChange = function () {
            $ctrl.clearData();
            if ($ctrl.selectedSpecies === null) {
                return;
            }
            if ($ctrl.selectedMooseArea === null) {
                return;
            }

            Rhys.getTaxationReport(
                {
                    year: $ctrl.selectedYear.year,
                    speciesCode: $ctrl.selectedSpecies.code,
                    rhyId: $stateParams.id,
                    htaId: $ctrl.selectedMooseArea.id
                }
            ).$promise.then(function (data) {
                if (data === undefined || data.id === undefined) {
                    $ctrl.data = {
                        hasTaxationPlanning: true
                    };
                } else {
                    $ctrl.data = data;
                }
            });
        };

        $ctrl.exportExcel = function (exportType) {
            var params = {
                huntingYear: $ctrl.selectedYear.year,
                gameSpeciesCode: $ctrl.selectedSpecies.code
            };
            if (exportType === 'rhy') {
                params.rhyId = $ctrl.rhy.id;
            } else if (!$ctrl.isAdminOrModerator && !$ctrl.isCoordinator) {
                return;
            }

            FetchAndSaveBlob.post('/api/v1/riistanhoitoyhdistys/taxation/excel', params);
        };

        $ctrl.canEdit = function () {
            if (!$ctrl.selectedYear || !$ctrl.selectedSpecies) {
                return false;
            }

            var today = Helpers.toMoment(new Date());
            var thisYear = today.year();
            var lastFillingDateOfTheYear = Helpers.toMoment(new Date(thisYear, 3, 30));

            return $ctrl.isAdminOrModerator ||
                (($ctrl.data.id === undefined || $ctrl.data.state === "DRAFT") &&
                    $ctrl.selectedYear.year >= thisYear &&
                    today.isSameOrBefore(lastFillingDateOfTheYear));
        };

        function getMooseAreas() {
            var i18n = $filter('rI18nNameFilter');
            Rhys.getMooseAreas(
                {
                    rhyId: $stateParams.id
                }
            ).$promise.then(function (data) {
                $ctrl.mooseAreas = [];
                if (data === undefined) {
                    data = {};
                }

                Object.keys(data).forEach(function (key) {
                    var id = parseInt(key);
                    if (id) {
                        $ctrl.mooseAreas.push(
                            {
                                id: id,
                                name: i18n(data[key])
                            }
                        );
                    }
                });
            });
        }

        function getHuntingYears() {
            Rhys.getTaxationReportYears(
                {
                    rhyId: $stateParams.id
                }
            ).$promise.then(function (data) {
                if (data === undefined) {
                    data = [];
                }
                var thisYear = Helpers.toMoment(new Date()).year();
                if (!data.includes(thisYear)) {
                    data.push(thisYear);
                }

                $ctrl.decoratedYears = _(data)
                    .map(function (year) {
                        var obj = {
                            year: year
                        };
                        if (_.isFinite(obj.year)) {
                            obj.name = HuntingYearService.toObj(obj.year).name;
                        } else {
                            obj.name = obj.year;
                        }

                        return obj;
                    })
                    .value();
            });
        }

        function getSpeciesList() {
            return _.chain(Species.getPermitBasedMooselike())
                .map(function (species) {
                    return TranslatedSpecies.translateSpecies(species);
                })
                .sortBy('name')
                .value();
        }
    })
    .controller('TaxationFormController', function ($scope, $stateParams, $uibModalInstance, data, selectedYear,
                                                    selectedSpecies, selectedMooseArea, rhy, Helpers,
                                                    Rhys, AttachmentService, TaxationService, NotificationService,
                                                    TranslatedBlockUI) {
        var $ctrl = this;

        $ctrl.data = {};
        $ctrl.attachments = {};
        $ctrl.selectedYear = null;
        $ctrl.selectedSpecies = null;
        $ctrl.selectedMooseArea = null;
        $ctrl.attachmentBaseUri = null;
        $ctrl.rhy = null;

        $ctrl.downloadAttachment = TaxationService.downloadAttachment;
        $ctrl.deleteAttachment = TaxationService.deleteAttachment;

        $ctrl.dateOptions = {
            maxDate: new Date()
        };

        $ctrl.$onInit = function () {
            $ctrl.data = data;
            $ctrl.selectedYear = selectedYear;
            $ctrl.selectedSpecies = selectedSpecies;
            $ctrl.selectedMooseArea = selectedMooseArea;
            $ctrl.rhy = rhy;
            $ctrl.attachmentBaseUri = '/api/v1/riistanhoitoyhdistys/taxation/withattachments';
        };


        $ctrl.arePercentagesValid = function () {
            return $ctrl.data.youngPercent >= 0 &&
                $ctrl.data.youngPercent <= 100 &&

                $ctrl.data.plannedUtilizationRateOfThePermits >= 0 &&
                $ctrl.data.plannedUtilizationRateOfThePermits <= 100 &&

                $ctrl.data.plannedCatchYoungPercent >= 0 &&
                $ctrl.data.plannedCatchYoungPercent <= 100 &&

                $ctrl.data.plannedCatchMalePercent >= 0 &&
                $ctrl.data.plannedCatchMalePercent <= 100 &&

                $ctrl.data.shareOfBankingPermits >= 0 &&
                $ctrl.data.shareOfBankingPermits <= 100;
        };

        $ctrl.areRangesValid = function () {
            return $ctrl.data.plannedPermitMin >= 0 &&
                $ctrl.data.plannedPermitMax >= 0 &&
                $ctrl.data.plannedPermitMin <= $ctrl.data.plannedPermitMax &&

                $ctrl.data.plannedCatchMin >= 0 &&
                $ctrl.data.plannedCatchMax >= 0 &&
                $ctrl.data.plannedCatchMin <= $ctrl.data.plannedCatchMax &&

                $ctrl.data.plannedPreyDensityMin >= 0 &&
                $ctrl.data.plannedPreyDensityMax >= 0 &&
                $ctrl.data.plannedPreyDensityMin <= $ctrl.data.plannedPreyDensityMax &&

                $ctrl.data.plannedPermitDensityMin >= 0 &&
                $ctrl.data.plannedPermitDensityMax >= 0 &&
                $ctrl.data.plannedPermitDensityMin <= $ctrl.data.plannedPermitDensityMax;

        };

        $ctrl.isStakeholdersConsultedValid = function () {
            if (!$ctrl.data.stakeholdersConsulted) {
                return true;
            }
            var momentDate = Helpers.toMoment($ctrl.data.stakeholdersConsulted, 'YYYY-MM-DD');
            return momentDate.isSameOrBefore(new Date(), 'day');
        };

        $scope.isValid = function (form) {
            if (form === undefined) {
                return;
            }
            if ($ctrl.data.hasTaxationPlanning) {
                // more validation
                return form.$valid && $ctrl.arePercentagesValid() && $ctrl.areRangesValid() && $ctrl.isStakeholdersConsultedValid();
            } else {
                return form.$valid && $ctrl.isStakeholdersConsultedValid();
            }
        };

        $ctrl.isGreaterThan = function (value, compareValue) {
            if (value === undefined || compareValue === undefined) {
                return false;
            }

            return value > compareValue;
        };

        $ctrl.isDisabled = function () {
            return !$ctrl.data.hasTaxationPlanning;
        };

        $ctrl.saveAsDraft = function () {
            TranslatedBlockUI.start("global.block.wait");
            $ctrl.saveReport('DRAFT').then(function (data) {
                TranslatedBlockUI.stop();
                $ctrl.data = data;
                $uibModalInstance.close();
            }, function () {
                TranslatedBlockUI.stop();
                showSavingFailureMessage();
                $uibModalInstance.dismiss('error');
            });
        };

        $ctrl.save = function () {
            TranslatedBlockUI.start("global.block.wait");
            $ctrl.saveReport('CONFIRMED').then(function (data) {
                TranslatedBlockUI.stop();
                $ctrl.data = data;
                $uibModalInstance.close();
            }, function () {
                TranslatedBlockUI.stop();
                showSavingFailureMessage();
                $uibModalInstance.dismiss('error');
            });
        };

        $ctrl.saveReport = function (state) {
            var params = $ctrl.data;

            params.huntingYear = $ctrl.selectedYear.year;
            params.gameSpeciesCode = $ctrl.selectedSpecies.code;
            params.htaId = $ctrl.selectedMooseArea.id;
            params.rhyId = $ctrl.rhy.id;
            params.state = state;

            if (AttachmentService.hasAttachments()) {
                return AttachmentService.sendAttachments(params);
            }

            return Rhys.saveOrUpdateTaxationReport({}, params).$promise;
        };

        $ctrl.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };

        function showSavingFailureMessage() {
            NotificationService.showMessage('rhy.taxationPlan.savingError', {ttl: -1});
        }
    });
