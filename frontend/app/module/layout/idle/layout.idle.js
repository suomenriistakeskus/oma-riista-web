(function () {
    'use strict';

    angular.module('app.layout.idle', [])
        .controller('IdleController', IdleController);

    function IdleController($rootScope, $state, $scope, $uibModal,
                            Keepalive, Idle, Title,
                            UnsavedChangesConfirmationService) {
        var originalTitle = Title.value();

        Title.idleMessage(originalTitle);
        Title.timedOutMessage(originalTitle);

        function closeModals() {
            if ($scope.warning) {
                $scope.warning.dismiss();
                $scope.warning = null;
            }

            if ($scope.timedout) {
                $scope.timedout.dismiss();
                $scope.timedout = null;
            }
        }

        $scope.$on('IdleStart', function () {
            closeModals();

            $scope.warning = $uibModal.open({
                templateUrl: 'layout/idle/warning-dialog.html',
                windowClass: 'modal-danger'
            });
        });

        $scope.$on('IdleEnd', function () {
            closeModals();
        });

        $scope.$on('IdleTimeout', function () {
            $rootScope.$broadcast('event:auth-loginCancelled');

            closeModals();

            $scope.timedout = $uibModal.open({
                templateUrl: 'layout/idle/timedout-dialog.html',
                windowClass: 'modal-danger'
            });

            $scope.timedout.result.finally(function () {
                closeModals();

                // Prevent opening another warning dialog on navigation
                UnsavedChangesConfirmationService.setChanges(false);

                $state.go('login');
            });
        });

        $scope.$on('event:auth-loginConfirmed', function (event, account) {
            if (account && !account.rememberMe) {
                Idle.watch();
            } else {
                Idle.unwatch();
            }

            Keepalive.start();
        });

        $scope.$on('event:auth-loginRequired', function () {
            Idle.unwatch();
        });

        $scope.$on('event:auth-loginCancelled', function () {
            Idle.unwatch();
        });
    }
})();
