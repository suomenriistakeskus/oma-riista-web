'use strict';

angular.module('app.account.services', ['ngResource'])
    .factory('Account', function ($resource, CacheFactory) {
        return $resource('/api/v1/account/:id', {"id": "@id"}, {
            updateAddress: {
                method: 'PUT',
                url: '/api/v1/account/:personId/address',
                params: {personId: '@personId'}
            },
            updateOtherInfo: {
                method: 'PUT',
                url: '/api/v1/account/:personId/other',
                params: {personId: '@personId'}
            },
            deactivate: {url: '/api/v1/account/deactivate', params: {personId: '@personId'}, method: 'POST'},
            unregister: {
                method: 'POST',
                url: '/api/v1/account/unregister',
                params: {personId: '@personId'}
            },
            cancelUnregister: {
                method: 'POST',
                url: '/api/v1/account/cancel-unregister',
                params: {personId: '@personId'}
            },
            activateSrvaFeature: {url: '/api/v1/account/:id/srva/enable', method: 'PUT'},
            deactivateSrvaFeature: {url: '/api/v1/account/:id/srva/disable', method: 'PUT'},
            activateShootingTestFeature: {url: '/api/v1/account/:id/shootingtests/enable', method: 'PUT'},
            deactivateShootingTestFeature: {url: '/api/v1/account/:id/shootingtests/disable', method: 'PUT'},
            contactShare: {
                method: 'PUT',
                url: 'api/v1/account/contactshare'
            },
            contactShareAndVisibility: {
                method: 'PUT',
                url: 'api/v1/account/contactshare-and-visibility'
            },
            clubInvitations: {
                method: 'GET',
                url: 'api/v1/account/invitation',
                isArray: true
            },
            countPermitTodo: {
                url: 'api/v1/account/permittodocount',
                method: 'GET',
                cache: CacheFactory.get('accountPermitTodoCountCache')
            },
            countInvitationTodo: {
                url: 'api/v1/account/invitationtodocount',
                method: 'GET',
                cache: CacheFactory.get('accountInvitationTodoCountCache')
            },
            countSrvaTodo: {
                url: 'api/v1/account/srvatodocount',
                params: {rhyId: '@rhyId'},
                method: 'GET',
                cache: CacheFactory.get('accountSrvaTodoCountCache')
            },
            countShootingTestTodo: {
                url: 'api/v1/account/shootingtesttodocount',
                params: {rhyId: '@rhyId'},
                method: 'GET',
                cache: CacheFactory.get('accountShootingTestTodoCountCache')
            },
            countTaxationTodo: {
                url: 'api/v1/account/taxationtodocount',
                params: {rhyId: '@rhyId'},
                method: 'GET',
                cache: CacheFactory.get('accountTaxationTodoCountCache')
            },
            countHuntingControlEventTodo: {
                url: 'api/v1/account/huntingcontroleventtodocount',
                params: {rhyId: '@rhyId'},
                method: 'GET'
            },
            shootingTests: {
                method: 'GET',
                url: 'api/v1/account/shootingtests',
                params: {personId: '@personId'},
                isArray: true
            },
            contactInfoVisibility: {
                method: 'PUT',
                url: 'api/v1/account/occupation-contact-info-visibility'
            },
            occupationTrainings: {
                method: 'GET',
                url: 'api/v1/account/occupation-trainings',
                params: {personId: '@personId'},
                isArray: true
            }
        });
    })
    .service('AccountService', function ($translate, Account, AvailableRoleService, AuthenticationService,
                                         HuntingYearService) {

        this.updateRoles = function () {
            return Account.get().$promise.then(function (account) {
                // Whenever user updates his account and state reload is called, then this is
                // resolved, so we update users account information now when we have fresh data

                AvailableRoleService.updateAvailableRoles(account);
                return account;
            });
        };

        this.loadAccount = function (accountId) {
            if (accountId === "me") {
                return this.updateRoles();
            } else {
                return Account.get({id: accountId}).$promise;
            }
        };

        this.isSrvaFeatureEnabled = function () {
            var account = AuthenticationService.getAuthentication();
            return account && !!account.enableSrva;
        };

        this.isShootingTestFeatureEnabled = function () {
            var account = AuthenticationService.getAuthentication();
            return account && !!account.enableShootingTests;
        };

        this.isAccountUnregistrationRequested = function () {
            return Account.get().$promise.then(function (account) {
                return account.unregisterRequestedTime;
            });
        };

        this.getPdfOptions = function (account) {
            function _certificateOption(type, lang) {
                return {
                    title: $translate.instant('account.profile.pdf.' + type + '.' + lang),
                    url: '/api/v1/certificate/' + account.hunterNumber + '/' + type + '.pdf?lang=' + lang
                };
            }

            var result = [];

            if (account.allowPrintCertificate) {
                Array.prototype.unshift.apply(result, [
                    _certificateOption('huntingCard', 'fi'),
                    _certificateOption('huntingCard', 'sv'),
                    _certificateOption('foreign', 'en'),
                    _certificateOption('foreign', 'de')
                ]);
            }

            _.forEach(account.huntingPaymentPdfYears, function (huntingYear) {
                result.unshift({
                    title: $translate.instant('account.profile.pdf.huntingPayment') +
                    ' ' + HuntingYearService.toStr(huntingYear),
                    url: '/api/v1/account/' + account.personId + '/payment/' + huntingYear
                });
            });

            return result;
        };
    })
    .factory('Password', function ($resource) {
        return $resource('/api/v1/account/password', {}, {});
    })
    .service('AccountBeingUnregisteredNotifier', function ($uibModal, AccountService, Helpers) {
        var lastNotification = null;

        var isCoolingDown = function () {
            var now = new Date();
            if (lastNotification) {
                var secondsSinceLastNotification = (now.getTime() - lastNotification.getTime()) / 1000;
                if (secondsSinceLastNotification < 15) {
                    return true;
                }
            }
            lastNotification = now;
            return false;
        };

        this.notifyAccountUnregistration = function () {
            AccountService.isAccountUnregistrationRequested().then(function (unregisterRequestedTime) {
                if (!unregisterRequestedTime) {
                    return;
                }

                if (isCoolingDown()) {
                    return;
                }

                $uibModal.open({
                    templateUrl: 'account/account_being_unregistered_notification.html',
                    controllerAs: "$ctrl",
                    controller: function () {
                        var $ctrl = this;

                        var datetime = Helpers.toMoment(unregisterRequestedTime, 'YYYY-MM-DD[T]HH:mm:ss.SSS');

                        $ctrl.requestFormattedDate = Helpers.dateToString(datetime, "DD.MM.YYYY");
                        $ctrl.requestFormattedTime = Helpers.dateToString(datetime, "HH:mm");
                    }
                });
            });
        };
    });
