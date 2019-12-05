'use strict';

angular.module('app.harvestpermit.application.wizard.area', ['app.metadata', 'app.harvestpermit.area'])
    .controller('DerogationPermitWizardMapController', function ($state, $stateParams, $http, $translate, $scope,
                                                                 ApplicationWizardNavigationHelper,
                                                                 UnsavedChangesConfirmationService,
                                                                 DerogationApplicationAddClubAreaModal,
                                                                 DerogationApplicationAddPersonalAreaModal,
                                                                 DerogationApplicationAddPersonalAreaUnionModal,
                                                                 NotificationService,
                                                                 TranslatedBlockUI, HarvestPermitAddPartnerAreaErrorModal,
                                                                 DerogationPermitApplication, HarvestPermitApplications,
                                                                 states, wizard, applicationId, areaInfo, attachmentList) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.areaInfo = areaInfo;
            $ctrl.attachmentList = attachmentList;
            $ctrl.attachmentBaseUri = '/api/v1/harvestpermit/application/' + applicationId + '/attachment';

            $ctrl.attachmentConfig = {
                baseUri: '/api/v1/harvestpermit/application/' + applicationId + '/attachment',
                canDownload: true,
                canDelete: true
            };
            if ($ctrl.areaInfo.areaDescription) {
                $ctrl.firstPanelOpen = false;
                $ctrl.secondPanelOpen = false;
                $ctrl.thirdPanelOpen = true;
            } else {
                $ctrl.firstPanelOpen = true;
                $ctrl.secondPanelOpen = false;
                $ctrl.thirdPanelOpen = false;
            }

            $scope.$watch('mammalHuntingAreaForm.$pristine', function (newVal, oldVal) {
                if (oldVal && !newVal) {
                    UnsavedChangesConfirmationService.setChanges(true);
                }
            }, true);
        };

        $ctrl.exit = function (form) {
            ApplicationWizardNavigationHelper.previous(invalid(form), $ctrl.save, wizard.exit);
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
            UnsavedChangesConfirmationService.setChanges(false);
            return DerogationPermitApplication.updateArea({id: applicationId}, $ctrl.areaInfo).$promise;
        };

        $ctrl.refreshAttachments = function () {
            HarvestPermitApplications.getAttachments({id: applicationId}).$promise.then(function (res) {
                $ctrl.attachmentList = res;
            });
        };

        $ctrl.addAreaUnion = function () {
            HarvestPermitApplications.get({id: applicationId}).$promise.then(function (application) {
                var personId = $stateParams.id || application.contactPerson.id;
                DerogationApplicationAddPersonalAreaUnionModal.open(application, personId).then(function (externalId) {
                    addAreaAttachment(applicationId, externalId);
                });
            });
        };

        $ctrl.addPersonalArea = function () {
            HarvestPermitApplications.get({id: applicationId}).$promise.then(function (application) {
                var personId = $stateParams.id || application.contactPerson.id;
                DerogationApplicationAddPersonalAreaModal.open(application, personId).then(function (externalId) {
                    addAreaAttachment(applicationId, externalId);
                });
            });
        };

        $ctrl.addClubArea = function () {
            HarvestPermitApplications.get({id: applicationId}).$promise.then(function (application) {
                DerogationApplicationAddClubAreaModal.open(application).then(function (externalId) {
                    addAreaAttachment(applicationId, externalId);
                });
            });
        };

        function addAreaAttachment(applicationId, externalId) {
            TranslatedBlockUI.start("global.block.wait");
            DerogationPermitApplication.addAreaAttachment({
                id: applicationId,
                externalId: externalId
            }).$promise.then(function () {
                    $ctrl.refreshAttachments();
                }, HarvestPermitAddPartnerAreaErrorModal.open
            ).finally(TranslatedBlockUI.stop);
        }

        function invalid(form) {
            return form.$invalid
                || !validLocation($ctrl.areaInfo.geoLocation)
                || (_.isEmpty($ctrl.attachmentList) && !$ctrl.areaInfo.areaDescription);
        }

        function validLocation(l) {
            return _.isObject(l) && _.isFinite(l.longitude) && _.isFinite(l.latitude);
        }

    })
    .service('DerogationApplicationAddPersonalAreaUnionModal', function ($uibModal) {
        this.open = function (application, personId) {
            return $uibModal.open({
                    templateUrl: 'harvestpermit/area/add-area-modal.html',
                    controllerAs: '$ctrl',
                    controller: 'HarvestPermitAreaAddPartnerModalController',
                    size: 'lg',
                    resolve: {
                        application: function () {
                            return application;
                        },
                        huntingYear: function () {
                            return application.huntingYear;
                        },
                        areaList: function (PersonalAreaUnions) {
                            return PersonalAreaUnions.listReadyUnionsForPerson({
                                huntingYear: application.huntingYear,
                                personId: personId
                            }).$promise;

                        },
                        availableClubs: _.constant([]),
                        showClubs: _.constant(false)
                    },
                }
            ).result;
        };
    })
    .service('DerogationApplicationAddPersonalAreaModal', function ($uibModal) {
        this.open = function (application, personId) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/area/add-area-modal.html',
                controllerAs: '$ctrl',
                controller: 'HarvestPermitAreaAddPartnerModalController',
                size: 'lg',
                resolve: {
                    application: function () {
                        return application;
                    },
                    huntingYear: function () {
                        return application.huntingYear;
                    },
                    areaList: function (AccountAreas) {
                        return AccountAreas.listForPerson({personId: personId}).$promise;
                    },
                    availableClubs: _.constant([]),
                    showClubs: _.constant(false)
                }
            }).result;
        };

    })
    .service('DerogationApplicationAddClubAreaModal', function ($uibModal) {
        this.open = function (application) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/area/add-area-modal.html',
                controllerAs: '$ctrl',
                controller: 'HarvestPermitAreaAddPartnerModalController',
                size: 'lg',
                resolve: {
                    application: function () {
                        return application;
                    },
                    huntingYear: function () {
                        return application.huntingYear;
                    },
                    areaList: function () {
                        return [];
                    },
                    availableClubs: function (DerogationPermitApplication) {
                        return DerogationPermitApplication.listAvailableClubs({id: application.id}).$promise;
                    },
                    showClubs: _.constant(true)
                }
            }).result;
        };

    })
;
