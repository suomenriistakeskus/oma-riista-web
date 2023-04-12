'use strict';

angular.module('app.harvestpermit.application.weapontransportation.applicant', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.weapontransportation.applicant', {
                url: '/applicant',
                templateUrl: 'harvestpermit/applications/weapontransportation/applicant/applicant.html',
                controller: 'WeaponTransportationWizardApplicantController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    application: function (applicationId, HarvestPermitApplications) {
                        return HarvestPermitApplications.get({id: applicationId}).$promise;
                    }
                }
            })
            .state('jht.decision.application.wizard.weapontransportation.applicant', {
                url: '/applicant',
                templateUrl: 'harvestpermit/applications/weapontransportation/applicant/applicant.html',
                controller: 'WeaponTransportationWizardApplicantController',
                controllerAs: '$ctrl',
                resolve: {
                    application: function (applicationId, HarvestPermitApplications) {
                        return HarvestPermitApplications.get({id: applicationId}).$promise;
                    }
                }
            });
    })

    .controller('WeaponTransportationWizardApplicantController', function ($state,
                                                                           wizard, application,
                                                                           WeaponTransportationPermitApplication,
                                                                           ApplicationWizardNavigationHelper) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.contactPerson = application.contactPerson;
            $ctrl.permitHolder = application.permitHolder;
        };

        $ctrl.exit = function (form) {
            ApplicationWizardNavigationHelper.exit($ctrl.isNextDisabled(form), $ctrl.save, wizard.exit);
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
                wizard.goto('reason');
            });
        };

        $ctrl.save = function () {
            if ($ctrl.permitHolder.type === 'PERSON') {
                $ctrl.permitHolder.name = $ctrl.contactPerson.firstName + ' ' + $ctrl.contactPerson.lastName;
                $ctrl.permitHolder.code = "";
            } else if ($ctrl.permitHolder.type === 'OTHER') {
                $ctrl.permitHolder.code = "";
            }
            return WeaponTransportationPermitApplication.updatePermitHolder({id: application.id}, $ctrl.permitHolder).$promise;
        };
    });
