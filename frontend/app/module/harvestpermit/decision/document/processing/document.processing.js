'use strict';

angular.module('app.harvestpermit.decision.document.processing', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.decision.document.processing', {
            url: '/processing',
            templateUrl: 'harvestpermit/decision/document/processing/document.processing.html',
            controllerAs: '$ctrl',
            controller: function (PermitDecisionUtils, PermitDecisionActionListModal,
                                  PermitDecisionAdjustedAreaSize, NotificationService, $state,
                                  RefreshDecisionStateService, decision, decisionId) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.sectionId = 'processing';
                    $ctrl.decision = decision;
                    $ctrl.sectionContent = PermitDecisionUtils.getSectionContent(decision, $ctrl.sectionId);
                    $ctrl.mooseLike = $ctrl.decision.permitTypeCode === '100';
                };

                $ctrl.editSection = function () {
                    PermitDecisionActionListModal.open(decisionId).then(function () {
                        RefreshDecisionStateService.refresh();
                    });
                };

                $ctrl.canEditContent = function () {
                    return PermitDecisionUtils.canEditContent(decision, $ctrl.sectionId);
                };

                $ctrl.canCreateAdjustedAreaAction = function () {
                    return $ctrl.canEditContent() && $ctrl.mooseLike;
                };

                $ctrl.createAdjustedAreaAction = function () {
                    PermitDecisionAdjustedAreaSize.create(decisionId).then(function () {
                        NotificationService.showDefaultSuccess();

                        $state.reload();
                    });
                };
            }
        });
    })

    .service('PermitDecisionAdjustedAreaSize', function ($q, dialogs, Helpers, NotificationService,
                                                         PermitDecisionAction, PermitDecision) {
        this.create = function (decisionId) {
            return PermitDecision.generateText({
                id: decisionId,
                sectionId: 'adjustedAreaSizeAction'

            }).$promise.then(function (res) {
                if (!res.content) {
                    NotificationService.showMessage('Korjattua pinta-alaa ei ole saatavilla', 'warn');
                    return $q.reject();
                }

                var dialogTitle = 'Luo välitoimenpide';
                var actionTextPlain = res.content;
                var actionTextHtml = res.content.replace(/\n\n/g, '<br/><br/>').replace(/\n/g, '<br/>');
                var dialogMessage = '<p><strong>Lisätäänkö päätökselle seuraava välitoimenpide?</strong></p>' +
                    '<hr><p>' + actionTextHtml + '</p>';

                return dialogs.confirm(dialogTitle, dialogMessage).result.then(function () {
                    var action = {
                        actionType: 'MUU',
                        pointOfTime: Helpers.dateTimeToString(new Date()),
                        text: actionTextPlain,
                        decisionText: actionTextPlain
                    };

                    return PermitDecisionAction.save({decisionId: decisionId}, action).$promise;
                });
            });

        };
    });
