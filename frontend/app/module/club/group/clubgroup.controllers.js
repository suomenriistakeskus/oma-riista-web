'use strict';

angular.module('app.clubgroup.controllers', ['ui.router', 'app.clubgroup.services'])

    .config(function ($stateProvider) {
        $stateProvider
            .state('club.groups', {
                url: '/group?year&species',
                templateUrl: 'club/group/layout.html',
                controller: 'ClubGroupListController',
                params: {
                    year: null,
                    species: null
                },
                resolve: {
                    groups: function (ClubGroups, clubId) {
                        return ClubGroups.query({clubId: clubId}).$promise;
                    },
                    groupsToYearSelection: function ($stateParams, ClubGroupService, HuntingYearService, groups) {
                        // if year is not given initialize year to current hunting year
                        var initialYear = $stateParams.year ? parseInt($stateParams.year) : HuntingYearService.getCurrent();
                        return ClubGroupService.groupsToYearSelection(groups, initialYear);
                    },
                    year: function ($stateParams, groupsToYearSelection) {
                        var selectedYear = groupsToYearSelection.selected;
                        $stateParams.year = selectedYear;
                        return selectedYear;
                    },
                    species: function ($stateParams) {
                        return $stateParams.species ? parseInt($stateParams.species) : null;
                    },
                    mooseDataCardGroupExistsForCurrentYear: function (HuntingYearService, groups) {
                        var currentYear = HuntingYearService.getCurrent();

                        return _.chain(groups)
                            .filter(function (group) {
                                return group.huntingYear === currentYear;
                            })
                            .some(function (group) {
                                return group.fromMooseDataCard;
                            })
                            .value();
                    }
                }
            })
            .state('club.groups.group', {
                url: '/{groupId:[0-9]{1,8}}',
                templateUrl: 'club/group/show.html',
                controller: 'ClubGroupShowController',
                resolve: {
                    groupId: function ($stateParams) {
                        return $stateParams.groupId;
                    },
                    group: function (ClubGroups, clubId, groupId) {
                        return ClubGroups.get({clubId: clubId, id: groupId}).$promise;
                    },
                    members: function (ClubGroupMembers, clubId, groupId) {
                        return ClubGroupMembers.query({clubId: clubId, groupId: groupId}).$promise;
                    },
                    huntingArea: function (ClubGroupAreas, group) {
                        return ClubGroupAreas.loadGroupArea(group);
                    },
                    mooseDataCardImports: function ($q, MooseDataCardImports, group) {
                        if (group.fromMooseDataCard) {
                            return MooseDataCardImports.listForGroup({ groupId: group.id }).$promise;
                        }
                        return $q.when([]);
                    }
                }
            });
    })

    .controller('ClubGroupListController', function ($scope, $state, ClubGroupService, NotificationService,
                                                     clubId, groupsToYearSelection, species,
                                                     mooseDataCardGroupExistsForCurrentYear) {

        $scope.groupsToYearSelection = groupsToYearSelection;
        $scope.species = species;
        $scope.clubId = clubId;

        $scope.addClubGroup = function () {
            var modalPromise = ClubGroupService.createGroup(clubId, mooseDataCardGroupExistsForCurrentYear);

            NotificationService.handleModalPromise(modalPromise).then(function () {
                $state.reload();
            });
        };
    })

    .controller('ClubGroupFormController', function ($uibModalInstance, ClubGroupAreas, ClubGroups,
                                                     ClubGroupPermits, GameSpeciesCodes, NotificationService, areas,
                                                     availableHuntingYearsBySpeciesCode, availableSpecies, group,
                                                     parameters, permits) {
        var $ctrl = this;

        $ctrl.huntingYears = availableHuntingYearsBySpeciesCode[group.gameSpeciesCode] || [];
        $ctrl.species = availableSpecies;
        $ctrl.group = group;
        $ctrl.areas = areas;
        $ctrl.permits = permits;
        $ctrl.selectedPermit = null;
        $ctrl.getCategoryName = parameters.$getCategoryName;
        $ctrl.getGameName = parameters.$getGameName;

        $ctrl.huntingNotStarted = !(group.huntingDaysExist || group.huntingFinished);
        $ctrl.showSpeciesSelection = $ctrl.huntingNotStarted;

        function reloadPermits() {
            ClubGroupPermits.loadGroupPermits($ctrl.group).then(function (permits) {
                $ctrl.permits = permits;
                $ctrl.group.permit = null;
            });
        }

        function reloadAreas() {
            ClubGroupAreas.loadGroupAreaOptions($ctrl.group).then(function (areas) {
                $ctrl.areas = areas;
            });
        }

        $ctrl.selectPermit = function (permit) {
            $ctrl.group.permit = permit;
            $ctrl.selectedPermit = null;
        };

        $ctrl.clearPermit = function () {
            $ctrl.group.permit = null;
        };

        $ctrl.onHuntingYearChange = function () {
            reloadAreas();
            reloadPermits();
        };

        $ctrl.onGameSpeciesChange = function () {
            var group = $ctrl.group;

            $ctrl.huntingYears = availableHuntingYearsBySpeciesCode[group.gameSpeciesCode] || [];

            var selectedYearIncludedInNewlyChangedHuntingYear = _.find($ctrl.huntingYears, function (yearObj) {
                return yearObj.year === group.huntingYear;
            });

            if (!selectedYearIncludedInNewlyChangedHuntingYear) {
                if ($ctrl.huntingYears.length > 0) {
                    group.huntingYear = $ctrl.huntingYears[0].year;
                }
                reloadAreas();
            }
            reloadPermits();
        };

        $ctrl.save = function () {
            $uibModalInstance.close($ctrl.group);
        };

        $ctrl.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    })

    .controller('ClubGroupCopyController', function ($scope, $uibModalInstance,
                                                     HuntingYearService, ClubGroupAreas,
                                                     group, areas) {

        $scope.huntingYears = HuntingYearService.currentAndNextObj();
        $scope.group = group;
        $scope.areas = areas;

        function reloadAreas() {
            ClubGroupAreas.loadGroupAreaOptions($scope.group).then(function (areas) {
                $scope.areas = areas;
            });
        }

        $scope.onHuntingYearChange = function () {
            reloadAreas();
        };

        $scope.save = function () {
            $uibModalInstance.close($scope.group);
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    })

    .controller('ClubGroupShowController', function ($scope, $uibModal, $state, $q, ActiveRoleService,
                                                     ClubGroupService, AddGroupMemberModal, NotificationService,
                                                     club, group, huntingArea, members, mooseDataCardImports) {
        $scope.group = group;
        $scope.members = members;
        $scope.huntingArea = huntingArea;
        $scope.mooseDataCardImports = mooseDataCardImports;
        $scope.isModerator = ActiveRoleService.isModerator();

        $scope.addMember = function () {
            var modalPromise = AddGroupMemberModal.open($scope.group);
            NotificationService.handleModalPromise(modalPromise).then(function () {
                $state.reload();
            });
        };

        $scope.canCopy = function () {
            return !$scope.group.fromMooseDataCard && ($scope.isModerator || ActiveRoleService.isClubContact());
        };

        $scope.copy = function () {
            ClubGroupService.copy(group)
                .then(function (newGroup) {
                    $state.go('club.groups.group', {
                        groupId: newGroup.id,
                        species: newGroup.gameSpeciesCode,
                        year: newGroup.huntingYear
                    }, {reload: true});
                    NotificationService.showDefaultSuccess();
                });
        };

        $scope.canEdit = function () {
            return $scope.group.canEdit;
        };

        $scope.edit = function () {
            var modalPromise = ClubGroupService.editGroup($scope.group);

            NotificationService.handleModalPromise(modalPromise).then(function () {
                $state.reload();
            });
        };

        $scope.canDelete = function () {
            return !$scope.group.huntingDaysExist && $scope.group.canEdit &&
                ($scope.isModerator || ActiveRoleService.isClubContact()) && !nonDeletedMooseDataCardImportExists();
        };

        $scope.delete = function () {
            ClubGroupService.delete($scope.group).then(function () {
                $state.go('club.groups', null, {reload: true});
                NotificationService.showDefaultSuccess();
            }, NotificationService.showDefaultFailure);
        };

        function nonDeletedMooseDataCardImportExists() {
            return _.some($scope.mooseDataCardImports, function (mooseDataCardImport) {
                return !mooseDataCardImport.revocationTimestamp;
            });
        }
    });
