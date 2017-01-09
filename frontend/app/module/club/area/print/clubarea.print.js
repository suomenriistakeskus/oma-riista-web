'use strict';

angular.module('app.clubarea.print', [])

    .service('ClubHuntingAreaPrintService', function($uibModal, FormPostService) {
        this.showModalDialog = function (area) {
            return $uibModal.open({
                controller: 'ClubHuntingAreaPrintController',
                templateUrl: 'club/area/print/club-area-print.html',
                resolve: {
                    area: _.constant(area)
                }
            });
        };

        this.printRequest = function (area, request) {
            var formSubmitAction = '/api/v1/club/' + area.clubId + '/area/' + area.id + '/print';

            FormPostService.submitFormUsingBlankTarget(formSubmitAction, request);
        };
    })

    .controller('ClubHuntingAreaPrintController', function (ClubHuntingAreaPrintService,
                                                            $scope, area) {
        $scope.request = {
            paperSize: 'A4',
            paperDpi: '300',
            paperOrientation: 'PORTRAIT'
        };
          
        $scope.save = function () {
            $scope.$close();

            ClubHuntingAreaPrintService.printRequest(area, $scope.request);
        };
        
        $scope.cancel = function () {
            $scope.$dismiss();
        };
    });
