'use strict';

angular.module('app.harvestpermit.search.permit', [])

    .controller('PermitSearchController', function ($translate,
                                                    HarvestPermitSearchStateHolder, HarvestPermits, TranslatedBlockUI,
                                                    areas, species, permitTypes, rhyId) {
        var $ctrl = this;
        $ctrl.allSpecies = species;
        $ctrl.permitTypes = permitTypes;
        $ctrl.speciesSortProperty = 'name.' + $translate.use();
        $ctrl.areas = areas;
        $ctrl.moderatorView = rhyId === null;

        $ctrl.states = ['EMPTY', 'SENT_FOR_APPROVAL', 'APPROVED', 'REJECTED'];
        $ctrl.validity = ['ACTIVE', 'PASSED', 'FUTURE'];

        var savedState = HarvestPermitSearchStateHolder.getState();
        $ctrl.searchParams = _.get(savedState, 'searchParams') || {
            areaId: null,
            speciesCode: null,
            permitNumber: null,
            state: null,
            reportNotDone: null,
            year: null,
            permitType: null
        };

        $ctrl.searchSort = _.get(savedState, 'searchSort') || {
            sortingType: 'NORMAL',
            permitNumberSort: 'ASC',
            yearSort: 'DESC',
            ordinalSort: 'DESC'
        };

        function search() {
            HarvestPermitSearchStateHolder.setState({
                searchParams: $ctrl.searchParams,
                searchSort: $ctrl.searchSort
            });

            var requestParams = angular.copy($ctrl.searchParams);
            requestParams.reportNotDone = $ctrl.searchParams.state === 'EMPTY';
            if (requestParams.reportNotDone) {
                requestParams.state = null;
            }
            requestParams.rhyId = rhyId;
            requestParams.sortingType = $ctrl.searchSort.sortingType;
            requestParams.permitNumberSort = $ctrl.searchSort.permitNumberSort;
            requestParams.yearSort = $ctrl.searchSort.yearSort;
            requestParams.ordinalSort = $ctrl.searchSort.ordinalSort;

            TranslatedBlockUI.start('global.block.wait');
            var searchMethod = rhyId ? HarvestPermits.rhySearch : HarvestPermits.search;
            searchMethod(requestParams).$promise.then(function (data) {
                $ctrl.permits = data;
            }).finally(function () {
                TranslatedBlockUI.stop();
            });
        }

        $ctrl.search = search;

        $ctrl.canSearch = function () {
            return _($ctrl.searchParams).values().without(null).value().length > 0;
        };

        if ($ctrl.canSearch()) {
            search();
        }

        $ctrl.permitTypeChanged = function () {
            if ($ctrl.searchParams.permitType) {
                $ctrl.allSpecies = _.filter(species, function (s) {
                    return _.indexOf($ctrl.searchParams.permitType.speciesCodes, s.code) >= 0;
                });
                if ($ctrl.allSpecies.length === 1) {
                    $ctrl.searchParams.speciesCode = $ctrl.allSpecies[0].code;
                }
            } else {
                $ctrl.allSpecies = species;
            }
        };
    })

    .component('searchPermitList', {
        templateUrl: 'harvestpermit/search/search-permit-list.html',
        bindings: {
            permits: '<',
            showOpenPermit: '<'
        }
    })

    .service('HarvestPermitSearchStateHolder', function () {
        var states = {};

        this.getState = function () {
            return states.permitSearchState;
        };

        this.setState = function (newState) {
            states.permitSearchState = newState;
        };
    })
;
