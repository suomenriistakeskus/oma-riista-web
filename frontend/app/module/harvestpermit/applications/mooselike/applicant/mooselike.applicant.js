'use strict';

angular.module('app.harvestpermit.application.mooselike.applicant', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.mooselike.applicant', {
                url: '/applicant',
                templateUrl: 'harvestpermit/applications/mooselike/applicant/applicant.html',
                controller: 'MooselikePermitWizardApplicantController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    application: function (applicationId, HarvestPermitApplications) {
                        return HarvestPermitApplications.get({id: applicationId}).$promise;
                    }
                }
            })

            .state('jht.decision.application.wizard.mooselike.applicant', {
                url: '/applicant',
                templateUrl: 'harvestpermit/applications/mooselike/applicant/applicant.html',
                controller: 'MooselikePermitWizardApplicantController',
                controllerAs: '$ctrl',
                resolve: {
                    application: function (applicationId, HarvestPermitApplications) {
                        return HarvestPermitApplications.get({id: applicationId}).$promise;
                    }
                }
            });
    })

    .controller('MooselikePermitWizardApplicantController', function ($state,
                                                                      MooselikePermitApplication, NotificationService,
                                                                      MooselikePermitWizardChangeApplicantModal,
                                                                      wizard, application) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.contactPerson = application.contactPerson;
            // 17-Apr-2019 TODO: Properly store 'seurue' as separate permit holder type in the backend
            $ctrl.permitHolder = application.huntingClub ? application.permitHolder : null;
            $ctrl.personalApplication = !$ctrl.permitHolder;
        };

        $ctrl.changeApplicant = function () {
            MooselikePermitWizardChangeApplicantModal.changeApplicant(application.id, application.contactPerson).then(function (newPermitHolder) {
                var promise = MooselikePermitApplication.updatePermitHolder({id: application.id}, newPermitHolder).$promise;
                return NotificationService.handleModalPromise(promise);
            }).then(function () {
                $state.reload();
            });
        };

        $ctrl.exit = wizard.exit;
        $ctrl.constantTrue = _.constant(true);
        $ctrl.previous = function () {
            // no previous
        };

        $ctrl.isNextDisabled = function () {
            return $ctrl.permitHolder && !_.get($ctrl.permitHolder, 'type');
        };

        $ctrl.next = function () {
            wizard.goto('species');
        };
    })

    .service('MooselikePermitWizardChangeApplicantModal', function ($q, $uibModal,
                                                                    NotificationService, ActiveRoleService,
                                                                    MooselikePermitApplication, ChangeClubTypeModal) {

        function ModalController($uibModalInstance, contactPerson, applicants, moderator, applicationId) {
            var $ctrl = this;

            $ctrl.contactPerson = contactPerson;
            $ctrl.permitHolderCandidates = applicants;

            // for moderator search
            $ctrl.moderator = moderator;
            $ctrl.officialCodeSearch = null;
            $ctrl.searchResultClub = null;

            $ctrl.selectPermitHolderPerson = function () {
                $uibModalInstance.close(null);
            };

            $ctrl.selectPermitHolderClub = function (club) {
                if (club.subtype) {
                    return $uibModalInstance.close(club);
                }
                var modalPromise = ChangeClubTypeModal.editClubType(club);
                return NotificationService.handleModalPromise(modalPromise).then(function (updatedClub) {
                    return $uibModalInstance.close(updatedClub);
                });
            };

            $ctrl.searchByCode = function () {
                $ctrl.searchResultClub = null;
                if (_.size($ctrl.officialCodeSearch) < 7) {
                    return;
                }
                MooselikePermitApplication.searchPermitHolder({
                    id: applicationId,
                    officialCode: $ctrl.officialCodeSearch
                }).$promise.then(function (result) {
                    $ctrl.searchResultClub = result;
                }, function (err) {
                    if (err.status !== 404) {
                        NotificationService.showDefaultFailure();
                    }
                });
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }

        function showModal(applicationId, contactPerson) {
            var modalInstance = $uibModal.open({
                templateUrl: 'harvestpermit/applications/mooselike/applicant/applicant-select.html',
                size: 'lg',
                resolve: {
                    contactPerson: _.constant(contactPerson),
                    applicants: function () {
                        return MooselikePermitApplication.listAvailablePermitHolders({id: applicationId}).$promise;
                    },
                    moderator: _.constant(ActiveRoleService.isModerator()),
                    applicationId: _.constant(applicationId)
                },
                controller: ModalController,
                controllerAs: '$ctrl'
            });

            return modalInstance.result;
        }

        this.changeApplicant = function (applicationId, contactPerson) {
            return showModal(applicationId, contactPerson);
        };
    });
