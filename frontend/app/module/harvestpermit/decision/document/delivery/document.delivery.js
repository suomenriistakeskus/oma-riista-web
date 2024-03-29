'use strict';

angular.module('app.harvestpermit.decision.document.delivery', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.decision.document.delivery', {
            url: '/delivery',
            templateUrl: 'harvestpermit/decision/document/delivery/document.delivery.html',
            controllerAs: '$ctrl',
            controller: function (PermitDecisionUtils, PermitDecisionDeliveryModal, PermitDecision,
                                  PermitDecisionSection, NotificationService, RefreshDecisionStateService,
                                  decision, reference, decisionId) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.sectionId = PermitDecisionSection.DELIVERY;
                    $ctrl.decision = decision;
                    $ctrl.sectionContent = PermitDecisionUtils.getSectionContent(decision, $ctrl.sectionId);
                };

                $ctrl.editSection = function () {
                    var referenceDecisionId = _.get(reference, 'id');

                    PermitDecisionDeliveryModal.open(decisionId, referenceDecisionId, decision.automaticDeliveryDeduction)
                        .then(function (deliveries) {
                            return PermitDecision.updateDeliveries({
                                id: decisionId,
                                deliveries: deliveries
                            }).$promise.then(function () {
                                NotificationService.showDefaultSuccess();
                                RefreshDecisionStateService.refresh();

                            }, function () {
                                NotificationService.showDefaultFailure();
                            });
                        });
                };

                $ctrl.canEditContent = function () {
                    return PermitDecisionUtils.canEditContent(decision, $ctrl.sectionId);
                };
            }
        });
    })

    .component('permitDecisionDeliveryListing', {
        templateUrl: 'harvestpermit/decision/document/delivery/delivery-listing.html',
        bindings: {
            deliveries: '<',
            remove: '&'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                var removeFn = $ctrl.remove();
                $ctrl.editable = !!removeFn;

                $ctrl.remove = function (delivery) {
                    removeFn(delivery);
                };
            };
        }
    })

    .service('PermitDecisionDeliveryModal', function ($uibModal, PermitDecision, DecisionRkaRecipient) {
        this.open = function (decisionId, referenceId, automaticDeliveryDeduction) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/document/delivery/select-delivery-modal.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'lg',
                resolve: {
                    decisionId: _.constant(decisionId),
                    deliveries: function () {
                        return PermitDecision.getDeliveries({id: decisionId}).$promise;
                    },
                    referenceDeliveries: function () {
                        return referenceId ? PermitDecision.getDeliveries({id: referenceId}).$promise : null;
                    },
                    rkaDeliveryList: function () {
                        return DecisionRkaRecipient.query().$promise;
                    },
                    automaticDeliveryDeduction: _.constant(automaticDeliveryDeduction)
                }
            }).result;
        };

        function ModalController($uibModalInstance, $filter, NotificationService,
                                 decisionId, deliveries, referenceDeliveries, rkaDeliveryList,
                                 automaticDeliveryDeduction) {
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

                $ctrl.deliveries = deliveries;
                $ctrl.automaticDeliveryDeduction = automaticDeliveryDeduction;
                $ctrl.referenceDeliveries = referenceDeliveries;
                $ctrl.typeaheadModel = null;

                $ctrl.referenceEnabled = referenceDeliveries && referenceDeliveries.length;
                $ctrl.referenceContent = referenceDeliveries;

                $ctrl.fromList = true;
                $ctrl.adhoc = {};

                $ctrl.rka = null;
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

            $ctrl.updateAutomaticDeliveryDeduction = function () {
                var params = {id: decisionId, enabled: $ctrl.automaticDeliveryDeduction};

                PermitDecision.updateAutomaticDeliveryDeduction(params).$promise
                    .then(function () {
                        NotificationService.showDefaultSuccess();
                    });
            };
        }
    });
