'use strict';

angular.module('app.jht.nominationdecision.reference', [])
    .service('NominationDecisionChangeReferenceModal', function ($uibModal, NotificationService, NominationDecision) {
        this.open = function (decision) {
            var modalPromise = $uibModal.open({
                templateUrl: 'jht/nominationdecision/reference/select-reference-modal.html',
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
                return NominationDecision.updateReference({
                    id: decision.id,
                    referenceId: reference.id
                }).$promise;
            });
        };

        function ModalController($uibModalInstance, ApplicationStatusList, NominationDecision,
                                 NominationDecisionTypes, JHTOccupationTypes, DecisionGrantStatus,
                                 decision, handlers) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.statusList = ApplicationStatusList.decision();
                $ctrl.handlers = handlers;
                $ctrl.decisionTypeList = NominationDecisionTypes;
                $ctrl.occupationTypes = JHTOccupationTypes;
                $ctrl.grantStatusList = DecisionGrantStatus;
                $ctrl.filters = {
                };

                var beginYear = 2020;
                var endYear = new Date().getFullYear();
                $ctrl.decisionYears = _.range(beginYear, endYear + 1);

                $ctrl.previewEnabledId = null;
            };

            $ctrl.loadPage = function (page) {
                var f = $ctrl.filters;

                NominationDecision.searchReferences({
                    // required
                    year: f.decisionYear,
                    statuses: f.statusText ? [f.statusText] : null,
                    page: page || 0,
                    size: 100,

                    // optional
                    decisionTypes: f.decisionType ? [f.decisionType] : null,
                    occupationTypes: f.occupationType ? [f.occupationType] : null,
                    rhyOfficialCode: f.rhyOfficialCode,
                    rkaOfficialCode: f.rkaOfficialCode,
                    decisionNumber: f.decisionNumber,
                    handlerId: f.handlerId

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
                return '/api/v1/nominationdecision/' + id + '/print/html?sectionId=decision';
            };

            $ctrl.selectReference = function (reference) {
                $uibModalInstance.close(reference);
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }
    });
