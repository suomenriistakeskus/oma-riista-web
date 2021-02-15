'use strict';

angular.module('app.jht.nominationdecision.document.delivery', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.nominationdecision.document.delivery', {
            url: '/delivery',
            templateUrl: 'jht/nominationdecision/document/delivery/document.delivery.html',
            controllerAs: '$ctrl',
            controller: function (NominationDecisionUtils, NominationDecisionDeliveryModal, NominationDecision,
                                  NotificationService, RefreshNominationStateService, NominationDecisionSection,
                                  decision, reference, decisionId) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.sectionId = NominationDecisionSection.DELIVERY;
                    $ctrl.decision = decision;
                    $ctrl.sectionContent = NominationDecisionUtils.getSectionContent(decision, $ctrl.sectionId);
                };

                $ctrl.editSection = function () {
                    var referenceDecisionId = _.get(reference, 'id');

                    NominationDecisionDeliveryModal.open(decisionId, referenceDecisionId).then(function (deliveries) {
                        return NominationDecision.updateDeliveries({
                            id: decisionId,
                            deliveries: deliveries
                        }).$promise.then(function () {
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

    .service('NominationDecisionDeliveryModal', function ($uibModal, NominationDecision, DecisionRkaRecipient) {
        this.open = function (decisionId, referenceId) {
            return $uibModal.open({
                templateUrl: 'jht/nominationdecision/document/delivery/select-delivery-modal.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'lg',
                resolve: {
                    decisionId: _.constant(decisionId),
                    deliveries: function () {
                        return NominationDecision.getDeliveries({id: decisionId}).$promise;
                    },
                    referenceDeliveries: function () {
                        return referenceId ? NominationDecision.getDeliveries({id: referenceId}).$promise : null;
                    },
                    rkaDeliveryList: function () {
                        return DecisionRkaRecipient.query().$promise;
                    }
                }
            }).result;
        };

        function ModalController($uibModalInstance, $filter,
                                 decisionId, deliveries, referenceDeliveries, rkaDeliveryList) {
            var $ctrl = this;

            var rI18nNameFilter = $filter('rI18nNameFilter');

            function calculateAvailableDeliveries() {
                $ctrl.availableDeliveries = _(rkaDeliveryList)
                    .map(function (delivery) {
                        return {
                            rka: rI18nNameFilter(delivery.rka),
                            name: rI18nNameFilter(delivery),
                            email: delivery.email
                        };
                    })
                    .filter(function (delivery) {
                        return delivery.rka === $ctrl.rka;
                    })
                    .filter(function (delivery) {
                        return !_.some($ctrl.deliveries, function (d) {
                            return d.name === delivery.name;
                        });
                    })
                    .sortBy('name')
                    .value();
            }

            $ctrl.$onInit = function () {
                $ctrl.selectedTab = 'a';

                $ctrl.rka = null;
                $ctrl.deliveries = deliveries;
                $ctrl.referenceDeliveries = referenceDeliveries;
                $ctrl.typeaheadModel = null;

                $ctrl.referenceEnabled = referenceDeliveries && referenceDeliveries.length;
                $ctrl.referenceContent = referenceDeliveries;

                $ctrl.fromList = true;

                // Arbitrary delivery where recipient name and email added manually.
                $ctrl.adhoc = {};

                $ctrl.rkas = _(rkaDeliveryList).map(function (d) {
                    return rI18nNameFilter(d.rka);
                }).uniq().sort().value();

                calculateAvailableDeliveries();
            };

            $ctrl.overwriteWithReference = function () {
                $ctrl.deliveries = referenceDeliveries;
            };

            $ctrl.rkaSelected = function () {
                calculateAvailableDeliveries();
            };

            $ctrl.addAdhoc = function () {
                $ctrl.add($ctrl.adhoc);
                $ctrl.adhoc = {};
            };

            $ctrl.add = function (item) {
                if (item) {
                    var delivery = angular.copy(item);
                    delete delivery.rka;
                    if (!_.find($ctrl.deliveries, delivery)) {
                        $ctrl.deliveries.push(delivery);
                    }
                    calculateAvailableDeliveries();
                }
            };

            $ctrl.remove = function (delivery) {
                _.remove($ctrl.deliveries, delivery);
                calculateAvailableDeliveries();
            };

            $ctrl.save = function () {
                $uibModalInstance.close($ctrl.deliveries);
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }
    });
