(function () {
    'use strict';

    angular.module('app.clubgroup.members', ['ngResource', 'ui.router'])
        .factory('ClubGroupMembers', function ($resource) {
            var apiPrefix = 'api/v1/club/group/:groupId/member';

            return $resource(apiPrefix + '/:id', {'groupId': '@groupId', 'id': '@id'}, {
                query: {
                    method: 'GET',
                    params: {type: 'page'},
                    isArray: true
                },
                isLocked: {
                    method: 'GET',
                    url: apiPrefix + '/:id/locked'
                },
                updateType: {
                    method: 'PUT',
                    url: apiPrefix + '/:id/type'
                },
                updateOrder: {
                    method: 'POST',
                    url: apiPrefix + '/order'
                }
            });
        })

        .service('ClubGroupMemberService', function ($q, ClubGroupMembers, ClubMembers, ClubInvitations) {
            this.listShooterCandidates = function (clubId, groupId) {
                var groupMembers = ClubGroupMembers.query({clubId: clubId, groupId: groupId}).$promise;

                return groupMembers.then(function (members) {
                    return _.filter(members, function (m) {
                        return m.person && m.person.hunterNumber;
                    });
                });
            };

            this.listMemberCandidates = function (group) {
                var clubMembersPromise = ClubMembers.query({clubId: group.clubId}).$promise;
                var clubInvitationsPromise = ClubInvitations.query({clubId: group.clubId}).$promise;
                var groupMembersPromise = ClubGroupMembers.query({clubId: group.clubId, groupId: group.id}).$promise;
                var promiseArray = [groupMembersPromise, clubMembersPromise, clubInvitationsPromise];

                return $q.all(promiseArray).then(function (promises) {
                    var currentMembers = promises[0];
                    var clubMembers = promises[1];
                    var clubInvitations = promises[2];

                    // group member can be added from club members or invited members
                    var clubMembersAndInvitations = _.flatten([clubMembers, clubInvitations]);

                    return notExistingMember(clubMembersAndInvitations, currentMembers);
                });

                function notExistingMember(clubMembersAndInvitations, currentMembers) {
                    return _.filter(clubMembersAndInvitations, function (person) {
                        return !_.find(currentMembers, function (existingMember) {
                            return existingMember.personId === person.personId;
                        });
                    });
                }
            };
        })

        .service('AddGroupMemberModal', function ($uibModal, ClubGroupMembers, ClubGroupMemberService) {
            this.open = function (group) {
                var modalInstance = $uibModal.open({
                    templateUrl: 'club/group/members/addmember.html',
                    resolve: {
                        memberCandidates: function () {
                            return ClubGroupMemberService.listMemberCandidates(group);
                        }
                    },
                    controllerAs: '$ctrl',
                    controller: ModalController
                });

                return modalInstance.result.then(function (m) {
                    return ClubGroupMembers.save({groupId: group.id}, m).$promise;
                });
            };

            function ModalController($uibModalInstance, memberCandidates) {
                var $ctrl = this;

                $ctrl.newMember = null;

                $ctrl.getName = function (member) {
                    var person = member.person;
                    var name = person.lastName + ', ' + person.byName;
                    if (person.address) {
                        return name + ' ' + person.address.city.toUpperCase();
                    }
                    return name;
                };

                $ctrl.memberCandidates = _.sortBy(memberCandidates, $ctrl.getName);

                $ctrl.ok = function () {
                    $uibModalInstance.close({
                        personId: $ctrl.newMember.personId,
                        occupationType: 'RYHMAN_JASEN'
                    });
                };

                $ctrl.cancel = function () {
                    $uibModalInstance.dismiss('cancel');
                };
            }
        })

        .service('LockedGroupMemberService', function ($q, $translate, dialogs, NotificationService,
                                                       AuthenticationService, ClubGroupMembers) {
            this.resolveIfCanChangeMemberRole = function (member) {
                return checkMemberLocked(member).then(function () {
                    return checkModifyHuntingLeader(member);
                });
            };

            this.resolveIfCanRemoveMember = function (member) {
                return checkMemberLocked(member).then(function () {
                    return confirmOperation();
                });
            };

            function checkMemberLocked(member) {
                return ClubGroupMembers.isLocked({
                    groupId: member.organisationId,
                    id: member.id
                }).$promise.then(function (response) {
                    if (!response.isLocked) {
                        return $q.when();
                    }

                    NotificationService.showMessage('club.group.members.warningRemoveOnlyHuntingLeaderWithData', 'error');

                    return $q.reject();
                });
            }

            function checkModifyHuntingLeader(member) {
                if (member.occupationType !== 'RYHMAN_METSASTYKSENJOHTAJA') {
                    return $q.when();
                }

                if (AuthenticationService.isCurrentPersonId(member.personId)) {
                    var dialogTitle = $translate.instant('global.dialog.confirmation.title');
                    var dialogMessage = $translate.instant('club.group.members.warningRemoveHuntingLeaderSelf');

                    return dialogs.confirm(dialogTitle, dialogMessage).result;
                }

                return confirmOperation();
            }

            function confirmOperation() {
                var dialogTitle = $translate.instant('global.dialog.confirmation.title');
                var dialogMessage = $translate.instant('global.dialog.confirmation.text');

                return dialogs.confirm(dialogTitle, dialogMessage).result;
            }
        })

        .component('groupMemberList', {
            templateUrl: 'club/group/members/member_list.html',
            bindings: {
                members: '<',
                canEdit: '<'
            },
            controller: function ($state, NotificationService,
                                  ClubGroupMembers, LockedGroupMemberService, ActiveRoleService) {
                var $ctrl = this;

                $ctrl.isClubContact = ActiveRoleService.isClubContact() || ActiveRoleService.isModerator();

                $ctrl.canEditMember = function (member) {
                    return member.occupationType === 'RYHMAN_METSASTYKSENJOHTAJA'
                        ? $ctrl.canEdit && $ctrl.isClubContact : $ctrl.canEdit;
                };

                function handleResult(resultPromise) {
                    resultPromise
                        .then(NotificationService.showDefaultSuccess, NotificationService.showDefaultFailure)
                        .finally(function () {
                            $state.reload();
                        });
                }

                $ctrl.setMemberType = function (type, member) {
                    LockedGroupMemberService.resolveIfCanChangeMemberRole(member).then(function () {
                        handleResult(ClubGroupMembers.updateType({
                            groupId: member.organisationId,
                            id: member.id,
                            occupationType: type
                        }).$promise);
                    });
                };

                $ctrl.removeMember = function (member) {
                    LockedGroupMemberService.resolveIfCanRemoveMember(member).then(function () {
                        handleResult(ClubGroupMembers.delete({
                            groupId: member.organisationId,
                            id: member.id
                        }).$promise);
                    });
                };

                $ctrl.adjustMember = function (index, delta) {
                    var member = $ctrl.members.splice(index, 1)[0];
                    $ctrl.members.splice(index + delta, 0, member);

                    var ordering = _($ctrl.members)
                        .filter({'occupationType': 'RYHMAN_METSASTYKSENJOHTAJA'})
                        .pluck('id')
                        .value();

                    // Need to reload, otherwise when callOrder is changed then immediate occupationType change
                    // will fail to version mismatch.
                    ClubGroupMembers.updateOrder({
                        groupId: member.organisationId
                    }, ordering).$promise.then(function () {
                        $state.reload();
                    });
                };

                var maxCallOrder = calculateMaxCallOrder($ctrl.members);

                $ctrl.adjustDisabled = function (index, delta) {
                    var member = $ctrl.members[index];

                    if (delta < 0) {
                        return member.callOrder < 1;
                    } else {
                        return member.callOrder >= maxCallOrder;
                    }
                };

                function calculateMaxCallOrder(members) {
                    return _(members)
                        .filter('occupationType', 'RYHMAN_METSASTYKSENJOHTAJA')
                        .pluck('callOrder')
                        .max();
                }
            }
        });
})();
