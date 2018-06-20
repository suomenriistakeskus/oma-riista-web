'use strict';

angular.module('app.clubgroup.directives', [])
    .component('rClubGroupsMenu', {
        templateUrl: 'club/group/r-club-groups-menu.html',
        bindings: {
            years: '<',
            selectedSpeciesCode: '<',
            clubId: '<'
        },
        controller: function ($state, SpeciesSortByName, ActiveRoleService, FormPostService) {
            var $ctrl = this;

            function getGroups(speciesCode) {
                var found = _.find($ctrl.years.values, 'year', $ctrl.years.selected);
                var groups = found ? found.groups : null;
                if (speciesCode) {
                    return _.filter(groups, 'gameSpeciesCode', speciesCode);
                }
                return groups;
            }

            $ctrl.$onInit = function () {
                $ctrl.groups = getGroups($ctrl.selectedSpeciesCode);
                $ctrl.speciesForYear = SpeciesSortByName.sort(_(getGroups())
                    .pluck('species')
                    .uniq('code')
                    .value());
            };

            $ctrl.speciesChanged = function () {
                $state.go('club.groups', {year: $ctrl.years.selected, species: $ctrl.selectedSpeciesCode});
            };

            $ctrl.groupChanged = function (group) {
                if (group) {
                    $state.go('club.groups.group', {groupId: group.id});
                }
            };

            $ctrl.yearChanged = function () {
                if ($ctrl.years.selected) {
                    // year where we'll go might not have selected species
                    var s = getGroups($ctrl.selectedSpeciesCode).length > 0 ? $ctrl.selectedSpeciesCode : null;
                    $state.go('club.groups', {year: $ctrl.years.selected, species: s});
                }
            };

            $ctrl.isGroupExportVisible = function () {
                return ActiveRoleService.isClubContact() || ActiveRoleService.isModerator();
            };

            $ctrl.exportGroups = function () {
                var year = $ctrl.years.selected;
                var speciesCode = $ctrl.selectedSpeciesCode;

                var params = {year: year};
                if (speciesCode) {
                    params.speciesCode = speciesCode;
                }

                var formSubmitAction = '/api/v1/club/' + $ctrl.clubId + '/group/export-groups';

                FormPostService.submitFormUsingBlankTarget(formSubmitAction, params);
            };
        }
    })
;
