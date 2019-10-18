'use strict';

angular.module('app.layout.controllers', [])
    .directive('siteNav', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: true,
            bindToController: true,
            controllerAs: '$ctrl',
            templateUrl: 'layout/nav.html',
            controller: function (AuthenticationService, ActiveRoleService) {
                var $ctrl = this;

                $ctrl.isAuthenticated = AuthenticationService.isAuthenticated;
                $ctrl.isUser = ActiveRoleService.isUser;
                $ctrl.isAdmin = ActiveRoleService.isAdmin;
                $ctrl.isAdminOrModerator = ActiveRoleService.isModerator;
            }
        };
    })

    .controller('DatePickerController', function ($scope, Helpers) {
        $scope.isDatePickerOpen = false;
        $scope.toggleDatePopup = function ($event, formState, fieldName) {
            $event.preventDefault();
            $event.stopPropagation();
            $scope.isDatePickerOpen = !$scope.isDatePickerOpen;
        };
        $scope.today = function () {
            return Helpers.dateToString(new Date());
        };
    });
