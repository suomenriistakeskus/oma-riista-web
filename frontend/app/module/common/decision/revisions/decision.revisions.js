'use strict';

angular.module('app.common.decision.revisions', [])
    .component('decisisionRevisionSelection', {
        templateUrl: 'common/decision/revisions/revision-selection.html',
        bindings: {
            revisions: '<',
            onActiveRevisionChanged: '&',
            downloadPdf: '&'
        },
        controllerAs: '$ctrl',
        controller: function ($filter, $translate) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                if (_.size($ctrl.revisions) > 0) {
                    $ctrl.activeRevision = $ctrl.revisions[0];
                    $ctrl.activeRevision.latest = true;
                    $ctrl.updateRevision();
                }
            };
            var dateFilter = $filter('date');

            $ctrl.getRevisionName = function (rev) {
                return dateFilter(rev.lockedDate, 'd.M.yyyy HH:mm')
                    + (rev.externalId ? ' - ' + rev.externalId : '')
                    + (rev.latest ? ' - ' + $translate.instant('decision.revision.latest') : '');
            };

            $ctrl.updateRevision = function () {
                $ctrl.onActiveRevisionChanged({revision: $ctrl.activeRevision});
            };
        }
    })

    .component('decisionRevisionDetails', {
        templateUrl: 'common/decision/revisions/revision-details.html',
        bindings: {
            activeRevision: '<'
        },
        controllerAs: '$ctrl'
    })

    .component('decisionRevisionAttachments', {
        templateUrl: 'common/decision/revisions/revision-attachments.html',
        bindings: {
            attachments: '<',
            downloadUrl: '<',
            allowEditOfAdditionalAttachments: '<',
            openAttachmentDialog: '&'
        },
        controllerAs: '$ctrl',
        controller: function (FormPostService) {
            var $ctrl = this;

            $ctrl.$onInit = function() {
                $ctrl.decisionAttachments = _.reject($ctrl.attachments, ['orderingNumber', null]);
                $ctrl.additionalAttachments = _.filter($ctrl.attachments, ['orderingNumber', null]);
            };

            $ctrl.downloadAttachment = function (attachment) {
                FormPostService.submitFormUsingBlankTarget($ctrl.downloadUrl + attachment.id);
            };
        }
    });
