(function () {
    'use strict';

    angular.module('app.account.profile', [])
        .config(function ($stateProvider) {
            $stateProvider.state('profile.account', {
                url: '/account',
                templateUrl: 'account/profile/profile.html',
                controllerAs: '$ctrl',
                resolve: {
                    personId: function ($stateParams) {
                        return $stateParams.id;
                    },
                    isUserOwnProfile: function (personId) {
                        return personId === 'me';
                    },
                    profile: function (AccountService, personId) {
                        return AccountService.loadAccount(personId);
                    },
                    clubInvitations: function (Account, personId, isUserOwnProfile) {
                        return isUserOwnProfile
                            ? Account.clubInvitations().$promise
                            : Account.clubInvitations({personId: personId}).$promise;
                    },
                    jhtTrainings: function (JHTTrainings, personId, isUserOwnProfile) {
                        var sortResults = _.partialRight(_.sortBy, 'trainingDate');

                        return isUserOwnProfile
                            ? JHTTrainings.mine().$promise.then(sortResults)
                            : JHTTrainings.forPerson({personId: personId}).$promise.then(sortResults);
                    }
                },
                controller: function ($uibModal, Account, AccountService,
                                      profile, personId, isUserOwnProfile, clubInvitations, jhtTrainings) {
                    var $ctrl = this;
                    $ctrl.profile = profile;
                    $ctrl.isUserOwnProfile = isUserOwnProfile;
                    $ctrl.clubInvitations = clubInvitations;
                    $ctrl.jhtTrainings = jhtTrainings;

                    $ctrl.loadAccount = function () {
                        return AccountService.loadAccount(personId);
                    };

                    $ctrl.save = function (profile) {
                        var saveMethod = $ctrl.isUserOwnProfile ? Account.updateSelf : Account.updateOther;
                        return saveMethod(profile).$promise;
                    };
                }
            });
        })
        .component('accountProfilePerson', {
            templateUrl: 'account/profile/profile_person.html',
            bindings: {
                profile: '<'
            }
        })
        .component('accountProfileOccupations', {
            templateUrl: 'account/profile/profile_occupations.html',
            bindings: {
                occupations: '<'
            }
        })
        .component('accountProfileJht', {
            templateUrl: 'account/profile/profile_jht.html',
            bindings: {
                jhtTrainings: '<'
            }
        })
        .component('accountProfileOther', {
            templateUrl: 'account/profile/profile_other.html',
            bindings: {
                profile: '<',
                onReloadProfile: '&',
                onSave: '&'
            },
            controller: function ($uibModal, $state, NotificationService) {
                var $ctrl = this;

                $ctrl.editOtherInfo = function () {
                    $ctrl.onReloadProfile().then(showModal);
                };

                function showModal(profile) {
                    var modalPromise = $uibModal.open({
                        templateUrl: 'account/edit_otherinfo.html',
                        controllerAs: '$ctrl',
                        controller: ModalController,
                        resolve: {
                            profile: _.constant(profile)
                        }
                    }).result.then(function (profile) {
                        return $ctrl.onSave({profile: profile});
                    });

                    NotificationService.handleModalPromise(modalPromise).then(function () {
                        NotificationService.flashMessage('account.edit.messages.success', 'success');
                        $state.reload();
                    });
                }

                function ModalController($uibModalInstance, profile) {
                    var $modalCtrl = this;
                    $modalCtrl.profile = profile;

                    $modalCtrl.save = function () {
                        $uibModalInstance.close($modalCtrl.profile);
                    };

                    $modalCtrl.cancel = function () {
                        $uibModalInstance.dismiss('cancel');
                    };
                }
            }
        })
        .component('accountProfileHunter', {
            templateUrl: 'account/profile/profile_hunter.html',
            bindings: {
                profile: '<'
            },
            controller: function ($window, AccountService) {
                var $ctrl = this;

                $ctrl.pdfOptions = AccountService.getPdfOptions($ctrl.profile);
                $ctrl.pdfSelection = $ctrl.pdfOptions[0];

                $ctrl.printPdf = function () {
                    if ($ctrl.pdfSelection) {
                        $window.open($ctrl.pdfSelection.url);
                    }
                };

            }
        })
        .component('accountProfileAddress', {
            templateUrl: 'account/profile/profile_address.html',
            bindings: {
                address: '<',
                addressSource: '<',
                onReloadProfile: '&',
                onSave: '&'
            },
            controller: function ($uibModal, $state, NotificationService) {
                var $ctrl = this;

                $ctrl.editAddress = function () {
                    showModal($ctrl.address);
                };

                function showModal(address) {
                    var modalPromise = $uibModal.open({
                        templateUrl: 'account/edit_address.html',
                        controllerAs: '$ctrl',
                        controller: ModalController,
                        resolve: {
                            address: _.constant(address)
                        }
                    }).result.then(saveAddress);

                    NotificationService.handleModalPromise(modalPromise).then(function () {
                        NotificationService.flashMessage('account.edit.messages.success', 'success');
                        $state.reload();
                    });
                }

                function saveAddress(address) {
                    return $ctrl.onReloadProfile().then(function (profile) {
                        profile.address = address;

                        return $ctrl.onSave({profile: profile});
                    });
                }

                function ModalController($uibModalInstance, address) {
                    var $modalCtrl = this;
                    $modalCtrl.address = address || {};

                    $modalCtrl.save = function () {
                        $uibModalInstance.close($modalCtrl.address);
                    };

                    $modalCtrl.cancel = function () {
                        $uibModalInstance.dismiss('cancel');
                    };
                }
            }
        })
        .component('accountProfilePassword', {
            templateUrl: 'account/profile/profile_password.html',
            bindings: {
                profile: '<'
            },
            controller: function ($uibModal, NotificationService) {
                var $ctrl = this;

                $ctrl.showChangePassword = function () {
                    var modalPromise = $uibModal.open({
                        templateUrl: 'account/change_password.html',
                        controllerAs: '$ctrl',
                        controller: ModalController
                    }).result;

                    NotificationService.handleModalPromise(modalPromise).then(function () {
                        NotificationService.flashMessage('change_password.messages.success', 'success');
                    });
                };

                function ModalController($uibModalInstance, Password) {
                    var $modalCtrl = this;

                    $modalCtrl.errorStatus = null;
                    $modalCtrl.doNotMatch = null;

                    $modalCtrl.cancel = function () {
                        $modalCtrl.modalInstance.dismiss('cancel');
                    };

                    $modalCtrl.changePassword = function ($form) {
                        if ($modalCtrl.password !== $modalCtrl.passwordConfirm) {
                            $modalCtrl.doNotMatch = "ERROR";
                            return;
                        }
                        $modalCtrl.doNotMatch = null;

                        Password.save({
                            'password': $modalCtrl.password,
                            'passwordConfirm': $modalCtrl.passwordConfirm,
                            'passwordCurrent': $modalCtrl.passwordCurrent
                        }).$promise.then(function () {
                            $uibModalInstance.close();

                        }, function (response) {
                            var notValidPw = response.status === 400;
                            if (notValidPw) {
                                $modalCtrl.passwordCurrent = '';
                            }
                            $modalCtrl.errorStatus = notValidPw ? 'invalid' : 'error';
                            $form.passwordCurrent.$setValidity('incorrect', !notValidPw);

                        }).finally(function () {
                            $form.$setPristine();
                        });
                    };

                    $modalCtrl.cancel = function () {
                        $uibModalInstance.dismiss('cancel');
                    };
                }
            }
        })
        .component('accountProfileDeactivate', {
            templateUrl: 'account/profile/profile_deactivate.html',
            bindings: {
                profile: '<'
            },
            controller: function ($uibModal, $state, Account, ActiveRoleService, NotificationService) {
                var $ctrl = this;
                $ctrl.isModerator = ActiveRoleService.isModerator();

                $ctrl.deactivate = function () {
                    var modalPromise = $uibModal.open({
                        templateUrl: 'account/disable_account.html',
                        controller: ModalController,
                        controllerAs: '$ctrl'
                    }).result.then(function () {
                        return Account.deactivate({
                            personId: $ctrl.profile.personId
                        }).$promise;
                    });

                    NotificationService.handleModalPromise(modalPromise).then(function () {
                        NotificationService.flashMessage('account.edit.messages.success', 'success');
                        $state.reload();
                    });
                };

                function ModalController($uibModalInstance) {
                    var $modalCtrl = this;

                    $modalCtrl.cancel = function () {
                        $uibModalInstance.dismiss('cancel');
                    };

                    $modalCtrl.deactivate = $uibModalInstance.close;
                }
            }
        })
        .component('accountProfileClubs', {
            templateUrl: 'account/profile/profile_clubs.html',
            bindings: {
                clubOccupations: '<',
                isUserOwnProfile: '<'
            },
            controller: function ($uibModal, $state, AccountService) {
                var $ctrl = this;

                $ctrl.registerClub = function () {
                    $uibModal.open({
                        size: 'lg',
                        templateUrl: 'account/club_register.html',
                        controller: 'AccountClubRegisterController',
                        controllerAs: 'modalCtrl',
                        bindToController: true
                    }).result.then(function () {
                        AccountService.updateRoles().finally(function () {
                            $state.go('roleselection');
                        });
                    });
                };

                $ctrl.createClub = function () {
                    $uibModal.open({
                        size: 'lg',
                        templateUrl: 'account/club_create.html',
                        controller: 'AccountClubCreateController',
                        controllerAs: 'modalCtrl',
                        bindToController: true
                    }).result.then(function () {
                        AccountService.updateRoles().finally(function () {
                            $state.go('roleselection');
                        });
                    });
                };
            }
        })
        .component('accountProfileInvitation', {
            templateUrl: 'account/profile/profile_invitation.html',
            bindings: {
                clubInvitations: '<',
                isUserOwnProfile: '<'
            },
            controller: function ($state, NotificationService, ClubInvitations) {
                var $ctrl = this;

                var _handleInvitation = function (serviceFunc, onSuccess, invitation) {
                    serviceFunc({id: invitation.id}).$promise.then(function () {
                        NotificationService.showDefaultSuccess();
                        onSuccess();
                    });
                };

                $ctrl.acceptInvitation = _.partial(_handleInvitation, ClubInvitations.accept, function () {
                    $state.go('profile.clubconfig');
                });

                $ctrl.rejectInvitation = _.partial(_handleInvitation, ClubInvitations.reject, $state.reload);
            }
        })
        .component('accountProfileSrva', {
            templateUrl: 'account/profile/profile_srva.html',
            bindings: {
                profile: '<'
            },
            controller: function ($state, $translate, NotificationService, SrvaService, dialogs) {
                var $ctrl = this;

                $ctrl.deactivate = function () {
                    updateSrvaStatus(false);
                };

                $ctrl.activate = function () {
                    var dialogTitle = $translate.instant('global.dialog.confirmation.title');
                    var dialogMessage = $translate.instant('account.profile.srva.activateConfirmation');

                    return dialogs.confirm(dialogTitle, dialogMessage).result.then(function () {
                        updateSrvaStatus(true);
                    });
                };

                function updateSrvaStatus(enable) {
                    SrvaService.updateSrvaStatus(enable)
                        .then(function () {
                                NotificationService.showDefaultSuccess();
                                $state.reload();
                            },
                            NotificationService.showDefaultFailure);
                }
            }
        })
    ;
})();
