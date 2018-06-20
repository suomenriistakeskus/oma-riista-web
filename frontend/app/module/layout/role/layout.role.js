'use strict';

angular.module('app.layout.role', [])
    .directive('navActiveRole', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: true,
            bindToController: true,
            templateUrl: 'layout/role/nav-active-role.html',
            controllerAs: '$ctrl',
            controller: function ($state, ActiveRoleService) {
                var $ctrl = this;

                $ctrl.goHome = function () {
                    var roleCount = _.size(ActiveRoleService.getAvailableRoles());

                    $state.go(roleCount <= 1 ? 'main' : 'roleselection');
                };
            }
        };
    })
    .directive('navChangeRole', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: true,
            bindToController: true,
            templateUrl: 'layout/role/nav-change-role.html',
            controllerAs: '$ctrl',
            controller: function ($state, ActiveRoleService, MapState) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.selectedRole = ActiveRoleService.getActiveRole;
                    $ctrl.roles = ActiveRoleService.getAvailableRoles;
                    $ctrl.getRoleDisplayName = ActiveRoleService.getRoleDisplayName;
                    $ctrl.getRoleLogo = ActiveRoleService.getRoleLogo;
                };

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
        };
    })

    .service('ActiveRoleService', function ActiveRoleService(LocalStorageService, $filter) {
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
            if (!role) {
                return null;
            }
            if (role.displayName) {
                return role.displayName;
            }
            if (role.type === 'PERMIT') {
                var permitType = role.context.permitType || '';
                var permitNumber = role.context.permitNumber || '';
                return permitType + ' ' + permitNumber.substring(0,4);
            }
            if (role.context) {
                var i18nFilter = $filter('rI18nNameFilter');
                return i18nFilter(role.context);
            }
            return '';
        };

        this.getRoleLogo = function (role) {
            if (!role) {
                return null;
            }

            switch (role.type) {
                case 'SEURAN_JASEN':
                case 'SEURAN_YHDYSHENKILO':
                    return 'fa-group';
                case 'PERMIT':
                    return 'fa-file-text';
                default:
                    return 'fa-user';
            }
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

        this.isShootingTestOfficial = function () {
            return activeRole && ('AMPUMAKOKEEN_VASTAANOTTAJA' === activeRole.type);
        };

        this.isClubContact = function () {
            return activeRole && ('SEURAN_YHDYSHENKILO' === activeRole.type);
        };

        this.getActiveOccupationId = function () {
            if (activeRole && activeRole.id) {
                var activeOccupationId = _.parseInt(activeRole.id.split(':')[1]);
                return activeOccupationId || null;
            }
            return null;
        };
    });
