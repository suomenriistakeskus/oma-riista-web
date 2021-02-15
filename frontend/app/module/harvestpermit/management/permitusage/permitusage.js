'use strict';

angular.module('app.harvestpermit.management.permitusage', [])

    .controller('PermitUsageController', function ($state, $uibModal, NotificationService, SpeciesNameService,
                                                   PermitUsage, permitUsages, permitId) {

        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.permitUsages = permitUsages;
            $ctrl.permitId = permitId;

            $ctrl.lastModifier = permitUsages && permitUsages.length > 0 ? permitUsages[0].lastModifier : null;

            updateKeys();
        };

        function updateKeys() {
            $ctrl.permitUsages.forEach(function (usage) {
                usage.permitUsageLocations.forEach(function (location, index) {
                    location.key = index + 1;
                });
            });
        }

        $ctrl.addLocation = function (usage, form) {
            usage.permitUsageLocations.push({
                geoLocation: {},
                key: usage.permitUsageLocations.length + 1});
            form.$setDirty();
        };

        $ctrl.removeLocation = function (usage, removedLocation, form) {
            var locationPosition = _.findIndex(usage.permitUsageLocations, function (location) {
                return location.key === removedLocation.key;
            });
            if (locationPosition >= 0) {
                usage.permitUsageLocations.splice(locationPosition, 1);
                updateKeys();
            }

            form.$setDirty();
        };

        $ctrl.addLocationFromMap = function (usage, location, form) {
            $uibModal.open({
                templateUrl: 'harvestpermit/management/permitusage/permit-usage-location.html',
                controller: 'HarvestPermitUsageLocationModalController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesCode: usage.speciesCode,
                    location: location,
                    form: form
                }
            }).result.then($ctrl.onSuccess, $ctrl.onFailure);
        };

        $ctrl.onSuccess = function (newLocation) {
            var usagePos = _.findIndex($ctrl.permitUsages, function (usage) {
                return usage.speciesCode === newLocation.speciesCode;
            });
            if (usagePos === -1) {
                return;
            }

            var locationPos = _.findIndex($ctrl.permitUsages[usagePos].permitUsageLocations, function (location) {
                return location.key === newLocation.location.key;
            });
            if (locationPos === -1) {
                return;
            }

            delete newLocation.speciesCode;
            $ctrl.permitUsages[usagePos].permitUsageLocations[locationPos] = newLocation.location;
            newLocation.form.$setDirty();
        };

        $ctrl.onFailure = function (error) {
            if (error !== 'cancel') {
                NotificationService.showDefaultFailure();
            }
        };

        $ctrl.isLocationsValid = function () {
            var invalidLocations = _.filter($ctrl.permitUsages, function (usage) {
                var locs = _.filter(usage.permitUsageLocations, function (location) {
                    return !location.geoLocation.latitude || !location.geoLocation.longitude;
                });
                return locs.length > 0;
            });
            return invalidLocations.length === 0;
        };

        $ctrl.save = function (form) {
            $ctrl.permitUsages.forEach(function (usage) {
                usage.permitUsageLocations.forEach(function (location) {
                    delete location.key;
                });
            });
            PermitUsage.save({id: $ctrl.permitId}, {usageList: $ctrl.permitUsages}).$promise
                .then(function () {
                    NotificationService.showDefaultSuccess();
                    updateKeys();
                    $state.reload();
                }, function () {
                    NotificationService.showDefaultFailure();
                });

        };

        $ctrl.getSpeciesName = function (speciesCode) {
            return SpeciesNameService.translateSpeciesCode(speciesCode);
        };
    })

    .controller('HarvestPermitUsageLocationModalController', function($uibModalInstance,
                                                                      MapState, MapDefaults, MapUtil,
                                                                      speciesCode, location, form) {
        var $ctrl = this;

        $ctrl.$onInit = function() {
            $ctrl.location = location;
            $ctrl.locationKey = location.key;
            $ctrl.speciesCode = speciesCode;
            $ctrl.form = form;

            $ctrl.mapState = MapState.get();
            $ctrl.mapDefaults = MapDefaults.create({scrollWheelZoom: false});
            $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents(['click']);

            MapState.updateMapCenter(location.geoLocation.longitude
                ? angular.copy(location.geoLocation)
                : MapUtil.getDefaultGeoLocation(), 6);
        };

        $ctrl.save = function () {
            angular.extend($ctrl.location, {key: $ctrl.locationKey});
            $uibModalInstance.close({
                speciesCode: $ctrl.speciesCode,
                location: $ctrl.location,
                form: $ctrl.form
            });
        };

        $ctrl.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    });

