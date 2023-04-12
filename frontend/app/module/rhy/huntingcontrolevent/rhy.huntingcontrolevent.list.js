'use strict';

angular.module('app.rhy.huntingcontrolevent.list', [])
    .controller('HuntingControlEventListController', function ($state, $scope, $uibModal, Helpers, NotificationService,
                                                               TranslatedSpecies, Species, ActiveRoleService,
                                                               FetchAndSaveBlob, HuntingControlEvents, rhy, events,
                                                               availableYears, rhyBounds, rhyGeoJson, refreshEvents,
                                                               HuntingControlEventStatus, HuntingControlEventTypeFilter,
                                                               HuntingControlEventStatusFilter, HuntingControlEventSubsidizedFilter) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.rhy = rhy;
            $ctrl.rhyBounds = rhyBounds;
            $ctrl.rhyGeoJson = rhyGeoJson;
            $ctrl.events = events;
            $ctrl.filters = {};
            $ctrl.filteredEvents = events;
            $ctrl.filterOptions = {
                types: [],          // Values set by updateFilterOptions()
                cooperations: [],   // Values set by updateFilterOptions()
                statuses: [
                    {value: HuntingControlEventStatusFilter.ACCEPTED_OR_SUBSIDIZED, name: 'rhy.huntingControlEvent.eventStatus.ACCEPTED'},
                    {value: HuntingControlEventStatusFilter.PROPOSED, name: 'rhy.huntingControlEvent.eventStatus.PROPOSED'},
                    {value: HuntingControlEventStatusFilter.REJECTED, name: 'rhy.huntingControlEvent.eventStatus.REJECTED'}],
                subsidizedValues: [
                    {value: HuntingControlEventSubsidizedFilter.NOT_SUBSIDIZED, name: 'rhy.huntingControlEvent.subsidizedEvent.no'},
                    {value: HuntingControlEventSubsidizedFilter.SUBSIDIZED, name: 'rhy.huntingControlEvent.subsidizedEvent.yes'}]
            };
            updateFilterOptions();

            $ctrl.availableYears = _.isEmpty(availableYears) ? [new Date().getFullYear()] : availableYears;
            $ctrl.calendarYear = _.last($ctrl.availableYears);

            $ctrl.isModerator = ActiveRoleService.isModerator();
            $ctrl.isCoordinator = ActiveRoleService.isCoordinator() || $ctrl.isModerator;

        };

        // Show only options that are present in results
        function updateFilterOptions() {
            $ctrl.filterOptions.types = _.map(_.uniq(_.map($ctrl.events, 'eventType')), function (t) {
                return t || HuntingControlEventTypeFilter.NOT_AVAILABLE; // null -> 'NOT_AVAILABLE'
            });
            $ctrl.filterOptions.cooperations = _.uniq(_.flatMap($ctrl.events, 'cooperationTypes'));
        }

        $ctrl.addEvent = function () {
            $uibModal.open(
                {
                   templateUrl: 'rhy/huntingcontrolevent/form.html',
                   resolve: {
                       event: _.constant({inspectors: [], cooperationTypes: []}),
                       rhy: _.constant($ctrl.rhy),
                       rhyBounds: _.constant($ctrl.rhyBounds),
                       rhyGeoJSON: _.constant($ctrl.rhyGeoJson)
                   },
                   controller: 'HuntingControlEventFormController',
                   controllerAs: '$ctrl',
                   size: 'lg'
                }).result.then($ctrl.onSuccess, $ctrl.onFailure);
        };

        $ctrl.onSuccess = function () {
            $state.reload();
            NotificationService.showDefaultSuccess();
        };

        $ctrl.onFailure = function (reason) {
            if (reason === 'error') {
                NotificationService.showDefaultFailure();
            } else if (reason === 'attachmentsDeleted') {
                refreshList(); // Even when edit is cancelled, attachment list were changed
            }
        };

        $ctrl.loadReport = function () {
            var filters = {
                year: $ctrl.calendarYear,
                eventType: $ctrl.filters.type,
                cooperationType: $ctrl.filters.cooperation,
                status: $ctrl.filters.status === HuntingControlEventStatusFilter.ACCEPTED_OR_SUBSIDIZED ?
                    HuntingControlEventStatus.ACCEPTED : $ctrl.filters.status,
                subsidized: $ctrl.filters.subsidized
            };
            FetchAndSaveBlob.post('/api/v1/hunting-control-event/report/' + $ctrl.rhy.id + '/pdf', filters);
        };

        function refreshList() {
            refreshEvents($ctrl.rhy.id, $ctrl.calendarYear)
                .then(function (events) {
                    $ctrl.events = events;
                    $ctrl.filteredEvents = events;
                    $ctrl.filters = {};
                    updateFilterOptions();
                });
        }

        $ctrl.onSelectedYearChanged = function () {
            refreshList();
        };

        $ctrl.onFilterChange = function () {
            var filter = {};
            var filterAcceptedAndSubsidized = false;

            if (!_.isNil($ctrl.filters.type)) {
                if ($ctrl.filters.type === HuntingControlEventStatus.NOT_AVAILABLE) {
                    filter.eventType = null;
                } else {
                    filter.eventType = $ctrl.filters.type;
                }
            }

            if (!_.isNil($ctrl.filters.cooperation)) {
                filter.cooperationTypes = [$ctrl.filters.cooperation]; // Filter by value in array
            }

            if (!_.isNil($ctrl.filters.status)) {
                if ($ctrl.filters.status === HuntingControlEventStatusFilter.ACCEPTED_OR_SUBSIDIZED) {
                    filterAcceptedAndSubsidized = true;
                } else {
                    filter.status = $ctrl.filters.status;
                }
            }

            if (!_.isNil($ctrl.filters.subsidized)) {
                filter.status = $ctrl.filters.subsidized;
                filterAcceptedAndSubsidized = false;
            }

            $ctrl.filteredEvents = _.filter(_.filter($ctrl.events, filter), function (e) {
                return !filterAcceptedAndSubsidized || HuntingControlEventStatusFilter.isAcceptedOrSubsidized(e.status);
            });
        };

        function subsidizeStatusUnknown(status) {
            return status === HuntingControlEventStatus.REJECTED
                || status === HuntingControlEventStatus.PROPOSED;
        }

        $ctrl.onStatusChange = function () {
            if (subsidizeStatusUnknown($ctrl.filters.status)) {
                $ctrl.filters.subsidized = null;
            }
            $ctrl.onFilterChange();
        };

        $ctrl.onSubsidizedChange = function () {
            if (!_.isNil($ctrl.filters.subsidized) && subsidizeStatusUnknown($ctrl.filters.status)) {
                $ctrl.filters.status = null;
            }
            $ctrl.onFilterChange();
        };

        $ctrl.onChange = function () {
            $state.reload();
        };

        $ctrl.remove = function (event) {
            $uibModal.open(
                {
                    templateUrl: 'rhy/huntingcontrolevent/remove.html',
                    resolve: {
                        event: Helpers.wrapToFunction(event)
                    },
                    controller: 'HuntingControlEventRemoveController',
                    controllerAs: '$ctrl'
                }).result.then($ctrl.onSuccess, $ctrl.onFailure);
        };

        $ctrl.exportToExcel = function () {
            // E.g.
            // coordinator: /api/v1/riistanhoitoyhdistys/123/huntingcontrolevents/excel/2021
            // game warden: /api/v1/riistanhoitoyhdistys/123/huntingcontrolevents/my/excel/2021
            FetchAndSaveBlob.post('/api/v1/riistanhoitoyhdistys/' + $ctrl.rhy.id + '/huntingcontrolevents/'
                                  + ($ctrl.isCoordinator ? '' : 'my/') + 'excel/' + $ctrl.calendarYear);
        };

        $ctrl.exportAllToExcel = function () {
            FetchAndSaveBlob.post('/api/v1/riistanhoitoyhdistys/huntingcontrolevents/excel/all/' + $ctrl.calendarYear);
        };
    });