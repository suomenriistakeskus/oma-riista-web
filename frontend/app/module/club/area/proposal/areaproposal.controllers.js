(function () {
    "use strict";

    angular.module('app.clubarea.proposal', [])
        .controller('AreaProposalListController', AreaProposalListController)
        .config(configureState);

    function configureState($stateProvider) {
        $stateProvider.state('club.area.proposals', {
            url: '/areaproposals?{areaProposalId:[0-9]{1,8}}',
            templateUrl: 'club/area/proposal/area-proposal-list.html',
            controller: 'AreaProposalListController',
            controllerAs: '$ctrl',
            bindToController: true,
            wideLayout: true,
            reloadOnSearch: false,
            params: {
                areaProposalId: {
                    value: null
                }
            },
            resolve: {
                areas: function (clubId) {
                    return [];
                }
            }
        });
    }

    function AreaProposalListController($scope, $location, club, rhyBounds, areas) {
        var $ctrl = this;

        $ctrl.club = club;
        $ctrl.rhyBounds = rhyBounds;
        $ctrl.areas = areas;
        $ctrl.featureCollection = null;
    }

})();
