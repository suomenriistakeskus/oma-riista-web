'use strict';

angular.module('app.jht.natura', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.natura', {
            url: '/natura',
            templateUrl: 'harvestpermit/decision/natura/natura.html',
            resolve: {
                decision: _.constant(null),
                applicationId: _.constant('jhtNatura'),
                applicationLocations: _.constant([])
            },
            controllerAs: '$ctrl',
            controller: 'DecisionNaturaController'
        });
    });
