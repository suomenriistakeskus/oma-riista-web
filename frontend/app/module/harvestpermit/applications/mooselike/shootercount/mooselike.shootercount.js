'use strict';

angular.module('app.harvestpermit.application.mooselike.shootercount', ['app.metadata'])
    .component('mooselikeApplicationShooterCounts', {
        templateUrl: 'harvestpermit/applications/mooselike/shootercount/shooter-counts.html',
        bindings: {
            shooterCounts: '<'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.shooterTotalCount = function () {
                return ($ctrl.shooterCounts.shooterOnlyClub || 0) + ($ctrl.shooterCounts.shooterOtherClubPassive || 0);
            };
        }
    });
