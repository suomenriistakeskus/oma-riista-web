'use strict';

angular.module('app.main.controllers', [])
    .config(function ($stateProvider) {
            $stateProvider
                .state('main', {
                    url: '/',
                    controller: 'MainController'
                })
                .state('roleselection', {
                    url: '/roleselection',
                    templateUrl: 'main/roleselection.html',
                    controller: 'RoleSelectionController',
                    controllerAs: '$ctrl',
                    params: {lang: {value: null}},
                    bindToController: true
                })
                .state('about', {
                    authenticate: false,
                    templateUrl: 'main/about/about.html',
                    url: '/about?{lang}',
                    params: {lang: {value: null}},
                    controller: 'ScrollToTop'
                })
                .state('terms_and_conditions', {
                    authenticate: false,
                    templateUrl: 'main/about/terms_and_conditions.html',
                    url: '/terms_and_conditions?{lang}',
                    params: {lang: {value: null}},
                    controller: 'ScrollToTop'
                })
                .state('privacy_policy', {
                    authenticate: false,
                    templateUrl: 'main/about/privacy_policy.html',
                    url: '/privacy_policy?{lang}',
                    params: {lang: {value: null}},
                    controller: 'ScrollToTop'
                })
                .state('technical_requirements', {
                    authenticate: false,
                    templateUrl: 'main/about/technical_requirements.html',
                    url: '/technical_requirements?{lang}',
                    params: {lang: {value: null}},
                    controller: 'ScrollToTop'
                })
                .state('contact_information', {
                    authenticate: false,
                    templateUrl: 'main/about/contact_information.html',
                    url: '/contact_information?{lang}',
                    params: {lang: {value: null}},
                    controller: 'ScrollToTop'
                })
            ;
        }
    )

    .controller('MainController',
        function ($scope, ActiveRoleService, $state) {
            $scope.selectedRole = ActiveRoleService.getActiveRole();

            if ($scope.selectedRole) {
                if ($scope.selectedRole.type === 'ROLE_ADMIN') {
                    $state.go('admin.home');
                } else if ($scope.selectedRole.type === 'ROLE_MODERATOR') {
                    $state.go('reporting.home');
                } else if ($scope.selectedRole.type === 'SRVA_YHTEYSHENKILO') {
                    $state.go('srva.list', {id: $scope.selectedRole.context.rhyId});
                } else if ($scope.selectedRole.type === 'TOIMINNANOHJAAJA') {
                    $state.go('rhy.show', {id: $scope.selectedRole.context.rhyId});
                } else if ($scope.selectedRole.context.clubId) {
                    $state.go('club.main', {id: $scope.selectedRole.context.clubId});
                } else {
                    $state.go('profile.diary', {id: 'me'});
                }
            }
        })

    .controller('RoleSelectionController', function ($state, ActiveRoleService, MapState) {
        var $ctrl = this;

        $ctrl.availableRoles = ActiveRoleService.getAvailableRoles();
        $ctrl.getRoleDisplayName = ActiveRoleService.getRoleDisplayName;

        // Update selected role in view on change
        $ctrl.selectRole = function (role) {
            ActiveRoleService.selectActiveRole(role);
            MapState.reset();

            $state.go('main');
        };
    })

    .controller('ScrollToTop', function ($location, $translate, $anchorScroll, $stateParams) {
        var lang = $stateParams.lang;
        if (lang === 'fi' || lang === 'sv' || lang === 'en') {
            $translate.use(lang);
        }
        $location.hash('scrollToTop');
        $anchorScroll();
    });

