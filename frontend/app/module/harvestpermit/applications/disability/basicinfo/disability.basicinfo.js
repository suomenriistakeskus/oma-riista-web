'use strict';

angular.module('app.harvestpermit.application.disability.basicinfo', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.disability.basicinfo', {
                url: '/basicinfo',
                templateUrl: 'harvestpermit/applications/disability/basicinfo/basicinfo.html',
                controller: 'DisabilityPermitWizardBasicInfoController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    basicInfo: function (DisabilityPermitApplication, applicationId) {
                        return DisabilityPermitApplication.getBasicInfo({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'applicant',
                            next: 'justification'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.disability.basicinfo', {
                url: '/basicinfo',
                templateUrl: 'harvestpermit/applications/disability/basicinfo/basicinfo.html',
                controller: 'DisabilityPermitWizardBasicInfoController',
                controllerAs: '$ctrl',
                resolve: {
                    basicInfo: function (DisabilityPermitApplication, applicationId) {
                        return DisabilityPermitApplication.getBasicInfo({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'applicant',
                            next: 'justification'
                        };
                    }
                }
            });
    })

    .controller('DisabilityPermitWizardBasicInfoController', function ($scope, $q, ApplicationWizardNavigationHelper,
                                                                       UnsavedChangesConfirmationService, Helpers,
                                                                       DisabilityPermitApplication, applicationId,
                                                                       states, wizard, basicInfo) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.basicInfo = basicInfo ||
                {
                    useMotorVehicle: false,
                    useVehicleForWeaponTransport: false
                };
            $scope.$watch('disabilityBasicInfoForm.$pristine', function (newVal, oldVal) {
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
            return DisabilityPermitApplication
                .updateBasicInfo({id: applicationId}, $ctrl.basicInfo)
                .$promise;
        };

        function invalid(form) {
            return form.$invalid || $ctrl.isEndDateBeforeBeginDate() || $ctrl.isReasonInvalid();
        }

        $ctrl.isEndDateBeforeBeginDate = function () {
            var b = $ctrl.basicInfo.beginDate ? Helpers.toMoment($ctrl.basicInfo.beginDate, 'YYYY-MM-DD') : null;
            var e = $ctrl.basicInfo.endDate ? Helpers.toMoment($ctrl.basicInfo.endDate, 'YYYY-MM-DD') : null;

            return b && e && e.isBefore(b);
        };

       $ctrl.isReasonInvalid = function () {
            return !$ctrl.basicInfo.useMotorVehicle && !$ctrl.basicInfo.useVehicleForWeaponTransport;
        };

    });
