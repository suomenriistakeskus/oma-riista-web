'use strict';

angular.module('app.common.species', ['ngResource'])

    .service('GameSpeciesCodes', function () {
        var BEAR = 47348;
        var FALLOW_DEER = 47484;
        var GREY_SEAL = 47282;
        var LYNX = 46615;
        var MOOSE = 47503;
        var ROE_DEER = 47507;
        var WHITE_TAILED_DEER = 47629;
        var WILD_BOAR = 47926;
        var WILD_FOREST_REINDEER = 200556;
        var WOLF = 46549;
        var WOLVERINE = 47212;

        _.assign(this, {
            "BEAR": BEAR,
            "FALLOW_DEER": FALLOW_DEER,
            "GREY_SEAL": GREY_SEAL,
            "LYNX": LYNX,
            "MOOSE": MOOSE,
            "ROE_DEER": ROE_DEER,
            "WHITE_TAILED_DEER": WHITE_TAILED_DEER,
            "WILD_BOAR": WILD_BOAR,
            "WILD_FOREST_REINDEER": WILD_FOREST_REINDEER,
            "WOLF": WOLF,
            "WOLVERINE": WOLVERINE
        });

        var deerCodes = [FALLOW_DEER, ROE_DEER, WHITE_TAILED_DEER, WILD_FOREST_REINDEER];
        var mooselikeCodes = [FALLOW_DEER, MOOSE, ROE_DEER, WHITE_TAILED_DEER, WILD_FOREST_REINDEER];
        var permitBasedDeerCodes = [FALLOW_DEER, WHITE_TAILED_DEER, WILD_FOREST_REINDEER];
        var permitBasedMooselikeCodes = [FALLOW_DEER, MOOSE, WHITE_TAILED_DEER, WILD_FOREST_REINDEER];

        this.isMoose = function (gameSpeciesCode) {
            return gameSpeciesCode === MOOSE;
        };

        this.isDeer = function (gameSpeciesCode) {
            return _.includes(deerCodes, gameSpeciesCode);
        };

        this.isMooselike = function (gameSpeciesCode) {
            return _.includes(mooselikeCodes, gameSpeciesCode);
        };

        this.isPermitBasedDeer = function (gameSpeciesCode) {
            return _.includes(permitBasedDeerCodes, gameSpeciesCode);
        };

        this.isPermitBasedMooselike = function (gameSpeciesCode) {
            return _.includes(permitBasedMooselikeCodes, gameSpeciesCode);
        };

        this.isWildBoar = function (gameSpeciesCode) {
            return gameSpeciesCode === WILD_BOAR;
        };

        this.isRoeDeer = function (gameSpeciesCode) {
            return gameSpeciesCode === ROE_DEER;
        };

        this.isGreySeal = function (gameSpeciesCode) {
            return gameSpeciesCode === GREY_SEAL;
        };
    })

    .service('MooselikeSpecies', function (GameDiaryParameters, GameSpeciesCodes) {
        this.getPermitBased = function () {
            return GameDiaryParameters.query().$promise.then(function (parameters) {
                return _.filter(parameters.species, function (species) {
                    return GameSpeciesCodes.isPermitBasedMooselike(species.code);
                });
            });
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
                return cat ? getTranslatedName(cat.name) : '';
            };
        };
    })

    .component('rSpeciesSelection', {
        templateUrl: 'common/species/select-species.html',
        bindings: {
            availableSpecies: '<',
            onSelectedSpeciesChanged: '&'
        },
        controller: function (SpeciesSortByName) {
            var $ctrl = this;
            $ctrl.selectedSpecies = null;

            $ctrl.onSpeciesChanged = function () {
                $ctrl.onSelectedSpeciesChanged({speciesCode: _.get($ctrl.selectedSpecies, 'code', null)});
            };

            $ctrl.$onChanges = function (changes) {
                if (!changes.availableSpecies) {
                    return;
                }
                $ctrl.availableSpecies = SpeciesSortByName.sort(changes.availableSpecies.currentValue);
                $ctrl.selectedSpecies = _($ctrl.availableSpecies).first();
                $ctrl.onSpeciesChanged();
            };
        }
    })
    .component('rHuntingYearAndSpeciesSelection', {
        templateUrl: 'common/species/select-species-and-year.html',
        bindings: {
            huntingYears: '<',
            preselectCurrentHuntingYear: '<',
            availableSpecies: '<',
            onHuntingYearOrSpeciesChanged: '&'
        },
        controller: function (HuntingYearService) {
            var $ctrl = this;
            $ctrl.selectedYear = null;
            $ctrl.selectedSpeciesCode = null;

            // Decorate with name parameter.
            $ctrl.$onInit = function () {
                $ctrl.decoratedYears = _($ctrl.huntingYears)
                    .map(function (param) {
                        var obj;

                        if (_.isObject(param)) {
                            obj = param;
                        } else if (_.isFinite(param)) {
                            obj = {year: param};
                        }

                        if (_.isFinite(obj.year)) {
                            obj.name = HuntingYearService.toObj(obj.year).name;
                        }

                        return obj;
                    })
                    .value();
                if ($ctrl.preselectCurrentHuntingYear) {
                    var huntingYear = HuntingYearService.getCurrent();
                    $ctrl.selectedYear = _.find($ctrl.decoratedYears, function (y) {
                        return y.year === huntingYear;
                    });
                } else {
                    $ctrl.selectedYear = _($ctrl.decoratedYears).last();
                }
            };

            function updateYearAndSpecies() {
                $ctrl.onHuntingYearOrSpeciesChanged({
                    huntingYear: _.get($ctrl.selectedYear, 'year', null),
                    speciesCode: $ctrl.selectedSpeciesCode
                });
            }

            $ctrl.onHuntingYearChanged = function () {
                updateYearAndSpecies();
            };

            $ctrl.selectSpeciesCode = function (speciesCode) {
                $ctrl.selectedSpeciesCode = speciesCode;
                updateYearAndSpecies();
            };
        }
    });
