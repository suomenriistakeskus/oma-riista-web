(function () {
    'use strict';

    angular.module('app.club.members', ['ngResource', 'ui.router'])
        .config(function ($stateProvider) {
            $stateProvider
                .state('club.members', {
                    url: '/members',
                    templateUrl: 'club/members/members.html',
                    controller: 'ClubMembersController',
                    controllerAs: '$ctrl',
                    bindToController: true,
                    resolve: {
                        members: function (ClubMembers, clubId) {
                            return ClubMembers.query({clubId: clubId}).$promise;
                        },
                        invitations: function (ClubInvitations, clubId) {
                            return ClubInvitations.query({clubId: clubId}).$promise;
                        }
                    }
                });
        })

        .factory('ClubMembers', function ($resource) {
            var apiPrefix = 'api/v1/club/:clubId/member/:id';

            return $resource(apiPrefix, {'clubId': '@clubId', 'id': '@id'}, {
                isLocked: {
                    method: 'GET',
                    url: apiPrefix + '/locked'
                },
                updateType: {
                    method: 'PUT',
                    url: apiPrefix + '/type'
                },
                primaryContact: {
                    method: 'PUT',
                    url: apiPrefix + '/primarycontact'
                },
                invite: {
                    method: 'POST',
                    url: 'api/v1/club/:clubId/invite',
                    params: {'clubId': '@clubId'}
                }
            });
        })

        .factory('ClubInvitations', function ($resource) {
            var apiPrefix = 'api/v1/club/invitation/:id';
            return $resource(apiPrefix, {'id': '@id'}, {
                query: {
                    method: 'GET',
                    url: 'api/v1/club/:clubId/invitation',
                    params: {'clubId': '@clubId'},
                    isArray: true
                },
                accept: {
                    method: 'PUT',
                    url: apiPrefix + '/accept'
                },
                reject: {
                    method: 'PUT',
                    url: apiPrefix + '/reject'
                },
                resend: {
                    method: 'POST',
                    url: apiPrefix + '/resend'
                }
            });
        })

        .service('ClubMemberService', function ($uibModal, ClubMembers, AuthenticationService) {
            this.isLockedMember = function (member) {
                return ClubMembers.isLocked({
                    clubId: member.organisationId,
                    id: member.id
                }).$promise;
            };

            this.isMemberCurrentUserAndContactPerson = function (member) {
                var memberIsActivePerson = AuthenticationService.isCurrentPersonId(member.personId);
                var memberIsContactPerson = member.occupationType === 'SEURAN_YHDYSHENKILO';

                return memberIsActivePerson && memberIsContactPerson;
            };

            this.setPrimaryContact = function (member) {
                return ClubMembers.primaryContact({
                    clubId: member.organisationId,
                    id: member.id
                }).$promise;
            };

            this.showAddMembersBulkDialog = function (club) {
                return $uibModal.open({
                    templateUrl: 'club/members/addmembers.html',
                    controller: 'AddClubMembersBulkController',
                    controllerAs: '$ctrl',
                    resolve: {
                        club: _.constant(club),
                        groups: function (ClubGroups) {
                            return ClubGroups.query({clubId: club.id}).$promise;
                        }
                    }
                }).result;
            };

            this.removeMember = function (member) {
                return ClubMembers.delete({
                    clubId: member.organisationId,
                    id: member.id
                }).$promise;
            };

            function setMemberType(type, member) {
                return ClubMembers.updateType({
                    clubId: member.organisationId,
                    id: member.id,
                    occupationType: type
                }).$promise.then(function (ret) {
                    angular.extend(member, ret);
                });
            }

            this.setMember = _.partial(setMemberType, 'SEURAN_JASEN');
            this.setContact = _.partial(setMemberType, 'SEURAN_YHDYSHENKILO');
        })

        .component('clubInvitationList', {
            templateUrl: 'club/members/invitation_list.html',
            bindings: {
                reload: '&',
                invitations: '<',
                canEdit: '<'
            },
            controller: function (ClubInvitations) {
                var $ctrl = this;

                $ctrl.reSendInvitation = function (invitation) {
                    ClubInvitations.resend({
                        id: invitation.id
                    }).$promise.then(function () {
                        $ctrl.reload();
                    });
                };

                $ctrl.removeInvitation = function (invitation) {
                    ClubInvitations.delete({
                        id: invitation.id
                    }).$promise.then(function () {
                        $ctrl.reload();
                    });
                };
            }
        })

        .component('clubMemberList', {
            templateUrl: 'club/members/member_list.html',
            bindings: {
                reload: '&',
                members: '<',
                canEdit: '<'
            },
            controller: function ($q, $state, $translate, dialogs,
                                  NotificationService, AccountService,
                                  ClubMemberService) {
                var $ctrl = this;

                $ctrl.setMember = function (member) {
                    checkCanModify(member).then(function () {
                        ClubMemberService.setMember(member).then(onSuccess, onFailure);
                    });
                };

                $ctrl.setContact = function (member) {
                    ClubMemberService.setContact(member).then(onSuccess, onFailure);
                };

                $ctrl.removeMember = function (member) {
                    checkCanModify(member).then(function () {
                        var resultPromise = ClubMemberService.removeMember(member);

                        if (ClubMemberService.isMemberCurrentUserAndContactPerson(member)) {
                            resultPromise.then(syncRolesAndSelectNewRole);
                        } else {
                            resultPromise.then(onSuccess, onFailure);
                        }
                    });
                };

                function onSuccess() {
                    NotificationService.showDefaultSuccess();
                    $ctrl.reload();
                }

                function onFailure() {
                    NotificationService.showDefaultFailure();
                }

                function syncRolesAndSelectNewRole() {
                    AccountService.updateRoles().finally(function () {
                        $state.go('roleselection');
                    });
                }

                function checkCanModify(member) {
                    return ClubMemberService.isLockedMember(member).then(function (response) {
                        if (response.isLocked) {
                            NotificationService.showMessage('club.main.contactPersons.warningRemoveOnlyContactPersonWithData', 'error');
                            return $q.reject();
                        }
                        return $q.when();
                    }).then(function () {
                        var warnContactPerson = ClubMemberService.isMemberCurrentUserAndContactPerson(member);
                        var dialogTitle = $translate.instant('global.dialog.confirmation.title');
                        var dialogMessage = $translate.instant(warnContactPerson
                            ? 'club.main.contactPersons.warningRemoveContactPersonSelf'
                            : 'global.dialog.confirmation.text');

                        return dialogs.confirm(dialogTitle, dialogMessage).result;
                    });
                }
            }
        })

        .controller('ClubMembersController', function (ClubMemberService, ClubMembers, ClubInvitations,
                                                       club, members, invitations) {
            var $ctrl = this;

            $ctrl.invitations = invitations;
            $ctrl.members = members;
            $ctrl.club = club;

            $ctrl.addMembersBulk = function () {
                ClubMemberService.showAddMembersBulkDialog(club).then(function () {
                    $ctrl.reloadInvitations();
                });
            };

            $ctrl.reloadMembers = function () {
                ClubMembers.query({clubId: club.id}).$promise.then(function (result) {
                    $ctrl.members = result;
                });
            };

            $ctrl.reloadInvitations = function () {
                ClubInvitations.query({clubId: club.id}).$promise.then(function (result) {
                    $ctrl.invitations = result;
                });
            };
        })

        .controller('AddClubMembersBulkController', function ($uibModalInstance, NotificationService, HttpPost,
                                                              ClubMembers, ClubGroupService,
                                                              HunterNumberValidatorService,
                                                              club, groups) {
            var $ctrl = this;

            $ctrl.years = ClubGroupService.groupsToYearSelection(groups);
            $ctrl.groups = groups;
            $ctrl.hunterNumbers = '';
            $ctrl.invalidHunterNumbers = [];
            $ctrl.selectedGroup = null;
            $ctrl.isAddToGroupEnabled = false;
            $ctrl.notFoundHunterNumbers = null;

            $ctrl.yearChanged = function () {
                var v = _.find($ctrl.years.values, 'year', $ctrl.years.selected);
                $ctrl.groups = v ? v.groups : groups;
            };
            $ctrl.yearChanged();

            $ctrl.isAddToGroupEnabledChanged = function () {
                if (!$ctrl.isAddToGroupEnabled) {
                    $ctrl.selectedGroup = null;
                }
            };

            $ctrl.hunterNumberTyped = function () {
                $ctrl.invalidHunterNumbers = getInvalidHunterNumbers();

                if (!_.isEmpty($ctrl.invalidHunterNumbers)) {
                    $ctrl.notFoundHunterNumbers = null;
                    return;
                }

                HttpPost.post('api/v1/club/' + club.id + '/validateHunterNumbers', {
                    hunterNumbers: getHunterNumbers()
                }).success(function (res) {
                    $ctrl.notFoundHunterNumbers = res && res.length ? res : null;
                });
            };

            $ctrl.hasInvalidHunterNumbers = function () {
                return _.size($ctrl.invalidHunterNumbers) > 0;
            };

            $ctrl.isOk = function () {
                return !$ctrl.hasInvalidHunterNumbers() && !_.isEmpty(getHunterNumbers()) && !$ctrl.notFoundHunterNumbers;
            };

            $ctrl.ok = function () {
                ClubMembers.invite({clubId: club.id}, {
                    groupId: $ctrl.selectedGroup ? $ctrl.selectedGroup.id : null,
                    hunterNumbers: getHunterNumbers()
                }).$promise.then(function () {
                    NotificationService.showDefaultSuccess();
                    $uibModalInstance.close();
                }, NotificationService.showDefaultFailure);
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };

            function getHunterNumbers() {
                return angular.isString($ctrl.hunterNumbers)
                    ? _($ctrl.hunterNumbers.split(/[\s,;]+/)).map(_.trim).compact().uniq().value()
                    : [];
            }

            function getInvalidHunterNumbers() {
                return _(getHunterNumbers())
                    .filter(_.negate(HunterNumberValidatorService.validate))
                    .compact()
                    .value();
            }
        });
})();
