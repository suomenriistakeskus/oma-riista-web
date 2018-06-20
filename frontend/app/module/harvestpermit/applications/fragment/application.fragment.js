'use strict';

angular.module('app.harvestpermit.application.fragment', [])

    .service('HarvestPermitApplicationFragmentInfoPopup', function ($uibModal, $q,
                                                                    WGS84, TranslatedBlockUI, HarvestPermitApplications) {

        var self = this;

        function fetchInfo(applicationId, location) {
            TranslatedBlockUI.start("global.block.wait");
            return HarvestPermitApplications.getGeometryFragmentInfo({id: applicationId}, location).$promise
                .then(function (data) {
                    return _.isEmpty(data.toJSON()) ? $q.reject() : data;
                })
                .finally(TranslatedBlockUI.stop);
        }

        self.popup = function (applicationId, wgs84LatLng) {
            var latLng = WGS84.toETRS(wgs84LatLng.lat, wgs84LatLng.lng);
            var location = {
                latitude: latLng.lat,
                longitude: latLng.lng
            };

            $uibModal.open({
                templateUrl: 'harvestpermit/applications/fragment/fragment-info.html',
                resolve: {
                    data: fetchInfo(applicationId, location)
                },
                size: 'lg',
                controllerAs: '$ctrl',
                controller: function ($scope, data) {
                    var $ctrl = this;
                    $ctrl.data = data;
                }
            });
        };
    });
