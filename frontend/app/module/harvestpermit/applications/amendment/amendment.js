'use strict';

angular.module('app.harvestpermit.application.amendment', [])

    .factory('HarvestPermitAmendmentApplications', function ($resource) {
        var apiPrefix = 'api/v1/harvestpermit/application/amendment/:id';

        function xMethod(method, suffix, isArray) {
            return {method: method, url: apiPrefix + suffix, isArray: !!isArray};
        }

        function getMethod(suffix, isArray) {
            return xMethod('GET', suffix, isArray);
        }

        function postMethod(suffix, isArray) {
            return xMethod('POST', suffix, isArray);
        }

        function putMethod(suffix, isArray) {
            return xMethod('PUT', suffix, isArray);
        }

        return $resource(apiPrefix, {id: '@id'}, {
            createAmendmentApplication: postMethod('/'),
            update: putMethod('/'),
            listPartners: getMethod('/partners', true),
            listSpecies: getMethod('/species', true),
            list: getMethod('/list/:permitId', true)
        });
    })

    .service('HarvestPermitAmendmentApplicationService', function ($q, $state,
                                                                   HarvestPermitAmendmentApplications,
                                                                   HarvestPermitAmendmentApplicationsModal) {
        var self = this;

        self.openModal = function (permit) {
            HarvestPermitAmendmentApplicationsModal.showModal(permit).then(function (res) {
                if (!res) {
                    return;
                }
                if (res.applicationId) {
                    return $state.go('permitmanagement.amendment', {applicationId: res.applicationId});
                } else if (res.nonEdibleHarvestId) {
                    return self.createNewAmendmentApplication({
                        originalPermitId: permit.id,
                        nonEdibleHarvestId: res.nonEdibleHarvestId
                    });
                } else if (res.gameSpeciesCode) {
                    return self.createNewAmendmentApplication({
                        originalPermitId: permit.id,
                        gameSpeciesCode: res.gameSpeciesCode
                    });
                } else {
                    // should not happen
                    console.log('Unknown result from modal', res);
                }
            });
        };

        self.createNewAmendmentApplication = function (data) {
            return HarvestPermitAmendmentApplications.createAmendmentApplication(data).$promise.then(function (res) {
                return $state.go('permitmanagement.amendment', {applicationId: res.id});
            });
        };
    })

    .service('HarvestPermitAmendmentApplicationsModal', function ($uibModal,
                                                                  FormPostService, HarvestPermitAmendmentApplications) {
        this.showModal = function (permit) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/applications/amendment/amendment-list.html',
                resolve: {
                    list: function () {
                        return HarvestPermitAmendmentApplications.list({permitId: permit.id}).$promise
                            .then(function (a) {
                                return _.sortBy(a, ['gameSpeciesCode', 'id']);
                            });
                    },
                    gameSpecies: function (GameDiaryParameters) {
                        return GameDiaryParameters.query().$promise.then(function (params) {
                            return params.species;
                        });
                    },
                    permitSpecies: function (HarvestPermitAmendmentApplications) {
                        return HarvestPermitAmendmentApplications.listSpecies({id: permit.id}).$promise;
                    }
                },
                controller: ModalController,
                controllerAs: '$ctrl',
                bindToController: true
            }).result;
        };

        function ModalController($uibModalInstance, $filter, $translate, list, gameSpecies, permitSpecies) {
            var $ctrl = this;

            $ctrl.permitSpecies = permitSpecies;

            $ctrl.incomplete = _.filter(list, function (a) {
                return a.id && a.status === 'DRAFT';
            });

            $ctrl.sent = _.filter(list, function (a) {
                return a.id && a.status !== 'DRAFT';
            });

            $ctrl.nonedibles = _.filter(list, function (a) {
                return !a.id;
            });

            var nameFilter = $filter('rI18nNameFilter');
            var dateFilter = $filter('date');

            $ctrl.applicationAsText = function (a) {
                return (a.applicationNumber ? a.applicationNumber + ' ' : '') +
                    _.capitalize(nameFilter($ctrl.getSpeciesName(a))) +
                    (a.pointOfTime ? ', ' + dateFilter(a.pointOfTime, 'd.M.yyyy HH:mm') : '') +
                    (a.gender ? ', ' + $translate.instant('gamediary.gender.' + a.gender) : '') +
                    (a.age ? ', ' + $translate.instant('gamediary.age.' + a.age) : '');
            };

            $ctrl.getSpeciesName = function (application) {
                var x = _.find(gameSpecies, function (g) {
                    return g.code === application.gameSpeciesCode;
                });
                return x.name;
            };

            $ctrl.createByHarvest = function (harvestId) {
                $uibModalInstance.close({nonEdibleHarvestId: harvestId});
            };

            $ctrl.createBySpecies = function (species) {
                $uibModalInstance.close({gameSpeciesCode: species.code});
            };

            $ctrl.viewApplication = function (applicationId) {
                $uibModalInstance.close({applicationId: applicationId});
            };

            $ctrl.zip = function () {
                FormPostService.submitFormUsingBlankTarget('/api/v1/harvestpermit/application/' + $ctrl.selectedSentApplication.id + '/archive');
            };

            $ctrl.pdf = function () {
                FormPostService.submitFormUsingBlankTarget('/api/v1/harvestpermit/application/' + $ctrl.selectedSentApplication.id + '/print/pdf');
            };
        }
    })

    .controller('HarvestPermitAmendmentController', function ($translate, $state, $filter, dialogs,
                                                              MapState, MapDefaults, MapUtil, Helpers, ReasonAsker,
                                                              PersonSearchModal, NotificationService, ActiveRoleService,
                                                              HarvestPermitAmendmentApplicationService,
                                                              HarvestPermitAmendmentApplications,
                                                              HarvestPermitApplications,
                                                              application, attachments, species,
                                                              diaryParameters, partners, decisionId) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.application = application;
            $ctrl.species = species;
            $ctrl.attachments = attachments;
            $ctrl.partners = partners;

            $ctrl.editable = $ctrl.application.status !== 'ACTIVE';

            $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents(['click']);
            $ctrl.mapState = MapState.get();
            $ctrl.mapDefaults = MapDefaults.create();


            var filterUnknownOut = $filter('stripUnknown');

            $ctrl.getAvailableGameGenders = function () {
                return filterUnknownOut(diaryParameters.genders);
            };

            $ctrl.getAvailableGameAges = function () {
                return filterUnknownOut(diaryParameters.ages);
            };

            var dateFilter = $filter('date');

            $ctrl.viewState = {
                date: dateFilter($ctrl.application.pointOfTime, 'yyyy-MM-dd'),
                time: dateFilter($ctrl.application.pointOfTime, 'HH:mm'),
                partnerId: _.get($ctrl.application, 'partner.id')
            };

            MapState.updateMapCenter($ctrl.application.geoLocation ? $ctrl.application.geoLocation : MapUtil.getDefaultGeoLocation());
            MapState.get().center.zoom = $ctrl.application.geoLocation ? 11 : 5;

            $ctrl.attachmentConfig = {
                baseUri: '/api/v1/harvestpermit/application/' + application.id + '/attachment',
                canDownload: true,
                canDelete: $ctrl.editable
            };

            $ctrl.showGeolocationInput = $ctrl.editable && ActiveRoleService.isModerator();
            $ctrl.showSubmitDate = ActiveRoleService.isModerator();
            $ctrl.submitDate = application.submitDate ? dateFilter(application.submitDate, 'yyyy-MM-dd') : null;
        };

        $ctrl.updatePointOfTime = function () {
            if ($ctrl.viewState.date && $ctrl.viewState.time) {
                var dateTime = Helpers.parseDateAndTime($ctrl.viewState.date, $ctrl.viewState.time);
                $ctrl.application.pointOfTime = Helpers.dateTimeToString(dateTime);
            } else {
                $ctrl.application.pointOfTime = null;
            }
        };


        $ctrl.partnerChanged = function () {
            $ctrl.application.partner = _.find($ctrl.partners, function (p) {
                return p.id === $ctrl.viewState.partnerId;
            });
        };

        $ctrl.harvestText = function (harvest) {
            return _([
                $translate.instant('gamediary.age.' + harvest.age),
                $translate.instant('gamediary.gender.' + harvest.gender),
                Helpers.dateTimeToString(harvest.pointOfTime, 'd.M.YYYY HH:mm')
            ]).join(' ');
        };

        $ctrl.findShooter = function () {
            PersonSearchModal.searchPerson().then(function (personInfo) {
                $ctrl.application.shooter = personInfo;
            });
        };

        function saveAndReload() {
            doSave().then(function () {
                NotificationService.showDefaultSuccess();
                $state.reload();
            }, function () {
                NotificationService.showDefaultFailure();
            });
        }

        $ctrl.attachmentUploadComplete = saveAndReload;
        $ctrl.refreshAttachments = saveAndReload;

        $ctrl.save = function () {
            doSave().then(function () {
                NotificationService.showDefaultSuccess();
                $state.go('permitmanagement.amendment', {applicationId: $ctrl.application.id});
            });
        };

        function doSave() {
            if ($ctrl.application.id) {
                return HarvestPermitAmendmentApplications.update($ctrl.application).$promise;
            } else {
                var data = {
                    originalPermitId: $ctrl.application.originalPermitId,
                    gameSpeciesCode: $ctrl.application.gameSpeciesCode
                };
                return HarvestPermitAmendmentApplications.createAmendmentApplication(data).$promise.then(function (res) {
                    $ctrl.application.id = res.id;
                    return HarvestPermitAmendmentApplications.update($ctrl.application).$promise;
                });
            }
        }

        $ctrl.canSave = function () {
            return $ctrl.editable && $ctrl.application.gameSpeciesCode;
        };

        $ctrl.canSend = function () {
            return $ctrl.editable &&
                $ctrl.application.gameSpeciesCode &&
                $ctrl.application.pointOfTime &&
                $ctrl.application.age &&
                $ctrl.application.gender &&
                $ctrl.application.partner &&
                $ctrl.application.geoLocation;
        };

        function confirmSend() {
            var modalTitle = $translate.instant('harvestpermit.wizard.summary.sendConfirmation.title');
            var modalBody = $translate.instant('harvestpermit.wizard.summary.sendConfirmation.body');
            return dialogs.confirm(modalTitle, modalBody).result;
        }

        function confirmAmend() {
            return ReasonAsker.openModal({
                titleKey: 'harvestpermit.wizard.amendConfirm.title',
                messageKey: 'harvestpermit.wizard.amendConfirm.message'
            });
        }

        function amend(changeReason) {
            return HarvestPermitApplications.stopAmending({
                id: application.id,
                changeReason: changeReason,
                submitDate: $ctrl.submitDate

            }).$promise.then(function () {
                NotificationService.showDefaultSuccess();
                return $state.go('jht.decision.application.overview', {decisionId: decisionId});
            }, showFailure);
        }

        function send() {
            return HarvestPermitApplications.send({
                id: $ctrl.application.id,
                submitDate: $ctrl.submitDate
            }).$promise
                .then(function () {
                    NotificationService.showDefaultSuccess();
                    return $state.go('permitmanagement.dashboard', {permitId: $ctrl.application.originalPermitId});
                }, showFailure);
        }

        function showFailure() {
            NotificationService.showDefaultFailure();
        }

        $ctrl.send = function () {
            doSave().then(function () {
                var isAmending = $ctrl.application.status === 'AMENDING';
                if (isAmending) {
                    confirmAmend().then(amend);
                } else {
                    confirmSend().then(send);
                }
            });
        };

        $ctrl.delete = function () {
            HarvestPermitApplications.delete({id: $ctrl.application.id}).$promise
                .then(function () {
                    NotificationService.showDefaultSuccess();
                    return $state.go('permitmanagement.dashboard', {permitId: $ctrl.application.originalPermitId});
                }, showFailure);
        };

    })

    .component('amendmentApplicationSummary', {
        templateUrl: 'harvestpermit/applications/amendment/amendment-summary.html',
        bindings: {
            application: '<',
            permitArea: '<',
            amendmentApplication: '<'
        },
        controller: function (ActiveRoleService, HarvestPermitAmendmentApplications, MapDefaults, MapState,
                              FormPostService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.toggle = {a: true};
                $ctrl.firstPanelOpen = true;
                $ctrl.mapState = MapState.get();
                $ctrl.mapDefaults = MapDefaults.create();
                $ctrl.attachmentConfig = {
                    baseUri: '/api/v1/harvestpermit/application/' + $ctrl.application.id + '/attachment',
                    canDownload: ActiveRoleService.isModerator(),
                    canDelete: false
                };

                MapState.get().center.zoom = 11;

                if ($ctrl.amendmentApplication) {
                    MapState.updateMapCenter($ctrl.amendmentApplication.geoLocation);
                }
            };

            $ctrl.contactPersonsStr = function (contactPersons) {
                return _(contactPersons).map(function (c) {
                    return c.byName + ' ' + c.lastName;
                }).join(', ');
            };

            $ctrl.getAttachmentCount = function () {
                return _.size($ctrl.application.attachments);
            };

            $ctrl.exportMmlExcel = function () {
                FormPostService.submitFormUsingBlankTarget('/api/v1/application/area/mml/'
                    + $ctrl.application.id + '/print/pdf');
            };
        }
    });
