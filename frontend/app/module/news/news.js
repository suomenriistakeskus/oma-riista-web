'use strict';

angular.module('app.news', [])
    .controller('NewsListController', function ($uibModal, Helpers, NotificationService, News, slice) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.slice = slice;
        };

        $ctrl.add = function () {
            openModal({});
        };

        $ctrl.edit = function (news) {
            openModal(news);
        };

        $ctrl.remove = function (news) {
            $uibModal.open({
                templateUrl: 'news/remove.html',
                resolve: {
                    news: Helpers.wrapToFunction(news)
                },
                controller: 'NewsRemoveController',
                controllerAs: '$ctrl'
            }).result.then($ctrl.onSuccess, $ctrl.onFailure);
        };

        $ctrl.onSuccess = function () {
            $ctrl.loadPage($ctrl.slice.page);
        };

        $ctrl.onFailure = function (reason) {
            if (reason === 'error') {
                NotificationService.showDefaultFailure();
            }
        };

        $ctrl.loadPage = function (page) {
            News.list({page: page}).$promise
                .then(function (slice) {
                    $ctrl.slice = slice;
                });
        };

        function openModal (news) {
            $uibModal.open({
                templateUrl: 'news/form.html',
                resolve: {
                    news: Helpers.wrapToFunction(angular.copy(news))
                },
                controller: 'NewsFormController',
                controllerAs: '$ctrl',
                size: 'lg'
            }).result.then($ctrl.onSuccess, $ctrl.onFailure);
        }
    })
    .controller('NewsFormController', function ($uibModalInstance, News, news) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.news = news;
        };

        $ctrl.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };

        $ctrl.save = function () {
            var saveMethod = !$ctrl.news.id ? News.save : News.update;
            saveMethod(null, $ctrl.news).$promise
                .then(function () {
                    $uibModalInstance.close();
                }, function () {
                    $uibModalInstance.dismiss('error');
                });
        };

        $ctrl.isLinkValid = function (link) {
            if (!link || _.isEmpty(link)) {
                return true;
            }

            try {
                var url = new URL(link);
            } catch (e) {
                return false;
            }

            return true;
        };
    })
    .controller('NewsRemoveController', function ($uibModalInstance, News, news) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.news = news;
        };

        $ctrl.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };

        $ctrl.remove = function () {
            News.delete({id: $ctrl.news.id}).$promise
                .then(function () {
                    $uibModalInstance.close();
                }, function () {
                    $uibModalInstance.dismiss('error');
                });
        };
    })
    .factory('News', function ($resource) {
        var API_PREFIX = 'api/v1/news/:id';
        return $resource(API_PREFIX, {"id": "@id"}, {
            'list': {url: API_PREFIX + '/list', method: 'GET', isArray: false},
            'listLatest': {url: '/api/v1/anon/news/latest', method: 'GET', isArray: true},
            'update': {method: 'PUT'}
        });
    });
