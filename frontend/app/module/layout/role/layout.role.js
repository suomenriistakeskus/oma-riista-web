'use strict';

angular.module('app.layout.role', [])
    .config(function ($stateProvider) {
        $stateProvider.state('roleselection', {
            url: '/',
            templateUrl: 'layout/role/roleselection.html',
            controller: 'RoleSelectionController',
            controllerAs: '$ctrl',
            bindToController: true
        });
    })

    .service('AvailableRoleService', function () {
        var availableRoles = [];
        var privileges = [];

        this.getAvailableRoles = function () {
            return availableRoles;
        };

        this.updateAvailableRoles = function (account) {

            if (account && _.isArray(account.accountRoles)) {
                availableRoles = account.accountRoles;
            } else {
                // Do not create new array to avoid digest loops
                availableRoles.length = 0;
            }
            if (account && _.isArray(account.privileges)) {
                this.privileges = account.privileges;
            } else {
                // Do not create new array to avoid digest loops
                privileges.length = 0;
            }

            return availableRoles;
        };

        this.hasPrivilege = function (privilege) {
            return _.includes(this.privileges, privilege);
        };

        this.clearAvailableRoles = function () {
            // Do not create new array to avoid digest loops
            availableRoles.length = 0;
            privileges.length = 0;
        };

        this.getOnlyAvailableRole = function () {
            return _.size(availableRoles) === 1 ? availableRoles[0] : null;
        };

        this.findUserRole = function () {
            return _.find(availableRoles, {type: 'ROLE_USER'});
        };

        this.findModeratorRole = function () {
            return _.find(availableRoles, {type: 'ROLE_MODERATOR'});
        };

        this.findAdminRole = function () {
            return _.find(availableRoles, {type: 'ROLE_ADMIN'});
        };

        this.findRoleForPermitId = function (permitId) {
            return findRoleByContextProperty('permitId', permitId);
        };

        this.findRoleForClubId = function (clubId) {
            return findRoleByContextProperty('clubId', clubId);
        };

        this.findRoleForRhy = function (rhyId, type) {
            return _.find(availableRoles, function (role) {
                return role.type === type && role.context.rhyId === rhyId;
            });
        };

        function findRoleByContextProperty(propertyName, propertyValue) {
            return _.find(availableRoles, function (role) {
                return role.context[propertyName] === propertyValue;
            });
        }
    })

    .service('ActiveRoleService', function (AvailableRoleService) {
        var self = this;
        var activeRole = null;


        this.getActiveRole = function () {
            return activeRole;
        };

        this.selectActiveRole = function (role) {
            activeRole = role;
        };

        this.clearActiveRole = function () {
            activeRole = null;
        };

        this.getActiveOccupationId = function () {
            if (activeRole && activeRole.id) {
                var activeOccupationId = _.parseInt(activeRole.id.split(':')[1]);
                return activeOccupationId || null;
            }
            return null;
        };

        this.getActivePersonId = function () {
            return _.get(activeRole, 'context.personId');
        };

        this.isUser = function () {
            return !self.isModerator();
        };

        this.isModerator = function () {
            return activeRoleTypeIs('ROLE_MODERATOR') || activeRoleTypeIs('ROLE_ADMIN');
        };

        this.isAdmin = function () {
            return activeRoleTypeIs('ROLE_ADMIN');
        };

        this.isCoordinator = function () {
            return activeRoleTypeIs('TOIMINNANOHJAAJA');
        };

        this.isPrivilegedModerator = function (privilege) {
            return !!this.isAdmin() ||
                (this.isModerator() && AvailableRoleService.hasPrivilege(privilege));
        };

        this.isSrvaContactPerson = function () {
            return activeRoleTypeIs('SRVA_YHTEYSHENKILO');
        };

        this.isShootingTestOfficial = function () {
            return activeRoleTypeIs('AMPUMAKOKEEN_VASTAANOTTAJA');
        };

        this.isClubContact = function () {
            return activeRoleTypeIs('SEURAN_YHDYSHENKILO');
        };

        this.isGameWarden = function () {
            return activeRoleTypeIs('METSASTYKSENVALVOJA');
        };

        this.isClubGroupLeader = function () {
            return activeRoleTypeIs('RYHMAN_METSASTYKSENJOHTAJA');
        };

        function activeRoleTypeIs(type) {
            return activeRole && (type === activeRole.type);
        }
    })

    .service('LoginRedirectService', function ($state, AvailableRoleService, ActiveRoleService) {
        var self = this;

        this.redirectToDefault = function () {
            var onlyRole = AvailableRoleService.getOnlyAvailableRole();

            if (onlyRole) {
                ActiveRoleService.selectActiveRole(onlyRole);
                self.redirectToRoleDefaultState(onlyRole);

            } else {
                $state.go('roleselection');

                // Always try to show something as active role
                ActiveRoleService.selectActiveRole(AvailableRoleService.findUserRole());
            }
        };

        this.redirectToRoleDefaultState = function (selectedRole) {
            var roleContext = selectedRole.context || {};
            var defaultRoleState = getDefaultRoleState(selectedRole.type, roleContext);

            $state.go(defaultRoleState.name, defaultRoleState.params, {reload: true});
        };

        this.redirectToPendingState = function (state, params) {
            var role = findRoleForState(state, params);

            if (!role) {
                role = AvailableRoleService.findUserRole();
            }

            ActiveRoleService.selectActiveRole(role);

            $state.go(state, params);
        };

        function findRoleForState(state, params) {
            var onlyRole = AvailableRoleService.getOnlyAvailableRole();

            if (onlyRole) {
                return onlyRole;
            }

            if (_.startsWith(state, 'club')) {
                return AvailableRoleService.findRoleForClubId(_.parseInt(params.id));

            } else if (_.startsWith(state, 'permitmanagement')) {
                return AvailableRoleService.findRoleForPermitId(_.parseInt(params.permitId));

            } else if (_.startsWith(state, 'rhy.shootingtest')) {
                var primaryRole = AvailableRoleService.findRoleForRhy(_.parseInt(params.id), 'TOIMINNANOHJAAJA');

                if (primaryRole) {
                    return primaryRole;
                }

                return AvailableRoleService.findRoleForRhy(_.parseInt(params.id), 'AMPUMAKOKEEN_VASTAANOTTAJA');
            } else if (_.startsWith(state, 'rhy.huntingcontrolevent.gamewarden')) {
                return AvailableRoleService.findRoleForRhy(_.parseInt(params.id), 'METSASTYKSENVALVOJA');
            } else if (_.startsWith(state, 'rhy')) {
                return AvailableRoleService.findRoleForRhy(_.parseInt(params.id), 'TOIMINNANOHJAAJA');

            } else if (_.startsWith(state, 'srva')) {
                return AvailableRoleService.findRoleForRhy(_.parseInt(params.id), 'SRVA_YHTEYSHENKILO');

            } else if (_.startsWith(state, 'jht') || _.startsWith(state, 'reporting')) {
                return AvailableRoleService.findModeratorRole() || AvailableRoleService.findAdminRole();

            } else if (_.startsWith(state, 'admin')) {
                return AvailableRoleService.findAdminRole();
            } else {
                return AvailableRoleService.findUserRole();
            }
        }

        function getDefaultRoleState(roleType, roleContext) {
            switch (roleType) {
                case 'SEURAN_JASEN':
                case 'SEURAN_YHDYSHENKILO':
                case 'RYHMAN_METSASTYKSENJOHTAJA':
                case 'RYHMAN_JASEN':
                    return {name: 'club.main', params: {id: roleContext.clubId}};
                case 'TOIMINNANOHJAAJA':
                    return {name: 'rhy.show', params: {id: roleContext.rhyId}};
                case 'SRVA_YHTEYSHENKILO':
                    return {name: 'srva.list', params: {id: roleContext.rhyId}};
                case 'AMPUMAKOKEEN_VASTAANOTTAJA':
                    return {name: 'rhy.shootingtest.events', params: {id: roleContext.rhyId}};
                case 'PERMIT':
                    return {name: 'permitmanagement.dashboard', params: {permitId: roleContext.permitId}};
                case 'ROLE_ADMIN':
                    return {name: 'admin.home', params: {}};
                case 'ROLE_MODERATOR':
                    return {name: 'jht.home', params: {}};
                case 'METSASTYKSENVALVOJA':
                    return {name: 'rhy.huntingcontrolevent.gamewarden', params: {id: roleContext.rhyId}};
                default:
                    return {name: 'profile.diary', params: {id: 'me'}};
            }
        }
    })

    .service('RoleService', function ($filter) {
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

                return permitType + ' ' + permitNumber.substring(0, 4);
            }

            if (role.context) {
                var i18nFilter = $filter('rI18nNameFilter');
                return i18nFilter(role.context);
            }

            return null;
        };

        this.getRoleSubtitle = function (role) {
            return role.type === 'PERMIT' ? role.context.permitNumber : null;
        };

        this.getRoleLogo = function (role) {
            var roleType = role.type || '';

            switch (roleType) {
                case 'SEURAN_JASEN':
                case 'SEURAN_YHDYSHENKILO':
                    return 'fa-group';
                case 'PERMIT':
                    return 'fa-file-text';
                default:
                    return 'fa-user';
            }
        };
    })

    .controller('RoleSelectionController', function ($state, RoleService, AvailableRoleService,
                                                     ActiveRoleService, LoginRedirectService,
                                                     AccountBeingUnregisteredNotifier) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.availableRoles = AvailableRoleService.getAvailableRoles();
            $ctrl.getRoleLogo = RoleService.getRoleLogo;
            $ctrl.getRoleTitle = RoleService.getRoleDisplayName;
            $ctrl.getRoleSubtitle = RoleService.getRoleSubtitle;

            AccountBeingUnregisteredNotifier.notifyAccountUnregistration();
        };

        // Update selected role in view on change
        $ctrl.selectRole = function (role) {
            ActiveRoleService.selectActiveRole(role);
            LoginRedirectService.redirectToRoleDefaultState(role);
        };
    })

    .directive('navActiveRole', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: true,
            bindToController: true,
            templateUrl: 'layout/role/nav-active-role.html',
            controllerAs: '$ctrl',
            controller: function (LoginRedirectService) {
                var $ctrl = this;

                $ctrl.goHome = function () {
                    LoginRedirectService.redirectToDefault();
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
            controller: function ($state, RoleService, AvailableRoleService, ActiveRoleService,
                                  LoginRedirectService, LoginService) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.getActiveRole = ActiveRoleService.getActiveRole;
                    $ctrl.getAvailableRoles = AvailableRoleService.getAvailableRoles;
                    $ctrl.getRoleDisplayName = RoleService.getRoleDisplayName;
                    $ctrl.getRoleLogo = RoleService.getRoleLogo;
                };

                $ctrl.getActiveRoleLogo = function () {
                    var activeRole = ActiveRoleService.getActiveRole();
                    return activeRole ? RoleService.getRoleLogo(activeRole) : '';
                };

                $ctrl.getActiveRoleType = function () {
                    var activeRole = ActiveRoleService.getActiveRole();
                    return activeRole && activeRole.type ? activeRole.type : '';
                };

                $ctrl.getActiveRoleDisplayName = function () {
                    var activeRole = ActiveRoleService.getActiveRole();
                    return activeRole ? $ctrl.getRoleDisplayName(activeRole) : '';
                };

                $ctrl.selectRole = function (role) {
                    ActiveRoleService.selectActiveRole(role);
                    LoginRedirectService.redirectToRoleDefaultState(role);
                };

                $ctrl.isSelectedRole = function (role) {
                    var activeRole = ActiveRoleService.getActiveRole();
                    return activeRole && activeRole.id === role.id;
                };

                $ctrl.logout = function () {
                    LoginService.logout().then(function () {
                        $state.go('login');
                    });
                };
            }
        };
    });
