'use strict';

angular.module('app.clubhunting.day', [])

    .factory('ClubGroupHuntingDay', function ($resource) {
        return $resource('api/v1/club/group/huntingday/:id', {'id': '@id'}, {
            'get': {method: 'GET'},
            'update': {method: 'PUT'},
            'getOrCreate': {
                method: 'POST',
                url: 'api/v1/club/group/huntingday/get-or-create',
                params: {huntingGroupId: '@huntingGroupId', date: '@date'}
            }
        });
    })

    .service('ClubHuntingDayService', function ($translate, dialogs, ClubGroupDiary, ClubGroupHuntingDay, ClubGroups,
                                                FormSidebarService, Helpers, NotificationService) {
        var modalOptions = {
            controller: 'GroupHuntingDayFormController',
            templateUrl: 'club/hunting/day/hunting-day.html',
            largeDialog: false,
            resolve: {}
        };

        function parametersToResolve(params) {
            return {
                huntingDay: _.constant(params.huntingDay),
                existingHuntingDates: function () {
                    return ClubGroupDiary.huntingDays({
                        id: params.groupId
                    }).$promise.then(function (days) {
                        return _.map(days, 'startDate');
                    });
                },
                permitSpeciesAmount: function () {
                    return ClubGroups.permitSpeciesAmount({
                        clubId: params.clubId,
                        id: params.groupId
                    }).$promise;
                }
            };
        }

        var formSidebar = FormSidebarService.create(modalOptions, ClubGroupHuntingDay, parametersToResolve);

        this.createHuntingDay = function (clubId, groupId, startDateAsString) {
            if (!groupId) {
                console.log("groupId is null");
                return;
            }

            return formSidebar.show({
                clubId: clubId,
                groupId: groupId,
                huntingDay: {
                    huntingGroupId: groupId,
                    startDate: startDateAsString,
                    startTime: '06:00',
                    endDate: startDateAsString,
                    endTime: '21:00'
                }
            });
        };

        this.editHuntingDay = function (clubId, groupId, huntingDayId) {
            return formSidebar.show({
                id: huntingDayId,
                clubId: clubId,
                groupId: groupId,
                huntingDay: ClubGroupHuntingDay.get({id: huntingDayId}).$promise
            });
        };

        this.deleteHuntingDay = function (huntingDayId) {
            var dialogTitle = $translate.instant('global.dialog.confirmation.title');
            var dialogMessage = $translate.instant('global.dialog.confirmation.text');

            var dialog = dialogs.confirm(dialogTitle, dialogMessage);

            return dialog.result.then(function () {
                return ClubGroupHuntingDay.delete({id: huntingDayId}).$promise.then(function () {
                    NotificationService.showDefaultSuccess();
                }, function () {
                    NotificationService.showDefaultFailure();
                });
            });
        };

        this.getOrCreate = function (groupId, pointOfTime) {
            var date = Helpers.dateToString(moment(pointOfTime, 'YYYY-MM-DD[T]HH:mm'), 'YYYY-MM-DD');
            return ClubGroupHuntingDay.getOrCreate({huntingGroupId: groupId, date: date}).$promise;
        };
    })

    .controller('GroupHuntingDayFormController', function ($scope, $filter, Helpers,
                                                           ClubHuntingDayService,
                                                           ClubHuntingPersistentState,
                                                           HuntingYearService,
                                                           HarvestPermitSpeciesAmountService,
                                                           huntingDay, existingHuntingDates, permitSpeciesAmount) {
        $scope.huntingDay = huntingDay;
        $scope.huntingMethods = _.range(1, 9).map(function (i) {
            return 'OPTION_' + i;
        });

        var selectedHuntingYear = ClubHuntingPersistentState.getSelectedHuntingYear();
        var currentHuntingYear = HuntingYearService.getCurrent();
        var huntingYear = selectedHuntingYear || currentHuntingYear;
        var huntingStart = HuntingYearService.getBeginDateStr(huntingYear);
        var huntingEnd = HuntingYearService.getEndDateStr(huntingYear);

        function toMoment(date) {
            return Helpers.toMoment(date, 'YYYY-MM-DD');
        }

        function parseDateAndTime(date, time) {
            return Helpers.parseDateAndTime(date, time, 'YYYY-MM-DD');
        }

        function minutesToHours(minutes) {
            return angular.isNumber(minutes) ? _.round(minutes / 60.0, 1) : 0;
        }

        $scope.viewState = {
            startDate: huntingDay.startDate,
            startTime: huntingDay.startTime,
            endDate: huntingDay.endDate,
            endTime: huntingDay.endTime,
            breakDuration: minutesToHours(huntingDay.breakDurationInMinutes)
        };

        var originalDate = huntingDay.startDate;
        $scope.isNotDuplicateDate = function () {
            if (!$scope.viewState.startDate) {
                return true;
            }
            var day = Helpers.dateToString($scope.viewState.startDate, 'YYYY-MM-DD');
            var dateIsUnchanged = day && originalDate === day && huntingDay.id;
            var noDuplicateFound = day && _.indexOf(existingHuntingDates, day) < 0;

            return dateIsUnchanged || noDuplicateFound;
        };

        $scope.isPermitValidOnDate = function () {
            return !$scope.viewState.startDate || HarvestPermitSpeciesAmountService.isValidDateForSpeciesAmount(
                    permitSpeciesAmount, $scope.viewState.startDate);
        };

        $scope.onStartDateChanged = function (startDay) {
            if (!$scope.huntingDay.id) {
                $scope.viewState.endDate = startDay;
            }
        };

        $scope.onEndDateChanged = function (endDate) {
            var start = toMoment($scope.viewState.startDate);
            var end = toMoment(endDate);

            if (start && end && !$scope.huntingDay.id) {
                var diff = end.diff(start, 'days');

                if (diff < 0 || diff > 1) {
                    $scope.viewState.startDate = endDate;
                }
            }
        };

        $scope.getMinDate = function (dayId) {
            if (dayId === 'end') {
                if ($scope.huntingDay.id) {
                    return Helpers.dateToString(toMoment($scope.viewState.startDate));
                }
            }

            return huntingStart;
        };

        $scope.getMaxDate = function (dayId) {
            var maxDate = toMoment(huntingEnd);

            if (maxDate.isAfter(new Date(), 'day')) {
                maxDate = moment();
            }

            if (dayId === 'end') {
                var startDate = $scope.viewState.startDate;

                if (startDate) {
                    var startMoment = toMoment(startDate);
                    var nextDay = startMoment.add(1, 'day');

                    if (nextDay.isBefore(maxDate, 'day') || nextDay.isSame(maxDate, 'day')) {
                        maxDate = nextDay;
                    }
                }
            }

            return Helpers.dateToString(maxDate);
        };

        function breakDurationInMinutes() {
            var hours = $scope.viewState.breakDuration;
            return angular.isNumber(hours) ? Math.round(hours * 60.0) : 0;
        }

        $scope.periodLengthInMinutes = function () {
            var start = parseDateAndTime($scope.viewState.startDate, $scope.viewState.startTime);
            var end = parseDateAndTime($scope.viewState.endDate, $scope.viewState.endTime);
            var breakMinutes = breakDurationInMinutes();

            return start && end ? end.diff(start, 'minute') - breakMinutes : null;
        };

        $scope.isValidPeriod = function () {
            var start = parseDateAndTime($scope.viewState.startDate, $scope.viewState.startTime);
            var end = parseDateAndTime($scope.viewState.endDate, $scope.viewState.endTime);

            if (start && end) {
                var durationMinutes = end.diff(start, 'minute') - breakDurationInMinutes();

                return durationMinutes > 0 && durationMinutes <= (2 * 24 * 60);
            }

            return false;
        };

        $scope.showHoundCount = function () {
            var method = $scope.huntingDay.huntingMethod;
            var methodIndex = _.indexOf($scope.huntingMethods, method);

            return methodIndex === 0 || methodIndex === 1;
        };

        $scope.isValid = function (form) {
            return form.$valid && $scope.isValidPeriod() && $scope.isNotDuplicateDate() && $scope.isPermitValidOnDate();
        };

        $scope.delete = function () {
            if ($scope.huntingDay.id) {
                ClubHuntingDayService.deleteHuntingDay($scope.huntingDay.id).then(function () {
                    $scope.$dismiss('delete');
                });
            }
        };

        $scope.cancel = function () {
            $scope.$dismiss('cancel');
        };

        $scope.ok = function (form) {
            $scope.$broadcast('show-errors-check-validity');

            if (!$scope.isValid(form)) {
                return;
            }

            if (!$scope.showHoundCount()) {
                $scope.huntingDay.numberOfHounds = 0;
            }

            $scope.huntingDay.breakDurationInMinutes = breakDurationInMinutes();
            $scope.huntingDay.startDate = Helpers.dateToString($scope.viewState.startDate);
            $scope.huntingDay.endDate = Helpers.dateToString($scope.viewState.endDate);
            $scope.huntingDay.startTime = $scope.viewState.startTime;
            $scope.huntingDay.endTime = $scope.viewState.endTime;

            $scope.$close($scope.huntingDay);
        };
    });
