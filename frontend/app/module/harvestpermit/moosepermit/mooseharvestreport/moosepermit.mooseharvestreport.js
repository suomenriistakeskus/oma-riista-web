(function () {
    'use strict';

    angular.module('app.moosepermit.mooseharvetsreport', [])
        .service('MooseHarvestReportService', MooseHarvestReportService)
        .controller('MooseHarvestReportFormController', MooseHarvestReportFormController);

    function MooseHarvestReportService(FormSidebarService) {
        this.editMoosePermitHarvestReport = function (permit, species) {
            var modalOptions = {
                controller: 'MooseHarvestReportFormController',
                templateUrl: 'harvestpermit/moosepermit/mooseharvestreport/moose-harvest-report.html',
                largeDialog: true,
                resolve: {
                    permit: _.constant(permit),
                    species: _.constant(species)
                }
            };
            var formSidebar = FormSidebarService.create(modalOptions, null, _.identity);
            return formSidebar.show({});
        };
    }

    function MooseHarvestReportFormController($scope, NotificationService, ClubPermits, permit, species) {
        $scope.permit = permit;
        $scope.mooseHarvestReport = permit.mooseHarvestReport;
        $scope.hasHarvests = permit.totalPayment.totalPayment > 0;

        $scope.canCreateMooseHarvestReport = permit.hasPermissionToCreateOrRemove &&
            !(permit.mooseHarvestReport && permit.mooseHarvestReport.moderatorOverride) &&
            permit.allPartnersFinishedHunting &&
            permit.amendmentPermitsMatchHarvests;

        $scope.canRemoveMooseHarvestReport = permit.hasPermissionToCreateOrRemove &&
            permit.mooseHarvestReport && !permit.mooseHarvestReport.moderatorOverride;

        $scope.dropzone = null;
        $scope.dropzoneIncompatibleFileType = false;

        var url = '/api/v1/harvestpermit/moosepermit/' + permit.id + '/species/' + species.code + '/harvestreport';
        $scope.dropzoneConfig = {
            autoProcessQueue: true,
            maxFiles: 1,
            maxFilesize: 10, // MiB
            uploadMultiple: false,
            url: url
        };

        $scope.dropzoneEventHandlers = {
            success: function (file) {
                $scope.dropzone.removeFile(file);
                $scope.dropzoneIncompatibleFileType = false;
                $scope.$close();
                NotificationService.showDefaultSuccess();
            },
            error: function (file, response, xhr) {
                $scope.dropzone.removeFile(file);
                $scope.dropzoneIncompatibleFileType = true;
            }
        };

        $scope.getReceiptUrl = function () {
            return url + '/receipt';
        };

        function showOkAndClose () {
            NotificationService.showDefaultSuccess();
            $scope.$close();
        }

        $scope.createNoHarvestsReport = function () {
            ClubPermits.noHarvests({permitId: permit.id, speciesCode: species.code}).$promise
                .then(showOkAndClose, NotificationService.showDefaultFailure);
        };

        $scope.removeHarvestReport = function () {
            ClubPermits.removeHarvestReport({permitId: permit.id, speciesCode: species.code}).$promise
                .then(showOkAndClose, NotificationService.showDefaultFailure);
        };

        $scope.cancel = function () {
            $scope.$dismiss('cancel');
        };
    }

})();
