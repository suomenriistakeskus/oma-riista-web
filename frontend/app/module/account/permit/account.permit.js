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
                    permits: function (personId, ActiveRoleService, HarvestPermits) {
                        return HarvestPermits.query(personId === 'me' ? {} : {personId: personId}).$promise;
                    },
                    applications: function (personId, HuntingYearService, ActiveRoleService, HarvestPermitApplications) {
                        return HarvestPermitApplications.query({
                            personId: personId === 'me' ? null : personId,
                            huntingYear: HuntingYearService.getCurrent()
                        }).$promise;
                    },
                    todoPermitIds: function ($q, ActiveRoleService, Account) {
                        if (ActiveRoleService.isModerator()) {
                            return $q.when([]);
                        }

                        return Account.countTodo().$promise.then(function (res) {
                            return res.permitIds;
                        });
                    },
                    diaryParameters: function (GameDiaryParameters) {
                        return GameDiaryParameters.query().$promise;
                    }
                },
                controllerAs: '$ctrl',
                controller: function ($state, ActiveRoleService,
                                      personId, permits, todoPermitIds, applications, diaryParameters) {
                    var $ctrl = this;

                    $ctrl.$onInit = function () {
                        $ctrl.moderatorView = ActiveRoleService.isModerator();
                        $ctrl.permits = permits;
                        $ctrl.applications = applications;
                        $ctrl.diaryParameters = diaryParameters;
                    };
                }
            })
            .state('profile.newpermit', {
                url: '/newpermit',
                templateUrl: 'harvestpermit/applications/wizard/new.html',
                controller: 'HarvestPermitWizardTypeController',
                controllerAs: '$ctrl',
                resolve: {
                    applicationTypes: function (HarvestPermitApplications) {
                        return HarvestPermitApplications.listTypes().$promise;
                    },
                    personId: function ($stateParams, ActiveRoleService) {
                        return ActiveRoleService.isModerator() ? $stateParams.id : null;
                    }
                }
            })
            .state('profile.permitwizard', {
                url: '/wizard/{applicationId:[0-9]{1,8}}',
                templateUrl: 'harvestpermit/applications/wizard/layout.html',
                abstract: true,
                controllerAs: '$ctrl',
                controller: function (applicationBasicDetails) {
                    this.applicationBasicDetails = applicationBasicDetails;
                },
                resolve: {
                    applicationId: function ($stateParams) {
                        return _.parseInt($stateParams.applicationId);
                    },
                    applicationBasicDetails: function (HarvestPermitApplications, applicationId) {
                        return HarvestPermitApplications.get({id: applicationId}).$promise;
                    },
                    wizard: function ($state, applicationId, applicationBasicDetails) {
                        return {
                            isAmending: _.constant(applicationBasicDetails.status === 'AMENDING'),
                            reload: $state.reload,
                            exit: function () {
                                $state.go('profile.permits');
                            },
                            goto: function (childState, params) {
                                $state.go('profile.permitwizard.' + childState, _.extend({
                                    applicationId: applicationId
                                }, params));
                            }
                        };
                    }
                }
            })
            .state('profile.permitwizard.applicant', {
                url: '/applicant',
                templateUrl: 'harvestpermit/applications/wizard/applicant.html',
                controller: 'HarvestPermitWizardApplicantController',
                controllerAs: '$ctrl',
                resolve: {
                    application: function (applicationId, HarvestPermitApplications) {
                        return HarvestPermitApplications.get({id: applicationId}).$promise;
                    }
                }
            })
            .state('profile.permitwizard.species', {
                url: '/species',
                templateUrl: 'harvestpermit/applications/wizard/species.html',
                controller: 'HarvestPermitWizardSpeciesController',
                controllerAs: '$ctrl',
                resolve: {
                    gameDiaryParameters: function (GameDiaryParameters) {
                        return GameDiaryParameters.query().$promise;
                    },
                    speciesAmounts: function (HarvestPermitApplications, applicationId) {
                        return HarvestPermitApplications.listSpeciesAmounts({id: applicationId}).$promise;
                    }
                }
            })
            .state('profile.permitwizard.partners', {
                url: '/partners',
                templateUrl: 'harvestpermit/applications/wizard/partners.html',
                controller: 'HarvestPermitWizardPartnersController',
                controllerAs: '$ctrl',
                resolve: {
                    partners: function (HarvestPermitApplicationAreaPartners, applicationId) {
                        return HarvestPermitApplicationAreaPartners.query({
                            applicationId: applicationId
                        }).$promise;
                    }
                }
            })
            .state('profile.permitwizard.map', {
                url: '/map',
                templateUrl: 'harvestpermit/applications/wizard/map.html',
                controller: 'HarvestPermitWizardMapController',
                controllerAs: '$ctrl',
                resolve: {
                    permitArea: function (HarvestPermitApplications, applicationId) {
                        return HarvestPermitApplications.getArea({
                            id: applicationId
                        }).$promise;
                    }
                }
            })
            .state('profile.permitwizard.attachments', {
                url: '/attachments',
                templateUrl: 'harvestpermit/applications/wizard/attachments.html',
                controller: 'HarvestPermitWizardAttachmentsController',
                controllerAs: '$ctrl',
                resolve: {
                    attachments: function (HarvestPermitApplications, applicationId) {
                        return HarvestPermitApplications.getAttachments({id: applicationId}).$promise;
                    },
                    shooterCounts: function (HarvestPermitApplications, applicationId) {
                        return HarvestPermitApplications.getShooterCounts({id: applicationId}).$promise;
                    },
                    freeHunting: function (HarvestPermitApplications, applicationId){
                        return HarvestPermitApplications.getArea({
                            id: applicationId
                        }).$promise.then(function(res) {
                            return res.freeHunting;
                        });
                    }
                }
            })
            .state('profile.permitwizard.summary', {
                url: '/summary',
                templateUrl: 'harvestpermit/applications/wizard/summary.html',
                controller: 'HarvestPermitWizardSummaryController',
                controllerAs: '$ctrl',
                resolve: {
                    application: function (applicationId, HarvestPermitApplications) {
                        return HarvestPermitApplications.getFullDetails({id: applicationId}).$promise;
                    },
                    isLate: function (HarvestPermitApplications, application) {
                        var params = {applicationId: application.id};
                        return HarvestPermitApplications.findType(params).$promise.then(function (applicationType) {
                            return !applicationType.active;
                        });
                    },
                    permitArea: function (HarvestPermitApplications, applicationId) {
                        return HarvestPermitApplications.getArea({
                            id: applicationId
                        }).$promise;
                    },
                    diaryParameters: function (GameDiaryParameters) {
                        return GameDiaryParameters.query().$promise;
                    }
                }
            });
    })

    .component('accountMyPermits', {
        templateUrl: 'account/permit/list-my-permits.html',
        bindings: {
            permits: '<',
            todo: '<'
        },
        controller: function ($state, ActiveRoleService) {
            var $ctrl = this;

            $ctrl.requiresAction = function (permit) {
                return !$ctrl.moderatorView && angular.isArray($ctrl.todo) && $ctrl.todo.indexOf(permit.id) >= 0;
            };

            $ctrl.open = function (harvestPermit) {
                var role = _.find(ActiveRoleService.getAvailableRoles(), function (r) {
                    return r.context.permitId === harvestPermit.id;
                });

                if (role) {
                    ActiveRoleService.selectActiveRole(role);
                    $state.go('permitmanagement.dashboard', {permitId: harvestPermit.id});
                } else {
                    $state.go('permitmanagement.dashboard', {permitId: harvestPermit.id});
                }
            };
        }
    })

    .component('accountMyApplications', {
        templateUrl: 'account/permit/list-my-applications.html',
        bindings: {
            applications: '<',
            diaryParameters: '<'
        },
        controller: function ($state, $uibModal, ActiveRoleService, FormPostService,
                              HarvestPermitApplications, MapPdfModal) {
            var $ctrl = this;

            $ctrl.edit = function (application) {
                $state.go('profile.permitwizard.applicant', {applicationId: application.id});
            };

            $ctrl.view = function (application) {
                $uibModal.open({
                    templateUrl: 'harvestpermit/applications/wizard/summary_modal.html',
                    size: 'lg',
                    resolve: {
                        application: function (HarvestPermitApplications) {
                            return HarvestPermitApplications.getFullDetails({id: application.id}).$promise;
                        },
                        permitArea: function (HarvestPermitApplications) {
                            return HarvestPermitApplications.getArea({
                                id: application.id
                            }).$promise;
                        },
                        diaryParameters: function (GameDiaryParameters) {
                            return GameDiaryParameters.query().$promise;
                        }
                    },
                    controllerAs: '$ctrl',
                    controller: function ($uibModalInstance, application, permitArea, diaryParameters) {
                        var $modalCtrl = this;
                        $modalCtrl.application = application;
                        $modalCtrl.permitArea = permitArea;
                        $modalCtrl.diaryParameters = diaryParameters;

                        $modalCtrl.cancel = function () {
                            $uibModalInstance.dismiss('cancel');
                        };
                    }
                });
            };

            $ctrl.zip = function (application) {
                FormPostService.submitFormUsingBlankTarget('/api/v1/harvestpermit/application/' + application.id + '/archive');
            };

            $ctrl.pdf = function (application) {
                FormPostService.submitFormUsingBlankTarget('/api/v1/harvestpermit/application/' + application.id + '/print/pdf');
            };

            $ctrl.mapPdf = function (application) {
                MapPdfModal.printArea('/api/v1/harvestpermit/application/' + application.id + '/area/pdf');
            };

            $ctrl.getSpeciesName = function (gameSpeciesCode) {
                return $ctrl.diaryParameters
                    ? $ctrl.diaryParameters.$getGameName(gameSpeciesCode, null)
                    : gameSpeciesCode;
            };

            $ctrl.canAmend = function (application) {
                return ActiveRoleService.isModerator() && (application.status === 'ACTIVE' || application.status === 'AMENDING');
            };

            $ctrl.startAmending = function (application) {
                HarvestPermitApplications.startAmending({id: application.id}).$promise.then(function () {
                    $state.go('profile.permitwizard.applicant', {applicationId: application.id});
                });
            };
        }
    });
