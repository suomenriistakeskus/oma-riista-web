'use strict';

angular.module('app.harvestpermit.application.lawsectionten.period', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.lawsectionten.period', {
                url: '/period',
                templateUrl: 'harvestpermit/applications/lawsectionten/period/period.html',
                controller: 'LawSectionTenPermitWizardPeriodController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    speciesPeriod: function (LawSectionTenPermitApplication, applicationId) {
                        return LawSectionTenPermitApplication.getSpeciesPeriod({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'mapdetails',
                            next: 'population'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.lawsectionten.period', {
                url: '/period',
                templateUrl: 'harvestpermit/applications/lawsectionten/period/period.html',
                controller: 'LawSectionTenPermitWizardPeriodController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesPeriod: function (LawSectionTenPermitApplication, applicationId) {
                        return LawSectionTenPermitApplication.getSpeciesPeriod({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'mapdetails',
                            next: 'population'
                        };
                    }
                }
            });
    })

    .controller('LawSectionTenPermitWizardPeriodController', function ($scope, $q, ApplicationWizardNavigationHelper,
                                                                       UnsavedChangesConfirmationService, Helpers,
                                                                       LawSectionTenPermitApplication, applicationId,
                                                                       states, wizard, speciesPeriod, GameSpeciesCodes) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.speciesPeriod = speciesPeriod;

            $ctrl.isBeaverApplication = GameSpeciesCodes.isEuropeanBeaver(speciesPeriod.gameSpeciesCode);

            $scope.$watch('speciesPeriodForm.$pristine', function (newVal, oldVal) {
                if (oldVal && !newVal) {
                    UnsavedChangesConfirmationService.setChanges(true);
                }
            }, true);
        };

        $ctrl.exit = function (form) {
            ApplicationWizardNavigationHelper.exit(invalid(form), $ctrl.save, wizard.exit);
        };

        $ctrl.previous = function (form) {
            ApplicationWizardNavigationHelper.previous(invalid(form), $ctrl.save, $ctrl.doGotoPrevious);
        };

        $ctrl.doGotoPrevious = function () {
            doGoto(states.previous);
        };

        function doGoto(state) {
            wizard.goto(state);
        }

        $ctrl.next = function () {
            $ctrl.save().then(function () {
                doGoto(states.next);
            });
        };

        $ctrl.nextDisabled = function (form) {
            return invalid(form);
        };

        $ctrl.save = function () {
            UnsavedChangesConfirmationService.setChanges(false);
            return LawSectionTenPermitApplication
                .saveSpeciesPeriod({id: applicationId}, speciesPeriod)
                .$promise;
        };

        function invalid(form) {
            return form.$invalid || $ctrl.isPeriodInvalid($ctrl.speciesPeriod);
        }

        $ctrl.isPeriodInvalid = function (species) {
            var b = species.beginDate ? Helpers.toMoment(species.beginDate, 'YYYY-MM-DD') : null;
            var e = species.endDate ? Helpers.toMoment(species.endDate, 'YYYY-MM-DD') : null;
            if (!b || !e || b.isAfter(e)) {
                return true;
            }

            var isWithinCurrentSeason =
                isDateWithinSeason(b, $ctrl.speciesPeriod.seasons.currentSeason.rangesByLowerBound) &&
                isDateWithinSeason(e, $ctrl.speciesPeriod.seasons.currentSeason.rangesByLowerBound);
            var isWithinNextSeason =
                isDateWithinSeason(b, $ctrl.speciesPeriod.seasons.nextSeason.rangesByLowerBound) &&
                isDateWithinSeason(e, $ctrl.speciesPeriod.seasons.nextSeason.rangesByLowerBound);

            return !(isWithinCurrentSeason || isWithinNextSeason);
        };

        function isDateWithinSeason(date, season) {
            return _.filter(season, function (range) {
                return date.isBetween(range.lowerBound.endpoint, range.upperBound.endpoint, 'days', []);
            }).length > 0;
        }
    });
