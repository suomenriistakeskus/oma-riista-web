'use strict';

angular.module('app.harvestpermit.management.nestremoval', [])

    .controller('HarvestPermitNestRemovalUsageController', function ($state, $uibModal, NotificationService, SpeciesNameService,
                                                                     NestRemovalPermitUsage, permitUsages, permitId) {

        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.permitUsages = permitUsages;
            $ctrl.canEdit = permitUsages[0].canEdit;
            $ctrl.permitId = permitId;

            $ctrl.lastModifier = permitUsages && permitUsages.length > 0 ? permitUsages[0].lastModifier : null;

            $ctrl.locationTypes = ['NEST', 'CONSTRUCTION'];
            updateKeys();
        };

        function updateKeys() {
            $ctrl.permitUsages.forEach(function (usage) {
                usage.nestLocations.forEach(function (nestLocation, index) {
                    nestLocation.key = index + 1;
                });
            });
        }

        $ctrl.isLocationAddAvailable = function () {
            return _.findIndex(permitUsages, function (usage) {
                return usage.permitNestAmount || usage.permitConstructionAmount;
            }) !== -1;
        };

        $ctrl.addLocation = function (usage, form) {
            usage.nestLocations.push({
                geoLocation: {},
                nestLocationType: 'NEST',
                key: usage.nestLocations.length + 1});
            form.$setDirty();
        };

        $ctrl.removeLocation = function (usage, location, form) {
            var locationPosition = _.findIndex(usage.nestLocations, function (nestLocation) {
                return nestLocation.key === location.key;
            });
            if (locationPosition >= 0) {
                usage.nestLocations.splice(locationPosition, 1);
                updateKeys();
            }

            form.$setDirty();
        };

        $ctrl.addLocationFromMap = function (usage, location, form) {
            $uibModal.open({
                templateUrl: 'harvestpermit/management/nestremoval/nest-removal-location.html',
                controller: 'HarvestPermitNestRemovalUsageLocationModalController',
                controllerAs: '$ctrl',
                resolve: {
                    speciesCode: usage.speciesCode,
                    nestLocation: location,
                    form: form
                }
            }).result.then($ctrl.onSuccess, $ctrl.onFailure);
        };

        $ctrl.onSuccess = function (location) {
            var usagePos = _.findIndex($ctrl.permitUsages, function (usage) {
                return usage.speciesCode === location.speciesCode;
            });
            if (usagePos === -1) {
                return;
            }

            var locationPos = _.findIndex($ctrl.permitUsages[usagePos].nestLocations, function (nestLocation) {
                return nestLocation.key === location.location.key;
            });
            if (locationPos === -1) {
                return;
            }

            delete location.speciesCode;
            $ctrl.permitUsages[usagePos].nestLocations[locationPos] = location.location;
            location.form.$setDirty();
        };

        $ctrl.onFailure = function (error) {
            if (error !== 'cancel') {
                NotificationService.showDefaultFailure();
            }
        };

        $ctrl.isNestLocationsValid = function () {
            var invalidLocations = _.filter($ctrl.permitUsages, function (usage) {
                var locs = _.filter(usage.nestLocations, function (location) {
                    return !location.geoLocation.latitude || !location.geoLocation.longitude || !location.nestLocationType;
                });
                return locs.length > 0;
            });
            return invalidLocations.length === 0;
        };

        $ctrl.isUsageAmountsValid = function () {
            return _.filter($ctrl.permitUsages, function (usage) {
                return usage.usedNestAmount === null && usage.usedEggAmount === null && usage.usedConstructionAmount === null;
            }).length === 0;
        };

        $ctrl.save = function (form) {
            $ctrl.permitUsages.forEach(function (usage) {
                usage.nestLocations.forEach(function (nestLocation) {
                    delete nestLocation.key;
                });
            });
            NestRemovalPermitUsage.save({id: $ctrl.permitId}, {usageList: $ctrl.permitUsages}).$promise
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

    .controller('HarvestPermitNestRemovalUsageLocationModalController', function($uibModalInstance,
                                                                                 MapState, MapDefaults, MapUtil,
                                                                                 speciesCode, nestLocation, form) {
        var $ctrl = this;

        $ctrl.$onInit = function() {
            $ctrl.nestLocation = nestLocation;
            $ctrl.locationKey = nestLocation.key;
            $ctrl.nestLocationType = nestLocation.nestLocationType;
            $ctrl.speciesCode = speciesCode;
            $ctrl.form = form;

            $ctrl.mapState = MapState.get();
            $ctrl.mapDefaults = MapDefaults.create({scrollWheelZoom: false});
            $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents(['click']);

            MapState.updateMapCenter(nestLocation.geoLocation.longitude
                ? angular.copy(nestLocation.geoLocation)
                : MapUtil.getDefaultGeoLocation(), 6);
        };

        $ctrl.save = function () {
            angular.extend($ctrl.nestLocation, {key: $ctrl.locationKey, nestLocationType: $ctrl.nestLocationType});
            $uibModalInstance.close({
                speciesCode: $ctrl.speciesCode,
                location: $ctrl.nestLocation,
                form: $ctrl.form
            });
        };

        $ctrl.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    });

