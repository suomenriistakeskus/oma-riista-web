(function () {
    'use strict';

    angular.module('app.account.announcements', [])
        .config(function ($stateProvider) {
            $stateProvider.state('profile.messages', {
                url: '/account/messages',
                template: '<account-announcements-list/>',
                controllerAs: '$ctrl'
            });
        })

        .factory('AccountAnnouncements', function ($resource) {
            return $resource('api/v1/account/announcements', {}, {
                'query': {
                    isArray: false
                }
            });
        })

        .component('accountAnnouncementsList', {
            templateUrl: 'account/announcements/list.html',
            controller: function (AccountAnnouncements) {
                var $ctrl = this;

                $ctrl.loadPage = function (page) {
                    AccountAnnouncements.query({
                        sort: 'id,desc',
                        page: page,
                        size: 10
                    }).$promise.then(function (result) {
                        $ctrl.announcements = result;
                    });
                };
            }
        });
})();
