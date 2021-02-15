'use strict';

angular.module('app.harvestreport.services', ['ngResource'])

    .factory('HarvestReports', function ($resource) {
        return $resource('api/v1/harvestreport/:id', {id: '@harvestId'}, {
            update: {method: 'PUT'},
            changeState: {
                method: 'POST',
                url: 'api/v1/harvestreport/harvest/:id/state'
            }
        });
    })

    .factory('HarvestReportLocalityResolver', function ($filter, Areas) {
        var i18n = $filter('rI18nNameFilter');

        function createOrganisationNameLookup(areas) {
            return _.chain(areas)
                .map(function (area) {
                    return _.map(area.subOrganisations, function (org) {
                        return [org.id, {
                            name: org.name,
                            areaName: area.name
                        }];
                    });
                })
                .filter()
                .flatten()
                .fromPairs()
                .value();
        }

        return {
            get: function () {
                return Areas.query().$promise.then(function (areas) {
                    var organisations = createOrganisationNameLookup(areas);

                    return {
                        getHarvestAreaName: function (harvest) {
                            return i18n(harvest.harvestArea) || '-';
                        },
                        getAreaName: function (harvest) {
                            return _.get(organisations[harvest.rhyId], 'areaName', '-');
                        },
                        getRhyName: function (harvest) {
                            return _.get(organisations[harvest.rhyId], 'name', '-');
                        }
                    };
                });
            }
        };
    })

    .service('HarvestReportFieldsAndSeasons', function ($filter, $http, $translate, ActiveRoleService, Helpers) {
        var i18nNameFilter = $filter('rI18nNameFilter');

        function namePermitHarvest() {
            return $translate.instant('harvestreport.reportType.PERMIT');
        }

        function nameOtherHarvest() {
            return $translate.instant('harvestreport.reportType.OTHERS');
        }

        function translateSeason(season) {
            var parts = [];

            parts.push(i18nNameFilter(season.name));
            parts.push(' ');
            parts.push(Helpers.dateIntervalToString(season.beginDate, season.endDate));

            if (season.beginDate2 && moment().isAfter(season.beginDate2)) {
                parts.push(', ');
                parts.push(Helpers.dateIntervalToString(season.beginDate2, season.endDate2));
            }

            return parts.join('');
        }

        function translatePermit(species) {
            return i18nNameFilter(species.name) + ' - ' + namePermitHarvest();
        }

        this.valids = function (excludePermitNotRequiredWithoutSeason) {
            // For normal users request active seasons, which limits seasons to only those
            // where today is between begin-date and end-of-reporting-date.
            // For moderators and coordinators request all, even expired seasons.
            var moderatorOrCoordinator = ActiveRoleService.isModerator() || ActiveRoleService.isCoordinator();

            return $http.get('/api/v1/harvestreport/categories', {
                params: {
                    activeOnly: !moderatorOrCoordinator,
                    excludePermitNotRequiredWithoutSeason: !!excludePermitNotRequiredWithoutSeason
                }
            }).then(function (res) {
                return _.chain(res.data)
                    .map(function (category) {
                        category.speciesName = i18nNameFilter(category.species.name);

                        if (category.type === 'SEASON') {
                            category.type = nameOtherHarvest();
                            category.name = translateSeason(category.season);
                            category.id = category.type + category.season.id;

                        } else if (category.type === 'PERMIT') {
                            category.type = namePermitHarvest();
                            category.name = translatePermit(category.species);
                            category.id = category.type + category.species.code;
                        }
                        return category;
                    })
                    .value();
            });
        };
    })

    .service('HarvestReportReasonAsker', function ($q, ReasonAsker, ActiveRoleService) {
        this.promptForReason = function () {
            if (!ActiveRoleService.isModerator()) {
                // No reason is required
                return $q.when(null);
            }

            return ReasonAsker.openModal({
                titleKey: 'harvestreport.admin.reasonTitle',
                messageKey: 'harvestreport.admin.reason'
            });
        };
    })
    .directive('harvestReportStateToClass', function () {
        function stateToClass(state) {
            if (state === 'REJECTED' || !state) {
                return 'r-harvestreport-rejected';
            } else if (state === 'APPROVED' || state === 'ACCEPTED') {
                return 'r-harvestreport-approved';
            } else if (state === 'SENT_FOR_APPROVAL') {
                return 'r-harvestreport-sent-for-approval';
            } else if (state === 'PROPOSED') {
                return 'r-harvestreport-proposed';
            }

            return 'r-harvestreport-unknown';
        }

        return {
            restrict: 'A',
            scope: false,
            link: function (scope, element, attrs) {
                var stateAttr = attrs.harvestReportStateToClass;

                element.addClass(stateToClass(scope.$eval(stateAttr)));

                scope.$watch(stateAttr, function (state, oldState) {
                    if (state !== oldState) {
                        element.removeClass(stateToClass(oldState));
                        element.addClass(stateToClass(state));
                    }
                });
            }
        };
    });
