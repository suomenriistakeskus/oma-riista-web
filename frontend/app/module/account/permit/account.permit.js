'use strict';

angular.module('app.account.permit', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permits', {
                url: '/permits',
                templateUrl: 'account/permit/list-mine.html',
                resolve: {
                    personId: function ($stateParams, ActiveRoleService) {
                        return ActiveRoleService.isModerator() ? $stateParams.id : 'me';
                    },
                    applicationsAndDecisions: function (personId, HarvestPermitApplications) {
                        var params = personId === 'me' ? {} : {personId: personId};
                        return HarvestPermitApplications.listMyApplicationsAndDecisions(params).$promise;
                    },
                    metsahallitusPermits: function (personId, HarvestPermits) {
                        var params = personId === 'me' ? {} : {personId: personId};
                        return HarvestPermits.listMetsahallitusPermits(params).$promise;
                    },
                    todoPermitIds: function ($q, ActiveRoleService, Account) {
                        if (ActiveRoleService.isModerator()) {
                            return $q.when([]);
                        }

                        return Account.countPermitTodo().$promise.then(function (res) {
                            return res.permitIds;
                        });
                    }
                },
                controllerAs: '$ctrl',
                controller: function ($state, ActiveRoleService,
                                      personId, todoPermitIds, applicationsAndDecisions, metsahallitusPermits) {
                    var $ctrl = this;

                    $ctrl.$onInit = function () {
                        $ctrl.applicationsAndDecisions = applicationsAndDecisions;
                        $ctrl.metsahallitusPermits = metsahallitusPermits;
                        $ctrl.todoPermitIds = todoPermitIds;
                        $ctrl.activeTab = 0;
                    };
                }
            });
    })

    .component('accountListRiistakeskusPermit', {
        templateUrl: 'account/permit/list-riistakeskus.html',
        bindings: {
            applicationsAndDecisions: '<',
            todoPermitIds: '<'
        }
    })

    .component('accountListMetsahallitusPermit', {
        templateUrl: 'account/permit/list-metsahallitus.html',
        bindings: {
            metsahallitusPermits: '<'
        }
    })

    .component('accountMyPermit', {
        templateUrl: 'account/permit/my-permit.html',
        bindings: {
            permit: '<',
            todo: '<'
        },
        controller: function ($state, AvailableRoleService, ActiveRoleService, LoginRedirectService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.moderatorView = ActiveRoleService.isModerator();
            };

            $ctrl.requiresAction = function () {
                return !$ctrl.moderatorView && angular.isArray($ctrl.todo) && $ctrl.todo.indexOf($ctrl.permit.id) >= 0;
            };

            $ctrl.open = function () {
                var permitRole = AvailableRoleService.findRoleForPermitId($ctrl.permit.id);

                if (permitRole) {
                    ActiveRoleService.selectActiveRole(permitRole);
                    LoginRedirectService.redirectToRoleDefaultState(permitRole);

                } else {
                    $state.go('permitmanagement.dashboard', {permitId: $ctrl.permit.id});
                }
            };
        }
    })

    .component('accountMyMetsahallitusPermit', {
        templateUrl: 'account/permit/my-metsahallitus.html',
        bindings: {
            permit: '<'
        },
        controller: function ($window, $translate, dialogs) {
            var $ctrl = this;

            $ctrl.submitHarvestFeedback = function () {

                var dialogTitle = $translate.instant('harvestpermit.mine.metsahallitus.openUrlDialogTitle');
                var dialogMessage = $translate.instant('harvestpermit.mine.metsahallitus.openUrlDialogText');
                dialogs.notify(dialogTitle, dialogMessage).result.then(function (yes) {
                    $window.open($ctrl.permit.url, '_blank');
                });

            };
        }
    })

    .component('accountMyDecision', {
        templateUrl: 'account/permit/my-decision.html',
        bindings: {
            decision: '<',
            todo: '<'
        },
        controller: function (FormPostService, HarvestPermitPdfUrl) {
            var $ctrl = this;

            $ctrl.pdf = function () {
                FormPostService.submitFormUsingBlankTarget(HarvestPermitPdfUrl.get($ctrl.decision.permitNumber));
            };
        }
    })

    .component('accountMyApplication', {
        templateUrl: 'account/permit/my-application.html',
        bindings: {
            application: '<'
        },
        controller: function ($state, $uibModal, ActiveRoleService, FetchAndSaveBlob,
                              HarvestPermitWizardSelectorService,
                              HarvestPermitApplications, AccountApplicationSummaryModal, MapPdfModal) {
            var $ctrl = this;

            $ctrl.edit = function () {
                var harvestPermitCategory = $ctrl.application.harvestPermitCategory;
                var wizardName = HarvestPermitWizardSelectorService.getWizardName(harvestPermitCategory);
                var wizardStateName = 'profile.permitwizard.' + wizardName + '.applicant';

                $state.go(wizardStateName, {applicationId: $ctrl.application.id});
            };

            $ctrl.view = function () {
                AccountApplicationSummaryModal.show($ctrl.application);
            };

            $ctrl.zip = function () {
                FetchAndSaveBlob.post('/api/v1/harvestpermit/application/' + $ctrl.application.id + '/archive');
            };

            $ctrl.pdf = function () {
                FetchAndSaveBlob.post('/api/v1/harvestpermit/application/' + $ctrl.application.id + '/print/pdf');
            };

            $ctrl.mapPdf = function () {
                MapPdfModal.printArea('/api/v1/harvestpermit/application/' + $ctrl.application.id + '/area/pdf');
            };

            $ctrl.delete = function () {
                HarvestPermitApplications.delete({id: $ctrl.application.id}).$promise.then(function () {
                    $state.reload();
                });
            };

            $ctrl.canDelete = function () {
                return $ctrl.application.status === 'DRAFT' && $ctrl.application.harvestPermitCategory !== 'MOOSELIKE';
            };
        }
    });
