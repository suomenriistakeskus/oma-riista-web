'use strict';

angular.module('app.jht.otherwisedeceased-details', [])
    .component('rOtherwiseDeceasedDetails', {
        templateUrl: 'jht/otherwisedeceased/otherwisedeceased-details.html',
        bindings: {
            itemId: '<',
            onUpdate: '&'
        },
        controller: function ($scope, MapDefaults, MapUtil, WGS84, TranslatedBlockUI, NotificationService,
                              OtherwiseDeceasedService, OtherwiseDeceasedEditModal) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                getDetails();
                $ctrl.mapDefaults = MapDefaults.create({scrollWheelZoom: false});
                $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents(['click']);
            };

            $ctrl.hasAttachments = function () {
                return !!$ctrl.item && !!$ctrl.item.attachments && $ctrl.item.attachments.length() > 0;
            };

            $ctrl.hasOtherCause = function () {
                return $ctrl.item.cause === 'OTHER' && !_.isEmpty($ctrl.item.causeOther);
            };

            $ctrl.edit = function () {
                OtherwiseDeceasedEditModal.open(angular.copy($ctrl.item)) // Needs to be copied, otherwise changes are shown when cancelled
                    .then(
                        function (savedItem) {
                            $ctrl.item = savedItem;
                            $ctrl.onUpdate(); // Apply changes to brief list too
                        },
                        getDetails); // There might an attachment been deleted.
            };

            $ctrl.copy = function () {
                var newItem = angular.copy($ctrl.item);
                newItem.id = null;
                newItem.attachments = [];
                newItem.changeHistory = [];
                OtherwiseDeceasedEditModal.open(newItem).then($ctrl.onUpdate);
            };

            $ctrl.reject = function () {
                OtherwiseDeceasedService.reject($ctrl.item.id).then($ctrl.onUpdate);
            };

            $ctrl.restore = function () {
                OtherwiseDeceasedService.restore($ctrl.item.id).then($ctrl.onUpdate);
            };

            $ctrl.downloadAttachment = function (id) {
                OtherwiseDeceasedService.downloadAttachment(id);
            };

            $scope.$watchGroup(
                ['$ctrl.item.geoLocation.longitude', '$ctrl.item.geoLocation.latitude'],
                function (newValues, oldValues) {
                    if (newValues[0] !== oldValues[0] || newValues[1] !== oldValues[1]) {
                        updateMapCenter();
                    }
                });

            function getDetails() {
                TranslatedBlockUI.start("global.block.wait");
                OtherwiseDeceasedService.getDetails($ctrl.itemId).then(
                    function (item) {
                        $ctrl.item = item;
                        updateMapCenter();
                        TranslatedBlockUI.stop();
                    },
                    function (reason) {
                        var message = reason && reason.statusText || 'Unknown Error';
                        NotificationService.showMessage(message, 'error', {translateMessage: false});
                        TranslatedBlockUI.stop();
                    });
            }

            function updateMapCenter() {
                if (_.isObject($ctrl.item.geoLocation)) {
                    var latlng = WGS84.fromETRS($ctrl.item.geoLocation.latitude, $ctrl.item.geoLocation.longitude);
                    $ctrl.center = {
                        lat: latlng.lat,
                        lng: latlng.lng,
                        zoom: MapUtil.limitDefaultZoom(7)
                    };
                }
            }
        }
    });