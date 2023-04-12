'use strict';

angular.module('app.club.deercensus', [])

    .factory('DeerCensus', function ($resource) {
        return $resource('api/v1/deercensus/:id', {id: "@id"}, {
            update: {method: 'PUT'},
            findByClubId: {
                method: 'GET',
                isArray: true,
                url: 'api/v1/deercensus/club/:clubId',
                params: {
                    clubId: "@clubId"
                }
            },
            getAttachments: {method: 'GET', url: 'api/v1/deercensus/:id/attachment', isArray: true},
            getAttachmentsByIds: {
                method: 'GET',
                url: 'api/v1/deercensus/attachment/tmp',
                isArray: true,
                params: {
                    attachmentIds: "@attachmentIds"
                }},
        });
    })

    .service('DeerCensusService', function ($q, $uibModal, NotificationService, ActiveRoleService,
                                            DeerCensus) {

        /**
         * Notification is visible if editing DeerCensus is allowed,
         * current month is April or May, and there is no DeerCensus for current year.
         */
        this.isCurrentYearDeerCensusNotificationVisible  = function (clubId) {
            if (!this.isEditDeerCensusAllowed()) {
                return $q.when(false);
            }

            if (!isAprilOrMay()) {
                return $q.when(false);
            }

            return DeerCensus.findByClubId({clubId: clubId}).$promise.then(function (deerCensuses) {
                return isDeerCensusMissingForCurrentYear(deerCensuses);
            });
        };

        function isAprilOrMay() {
            var currentMonth = new Date().getMonth();
            var MONTH_APRIL = 3;
            var MONTH_MAY = 4;
            return currentMonth === MONTH_APRIL || currentMonth === MONTH_MAY;
        }

        function isDeerCensusMissingForCurrentYear (deerCensuses) {
            if (deerCensuses && deerCensuses.length) {
                var currentYear = new Date().getFullYear();
                for (var i = 0; i < deerCensuses.length; i++) {
                    if (deerCensuses[i].year === currentYear) {
                        return false;
                    }
                }
            }
            return true;
        }

        this.isEditDeerCensusAllowed  = function () {
            return ActiveRoleService.isModerator() ||
                ActiveRoleService.isClubContact() || ActiveRoleService.isClubGroupLeader();
        };

        this.viewDeerCensuses = function (clubId) {

            var modalInstance = $uibModal.open({
                templateUrl: 'club/deercensus/deer-census-view.html',
                size: 'lg',
                controllerAs: '$ctrl',
                controller: 'DeerCensusViewController',
                resolve: {
                    deerCensuses: DeerCensus.findByClubId({clubId: clubId}).$promise
                }
            });

            modalInstance.rendered.then(function () {
                var nodeList = document.querySelectorAll('.modal');

                for (var i = 0; i < nodeList.length; i++) {
                    nodeList[i].scrollTop = 0;
                }
            });

            return modalInstance.result.then(function () {
                return $q.resolve();
            });
        };

        this.createDeerCensus = function (clubId) {
            var deerCensus = { huntingClubId: clubId };
            return openDeerCensusForm(deerCensus);
        };
        this.editDeerCensus = function (deerCensus) {
            deerCensus = Object.assign({}, deerCensus);
            return openDeerCensusForm(deerCensus);
        };
        function openDeerCensusForm (deerCensus) {
            var modalInstance = $uibModal.open({
                templateUrl: 'club/deercensus/deer-census.html',
                size: 'lg',
                controllerAs: '$ctrl',
                controller: 'DeerCensusFormController',
                resolve: {
                    deerCensus: deerCensus,
                    deerCensuses: DeerCensus.findByClubId({clubId: deerCensus.huntingClubId }).$promise
                }
            });

            modalInstance.rendered.then(function () {
                var nodeList = document.querySelectorAll('.modal');

                for (var i = 0; i < nodeList.length; i++) {
                    nodeList[i].scrollTop = 0;
                }
            });

            return modalInstance.result.then(function (deerCensus) {
            });
        }

        /**
         * Returns array of years from deerCensuses excluding deerCensus.
         */
        this.getYearsFromOtherDeerCensuses = function (deerCensuses, deerCensus) {
            var deerCensusesWithoutCurrent = _.reject(deerCensuses, ['id', deerCensus.id]);
            return _.map(deerCensusesWithoutCurrent, "year");
        };
    })
    /**
     * Directive for date input.
     * Sets datesOverlapping error if year of the date input is found
     * from scope.uniqueYearsBlacklist array.
     * Example value for uniqueYearsBlacklist: [2020, 2021]
     */
    .directive('uniqueYear', function() {
        return {
            require: 'ngModel',
            link: function(scope, element, attrs, ngModelCtrl) {
                ngModelCtrl.$validators.datesOverlapping = function(modelValue, viewValue) {
                    if (ngModelCtrl.$isEmpty(modelValue)) {
                        return true;
                    }
                    var uniqueYearsBlacklist = scope.uniqueYearsBlacklist;
                    if (!uniqueYearsBlacklist) {
                        return true;
                    }
                    var modelYear = null;
                    if (modelValue instanceof Date) {
                        modelYear = modelValue.getFullYear();
                    } else {
                        modelYear = new Date(modelValue).getFullYear();
                    }
                    var foundFromBlacklist = uniqueYearsBlacklist.indexOf(modelYear) > -1;
                    return !foundFromBlacklist;
                };
            }
        };
    })
    /**
     * Directive for date input.
     * Sets dayAndMonthBetweenMinMax error if date is outside given range.
     * Expects min-day, min-month, max-day and max-month attributes.
     * Expects days 1-31 and months 1-12.
     */
    .directive('dayAndMonthBetweenMinMax', function (Helpers) {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, element, attrs, ngModelCtrl) {
                ngModelCtrl.$validators.dayAndMonthBetweenMinMax = function (modelValue, viewValue) {
                    var dateString = modelValue || viewValue;

                    if (ngModelCtrl.$isEmpty(dateString)) {
                        return true;
                    }

                    var date = Helpers.toMoment(dateString, 'YYYY-MM-DD');
                    var year = date.year();
                    var minDay = attrs.minDay ? scope.$eval(attrs.minDay) : 1;
                    var minMonth = attrs.minMonth ? scope.$eval(attrs.minMonth) : 1;
                    var maxDay = attrs.maxDay ? scope.$eval(attrs.maxDay) : 31;
                    var maxMonth = attrs.maxMonth ? scope.$eval(attrs.maxMonth) : 12;

                    var minDate = moment({ year: year , month: minMonth - 1, day: minDay });
                    var maxDate = moment({ year: year , month: maxMonth - 1, day: maxDay });
                    if (date.isBefore(minDate) || date.isAfter(maxDate)) {
                        return false;
                    }
                    return true;
                };
            }
        };
    })

    .controller('DeerCensusViewController', function ($uibModalInstance, $scope, DeerCensusService,
                                                      DeerCensus, Helpers, NotificationService, deerCensuses) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.deerCensuses = deerCensuses;
            $ctrl.selectDeerCensus($ctrl.deerCensuses.length ? $ctrl.deerCensuses[0] : {});
            $ctrl.isEditAllowed =  DeerCensusService.isEditDeerCensusAllowed();

            $ctrl.attachmentConfig = {
                baseUri: '/api/v1/deercensus/attachment',
                canDownload: true,
                canDelete: false
            };
        };

        $ctrl.selectDeerCensus = function (deerCensus) {
            $ctrl.deerCensus = deerCensus;
            refreshAttachments();
        };

        $ctrl.editDeerCensus = function () {
            DeerCensusService.editDeerCensus($ctrl.deerCensus).finally(function () {
                DeerCensus.findByClubId({clubId: $ctrl.deerCensus.huntingClubId}).$promise.then(function (result) {
                    $ctrl.deerCensuses = result;
                    $ctrl.selectDeerCensus(_.find($ctrl.deerCensuses, ['id', $ctrl.deerCensus.id]));
                }, function (err) {
                    NotificationService.showDefaultFailure();
                });
            });
        };

        $ctrl.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };

        function refreshAttachments() {
            $ctrl.attachments = null;

            DeerCensus.getAttachments({id: $ctrl.deerCensus.id}).$promise.then(function (result) {
                $ctrl.attachments = result;
            }, function (err) {
                NotificationService.showDefaultFailure();
            });
        }
    })

    .controller('DeerCensusFormController', function ($uibModalInstance, $scope, DeerCensusService,
                                                      DeerCensus, Helpers, deerCensus, deerCensuses,
                                                      $timeout, NotificationService, $cookies) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            //Edited deerCensus can not have same year as previously saved deerCensuses
            $scope.uniqueYearsBlacklist = DeerCensusService.getYearsFromOtherDeerCensuses(deerCensuses, deerCensus);

            $ctrl.deerCensus = deerCensus;
            $ctrl.deerCensuses = deerCensuses;
            $ctrl.currentDate = new Date();
            $ctrl.isEditAllowed =  DeerCensusService.isEditDeerCensusAllowed();

            $ctrl.refreshAttachments();
            initDropzones();
        };

        $ctrl.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };

        $ctrl.submit = function (form) {
            prepareForSubmit(form, $ctrl.isValid);

            var saveMethod = deerCensus.id ? DeerCensus.update : DeerCensus.save;

            saveMethod(deerCensus).$promise.then(function (result) {
                NotificationService.showDefaultSuccess();
                $uibModalInstance.close($ctrl.deerCensus);

            }, function (err) {
                NotificationService.showDefaultFailure();
                $uibModalInstance.close($ctrl.deerCensus);
            });
        };

        $ctrl.doFinalSubmit = function (form) {
            prepareForSubmit(form, $ctrl.isValidForFinalSubmit);
            $uibModalInstance.close($ctrl.deerCensus);
        };

        $ctrl.isValid = function (form) {
            return form.$valid;
        };

        $ctrl.isValidForFinalSubmit = function (form) {
            return $ctrl.isValid(form);
        };

        function prepareForSubmit(form, checkFormValidFn) {
            $scope.$broadcast('show-errors-check-validity');

            if (!checkFormValidFn(form)) {
                return;
            }
        }

        $ctrl.refreshAttachments = function() {
            $ctrl.attachments = null;

            if ($ctrl.deerCensus.id) {
                DeerCensus.getAttachments({id: $ctrl.deerCensus.id}).$promise.then(function (result) {
                    $ctrl.attachments = result;
                }, function (err) {
                    NotificationService.showDefaultFailure();
                });
            } else if ($ctrl.deerCensus.attachmentIds && $ctrl.deerCensus.attachmentIds.length) {
                DeerCensus.getAttachmentsByIds({attachmentIds: $ctrl.deerCensus.attachmentIds}).$promise.then(function (result) {
                    $ctrl.attachments = result;
                }, function (err) {
                    NotificationService.showDefaultFailure();
                });
            }
        };

        function initDropzones() {
            $ctrl.attachmentConfig = {
                baseUri: '/api/v1/deercensus/attachment',
                canDownload: true,
                canDelete: true
            };

            $ctrl.whiteTailDeerDropzone = null;
            $ctrl.roeDeerDropzone = null;
            $ctrl.fallowDeerDropzone = null;

            $ctrl.whiteTailDeerDropzoneConfig = getDropzoneConfig('WHITE_TAIL_DEER');
            $ctrl.roeDeerDropzoneConfig = getDropzoneConfig('ROE_DEER');
            $ctrl.fallowDeerDropzoneConfig = getDropzoneConfig('FALLOW_DEER');

            $ctrl.whiteTailDeerDropzoneEventHandlers = {
                success: function (file, response, xhr) {
                    handleFileUploadSuccess(file, response, $ctrl.whiteTailDeerDropzone);
                },
                error: function (file, response, xhr) {
                    handleFileUploadError(file, $ctrl.whiteTailDeerDropzone);
                }
            };
            $ctrl.roeDeerDropzoneEventHandlers = {
                success: function (file, response, xhr) {
                    handleFileUploadSuccess(file, response, $ctrl.roeDeerDropzone);
                },
                error: function (file, response, xhr) {
                    handleFileUploadError(file, $ctrl.roeDeerDropzone);
                }
            };
            $ctrl.fallowDeerDropzoneEventHandlers = {
                success: function (file, response, xhr) {
                    handleFileUploadSuccess(file, response, $ctrl.fallowDeerDropzone);
                },
                error: function (file, response, xhr) {
                    handleFileUploadError(file, $ctrl.fallowDeerDropzone);
                }
            };
        }

        function getDropzoneConfig(attachmentType) {
            var attachmentBaseUri = $ctrl.deerCensus.id ?
                '/api/v1/deercensus/' + $ctrl.deerCensus.id + '/attachment' :
                '/api/v1/deercensus/attachment/tmp';

            return  {
                autoProcessQueue: true,
                maxFiles: 1,
                maxFilesize: 50, // MiB
                uploadMultiple: false,
                url: attachmentBaseUri,
                paramName: 'file',
                params: {
                    attachmentType: attachmentType,
                    "_csrf": $cookies.get('XSRF-TOKEN')
                }
            };
        }

        function handleFileUploadSuccess(file, response, dropzone) {
            dropzone.removeFile(file);
            updateAttachmentIdToDeerCensus(response.id);
            $ctrl.refreshAttachments();
        }
        function handleFileUploadError(file, dropzone) {
            dropzone.removeFile(file);
            NotificationService.showDefaultFailure();
        }
        function updateAttachmentIdToDeerCensus(attachmentId) {
            if (!$ctrl.deerCensus.id) {
                if (!$ctrl.deerCensus.attachmentIds) {
                    $ctrl.deerCensus.attachmentIds = [];
                }
                $ctrl.deerCensus.attachmentIds.push(attachmentId);
            }
        }
    });
