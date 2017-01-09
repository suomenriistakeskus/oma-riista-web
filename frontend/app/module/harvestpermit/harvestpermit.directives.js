'use strict';

angular.module('app.harvestpermit.directives', [])

    .directive('rPermitSpeciesAmounts', function () {
        return {
            replace: false,
            restrict: 'A',
            scope: { permit: '=permit'},
            link: function (scope, element, attrs) {
                var speciesSummary = {};
                var harvestsOrReports;
                var counter;
                if (scope.permit.harvestsAsList) {
                    harvestsOrReports = scope.permit.harvests;
                    counter = function (code, harvest) {
                        if (harvest.stateAcceptedToHarvestPermit === 'ACCEPTED') {
                            speciesSummary[code] += harvest.totalSpecimenAmount;
                        }
                    };
                } else {
                    harvestsOrReports = _.filter(scope.permit.harvestReports, function (report) {
                        return report.state !== 'REJECTED';
                    });
                    counter = function (code, harvest) {
                        speciesSummary[code] += 1;
                    };
                }
                _.each(harvestsOrReports, function (harvest) {
                    var code = harvest.gameSpecies.code;
                    if (_.isUndefined(speciesSummary[code])) {
                        speciesSummary[code] = 0;
                    }
                    counter(code, harvest);
                });

                var _reportedSum = function (speciesAmount) {
                    return speciesSummary[speciesAmount.gameSpecies.code] || 0;
                };
                scope.getReportedSum = _reportedSum;
                scope.getRemainingSum = function (speciesAmount) {
                    return speciesAmount.amount - _reportedSum(speciesAmount);
                };
            },
            templateUrl: 'harvestpermit/show-species-amounts.html'
        };
    })

    .directive('rHarvestPermitListing', function ($parse, ActiveRoleService) {
        return {
            replace: false,
            restrict: 'A',
            scope: false,
            link: function (scope, element, attrs) {
                scope.permits = $parse(attrs.rHarvestPermitListing)(scope);
                scope.moderator = ActiveRoleService.isModerator();
                scope.getHarvestReports = function (permit) {
                    // if end-of-hunting report exists return it first among other harvest reports
                    var reports = permit.endOfHuntingReport ? [permit.endOfHuntingReport] : [];
                    return reports.concat(_.sortByOrder(permit.harvestReports, 'pointOfTime', false));
                };
            },
            templateUrl: 'harvestpermit/r-harvest-permit-listing.html'
        };
    })
;
