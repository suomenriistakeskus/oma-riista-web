'use strict';

angular.module('app.harvestpermit.application.nestremoval.summary', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.nestremoval.summary', {
                url: '/summary',
                templateUrl: 'harvestpermit/applications/nestremoval/summary/summary.html',
                controller: 'NestRemovalPermitWizardSummaryController',
                controllerAs: '$ctrl',
                hideFooter: true,
                resolve: {
                    application: function (applicationId, NestRemovalPermitApplication) {
                        return NestRemovalPermitApplication.getFullDetails({id: applicationId}).$promise;
                    }
                }
            })
            .state('jht.decision.application.wizard.nestremoval.summary', {
                url: '/summary',
                templateUrl: 'harvestpermit/applications/nestremoval/summary/summary.html',
                controller: 'NestRemovalPermitWizardSummaryController',
                controllerAs: '$ctrl',
                resolve: {
                    application: function (applicationId, NestRemovalPermitApplication) {
                        return NestRemovalPermitApplication.getFullDetails({id: applicationId}).$promise;
                    }
                }
            });
    })

    .controller('NestRemovalPermitWizardSummaryController', function ($scope, $q, $translate, $filter, dialogs,
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
            return ConfirmationDialogService.showConfirmationDialogWithPrimaryAccept(
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

    .component('nestRemovalApplicationSummary', {
        templateUrl: 'harvestpermit/applications/nestremoval/summary/summary-accordion.html',
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

    .component('nestRemovalApplicationSummarySpecies', {
        templateUrl: 'harvestpermit/applications/nestremoval/summary/summary-species.html',
        bindings: {
            species: '<'
        }
    })

    .component('nestRemovalApplicationSummaryArea', {
        templateUrl: 'harvestpermit/applications/nestremoval/summary/summary-area.html',
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

    .component('nestRemovalApplicationSummaryReasons', {
        templateUrl: 'harvestpermit/applications/nestremoval/summary/summary-reasons.html',
        bindings: {
            reasons: '<'
        }
    })

    .component('nestRemovalApplicationSummaryPeriods', {
        templateUrl: 'harvestpermit/applications/nestremoval/summary/summary-periods.html',
        bindings: {
            periods: '<',
        }
    })


    .component('nestRemovalApplicationSummaryPopulation', {
        templateUrl: 'harvestpermit/applications/nestremoval/summary/summary-population.html',
        bindings: {
            population: '<'
        }
    });
