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
                template: '<dashboard></dashboard>'
            });
    })
;
