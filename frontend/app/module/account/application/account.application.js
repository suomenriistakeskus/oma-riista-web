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
                harvestPermitCategory === 'LARGE_CARNIVORE_WOLF'
            ;

            $uibModal.open({
                templateUrl: 'account/application/application-modal.html',
                size: 'lg',
                controllerAs: '$ctrl',
                controller: ModalController,
                resolve: {
                    applicationSummary: function (MooselikePermitApplication, BirdPermitApplication,
                                                  CarnivorePermitApplication) {
                        if (isMooselikeApplication) {
                            return MooselikePermitApplication.getFullDetails({id: application.id}).$promise;
                        } else if (isBirdApplication) {
                            return BirdPermitApplication.getFullDetails({id: application.id}).$promise;
                        } else if (isCarnovireApplication) {
                            return CarnivorePermitApplication.getFullDetails({id: application.id}).$promise;
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
