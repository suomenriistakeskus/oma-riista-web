'use strict';

angular.module('app.harvestreport.changehistory', [])
    .service('HarvestChangeHistoryModal', function ($uibModal, $http) {
        this.showModal = function (harvestId) {
            return $uibModal.open({
                templateUrl: 'harvestreport/history/harvest-change-history.html',
                resolve: {
                    eventList: function () {
                        return $http({
                            method: 'GET',
                            url: 'api/v1/harvestreport/harvest/' + harvestId + '/history'
                        }).then(function (res) {
                            return res.data;
                        });
                    }
                },
                controller: ModalController,
                controllerAs: '$ctrl',
                bindToController: true,
                size: 'lg'
            }).result;
        };

        function ModalController($uibModalInstance, eventList) {
            var $ctrl = this;

            $ctrl.eventList = eventList;
        }
    });
