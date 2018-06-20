'use strict';

angular.module('app.layout.controllers', [])
    .config(function ($stateProvider) {
            $stateProvider
                .state('main', {
                    url: '/',
                    controller: 'MainController'
                });
        }
    )
    .controller('MainController', function ($scope, ActiveRoleService, $state) {
        $scope.selectedRole = ActiveRoleService.getActiveRole();

        if ($scope.selectedRole) {
            if ($scope.selectedRole.type === 'PERMIT') {
                $state.go('permitmanagement.dashboard', {permitId: $scope.selectedRole.context.permitId});
            } else if ($scope.selectedRole.type === 'ROLE_ADMIN') {
                $state.go('admin.home');
            } else if ($scope.selectedRole.type === 'ROLE_MODERATOR') {
                $state.go('jht.home');
            } else if ($scope.selectedRole.type === 'SRVA_YHTEYSHENKILO') {
                $state.go('srva.list', {id: $scope.selectedRole.context.rhyId});
            } else if ($scope.selectedRole.type === 'TOIMINNANOHJAAJA') {
                $state.go('rhy.show', {id: $scope.selectedRole.context.rhyId});
            } else if ($scope.selectedRole.type === 'AMPUMAKOKEEN_VASTAANOTTAJA') {
                $state.go('rhy.shootingtest.events', {id: $scope.selectedRole.context.rhyId});
            } else if ($scope.selectedRole.context.clubId) {
                $state.go('club.main', {id: $scope.selectedRole.context.clubId});
            } else {
                $state.go('profile.diary', {id: 'me'});
            }
        }
    })
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
