'use strict';

angular.module('app.mapeditor.ui', [])
    .component('mapEditorLayout', {
        templateUrl: 'common/map-editor/editor-layout.html',
        bindings: {
            areaName: '<',
            featureCollection: '<',
            defaultBounds: '<',
            metsahallitusYear: '<',
            hirviAreaList: '<',
            saveFeatures: '&',
            close: '&'
        },
        controller: function ($scope,
                              TranslatedBlockUI,
                              UnsavedChangesConfirmationService,
                              GIS, MapDefaults, MapState, MapBounds, ClubAreas,
                              GeoJsonEditor, GeoJsonEditorMetsahallitus) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.selectedTab = 'a';
                $ctrl.mapEvents = MapDefaults.getMapBroadcastEvents();
                $ctrl.mapDefaults = MapDefaults.create({hideOverlays: true, doubleClickZoom: false});
                $ctrl.mapState = MapState.get();
                $ctrl.metsahallitus = GeoJsonEditorMetsahallitus.create(
                    $ctrl.hirviAreaList, $ctrl.featureCollection, $ctrl.metsahallitusYear);

                $ctrl.editor = GeoJsonEditor.create($ctrl.featureCollection, function (fn) {
                    $scope.$apply(fn);
                });

                var areaBounds = MapBounds.getBoundsFromGeoJsonBbox($ctrl.featureCollection.bbox);
                MapState.updateMapBounds(areaBounds, $ctrl.defaultBounds, true);
            };

            $ctrl.save = function () {
                TranslatedBlockUI.start("global.block.wait");

                $ctrl.saveFeatures({
                    geoJSON: $ctrl.editor.toGeoJSON()
                }).finally(function () {
                    TranslatedBlockUI.stop();
                });
            };

            $ctrl.showBulkInsert = function () {
                $ctrl.selectedTab = 'c';
            };
        }
    })

    .component('mapEditorToolbar', {
        templateUrl: 'common/map-editor/editor-toolbar.html',
        bindings: {
            api: '<'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.currentTool = 'move';

            $ctrl.useTool = function (toolName) {
                $ctrl.api.useTool(toolName);
                $ctrl.currentTool = toolName;
            };

            $ctrl.isActiveTool = function (name) {
                return name && _.startsWith($ctrl.currentTool, name);
            };

            $ctrl.toolStyle = function (name, activeStyle, inactiveStyle) {
                var isActive = $ctrl.isActiveTool(name);
                return _.zipObject([activeStyle, inactiveStyle], [isActive, !isActive]);
            };

            $ctrl.buttonStyle = function (name) {
                return $ctrl.toolStyle(name, 'btn-primary', 'btn-default');
            };
        }
    })

    .filter('prettyAreaSize', function ($filter) {
        var numberFilter = $filter('number');

        return function (areaSize, fractionSize) {
            fractionSize = angular.isNumber(fractionSize) ? fractionSize : 2;
            return _.isNumber(areaSize) ? numberFilter(areaSize / 10000, fractionSize) + ' ha' : null;
        };
    })

    .filter('featureAreaSize', function ($filter) {
        var prettyAreaSize = $filter('prettyAreaSize');
        var propertyGetter = _.property('properties.size');

        return function (feature, fractionSize) {
            var areaSize = propertyGetter(feature);
            return _.isNumber(areaSize) ? prettyAreaSize(areaSize, fractionSize) : null;
        };
    })

    .filter('featureAreaSizeDiff', function ($filter) {
        var prettyAreaSize = $filter('prettyAreaSize');
        var propertyGetter = _.property('properties.diff_area');

        return function (feature) {
            var areaSize = propertyGetter(feature);
            if (_.isNumber(areaSize)) {
                return areaSize < 0 ? prettyAreaSize(areaSize) : '+' + prettyAreaSize(areaSize);
            }
            return null;
        };
    });
