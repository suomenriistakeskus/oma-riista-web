'use strict';

angular.module('app.harvestpermit.decision.reference', [])
    .service('PermitDecisionChangeReferenceModal', function ($uibModal, NotificationService, PermitDecision) {
        this.open = function (decision) {
            var modalPromise = $uibModal.open({
                templateUrl: 'harvestpermit/decision/reference/select-reference-modal.html',
                size: 'lg',
                controllerAs: '$ctrl',
                controller: ModalController,
                resolve: {
                    decision: _.constant(decision),
                    handlers: function (HarvestPermitApplications) {
                        return HarvestPermitApplications.listHandlers().$promise;
                    }
                }
            }).result;

            return NotificationService.handleModalPromise(modalPromise).then(function (reference) {
                return PermitDecision.updateReference({
                    id: decision.id,
                    referenceId: reference.id
                }).$promise;
            });
        };

        function ModalController($uibModalInstance, HuntingYearService, ApplicationStatusList, DerogationReasonType, PermitDecision,
                                 PermitCategories, DecisionGrantStatus, HarvestPermitApplications,
                                 decision, handlers) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.statusList = ApplicationStatusList.decision();
                $ctrl.availableSpecies = [];
                $ctrl.handlers = handlers;
                $ctrl.permitCategoryList = _.map(PermitCategories, function (category) {
                    return {
                        category: category,
                        translationKey: 'harvestpermit.wizard.summary.permitCategory.' + category
                    };
                });
                $ctrl.languages = ['fi', 'sv'];
                $ctrl.grantStatusList = DecisionGrantStatus;
                $ctrl.derogationReasonList = DerogationReasonType;
                $ctrl.filters = {
                    harvestPermitCategory: decision.harvestPermitCategory
                };
                reloadAvailableSpecies(decision.harvestPermitCategory);

                var beginYear = 2018;
                var endYear = new Date().getFullYear();
                var years = _.range(beginYear, endYear + 1);

                $ctrl.huntingYears = _.map(years, function (year) {
                    return HuntingYearService.toObj(year);
                });

                $ctrl.previewEnabledId = null;

                $ctrl.onPermitCategoryChange = function () {
                    reloadAvailableSpecies($ctrl.filters.harvestPermitCategory);
                };

                function reloadAvailableSpecies(harvestPermitCategory) {
                    HarvestPermitApplications.listSpecies({permitCategory: harvestPermitCategory}).$promise
                        .then(function (res) {
                            $ctrl.availableSpecies = res;
                        });
                }
            };

            $ctrl.loadPage = function (page) {
                var f = $ctrl.filters;

                PermitDecision.searchReferences({
                    // required
                    huntingYear: f.huntingYear,
                    status: f.statusText ? [f.statusText] : null,
                    page: page || 0,
                    size: 100,

                    // optional
                    gameSpeciesCode: f.gameSpeciesCode,
                    harvestPermitCategory: f.harvestPermitCategory === 'ALL' ? null : f.harvestPermitCategory,
                    decisionLocale: $ctrl.filters.decisionLocale,
                    rhyOfficialCode: f.rhyOfficialCode,
                    rkaOfficialCode: f.rkaOfficialCode,
                    applicationNumber: f.applicationNumber,
                    handlerId: f.handlerId,
                    grantStatus: f.grantStatus ? [f.grantStatus] : null,
                    derogationReason: f.derogationReason ? [f.derogationReason] : null

                }).$promise.then(function (res) {
                    $ctrl.results = res;
                    $ctrl.results.content = _.filter($ctrl.results.content, function (r) {
                        return r.id !== decision.id;
                    });
                });
            };

            $ctrl.togglePreview = function (id) {
                if ($ctrl.previewEnabledId === id) {
                    $ctrl.previewEnabledId = null;
                } else {
                    $ctrl.previewEnabledId = id;
                }
            };

            $ctrl.previewUrl = function (id) {
                return '/api/v1/decision/' + id + '/print/html?sectionId=decision';
            };

            $ctrl.resolveUnifiedStatus = function (application) {
                if (application.status === 'AMENDING') {
                    return 'AMENDING';
                }

                if (application.status === 'ACTIVE' && !application.handler) {
                    return 'ACTIVE';
                }
                return application.decisionStatus;
            };


            $ctrl.selectReference = function (reference) {
                $uibModalInstance.close(reference);
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }
    });
