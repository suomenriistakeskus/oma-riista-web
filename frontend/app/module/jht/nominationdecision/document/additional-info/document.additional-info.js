'use strict';

angular.module('app.jht.nominationdecision.document.additionalinfo', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.nominationdecision.document.additional-info', {
            url: '/additional-info',
            templateUrl: 'jht/nominationdecision/document/additional-info/document.additional-info.html',
            controllerAs: '$ctrl',
            controller: function (NominationDecisionUtils, NominationDecision, NominationDecisionAuthoritiesModal,
                                  RefreshNominationStateService, NotificationService, NominationDecisionSection,
                                  decision, reference, decisionId) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.sectionId = NominationDecisionSection.ADDITIONAL_INFO;
                    $ctrl.decision = decision;
                    $ctrl.sectionContent = NominationDecisionUtils.getSectionContent(decision, $ctrl.sectionId);
                };

                $ctrl.editSection = function () {
                    var referenceDecisionId = _.get(reference, 'id');
                    var decisionLocale = $ctrl.decision.locale;

                    NominationDecisionAuthoritiesModal.open(decisionId, referenceDecisionId, decisionLocale).then(function (authorities) {
                        authorities.id = decisionId;

                        return NominationDecision.updateAuthorities(authorities).$promise.then(function () {
                            NotificationService.showDefaultSuccess();
                            RefreshNominationStateService.refresh();
                        }, function () {
                            NotificationService.showDefaultFailure();
                        });
                    });
                };

                $ctrl.canEditContent = function () {
                    return NominationDecisionUtils.canEditContent(decision, $ctrl.sectionId);
                };
            }
        });
    })

    .service('NominationDecisionAuthoritiesModal', function ($uibModal, NominationDecision) {
        this.open = function (decisionId, referenceId, locale) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/document/additional-info/select-authorities-modal.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'lg',
                resolve: {
                    decisionId: _.constant(decisionId),
                    decisionAuthorities: function () {
                        return NominationDecision.getAuthorities({id: decisionId}).$promise;
                    },
                    referenceAuthorities: function () {
                        return referenceId ? NominationDecision.getAuthorities({id: referenceId}).$promise : null;
                    },
                    rkaAuthorities: function (NominationDecisionRkaAuthority) {
                        var swedishLocale = locale === 'sv_FI';

                        return NominationDecisionRkaAuthority.listByNominationDecision({decisionId: decisionId}).$promise.then(function (res) {
                            return _.map(res, function (a) {
                                var res = _.pick(a, ['firstName', 'lastName', 'phoneNumber', 'email']);
                                res.title = swedishLocale ? a.titleSwedish : a.titleFinnish;
                                return res;
                            });
                        });
                    }
                }
            }).result;
        };

        function ModalController($uibModalInstance, $translate,
                                 decisionId, decisionAuthorities, rkaAuthorities, referenceAuthorities) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.selectedTab = 'a';
                $ctrl.decisionAuthorities = decisionAuthorities;
                $ctrl.rkaAuthorities = rkaAuthorities;
                $ctrl.referenceEnabled = referenceAuthorities && (referenceAuthorities.presenter || referenceAuthorities.decisionMaker);
                $ctrl.referenceAuthorities = referenceAuthorities;
                $ctrl.selectedAuthorities = {
                    presenter: null,
                    decisionMaker: null
                };
                updateAvailableAuthorities();
            };

            $ctrl.save = function () {
                $uibModalInstance.close($ctrl.decisionAuthorities);
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };

            $ctrl.formatAuthority = function (a) {
                return a === null ? '' : a.firstName + ' ' + a.lastName + ', ' + a.title + ', ' + a.phoneNumber + ', ' + a.email;
            };

            $ctrl.onPresenterSelected = function (presenter) {
                if (presenter) {
                    $ctrl.decisionAuthorities.presenter = angular.copy(presenter);
                    $ctrl.selectedAuthorities.presenter = null;
                    updateAvailableAuthorities();
                }
            };

            $ctrl.onDecisionMakerSelected = function (decisionMaker) {
                if (decisionMaker) {
                    $ctrl.decisionAuthorities.decisionMaker = angular.copy(decisionMaker);
                    $ctrl.selectedAuthorities.decisionMaker = null;
                    updateAvailableAuthorities();
                }
            };

            $ctrl.clear = function (attr) {
                $ctrl.decisionAuthorities[attr] = null;
                updateAvailableAuthorities();
            };

            $ctrl.overwriteWithReference = function () {
                $ctrl.decisionAuthorities.presenter = $ctrl.referenceAuthorities.presenter;
                $ctrl.decisionAuthorities.decisionMaker = $ctrl.referenceAuthorities.decisionMaker;
                updateAvailableAuthorities();
            };

            function updateAvailableAuthorities() {
                $ctrl.availableRkaAuthorities = _.filter($ctrl.rkaAuthorities, function (a) {
                    var aa = $ctrl.formatAuthority(a);
                    return aa !== $ctrl.formatAuthority($ctrl.decisionAuthorities.presenter) &&
                        aa !== $ctrl.formatAuthority($ctrl.decisionAuthorities.decisionMaker);
                });
            }
        }
    });
