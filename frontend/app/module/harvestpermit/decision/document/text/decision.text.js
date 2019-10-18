'use strict';

angular.module('app.harvestpermit.decision.document.text', [])

    .component('permitDecisionDocumentTextSection', {
        templateUrl: 'harvestpermit/decision/document/text/text-section.html',
        controllerAs: '$ctrl',
        bindings: {
            sectionId: '<',
            decision: '<',
            reference: '<'
        },
        controller: function (PermitDecision, PermitDecisionUtils, PermitDecisionDocumentEditModal,
                              NotificationService, RefreshDecisionStateService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.sectionContent = PermitDecisionUtils.getSectionContent($ctrl.decision, $ctrl.sectionId);
            };

            $ctrl.canEditContent = function () {
                return PermitDecisionUtils.canEditContent($ctrl.decision, $ctrl.sectionId);
            };

            $ctrl.editSection = function () {
                PermitDecisionUtils.reloadSectionContent($ctrl.sectionId, $ctrl.decision.id).then(function (textContent) {
                    return editContent(textContent).then(storeTextContent);
                });
            };

            function editContent(textContent) {
                var referenceContent = PermitDecisionUtils.getReferenceContent($ctrl.reference, $ctrl.sectionId);

                return PermitDecisionDocumentEditModal.open($ctrl.decision.id, $ctrl.sectionId, textContent, referenceContent);
            }

            function storeTextContent(editedTextContent) {
                PermitDecision.updateDocument({id: $ctrl.decision.id}, {
                    sectionId: $ctrl.sectionId,
                    content: editedTextContent

                }).$promise.then(function () {
                    NotificationService.showDefaultSuccess();

                    RefreshDecisionStateService.refresh();

                }, function () {
                    NotificationService.showDefaultFailure();
                });
            }
        }
    })

    .service('PermitDecisionDocumentEditModal', function ($uibModal) {
        this.open = function (decisionId, sectionId, textContent, referenceContent) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/document/text/edit-modal.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'lg',
                resolve: {
                    decisionId: _.constant(decisionId),
                    sectionId: _.constant(sectionId),
                    textContent: _.constant(textContent),
                    referenceContent: _.constant(referenceContent)
                }
            }).result;
        };

        function ModalController($uibModalInstance, $timeout, PermitDecision,
                                 sectionId, decisionId, textContent, referenceContent) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.sectionId = sectionId;
                $ctrl.textContent = (textContent || '').trim();
                $ctrl.generatePossible = sectionId !== 'execution';
                $ctrl.referenceEnabled = _.isString(referenceContent);
                $ctrl.referenceShown = $ctrl.referenceEnabled;
                $ctrl.referenceContent = referenceContent || '';
                $ctrl.splitView = !!referenceContent;
                $ctrl.showDiff = false;
                $ctrl.contentChanged();
            };

            $ctrl.overwriteWithReference = function () {
                $ctrl.textContent = $ctrl.referenceContent;
                $ctrl.contentChanged();
            };

            $ctrl.generateText = function () {
                PermitDecision.generateText({
                    id: decisionId,
                    sectionId: sectionId
                }).$promise.then(function (res) {
                    $ctrl.textContent = res.content;
                    $ctrl.contentChanged();
                });
            };

            $ctrl.save = function () {
                $uibModalInstance.close($ctrl.textContent);
            };

            $ctrl.toggleReference = function () {
                $ctrl.referenceShown = !$ctrl.referenceShown;
                $ctrl.splitView = !$ctrl.splitView;
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };

            $ctrl.contentChanged = function () {
                updateDiff();
            };

            function updateDiff() {
                if (!$ctrl.showDiff) {
                    return;
                }
                var diff = JsDiff.diffWordsWithSpace($ctrl.referenceContent, $ctrl.textContent);
                if (diff && diff.length === 1 && !diff[0].added && !diff[0].removed) {
                    $ctrl.diff = '<strong>Ei eroa referenssiin</strong>';
                    return;
                }

                var nodes = '';
                for (var i = 0; i < diff.length; i++) {
                    var node;
                    if (diff[i].removed) {
                        node = '<span class="del" title="Poistettu">' + _makeSpacesVisible(diff[i].value) + '</span>';
                    } else if (diff[i].added) {
                        node = '<span class="ins" title="Lisätty">' + _makeSpacesVisible(diff[i].value) + '</span>';
                    } else {
                        node = diff[i].value;
                    }
                    nodes += node;
                }
                $ctrl.diff = _makeNewLinesVisible(nodes);
            }

            function _makeSpacesVisible(str) {
                // This is to handle changes in whitespace
                // Make sure there really is a space before nbsp, otherwise text will not wrap as expected
                return str.replace(/ /gi, ' &nbsp;');
            }

            function _makeNewLinesVisible(str) {
                return str.replace(/\n/gi, '&nbsp;<br>');
            }
        }
    });
