'use strict';

angular.module('app.harvestpermit.application.gamemanagement.period', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.gamemanagement.period', {
                url: '/period',
                templateUrl: 'harvestpermit/applications/gamemanagement/period/period.html',
                controller: 'GameManagementPermitWizardPeriodController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesPeriod: function (GameManagementPermitApplication, applicationId) {
                        return GameManagementPermitApplication.getSpeciesPeriod({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'species',
                            next: 'map'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.gamemanagement.period', {
                url: '/period',
                templateUrl: 'harvestpermit/applications/gamemanagement/period/period.html',
                controller: 'GameManagementPermitWizardPeriodController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesPeriod: function (GameManagementPermitApplication, applicationId) {
                        return GameManagementPermitApplication.getSpeciesPeriod({id: applicationId}).$promise;
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

    .controller('GameManagementPermitWizardPeriodController', function ($scope, $q, ApplicationWizardNavigationHelper,
                                                                        UnsavedChangesConfirmationService, Helpers,
                                                                        GameManagementPermitApplication, applicationId,
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
            return GameManagementPermitApplication.saveSpeciesPeriod({id: applicationId}, $ctrl.speciesPeriod).$promise;
        };

        $ctrl.isIntervalTooLong = function () {
            var b = $ctrl.speciesPeriod.beginDate ? Helpers.toMoment($ctrl.speciesPeriod.beginDate, 'YYYY-MM-DD') : null;
            var e = $ctrl.speciesPeriod.endDate ? Helpers.toMoment($ctrl.speciesPeriod.endDate, 'YYYY-MM-DD') : null;
            if (!b || !e) {
                return false;
            }
            return e.diff(b, 'years') >= 1;
        };

        function invalid(form) {
            return form.$invalid || $ctrl.isIntervalTooLong();
        }
    });
