(function () {
    'use strict';

    angular.module('app.layout.role', [])
        .service('ActiveRoleService', ActiveRoleService)
        .controller('ChangeRoleController', ChangeRoleController)
        .directive('navActiveRole', function () {
            return {
                restrict: 'E',
                replace: true,
                scope: true,
                bindToController: true,
                controllerAs: '$ctrl',
                controller: ActiveRoleController,
                templateUrl: 'layout/role/nav-active-role.html'
            };
        })
        .directive('navChangeRole', function () {
            return {
                restrict: 'E',
                replace: true,
                scope: true,
                bindToController: true,
                controllerAs: '$ctrl',
                controller: ChangeRoleController,
                templateUrl: 'layout/role/nav-change-role.html'
            };
        });

    function ActiveRoleService(LocalStorageService, $filter) {
        var self = this;
        var availableRoles = [];
        var activeRole = null;

        function getDefaultRoleFromAvailable() {
            var storedValue = LocalStorageService.getKey('activeRole');
            if (storedValue) {
                return angular.fromJson(storedValue);
            }
            if (angular.isArray(availableRoles) && availableRoles.length > 0) {
                return availableRoles[0];
            }
            return false;
        }

        function persistSelection() {
            LocalStorageService.setKey('activeRole', activeRole ? angular.toJson(activeRole) : null);
        }

        this.getAvailableRoles = function () {
            return availableRoles || [];
        };

        this.getRoleDisplayName = function (role) {
            if (role) {
                if (role.displayName) {
                    return role.displayName;
                }
                if (role.context) {
                    var i18nFilter = $filter('rI18nNameFilter');
                    return i18nFilter(role.context);
                }
            }
            return null;
        };

        this.getActiveRole = function () {
            return activeRole;
        };

        this.selectActiveRole = function (role) {
            activeRole = role;
            persistSelection();
        };

        this.updateRoles = function (account) {
            availableRoles = account.accountRoles || [];

            //If logged in user does not have SRVA enabled, then filter out SRVA_YHTEYSHENKILO role.
            //This should be removed when SRVA services officially published.
            if (!account.enableSrva) {
                availableRoles = _.filter(availableRoles, function (role) {
                    return role.type !== 'SRVA_YHTEYSHENKILO';
                });
            }

            activeRole = getDefaultRoleFromAvailable();
            persistSelection();
        };

        this.clearActiveRole = function () {
            availableRoles = [];
            activeRole = null;
            persistSelection();
        };

        this.isUser = function () {
            return !self.isModerator();
        };

        this.isModerator = function () {
            return activeRole && ('ROLE_MODERATOR' === activeRole.type || 'ROLE_ADMIN' === activeRole.type);
        };

        this.isAdmin = function () {
            return activeRole && ('ROLE_ADMIN' === activeRole.type);
        };

        this.isCoordinator = function () {
            return activeRole && ('TOIMINNANOHJAAJA' === activeRole.type);
        };

        this.isSrvaContactPerson = function () {
            return activeRole && ('SRVA_YHTEYSHENKILO' === activeRole.type);
        };

        this.isClubContact = function () {
            return activeRole && ('SEURAN_YHDYSHENKILO' === activeRole.type);
        };
    }

    function ActiveRoleController($state, ActiveRoleService) {
        var $ctrl = this;

        $ctrl.goHome = function () {
            var roleCount = _.size(ActiveRoleService.getAvailableRoles());

            $state.go(roleCount <= 1 ? 'main' : 'roleselection');
        };
    }

    function ChangeRoleController($state, ActiveRoleService, MapState) {
        var $ctrl = this;

        $ctrl.selectedRole = ActiveRoleService.getActiveRole;
        $ctrl.roles = ActiveRoleService.getAvailableRoles;
        $ctrl.getRoleDisplayName = ActiveRoleService.getRoleDisplayName;

        // Update selected role in view on change
        $ctrl.selectRole = function (role) {
            ActiveRoleService.selectActiveRole(role);
            MapState.reset();

            $state.go('main');
        };

        $ctrl.isSelectedRole = function (role) {
            var activeRole = ActiveRoleService.getActiveRole();
            return activeRole && activeRole.id === role.id;
        };
    }
})();
