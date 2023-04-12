'use strict';

angular.module('app.harvestpermit.application.importing.period', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.importing.period', {
                url: '/period',
                templateUrl: 'harvestpermit/applications/importing/period/period.html',
                controller: 'ImportingPermitWizardPeriodController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    speciesPeriodList: function (ImportingPermitApplication, applicationId) {
                        return ImportingPermitApplication.getSpeciesPeriods({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'species',
                            next: 'map'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.importing.period', {
                url: '/period',
                templateUrl: 'harvestpermit/applications/importing/period/period.html',
                controller: 'ImportingPermitWizardPeriodController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesPeriodList: function (ImportingPermitApplication, applicationId) {
                        return ImportingPermitApplication.getSpeciesPeriods({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'species',
                            next: 'map'
                        };
                    }
                }
            });
    })

    .controller('ImportingPermitWizardPeriodController', function ($scope, $q, ApplicationWizardNavigationHelper,
                                                                   UnsavedChangesConfirmationService, Helpers,
                                                                   ImportingPermitApplication, applicationId,
                                                                   states, wizard, speciesPeriodList) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.speciesPeriodList = speciesPeriodList.speciesPeriods;
            $ctrl.validityYears = speciesPeriodList.validityYears || 1;

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
            return ImportingPermitApplication.saveSpeciesPeriods({id: applicationId}, {
                speciesPeriods: $ctrl.speciesPeriodList,
                validityYears: $ctrl.validityYears
            }).$promise;
        };

        function invalid(form) {
            return form.$invalid
                || _.some($ctrl.speciesPeriodList, function (period) {
                    return $ctrl.isIntervalOverSpeciesLimit(period);
                });
        }

        $ctrl.isIntervalOverSpeciesLimit = function (species) {
            var b = species.beginDate ? Helpers.toMoment(species.beginDate, 'YYYY-MM-DD') : null;
            var e = species.endDate ? Helpers.toMoment(species.endDate, 'YYYY-MM-DD') : null;
            if (!b || !e) {
                return false;
            }

            // Maximum period is 1 year
            return e.diff(b, 'years') >= 1;
        };
    });
