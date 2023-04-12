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
                    },
                    shootingTests: function (Account, personId, isUserOwnProfile) {
                        return isUserOwnProfile
                            ? Account.shootingTests().$promise
                            : Account.shootingTests({personId: personId}).$promise;
                    },
                    occupationTrainings: function (Account, personId, isUserOwnProfile) {
                        return isUserOwnProfile
                            ? Account.occupationTrainings().$promise
                            : Account.occupationTrainings({personId: personId}).$promise;
                    },
                    huntingLeaderOccupations: function (HarvestPermits, isUserOwnProfile) {
                        return isUserOwnProfile
                            ? HarvestPermits.listWithHuntingClubGroups().$promise
                            : _.constant([]);
                    }
                },
                controller: function ($uibModal, Account, AccountService,
                                      profile, personId, isUserOwnProfile, clubInvitations, huntingLeaderOccupations,
                                      jhtTrainings, occupationTrainings, shootingTests) {
                    var $ctrl = this;
                    $ctrl.profile = profile;
                    $ctrl.profile.address = $ctrl.profile.address || {};
                    $ctrl.isUserOwnProfile = isUserOwnProfile;
                    $ctrl.clubInvitations = clubInvitations;
                    $ctrl.jhtTrainings = jhtTrainings;
                    $ctrl.occupationTrainings = occupationTrainings;
                    $ctrl.shootingTests = shootingTests;
                    $ctrl.personId = personId;
                    $ctrl.huntingLeaderOccupations = huntingLeaderOccupations;

                    $ctrl.saveAddress = function (address) {
                        return Account.updateAddress({
                            personId: personId
                        }, address).$promise;
                    };

                    $ctrl.saveOtherInfo = function (otherInfo) {
                        return Account.updateOtherInfo({
                            personId: personId
                        }, otherInfo).$promise;
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
            },
            controller: function ($state, $uibModal, Account, NotificationService, OccupationContactInfoVisibilityRules) {
                var $ctrl = this;

                $ctrl.editContactInfoSettings = function () {
                    var modalPromise = $uibModal.open({
                        size: 'lg',
                        templateUrl: 'account/edit_contact_info_settings.html',
                        controller: ModalController,
                        controllerAs: '$ctrl',
                        bindToController: true,
                        resolve: {
                            occupations: _.constant($ctrl.occupations),
                            occupationContactInfoVisibilityRules: OccupationContactInfoVisibilityRules.get().$promise
                        }
                    }).result.then(function () {
                        var contactInfoVisibility = _.map($ctrl.occupations, function (occupation) {
                            return _.pick(occupation,
                                ['id', 'nameVisibility', 'phoneNumberVisibility', 'emailVisibility']);
                        });
                        return Account.contactInfoVisibility(contactInfoVisibility).$promise;
                    });

                    NotificationService.handleModalPromise(modalPromise)
                        .then(function () {
                            NotificationService.showDefaultSuccess();
                        })
                        .finally(function () {
                            $state.reload();
                        });
                };

                function ModalController($uibModalInstance, occupations, occupationContactInfoVisibilityRules) {
                    var $modalCtrl = this;
                    $modalCtrl.occupations = occupations;
                    $modalCtrl.occupationContactInfoVisibilityRules = occupationContactInfoVisibilityRules;

                    $modalCtrl.save = function () {
                        $uibModalInstance.close();
                    };

                    $modalCtrl.cancel = function () {
                        $uibModalInstance.dismiss('cancel');
                    };

                    $modalCtrl.getVisibilityRule = function (occupation) {
                        return $modalCtrl.occupationContactInfoVisibilityRules[occupation.organisation.organisationType][occupation.occupationType];
                    };

                    $modalCtrl.isAnyEditable = function (occupation) {
                        var rule = $modalCtrl.getVisibilityRule(occupation);
                        return rule.nameVisibility === 'OPTIONAL' ||
                            rule.phoneNumberVisibility === 'OPTIONAL' ||
                            rule.emailVisibility === 'OPTIONAL';
                    };

                    $modalCtrl.isAnyEditableFalse = function (occupation) {
                        var rule = $modalCtrl.getVisibilityRule(occupation);
                        return rule.nameVisibility === 'OPTIONAL' && occupation.nameVisibility === false ||
                            rule.phoneNumberVisibility === 'OPTIONAL' && occupation.phoneNumberVisibility === false ||
                            rule.emailVisibility === 'OPTIONAL' && occupation.emailVisibility === false;
                    };

                    $modalCtrl.toggleSelection = function (occupation) {
                        var anyFalse = $modalCtrl.isAnyEditableFalse(occupation);
                        setAllEditableVisibilities(occupation, anyFalse);
                    };

                    $modalCtrl.onNameVisibilityChange = function (occupation) {
                        if (!occupation.nameVisibility) {
                            setAllEditableVisibilities(occupation, false);
                        }
                    };

                    function setAllEditableVisibilities(occupation, visibility) {
                        var rule = $modalCtrl.getVisibilityRule(occupation);

                        if (rule.nameVisibility === 'OPTIONAL') {
                            occupation.nameVisibility = visibility;
                        }
                        if (rule.phoneNumberVisibility === 'OPTIONAL') {
                            occupation.phoneNumberVisibility = visibility;
                        }
                        if (rule.emailVisibility === 'OPTIONAL') {
                            occupation.emailVisibility = visibility;
                        }
                    }
                }
            }
        })
        .component('accountProfileTrainings', {
            templateUrl: 'account/profile/profile_trainings.html',
            bindings: {
                jhtTrainings: '<',
                occupationTrainings: '<'
            },
            controllerAs: '$ctrl',
            controller: function () {
                var $ctrl = this;

                $ctrl.showTrainings = function () {
                    return $ctrl.jhtTrainings && $ctrl.jhtTrainings.length ||
                        $ctrl.occupationTrainings && $ctrl.occupationTrainings.length;
                };
            }
        })
        .component('accountProfileShootingTests', {
            templateUrl: 'account/profile/profile_shooting_test.html',
            bindings: {
                shootingTests: '<'
            }
        })
        .component('accountProfileOther', {
            templateUrl: 'account/profile/profile_other.html',
            bindings: {
                profile: '<',
                onSave: '&'
            },
            controller: function ($uibModal, $state, NotificationService) {
                var $ctrl = this;

                $ctrl.editOtherInfo = function () {
                    var profile = $ctrl.profile;
                    var otherInfo = {
                        email: profile.email,
                        byName: profile.byName,
                        phoneNumber: profile.phoneNumber,
                        denyAnnouncementEmail: profile.denyAnnouncementEmail
                    };
                    showModal(otherInfo, profile.registered);
                };

                function showModal(otherInfo, registered) {
                    var modalPromise = $uibModal.open({
                        templateUrl: 'account/edit_otherinfo.html',
                        controllerAs: '$ctrl',
                        controller: ModalController,
                        resolve: {
                            otherInfo: _.constant(otherInfo),
                            registered: _.constant(registered)
                        }
                    }).result.then(function () {
                        return $ctrl.onSave({otherInfo: otherInfo});
                    });

                    NotificationService.handleModalPromise(modalPromise).then(function () {
                        NotificationService.showDefaultSuccess();
                        $state.reload();
                    });
                }

                function ModalController($uibModalInstance, otherInfo, registered) {
                    var $modalCtrl = this;
                    $modalCtrl.otherInfo = otherInfo;
                    $modalCtrl.registered = registered;

                    $modalCtrl.save = function () {
                        $uibModalInstance.close();
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

                $ctrl.$onInit = function () {
                    $ctrl.pdfOptions = AccountService.getPdfOptions($ctrl.profile);
                    $ctrl.pdfSelection = $ctrl.pdfOptions[0];
                };

                $ctrl.printPdf = function () {
                    if ($ctrl.pdfSelection) {
                        $window.open($ctrl.pdfSelection.url);
                    }
                };

            }
        })
        .component('accountProfileForeignHunter', {
            templateUrl: 'account/profile/profile_hunter_foreign.html',
            bindings: {
                profile: '<'
            },
            controller: function ($window, AccountService) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.pdfOptions = AccountService.getPdfOptions($ctrl.profile);
                    $ctrl.pdfSelection = $ctrl.pdfOptions[0];
                };

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
                onSave: '&'
            },
            controller: function ($uibModal, $state, NotificationService) {
                var $ctrl = this;

                $ctrl.editAddress = function () {
                    var a = $ctrl.address;

                    showModal({
                        streetAddress: a.streetAddress,
                        postalCode: a.postalCode,
                        city: a.city,
                        country: a.country
                    });
                };

                function showModal(address) {
                    var modalPromise = $uibModal.open({
                        templateUrl: 'account/edit_address.html',
                        controllerAs: '$ctrl',
                        controller: ModalController,
                        resolve: {
                            address: _.constant(address)
                        }
                    }).result.then(function () {
                        return $ctrl.onSave({address: address});
                    });

                    NotificationService.handleModalPromise(modalPromise).then(function () {
                        NotificationService.showDefaultSuccess();
                        $state.reload();
                    });
                }

                function ModalController($uibModalInstance, address) {
                    var $modalCtrl = this;
                    $modalCtrl.address = address;

                    $modalCtrl.save = function () {
                        $uibModalInstance.close();
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
        .component('accountProfileUnregisterUserAccount', {
            templateUrl: 'account/profile/profile_unregister_user_account.html',
            bindings: {
                profile: '<',
                isUserOwnProfile: '<'
            },
            controller: function ($translate, $uibModal, $state, Account, NotificationService,
                                  ConfirmationDialogService, Helpers) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    var profile = $ctrl.profile;
                    var unregisterRequestedTime = profile.unregisterRequestedTime || null;
                    $ctrl.unregistrationRequested = unregisterRequestedTime !== null;

                    if (unregisterRequestedTime) {
                        var datetime = Helpers.toMoment(unregisterRequestedTime, 'YYYY-MM-DD[T]HH:mm:ss.SSS');

                        $ctrl.requestFormattedDate = Helpers.dateToString(datetime, "DD.MM.YYYY");
                        $ctrl.requestFormattedTime = Helpers.dateToString(datetime, "HH:mm");
                    }
                };

                $ctrl.unregister = function () {
                    var modalPromise = $uibModal.open({
                        templateUrl: 'account/unregister_account.html',
                        controller: ModalController,
                        controllerAs: '$ctrl'
                    }).result.then(function () {
                        return Account.unregister({
                            personId: $ctrl.profile.personId
                        }).$promise;
                    });

                    NotificationService.handleModalPromise(modalPromise).then(function () {
                        NotificationService.showDefaultSuccess();
                        $state.reload();
                    });
                };

                $ctrl.continueUsingService = function () {
                    ConfirmationDialogService.showConfirmationDialogWithPrimaryAccept(
                        'global.dialog.confirmation.title',
                        'global.dialog.confirmation.text')
                        .then(function () {
                            Account.cancelUnregister({ personId: $ctrl.profile.personId })
                                .$promise
                                .then(function () {
                                    NotificationService.showDefaultSuccess();
                                    $state.reload();
                                });
                        });
                };

                function ModalController($uibModalInstance) {
                    var $modalCtrl = this;

                    $modalCtrl.cancel = function () {
                        $uibModalInstance.dismiss('cancel');
                    };

                    $modalCtrl.unregister = $uibModalInstance.close;
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
                        NotificationService.showDefaultSuccess();
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
                huntingLeaderOccupations: '<',
                isUserOwnProfile: '<',
                personId: '<'
            },
            controller: function ($uibModal, $state, AccountService, ActiveRoleService, NotificationService) {
                var $ctrl = this;

                function ok(clubId) {
                    if (ActiveRoleService.isModerator()) {
                        NotificationService.showDefaultSuccess();
                        $state.go('club.main', {id: clubId});
                    } else {
                        AccountService.updateRoles().finally(function () {
                            $state.go('roleselection');
                        });
                    }
                }

                $ctrl.createClub = function () {
                    $uibModal.open({
                        size: 'lg',
                        templateUrl: 'account/club_create.html',
                        controller: 'AccountClubCreateController',
                        controllerAs: 'modalCtrl',
                        bindToController: true,
                        resolve: {
                            personId: _.constant($ctrl.isUserOwnProfile ? null : $ctrl.personId)
                        }
                    }).result.then(function (club) {
                        ok(club.id);
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
        .component('accountProfileFeatureActivation', {
            templateUrl: 'account/profile/profile_feature_activation.html',
            bindings: {
                profile: '<'
            },
            controller: function ($translate, Account, AccountService, AuthenticationService, NotificationService,
                                  dialogs) {

                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.srvaEnabled = $ctrl.profile.enableSrva;
                    $ctrl.shootingTestsEnabled = $ctrl.profile.enableShootingTests;

                    $ctrl.shootingTestActivationVisible = _.some($ctrl.profile.occupations, function (occ) {
                        return occ.occupationType === 'AMPUMAKOKEEN_VASTAANOTTAJA';
                    });
                };

                function toggleActivationOfSrvaFeature(enable) {
                    var method = enable ? Account.activateSrvaFeature : Account.deactivateSrvaFeature;

                    method({id: $ctrl.profile.personId}).$promise
                        .then(AuthenticationService.reloadAuthentication)
                        .then(
                            function () {
                                NotificationService.showDefaultSuccess();
                                $ctrl.srvaEnabled = enable;
                            },
                            NotificationService.showDefaultFailure);
                }

                function toggleActivationOfShootingTestFeature(enable) {
                    var method = enable ? Account.activateShootingTestFeature : Account.deactivateShootingTestFeature;

                    method({id: $ctrl.profile.personId}).$promise
                        .then(AccountService.updateRoles)
                        .then(function () {
                                NotificationService.showDefaultSuccess();
                                $ctrl.shootingTestsEnabled = enable;
                            },
                            NotificationService.showDefaultFailure);
                }

                $ctrl.enableSrva = function () {
                    var dialogTitle = $translate.instant('global.dialog.confirmation.title');
                    var dialogMessage = $translate.instant('account.profile.srva.activateConfirmation');

                    return dialogs.confirm(dialogTitle, dialogMessage).result.then(function () {
                        toggleActivationOfSrvaFeature(true);
                    });
                };

                $ctrl.disableSrva = function () {
                    toggleActivationOfSrvaFeature(false);
                };

                $ctrl.enableShootingTests = function () {
                    var dialogTitle = $translate.instant('global.dialog.confirmation.title');
                    var dialogMessage = $translate.instant('account.profile.shootingTests.activateConfirmation');

                    return dialogs.confirm(dialogTitle, dialogMessage).result.then(function () {
                        toggleActivationOfShootingTestFeature(true);
                    });
                };

                $ctrl.disableShootingTests = function () {
                    toggleActivationOfShootingTestFeature(false);
                };
            }
        })
    ;
})();
