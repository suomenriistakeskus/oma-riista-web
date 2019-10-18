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
            controller: function ($state, $filter, $translate, PermitDecision, NotificationService,
                                  FormPostService, GameSpeciesCodes,
                                  decisionId, decision, revisions, decisionSpeciesAmounts, diaryParameters) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.decision = decision;
                    $ctrl.revisions = _.orderBy(revisions, ['id'], ['desc']);
                    $ctrl.mooselikeSpeciesCodes = _.chain(decisionSpeciesAmounts)
                        .map('gameSpeciesCode')
                        .filter(GameSpeciesCodes.isPermitBasedMooselike)
                        .value();
                    $ctrl.showHarvestReports =
                        decision.harvestPermitCategory === 'BIRD' &&
                        decision.status === 'PUBLISHED' &&
                        decision.decisionType === 'HARVEST_PERMIT' &&
                        decision.grantStatus !== 'REJECTED';
                    $ctrl.contactPersonReceivers = [];
                    $ctrl.otherReceivers = [];
                    $ctrl.activeRevision = null;
                    $ctrl.activeTab = 1;

                    if (_.size($ctrl.revisions) > 0) {
                        $ctrl.revisions[0].latest = true;
                        $ctrl.activeRevision = $ctrl.revisions[0];
                        $ctrl.onActiveRevisionChanged($ctrl.activeRevision);
                    }
                };

                $ctrl.onActiveRevisionChanged = function (revision) {
                    $ctrl.contactPersonReceivers = filterReceivers(revision, 'CONTACT_PERSON');
                    $ctrl.otherReceivers = filterReceivers(revision, 'OTHER');
                };

                function filterReceivers(revision, type) {
                    return _.filter(revision.receivers, _.matchesProperty('receiverType', type));
                }

                var dateFilter = $filter('date');

                $ctrl.getRevisionName = function (rev) {
                    return dateFilter(rev.lockedDate, 'd.M.yyyy HH:mm')
                        + (rev.externalId ? ' - ' + rev.externalId : '')
                        + (rev.latest ? ' - ' + $translate.instant('harvestpermit.decision.revision.latest') : '');
                };

                $ctrl.togglePosted = function (posted) {
                    var m = posted ? PermitDecision.updatePosted : PermitDecision.updateNotPosted;
                    m({id: decisionId, revisionId: $ctrl.activeRevision.id}).$promise.then(function (r) {
                        $ctrl.activeRevision.posted = r.posted;
                        $ctrl.activeRevision.postedByMailDate = r.postedByMailDate;
                        $ctrl.activeRevision.postedByMailUsername = r.postedByMailUsername;
                    }, function () {
                        NotificationService.showDefaultFailure();
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

                // Allow invoice downloading only for published decisions since decision status can change
                // back to draft after invoice is generated
                var canDownloadDecisionInvoice = function () {
                    return $ctrl.decision.hasDecisionInvoice
                        && $ctrl.decision.status === 'PUBLISHED'
                        && $ctrl.activeRevision
                        && !!$ctrl.activeRevision.publishDate;
                };

                $ctrl.getProcessingInvoiceDownloadStatus = function () {
                    if (!canDownloadDecisionInvoice()) {
                        return 'NOT_AVAILABLE';
                    }

                    if (!$ctrl.activeRevision.postalByMail) {
                        return 'DISABLED_BY_ELECTRONIC_INVOICING';
                    }

                    return 'AVAILABLE';
                };

                $ctrl.downloadProcessingInvoice = function () {
                    window.open('/api/v1/decision/' + decisionId + '/invoice/processing');
                };

                $ctrl.downloadHarvestInvoice = function (gameSpeciesCode) {
                    window.open('/api/v1/decision/' + decisionId + '/invoice/harvest/' + gameSpeciesCode);
                };

                $ctrl.downloadBirdHarvestReport = function () {
                    window.open('/api/v1/decision/' + decisionId + '/bird-harvest-report');
                };

                $ctrl.moveToInvoices = function () {
                    $state.go('jht.invoice.search', {applicationNumber: decision.applicationNumber});
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
