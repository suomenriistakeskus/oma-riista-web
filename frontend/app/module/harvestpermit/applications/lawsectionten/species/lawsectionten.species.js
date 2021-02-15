'use strict';

angular.module('app.harvestpermit.application.lawsectionten.species', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.lawsectionten.species', {
                url: '/species',
                templateUrl: 'harvestpermit/applications/lawsectionten/species/species.html',
                controller: 'LawSectionTenPermitWizardSpeciesController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesAmount: function (LawSectionTenPermitApplication, applicationId) {
                        return LawSectionTenPermitApplication.getSpeciesAmount({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'applicant',
                            next: 'map'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.lawsectionten.species', {
                url: '/species',
                templateUrl: 'harvestpermit/applications/lawsectionten/species/species.html',
                controller: 'LawSectionTenPermitWizardSpeciesController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesAmount: function (LawSectionTenPermitApplication, applicationId) {
                        return LawSectionTenPermitApplication.getSpeciesAmount({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'applicant',
                            next: 'map'
                        };
                    }
                }
            });
    })

    .controller('LawSectionTenPermitWizardSpeciesController', function ($scope, LawSectionTenPermitApplication,
                                                                        UnsavedChangesConfirmationService,
                                                                        ApplicationWizardNavigationHelper,
                                                                        states, wizard, applicationId, speciesAmount) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.speciesAmount = speciesAmount || {};
            $scope.$watch('speciesAmountForm.$pristine', function (newVal, oldVal) {
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
            return LawSectionTenPermitApplication.saveSpeciesAmount({id: applicationId}, $ctrl.speciesAmount).$promise;
        };

        function invalid(form) {
            return form.$invalid || !$ctrl.speciesAmount.gameSpeciesCode;
        }

    });
