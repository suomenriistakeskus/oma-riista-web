'use strict';

angular.module('app.harvestpermit.application.mooselike.metsahallitus', ['app.metadata'])
    .service('MooselikePermitWizardMetsahallitusModal', function ($uibModal,
                                                                  TranslatedBlockUI, NotificationService,
                                                                  MooselikePermitApplication) {

        function ModalController($filter, $uibModalInstance) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.data = {
                    mhApplicationNumber: null,
                    mhPermitNumber: null
                };
            };

            $ctrl.ok = function () {
                $uibModalInstance.close($ctrl.data);
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }

        this.showImport = function (applicationId) {
            var modalInstance = $uibModal.open({
                templateUrl: 'harvestpermit/applications/mooselike/metsahallitus/metsahallitus.html',
                controller: ModalController,
                controllerAs: '$ctrl',
                resolve: {}
            });

            return modalInstance.result.then(function (data) {
                TranslatedBlockUI.start('global.block.wait');

                return MooselikePermitApplication.importMh({id: applicationId}, data).$promise.then(
                    function (res) {
                        NotificationService.showDefaultSuccess();
                        return res;
                    }, function (err) {
                        if (err.status === 404) {
                            NotificationService.showMessage('harvestpermit.wizard.attachments.mhNotFound', 'error');
                        } else {
                            NotificationService.showDefaultFailure();
                        }
                        return err;
                    })
                    .finally(function () {
                        TranslatedBlockUI.stop();
                    });
            });
        };
    });
