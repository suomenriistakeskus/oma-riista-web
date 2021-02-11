'use strict';

angular.module('app.harvestpermit.decision.document.decision', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.decision.document.decision', {
            url: '/decision',
            templateUrl: 'harvestpermit/decision/document/decision/document.decision.html',
            resolve: {
                speciesAmounts: function (Species, PermitDecisionSpecies, $filter, $translate, decisionId) {
                    return PermitDecisionSpecies.getSpecies({decisionId: decisionId}).$promise;
                },
                derogationLawSections: function (PermitDecisionDerogation, decision) {
                    return PermitDecisionDerogation.getReasons({id: decision.id}).$promise.then(function (data) {
                        return data.lawSections;
                    });
                },
                protectedAreaTypes: function (PermitDecisionDerogation, decision) {
                    return PermitDecisionDerogation.getProtectedAreaTypes({id: decision.id}).$promise.then(function (data) {
                        return data.types;
                    });
                },
                legalFields: function (PermitDecision, decisionId) {
                    return PermitDecision.getLegalFields({id: decisionId}).$promise;
                }
            },
            controllerAs: '$ctrl',
            controller: function (PermitDecisionUtils, PermitDecisionSection, PermitTypeCode,
                                  HarvestPermitCategoryType,
                                  decision, reference, speciesAmounts, derogationLawSections,
                                  protectedAreaTypes, legalFields) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.sectionId = PermitDecisionSection.DECISION;
                    $ctrl.extraSectionId = PermitDecisionSection.DECISION_EXTRA;
                    $ctrl.decision = decision;
                    $ctrl.reference = reference;
                    $ctrl.speciesAmounts = speciesAmounts;
                    $ctrl.showDerogationReasons = !_.isEmpty(derogationLawSections);
                    $ctrl.showProtectedAreaTypes = !_.isEmpty(protectedAreaTypes);
                    $ctrl.derogationLawSections = derogationLawSections;
                    $ctrl.showSpeciesAmounts = PermitTypeCode.hasSpeciesAmounts($ctrl.decision.permitTypeCode);
                    $ctrl.protectedAreaTypes = getSelectedItems(protectedAreaTypes);
                    $ctrl.legalFields = legalFields;
                    $ctrl.incompleteData = $ctrl.decision.decisionType === 'HARVEST_PERMIT' &&
                        $ctrl.decision.grantStatus !== 'REJECTED' &&
                        ($ctrl.showDerogationReasons && someSectionHasNothingSelected() ||
                        $ctrl.showProtectedAreaTypes && _.isEmpty($ctrl.protectedAreaTypes));
                    $ctrl.showLegalFields = HarvestPermitCategoryType.isDamageBasedDerogation(decision.harvestPermitCategory) ||
                        HarvestPermitCategoryType.isOtherDerogation(decision.harvestPermitCategory);
                    $ctrl.showGrantStatusHeader = !$ctrl.showSpeciesAmounts;
                };

                $ctrl.canEditContent = function () {
                    return PermitDecisionUtils.canEditContent(decision, $ctrl.sectionId);
                };

                $ctrl.denyComplete = function () {
                    return $ctrl.incompleteData;
                };

                function someSectionHasNothingSelected() {
                    return _.some($ctrl.derogationLawSections, function (section) {
                        return !_.some(section.reasons, 'checked');
                    });
                }

                function getSelectedItems(items) {
                    return _.filter(items, 'checked');
                }
            }
        });
    })

    .component('permitDecisionSpeciesAmounts', {
        bindings: {
            decisionId: '<',
            speciesAmounts: '<',
            permitTypeCode: '<',
            canEditContent: '<',
            harvestPermitCategory: '<'
        },
        templateUrl: 'harvestpermit/decision/document/decision/species-amounts.html',
        controller: function ($filter, $translate, Species, NotificationService, HarvestPermitCategoryType,
                              RefreshDecisionStateService, PermitDecisionSpeciesAmountModal, NonHarvestDecisionSpeciesAmountModal,
                              PermitDecisionSpeciesMethodModal, PermitTypes) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                var speciesMapping = Species.getSpeciesMapping();
                var i18n = $filter('rI18nNameFilter');

                $ctrl.speciesList = _.chain($ctrl.speciesAmounts)
                    .groupBy('gameSpeciesCode')
                    .map(function (amountList, gameSpeciesCode) {
                        var species = speciesMapping[gameSpeciesCode];
                        var amountComplete = _.every(amountList, 'amountComplete');
                        var forbiddenMethodComplete = _.every(amountList, 'forbiddenMethodComplete');

                        return {
                            name: i18n(species),
                            category: $translate.instant('global.gameCategory.' + species.category),
                            code: species.code,
                            amountComplete: amountComplete,
                            forbiddenMethodComplete: forbiddenMethodComplete,
                            sortOrder: species.category === 'UNPROTECTED' ? 1 : 2 // Unprotected first
                        };
                    })
                    .sortBy(['sortOrder', 'name'])
                    .value();

                $ctrl.showForbiddenMethods = HarvestPermitCategoryType.isDamageBasedDerogation($ctrl.harvestPermitCategory) ||
                    HarvestPermitCategoryType.isOtherDerogation($ctrl.harvestPermitCategory);

            };

            $ctrl.editSpeciesAmounts = function (gameSpeciesCode) {
                var modalPromise = $ctrl.permitTypeCode === PermitTypes.IMPORTING || $ctrl.permitTypeCode === PermitTypes.GAME_MANAGEMENT
                    ? NonHarvestDecisionSpeciesAmountModal.open($ctrl.decisionId, gameSpeciesCode, $ctrl.permitTypeCode)
                    : PermitDecisionSpeciesAmountModal.open($ctrl.decisionId, gameSpeciesCode, $ctrl.permitTypeCode);

                NotificationService.handleModalPromise(modalPromise).then(function () {
                    RefreshDecisionStateService.refresh();
                });
            };

            $ctrl.editForbiddenMethods = function (gameSpeciesCode) {
                var modalPromise = PermitDecisionSpeciesMethodModal.open($ctrl.decisionId, gameSpeciesCode);

                NotificationService.handleModalPromise(modalPromise).then(function () {
                    RefreshDecisionStateService.refresh();
                });
            };
        }
    })

    .component('permitDecisionLegalFields', {
        bindings: {
            decisionId: '<',
            legalFields: '<',
            canEditContent: '<'
        },
        templateUrl: 'harvestpermit/decision/document/decision/legal-fields.html',
        controller: function ($translate, PermitDecisionLegalFieldsModal, RefreshDecisionStateService) {
            var $ctrl = this;

            $ctrl.updateLegalFields = function () {
                PermitDecisionLegalFieldsModal.open($ctrl.decisionId).then(function () {
                    RefreshDecisionStateService.refresh();
                });
            };
        }
    })

    .service('PermitDecisionLegalFieldsModal', function ($uibModal, NotificationService, PermitDecision) {
        this.open = function (decisionId) {
            var modalPromise = $uibModal.open({
                templateUrl: 'harvestpermit/decision/document/decision/legal-fields-modal.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'md',
                resolve: {
                    decisionId: _.constant(decisionId),
                    legalFields: function () {
                        return PermitDecision.getLegalFields({id: decisionId}).$promise;
                    }
                }
            }).result;

            return NotificationService.handleModalPromise(modalPromise);
        };

        function ModalController($uibModalInstance, decisionId, legalFields) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.legalFields = legalFields;
            };

            $ctrl.save = function () {
                PermitDecision.updateLegalFields({id: decisionId}, $ctrl.legalFields).$promise.then(function () {
                    $uibModalInstance.close();
                }, function (err) {
                    $uibModalInstance.dismiss(err);
                });
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }
    });

