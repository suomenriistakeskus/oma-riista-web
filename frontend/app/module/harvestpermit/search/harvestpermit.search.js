'use strict';

angular.module('app.harvestpermit.search.permit', [])

    .controller('PermitSearchController', function ($translate,
                                                    HarvestPermitSearchStateHolder, HarvestPermits, TranslatedBlockUI,
                                                    FetchAndSaveBlob, areas, species, permitTypes, rhyId) {
        var $ctrl = this;
        $ctrl.allSpecies = species;
        $ctrl.permitTypes = initializePermitTypes(permitTypes);
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

        function getSearchParams() {
            var requestParams = angular.copy($ctrl.searchParams);
            if (requestParams.permitType) {
                delete requestParams.permitType.category;
                delete requestParams.permitType.permitTypeName;
                delete requestParams.permitType.sortOrder;
            }
            requestParams.reportNotDone = $ctrl.searchParams.state === 'EMPTY';
            if (requestParams.reportNotDone) {
                requestParams.state = null;
                requestParams.decisionStatuses = ['UNCHANGED', 'RESTRICTED'];
            }
            requestParams.rhyId = rhyId;
            requestParams.sortingType = $ctrl.searchSort.sortingType;
            requestParams.permitNumberSort = $ctrl.searchSort.permitNumberSort;
            requestParams.yearSort = $ctrl.searchSort.yearSort;
            requestParams.ordinalSort = $ctrl.searchSort.ordinalSort;
            return requestParams;
        }

        function search() {
            HarvestPermitSearchStateHolder.setState({
                searchParams: $ctrl.searchParams,
                searchSort: $ctrl.searchSort
            });
            TranslatedBlockUI.start('global.block.wait');
            var searchMethod = rhyId ? HarvestPermits.rhySearch : HarvestPermits.search;
            searchMethod(getSearchParams()).$promise.then(function (data) {
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

        $ctrl.exportToExcel = function () {
            FetchAndSaveBlob.post('/api/v1/harvestpermit/admin/export', getSearchParams());
        };

        function initializePermitTypes(permitTypes) {
            return _.chain(permitTypes)
                .map(function (permitType) {
                    return _.assign(permitType, {
                        category: mapPermitOrigin(permitType.origin),
                        permitTypeName: mapPermitType(permitType),
                        sortOrder: deduceSortOrder(permitType.origin)
                    });
                })
                .value();
        }

        function mapPermitType(permitType) {
            switch (permitType.origin) {
                case 'LUPAHALLINTA':
                    return permitType.permitType;
                case 'OMA_RIISTA':
                    return $translate.instant('harvestpermit.wizard.summary.permitType.' + permitType.permitTypeCode);
            }

        }

        function mapPermitOrigin(origin) {
            switch (origin) {
                case 'LUPAHALLINTA':
                    return 'Lupahallinta';
                case 'OMA_RIISTA':
                    return 'Oma riista';
            }
        }

        function deduceSortOrder(origin) {
            switch (origin) {
                case 'OMA_RIISTA':
                    return 0;
                case 'LUPAHALLINTA':
                    return 1;
            }
        }
    })

    .component('searchPermitList', {
        templateUrl: 'harvestpermit/search/search-permit-list.html',
        bindings: {
            permits: '<',
            showOpenPermit: '<'
        },
        controller: function (PermitTypeCode) {
            var $ctrl = this;

            $ctrl.hasPermission = function (permit) {
                return PermitTypeCode.hasPermission(permit.permitTypeCode);
            };
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
