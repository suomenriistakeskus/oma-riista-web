'use strict';

angular.module('app.harvestpermit.application.bird.cause', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.bird.cause', {
                url: '/cause',
                templateUrl: 'harvestpermit/applications/bird/cause/cause.html',
                controller: 'BirdPermitWizardCauseController',
                controllerAs: '$ctrl',
                resolve: {
                    permitCause: function (BirdPermitApplication, applicationId) {
                        return BirdPermitApplication.getCurrentPermitCause({id: applicationId}).$promise;
                    }
                }
            })
            .state('jht.decision.application.wizard.bird.cause', {
                url: '/cause',
                templateUrl: 'harvestpermit/applications/bird/cause/cause.html',
                controller: 'BirdPermitWizardCauseController',
                controllerAs: '$ctrl',
                resolve: {
                    permitCause: function (BirdPermitApplication, applicationId) {
                        return BirdPermitApplication.getCurrentPermitCause({id: applicationId}).$promise;
                    }
                }
            });
    })

    .controller('BirdPermitWizardCauseController', function ($scope, BirdPermitApplication, applicationId,
                                                             UnsavedChangesConfirmationService, ApplicationWizardNavigationHelper,
                                                             permitCause, wizard) {

        var $ctrl = this;

        $ctrl.$onInit = function () {

            $ctrl.permitCause = permitCause;
            $scope.$watch('deviationForm.$pristine', function (newVal, oldVal) {
                if (oldVal && !newVal) {
                    UnsavedChangesConfirmationService.setChanges(true);
                }
            }, true);
        };

        $ctrl.exit = function () {
            ApplicationWizardNavigationHelper.exit(!isSomeCauseSelected(), $ctrl.save, wizard.exit);
        };


        $ctrl.previous = function () {
            ApplicationWizardNavigationHelper.previous(!isSomeCauseSelected(), $ctrl.save, $ctrl.doGotoPrevious);
        };

        $ctrl.doGotoPrevious = function () {
            wizard.goto('map');
        };

        $ctrl.previousDisabled = function () {
            return !isSomeCauseSelected();
        };

        $ctrl.next = function () {

            $ctrl.save().then(function () {
                wizard.goto('period');
            });
        };

        $ctrl.nextDisabled = function () {
            return !isSomeCauseSelected();
        };

        $ctrl.save = function () {
            UnsavedChangesConfirmationService.setChanges(false);
            return BirdPermitApplication.updatePermitCause({
                id: applicationId
            }, $ctrl.permitCause).$promise;
        };

        function isSomeCauseSelected() {
            return $ctrl.permitCause.causePublicHealth ||
                $ctrl.permitCause.causePublicSafety ||
                $ctrl.permitCause.causeAviationSafety ||
                $ctrl.permitCause.causeCropsDamage ||
                $ctrl.permitCause.causeDomesticPets ||
                $ctrl.permitCause.causeForestDamage ||
                $ctrl.permitCause.causeFishing ||
                $ctrl.permitCause.causeWaterSystem ||
                $ctrl.permitCause.causeFlora ||
                $ctrl.permitCause.causeFauna ||
                $ctrl.permitCause.causeResearch;
        }
    });
