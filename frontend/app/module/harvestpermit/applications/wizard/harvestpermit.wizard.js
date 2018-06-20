'use strict';

angular.module('app.harvestpermit.application.wizard', [])
    .component('permitApplicationWizardContactPerson', {
        templateUrl: 'harvestpermit/applications/wizard/contact-person.html',
        bindings: {
            person: '<'
        }
    })
    .component('permitApplicationWizardApplicantType', {
        templateUrl: 'harvestpermit/applications/wizard/applicant-type.html',
        bindings: {
            subtype: '<'
        }
    })
    .controller('HarvestPermitWizardTypeController', function ($state, $q, HarvestPermitApplications,
                                                               HarvestPermitWizardApplicationNameModal,
                                                               ActiveRoleService,
                                                               applicationTypes, isProductionEnvironment, personId) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            var isModerator = ActiveRoleService.isModerator();

            $ctrl.types = _.filter(applicationTypes, function (a) {
                return a.active || !isProductionEnvironment || isModerator;
            });
        };

        $ctrl.create = function (applicationType) {
            HarvestPermitWizardApplicationNameModal.showModal(applicationType).then(function (applicationName) {
                return HarvestPermitApplications.save({
                    applicationName: applicationName,
                    permitTypeCode: applicationType.code,
                    huntingYear: applicationType.huntingYear,
                    personId: personId
                }).$promise;

            }, function () {
                return $q.reject();

            }).then(function (res) {
                $state.go('profile.permitwizard.applicant', {
                    applicationId: res.id
                });
            });
        };
    })
    .service('HarvestPermitWizardApplicationNameModal', function ($q, $uibModal) {
        this.showModal = function (applicationType) {
            var modalInstance = $uibModal.open({
                templateUrl: 'harvestpermit/applications/wizard/application-name.html',
                size: 'md',
                controllerAs: '$ctrl',
                controller: ModalController,
                resolve: {
                    applicationType: _.constant(applicationType)
                }
            });

            return modalInstance.result;
        };

        function ModalController($translate, $uibModalInstance, HuntingYearService, applicationType) {
            var $ctrl = this;

            var nextHuntingYear = 1 + HuntingYearService.getCurrent();
            $ctrl.applicationType = applicationType;

            var name = $translate.instant('harvestpermit.mine.applications.permitTypeCode.' + applicationType.code);
            $ctrl.applicationName = name + ' ' + (nextHuntingYear);

            $ctrl.ok = function () {
                $uibModalInstance.close($ctrl.applicationName);
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }
    })
    .component('permitApplicationWizardNavigation', {
        templateUrl: 'harvestpermit/applications/wizard/wizard-navigation.html',
        bindings: {
            previous: '&',
            previousDisabled: '&',
            next: '&',
            nextDisabled: '&',
            nextTitle: '@',
            exit: '&'
        }
    })
    .controller('HarvestPermitWizardApplicantController', function ($state,
                                                                    HarvestPermitApplications, NotificationService,
                                                                    ChangeApplicantModal,
                                                                    wizard, application) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.personalApplication = !application.permitHolder;
            $ctrl.contactPerson = application.contactPerson;
            $ctrl.permitHolder = application.permitHolder;
        };

        $ctrl.changeApplicant = function () {
            ChangeApplicantModal.changeApplicant(application.id, application.contactPerson).then(function (newPermitHolder) {
                var promise = HarvestPermitApplications.updatePermitHolder({id: application.id}, newPermitHolder).$promise;
                return NotificationService.handleModalPromise(promise);
            }).then(function () {
                $state.reload();
            });
        };

        $ctrl.exit = wizard.exit;
        $ctrl.constantTrue = _.constant(true);
        $ctrl.previous = function () {
            // no previous
        };

        $ctrl.isNextDisabled = function () {
            return $ctrl.permitHolder && !_.get($ctrl.permitHolder, 'subtype');
        };

        $ctrl.next = function () {
            wizard.goto('species');
        };
    })
    .service('ChangeApplicantModal', function ($q, $uibModal,
                                               NotificationService, ActiveRoleService,
                                               HarvestPermitApplications, ChangeClubTypeModal) {

        function ModalController($uibModalInstance, contactPerson, applicants, moderator, applicationId) {
            var $ctrl = this;

            $ctrl.contactPerson = contactPerson;
            $ctrl.permitHolderCandidates = applicants;

            // for moderator search
            $ctrl.moderator = moderator;
            $ctrl.officialCodeSearch = null;
            $ctrl.searchResultClub = null;

            $ctrl.selectPermitHolderPerson = function () {
                $uibModalInstance.close(null);
            };

            $ctrl.selectPermitHolderClub = function (club) {
                if (club.subtype) {
                    return $uibModalInstance.close(club);
                }
                var modalPromise = ChangeClubTypeModal.editClubType(club);
                return NotificationService.handleModalPromise(modalPromise).then(function (updatedClub) {
                    return $uibModalInstance.close(updatedClub);
                });
            };

            $ctrl.searchByCode = function () {
                $ctrl.searchResultClub = null;
                if (_.size($ctrl.officialCodeSearch) < 7) {
                    return;
                }
                HarvestPermitApplications.searchPermitHolder({
                    id: applicationId,
                    officialCode: $ctrl.officialCodeSearch
                }).$promise.then(function (result) {
                    $ctrl.searchResultClub = result;
                }, function (err) {
                    if (err.status !== 404) {
                        NotificationService.showDefaultFailure();
                    }
                });
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }

        function showModal(applicationId, contactPerson) {
            var modalInstance = $uibModal.open({
                templateUrl: 'harvestpermit/applications/wizard/applicant-select.html',
                size: 'lg',
                resolve: {
                    contactPerson: _.constant(contactPerson),
                    applicants: function (HarvestPermitApplications) {
                        return HarvestPermitApplications.listAvailablePermitHolders({id: applicationId}).$promise;
                    },
                    moderator: _.constant(ActiveRoleService.isModerator()),
                    applicationId: _.constant(applicationId)
                },
                controller: ModalController,
                controllerAs: '$ctrl'
            });

            return modalInstance.result;
        }

        this.changeApplicant = function (applicationId, contactPerson) {
            return showModal(applicationId, contactPerson);
        };
    })
    .controller('HarvestPermitWizardSpeciesController', function (GameSpeciesCodes, HarvestPermitApplications,
                                                                  wizard, applicationId, gameDiaryParameters, speciesAmounts) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.speciesAmounts = [
                getSpeciesAmount(GameSpeciesCodes.MOOSE),
                getSpeciesAmount(GameSpeciesCodes.WHITE_TAILED_DEER),
                getSpeciesAmount(GameSpeciesCodes.FALLOW_DEER),
                getSpeciesAmount(GameSpeciesCodes.WILD_FOREST_REINDEER)
            ];
        };

        function getSpeciesAmount(gameSpeciesCode) {
            var spa = _.find(speciesAmounts, 'gameSpeciesCode', gameSpeciesCode);

            return {
                enabled: !!spa,
                name: gameDiaryParameters.$getGameName(gameSpeciesCode, null),
                gameSpeciesCode: gameSpeciesCode,
                amount: spa ? spa.amount : 0,
                description: spa ? spa.description : ''
            };
        }

        $ctrl.speciesSelected = function () {
            return _.some($ctrl.speciesAmounts, function (spa) {
                return spa.enabled === true && spa.amount > 0;
            });
        };

        $ctrl.exit = wizard.exit;

        $ctrl.previous = function () {
            wizard.goto('applicant');
        };

        $ctrl.next = function () {
            var data = _.chain($ctrl.speciesAmounts).filter('enabled').map(function (spa) {
                return {
                    gameSpeciesCode: spa.gameSpeciesCode,
                    amount: spa.amount,
                    description: spa.description
                };
            }).value();

            HarvestPermitApplications.saveSpeciesAmounts({id: applicationId}, data).$promise.then(function () {
                wizard.goto('partners');
            });
        };
    })
    .controller('HarvestPermitWizardPartnersController', function (HarvestPermitApplications,
                                                                   HarvestPermitAreaProcessingModal,
                                                                   HarvestPermitAreaErrorModal,
                                                                   AddClubAreaToHarvestPermitApplicationModal,
                                                                   wizard, applicationId, partners, applicationBasicDetails) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.applicationId = applicationId;
            $ctrl.partners = partners;

            HarvestPermitApplications.getAreaStatus({
                id: applicationId

            }).$promise.then(function (result) {
                if (result.status !== 'INCOMPLETE') {
                    HarvestPermitApplications.setAreaIncomplete({
                        id: applicationId
                    });
                }
            });
        };

        $ctrl.showAddPartner = function () {
            AddClubAreaToHarvestPermitApplicationModal.open(applicationId, applicationBasicDetails.huntingYear).then(function () {
                wizard.reload();
            });
        };

        $ctrl.onPartnersChanged = function () {
            wizard.reload();
        };

        $ctrl.areaSelectionEmpty = function () {
            return _.size(partners) < 1;
        };

        $ctrl.exit = wizard.exit;

        $ctrl.previous = function () {
            wizard.goto('species');
        };

        function showProcessingModal() {
            HarvestPermitAreaProcessingModal.open(applicationId).then(function () {
                wizard.goto('map');
            }, function () {
                HarvestPermitAreaErrorModal.showProcessingFailed();
            });
        }

        $ctrl.next = function () {
            HarvestPermitApplications.getAreaStatus({
                id: applicationId

            }).$promise.then(function (result) {
                if (result.status === 'INCOMPLETE' || result.status === 'PROCESSING_FAILED') {
                    HarvestPermitApplications.setAreaReady({
                        id: applicationId

                    }).$promise.then(function () {
                        showProcessingModal();
                    });
                } else if (result.status === 'READY') {
                    wizard.goto('map');

                } else if (result.status === 'PROCESSING' || result.status === 'PENDING') {
                    showProcessingModal();
                }
            });
        };
    })
    .controller('HarvestPermitWizardMapController', function (HarvestPermitApplications,
                                                              wizard, applicationId, permitArea) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.permitArea = permitArea;

            if (permitArea.status !== 'READY') {
                wizard.goto('partners');
            }
        };

        $ctrl.selectTab = function (tabIndex) {
            $ctrl.featureCollection = null;

            if (tabIndex === 0) {
                loadGeometry('union');
            } else if (tabIndex === 1) {
                loadGeometry('partner');
            }
        };

        function loadGeometry(outputStyle) {
            $ctrl.featureCollection = null;

            HarvestPermitApplications.getGeometry({
                id: applicationId,
                outputStyle: outputStyle

            }).$promise.then(function (result) {
                $ctrl.featureCollection = result;
            });
        }

        $ctrl.exit = wizard.exit;

        $ctrl.previous = function () {
            HarvestPermitApplications.setAreaIncomplete({
                id: applicationId
            }).$promise.then(function () {
                wizard.goto('partners');
            });
        };

        $ctrl.next = function () {
            wizard.goto('attachments');
        };
    })
    .constant('HarvestPermitWizardAttachmentsToggleState', {})
    .controller('HarvestPermitWizardAttachmentsController', function (HarvestPermitApplications,
                                                                      HarvestPermitWizardAttachmentsToggleState,
                                                                      HarvestPermitWizardMhImportService,
                                                                      wizard, applicationId,
                                                                      attachments, shooterCounts, freeHunting) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.attachments = attachments;
            $ctrl.attachmentConfig = {
                baseUri: '/api/v1/harvestpermit/application/' + applicationId + '/attachment',
                canDownload: true,
                canDelete: true
            };
            $ctrl.shooterCounts = shooterCounts || {};
            $ctrl.freeHunting = freeHunting;
            $ctrl.toggle = HarvestPermitWizardAttachmentsToggleState;
        };

        $ctrl.attachmentUploadComplete = function () {
            $ctrl.refreshAttachments();
        };

        $ctrl.refreshAttachments = function () {
            storeShooterCounts().then(function () {
                wizard.reload();
            });
        };

        $ctrl.hasAttachmentWithType = function (type) {
            return _.some($ctrl.attachments, 'type', type);
        };

        $ctrl.getAttachmentCount = function (type) {
            return _($ctrl.attachments).filter('type', type).size();
        };

        $ctrl.attachmentListComplete = function () {
            return !$ctrl.freeHunting || $ctrl.hasAttachmentWithType('MH_AREA_PERMIT') && $ctrl.hasAttachmentWithType('SHOOTER_LIST');
        };

        $ctrl.shooterCountsValid = function () {
            if (!$ctrl.freeHunting) {
                return true;
            }

            var counts = $ctrl.shooterCounts;
            var a = counts.shooterOnlyClub;
            var b = counts.shooterOtherClubPassive;
            var c = counts.shooterOtherClubActive;

            return isValidCount(a) && isValidCount(b) && isValidCount(c) && (a + b) > 0;
        };

        function isValidCount(c) {
            return _.isFinite(c) && c >= 0;
        }

        function storeShooterCounts() {
            var requestBody = $ctrl.freeHunting ? $ctrl.shooterCounts : {};
            return HarvestPermitApplications.updateShooterCounts({id: applicationId}, requestBody).$promise;
        }

        $ctrl.importMh = function () {
            HarvestPermitWizardMhImportService.showImport(applicationId).then($ctrl.refreshAttachments);
        };

        $ctrl.exit = wizard.exit;

        $ctrl.previous = function () {
            wizard.goto('map');
        };

        $ctrl.next = function () {
            storeShooterCounts().then(function () {
                wizard.goto('summary');
            });
        };
    })

    .component('permitApplicationShooterCounts', {
        templateUrl: 'harvestpermit/applications/wizard/shooter-counts.html',
        bindings: {
            shooterCounts: '<'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.shooterTotalCount = function () {
                return ($ctrl.shooterCounts.shooterOnlyClub || 0) + ($ctrl.shooterCounts.shooterOtherClubPassive || 0);
            };
        }
    })

    .component('permitApplicationAttachmentList', {
        templateUrl: 'harvestpermit/applications/wizard/attachment-list.html',
        bindings: {
            attachments: '<',
            attachmentType: '<',
            attachmentConfig: '<', // baseUri, canDownload, canDelete
            refresh: '&'
        },
        controller: function ($http, $translate, dialogs, FormPostService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                if (angular.isString($ctrl.attachmentType)) {
                    $ctrl.filteredAttachments = _.filter($ctrl.attachments, 'type', $ctrl.attachmentType);
                } else {
                    $ctrl.filteredAttachments = $ctrl.attachments;
                }
            };

            $ctrl.canDownload = function () {
                return _.get($ctrl.attachmentConfig, 'canDownload', false);
            };

            $ctrl.canDelete = function () {
                return _.get($ctrl.attachmentConfig, 'canDelete', false);
            };

            $ctrl.downloadAttachment = function (id) {
                if ($ctrl.canDownload()) {
                    FormPostService.submitFormUsingBlankTarget(getAttachmentUri(id));
                }
            };

            $ctrl.deleteAttachment = function (id) {
                var modalTitle = $translate.instant('harvestpermit.wizard.attachments.deleteConfirmation.title');
                var modalBody = $translate.instant('harvestpermit.wizard.attachments.deleteConfirmation.body');

                dialogs.confirm(modalTitle, modalBody).result.then(function () {
                    $http.delete(getAttachmentUri(id)).then(function () {
                        $ctrl.refresh();
                    });
                });
            };

            function getAttachmentUri(id) {
                return $ctrl.attachmentConfig.baseUri + '/' + id;
            }
        }
    })

    .component('permitApplicationAttachmentUpload', {
        templateUrl: 'harvestpermit/applications/wizard/attachment-upload.html',
        bindings: {
            uri: '<',
            attachmentType: '<',
            done: '&'
        },
        controller: function ($timeout) {
            var $ctrl = this;

            $ctrl.dropzone = null;

            $ctrl.$onInit = function () {
                $ctrl.dropzoneConfig = {
                    autoProcessQueue: true,
                    maxFiles: 1,
                    maxFilesize: 50, // MiB
                    uploadMultiple: false,
                    url: $ctrl.uri,
                    paramName: 'file',
                    params: {
                        attachmentType: $ctrl.attachmentType
                    }
                };
                $ctrl.errors = {
                    incompatibleFileType: false
                };
            };

            $ctrl.dropzoneEventHandlers = {
                success: function (file) {
                    $ctrl.dropzone.removeFile(file);
                    $timeout(function () {
                        $ctrl.errors = {};
                    });

                    $ctrl.done();
                },
                error: function (file, response, xhr) {
                    $ctrl.dropzone.removeFile(file);
                    $timeout(function () {
                        $ctrl.errors = {
                            incompatibleFileType: true
                        };
                    });
                }
            };
        }
    })
    .service('HarvestPermitWizardMhImportService', function ($uibModal,
                                                             TranslatedBlockUI, NotificationService, HarvestPermitApplications) {

        function ModalController($filter, $uibModalInstance) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.data = {
                    mhApplicationNumber: null,
                    mhPermitNumber: null
                };
            };

            $ctrl.ok = function () {
                $uibModalInstance.close($ctrl.data);
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }

        this.showImport = function (applicationId) {
            var modalInstance = $uibModal.open({
                templateUrl: 'harvestpermit/applications/wizard/mh_import.html',
                controller: ModalController,
                controllerAs: '$ctrl',
                resolve: {}
            });

            return modalInstance.result.then(function (data) {
                TranslatedBlockUI.start('global.block.wait');
                return HarvestPermitApplications.importMh({id: applicationId}, data).$promise.then(
                    function (res) {
                        NotificationService.showDefaultSuccess();
                        return res;
                    }, function (err) {
                        if (err.status === 404) {
                            NotificationService.showMessage('harvestpermit.wizard.attachments.mhNotFound', 'error');
                        } else {
                            NotificationService.showDefaultFailure();
                        }
                        return err;
                    })
                    .finally(function () {
                        TranslatedBlockUI.stop();
                    });
            });
        };
    })
    .controller('HarvestPermitWizardSummaryController', function ($q, $translate, $filter, dialogs, NotificationService,
                                                                  HarvestPermitApplications,
                                                                  ActiveRoleService, ReasonAsker,
                                                                  wizard, application, isLate, permitArea,
                                                                  diaryParameters) {
        var $ctrl = this;
        var dateFilter = $filter('date');

        $ctrl.$onInit = function () {
            $ctrl.application = application;
            $ctrl.isLate = isLate;
            $ctrl.permitArea = permitArea;
            $ctrl.showSubmitDate = ActiveRoleService.isModerator();
            $ctrl.submitDate = application.submitDate ? dateFilter(application.submitDate, 'yyyy-MM-dd') : null;
            $ctrl.diaryParameters = diaryParameters;
            $ctrl.nextButtontTitleKey = wizard.isAmending()
                ? 'harvestpermit.wizard.navigation.amend'
                : 'harvestpermit.wizard.navigation.send';

            if (!_.isBoolean($ctrl.application.deliveryByMail)) {
                $ctrl.application.deliveryByMail = false;
            }
        };

        $ctrl.exit = wizard.exit;

        $ctrl.previous = function () {
            wizard.goto('attachments');
        };

        $ctrl.next = function () {
            saveAdditionalData().then(validate).then(function () {
                if (wizard.isAmending()) {
                    confirmAmend().then(amend);
                } else {
                    confirmSend().then(send);
                }
            });
        };

        $ctrl.nextDisabled = function (form) {
            return form.email1.$invalid || form.email2.$invalid || (form.submitDate && form.submitDate.$invalid);
        };

        function showApplicationInvalidMessage() {
            NotificationService.showMessage('harvestpermit.wizard.summary.invalid', {ttl: -1});
        }

        function saveAdditionalData() {
            return HarvestPermitApplications.updateAdditionalData({id: application.id}, {
                email1: $ctrl.application.email1,
                email2: $ctrl.application.email2,
                deliveryByMail: $ctrl.application.deliveryByMail
            }).$promise.then(null, function () {
                NotificationService.showDefaultFailure();
                return $q.reject();
            });
        }

        function validate() {
            return HarvestPermitApplications.validate({
                id: application.id
            }).$promise.then(function (result) {
                if (!result.valid) {
                    showApplicationInvalidMessage();
                    return $q.reject();
                }
                return result;
            });
        }

        // Normal send

        function confirmSend() {
            var modalTitle = $translate.instant('harvestpermit.wizard.summary.sendConfirmation.title');
            var modalBody = $ctrl.isLate
                ? $translate.instant('harvestpermit.wizard.summary.sendConfirmation.bodyLate')
                : $translate.instant('harvestpermit.wizard.summary.sendConfirmation.body');

            return dialogs.confirm(modalTitle, modalBody).result;
        }

        function send() {
            HarvestPermitApplications.sendApplication({
                id: application.id,
                submitDate: $ctrl.submitDate

            }).$promise.then(function () {
                NotificationService.showDefaultSuccess();
                wizard.exit();
            }, function () {
                showApplicationInvalidMessage();
            });
        }

        // Complete amend for moderator

        function confirmAmend() {
            return ReasonAsker.openModal({
                titleKey: 'harvestpermit.wizard.amendConfirm.title',
                messageKey: 'harvestpermit.wizard.amendConfirm.message'
            });
        }

        function amend(changeReason) {
            HarvestPermitApplications.stopAmending({
                id: application.id,
                changeReason: changeReason,
                submitDate: $ctrl.submitDate

            }).$promise.then(function () {
                NotificationService.showDefaultSuccess();
                wizard.exit();
            }, function () {
                showApplicationInvalidMessage();
            });
        }
    })
    .component('permitApplicationSummary', {
        templateUrl: 'harvestpermit/applications/wizard/summary-accordion.html',
        bindings: {
            application: '<',
            permitArea: '<',
            diaryParameters: '<'
        },
        controller: function (ActiveRoleService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.toggle = {a: true};
                $ctrl.firstPanelOpen = true;
                $ctrl.attachmentConfig = {
                    baseUri: '/api/v1/harvestpermit/application/' + $ctrl.application.id + '/attachment',
                    canDownload: ActiveRoleService.isModerator(),
                    canDelete: false
                };
            };

            $ctrl.contactPersonsStr = function (contactPersons) {
                return _(contactPersons).map(function (c) {
                    return c.byName + ' ' + c.lastName;
                }).join(', ');
            };

            $ctrl.getSpeciesName = function (gameSpeciesCode) {
                return $ctrl.diaryParameters
                    ? $ctrl.diaryParameters.$getGameName(gameSpeciesCode, null)
                    : gameSpeciesCode;
            };

            $ctrl.getAttachmentCount = function () {
                return _.size($ctrl.application.attachments);
            };
        }
    });
