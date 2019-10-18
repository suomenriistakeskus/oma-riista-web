'use strict';

angular.module('app.mapeditor.mml', [])
    .factory('GeoJsonEditorMML', function ($q, $timeout, GIS) {
        var REGEX_MML_FEATURE_ID = new RegExp('^[0-9]+');

        function isMmlFeature(feature) {
            return _.isObject(feature) && _.isString(feature.id) && REGEX_MML_FEATURE_ID.test(feature.id);
        }

        function isChangedFeature(feature) {
            return _.get(feature, 'properties.changed', false) === true;
        }

        function filterMmlFeatures(layers) {
            return _(layers)
                .map('feature')
                .filter(isMmlFeature)
                .sortBy(['properties.number', 'id'])
                .value();
        }

        function parseChangedFeatureIds(layers) {
            return _(layers)
                .map('feature')
                .filter(isMmlFeature)
                .filter(isChangedFeature)
                .map('id')
                .value();
        }

        function checkHasChangedFeatures(featureList) {
            return _.some(featureList, isChangedFeature);
        }

        function Service(leafletFeatureGroup) {
            this.leafletFeatureGroup = leafletFeatureGroup;
            this.originalChangedIds = parseChangedFeatureIds(leafletFeatureGroup.getLayers());
            this.cachedPalstaFeatures = filterMmlFeatures(leafletFeatureGroup.getLayers());
            this.cachedHasChangedFeatures = checkHasChangedFeatures(this.cachedPalstaFeatures);
        }

        var proto = Service.prototype;

        proto.updateCachedFeatures = function () {
            var self = this;

            $timeout(function () {
                self.cachedPalstaFeatures = filterMmlFeatures(self.leafletFeatureGroup.getLayers());
                self.cachedHasChangedFeatures = checkHasChangedFeatures(self.cachedPalstaFeatures);
            });
        };

        proto.getFeatureList = function () {
            return this.cachedPalstaFeatures;
        };

        proto.isFeatureListEmpty = function () {
            return _.size(this.cachedPalstaFeatures) === 0;
        };

        proto.hasChangedFeatures = function () {
            return this.cachedHasChangedFeatures;
        };

        proto.toGeoJSON = function () {
            var geoJson = this.leafletFeatureGroup.toGeoJSON();

            var self = this;

            _.forEach(geoJson.features, function (f) {
                var isChanged = isChangedFeature(f);

                // Skip geometries and extra properties
                delete f.properties;
                delete f.geometry;

                // Feature was initially marked as changed but was replaced with fresh copy
                if (!isChanged && self.originalChangedIds.indexOf(f.id) !== -1) {
                    f.properties = {
                        fixed: true
                    };
                }
            });

            return geoJson;
        };

        proto.addGeoJSON = function (geojson) {
            var currentIds = [];

            this.leafletFeatureGroup.eachLayer(function (layer) {
                if (layer.feature.id) {
                    currentIds.push(layer.feature.id);
                }
            });

            // Do not import duplicates
            _.remove(geojson.features, function (f) {
                return currentIds.indexOf(f.id) !== -1;
            });

            this.leafletFeatureGroup.addData(geojson);
            this.updateCachedFeatures();
        };

        proto.removeGeoJSON = function (geojson) {
            var featureIds = _.map(geojson.features, function (f) {
                return f.id;
            });

            var callback = _.bind(function (layer) {
                if (_.includes(featureIds, layer.feature.id)) {
                    this.leafletFeatureGroup.removeLayer(layer);
                }
            }, this);

            this.leafletFeatureGroup.eachLayer(callback);
            this.updateCachedFeatures();
        };

        proto.removeLayer = function (feature, layer) {
            this.leafletFeatureGroup.removeLayer(layer);
            this.updateCachedFeatures();
        };

        proto.removeFeatureById = function (featureId) {
            var callback = _.bind(function (layer) {
                this.leafletFeatureGroup.removeLayer(layer);
            }, this);

            this.eachLayerById(featureId, callback);
            this.updateCachedFeatures();
        };

        proto.eachLayerById = function (featureId, callback) {
            var propGetter = _.property('feature.id');

            this.leafletFeatureGroup.eachLayer(function (layer) {
                if (featureId === propGetter(layer)) {
                    callback(layer);
                }
            });
        };

        proto.updateChangedFeature = function (feature) {
            var oldId = feature.id;
            var replacementId = feature.properties.new_palsta_id;

            if (!replacementId || !oldId) {
                return $q.reject();
            }

            var self = this;

            return GIS.getPropertyPolygonById(replacementId).then(function (response) {
                var geoJson = response.data;

                if (geoJson.features.length) {
                    self.removeFeatureById(oldId);
                    self.addGeoJSON(geoJson);
                    return geoJson;
                } else {
                    return $q.reject();
                }
            });
        };

        return {
            isMmlFeature: isMmlFeature,
            isChangedFeature: isChangedFeature,
            create: function (leafletFeatureGroup) {
                return new Service(leafletFeatureGroup);
            }
        };
    })

    .filter('propertyFeaturesChanged', function () {
        return function (input) {
            if (_.isArray(input)) {
                return _.filter(input, _.matchesProperty('properties.changed', true));
            }
            return input;
        };
    })

    .service('PropertyIdentifierService', function () {
        var simplePattern = new RegExp("^\\d{14}$");
        var formattedPattern = new RegExp("^(\\d{1,3})-(\\d{1,3})-(\\d{1,4})-(\\d{1,4})$");

        this.parseFromString = function (value) {
            if (simplePattern.test(value)) {
                return value;

            } else {
                var parts = formattedPattern.exec(value);

                if (parts && parts.length > 4) {
                    var padThree = _.partial(_.padStart, _, 3, '0');
                    var padFour = _.partial(_.padStart, _, 4, '0');

                    return [
                        padThree(parts[1]),
                        padThree(parts[2]),
                        padFour(parts[3]),
                        padFour(parts[4])
                    ].join('');
                }
            }

            return undefined;
        };
    })

    .directive('propertyIdentifierList', function (PropertyIdentifierService) {
        return {
            restrict: 'A',
            priority: 100,
            require: 'ngModel',
            link: function (scope, element, attr, ctrl) {
                function _parseValues(propertyList) {
                    var list = _.chain(propertyList).map(PropertyIdentifierService.parseFromString).compact().value();
                    return list.length !== propertyList.length ? undefined : _.uniq(list);
                }

                var parse = function (viewValue) {
                    // If the viewValue is invalid (say required but empty) it will be `undefined`
                    if (angular.isUndefined(viewValue)) {
                        return;
                    }

                    var list = [];

                    if (viewValue) {
                        angular.forEach(viewValue.split(/[\s,;]+/), function (value) {
                            if (value) {
                                list.push(value.trim());
                            }
                        });
                    }

                    return _parseValues(list);
                };

                ctrl.$parsers.push(parse);
                ctrl.$formatters.push(function (value) {
                    if (angular.isArray(value)) {
                        return value.join('\n');
                    }

                    return undefined;
                });

                // Override the standard $isEmpty because an empty array means the input is empty.
                ctrl.$isEmpty = function (value) {
                    return !value || !value.length;
                };
            }
        };
    });
