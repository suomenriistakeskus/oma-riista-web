'use strict';

angular.module('app.harvestpermit.application.bird.damage', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.bird.damage', {
                url: '/damage',
                templateUrl: 'harvestpermit/applications/bird/damage/damage.html',
                controller: 'BirdPermitWizardDamageController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesDamageList: function (BirdPermitApplication, applicationId) {
                        return BirdPermitApplication.getSpeciesDamage({id: applicationId}).$promise;
                    }
                }
            })
            .state('jht.decision.application.wizard.bird.damage', {
                url: '/damage',
                templateUrl: 'harvestpermit/applications/bird/damage/damage.html',
                controller: 'BirdPermitWizardDamageController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesDamageList: function (BirdPermitApplication, applicationId) {
                        return BirdPermitApplication.getSpeciesDamage({id: applicationId}).$promise;
                    }
                }
            });
    })

    .controller('BirdPermitWizardDamageController', function ($scope, UnsavedChangesConfirmationService,
                                                              BirdPermitApplication, ApplicationWizardNavigationHelper,
                                                              wizard, speciesDamageList, applicationId) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.speciesDamageList = speciesDamageList;
            $scope.$watch('speciesDamageForm.$pristine', function (newVal, oldVal) {
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
            wizard.goto('methods');
        };

        $ctrl.next = function () {
            $ctrl.save().then(function () {
                wizard.goto('population');
            });
        };

        function invalid(form) {
            return form.$invalid;
        }

        $ctrl.nextDisabled = function (form) {
            return form.$invalid;
        };

        $ctrl.save = function () {
            UnsavedChangesConfirmationService.setChanges(false);
            return BirdPermitApplication.updateSpeciesDamage({id: applicationId}, {list: $ctrl.speciesDamageList}).$promise;
        };
    });
