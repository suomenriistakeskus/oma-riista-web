'use strict';

angular.module('app.common.components', [])
    .component('rSpeciesSelection', {
        templateUrl: 'common/r-species-selection.html',
        bindings: {
            availableSpecies: '<',
            onSelectedSpeciesChanged: '&'
        },
        controller: function (SpeciesSortByName) {
            var $ctrl = this;
            $ctrl.selectedSpecies = null;

            $ctrl.onSpeciesChanged = function () {
                $ctrl.onSelectedSpeciesChanged({ speciesCode: _.get($ctrl.selectedSpecies, 'code', null) });
            };

            $ctrl.$onChanges = function (changes) {
                $ctrl.availableSpecies = SpeciesSortByName.sort(changes.availableSpecies.currentValue);
                $ctrl.selectedSpecies = _($ctrl.availableSpecies).first();
                $ctrl.onSpeciesChanged();
            };
        }
    })
    .component('rHuntingYearAndSpeciesSelection', {
        templateUrl: 'common/r-hunting-year-and-species-selection.html',
        bindings: {
            huntingYears: '<',
            availableSpecies: '<',
            onHuntingYearOrSpeciesChanged: '&'
        },
        controller: function (HuntingYearService) {
            var $ctrl = this;

            // Decorate with name parameter.
            $ctrl.decoratedYears = _($ctrl.huntingYears)
                .map(function (param) {
                    var obj;

                    if (_.isObject(param)) {
                        obj = param;
                    } else if (_.isFinite(param)) {
                        obj = { year: param };
                    }

                    if (_.isFinite(obj.year)) {
                        obj.name = HuntingYearService.toObj(obj.year).name;
                    }

                    return obj;
                })
                .value();

            $ctrl.selectedYear = null;
            $ctrl.selectedSpeciesCode = null;

            function updateYearAndSpecies() {
                $ctrl.onHuntingYearOrSpeciesChanged({
                    huntingYear: _.get($ctrl.selectedYear, 'year', null),
                    speciesCode: $ctrl.selectedSpeciesCode
                });
            }

            $ctrl.onHuntingYearChanged = function () {
                updateYearAndSpecies();
            };

            $ctrl.selectSpeciesCode = function (speciesCode) {
                $ctrl.selectedSpeciesCode = speciesCode;
                updateYearAndSpecies();
            };

            // init
            $ctrl.selectedYear = _($ctrl.decoratedYears).last();
        }
    })
    .component('rGeojsonMap', {
        templateUrl: 'common/r-geojson-map.html',
        bindings: {
            initialViewBounds: '<',
            featureCollection: '<'
        },
        controller: function (GIS, MapDefaults, MapState) {
            var $ctrl = this;

            $ctrl.map = {
                defaults: MapDefaults.create(),
                state: MapState.get(),
                events: MapDefaults.getMapBroadcastEvents(),
                geojson: null
            };

            $ctrl.$onInit = function () {
                // XXX: Must reset center so that viewBounds update triggers update
                MapState.get().center = {};

                setFeatureCollection($ctrl.featureCollection);
            };

            $ctrl.$onChanges = function (changesObj) {
                var newFeatureCollection = changesObj.featureCollection;

                if (newFeatureCollection) {
                    setFeatureCollection(newFeatureCollection.currentValue);
                }
            };

            function setFeatureCollection(featureCollection) {
                var geometriesIncluded = false;

                if (featureCollection) {
                    geometriesIncluded = _.chain(featureCollection.features)
                        .some(function (feature) {
                            return _.isObject(feature.geometry);
                        })
                        .value();

                    $ctrl.map.geojson = {
                        data: featureCollection,
                        onEachFeature: _.noop,
                        style: MapDefaults.getGeoJsonOptions()
                    };
                } else {
                    $ctrl.map.geojson = null;
                }

                if (geometriesIncluded) {
                    var featureBounds = GIS.getBoundsFromGeoJsonFeatureCollection(featureCollection);
                    MapState.updateMapBounds(featureBounds, $ctrl.initialViewBounds, true);
                } else {
                    MapState.updateMapBounds(null, $ctrl.initialViewBounds, true);
                }
            }
        }
    });
