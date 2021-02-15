'use strict';

angular.module('app.common.decision.document.text', [])

    .service('DecisionDocumentEditTextModal', function ($uibModal) {
        this.open = function (decisionId, sectionId, textContent, referenceContent, textGenerator, allowGenerate) {
            return $uibModal.open({
                templateUrl: 'common/decision/document/text/edit-modal.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'lg',
                resolve: {
                    decisionId: _.constant(decisionId),
                    sectionId: _.constant(sectionId),
                    textContent: _.constant(textContent),
                    referenceContent: _.constant(referenceContent),
                    textGenerator: _.constant(textGenerator),
                    allowGenerate: _.constant(allowGenerate)
                }
            }).result;
        };

        function ModalController($uibModalInstance, sectionId, decisionId, textContent, referenceContent, textGenerator,
                                 allowGenerate) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.sectionId = sectionId;
                $ctrl.textContent = (textContent || '').trim();
                $ctrl.generatePossible = allowGenerate;
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
                textGenerator({
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
