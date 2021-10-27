'use strict';

angular.module('app.harvestpermit.application.wizard.area', ['app.metadata', 'app.harvestpermit.area'])
    .controller('DerogationPermitWizardMapController', function ($state, $stateParams, $http, $translate, $scope,
                                                                 ApplicationWizardNavigationHelper,
                                                                 UnsavedChangesConfirmationService,
                                                                 NotificationService,
                                                                 TranslatedBlockUI,
                                                                 DerogationPermitApplication, HarvestPermitApplications,
                                                                 states, wizard, applicationId, areaInfo) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.areaInfo = areaInfo;

            $scope.$watch('derogationAreaForm.$pristine', function (newVal, oldVal) {
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
            UnsavedChangesConfirmationService.setChanges(false);
            return DerogationPermitApplication.updateArea({id: applicationId}, $ctrl.areaInfo).$promise;
        };

        function invalid(form) {
            return form.$invalid
                || !validLocation($ctrl.areaInfo.geoLocation);
        }

        function validLocation(l) {
            return _.isObject(l) && _.isFinite(l.longitude) && _.isFinite(l.latitude);
        }

    })

    .controller('DerogationPermitWizardMapDetailsController', function ($state, $stateParams, $http, $translate, $scope,
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
            $ctrl.applicationId = applicationId;
            $ctrl.areaInfo = areaInfo;
            $ctrl.attachmentList = filterAreaAttachments(attachmentList);

            $ctrl.attachmentConfig = {
                baseUri: '/api/v1/harvestpermit/application/' + $ctrl.applicationId + '/attachment',
                canDownload: true,
                canDelete: true
            };

            if ($ctrl.attachmentList && $ctrl.attachmentList.length > 0) {
                // Omit free form description if attachment present
                $ctrl.areaInfo.areaDescription = null;
            }

            $scope.$watch('derogationAreaDetailsForm.$pristine', function (newVal, oldVal) {
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

        $ctrl.clearAreaDescription = function () {
            $ctrl.areaInfo.areaDescription = null;
        };

        $ctrl.save = function () {
            UnsavedChangesConfirmationService.setChanges(false);
            return DerogationPermitApplication.updateArea({id: applicationId}, $ctrl.areaInfo).$promise;
        };

        $ctrl.refreshAttachments = function () {
            HarvestPermitApplications.getAttachments({id: applicationId}).$promise.then(function (res) {
                $ctrl.attachmentList = filterAreaAttachments(res);
            });
        };

        function filterAreaAttachments(attachments){
            return _.filter(attachments, ['type', 'PROTECTED_AREA']);
        }


        function invalid(form) {
            return form.$invalid ||
                (_.isEmpty($ctrl.attachmentList) && !$ctrl.areaInfo.areaDescription);
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

    .component('derogationAreaDetailsDescription', {
        templateUrl: 'harvestpermit/applications/wizard/area/mapdetails-description.html',
        bindings: {
            areaInfo: '<',
            clearAreaDescription: '&'
        },
    })

    .component('derogationAreaDetailsAccordion', {
        templateUrl: 'harvestpermit/applications/wizard/area/mapdetails-accordion.html',
        bindings: {
            applicationId: '<',
            areaInfo: '<',
            attachmentList: '<',
            refreshAttachments: '&'
        },
        controller: function ($stateParams, DerogationPermitApplication, DerogationApplicationAddClubAreaModal,
                              DerogationApplicationAddPersonalAreaModal,
                              DerogationApplicationAddPersonalAreaUnionModal,
                              HarvestPermitAddPartnerAreaErrorModal,
                              HarvestPermitApplications, TranslatedBlockUI) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.attachmentBaseUri = '/api/v1/harvestpermit/application/' + $ctrl.applicationId + '/attachment';

                $ctrl.areaFreeDescription = $ctrl.areaInfo.areaDescription;

                $ctrl.firstPanelOpen = true;
                $ctrl.secondPanelOpen = false;
                $ctrl.thirdPanelOpen = false;
            };

            $ctrl.isAreaDefined = function () {
                return !_.isEmpty($ctrl.areaInfo.areaDescription) || $ctrl.attachmentList.length > 0;
            };

            $ctrl.assignAreaDescription = function (description) {
                $ctrl.areaInfo.areaDescription = description;
            };

            $ctrl.addAreaUnion = function () {
                HarvestPermitApplications.get({id: $ctrl.applicationId}).$promise.then(function (application) {
                    var personId = $stateParams.id || application.contactPerson.id;
                    DerogationApplicationAddPersonalAreaUnionModal.open(application, personId).then(function (externalId) {
                        addAreaAttachment($ctrl.applicationId, externalId);
                    });
                });
            };

            $ctrl.addPersonalArea = function () {
                HarvestPermitApplications.get({id: $ctrl.applicationId}).$promise.then(function (application) {
                    var personId = $stateParams.id || application.contactPerson.id;
                    DerogationApplicationAddPersonalAreaModal.open(application, personId).then(function (externalId) {
                        addAreaAttachment($ctrl.applicationId, externalId);
                    });
                });
            };

            $ctrl.addClubArea = function () {
                HarvestPermitApplications.get({id: $ctrl.applicationId}).$promise.then(function (application) {
                    DerogationApplicationAddClubAreaModal.open(application).then(function (externalId) {
                        addAreaAttachment($ctrl.applicationId, externalId);
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
        }
    })

    .component('derogationApplicationLocationEditor', {
        templateUrl: 'harvestpermit/applications/wizard/area/location.html',
        bindings: {
            protectedArea: '<'
        },
        controller: function ($state, $scope, MapDefaults, MapUtil, MapState, GIS) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.mapState = MapState.get();
                $ctrl.mapDefaults = MapDefaults.create({scrollWheelZoom: false});
                $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents(['click']);

                var geoLocation;

                if ($ctrl.protectedArea && $ctrl.protectedArea.geoLocation) {
                    geoLocation = $ctrl.protectedArea.geoLocation;
                    updateRhy(geoLocation.longitude, geoLocation.latitude);
                } else {
                    geoLocation = null;
                }

                MapState.updateMapCenter(geoLocation
                    ? angular.copy(geoLocation)
                    : MapUtil.getDefaultGeoLocation(), 6);

                $scope.$watchGroup([
                    '$ctrl.protectedArea.geoLocation.longitude',
                    '$ctrl.protectedArea.geoLocation.latitude'
                ], function (newValues, oldValues) {
                    if (!(newValues[0] === oldValues[0] && newValues[1] === oldValues[1])) {
                        updateRhy(newValues[0], newValues[1]);
                    }
                });
            };

            function updateRhy(longitude, latitude) {
                if (_.isFinite(longitude) && _.isFinite(latitude)) {
                    GIS.getRhyForGeoLocation({
                        latitude: latitude,
                        longitude: longitude
                    }).then(function (res) {
                        $ctrl.rhy = res.data;
                    });
                }
            }
        }
    })
;
