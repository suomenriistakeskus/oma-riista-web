'use strict';

angular.module('app.harvestpermit.application.mooselike.partners', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.mooselike.partners', {
                url: '/partners',
                templateUrl: 'harvestpermit/applications/mooselike/partners/partners.html',
                controller: 'MooselikePermitWizardPartnersController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    partners: function (MooselikePermitApplicationAreaPartners, applicationId) {
                        return MooselikePermitApplicationAreaPartners.query({
                            applicationId: applicationId
                        }).$promise;
                    }
                }
            })

            .state('jht.decision.application.wizard.mooselike.partners', {
                url: '/partners',
                templateUrl: 'harvestpermit/applications/mooselike/partners/partners.html',
                controller: 'MooselikePermitWizardPartnersController',
                controllerAs: '$ctrl',
                resolve: {
                    partners: function (MooselikePermitApplicationAreaPartners, applicationId) {
                        return MooselikePermitApplicationAreaPartners.query({
                            applicationId: applicationId
                        }).$promise;
                    }
                }
            });
    })
    .controller('MooselikePermitWizardPartnersController', function (MooselikePermitApplication,
                                                                     MooselikePermitWizardAreaProcessingModal,
                                                                     HarvestPermitAreaErrorModal,
                                                                     MooselikePermitWizardAddClubAreaModal,
                                                                     MooselikePermitApplicationAreaPartners,
                                                                     wizard, applicationId, partners, applicationBasicDetails) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.applicationId = applicationId;
            $ctrl.partners = partners;

            MooselikePermitApplication.getAreaStatus({
                id: applicationId

            }).$promise.then(function (result) {
                if (result.status !== 'INCOMPLETE') {
                    MooselikePermitApplication.setAreaIncomplete({
                        id: applicationId
                    });
                }
            });
        };

        $ctrl.showAddPartner = function () {
            MooselikePermitWizardAddClubAreaModal.open(applicationId, applicationBasicDetails.huntingYear).then(function () {
                wizard.reload();
            });
        };

        $ctrl.onPartnerRemove = function (id) {
            MooselikePermitApplicationAreaPartners.remove({
                applicationId: $ctrl.applicationId,
                partnerId: id
            }).$promise.then(function () {
                wizard.reload();
            });
        };

        $ctrl.onPartnerRefresh = function (id) {
            MooselikePermitApplicationAreaPartners.save({
                applicationId: $ctrl.applicationId,
                partnerId: id
            }).$promise.then(function () {
                wizard.reload();
            });
        };

        $ctrl.areaSelectionEmpty = function () {
            return _.size(partners) < 1;
        };

        $ctrl.exit = wizard.exit;

        $ctrl.previous = function () {
            wizard.goto('species');
        };

        function showProcessingModal() {
            MooselikePermitWizardAreaProcessingModal.open(applicationId).then(function () {
                wizard.goto('map');
            }, function () {
                HarvestPermitAreaErrorModal.showProcessingFailed();
            });
        }

        $ctrl.next = function () {
            MooselikePermitApplication.getAreaStatus({
                id: applicationId

            }).$promise.then(function (result) {
                if (result.status === 'INCOMPLETE' || result.status === 'PROCESSING_FAILED') {
                    MooselikePermitApplication.setAreaReady({
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

    .service('MooselikePermitWizardAddClubAreaModal', function ($uibModal, Account, NotificationService,
                                                                HarvestPermitAreaErrorModal,
                                                                MooselikePermitApplicationAreaPartners) {
        this.open = function (applicationId, huntingYear) {
            var modalInstance = $uibModal.open({
                templateUrl: 'harvestpermit/applications/mooselike/partners/add-club-area.html',
                controller: ModalController,
                controllerAs: '$ctrl',
                resolve: {
                    huntingYear: _.constant(huntingYear),
                    availableHuntingClubs: function (MooselikePermitApplicationAreaPartners, $filter) {
                        var i18n = $filter('rI18nNameFilter');

                        return MooselikePermitApplicationAreaPartners.listAvailable({
                            applicationId: applicationId
                        }).$promise.then(function (result) {
                            return _.map(result, function (club) {
                                return {
                                    id: club.id,
                                    name: i18n(club)
                                };
                            });
                        });
                    },
                    showClubs: _.constant(true)
                }
            });

            return modalInstance.result.then(function (areaExternalId) {
                return MooselikePermitApplicationAreaPartners.save({
                    applicationId: applicationId,
                    externalId: areaExternalId
                }).$promise.then(function () {
                    NotificationService.showDefaultSuccess();
                }, function (response) {
                    if (response.status === 404) {
                        HarvestPermitAreaErrorModal.showNotFound();
                    } else if (response.status === 400) {
                        if (response.data.exception === 'HarvestPermitAreaHuntingYearException') {
                            HarvestPermitAreaErrorModal.showHuntingYearMismatch();
                        }
                        if (response.data.exception === 'MetsahallitusYearMismatchException') {
                            HarvestPermitAreaErrorModal.showMhYearMismatch();
                        }
                    } else {
                        NotificationService.showDefaultFailure();
                    }
                });
            });
        };

        function ModalController($filter, $uibModalInstance, ClubAreas, HuntingYearService,
                                 huntingYear, availableHuntingClubs) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.availableHuntingClubs = availableHuntingClubs;
                $ctrl.availableHuntingClubAreas = [];
                $ctrl.huntingYear = huntingYear;
                $ctrl.huntingClubId = _($ctrl.availableHuntingClubs).map('id').head();
                $ctrl.huntingClubAreaId = null;
                $ctrl.areaExternalId = null;

                fetchHuntingClubAreas($ctrl.huntingClubId);
            };

            $ctrl.onReloadAreas = function () {
                fetchHuntingClubAreas($ctrl.huntingClubId);
            };

            $ctrl.onAreaSelected = function () {
                $ctrl.areaExternalId = _($ctrl.availableHuntingClubAreas)
                    .filter(_.matchesProperty('id', $ctrl.huntingClubAreaId))
                    .map('externalId')
                    .filter()
                    .head();
            };

            function fetchHuntingClubAreas(huntingClubId) {
                var i18n = $filter('rI18nNameFilter');

                $ctrl.availableHuntingClubAreas = [];
                $ctrl.huntingClubAreaId = null;
                $ctrl.areaExternalId = null;

                if (!_.isFinite(huntingYear) || !_.isFinite(huntingClubId)) {
                    return;
                }

                return ClubAreas.query({
                    year: huntingYear,
                    clubId: huntingClubId,
                    activeOnly: true,
                    includeEmpty: false

                }).$promise.then(function (result) {
                    $ctrl.availableHuntingClubAreas = _.map(result, function (area) {
                        return {
                            id: area.id,
                            name: i18n(area),
                            externalId: area.externalId
                        };
                    });
                });
            }

            $ctrl.ok = function () {
                $uibModalInstance.close($ctrl.areaExternalId);
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }
    })


    .service('HarvestPermitAreaErrorModal', function ($uibModal) {
        this.showNotFound = function () {
            showErrorModal('harvestpermit.wizard.partners.addClubArea.notFound');
        };

        this.showHuntingYearMismatch = function () {
            showErrorModal('harvestpermit.wizard.partners.addClubArea.huntingYearMismatch');
        };

        this.showMhYearMismatch = function () {
            showErrorModal('harvestpermit.wizard.partners.addClubArea.mhYearMismatch');
        };

        this.showProcessingFailed = function () {
            showErrorModal('harvestpermit.wizard.partners.addClubArea.processingFailed');
        };

        function showErrorModal(localisationKey) {
            $uibModal.open({
                templateUrl: 'harvestpermit/applications/mooselike/partners/area-error-modal.html',
                controllerAs: '$ctrl',
                controller: function () {
                    this.titleKey = 'harvestpermit.wizard.partners.addClubArea.areaError';
                    this.key = localisationKey;
                }
            });
        }
    });
