'use strict';

angular.module('app.harvestpermit.application.carnivore.species', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.carnivore.species', {
                url: '/species',
                templateUrl: 'harvestpermit/applications/carnivore/species/species.html',
                controller: 'CarnicorePermitWizardSpeciesController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesAmount: function (CarnivorePermitApplication, applicationId) {
                        return CarnivorePermitApplication.listSpeciesAmounts({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'applicant',
                            next: 'justification'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.carnivore.species', {
                url: '/species',
                templateUrl: 'harvestpermit/applications/carnivore/species/species.html',
                controller: 'CarnicorePermitWizardSpeciesController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesAmount: function (CarnivorePermitApplication, applicationId) {
                        return CarnivorePermitApplication.listSpeciesAmounts({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'applicant',
                            next: 'justification'
                        };
                    }
                }
            });
    })

    .controller('CarnicorePermitWizardSpeciesController', function ($scope, CarnivorePermitApplication,
                                                                    UnsavedChangesConfirmationService,
                                                                    ApplicationWizardNavigationHelper,
                                                                    CarnivoreEditPeriodModal,
                                                                    states, wizard, applicationId, speciesAmount) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.spa = speciesAmount;
            $scope.$watch('carnivoreSpeciesAmountForm.$pristine', function (newVal, oldVal) {
                if (oldVal && !newVal) {
                    UnsavedChangesConfirmationService.setChanges(true);
                }
            }, true);
        };

        $ctrl.editPeriod = function () {
            CarnivoreEditPeriodModal.open(angular.copy($ctrl.spa)).then(function (spa) {
                $ctrl.spa = spa;
            });
        };

        $ctrl.exit = function (form) {
            ApplicationWizardNavigationHelper.previous(invalid(form), $ctrl.save, wizard.exit);
        };

        $ctrl.previous = function (form) {
            ApplicationWizardNavigationHelper.previous(invalid(form), $ctrl.save, $ctrl.doGotoPrevious);
        };

        $ctrl.doGotoPrevious = function () {
            doGoto(states.previous);
        };

        function doGoto(state) {
            wizard.goto(state);
        }

        $ctrl.next = function () {
            $ctrl.save().then(function () {
                doGoto(states.next);
            });
        };

        $ctrl.nextDisabled = function (form) {
            return invalid(form);
        };

        $ctrl.save = function () {
            UnsavedChangesConfirmationService.setChanges(false);
            return CarnivorePermitApplication.saveSpeciesAmounts({id: applicationId}, $ctrl.spa).$promise;
        };

        function invalid(form) {
            return form.$invalid;
        }
    })
    .service('CarnivoreEditPeriodModal', function ($uibModal) {
        this.open = function (spa) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/applications/carnivore/species/modify-period-modal.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'lg',
                resolve: {
                    spa: spa
                }
            }).result;
        };

        function ModalController($uibModalInstance, $translate, $filter,
                                 spa) {
            var $ctrl = this;
            var i18n = $filter('rI18nNameFilter');

            $ctrl.$onInit = function () {
                $ctrl.spa = spa;
            };

            $ctrl.close = function () {
                $uibModalInstance.close($ctrl.spa);
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };

        }
    });
