(function () {
    "use strict";

    angular.module('app.organisation.selection', [])
        .component('organisationNameLabel', {
            template: '{{ $ctrl.getText() }}</span>',
            bindings: {
                organisation: '<'
            },
            controller: function (OrganisationNameService) {
                var $ctrl = this;

                $ctrl.getText = function () {
                    return $ctrl.organisation ? OrganisationNameService.getName(
                        $ctrl.organisation.organisationType,
                        $ctrl.organisation.officialCode) : '';
                };
            }
        })
        .service('OrganisationNameService', function (Areas) {
            var areaPromise = Areas.query().$promise;
            var names = {};

            areaPromise.then(function (areas) {
                names['RK:850'] = 'Suomen riistakeskus';

                _.forEach(areas, function (area) {
                    names['RKA:' + area.officialCode] = area.name;

                    _.forEach(area.subOrganisations, function (rhy) {
                        names['RHY:' + rhy.officialCode] = rhy.name;
                    });
                });
            });

            this.getName = function (organisationType, officialCode) {
                var key = organisationType + ':' + officialCode;
                return organisationType && officialCode ? names[key] : key;
            };
        })
        .directive('organisationSelection', function () {
                return {
                    restrict: 'E',
                    templateUrl: 'organisation/organisation-selection.html',
                    scope: {
                        rhyCode: '=',
                        areaCode: '=',
                        showRk: '<',
                        showRka: '<',
                        showInactiveRhys: '<',
                        disableSelection: '<'
                    },
                    controllerAs: '$ctrl',
                    bindToController: true,
                    controller: OrganisationSelectionController
                };
            }
        );

    function OrganisationSelectionController($scope, OrganisationsByArea) {
        var $ctrl = this;

        $ctrl.updateModel = function () {
            if (!$ctrl.ready) {
                return;
            }

            if ($ctrl.selectedOrganisationType === 'RHY') {
                $ctrl.rhyCode = $ctrl.selectedRhy ? $ctrl.selectedRhy.officialCode : null;
                $ctrl.areaCode = $ctrl.selectedArea ? $ctrl.selectedArea.officialCode : null;

            } else if ($ctrl.selectedOrganisationType === 'RKA') {
                $ctrl.rhyCode = null;
                $ctrl.areaCode = $ctrl.selectedArea ? $ctrl.selectedArea.officialCode : null;

            } else {
                $ctrl.rhyCode = null;
                $ctrl.areaCode = null;
            }
        };

        function updateView(areas) {
            if ($ctrl.rhyCode) {
                $ctrl.selectedArea = _.find(areas, function (area) {
                    return _.some(area.subOrganisations, ['officialCode', $ctrl.rhyCode]);
                });

                if ($ctrl.selectedArea) {
                    $ctrl.selectedRhy = _.find($ctrl.selectedArea.subOrganisations, {officialCode: $ctrl.rhyCode});
                } else {
                    $ctrl.selectedRhy = null;
                }

            } else if ($ctrl.areaCode) {
                $ctrl.selectedArea = _.find(areas, {officialCode: $ctrl.areaCode});
                $ctrl.selectedRhy = null;

            } else {
                $ctrl.selectedOrganisationType = 'RK';
                $ctrl.selectedArea = null;
                $ctrl.selectedRhy = null;
            }
        }

        $ctrl.$onInit = function () {
            $ctrl.organisationTypes = ['RHY'];

            if ($ctrl.showRka) {
                $ctrl.organisationTypes.unshift('RKA');
            }

            if ($ctrl.showRk) {
                $ctrl.organisationTypes.unshift('RK');
            }

            $ctrl.selectedOrganisationType = $ctrl.rhyCode ? 'RHY'
                : $ctrl.showRka && $ctrl.areaCode ? 'RKA'
                    : $ctrl.showRk ? 'RK'
                        : 'RHY';

            var promise = !!$ctrl.showInactiveRhys
                ? OrganisationsByArea.queryAll().$promise
                : OrganisationsByArea.queryActive().$promise;

            // OR-4338 Force undefined codes to null to avoid extra calls to updateView
            if (!$ctrl.rhyCode) {
                $ctrl.rhyCode = null;
            }
            if (!$ctrl.areaCode) {
                $ctrl.areaCode = null;
            }

            promise.then(function (areas) {
                $ctrl.areas = areas;

                updateView(areas);

                $ctrl.ready = true;

                $scope.$watchGroup(['$ctrl.rhyCode', '$ctrl.areaCode'], function (newValues, oldValues) {
                    if (!angular.equals(newValues[0], oldValues[0]) ||
                        !angular.equals(newValues[1], oldValues[1])) {
                        updateView(areas);
                    }
                });
            });
        };
    }

})();
