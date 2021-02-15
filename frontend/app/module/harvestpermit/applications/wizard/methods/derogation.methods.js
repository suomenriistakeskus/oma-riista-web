'use strict';

angular.module('app.harvestpermit.application.wizard.methods', ['app.metadata'])

    .component('derogationApplicationMethodToggle', {
        templateUrl: 'harvestpermit/applications/wizard/methods/toggle.html',
        bindings: {
            toggle: '='
        }
    })

    .controller('DerogationPermitWizardMethodsController', function ($scope, wizard,
                                                                     UnsavedChangesConfirmationService, ApplicationWizardNavigationHelper,
                                                                     applicationId, forbiddenMethods, updateResource, states) {

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
            wizard.goto(states.previous);
        };

        $ctrl.next = function () {
            $ctrl.save().then(function () {
                wizard.goto(states.next);
            });
        };

        $ctrl.nextDisabled = function (form) {
            return invalid(form);
        };

        $ctrl.save = function () {
            clearUnusedData();
            UnsavedChangesConfirmationService.setChanges(false);
            return updateResource.updateDeviationJustification({
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
    })


    .component('derogationApplicationSummaryMethods', {
        templateUrl: 'harvestpermit/applications/wizard/methods/summary-methods.html',
        bindings: {
            methods: '<'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.show32 = !_.isEmpty($ctrl.methods.deviateSection32);
                $ctrl.show33 = !_.isEmpty($ctrl.methods.deviateSection33) || $ctrl.methods.tapeRecorders;
                $ctrl.show34 = !_.isEmpty($ctrl.methods.deviateSection34) || $ctrl.methods.traps;
                $ctrl.show35 = !_.isEmpty($ctrl.methods.deviateSection35);
                $ctrl.show51 = !_.isEmpty($ctrl.methods.deviateSection51);

                $ctrl.showSpecies = $ctrl.show32 || $ctrl.show33 || $ctrl.show34 || $ctrl.show35 || $ctrl.show51;
            };
        }
    })
;
