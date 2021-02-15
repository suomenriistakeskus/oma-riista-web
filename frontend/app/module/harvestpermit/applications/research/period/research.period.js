'use strict';

angular.module('app.harvestpermit.application.research.period', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.research.period', {
                url: '/period',
                templateUrl: 'harvestpermit/applications/research/period/period.html',
                controller: 'ResearchPermitWizardPeriodController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesPeriodList: function (ResearchPermitApplication, applicationId) {
                        return ResearchPermitApplication.listSpeciesPeriods({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'mapdetails',
                            next: 'methods'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.research.period', {
                url: '/period',
                templateUrl: 'harvestpermit/applications/research/period/period.html',
                controller: 'ResearchPermitWizardPeriodController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesPeriodList: function (ResearchPermitApplication, applicationId) {
                        return ResearchPermitApplication.listSpeciesPeriods({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'mapdetails',
                            next: 'methods'
                        };
                    }
                }
            });
    })

    .controller('ResearchPermitWizardPeriodController', function ($scope, $q, ApplicationWizardNavigationHelper,
                                                                  UnsavedChangesConfirmationService, Helpers,
                                                                  ResearchPermitApplication, applicationId,
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
            return ResearchPermitApplication.saveSpeciesPeriods({id: applicationId}, {
                speciesPeriods: $ctrl.speciesPeriodList,
                validityYears: $ctrl.validityYears
            }).$promise;
        };

        function invalid(form) {
            return form.$invalid;
        }

        $ctrl.isIntervalOverSpeciesLimit = function (species) {
            var b = species.beginDate ? Helpers.toMoment(species.beginDate, 'YYYY-MM-DD') : null;
            var e = species.endDate ? Helpers.toMoment(species.endDate, 'YYYY-MM-DD') : null;
            if (!b || !e) {
                return false;
            }

            return e.diff(b, 'years') >= 1;
        };
    });
