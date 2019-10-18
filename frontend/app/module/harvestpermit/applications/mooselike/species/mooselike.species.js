'use strict';

angular.module('app.harvestpermit.application.mooselike.species', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.mooselike.species', {
                url: '/species',
                templateUrl: 'harvestpermit/applications/mooselike/species/species.html',
                controller: 'MooselikePermitWizardSpeciesController',
                controllerAs: '$ctrl',
                resolve: {
                    gameDiaryParameters: function (GameDiaryParameters) {
                        return GameDiaryParameters.query().$promise;
                    },
                    speciesAmounts: function (MooselikePermitApplication, applicationId) {
                        return MooselikePermitApplication.listSpeciesAmounts({id: applicationId}).$promise;
                    }
                }
            })

            .state('jht.decision.application.wizard.mooselike.species', {
                url: '/species',
                templateUrl: 'harvestpermit/applications/mooselike/species/species.html',
                controller: 'MooselikePermitWizardSpeciesController',
                controllerAs: '$ctrl',
                resolve: {
                    gameDiaryParameters: function (GameDiaryParameters) {
                        return GameDiaryParameters.query().$promise;
                    },
                    speciesAmounts: function (MooselikePermitApplication, applicationId) {
                        return MooselikePermitApplication.listSpeciesAmounts({id: applicationId}).$promise;
                    }
                }
            });
    })
    .controller('MooselikePermitWizardSpeciesController', function (GameSpeciesCodes, MooselikePermitApplication,
                                                                    wizard, applicationId, gameDiaryParameters, speciesAmounts) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.speciesAmounts = [
                getSpeciesAmount(GameSpeciesCodes.MOOSE),
                getSpeciesAmount(GameSpeciesCodes.WHITE_TAILED_DEER),
                getSpeciesAmount(GameSpeciesCodes.FALLOW_DEER),
                getSpeciesAmount(GameSpeciesCodes.WILD_FOREST_REINDEER)
            ];
        };

        function getSpeciesAmount(gameSpeciesCode) {
            var spa = _.find(speciesAmounts, {gameSpeciesCode: gameSpeciesCode});

            return {
                enabled: !!spa,
                name: gameDiaryParameters.$getGameName(gameSpeciesCode, null),
                gameSpeciesCode: gameSpeciesCode,
                amount: spa ? spa.amount : 0,
                description: spa ? spa.description : ''
            };
        }

        $ctrl.speciesSelected = function () {
            return _.some($ctrl.speciesAmounts, function (spa) {
                return spa.enabled === true && spa.amount > 0;
            });
        };

        $ctrl.exit = wizard.exit;

        $ctrl.previous = function () {
            wizard.goto('applicant');
        };

        $ctrl.next = function () {
            var data = _.chain($ctrl.speciesAmounts).filter('enabled').map(function (spa) {
                return {
                    gameSpeciesCode: spa.gameSpeciesCode,
                    amount: spa.amount,
                    description: spa.description
                };
            }).value();

            MooselikePermitApplication.saveSpeciesAmounts({id: applicationId}, {list: data}).$promise.then(function () {
                wizard.goto('partners');
            });
        };
    });
