'use strict';

angular.module('app.account.services', ['ngResource'])
    .factory('Account', function ($resource, CacheFactory) {
        return $resource('/api/v1/account/:id', {"id": "@id"}, {
            updateOther: {url: '/api/v1/account/other', method: 'PUT'},
            updateSelf: {url: '/api/v1/account', method: 'PUT'},
            deactivate: {url: '/api/v1/account/deactivate', params: {personId: '@personId'}, method: 'POST'},
            srvaEnable: {url: '/api/v1/account/srva/enable', method: 'PUT'},
            srvaDisable: {url: '/api/v1/account/srva/disable', method: 'PUT'},
            contactShare: {
                method: 'PUT',
                url: 'api/v1/account/contactshare'
            },
            clubInvitations: {
                method: 'GET',
                url: 'api/v1/account/invitation',
                isArray: true
            },
            countTodo: {
                url: 'api/v1/account/todocount',
                method: 'GET',
                cache: CacheFactory.get('accountTodoCountCache')
            },
            countSrvaTodo: {
                url: 'api/v1/account/srvatodocount',
                params: {rhyId: '@rhyId'},
                method: 'GET',
                cache: CacheFactory.get('accountSrvaTodoCountCache')
            }
        });
    })
    .service('AccountService', function ($rootScope, $translate,
                                         Account, ActiveRoleService, HuntingYearService) {
        this.updateRoles = function () {
            return Account.get().$promise.then(function (account) {
                // Whenever user updates his account and state reload is called, then this is
                // resolved, so we update users account information now when we have fresh data
                ActiveRoleService.updateRoles(account);
                $rootScope.account = account;
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

            _.each(account.huntingPaymentPdfYears, function (huntingYear) {
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
    });
