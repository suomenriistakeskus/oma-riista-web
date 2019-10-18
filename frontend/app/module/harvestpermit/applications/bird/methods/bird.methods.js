'use strict';

angular.module('app.harvestpermit.application.bird.methods', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.bird.methods', {
                url: '/methods',
                templateUrl: 'harvestpermit/applications/bird/methods/methods.html',
                controller: 'BirdPermitWizardMethodsController',
                controllerAs: '$ctrl',
                resolve: {
                    forbiddenMethods: function (BirdPermitApplication, applicationId) {
                        return BirdPermitApplication.getCurrentDeviationJustification({id: applicationId}).$promise;
                    }
                }
            })
            .state('jht.decision.application.wizard.bird.methods', {
                url: '/methods',
                templateUrl: 'harvestpermit/applications/bird/methods/methods.html',
                controller: 'BirdPermitWizardMethodsController',
                controllerAs: '$ctrl',
                resolve: {
                    forbiddenMethods: function (BirdPermitApplication, applicationId) {
                        return BirdPermitApplication.getCurrentDeviationJustification({id: applicationId}).$promise;
                    }
                }
            });
    })

    .component('birdApplicationMethodToggle', {
        templateUrl: 'harvestpermit/applications/bird/methods/toggle.html',
        bindings: {
            toggle: '='
        }
    })

    .controller('BirdPermitWizardMethodsController', function ($scope, wizard, BirdPermitApplication,
                                                               UnsavedChangesConfirmationService, ApplicationWizardNavigationHelper,
                                                               applicationId, forbiddenMethods) {

        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.forbiddenMethods = forbiddenMethods;

            _.forEach($ctrl.forbiddenMethods.speciesJustifications, function (species) {
                species.forbiddenMethodsUsed = species.forbiddenMethodsUsed === null || species.forbiddenMethodsUsed;
            });

            $ctrl.toggle32 = !_.isEmpty($ctrl.forbiddenMethods.deviateSection32);
            $ctrl.toggle33 = !_.isEmpty($ctrl.forbiddenMethods.deviateSection33) || $ctrl.forbiddenMethods.tapeRecorders;
            $ctrl.toggle34 = !_.isEmpty($ctrl.forbiddenMethods.deviateSection34) || $ctrl.forbiddenMethods.traps;
            $ctrl.toggle35 = !_.isEmpty($ctrl.forbiddenMethods.deviateSection35);
            $ctrl.toggle51 = !_.isEmpty($ctrl.forbiddenMethods.deviateSection51);

            $scope.$watch('methodsForm.$pristine', function (newVal, oldVal) {
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
            wizard.goto('period');
        };

        $ctrl.next = function () {
            $ctrl.save().then(function () {
                wizard.goto('damage');
            });
        };

        $ctrl.nextDisabled = function (form) {
            return invalid(form);
        };

        $ctrl.save = function () {
            clearUnusedData();
            UnsavedChangesConfirmationService.setChanges(false);
            return BirdPermitApplication.updateDeviationJustification({
                id: applicationId
            }, $ctrl.forbiddenMethods).$promise;
        };

        $ctrl.showSpecies = function () {
            return forbiddenRequested();
        };

        function clearUnusedData() {
            if ($ctrl.toggle32 !== true) {
                $ctrl.forbiddenMethods.deviateSection32 = null;
            }
            if ($ctrl.toggle33 !== true) {
                $ctrl.forbiddenMethods.deviateSection33 = null;
                $ctrl.forbiddenMethods.tapeRecorders = false;
            }
            if ($ctrl.toggle34 !== true) {
                $ctrl.forbiddenMethods.deviateSection34 = null;
                $ctrl.forbiddenMethods.traps = false;
            }
            if ($ctrl.toggle35 !== true) {
                $ctrl.forbiddenMethods.deviateSection35 = null;
            }
            if ($ctrl.toggle51 !== true) {
                $ctrl.forbiddenMethods.deviateSection51 = null;
            }

            var methodsRequested = forbiddenRequested();

            _.forEach($ctrl.forbiddenMethods.speciesJustifications, function (species) {
                if (!methodsRequested) {
                    species.justification = null;
                    species.forbiddenMethodsUsed = null;
                } else if (!species.forbiddenMethodsUsed) {
                    species.justification = null;
                }
            });
        }

        function invalid(form) {
            return forbiddenRequested() && form.$invalid;
        }

        function forbiddenRequested() {
            return $ctrl.toggle32 ||
                $ctrl.toggle33 ||
                $ctrl.toggle34 ||
                $ctrl.toggle35 ||
                $ctrl.toggle51;
        }
    });
