'use strict';

angular.module('app.harvestpermit.application.deportation.period', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.deportation.period', {
                url: '/period',
                templateUrl: 'harvestpermit/applications/deportation/period/period.html',
                controller: 'DeportationPermitWizardPeriodController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    speciesPeriod: function (DeportationPermitApplication, applicationId) {
                        return DeportationPermitApplication.getSpeciesPeriod({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'reasons',
                            next: 'methods'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.deportation.period', {
                url: '/period',
                templateUrl: 'harvestpermit/applications/deportation/period/period.html',
                controller: 'DeportationPermitWizardPeriodController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesPeriod: function (DeportationPermitApplication, applicationId) {
                        return DeportationPermitApplication.getSpeciesPeriod({id: applicationId}).$promise;
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

    .controller('DeportationPermitWizardPeriodController', function ($scope, $q, ApplicationWizardNavigationHelper,
                                                                     UnsavedChangesConfirmationService, Helpers,
                                                                     DeportationPermitApplication, applicationId,
                                                                     states, wizard, speciesPeriod) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.speciesPeriod = speciesPeriod;
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
            return DeportationPermitApplication
                .saveSpeciesPeriod({id: applicationId}, $ctrl.speciesPeriod)
                .$promise;
        };

        function invalid(form) {
            return form.$invalid;
        }

        $ctrl.isIntervalOverSpeciesLimit = function () {
            var b = $ctrl.speciesPeriod.beginDate ? Helpers.toMoment($ctrl.speciesPeriod.beginDate, 'YYYY-MM-DD') : null;
            var e = $ctrl.speciesPeriod.endDate ? Helpers.toMoment($ctrl.speciesPeriod.endDate, 'YYYY-MM-DD') : null;
            if (!b || !e) {
                return false;
            }

            if ($ctrl.speciesPeriod.maxPeriod) {
                return e.diff(b, 'days') >= $ctrl.speciesPeriod.maxPeriod;
            }
            return e.diff(b, 'years') >= 1;
        };
    });
