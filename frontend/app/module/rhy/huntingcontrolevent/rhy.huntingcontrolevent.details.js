'use strict';

angular.module('app.rhy.huntingcontrolevent.details', [])
    .component('rHuntingControllerEventDetails', {
        templateUrl: 'rhy/huntingcontrolevent/details.html',
        bindings: {
            event: '<',
            rhyBounds: '<',
            rhyGeoJson: '<',
            isCoordinator: '<',
            onChange: '&'
        },
        controller: function ($uibModal, FetchAndSaveBlob, Helpers, MapDefaults, NotificationService,
                              HuntingControlEvents, HuntingControlEventStatus) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.leaflet = {
                    mapDefaults: MapDefaults.create({scrollWheelZoom: false}),
                    bounds: $ctrl.rhyBounds,
                    mapFeatures: {
                        data: $ctrl.rhyGeoJson,
                        style: {
                            fillColor: "#A080B0",
                            weight: 2,
                            opacity: 0,
                            color: 'none',
                            fillOpacity: 0.45
                        }
                    }
                };
            };

            $ctrl.canEdit = function () {
                return $ctrl.event.status !== HuntingControlEventStatus.ACCEPTED &&
                    $ctrl.event.status !== HuntingControlEventStatus.ACCEPTED_SUBSIDIZED;
            };

            $ctrl.canReject = function () {
                return $ctrl.event.status !== HuntingControlEventStatus.REJECTED;
            };

            $ctrl.edit = function () {
                $uibModal.open(
                    {
                        templateUrl: 'rhy/huntingcontrolevent/form.html',
                        resolve: {
                            event: _.constant(angular.copy($ctrl.event)),
                            rhy: _.constant($ctrl.event.rhy),
                            rhyBounds: _.constant($ctrl.rhyBounds),
                            rhyGeoJSON: _.constant($ctrl.rhyGeoJson)
                        },
                        controller: 'HuntingControlEventFormController',
                        controllerAs: '$ctrl',
                        size: 'lg'
                    }).result.then($ctrl.onSuccess, $ctrl.onFailure);
            };

            $ctrl.onSuccess = function () {
                $ctrl.onChange();
                NotificationService.showDefaultSuccess();
            };

            $ctrl.onFailure = function (reason) {
                if (reason === 'error') {
                    NotificationService.showDefaultFailure();
                } else if (reason === 'attachmentsDeleted') {
                    $ctrl.onChange();
                }
            };

            $ctrl.accept = function () {
                HuntingControlEvents.accept({id: $ctrl.event.id}).$promise.then($ctrl.onSuccess, $ctrl.onFailure);
            };

            $ctrl.acceptSubsidized = function () {
                HuntingControlEvents.acceptSubsidized({id: $ctrl.event.id}).$promise.then($ctrl.onSuccess, $ctrl.onFailure);
            };

            $ctrl.reject = function () {
                HuntingControlEvents.reject({id: $ctrl.event.id}).$promise.then($ctrl.onSuccess, $ctrl.onFailure);
            };

            $ctrl.visibleHistoryEvents = function (showFullLog) {
                if (!_.isEmpty($ctrl.event.changeHistory)) {
                    return showFullLog ? $ctrl.event.changeHistory : [ _.last($ctrl.event.changeHistory) ];
                }
                return [];
            };

            $ctrl.hasHistoryEvents = function () {
                return (_.size($ctrl.event.changeHistory) > 0);
            };

            $ctrl.isFullLogButtonVisible = function () {
                return (_.size($ctrl.event.changeHistory) > 1);
            };
        }});