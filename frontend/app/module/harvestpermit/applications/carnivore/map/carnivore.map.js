'use strict';

angular.module('app.harvestpermit.application.carnivore.map', ['app.metadata', 'app.harvestpermit.area'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.carnivore.map', {
                url: '/map',
                templateUrl: 'harvestpermit/applications/carnivore/map/map.html',
                controller: 'CarnivorePermitWizardMapController',
                controllerAs: '$ctrl',
                resolve: {
                    areaInfo: function (CarnivorePermitApplication, applicationId) {
                        return CarnivorePermitApplication.getArea({id: applicationId}).$promise;
                    },
                    attachmentList: function (HarvestPermitApplications, applicationId) {
                        return HarvestPermitApplications.getAttachments({id: applicationId}).$promise;
                    },
                    states: function () {
                        return {
                            previous: 'justification',
                            next: 'attachments'
                        };
                    }
                }
            }).state('jht.decision.application.wizard.carnivore.map', {
            url: '/map',
            templateUrl: 'harvestpermit/applications/carnivore/map/map.html',
            controller: 'CarnivorePermitWizardMapController',
            controllerAs: '$ctrl',
            resolve: {
                areaInfo: function (CarnivorePermitApplication, applicationId) {
                    return CarnivorePermitApplication.getArea({id: applicationId}).$promise;
                },
                attachmentList: function (HarvestPermitApplications, applicationId) {
                    return HarvestPermitApplications.getAttachments({id: applicationId}).$promise;
                },
                states: function () {
                    return {
                        previous: 'justification',
                        next: 'attachments'
                    };
                }
            }
        });
    })

    .controller('CarnivorePermitWizardMapController', function ($state, $http, $translate, $scope, dialogs,
                                                                ApplicationWizardNavigationHelper,
                                                                UnsavedChangesConfirmationService,
                                                                CarnivoreAddAreaModal, NotificationService,
                                                                TranslatedBlockUI, HarvestPermitAddPartnerAreaErrorModal,
                                                                CarnivorePermitApplication, HarvestPermitApplications,
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

            $scope.$watch('carnivoreHuntingAreaForm.$pristine', function (newVal, oldVal) {
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
            return CarnivorePermitApplication.updateArea({id: applicationId}, $ctrl.areaInfo).$promise;
        };

        $ctrl.refreshAttachments = function () {
            HarvestPermitApplications.getAttachments({id: applicationId}).$promise.then(function (res) {
                $ctrl.attachmentList = res;
            });
        };

        $ctrl.addAreaUnion = function () {
            CarnivoreAddAreaModal.open(applicationId, 'union').then(function (externalId) {
                TranslatedBlockUI.start("global.block.wait");
                CarnivorePermitApplication.addAreaAttachment({
                    id: applicationId,
                    externalId: externalId
                }).$promise.then(function () {
                        $ctrl.refreshAttachments();
                    }, HarvestPermitAddPartnerAreaErrorModal.open
                ).finally(TranslatedBlockUI.stop);
            });
        };

        $ctrl.addPersonalArea = function () {
            CarnivoreAddAreaModal.open(applicationId, 'personal').then(function (externalId) {
                TranslatedBlockUI.start("global.block.wait");
                CarnivorePermitApplication.addAreaAttachment({
                    id: applicationId,
                    externalId: externalId
                }).$promise.then(function () {
                        $ctrl.refreshAttachments();
                    }, HarvestPermitAddPartnerAreaErrorModal.open
                ).finally(TranslatedBlockUI.stop);
            });
        };

        $ctrl.addClubArea = function () {
            CarnivoreAddAreaModal.open(applicationId, 'club').then(function (externalId) {
                TranslatedBlockUI.start("global.block.wait");
                CarnivorePermitApplication.addAreaAttachment({
                    id: applicationId,
                    externalId: externalId
                }).$promise.then(function () {
                        $ctrl.refreshAttachments();
                    }, HarvestPermitAddPartnerAreaErrorModal.open
                ).finally(TranslatedBlockUI.stop);
            });
        };

        function invalid(form) {
            return form.$invalid
                || !validLocation($ctrl.areaInfo.geoLocation)
                || (_.isEmpty($ctrl.attachmentList) && !$ctrl.areaInfo.areaDescription);
        }

        function validLocation(l) {
            return _.isObject(l) && _.isFinite(l.longitude) && _.isFinite(l.latitude);
        }

    })
    .service('CarnivoreAddAreaModal', function ($uibModal) {
        this.open = function (applicationId, areaType) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/area/add-area-modal.html',
                controllerAs: '$ctrl',
                controller: 'HarvestPermitAreaAddPartnerModalController',
                size: 'lg',
                resolve: {
                    application: function (HarvestPermitApplications) {
                        return HarvestPermitApplications.get({id: applicationId}).$promise;
                    },
                    huntingYear: function (HarvestPermitApplications) {
                        return HarvestPermitApplications.get({id: applicationId}).$promise.then(function (application) {
                            return application.huntingYear;
                        });
                    },
                    areaList: function ($stateParams, AccountAreas, PersonalAreaUnions, HarvestPermitApplications) {
                        if (areaType === 'union' || areaType === 'personal') {
                            // TODO: Refactor multiple requests to HarvestPermitApplications.get
                            return HarvestPermitApplications.get({id: applicationId}).$promise
                                .then(function (application) {
                                    var personId = $stateParams.id || application.contactPerson.id;

                                    if (areaType === 'union') {
                                        return PersonalAreaUnions.listReadyUnionsForPerson({
                                            huntingYear: application.huntingYear,
                                            personId: personId
                                        }).$promise;

                                    } else {
                                        return HarvestPermitApplications.get({id: applicationId}).$promise
                                            .then(function (application) {
                                                return AccountAreas.listForPerson({personId: personId}).$promise;
                                            });

                                    }
                                });
                        }
                        return [];
                    },
                    availableClubs: function (CarnivorePermitApplication) {
                        if (areaType === 'club') {
                            return CarnivorePermitApplication.listAvailableClubs({id: applicationId}).$promise;
                        }
                        return [];
                    },
                    showClubs: _.constant(areaType === 'club')
                }
            }).result;
        };

    })
;
