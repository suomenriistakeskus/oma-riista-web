'use strict';

angular.module('app.admin.controllers', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('admin', {
                abstract: true,
                templateUrl: 'admin/layout.html',
                url: '/admin'
            })
            .state('admin.home', {
                url: '/home',
                templateUrl: 'admin/dashboard.html',
                controller: 'AdminDashboardController',
                resolve: {
                    'resolvedMetrics': function ($resource) {
                        return $resource('/api/v1/admin/metrics').get();
                    }
                }
            });
    })

    .controller('AdminBulkEmailDialogController',
        function ($scope, $uibModalInstance, $http, NotificationService) {
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
                        url = '/api/v1/admin/sendBulkMail';
                    } else if ($scope.viewState.sendTo === $scope.SEND_TO.clubContacts) {
                        url = '/api/v1/admin/sendBulkMailToClubContacts';
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
                    return _send('/api/v1/admin/sendTestBulkMail/', testData).then(_resetConfirmationCode);
                }
            };

            function _send(url, data) {
                return $http({
                    url: url,
                    method: 'POST',
                    data: data
                }).then(function (response) {
                    NotificationService.showMessage("Viestien lähetys tehty. " +
                        "Onnistuneita " + response.data.successCount + " kpl, " +
                        "epäonnistuneita " + response.data.errorCount + " kpl", "success");
                }, function () {
                    NotificationService.showMessage("Viestien lähetys epäonnistui", "error");
                });
            }
        })

    .controller('AdminDashboardController',
        function ($scope, $uibModal, resolvedMetrics, $http) {
            $scope.metrics = resolvedMetrics;
            $scope.formDates = {harvest: {}, rhy: {}};
            $scope.postParams = {harvest: null, rhy: null};

            var createPostParams = function (key) {
                var dates = $scope.formDates[key];
                var params = {};
                if (dates.begin) {
                    params.begin = moment(dates.begin).format('YYYY-MM-DD');
                }
                if (dates.end) {
                    params.end = moment(dates.end).format('YYYY-MM-DD');
                }
                $scope.postParams[key] = params;
                return params;
            };

            $scope.updateResolvedHarvestReportMetrics = function () {
                $scope.reportsInProgress = true;
                $http.get('/api/v1/admin/harvestreportmetrics', {params: createPostParams('harvest')})
                    .then(function (result) {
                        $scope.resolvedHarvestReportMetrics = result.data;
                        delete $scope.reportsInProgress;
                    });
            };

            $scope.updateResolvedRhyEditMetrics = function () {
                $scope.rhyInProgress = true;
                $http.get('/api/v1/admin/rhyeditmetrics', {params: createPostParams('rhy')})
                    .then(function (result) {
                        $scope.resolvedRhyEditMetrics = result.data;
                        delete $scope.rhyInProgress;
                    });
            };

            $scope.openEmailDialog = function () {
                $uibModal.open({
                    templateUrl: 'admin/send_email.html',
                    controller: 'AdminBulkEmailDialogController',
                    size: 'lg'
                });
            };
        });
