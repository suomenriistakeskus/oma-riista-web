'use strict';

angular.module('app.account.application', [])


    .service('AccountApplicationSummaryModal', function ($uibModal) {
        this.show = function (application) {
            var harvestPermitCategory = application.harvestPermitCategory;
            var isMooselikeApplication = harvestPermitCategory === 'MOOSELIKE';
            var isMooselikeAmendment = harvestPermitCategory === 'MOOSELIKE_NEW';
            var isBirdApplication = harvestPermitCategory === 'BIRD';
            var isCarnovireApplication = harvestPermitCategory === 'LARGE_CARNIVORE_BEAR' ||
                harvestPermitCategory === 'LARGE_CARNIVORE_LYNX' ||
                harvestPermitCategory === 'LARGE_CARNIVORE_LYNX_PORONHOITO' ||
                harvestPermitCategory === 'LARGE_CARNIVORE_WOLF';
            var isMammalApplication = harvestPermitCategory === 'MAMMAL';
            var isNestRemovalApplication = harvestPermitCategory === 'NEST_REMOVAL';
            var isLawSectionTenApplication = harvestPermitCategory === 'LAW_SECTION_TEN';
            var isWeaponTransportationApplication = harvestPermitCategory === 'WEAPON_TRANSPORTATION';
            var isDisabilityApplication = harvestPermitCategory === 'DISABILITY';
            var isDogUnleashApplication = harvestPermitCategory === 'DOG_UNLEASH';
            var isDogDisturbanceApplication = harvestPermitCategory === 'DOG_DISTURBANCE';
            var isDeportationApplication = harvestPermitCategory === 'DEPORTATION';
            var isResearchApplication = harvestPermitCategory === 'RESEARCH';
            var isImportingApplication = harvestPermitCategory === 'IMPORTING';
            var isGameManagementApplication = harvestPermitCategory === 'GAME_MANAGEMENT';

            $uibModal.open({
                templateUrl: 'account/application/application-modal.html',
                size: 'lg',
                controllerAs: '$ctrl',
                controller: ModalController,
                resolve: {
                    applicationSummary: function (MooselikePermitApplication, BirdPermitApplication,
                                                  CarnivorePermitApplication, MammalPermitApplication,
                                                  NestRemovalPermitApplication, LawSectionTenPermitApplication,
                                                  WeaponTransportationPermitApplication, DisabilityPermitApplication,
                                                  DogUnleashApplication, DogDisturbanceApplication,
                                                  DeportationPermitApplication, ResearchPermitApplication,
                                                  ImportingPermitApplication, GameManagementPermitApplication) {
                        if (isMooselikeApplication) {
                            return MooselikePermitApplication.getFullDetails({id: application.id}).$promise;
                        } else if (isBirdApplication) {
                            return BirdPermitApplication.getFullDetails({id: application.id}).$promise;
                        } else if (isCarnovireApplication) {
                            return CarnivorePermitApplication.getFullDetails({id: application.id}).$promise;
                        } else if (isMammalApplication) {
                            return MammalPermitApplication.getFullDetails({id: application.id}).$promise;
                        } else if (isNestRemovalApplication) {
                            return NestRemovalPermitApplication.getFullDetails({id: application.id}).$promise;
                        } else if (isLawSectionTenApplication) {
                            return LawSectionTenPermitApplication.getFullDetails({id: application.id}).$promise;
                        } else if (isWeaponTransportationApplication) {
                            return WeaponTransportationPermitApplication.getFullDetails({id: application.id}).$promise;
                        } else if (isDisabilityApplication) {
                            return DisabilityPermitApplication.getFullDetails({id: application.id}).$promise;
                        } else if (isDogUnleashApplication) {
                            return DogUnleashApplication.getFullDetails({id: application.id}).$promise;
                        } else if (isDogDisturbanceApplication) {
                            return DogDisturbanceApplication.getFullDetails({id: application.id}).$promise;
                        } else if (isDeportationApplication) {
                            return DeportationPermitApplication.getFullDetails({id: application.id}).$promise;
                        } else if (isResearchApplication) {
                            return ResearchPermitApplication.getFullDetails({id: application.id}).$promise;
                        } else if (isImportingApplication) {
                            return ImportingPermitApplication.getFullDetails({id: application.id}).$promise;
                        } else if (isGameManagementApplication) {
                            return GameManagementPermitApplication.getFullDetails({id: application.id}).$promise;
                        } else {
                            return null;
                        }
                    },
                    permitArea: function (MooselikePermitApplication) {
                        if (isMooselikeApplication) {
                            return MooselikePermitApplication.getArea({
                                id: application.id
                            }).$promise;
                        }

                        return null;
                    },
                    mooselikeAmendment: function (HarvestPermitAmendmentApplications) {
                        if (isMooselikeAmendment) {
                            return HarvestPermitAmendmentApplications.get({id: application.id}).$promise;
                        }

                        return null;
                    }
                }
            });
        };

        function ModalController($uibModalInstance, applicationSummary, permitArea, mooselikeAmendment) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.applicationSummary = applicationSummary;
                $ctrl.permitArea = permitArea;
                $ctrl.mooselikeAmendment = mooselikeAmendment;
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }
    });
