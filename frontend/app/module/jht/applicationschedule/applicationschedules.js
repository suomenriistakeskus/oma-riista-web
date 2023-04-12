'use strict';

angular.module('app.jht.applicationschedule', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('jht.applicationschedules', {
                url: '/applicationschedules',
                templateUrl: 'jht/applicationschedule/applicationschedules.html',
                resolve: {
                    schedules: function (ApplicationSchedule) {
                        return ApplicationSchedule.list().$promise;
                    }
                },
                controllerAs: '$ctrl',
                controller: function ($state, NotificationService, Helpers, ApplicationScheduleModal, schedules) {
                    var $ctrl = this;

                    $ctrl.$onInit = function () {
                        $ctrl.schedules = schedules;
                    };

                    $ctrl.edit = function (schedule) {
                        ApplicationScheduleModal.open(schedule).then(function (result) {
                            if (result !== 'error') {
                                NotificationService.showDefaultSuccess();
                                $state.reload();
                            } else {
                                NotificationService.showDefaultFailure();
                            }
                        });
                    };

                    $ctrl.isApplicationOpen = function (schedule) {
                        if (schedule.activeOverride !== null) {
                            return schedule.activeOverride;
                        }

                        var beginTime = Helpers.toMoment(schedule.beginTime);
                        var endTime = Helpers.toMoment(schedule.endTime);
                        var now = moment();
                        return now.isSameOrAfter(beginTime) && now.isSameOrBefore(endTime);
                    };

                    $ctrl.hasScheduledTimePassed = function (schedule) {
                        if (schedule.activeOverride !== null) {
                            return schedule.activeOverride;
                        }

                        var endTime = Helpers.toMoment(schedule.endTime);
                        var now = moment();
                        return now.isAfter(endTime);
                    };
                }
            });
    })

    .factory('ApplicationSchedule', function ($http, $resource) {
        return $resource('/api/v1/harvestpermit/application/schedules', {}, {
            list: {
                method: 'GET',
                isArray: true
            },
            update: {
                method: 'PUT'
            }
        });
    })

    .service('ApplicationScheduleModal', function ($uibModal, Helpers) {
        this.open = function (schedule) {
            return $uibModal.open({
                templateUrl: 'jht/applicationschedule/edit.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'md',
                resolve: {
                    schedule: Helpers.wrapToFunction(angular.copy(schedule))
                }
            }).result;
        };

        function ModalController($uibModalInstance, ApplicationSchedule, ActiveOverrideTypes, schedule) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.schedule = schedule;

                if ($ctrl.schedule.beginTime) {
                    var beginMoment = Helpers.toMoment($ctrl.schedule.beginTime);
                    $ctrl.beginDate = beginMoment.format("YYYY-MM-DD");
                    $ctrl.beginTime = beginMoment.format('HH:mm');
                } else {
                    $ctrl.beginDate = null;
                    $ctrl.beginTime = null;
                }

                if ($ctrl.schedule.endTime) {
                    var endMoment = Helpers.toMoment($ctrl.schedule.endTime);
                    $ctrl.endDate = endMoment.format("YYYY-MM-DD");
                    $ctrl.endTime = endMoment.format('HH:mm');
                } else {
                    $ctrl.endDate = null;
                    $ctrl.endTime = null;
                }

                $ctrl.activeOverrideTypes = ActiveOverrideTypes;

                switch ($ctrl.schedule.activeOverride) {
                    case true:
                        $ctrl.activeOverride = 'TRUE';
                        break;
                    case false:
                        $ctrl.activeOverride = 'FALSE';
                        break;
                    default:
                        $ctrl.activeOverride = 'SCHEDULED';
                }
            };

            $ctrl.save = function () {
                if ($ctrl.beginDate) {
                    var beginTime = $ctrl.beginTime ? $ctrl.beginTime : "00:00";
                    $ctrl.schedule.beginTime = $ctrl.beginDate + 'T' + beginTime;
                } else {
                    $ctrl.schedule.beginTime = null;
                }

                if ($ctrl.endDate) {
                    var endTime = $ctrl.endTime ? $ctrl.endTime : "23:59";
                    $ctrl.schedule.endTime = $ctrl.endDate + 'T' + endTime;
                } else {
                    $ctrl.schedule.endTime = null;
                }

                switch ($ctrl.activeOverride) {
                    case 'TRUE':
                        $ctrl.schedule.activeOverride = true;
                        break;
                    case 'FALSE':
                        $ctrl.schedule.activeOverride = false;
                        break;
                    default:
                        $ctrl.schedule.activeOverride = null;
                }

                ApplicationSchedule.update($ctrl.schedule).$promise
                    .then(function() {
                        $uibModalInstance.close();
                    }, function() {
                        $uibModalInstance.dismiss('error');
                    });
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };

            $ctrl.isScheduleStateInvalid = function () {
               return $ctrl.activeOverride === 'SCHEDULED' &&
                    ($ctrl.beginDate === null || $ctrl.beginTime === null ||
                    $ctrl.endDate === null || $ctrl.endTime === null);
            };

            $ctrl.isTimeRangeInvalid = function () {
                if ($ctrl.beginDate === null || $ctrl.beginTime === null ||
                    $ctrl.endDate === null || $ctrl.endTime === null) {
                    return false;
                }

                var begin = Helpers.toMoment($ctrl.beginDate + 'T' + $ctrl.beginTime, 'YYYY-MM-DD[T]HH:mm');
                var end = Helpers.toMoment($ctrl.endDate + 'T' + $ctrl.endTime, 'YYYY-MM-DD[T]HH:mm');

                return begin.isAfter(end);
            };

            $ctrl.stateChanged = function () {
                if ($ctrl.activeOverride !== 'SCHEDULED') {
                    $ctrl.beginDate = null;
                    $ctrl.beginTime = null;
                    $ctrl.endDate = null;
                    $ctrl.endTime = null;
                }
            };
        }
    })

    .constant('ActiveOverrideTypes', [
        'TRUE',
        'FALSE',
        'SCHEDULED'
    ]);
