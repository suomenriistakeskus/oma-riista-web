'use strict';

angular.module('app.harvestpermit.management.endofhunting', [])
    .factory('PermitEndOfHuntingReport', function ($resource) {
        var prefix = 'api/v1/harvestreport/permit/:id';
        return $resource(prefix, {id: '@id'}, {
            changeState: {
                method: 'POST',
                url: prefix + '/state'
            }
        });
    })
    .service('PermitEndOfHuntingReportService', function (PermitEndOfHuntingReport, PermitEndOfHuntingReportModal) {
        this.openModal = function (permitId) {
            return PermitEndOfHuntingReport.get({id: permitId}).$promise.then(function (report) {
                return PermitEndOfHuntingReportModal.showModal(report);
            });
        };
    })
    .service('PermitEndOfHuntingReportModal', function ($uibModal, NotificationService, PermitEndOfHuntingReport) {
        this.showModal = function (report) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/management/endofhunting/end-of-hunting-report.html',
                resolve: {
                    report: _.constant(report)
                },
                controller: ModalController,
                controllerAs: '$ctrl',
                bindToController: true,
                size: 'lg'
            }).result;
        };

        function ModalController($uibModalInstance, report) {
            var $ctrl = this;

            $ctrl.report = report;
            $ctrl.hasHarvests = report.harvests && report.harvests.length;

            $ctrl.create = function () {
                var promise = PermitEndOfHuntingReport.save({id: report.permitId}).$promise;
                handleActionResultPromise(promise);
            };

            $ctrl.remove = function () {
                var promise = PermitEndOfHuntingReport.delete({id: report.permitId}).$promise;
                handleActionResultPromise(promise);
            };

            $ctrl.accept = function () {
                var promise = PermitEndOfHuntingReport.changeState({id: report.permitId}, {
                    to: 'APPROVED',
                    id: report.permitId,
                    rev: report.permitRev
                }).$promise;
                handleActionResultPromise(promise);
            };

            function handleActionResultPromise($promise) {
                $promise.then(function () {
                    NotificationService.showDefaultSuccess();
                    $uibModalInstance.close();
                }, function () {
                    NotificationService.showDefaultFailure();
                });
            }
        }
    })
    .service('MooseHarvestReportModal', function ($uibModal, $state, MoosePermits) {
        this.openModal = function (permitId, gameSpeciesCode) {
            return MoosePermits.get({
                permitId: permitId,
                species: gameSpeciesCode

            }).$promise.then(function (permit) {
                return $uibModal.open({
                    templateUrl: 'harvestpermit/management/endofhunting/moose-harvest-report.html',
                    resolve: {
                        permit: _.constant(permit),
                        gameSpeciesCode: _.constant(gameSpeciesCode)
                    },
                    controller: ModalController,
                    controllerAs: '$ctrl',
                    size: permit.mooseHarvestReport ? 'md' : 'lg'
                }).result;
            });
        };

        function ModalController($uibModalInstance, $state, $translate, dialogs,
                                 NotificationService, FormPostService,
                                 MoosePermitEndOfHuntingReport,
                                 permit, gameSpeciesCode) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.permit = permit;
                $ctrl.mooseHarvestReport = permit.mooseHarvestReport;
                $ctrl.uri = '/api/v1/harvestreport/moosepermit/' + permit.id + '/' + gameSpeciesCode;

                $ctrl.hasHarvests = permit.totalPayment.totalPayment > 0;

                $ctrl.finishedWithoutHarvests = $ctrl.mooseHarvestReport
                    && !$ctrl.mooseHarvestReport.moderatorOverride
                    && $ctrl.mooseHarvestReport.noHarvests;

                $ctrl.canCreateMooseHarvestReport = permit.canModifyEndOfHunting
                    && permit.allPartnersFinishedHunting
                    && permit.amendmentPermitsMatchHarvests
                    && !($ctrl.mooseHarvestReport && $ctrl.mooseHarvestReport.moderatorOverride);

                $ctrl.canRemoveMooseHarvestReport = permit.canModifyEndOfHunting
                    && $ctrl.mooseHarvestReport
                    && !$ctrl.mooseHarvestReport.moderatorOverride;

                $ctrl.canDownloadMooseHarvestReport = $ctrl.mooseHarvestReport
                    && !$ctrl.mooseHarvestReport.noHarvests
                    && !$ctrl.mooseHarvestReport.moderatorOverride;
            };

            $ctrl.downloadReceipt = function () {
                FormPostService.submitFormUsingBlankTarget($ctrl.uri + '/receipt');
            };

            $ctrl.done = function () {
                NotificationService.showDefaultSuccess();
                $uibModalInstance.close();
                $state.reload();
            };

            $ctrl.createNoHarvestsReport = function () {
                MoosePermitEndOfHuntingReport.noHarvests({
                    permitId: permit.id,
                    speciesCode: gameSpeciesCode

                }).$promise.then($ctrl.done, function () {
                    NotificationService.showDefaultFailure();
                });
            };

            $ctrl.removeHarvestReport = function () {
                var dialogTitle = $translate.instant('global.dialog.confirmation.title');
                var dialogMessage = $translate.instant('global.dialog.confirmation.text');

                dialogs.confirm(dialogTitle, dialogMessage).result.then(function () {
                    MoosePermitEndOfHuntingReport.remove({
                        permitId: permit.id,
                        speciesCode: gameSpeciesCode

                    }).$promise.then($ctrl.done, function () {
                        NotificationService.showDefaultFailure();
                    });
                });
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }
    })

    .component('moosePermitUploadReceipt', {
        templateUrl: 'harvestpermit/management/endofhunting/moose-permit-upload-receipt.html',
        bindings: {
            uri: '<',
            done: '&'
        },
        controller: function (NotificationService) {
            var $ctrl = this;

            $ctrl.dropzone = null;
            $ctrl.dropzoneIncompatibleFileType = false;

            $ctrl.$onInit = function () {
                $ctrl.dropzoneConfig = {
                    autoProcessQueue: true,
                    maxFiles: 1,
                    maxFilesize: 50, // MiB
                    uploadMultiple: false,
                    url: $ctrl.uri
                };
            };

            $ctrl.dropzoneEventHandlers = {
                success: function (file) {
                    $ctrl.dropzone.removeFile(file);
                    $ctrl.dropzoneIncompatibleFileType = false;
                    $ctrl.done();
                    NotificationService.showDefaultSuccess();
                },
                error: function (file, response, xhr) {
                    $ctrl.dropzone.removeFile(file);
                    $ctrl.dropzoneIncompatibleFileType = true;
                }
            };
        }
    })

    .component('moosePermitPriceBreakdown', {
        templateUrl: 'harvestpermit/management/endofhunting/moose-permit-price-breakdown.html',
        bindings: {
            totalPayment: '<'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.showPriceBreakdown = false;
            };

            $ctrl.toggle = function () {
                $ctrl.showPriceBreakdown = !$ctrl.showPriceBreakdown;
            };

            $ctrl.getToggleClasses = function () {
                return {
                    'glyphicon-chevron-right': $ctrl.showPriceBreakdown,
                    'glyphicon-chevron-down': !$ctrl.showPriceBreakdown
                };
            };
        }
    });
