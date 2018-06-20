'use strict';

angular.module('app.clubhunting.fixgeolocation', [])
    .config(function ($stateProvider) {
        $stateProvider.state('club.hunting.fixgeolocation', {
            parent: 'club',
            url: '/hunting/entry/fixgeolocation',
            templateUrl: 'club/hunting/fixgeolocation/layout.html',
            controller: 'OpenFixGeoLocationController',
            wideLayout: true,
            resolve: {
                selectedItem: function (ClubHuntingActiveEntry) {
                    return ClubHuntingActiveEntry.reloadSelectedItem();
                }
            }
        });
    })

    .controller('OpenFixGeoLocationController', function ($scope, $state, ClubHuntingActiveEntry,
                                                          FixGeoLocationFormService, NotificationService,
                                                          selectedItem) {
        $scope.diaryEntry = selectedItem.diaryEntry;

        FixGeoLocationFormService.openGeolocationForm($scope.diaryEntry)
            .then(function (fixedGeoLocation) {
                $scope.diaryEntry.geoLocation = fixedGeoLocation;
                NotificationService.showDefaultSuccess();
            }, function (err) {
                ClubHuntingActiveEntry.clearSelectedItem();

                var errorsToIgnore = ['cancel', 'escape', 'delete', 'back'];

                if (!angular.isString(err) || errorsToIgnore.indexOf(err) < 0) {
                    NotificationService.showDefaultFailure();
                }

                return err;
            })
            .finally(function () {
                $state.go('club.hunting');
            });
    })

    .controller('FixGeoLocationController', function ($scope, diaryEntry, mapMarkerUpdater) {
        $scope.diaryEntry = diaryEntry;

        $scope.updateGeolocationOnMap = function () {
            mapMarkerUpdater($scope.diaryEntry.geoLocation);
        };

        $scope.save = function () {
            $scope.$close($scope.diaryEntry.geoLocation);
        };

        $scope.cancel = function () {
            $scope.$dismiss('cancel');
        };
    })

    .service('FixGeoLocationFormService', function ($q, ClubGroupDiary, offCanvasStack) {

        function createLocation (lat, lng) {
            return {
                latitude: lat,
                longitude: lng,
                accuracy: 0,
                source: 'MANUAL'
            };
        }

        this.openGeolocationForm = function (diaryEntry) {
            var resolve = {
                diaryEntry: _.constant(diaryEntry),
                mapMarkerUpdater: function () {
                    return function (geoLocation) {
                        diaryEntry.geoLocation = createLocation(geoLocation.latitude, geoLocation.longitude);
                    };
                }
            };

            return offCanvasStack.open({
                templateUrl: 'club/hunting/fixgeolocation/fix-geolocation.html',
                resolve: resolve,
                controller: 'FixGeoLocationController',
                largeDialog: false
            }).result.then(function (fixedGeoLocation) {
                var entryId = diaryEntry.id,
                    promise;

                if (diaryEntry.isHarvest()) {
                    promise = ClubGroupDiary.editHarvestLocation({harvestId: entryId}, fixedGeoLocation).$promise;
                } else if (diaryEntry.isObservation()) {
                    promise = ClubGroupDiary.editObservationLocation({observationId: entryId}, fixedGeoLocation).$promise;
                } else {
                    // Should never end up here!
                    return $q.reject();
                }

                return promise.then(function () {
                    return fixedGeoLocation;
                });
            });
        };
    });
