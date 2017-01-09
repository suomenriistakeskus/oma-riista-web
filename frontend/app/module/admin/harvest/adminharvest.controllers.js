'use strict';

angular.module('app.adminharvest.controllers', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('admin.harvest_base', {
                abstract: true,
                url: '',
                template: '<ui-view autoscroll="false"/>'
            })
            .state('admin.harvest', {
                parent: 'admin.harvest_base',
                url: '/harvest',
                templateUrl: 'admin/harvest/main.html',
                controller: 'HarvestAdminController',
                resolve: {
                    admin: function (HarvestAdmin) {
                        return HarvestAdmin.query().$promise;
                    }

                }
            });
    })
    .controller('HarvestAdminController',
        function ($scope, $uibModal, admin, TranslatedBlockUI, Helpers) {
            $scope.admin = admin;

            $scope.onUpload = function (files) {
                TranslatedBlockUI.start("global.block.wait");
            };

            $scope.onComplete = function (response) {
                TranslatedBlockUI.stop();
                $uibModal.open({
                    templateUrl: 'admin/harvest/permitimportresult.html',
                    resolve: {
                        importResult: Helpers.wrapToFunction(response)
                    },
                    controller: 'PermitImportResultController'
                });
            };

        })

    .controller('PermitImportResultController',
        function ($scope, $uibModalInstance, importResult) {
            var ok = importResult.status === 200;
            if (ok) {
                var data = importResult.data;
                $scope.modifiedOrAddedCount = data.modifiedOrAddedCount;
                $scope.errors = data.allErrors;
            }
            $scope.ok = ok;
        });
