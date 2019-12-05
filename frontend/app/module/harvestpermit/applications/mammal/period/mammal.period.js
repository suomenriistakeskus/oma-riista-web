'use strict';

angular.module('app.harvestpermit.application.mammal.period', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.mammal.period', {
                url: '/period',
                templateUrl: 'harvestpermit/applications/mammal/period/period.html',
                controller: 'MammalPermitWizardPeriodController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesPeriodList: function (MammalPermitApplication, applicationId) {
                        return MammalPermitApplication.listSpeciesPeriods({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'reasons',
                            next: 'methods'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.mammal.period', {
                url: '/period',
                templateUrl: 'harvestpermit/applications/mammal/period/period.html',
                controller: 'MammalPermitWizardPeriodController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesPeriodList: function (MammalPermitApplication, applicationId) {
                        return MammalPermitApplication.listSpeciesPeriods({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'reasons',
                            next: 'methods'
                        };
                    }
                }
            });
    })

    .controller('MammalPermitWizardPeriodController', function ($scope, $q, ApplicationWizardNavigationHelper,
                                                                UnsavedChangesConfirmationService, Helpers,
                                                                MammalPermitApplication, applicationId,
                                                                states, wizard, speciesPeriodList) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.speciesPeriodList = speciesPeriodList.speciesPeriods;
            $ctrl.validityYears = speciesPeriodList.validityYears || 1;
            $ctrl.extendedPeriodNotApplicable = speciesPeriodList.extendedPeriodNotApplicable;
            $ctrl.extendedPeriodGrounds = speciesPeriodList.extendedPeriodGrounds;
            $ctrl.extendedPeriodGroundsDescription = speciesPeriodList.extendedPeriodGroundsDescription;
            $ctrl.protectedAreaName = speciesPeriodList.protectedAreaName;
            $scope.$watch('speciesPeriodForm.$pristine', function (newVal, oldVal) {
                if (oldVal && !newVal) {
                    UnsavedChangesConfirmationService.setChanges(true);
                }
            }, true);
        };

        $ctrl.exit = function (form) {
            ApplicationWizardNavigationHelper.previous(invalid(form), $ctrl.save, wizard.exit);
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
            return MammalPermitApplication.saveSpeciesPeriods({id: applicationId}, {
                speciesPeriods: $ctrl.speciesPeriodList,
                validityYears: $ctrl.validityYears,
                extendedPeriodGrounds: $ctrl.validityYears === 1 ? null : $ctrl.extendedPeriodGrounds,
                extendedPeriodGroundsDescription: $ctrl.validityYears === 1 ? null : $ctrl.extendedPeriodGroundsDescription,
                protectedAreaName: $ctrl.validityYears === 1 ? null : $ctrl.protectedAreaName
            }).$promise;
        };

        function invalid(form) {
            return form.$invalid ||
                ($ctrl.validityYears > 1 && $ctrl.extendedPeriodNotApplicable) ||
                ($ctrl.validityYears > 1 && $ctrl.extendedPeriodGrounds === null);
        }

        $ctrl.isIntervalOverSpeciesLimit = function (species) {
            var b = species.beginDate ? Helpers.toMoment(species.beginDate, 'YYYY-MM-DD') : null;
            var e = species.endDate ? Helpers.toMoment(species.endDate, 'YYYY-MM-DD') : null;
            if (!b || !e) {
                return false;
            }

            if (species.maxPeriod) {
                return e.diff(b, 'days') > species.maxPeriod;
            }
            return e.diff(b, 'years') >= 1;
        };
    });
