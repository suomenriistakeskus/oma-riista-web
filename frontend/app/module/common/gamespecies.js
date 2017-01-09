'use strict';

angular.module('app.gamespecies', ['ngResource'])

    .service('GameSpeciesCodes', function () {
        var FALLOW_DEER = 47484;
        var MOOSE = 47503;
        var ROE_DEER = 47507;
        var WHITE_TAILED_DEER = 47629;
        var WILD_FOREST_REINDEER = 200556;
        var WILD_BOAR = 47926;

        _.assign(this, {
            "FALLOW_DEER": FALLOW_DEER,
            "MOOSE": MOOSE,
            "ROE_DEER": ROE_DEER,
            "WHITE_TAILED_DEER": WHITE_TAILED_DEER,
            "WILD_FOREST_REINDEER": WILD_FOREST_REINDEER,
            "WILD_BOAR": WILD_BOAR
        });

        var deerCodes = [FALLOW_DEER, ROE_DEER, WHITE_TAILED_DEER, WILD_FOREST_REINDEER];
        var permitBasedDeerCodes = [FALLOW_DEER, WHITE_TAILED_DEER, WILD_FOREST_REINDEER];
        var permitBasedMooselikeCodes = [FALLOW_DEER, MOOSE, WHITE_TAILED_DEER, WILD_FOREST_REINDEER];

        this.isMoose = function (gameSpeciesCode) {
            return gameSpeciesCode === MOOSE;
        };

        this.isDeer = function (gameSpeciesCode) {
            return _.includes(deerCodes, gameSpeciesCode);
        };

        this.isPermitBasedDeer = function (gameSpeciesCode) {
            return _.includes(permitBasedDeerCodes, gameSpeciesCode);
        };

        this.isPermitBasedMooselike = function (gameSpeciesCode) {
            return _.includes(permitBasedMooselikeCodes, gameSpeciesCode);
        };
    })

    .service('SpeciesNameService', function ($filter) {
        var getTranslatedName = $filter('rI18nNameFilter');

        this.addSpeciesNameFunctions = function (obj) {

            var getGameName = function (code, species) {
                if (!code && code !== 0) {
                    return null;
                }

                return _.chain(obj.species)
                    .filter('code', code)
                    .map(function (s) {
                        var translated = getTranslatedName(s.name);

                        // Side-effect
                        if (species) {
                            species.translatedName = translated;
                        }

                        return translated;
                    })
                    .first()
                    .value();
            };
            obj.$getGameName = getGameName;

            obj.$getGameNameWithAmount = function (entry) {
                if (!entry) {
                    return null;
                }

                var gameName = getGameName(entry.gameSpeciesCode);

                if (angular.isNumber(entry.totalSpecimenAmount) && entry.totalSpecimenAmount > 1) {
                    return gameName + ' (' + entry.totalSpecimenAmount + ')';
                }

                return gameName;
            };

            obj.$getCategoryName = function (categoryId) {
                var cat = categoryId ? _.find(obj.categories, 'code', categoryId) : null;
                return cat ? getTranslatedName(cat.name) : null;
            };
        };
    })

    .factory('GameDiaryParameters', function ($resource, CacheFactory, SpeciesNameService) {

        var addFunctions = function (parameters) {

            parameters.isMultipleSpecimensAllowedForHarvestSpecies = function (gameSpeciesCode) {
                var selectedSpecies;

                if (gameSpeciesCode) {
                    selectedSpecies = _.find(parameters.species, function (species) {
                        return species.code === gameSpeciesCode;
                    });
                }

                return selectedSpecies && selectedSpecies.multipleSpecimenAllowedOnHarvests;
            };
        };

        return $resource('api/v1/gamediary/parameters', null, {
            query: {
                method: 'GET',
                isArray: false,
                cache: CacheFactory.get('diaryParameterCache'),
                transformResponse: function (data, headers, status) {
                    var parameters = angular.fromJson(data);

                    if (status >= 400) {
                        return parameters;
                    }

                    SpeciesNameService.addSpeciesNameFunctions(parameters);
                    addFunctions(parameters);
                    return parameters;
                }
            },
            getObservationSpeciesWithinMooseHunting: {
                url: 'api/v1/gamediary/species/withinMooseHunting',
                method: 'GET',
                isArray: true
            }
        });
    })

    .service('MooselikeSpecies', function (GameDiaryParameters, GameSpeciesCodes) {
        this.getPermitBased = function () {
            return GameDiaryParameters.query().$promise.then(function (parameters) {
                return _.filter(parameters.species, function (species) {
                    return GameSpeciesCodes.isPermitBasedMooselike(species.code);
                });
            });
        };
    });
