'use strict';

angular.module('app.dashboard', [])
    .component('dashboard', {
        templateUrl: 'dashboard/dashboard.html',
        controller: function (ActiveRoleService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.selectedTab = null;
                $ctrl.shouldLoadFirstTab = false;
                $ctrl.isAdmin = ActiveRoleService.isAdmin();
            };

            $ctrl.select = function (i) {
                $ctrl.selectedTab = i;
            };

            $ctrl.loadFirstTab = function () {
                $ctrl.shouldLoadFirstTab = true;
            };
        }
    })
    .component('dashboardUsers', {
        templateUrl: 'dashboard/users.html',
        controller: function ($resource) {
            var $ctrl = this;
            $ctrl.$onInit = function () {
                $ctrl.metrics = $resource('/api/v1/dashboard/users').get();
            };
        }
    })
    .component('dashboardClubs', {
        templateUrl: 'dashboard/clubs.html',
        controller: function ($resource) {
            var $ctrl = this;
            $ctrl.$onInit = function () {
                $ctrl.metrics = $resource('/api/v1/dashboard/clubs').get();
            };
        }
    })
    .component('dashboardPdf', {
        templateUrl: 'dashboard/pdf.html',
        controller: function ($resource) {
            var $ctrl = this;
            $ctrl.$onInit = function () {
                $ctrl.metrics = $resource('/api/v1/dashboard/pdf').get();
            };
        }
    })
    .component('dashboardAnnouncement', {
        templateUrl: 'dashboard/announcement.html',
        controller: function ($resource) {
            var $ctrl = this;
            $ctrl.$onInit = function () {
                $ctrl.metrics = $resource('/api/v1/dashboard/announcement').get();
            };
        }
    })
    .component('dashboardShootingTest', {
        templateUrl: 'dashboard/shootingtest.html',
        controller: function ($resource) {
            var $ctrl = this;
            $ctrl.$onInit = function () {
                $ctrl.metrics = $resource('/api/v1/dashboard/shootingtest').get();
            };
        }
    })
    .component('dashboardMhPermits', {
        templateUrl: 'dashboard/mh-permits.html',
        controller: function ($resource, HuntingYearService) {
            var $ctrl = this;
            $ctrl.$onInit = function () {
                $ctrl.metrics = updateMetrics("");
                $ctrl.huntingYearFilter = "";
                $ctrl.availableHuntingYears = HuntingYearService.createHuntingYearChoices(2019, true);
            };

            $ctrl.onHuntingYearChange = function () {
                $ctrl.metrics = updateMetrics($ctrl.huntingYearFilter);
            };

            function updateMetrics(huntingYear) {
                return !huntingYear
                    ? $resource('/api/v1/moderator/mh/statistics').get()
                    : $resource('/api/v1/moderator/mh/statistics?huntingYear='+huntingYear).get();
            }
        }
    })
    .component('dashboardMobileLogin', {
        templateUrl: 'dashboard/mobile_login.html',
        controller: function ($http) {
            var $ctrl = this;
            $ctrl.$onInit = function () {
                $http.get('/api/v1/dashboard/mobile').then(function (response) {
                    $ctrl.metrics = response.data;

                    _.forEach($ctrl.metrics.platform, function (row) {
                        row.total = row.android + row.ios + row.wp;
                    });
                });
            };
        }
    })
    .component('dashboardMooselikeHunting', {
        templateUrl: 'dashboard/mooselike_hunting.html',
        controller: function ($resource) {
            var $ctrl = this;
            $ctrl.$onInit = function () {
                $ctrl.metrics = $resource('/api/v1/dashboard/moosehunting').get();
            };

        }
    })
    .component('dashboardHarvestObservations', {
        templateUrl: 'dashboard/harvests_observations.html',
        controller: function ($resource) {
            var $ctrl = this;
            $ctrl.$onInit = function () {
                $ctrl.metrics = $resource('/api/v1/dashboard/harvests_observations').get();
            };
        }

    })
    .component('dashboardSrva', {
        templateUrl: 'dashboard/srva.html',
        controller: function ($resource) {
            var $ctrl = this;
            $ctrl.$onInit = function () {
                $ctrl.metrics = $resource('/api/v1/dashboard/srva').get();
            };
        }
    })
    .component('dashboardBulkButton', {
        templateUrl: 'dashboard/bulk-button.html',
        controller: function ($uibModal) {
            var $ctrl = this;
            $ctrl.openEmailDialog = function () {
                $uibModal.open({
                    templateUrl: 'dashboard/bulk_send_email.html',
                    controller: 'AdminBulkEmailDialogController',
                    size: 'lg'
                });
            };
        }
    })
    .component('dashboardHarvestSummary', {
        templateUrl: 'dashboard/harvest_summary.html',
        controller: function (FormPostService, HuntingYearService, Helpers, GameDiaryParameters) {
            var $ctrl = this;

            GameDiaryParameters.query().$promise.then(function (parameters) {
                $ctrl.availableSpecies = parameters.species;
            });

            $ctrl.$onInit = function () {
                var huntingYear = HuntingYearService.getCurrent();
                $ctrl.begin = HuntingYearService.getBeginDateStr(huntingYear);
                $ctrl.end = HuntingYearService.getEndDateStr(huntingYear);
                $ctrl.harvestReportOnly = false;
                $ctrl.officialHarvestOnly = false;
            };

            $ctrl.selectSpeciesCode = function (gameSpeciesCode) {
                $ctrl.gameSpeciesCode = gameSpeciesCode;
            };

            $ctrl.exportExcel = function () {
                var organisationType = $ctrl.rhyCode ? 'RHY' : $ctrl.areaCode ? 'RKA' : 'RK';
                var officialCode = $ctrl.rhyCode || $ctrl.areaCode;

                FormPostService.submitFormUsingBlankTarget('api/v1/dashboard/harvestSummary', {
                    harvestReportOnly: $ctrl.harvestReportOnly,
                    officialHarvestOnly: $ctrl.officialHarvestOnly,
                    beginDate: Helpers.dateToString($ctrl.begin),
                    endDate: Helpers.dateToString($ctrl.end),
                    speciesCode: $ctrl.filterSpecies ? $ctrl.gameSpeciesCode : null,
                    organisationType: organisationType,
                    officialCode: officialCode
                });
            };
        }
    })
    .component('dashboardEndOfHuntingReports', {
        templateUrl: 'dashboard/end-of-hunting-reports.html',
        controllerAs: '$ctrl',
        controller: function (Species, GameSpeciesCodes, FetchAndSaveBlob, HuntingYearService) {
            var $ctrl = this;
            $ctrl.$onInit = function () {
                $ctrl.species = Species.getPermitBasedMooselike();
                $ctrl.huntingYearOptions = HuntingYearService.createHuntingYearChoices();
                $ctrl.selectedSpecies = _.find($ctrl.species, function (s) {
                    return GameSpeciesCodes.isMoose(s.code);
                });
                $ctrl.selectedHuntingYear = _.last($ctrl.huntingYearOptions).year;
            };

            $ctrl.exportEndOfHuntingReports = function () {
                FetchAndSaveBlob.post('api/v1/dashboard/mooselike/endofhunting/excel/' + $ctrl.selectedHuntingYear + '/' + $ctrl.selectedSpecies.code);
            };
        }
    })
    .controller('AdminBulkEmailDialogController',
        function ($scope, $uibModalInstance, $http, NotificationService, TranslatedBlockUI) {
            var _generateConfirmationCode = function () {
                return (new Date()).getTime().toString().slice(-5);
            };

            var _resetConfirmationCode = function () {
                $scope.message.confirmation = _generateConfirmationCode();
                $scope.message.userConfirmation = null;
            };

            $scope.message = {
                // Random confirmation code ...
                confirmation: _generateConfirmationCode(),
                // ... repeated by the user to verify intention.
                userConfirmation: null,
                body: null,
                subject: null
            };

            _resetConfirmationCode();

            $scope.confirmationMatches = function () {
                return $scope.message.confirmation === $scope.message.userConfirmation;
            };

            $scope.SEND_TO = {test: 'test', all: 'all', clubContacts: 'clubContacts'};

            $scope.viewState = {
                sendTo: $scope.SEND_TO.test,
                testRecipient: 'oma@riista.fi'
            };

            $scope.sendEmail = function () {
                if ($scope.viewState.sendTo !== $scope.SEND_TO.test) {
                    var url;
                    if ($scope.viewState.sendTo === $scope.SEND_TO.all) {
                        url = '/api/v1/bulkMail/sendBulkMail';
                    } else if ($scope.viewState.sendTo === $scope.SEND_TO.clubContacts) {
                        url = '/api/v1/bulkMail/sendBulkMailToClubContacts';
                    } else {
                        throw 'Invalid value for viewState.sendTo:' + $scope.viewState.sendTo;
                    }
                    // sending the real bulk mail, close dialog and send
                    $uibModalInstance.close();
                    return _send(url, $scope.message);
                }
            };

            $scope.sendTestEmail = function () {
                if ($scope.viewState.sendTo === $scope.SEND_TO.test) {
                    // sending the test mail, keep dialog open and send
                    var testData = angular.copy($scope.message);
                    testData.testRecipient = $scope.viewState.testRecipient;
                    return _send('/api/v1/bulkMail/sendTestBulkMail/', testData).then(_resetConfirmationCode);
                }
            };

            function _send(url, data) {
                TranslatedBlockUI.start("global.block.wait");

                return $http({
                    url: url,
                    method: 'POST',
                    data: data
                }).then(function () {
                    NotificationService.showMessage("Viestien lähetys tehty.", "success");
                }, function () {
                    NotificationService.showMessage("Viestien lähetys epäonnistui", "error");
                }).finally(function () {
                    TranslatedBlockUI.stop();
                });
            }
        })
    .component('dashboardHarvestReports', {
        templateUrl: 'dashboard/harvestreports.html',
        controller: function ($http) {
            var $ctrl = this;
            $ctrl.loading = false;
            $ctrl.formDates = {};
            $ctrl.postParams = {harvest: null, rhy: null};

            var createPostParams = function () {
                var params = {};
                if ($ctrl.formDates.begin) {
                    params.begin = moment($ctrl.formDates.begin).format('YYYY-MM-DD');
                }
                if ($ctrl.formDates.end) {
                    params.end = moment($ctrl.formDates.end).format('YYYY-MM-DD');
                }
                $ctrl.postParams = params;
                return params;
            };

            $ctrl.updateResolvedHarvestReportMetrics = function () {
                $ctrl.loading = true;
                $http.get('/api/v1/dashboard/harvestreport', {params: createPostParams()})
                    .then(function (result) {
                        $ctrl.resolvedHarvestReportMetrics = result.data;
                        $ctrl.loading = false;
                    });
            };
        }
    })
    .component('dashboardRhy', {
        templateUrl: 'dashboard/rhy.html',
        controller: function ($http) {
            var $ctrl = this;
            $ctrl.loading = false;
            $ctrl.formDates = {};
            $ctrl.postParams = {};

            var createPostParams = function () {
                var params = {};
                if ($ctrl.formDates.begin) {
                    params.begin = moment($ctrl.formDates.begin).format('YYYY-MM-DD');
                }
                if ($ctrl.formDates.end) {
                    params.end = moment($ctrl.formDates.end).format('YYYY-MM-DD');
                }
                $ctrl.postParams = params;
                return params;
            };

            $ctrl.updateResolvedRhyEditMetrics = function () {
                $ctrl.loading = true;
                $http.get('/api/v1/dashboard/rhyedit', {params: createPostParams()})
                    .then(function (result) {
                        $ctrl.resolvedRhyEditMetrics = result.data;
                        $ctrl.loading = false;
                    });
            };
        }
    })
    .component('dashboardEvents', {
        templateUrl: 'dashboard/events.html',
        controllerAs: '$ctrl',
        controller: function (EventTypes, FetchAndSaveBlob) {
            var $ctrl = this;
            $ctrl.$onInit = function () {
                var currentYear = new Date().getFullYear();
                // Show also the next year for event search
                var endYear = currentYear + 1;
                var beginYear = currentYear - 5;
                $ctrl.availableYears = _.range(beginYear, endYear + 1);
                $ctrl.selectedYear = currentYear;

                EventTypes.then(function (result) {
                    $ctrl.eventTypes = result.data;
                });
            };

            $ctrl.exportToExcel = function () {
                FetchAndSaveBlob.post('api/v1/dashboard/events', {
                    rkaCode: $ctrl.areaCode,
                    rhyCode: $ctrl.rhyCode,
                    year: $ctrl.selectedYear,
                    eventType: $ctrl.selectedEventType
                });
            };
        }
    })
    .component('dashboardCarnivorePublicPdfDownloads', {
        templateUrl: 'dashboard/carnivore-pdf-downloads.html',
        controller: function ($resource) {
            var $ctrl = this;
            $ctrl.$onInit = function () {
                $ctrl.data = $resource('/api/v1/dashboard/carnivore/downloads').get();
            };
        }

    });
