'use strict';

angular.module('app.jht.nominationdecision.overview', [])
    .config(function ($stateProvider) {
        $stateProvider

            .state('jht.nominationdecision.overview', {
                url: '/summary',
                templateUrl: 'jht/nominationdecision/overview/overview.html',
                resolve: {
                    decision: function (NominationDecision, decisionId) {
                        return NominationDecision.get({id: decisionId}).$promise;
                    },
                },
                controllerAs: '$ctrl',
                controller: function ($state, NominationDecision, NotificationService, FetchAndSaveBlob, ConfirmationDialogService,
                                      NominationDecisionActionListModal, NominationDecisionActionReadonlyListModal,
                                      NominationDecisionAppealSettingsModal, decisionId, decision) {
                    var $ctrl = this;

                    $ctrl.$onInit = function () {
                        $ctrl.decision = decision;
                    };

                    $ctrl.showActions = function () {
                        if ($ctrl.decision.userIsHandler && $ctrl.decision.status === 'DRAFT') {
                            NominationDecisionActionListModal.open(decisionId);
                        } else {
                            NominationDecisionActionReadonlyListModal.open(decisionId);
                        }
                    };

                    $ctrl.updateProposalDate = function () {
                        NominationDecision
                            .updateProposalDate({id: decisionId}, {date: $ctrl.decision.proposalDate})
                                .$promise
                                    .then(
                                        function (res) {
                                            NotificationService.showDefaultSuccess();
                                        },
                                        function (error) {
                                            NotificationService.showDefaultFailure();
                                        });
                    };

                    $ctrl.editAppealSettings = function () {
                        NominationDecisionAppealSettingsModal.open(decisionId).then(function () {
                            $state.reload();
                        });
                    };

                    $ctrl.assignDecision = function () {
                        return NominationDecision.assign({id: $ctrl.decision.id});
                    };

                    $ctrl.unassignDecision = function () {
                        return NominationDecision.unassign({id: $ctrl.decision.id});
                    };

                    $ctrl.remove = function () {
                        ConfirmationDialogService.showConfirmationDialogWithPrimaryAccept(
                            'jht.nomination.decision.removeConfirmation.title',
                            'jht.nomination.decision.removeConfirmation.body')
                            .then(function () {
                                NominationDecision.delete({id: $ctrl.decision.id}).$promise.then(
                                    function (res) {
                                        NotificationService.showDefaultSuccess();
                                        $state.go('jht.nominationdecisions');
                                    },
                                    function (error) {
                                        NotificationService.showDefaultFailure();
                                    });
                            });
                    };
                }
            });
    });
