'use strict';

angular.module('app.harvestpermit.decision.revisions', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.decision.revisions', {
            url: '/revisions',
            templateUrl: 'harvestpermit/decision/revisions/revisions.html',
            controllerAs: '$ctrl',
            resolve: {
                decision: function (PermitDecision, decisionId) {
                    return PermitDecision.get({id: decisionId}).$promise;
                },
                revisions: function (PermitDecision, decisionId) {
                    return PermitDecision.getRevisions({id: decisionId}).$promise;
                },
                decisionSpeciesAmounts: function (PermitDecisionSpecies, decisionId) {
                    return PermitDecisionSpecies.getSpecies({decisionId: decisionId}).$promise;
                },
                diaryParameters: function (GameDiaryParameters) {
                    return GameDiaryParameters.query().$promise;
                }

            },
            controller: function ($state, PermitDecision, NotificationService, FormPostService, GameSpeciesCodes,
                                  decisionId, decision, revisions, decisionSpeciesAmounts, diaryParameters) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.decision = decision;
                    $ctrl.revisions = _.sortByOrder(revisions, ['id'], ['desc']);
                    $ctrl.mooselikeSpeciesCodes = _.chain(decisionSpeciesAmounts)
                        .map('gameSpeciesCode')
                        .filter(GameSpeciesCodes.isPermitBasedMooselike)
                        .value();
                    $ctrl.activeRevision = null;
                    $ctrl.activeTab = 1;

                    if (_.size($ctrl.revisions) > 0) {
                        $ctrl.revisions[0].latest = true;
                        $ctrl.activeRevision = $ctrl.revisions[0];
                    }
                };

                $ctrl.togglePosted = function (posted) {
                    var m = posted ? PermitDecision.updatePosted : PermitDecision.updateNotPosted;
                    m({id: decisionId, revisionId: $ctrl.activeRevision.id}).$promise.then(function (r) {
                        $ctrl.activeRevision.posted = r.posted;
                        $ctrl.activeRevision.postedByMailDate = r.postedByMailDate;
                        $ctrl.activeRevision.postedByMailUsername = r.postedByMailUsername;
                    });
                };

                $ctrl.getSpeciesName = function (gameSpeciesCode) {
                    return diaryParameters
                        ? diaryParameters.$getGameName(gameSpeciesCode, null)
                        : gameSpeciesCode;
                };

                $ctrl.downloadPdf = function (id) {
                    FormPostService.submitFormUsingBlankTarget(
                        '/api/v1/decision/' + decisionId + '/revisions/' + id + '/pdf');
                };

                $ctrl.downloadAttachment = function (attachment) {
                    FormPostService.submitFormUsingBlankTarget(
                        '/api/v1/decision/' + decisionId + '/revisions/attachment' + '/' + attachment.id);
                };

                $ctrl.canDownloadDecisionInvoice = function () {
                    return $ctrl.activeRevision
                        && !!$ctrl.activeRevision.publishDate
                        && $ctrl.activeRevision.postalByMail;
                };

                $ctrl.downloadProcessingInvoice = function () {
                    window.open('/api/v1/decision/' + decisionId + '/invoice/processing');
                };

                $ctrl.downloadHarvestInvoice = function (gameSpeciesCode) {
                    window.open('/api/v1/decision/' + decisionId + '/invoice/harvest/' + gameSpeciesCode);
                };

                $ctrl.filterReceivers = function (type) {
                    return _.filter($ctrl.activeRevision.receivers, function (r) {
                        return r.receiverType === type;
                    });
                };

                $ctrl.moveToInvoices = function () {
                    $state.go('jht.payments.invoices', {applicationNumber: decision.applicationNumber});
                };

                $ctrl.moveToPermit = function () {
                    $state.go('permitmanagement.dashboard', {permitId: decision.harvestPermitId});
                };
            }
        });
    })
    .component('permitDecisionRevisionStateIcon', {
        templateUrl: 'harvestpermit/decision/revisions/revision-state-icon.html',
        bindings: {
            iconType: '@',
            iconEnabled: '<'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.iconClasses = {};
                $ctrl.iconClasses['fa-' + $ctrl.iconType] = true;
            };
        }
    });
