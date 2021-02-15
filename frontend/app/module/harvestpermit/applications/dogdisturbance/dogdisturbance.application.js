'use strict';

angular.module('app.harvestpermit.application.dogdisturbance.application', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.dogdisturbance', {
                url: '/dogdisturbance',
                templateUrl: 'harvestpermit/applications/dogdisturbance/layout.html',
                abstract: true,
                controllerAs: '$ctrl',
                controller: function (applicationBasicDetails) {
                    this.applicationBasicDetails = applicationBasicDetails;
                },
                resolve: {
                    wizard: function ($state, applicationId, applicationBasicDetails) {
                        return {
                            isAmending: _.constant(applicationBasicDetails.status === 'AMENDING'),
                            reload: $state.reload,
                            exit: function () {
                                $state.go('profile.permits');
                            },
                            goto: function (childState, params) {
                                $state.go('profile.permitwizard.dogdisturbance.' + childState,
                                          _.extend({applicationId: applicationId}, params));
                            }
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.dogdisturbance', {
                url: '/dogdisturbance',
                templateUrl: 'harvestpermit/applications/dogdisturbance/layout.html',
                abstract: true,
                controllerAs: '$ctrl',
                controller: function (applicationBasicDetails) {
                    this.applicationBasicDetails = applicationBasicDetails;
                },
                resolve: {
                    wizard: function ($state, applicationId, applicationBasicDetails) {
                        return {
                            isAmending: _.constant(applicationBasicDetails.status === 'AMENDING'),
                            reload: $state.reload,
                            exit: function () {
                                $state.go('jht.decision.application.overview');
                            },
                            goto: function (childState, params) {
                                $state.go('jht.decision.application.wizard.dogdisturbance.' + childState,
                                          _.extend({applicationId: applicationId}, params));
                            }
                        };
                    }
                }
            });
    })
    .factory('DogDisturbanceApplication', function ($resource) {
        var apiPrefix = 'api/v1/harvestpermit/application/dogevent/:id';

        function getMethod(suffix) {
            return {method: 'GET', url: apiPrefix + suffix};
        }

        function postMethod(suffix) {
            return {method: 'POST', url: apiPrefix + suffix};
        }

        return $resource(apiPrefix, {id: '@id', eventId: '@eventId'}, {
            // Permit holder
            getCurrentpermitHolder: getMethod('/permit-holder'),
            updatePermitHolder: postMethod('/permit-holder'),

            // Dog disturbance event details
            getEventDetails: getMethod( '/disturbance'),
            updateEventDetails: postMethod( '/disturbance'),

            getFullDetails: getMethod('/disturbance/full')

        });
    })
    .component('rShowDates', {
        template: ' <span>{{$ctrl.beginDate|date:"d.M.yyyy"}}' +
            '<span ng-hide="!$ctrl.endDate || $ctrl.beginDate === $ctrl.endDate">' +
            ' - {{$ctrl.endDate|date:"d.M.yyyy"}}' +
            '</span></span>',
        bindings: {
            beginDate: '<',
            endDate:'<'
        }
    });
