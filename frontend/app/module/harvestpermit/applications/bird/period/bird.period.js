'use strict';

angular.module('app.harvestpermit.application.bird.period', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.bird.period', {
                url: '/period',
                templateUrl: 'harvestpermit/applications/bird/period/period.html',
                controller: 'BirdPermitWizardPeriodController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesPeriodList: function (BirdPermitApplication, applicationId) {
                        return BirdPermitApplication.listSpeciesPeriods({id: applicationId}).$promise;
                    }
                }
            })
            .state('jht.decision.application.wizard.bird.period', {
                url: '/period',
                templateUrl: 'harvestpermit/applications/bird/period/period.html',
                controller: 'BirdPermitWizardPeriodController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesPeriodList: function (BirdPermitApplication, applicationId) {
                        return BirdPermitApplication.listSpeciesPeriods({id: applicationId}).$promise;
                    }
                }
            });
    })

    .controller('BirdPermitWizardPeriodController', function ($scope, BirdPermitApplication, Helpers,
                                                              UnsavedChangesConfirmationService, ApplicationWizardNavigationHelper,
                                                              wizard, speciesPeriodList, applicationId) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.speciesPeriodList = speciesPeriodList.speciesPeriods;
            $ctrl.validityYears = speciesPeriodList.validityYears;
            $ctrl.limitlessPermitAllowed = speciesPeriodList.limitlessPermitAllowed;
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
            wizard.goto('cause');
        };

        $ctrl.next = function () {
            $ctrl.save().then(function () {
                wizard.goto('methods');
            });
        };

        $ctrl.nextDisabled = function (form) {
            return invalid(form);
        };

        $ctrl.save = function () {
            UnsavedChangesConfirmationService.setChanges(false);
            return BirdPermitApplication.saveSpeciesPeriods({id: applicationId}, {
                speciesPeriods: $ctrl.speciesPeriodList,
                validityYears: $ctrl.validityYears
            }).$promise;
        };

        $ctrl.isIntervalIllegal = function (species) {
            var b = species.beginDate ? Helpers.toMoment(species.beginDate, 'YYYY-MM-DD') : null;
            var e = species.endDate ? Helpers.toMoment(species.endDate, 'YYYY-MM-DD') : null;

            return b !== null && e !== null && b.get('year') !== e.get('year');
        };

        $ctrl.isLimitlessDisallowed = function () {
            return $ctrl.validityYears === 0 && !$ctrl.limitlessPermitAllowed;
        };

        function invalid(form) {
            return form.$invalid
                || !isValidLimit($ctrl.validityYears, $ctrl.limitlessPermitAllowed)
                || _.some($ctrl.speciesPeriodList, function (s) {
                    return $ctrl.isIntervalIllegal(s);
                });
        }

        function isValidLimit(years, limitlessPermitAllowed) {
            return years === 0 && limitlessPermitAllowed || years >= 1 && years <= 5;
        }
    });
