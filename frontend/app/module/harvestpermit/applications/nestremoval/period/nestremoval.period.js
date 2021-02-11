'use strict';

angular.module('app.harvestpermit.application.nestremoval.period', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.nestremoval.period', {
                url: '/period',
                templateUrl: 'harvestpermit/applications/nestremoval/period/period.html',
                controller: 'NestRemovalPermitWizardPeriodController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesPeriodList: function (NestRemovalPermitApplication, applicationId) {
                        return NestRemovalPermitApplication.listSpeciesPeriods({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'reasons',
                            next: 'damage'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.nestremoval.period', {
                url: '/period',
                templateUrl: 'harvestpermit/applications/nestremoval/period/period.html',
                controller: 'NestRemovalPermitWizardPeriodController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesPeriodList: function (NestRemovalPermitApplication, applicationId) {
                        return NestRemovalPermitApplication.listSpeciesPeriods({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'reasons',
                            next: 'damage'
                        };
                    }
                }
            });
    })

    .controller('NestRemovalPermitWizardPeriodController', function ($scope, $q, ApplicationWizardNavigationHelper,
                                                                     UnsavedChangesConfirmationService, Helpers,
                                                                     NestRemovalPermitApplication, applicationId,
                                                                     states, wizard, speciesPeriodList) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.speciesPeriodList = speciesPeriodList.speciesPeriods;
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
            return NestRemovalPermitApplication.saveSpeciesPeriods({id: applicationId}, {
                speciesPeriods: $ctrl.speciesPeriodList,
            }).$promise;
        };

        function invalid(form) {
            return form.$invalid;
        }
    });
