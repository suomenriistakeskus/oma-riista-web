'use strict';

angular.module('app.account.directives', [])
    .directive('rAccountTodo', function (Account, ActiveRoleService) {
        return {
            restrict: 'A',
            template: '<span ng-show="count > 0" ng-class="classes">{{count}}</span>',
            scope: {rAccountTodo: '&'},
            link: function (scope, element, attrs) {
                if (ActiveRoleService.isUser()) {
                    var key = scope.rAccountTodo();
                    var rhyId;

                    var red = key !== 'invitations';
                    scope.classes = {'r-account-todo': red, 'r-account-todo-yellow': !red};

                    var getRhyId = function () {
                        var activeRole = ActiveRoleService.getActiveRole();
                        return _.get(activeRole, 'context.rhyId');
                    };

                    var setTodoCount = function (response) {
                        scope.count = response.todoCount;
                    };

                    if (key === 'permits') {
                        Account.countPermitTodo().$promise.then(function (res) {
                            scope.count = res.permitIds.length;
                        });
                    } else if (key === 'invitations') {
                        Account.countInvitationTodo().$promise.then(setTodoCount);
                    } else if (key === 'unfinishedSrvaEvents') {
                        rhyId = getRhyId();

                        if (rhyId) {
                            Account.countSrvaTodo({rhyId: rhyId}).$promise.then(setTodoCount);
                        }
                    } else if (key === 'unfinishedShootingTestEvents') {
                        rhyId = getRhyId();

                        if (rhyId) {
                            Account.countShootingTestTodo({rhyId: rhyId}).$promise.then(setTodoCount);
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
