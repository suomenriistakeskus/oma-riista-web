'use strict';

angular.module('app.harvestpermit.application.carnivore.applicant', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.carnivore.applicant', {
                url: '/applicant',
                templateUrl: 'harvestpermit/applications/carnivore/applicant/applicant.html',
                controller: 'CarnivorePermitWizardApplicantController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    application: function (applicationId, HarvestPermitApplications) {
                        return HarvestPermitApplications.get({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            next: 'species'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.carnivore.applicant', {
                url: '/applicant',
                templateUrl: 'harvestpermit/applications/carnivore/applicant/applicant.html',
                controller: 'CarnivorePermitWizardApplicantController',
                controllerAs: '$ctrl',
                resolve: {
                    application: function (applicationId, HarvestPermitApplications) {
                        return HarvestPermitApplications.get({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            next: 'species'
                        };
                    }
                }
            });
    })


    .controller('CarnivorePermitWizardApplicantController', function ($state, states, CarnivorePermitApplication,
                                                                      wizard, application) {
            var $ctrl = this;
            $ctrl.$onInit = function () {
                $ctrl.contactPerson = application.contactPerson;
                $ctrl.permitHolder = application.permitHolder;
            };

            $ctrl.exit = function (form) {
                $ctrl.save().then(wizard.exit());
            };

            $ctrl.constantTrue = _.constant(true);
            $ctrl.previous = function () {
                // no previous
            };

            $ctrl.isNextDisabled = function (form) {
                return form.$invalid;
            };

            $ctrl.next = function () {
                $ctrl.save().then(function () {
                    wizard.goto(states.next);
                });
            };

            $ctrl.save = function () {
                if ($ctrl.permitHolder.type === 'PERSON') {
                    $ctrl.permitHolder.name = $ctrl.contactPerson.firstName + ' ' + $ctrl.contactPerson.lastName;
                    $ctrl.permitHolder.code = "";
                } else if ($ctrl.permitHolder.type === 'OTHER') {
                    $ctrl.permitHolder.code = "";
                }
                return CarnivorePermitApplication.updatePermitHolder({id: application.id}, $ctrl.permitHolder).$promise;
            };
        }
    )
;
