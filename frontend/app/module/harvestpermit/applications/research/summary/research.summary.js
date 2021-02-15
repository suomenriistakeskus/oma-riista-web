'use strict';

angular.module('app.harvestpermit.application.research.summary', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.research.summary', {
                url: '/summary',
                templateUrl: 'harvestpermit/applications/research/summary/summary.html',
                controller: 'ResearchPermitWizardSummaryController',
                controllerAs: '$ctrl',
                resolve: {
                    application: function (applicationId, ResearchPermitApplication) {
                        return ResearchPermitApplication.getFullDetails({id: applicationId}).$promise;
                    }
                }
            })
            .state('jht.decision.application.wizard.research.summary', {
                url: '/summary',
                templateUrl: 'harvestpermit/applications/research/summary/summary.html',
                controller: 'ResearchPermitWizardSummaryController',
                controllerAs: '$ctrl',
                resolve: {
                    application: function (applicationId, ResearchPermitApplication) {
                        return ResearchPermitApplication.getFullDetails({id: applicationId}).$promise;
                    }
                }
            });
    })

    .controller('ResearchPermitWizardSummaryController', function ($scope, $q, $translate, $filter, dialogs,
                                                                   NotificationService, ActiveRoleService,
                                                                   UnsavedChangesConfirmationService, ReasonAsker,
                                                                   HarvestPermitApplications,
                                                                   ApplicationWizardNavigationHelper,
                                                                   DecisionDeliveryAddressModal, ConfirmationDialogService,
                                                                   wizard, application) {
        var $ctrl = this;
        var dateFilter = $filter('date');

        $ctrl.$onInit = function () {
            $ctrl.application = application;

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
            wizard.goto('attachments');
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
            return ConfirmationDialogService.showConfimationDialogWithPrimaryAccept(
                'harvestpermit.wizard.summary.sendConfirmation.title',
                'harvestpermit.wizard.summary.sendConfirmation.body');
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

    .component('researchApplicationSummary', {
        templateUrl: 'harvestpermit/applications/research/summary/summary-accordion.html',
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

    .component('researchApplicationSummarySpecies', {
        templateUrl: 'harvestpermit/applications/research/summary/summary-species.html',
        bindings: {
            species: '<'
        }
    })

    .component('researchApplicationSummaryArea', {
        templateUrl: 'harvestpermit/applications/research/summary/summary-area.html',
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

    .component('researchApplicationSummaryReasons', {
        templateUrl: 'harvestpermit/applications/research/summary/summary-reasons.html',
        bindings: {
            reasons: '<'
        }
    })

    .component('researchApplicationSummaryPeriods', {
        templateUrl: 'harvestpermit/applications/research/summary/summary-periods.html',
        bindings: {
            periods: '<',
            years: '<'
        }
    })


    .component('researchApplicationSummaryPopulation', {
        templateUrl: 'harvestpermit/applications/research/summary/summary-population.html',
        bindings: {
            population: '<'
        }
    })

    .component('researchApplicationSummaryJustification', {
        templateUrl: 'harvestpermit/applications/research/summary/summary-justification.html',
        bindings: {
            justification: '<'
        }
    })

    .component('researchApplicationSummaryMethods', {
        templateUrl: 'harvestpermit/applications/research/summary/summary-methods.html',
        bindings: {
            methods: '<'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.show32 = !_.isEmpty($ctrl.methods.deviateSection32);
                $ctrl.show33 = !_.isEmpty($ctrl.methods.deviateSection33) || $ctrl.methods.tapeRecorders;
                $ctrl.show34 = !_.isEmpty($ctrl.methods.deviateSection34) || $ctrl.methods.traps;
                $ctrl.show35 = !_.isEmpty($ctrl.methods.deviateSection35);
                $ctrl.show51 = !_.isEmpty($ctrl.methods.deviateSection51);

                $ctrl.showSpecies = $ctrl.show32 || $ctrl.show33 || $ctrl.show34 || $ctrl.show35 || $ctrl.show51;
            };
        }
    });
