(function () {
    'use strict';

    angular.module('app.account.twofactor', [])

        .directive('qrCode', function ($parse) {
            return {
                replace: false,
                restrict: 'A',
                scope: false,
                link: function ($scope, $element, $attrs) {
                    $scope.qrElement = new QRCode($element[0], {
                        text: '',
                        width: 256,
                        height: 256,
                        correctLevel: QRCode.CorrectLevel.H
                    });

                    var modelGetter = $parse($attrs.qrCode);

                    $scope.$watch(function () {
                        return modelGetter($scope);
                    }, function (modelValue) {
                        $scope.qrElement.clear();
                        $scope.qrElement.makeCode(modelValue);
                    });
                }
            };
        })

        .component('enableTwoFactorAuthenticationButton', {
            template: '<div ng-show="$ctrl.isVisible()">' +
            '<a class="btn btn-primary" ng-show="$ctrl.currentMode === \'SMS\'"' +
            ' ng-click="$ctrl.enable()">Aktivoi Google Authenticator</a>' +
            '<a class="btn btn-primary" ng-show="$ctrl.currentMode === \'OFFLINE\'"' +
            ' ng-click="$ctrl.disable()">Deaktivoi Google Authenticator</a>' +
            '</div>',
            controller: function (TwoFactorAuthenticationSettings, NotificationService, ActiveRoleService) {
                var $ctrl = this;
                $ctrl.currentMode = null;

                TwoFactorAuthenticationSettings.getCurrentMode().then(function (currentMode) {
                    $ctrl.currentMode = currentMode;
                });

                $ctrl.isVisible = function () {
                    return ActiveRoleService.isModerator() || ActiveRoleService.isAdmin();
                };

                $ctrl.enable = function () {
                    handleResultPromise(TwoFactorAuthenticationSettings.showModal());
                };

                $ctrl.disable = function () {
                    handleResultPromise(TwoFactorAuthenticationSettings.disable());
                };

                function handleResultPromise(promise) {
                    promise.then(function (currentMode) {
                        $ctrl.currentMode = currentMode;
                        NotificationService.showDefaultSuccess();

                    }, function (error) {
                        if (error !== 'cancel' && error !== 'escape key press') {
                            NotificationService.showDefaultFailure();
                        }
                    });
                }
            }
        })

        .service('TwoFactorAuthenticationSettings', function ($uibModal, $http) {
            this.getCurrentMode = function () {
                return $http.get('/api/v1/account/twofactor').then(function (response) {
                    return response.data.twoFactorAuthentication;
                });
            };

            this.disable = function () {
                return $http.post('/api/v1/account/twofactor', {
                    twoFactorAuthentication: 'SMS'
                }).then(function (response) {
                    return response.data.twoFactorAuthentication;
                });
            };

            this.showModal = function () {
                return $uibModal.open({
                    size: 'sm',
                    templateUrl: 'account/twofactor/enable_two_factor.html',
                    controller: ModalController,
                    controllerAs: '$ctrl',
                    bindToController: true,
                    resolve: {
                        twoFactorCodeUrl: function () {
                            return $http.get('/api/v1/account/twofactor').then(function (response) {
                                return response.data.twoFactorCodeUrl;
                            });
                        }
                    }
                }).result;
            };

            function ModalController($uibModalInstance, twoFactorCodeUrl) {
                var $modalCtrl = this;

                $modalCtrl.twoFactorCodeUrl = twoFactorCodeUrl;
                $modalCtrl.twoFactorCode = '';
                $modalCtrl.errorMessage = null;

                $modalCtrl.save = function () {
                    $modalCtrl.errorMessage = null;

                    $http.post('/api/v1/account/twofactor', {
                        twoFactorAuthentication: 'OFFLINE',
                        twoFactorCode: $modalCtrl.twoFactorCode

                    }).then(function (response) {
                        var currentMode = response.data.twoFactorAuthentication;

                        if (currentMode === 'OFFLINE') {
                            $uibModalInstance.close(currentMode);
                            return;
                        }

                        $modalCtrl.errorMessage = 'Väärä koodi';

                    }, function () {
                        $uibModalInstance.dismiss();
                    });
                };

                $modalCtrl.cancel = function () {
                    $uibModalInstance.dismiss('cancel');
                };
            }
        });

})();
