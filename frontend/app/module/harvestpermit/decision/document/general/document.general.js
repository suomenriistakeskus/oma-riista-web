'use strict';

angular.module('app.harvestpermit.decision.document.general', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.decision.document.general', {
            url: '/general',
            templateUrl: 'harvestpermit/decision/document/general/document.general.html',
            controllerAs: '$ctrl',
            controller: function (PermitDecisionUtils, PermitDecisionPublishSettingsModal,
                                  PermitDecisionDocumentSettingsModal,RefreshDecisionStateService,
                                  decision) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.decision = decision;
                };

                $ctrl.isHandler = function () {
                    return PermitDecisionUtils.isHandler($ctrl.decision);
                };

                $ctrl.editPublishSettings = function () {
                    PermitDecisionPublishSettingsModal.open($ctrl.decision.id).then(function () {
                        RefreshDecisionStateService.refresh();
                    });
                };

                $ctrl.editDocumentSettings = function () {
                    PermitDecisionDocumentSettingsModal.open($ctrl.decision.id, $ctrl.decision.permitTypeCode).then(function () {
                        RefreshDecisionStateService.refresh();
                    });
                };
            }
        });
    });
