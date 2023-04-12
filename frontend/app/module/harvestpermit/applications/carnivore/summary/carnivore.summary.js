'use strict';

angular.module('app.harvestpermit.application.carnivore.summary', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.carnivore.summary', {
                url: '/summary',
                templateUrl: 'harvestpermit/applications/carnivore/summary/summary.html',
                controller: 'CarnivorePermitWizardSummaryController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    application: function (applicationId, CarnivorePermitApplication) {
                        return CarnivorePermitApplication.getFullDetails({id: applicationId}).$promise;
                    },
                    isLate: function (HarvestPermitApplications, applicationId) {
                        var params = {applicationId: applicationId};
                        return HarvestPermitApplications.findType(params).$promise.then(function (applicationType) {
                            return !applicationType.active;
                        });
                    },
                    states: function () {
                        return {
                            previous: 'attachments'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.carnivore.summary', {
                url: '/summary',
                templateUrl: 'harvestpermit/applications/carnivore/summary/summary.html',
                controller: 'CarnivorePermitWizardSummaryController',
                controllerAs: '$ctrl',
                resolve: {
                    application: function (applicationId, CarnivorePermitApplication) {
                        return CarnivorePermitApplication.getFullDetails({id: applicationId}).$promise;
                    },
                    isLate: function () {
                        return false;
                    },
                    states: function () {
                        return {
                            previous: 'attachments'
                        };
                    }
                }
            });
    })

    .controller('CarnivorePermitWizardSummaryController', function ($scope, $q, $translate, $filter,
                                                                    NotificationService, ActiveRoleService,
                                                                    UnsavedChangesConfirmationService, ReasonAsker,
                                                                    ApplicationWizardNavigationHelper,
                                                                    HarvestPermitApplications, ConfirmationDialogService,
                                                                    DecisionDeliveryAddressModal,
                                                                    states, wizard, application, isLate) {
        var $ctrl = this;
        var dateFilter = $filter('date');

        $ctrl.$onInit = function () {
            $ctrl.application = application;
            $ctrl.isLate = isLate;
            $ctrl.showSubmitDate = ActiveRoleService.isModerator();
            $ctrl.submitDate = application.submitDate ? dateFilter(application.submitDate, 'yyyy-MM-dd') : null;
            if (!_.isBoolean($ctrl.application.deliveryByMail)) {
                $ctrl.application.deliveryByMail = false;
            }
            $ctrl.application.deliveryAddress = application.deliveryAddress || getDeliveryInfoFromContactPerson(application.contactPerson);
            $scope.$watch('summaryForm.$pristine', function (newVal, oldVal) {
                if (oldVal && !newVal) {
                    UnsavedChangesConfirmationService.setChanges(true);
                }
            }, true);
        };

        $ctrl.setDecisionLanguage = function (lang) {
            $ctrl.application.decisionLanguage = lang;
        };

        $ctrl.setDeliveryByMail = function (byMail) {
            $ctrl.application.deliveryByMail = byMail;
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
                return validate().then(function (validationResp) {
                    if (validationResp.valid !== true) {
                        showApplicationInvalidMessage();
                        return $q.reject();
                    }

                    if (wizard.isAmending()) {
                        confirmAmend().then(amend);
                    } else {
                        confirmSend().then(send);
                    }
                });
            });
        };
        $ctrl.nextDisabled = function (form) {
            return invalid(form);
        };

        function invalid(form) {
            return form.$invalid ||
                $ctrl.deliveryByMail === null ||
                $ctrl.decisionLanguage === null ||
                $ctrl.deliveryAddressMissing();
        }

        $ctrl.deliveryAddressMissing = function () {
            return !$ctrl.application.deliveryAddress.recipient ||
                !$ctrl.application.deliveryAddress.streetAddress ||
                !$ctrl.application.deliveryAddress.postalCode ||
                !$ctrl.application.deliveryAddress.city;
        };

        function showApplicationInvalidMessage() {
            NotificationService.showMessage('harvestpermit.wizard.summary.invalid', {ttl: -1});
        }

        $ctrl.save = function () {
            UnsavedChangesConfirmationService.setChanges(false);
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
        };

        $ctrl.changeDeliveryAddress = function () {
            DecisionDeliveryAddressModal.open($ctrl.application.deliveryAddress).then(function (address) {
                $ctrl.application.deliveryAddress = address;
            });
        };

        function validate() {
            return HarvestPermitApplications.validate({id: application.id}).$promise;
        }

        function confirmSend() {
            var modalTitle = $translate.instant('harvestpermit.wizard.summary.sendConfirmation.title');
            var modalBody = $ctrl.isLate
                ? $translate.instant('harvestpermit.wizard.summary.sendConfirmation.bodyLate')
                : $translate.instant('harvestpermit.wizard.summary.sendConfirmation.body');

            return ConfirmationDialogService.showConfirmationDialogWithPrimaryAccept(modalTitle, modalBody);
        }

        function getDeliveryInfoFromContactPerson(person) {
            var address = person.address || {};
            return {
                recipient: person.firstName + ' ' + person.lastName,
                streetAddress: address.streetAddress,
                postalCode: address.postalCode,
                city: address.city
            };
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
    .component('carnivoreApplicationSummary', {
        templateUrl: 'harvestpermit/applications/carnivore/summary/summary-accordion.html',
        bindings: {
            application: '<'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.toggle = {a: true};
                $ctrl.attachmentBaseUri = '/api/v1/harvestpermit/application/' + $ctrl.application.id + '/attachment';
            };

            $ctrl.getAttachmentCount = function () {
                return _.size($ctrl.application.otherAttachments);
            };
        }
    })

    .component('carnivoreApplicationSummarySpecies', {
        templateUrl: 'harvestpermit/applications/carnivore/summary/summary-species.html',
        bindings: {
            species: '<'
        }
    })
    .component('carnivoreApplicationSummaryJustification', {
        templateUrl: 'harvestpermit/applications/carnivore/summary/summary-justification.html',
        bindings: {
            justification: '<'
        }
    })

    .component('carnivoreApplicationSummaryPopulation', {
        templateUrl: 'harvestpermit/applications/carnivore/summary/summary-population.html',
        bindings: {
            population: '<'
        }
    })

    .component('carnivoreApplicationSummaryArea', {
        templateUrl: 'harvestpermit/applications/carnivore/summary/summary-area.html',
        bindings: {
            application: '<',
            attachments: '<',
            baseUri: '<'
        },
        controller: function (FetchAndSaveBlob) {
            var $ctrl = this;

            $ctrl.downloadAttachment = function (id) {
                FetchAndSaveBlob.post($ctrl.baseUri + '/' + id);
            };
        }
    })
;
