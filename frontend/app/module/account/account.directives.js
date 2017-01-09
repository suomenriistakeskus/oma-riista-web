'use strict';

angular.module('app.account.directives', [])
    .directive('rAccountTodo', function (Account, ActiveRoleService, $rootScope) {
        return {
            restrict: 'A',
            template: '<span ng-show="count > 0" ng-class="classes">{{count}}</span>',
            scope: {rAccountTodo: '&'},
            link: function (scope, element, attrs) {
                if ($rootScope.account.role === 'ROLE_USER') {
                    var key = scope.rAccountTodo() || 'harvestsAndPermitsTotal';

                    var red = key !== 'invitations';
                    scope.classes = {'r-account-todo': red, 'r-account-todo-yellow': !red};

                    if (key === 'harvestsAndPermitsTotal' || key === 'harvests' || key === 'permits' || key === 'invitations') {
                        Account.countTodo().$promise.then(function (res) {
                            scope.count = res[key];
                        });
                    } else if (key === 'unfinishedSrvaEvents') {
                        var activeRole = ActiveRoleService.getActiveRole();
                        var rhyId = _.get(activeRole, 'context.rhyId');

                        if (rhyId) {
                            Account.countSrvaTodo({rhyId: rhyId}).$promise.then(function (res) {
                                scope.count = res[key];
                            });
                        }
                    } else {
                        console.warn('invalid key:', key);
                    }
                }
            }
        };
    })
    .directive('rClubContactShare', function ($log) {
        return {
            restrict: 'A',
            scope: {contactShare: '&rClubContactShare', large: '&rClubContactShareLarge'},
            replace: false,
            link: function (scope, element, attrs) {
                var contactShare = scope.contactShare();
                var large = scope.large();

                if (!contactShare) {
                    scope.classes = 'fa fa-ban text-danger';
                    scope.key = 'club.config.contactShare.none';
                    if (large) {
                        scope.style = 'font-size:22px';
                    }
                } else if (contactShare === 'ONLY_OFFICIALS') {
                    scope.classes = 'fa fa-user-secret';
                    scope.key = 'club.config.contactShare.onlyOfficials';
                    if (large) {
                        scope.style = 'font-size:22px';
                    }
                } else if (contactShare === 'ALL_MEMBERS') {
                    scope.classes = 'fa fa-users';
                    scope.key = 'club.config.contactShare.allMembers';
                    if (large) {
                        scope.style = 'font-size:20px;margin-bottom:2px';
                    }
                } else {
                    $log.debug('rClubContactShare Unknown contact share value:', contactShare);
                }
            },
            template: '<span style="{{style}};" class="{{classes}}" uib-tooltip="{{key | translate}}"></span>'
        };
    });
