'use strict';

angular.module('app.mapeditor.sidebar', [])

    .component('mapEditorFeatureDetails', {
        templateUrl: 'common/map-editor/feature-info.html',
        bindings: {
            feature: '<'
        }
    })

    .component('mapEditorSidebarTabs', {
        templateUrl: 'common/map-editor/sidebar-tabs.html',
        bindings: {
            selectedTab: '=',
            editor: '<',
            metsahallitus: '<'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.hasMMLChanges = function () {
                return $ctrl.editor.mml.hasChangedFeatures();
            };

            $ctrl.hasMetsahallitusChanges = function () {
                return $ctrl.metsahallitus.hasChangedFeatures();
            };
        }
    })

    .component('mapEditorMmlSidebar', {
        templateUrl: 'common/map-editor/sidebar-mml.html',
        bindings: {
            editor: '<',
            showBulkInsert: '&'
        },
        controller: function (NotificationService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.selectedTab = 'all';
            };

            $ctrl.getFeatureList = function () {
                return $ctrl.editor.mml.getFeatureList();
            };

            $ctrl.isEmpty = function () {
                return $ctrl.editor.mml.isFeatureListEmpty();
            };

            $ctrl.zoomFeature = function (feature) {
                $ctrl.editor.zoom(feature.id);
            };

            $ctrl.removeFeature = function (feature) {
                $ctrl.editor.removeFeatureById(feature.id);
            };

            $ctrl.setHighlight = function (feature) {
                $ctrl.editor.selection.setHighlight(feature);
            };

            $ctrl.removeHighlight = function (feature) {
                $ctrl.editor.selection.removeHighlight(feature);
            };

            $ctrl.updateChangedFeature = function (feature) {
                $ctrl.editor.mml.updateChangedFeature(feature).then(function () {
                    $ctrl.editor.selection.clearSelection();
                }, function () {
                    NotificationService.showDefaultFailure();
                });
            };
        }
    })

    .component('mapEditorDrawSidebar', {
        templateUrl: 'common/map-editor/sidebar-draw.html',
        bindings: {
            editor: '<'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.getFeatureList = function () {
                return $ctrl.editor.draw.getFeatureList();
            };

            $ctrl.isEmpty = function () {
                return $ctrl.editor.draw.isFeatureListEmpty();
            };

            $ctrl.zoomFeature = function (feature) {
                $ctrl.editor.zoom(feature.id);
            };

            $ctrl.removeFeature = function (feature) {
                $ctrl.editor.removeFeatureById(feature.id);
            };
        }
    })

    .component('mapEditorMetsahallitusSidebar', {
        templateUrl: 'common/map-editor/sidebar-metsahallitus.html',
        bindings: {
            editor: '<',
            metsahallitus: '<'
        },
        controller: function (GIS, UnsavedChangesConfirmationService, HuntingYearService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.mooseAreaSearchQuery = null;
                $ctrl.selectedTab = 'all';
                $ctrl.showOnlyChanged = false;
            };

            $ctrl.focusMooseArea = function (area) {
                $ctrl.editor.zoom(area.id);
            };

            $ctrl.onSelectMooseArea = function (area) {
                UnsavedChangesConfirmationService.setChanges(true);

                // De-select
                $ctrl.mooseAreaSearchQuery = null;

                // Show geometry
                loadMooseFeature(area);
            };

            $ctrl.removeMooseArea = function (area) {
                UnsavedChangesConfirmationService.setChanges(true);
                $ctrl.metsahallitus.removeSelectedArea(area);
                $ctrl.editor.removeMooseArea(area);
            };

            $ctrl.updateMooseArea = function (area) {
                var replacement = $ctrl.metsahallitus.findByCode(area.number);

                if (replacement) {
                    loadMooseFeature(replacement);
                }
            };

            $ctrl.setOnlyChanged = function (value) {
                $ctrl.showOnlyChanged = value;
            };

            $ctrl.getAreaList = function () {
                var areaList = $ctrl.metsahallitus.getSelectedAreaList();
                if ($ctrl.showOnlyChanged) {
                    return _.filter(areaList, function (area) {
                        return !$ctrl.metsahallitus.isUpToDate(area);
                    });
                }

                return areaList;
            };

            function loadMooseFeature(area) {
                GIS.getMetsahallitusHirviById(area.gid).then(function (response) {
                    $ctrl.editor.removeMooseArea(area);
                    $ctrl.editor.addGeoJSONFeatures(response.data);

                    var mooseArea = $ctrl.metsahallitus.addSelectedArea(area);
                    $ctrl.focusMooseArea(mooseArea);
                });
            }
        }
    })

    .component('mapEditorBulkSidebar', {
        templateUrl: 'common/map-editor/sidebar-bulk-import.html',
        bindings: {
            editor: '<'
        },
        controller: function (GIS, NotificationService) {
            var $ctrl = this;

            $ctrl.propertyList = [];
            $ctrl.okPropertyCount = null;
            $ctrl.invalidPropertyList = null;

            // Add area geometry using property identifier
            $ctrl.addPropertiesAsText = function (form) {
                if (form.$invalid) {
                    return;
                }

                var propertyListCopy = $ctrl.propertyList || [];

                $ctrl.propertyList = [];
                $ctrl.okPropertyCount = 0;
                $ctrl.invalidPropertyList = [];

                var handleResponse = function (propertyIdentifier) {
                    return function (response) {
                        if (response.data.features.length) {
                            $ctrl.editor.addGeoJSONFeatures(response.data);
                            $ctrl.okPropertyCount++;
                        } else {
                            $ctrl.invalidPropertyList.push(propertyIdentifier);
                        }
                    };
                };

                for (var i = 0; i < propertyListCopy.length; i++) {
                    var value = propertyListCopy[i];

                    GIS.getPropertyPolygonByCode(value)
                        .then(handleResponse(value), NotificationService.showDefaultFailure);
                }
            };
        }
    });
