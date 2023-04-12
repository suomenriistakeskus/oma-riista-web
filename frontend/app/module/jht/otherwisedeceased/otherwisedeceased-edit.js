'use strict';

angular.module('app.jht.otherwisedeceased-edit', [])
    .service('OtherwiseDeceasedEditModal', function ($uibModal) {

        this.open = function (item) {
            return $uibModal
                .open({ templateUrl: 'jht/otherwisedeceased/otherwisedeceased-edit.html',
                          controllerAs: '$ctrl',
                          resolve: { item: _.constant(item) },
                          size: 'lg',
                          controller: ModalController}).result;
        };

        function ModalController($scope, $uibModalInstance, $filter, GIS, Helpers, MapDefaults,
                                 MapState, MapUtil, OtherwiseDeceasedAges, OtherwiseDeceasedGenders,
                                 OtherwiseDeceasedCauses, OtherwiseDeceasedSources, OtherwiseDeceasedService, item) {
            var $ctrl = this;
            $ctrl.item = item || {};
            $ctrl.speciesOptions = OtherwiseDeceasedService.getSpeciesCodes();
            $ctrl.ageOptions = OtherwiseDeceasedAges;
            $ctrl.genderOptions = OtherwiseDeceasedGenders;
            $ctrl.causeOptions = OtherwiseDeceasedCauses;
            $ctrl.sourceOptions = OtherwiseDeceasedSources;
            $ctrl.species = !!$ctrl.item.gameSpeciesCode ? $ctrl.item.gameSpeciesCode.toString() : '';
            $ctrl.validRhy = !!$ctrl.item.rhy;
            $ctrl.validMunicipality = !!$ctrl.item.municipality;
            $ctrl.downloadAttachment = OtherwiseDeceasedService.downloadAttachment;
            $ctrl.deleteAttachment = OtherwiseDeceasedService.deleteAttachment;

            var dateFilter = $filter('date');
            $ctrl.date = dateFilter($ctrl.item.pointOfTime, 'yyyy-MM-dd');
            $ctrl.time = dateFilter($ctrl.item.pointOfTime, 'HH:mm');

            var location = $ctrl.item.geoLocation;

            if (!isValidLocation(location)) {
                location = MapUtil.getDefaultGeoLocation();
            }

            MapState.updateMapCenter(angular.copy(location), 7);

            $ctrl.mapState = MapState.get();
            $ctrl.mapDefaults = MapDefaults.create({scrollWheelZoom: false});
            $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents(['click']);

            $ctrl.isValidLocation = function () {
                return isValidLocation($ctrl.item.geoLocation)
                    && $ctrl.validRhy
                    && $ctrl.validMunicipality;
            };

            $ctrl.save = function () {
                var dateTime = moment($ctrl.date).toDate();
                dateTime.setHours($ctrl.time.slice(0, 2));
                dateTime.setMinutes($ctrl.time.slice(3));
                $ctrl.item.pointOfTime = Helpers.dateTimeToString(dateTime);
                $ctrl.item.gameSpeciesCode = parseInt($ctrl.species);

                OtherwiseDeceasedService.save($ctrl.item).then(
                    function (savedItem) {
                        $uibModalInstance.close(savedItem);
                    });
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };

            $scope.$watchGroup(
                ['$ctrl.item.geoLocation.longitude', '$ctrl.item.geoLocation.latitude'],
                function (newValues, oldValues) {
                    if (newValues[0] !== oldValues[0] || newValues[1] !== oldValues[1]) {
                        updateRhy();
                        updateMunicipality();
                    }
                });

            function updateRhy() {
                if (isValidLocation($ctrl.item.geoLocation)) {
                    GIS.getRhyForGeoLocation($ctrl.item.geoLocation)
                        .then(function (res) {
                            $ctrl.item.rhy = res.data;
                            $ctrl.validRhy = true;
                        }, function () {
                            $ctrl.validRhy = false;
                    });
                }
            }

            function updateMunicipality() {
                if (isValidLocation($ctrl.item.geoLocation)) {
                    GIS.getMunicipalityForGeoLocation($ctrl.item.geoLocation)
                        .then(function (res) {
                            $ctrl.item.municipality = res.data;
                            $ctrl.validMunicipality = true;
                        }, function () {
                            $ctrl.validMunicipality = false;
                        });
                }
            }

            function isValidLocation(location) {
                return _.isObject(location)
                    && _.isNumber(location.latitude)
                    && _.isNumber(location.longitude);
            }
        }
    });
