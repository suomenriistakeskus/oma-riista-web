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
                    } else if (key === 'unfinishedTaxationReports') {
                        rhyId = getRhyId();

                        if (rhyId) {
                            Account.countTaxationTodo({rhyId: rhyId}).$promise.then(setTodoCount);
                        }
                    } else if (key === 'unfinishedHuntingControlEvents') {
                        rhyId = getRhyId();

                        if (rhyId) {
                            Account.countHuntingControlEventTodo({rhyId: rhyId}).$promise.then(setTodoCount);
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
                    scope.key = 'club.config.contactShare.null';
                    if (large) {
                        scope.style = 'font-size:22px';
                    }
                } else if (contactShare === 'ONLY_OFFICIALS') {
                    scope.classes = 'fa fa-user-secret';
                    scope.key = 'club.config.contactShare.ONLY_OFFICIALS';
                    if (large) {
                        scope.style = 'font-size:22px';
                    }
                } else if (contactShare === 'ALL_MEMBERS') {
                    scope.classes = 'fa fa-users';
                    scope.key = 'club.config.contactShare.ALL_MEMBERS';
                    if (large) {
                        scope.style = 'font-size:20px;margin-bottom:2px';
                    }
                } else {
                    $log.debug('rClubContactShare Unknown contact share value:', contactShare);
                }
            },
            template: '<span style="{{style}};" class="{{classes}}" uib-tooltip="{{key | translate}}"></span>'
        };
    })
    .directive('rClubContactInfoVisibility', function ($log) {
        return {
            restrict: 'A',
            scope: {
                contactVisibility: '&rClubContactInfoVisibility',
                large: '&rClubContactInfoVisibilityLarge',
                withLabel: '&rClubContactInfoVisibilityWithLabel'},
            replace: false,
            link: function (scope, element, attrs) {
                var contactVisibility = scope.contactVisibility();
                var large = scope.large();
                var withLabel = scope.withLabel();

                if(!withLabel) {
                    scope.labelStyle = 'display:none;';
                }

                if (contactVisibility === 'NAME') {
                    scope.classes = 'fa fa-user';
                    scope.key = 'club.config.name';
                    if (large) {
                        scope.style = 'font-size:22px';
                    }
                } else if (contactVisibility === 'PHONE') {
                    scope.classes = 'fa fa-phone';
                    scope.key = 'club.config.phoneNumber';
                    if (large) {
                        scope.style = 'font-size:22px';
                    }
                } else if (contactVisibility === 'EMAIL') {
                    scope.classes = 'fa fa-envelope';
                    scope.key = 'club.config.email';
                    if (large) {
                        scope.style = 'font-size:20px;margin-bottom:2px';
                    }
                } else {
                    $log.debug('rClubContactInfoVisibility Unknown contact info visibility value:', contactVisibility);
                }
            },
            template: '<span style="{{style}};" class="{{classes}}" uib-tooltip="{{key | translate}}"></span><div style="{{labelStyle}}">{{key | translate}}</div>'
        };
    });
