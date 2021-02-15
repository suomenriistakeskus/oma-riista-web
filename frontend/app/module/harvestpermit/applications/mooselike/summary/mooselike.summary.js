'use strict';

angular.module('app.harvestpermit.application.mooselike.summary', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.mooselike.summary', {
                url: '/summary',
                templateUrl: 'harvestpermit/applications/mooselike/summary/summary.html',
                controller: 'MooselikePermitWizardSummaryController',
                controllerAs: '$ctrl',
                resolve: {
                    application: function (applicationId, MooselikePermitApplication) {
                        return MooselikePermitApplication.getFullDetails({id: applicationId}).$promise;
                    },
                    isLate: function (HarvestPermitApplications, application) {
                        var params = {applicationId: application.id};
                        return HarvestPermitApplications.findType(params).$promise.then(function (applicationType) {
                            return !applicationType.active;
                        });
                    },
                    permitArea: function (MooselikePermitApplication, applicationId) {
                        return MooselikePermitApplication.getArea({
                            id: applicationId
                        }).$promise;
                    }
                }
            })

            .state('jht.decision.application.wizard.mooselike.summary', {
                url: '/summary',
                templateUrl: 'harvestpermit/applications/mooselike/summary/summary.html',
                controller: 'MooselikePermitWizardSummaryController',
                controllerAs: '$ctrl',
                resolve: {
                    application: function (applicationId, MooselikePermitApplication) {
                        return MooselikePermitApplication.getFullDetails({id: applicationId}).$promise;
                    },
                    isLate: _.constant(false),
                    permitArea: function (MooselikePermitApplication, applicationId) {
                        return MooselikePermitApplication.getArea({
                            id: applicationId
                        }).$promise;
                    }
                }
            });
    })

    .controller('MooselikePermitWizardSummaryController', function ($q, $translate, $filter, ConfirmationDialogService,
                                                                    NotificationService, ActiveRoleService, ReasonAsker,
                                                                    HarvestPermitApplications, DecisionDeliveryAddressModal,
                                                                    wizard, application, isLate, permitArea) {
        var $ctrl = this;
        var dateFilter = $filter('date');

        $ctrl.$onInit = function () {
            $ctrl.application = application;
            $ctrl.isLate = isLate;
            $ctrl.permitArea = permitArea;
            $ctrl.showSubmitDate = ActiveRoleService.isModerator();
            $ctrl.submitDate = application.submitDate ? dateFilter(application.submitDate, 'yyyy-MM-dd') : null;
            $ctrl.nextButtontTitleKey = wizard.isAmending()
                ? 'harvestpermit.wizard.navigation.amend'
                : 'harvestpermit.wizard.navigation.send';

            if (!_.isBoolean($ctrl.application.deliveryByMail)) {
                $ctrl.application.deliveryByMail = false;
            }
        };

        $ctrl.setDecisionLanguage = function (lang) {
            $ctrl.application.decisionLanguage = lang;
        };

        $ctrl.setDeliveryByMail = function (byMail) {
            $ctrl.application.deliveryByMail = byMail;
        };

        $ctrl.exit = function () {
            saveAdditionalData().then(wizard.exit());
        };

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
            return form.email1.$invalid ||
                form.email2.$invalid ||
                (form.submitDate && form.submitDate.$invalid) ||
                $ctrl.deliveryAddressMissing();
        };

        $ctrl.changeDeliveryAddress = function () {
            DecisionDeliveryAddressModal.open($ctrl.application.deliveryAddress).then(function (address) {
                $ctrl.application.deliveryAddress = address;
            });
        };

        $ctrl.deliveryAddressMissing = function () {
            return !$ctrl.application.deliveryAddress ||
                !$ctrl.application.deliveryAddress.recipient ||
                !$ctrl.application.deliveryAddress.streetAddress ||
                !$ctrl.application.deliveryAddress.postalCode ||
                !$ctrl.application.deliveryAddress.city;
        };

        function showApplicationInvalidMessage() {
            NotificationService.showMessage('harvestpermit.wizard.summary.invalid', {ttl: -1});
        }

        function saveAdditionalData() {
            return HarvestPermitApplications.updateAdditionalData({id: application.id}, {
                email1: $ctrl.application.email1,
                email2: $ctrl.application.email2,
                deliveryByMail: $ctrl.application.deliveryByMail,
                decisionLanguage: $ctrl.application.decisionLanguage,
                deliveryAddress: $ctrl.application.deliveryAddress
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

            return ConfirmationDialogService.showConfimationDialogWithPrimaryAccept(modalTitle, modalBody);
        }

        function send() {
            HarvestPermitApplications.send({
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

    .component('mooselikeApplicationSummary', {
        templateUrl: 'harvestpermit/applications/mooselike/summary/summary-accordion.html',
        bindings: {
            application: '<',
            permitArea: '<'
        },
        controller: function (ActiveRoleService, FormPostService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.toggle = {a: true};
                $ctrl.firstPanelOpen = true;
                $ctrl.attachmentConfig = {
                    baseUri: '/api/v1/harvestpermit/application/' + $ctrl.application.id + '/attachment',
                    canDownload: ActiveRoleService.isModerator(),
                    canDelete: false
                };
                $ctrl.permitHolder = !!$ctrl.application.huntingClub ? $ctrl.application.permitHolder : null;
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
    })

    .component('mooselikeApplicationSummaryAreaExternalIds', {
        templateUrl: 'harvestpermit/applications/mooselike/summary/summary-area-external-ids.html',
        bindings: {
            permitArea: '<'
        }
    });
