'use strict';

angular.module('app.common.species', ['ngResource'])
    .service('Species', function (GameSpeciesCodes) {
        var speciesList = [
            cs('FOWL', 26287, true, null, 'metsähanhi', 'sädgås', 'bean goose'),
            cs('FOWL', 26291, true, null, 'merihanhi', 'grågås', 'greylag goose'),
            cs('FOWL', 26298, true, null, 'kanadanhanhi', 'kanadagås', 'canada goose'),
            cs('FOWL', 26360, true, null, 'haapana', 'bläsand', 'wigeon'),
            cs('FOWL', 26366, true, null, 'tavi', 'kricka', 'teal'),
            cs('FOWL', 26373, true, null, 'heinäsorsa', 'gräsand', 'mallard'),
            cs('FOWL', 26382, true, null, 'jouhisorsa', 'stjärtand', 'pintail'),
            cs('FOWL', 26388, true, null, 'heinätavi', 'årta', 'garganey'),
            cs('FOWL', 26394, true, null, 'lapasorsa', 'skedand', 'shoveler'),
            cs('FOWL', 26407, true, null, 'punasotka', 'brunand', 'pochard'),
            cs('FOWL', 26415, true, null, 'tukkasotka', 'vigg', 'tufted duck'),
            cs('FOWL', 26419, true, null, 'haahka', 'ejder', 'common eider'),
            cs('FOWL', 26427, true, null, 'alli', 'alfågel', 'long-tailed duck'),
            cs('FOWL', 26435, true, null, 'telkkä', 'knipa', 'goldeneye'),
            cs('FOWL', 26440, true, null, 'tukkakoskelo', 'småskrake', 'red-breasted merganser'),
            cs('FOWL', 26442, true, null, 'isokoskelo', 'storskrake', 'goosander'),
            cs('FOWL', 26921, true, null, 'riekko', 'dalripa', 'willow grouse'),
            cs('FOWL', 26922, true, null, 'kiiruna', 'fjällripa', 'ptarmigan'),
            cs('FOWL', 26926, true, null, 'teeri', 'orre', 'black grouse'),
            cs('FOWL', 26928, true, null, 'metso', 'tjäder', 'capercaillie'),
            cs('FOWL', 26931, true, null, 'pyy', 'järpe', 'hazel grouse'),
            cs('FOWL', 27048, true, null, 'peltopyy', 'rapphöna', 'partridge'),
            cs('FOWL', 27152, true, null, 'fasaani', 'fasan', 'pheasant'),
            cs('FOWL', 27381, true, null, 'nokikana', 'sothöna', 'coot'),
            cs('FOWL', 27649, true, null, 'lehtokurppa', 'morkulla', 'woodcock'),
            cs('FOWL', 27911, true, null, 'sepelkyyhky', 'ringduva', 'wood pigeon'),
            cs('UNPROTECTED', 27750, true, null, 'harmaalokki', 'gråtrut', 'herring gull'),
            cs('UNPROTECTED', 27759, true, null, 'merilokki', 'havstrut', 'sea gull'),
            cs('UNPROTECTED', 33117, true, null, 'räkättirastas', 'björktrast', 'field fare'),
            cs('UNPROTECTED', 37122, true, null, 'harakka', 'skata', 'magpie'),
            cs('UNPROTECTED', 37142, true, null, 'naakka', 'kaja', 'eurasian jackdaw'),
            cs('UNPROTECTED', 37166, true, null, 'varis', 'kråka', 'crow'),
            cs('UNPROTECTED', 37178, true, null, 'korppi', 'korp', 'raven'),
            cs('GAME_MAMMAL', 46542, false, null, 'tarhattu naali', 'I farm uppfödd fjällräv', 'blue fox'),
            cs('GAME_MAMMAL', 46549, false, 9, 'susi', 'varg', 'wolf'),
            cs('GAME_MAMMAL', 46564, true, null, 'supikoira', 'mårdhund', 'raccoon dog'),
            cs('GAME_MAMMAL', 46587, false, null, 'kettu', 'rödräv', 'red fox'),
            cs('GAME_MAMMAL', 46615, false, 7, 'ilves', 'lodjur', 'lynx'),
            cs('GAME_MAMMAL', 47169, false, null, 'saukko', 'utter', 'otter'),
            cs('GAME_MAMMAL', 47180, true, null, 'mäyrä', 'grävling', 'badger'),
            cs('GAME_MAMMAL', 47212, false, 10, 'ahma', 'järv', 'wolverine'),
            cs('GAME_MAMMAL', 47223, false, null, 'näätä', 'mård', 'pine marten'),
            cs('GAME_MAMMAL', 47230, false, null, 'kärppä', 'hermelin', 'ermine'),
            cs('GAME_MAMMAL', 47240, false, null, 'hilleri', 'iller', 'polecat'),
            cs('GAME_MAMMAL', 47243, false, null, 'minkki', 'mink', 'american mink'),
            cs('GAME_MAMMAL', 47282, false, null, 'halli', 'gråsäl', 'grey seal'),
            cs('GAME_MAMMAL', 47305, false, null, 'kirjohylje', 'knubbsäl', 'harbour seal'),
            cs('GAME_MAMMAL', 47329, false, null, 'pesukarhu', 'tvättbjörn', 'raccoon'),
            cs('GAME_MAMMAL', 47348, false, 8, 'karhu', 'björn', 'brown bear'),
            cs('GAME_MAMMAL', 47476, false, null, 'saksanhirvi', 'kronhjort', 'red deer'),
            cs('GAME_MAMMAL', 47479, false, null, 'japaninhirvi', 'sikahjort', 'sika deer'),
            cs('GAME_MAMMAL', 47484, false, 5, 'kuusipeura', 'dovhjort', 'fallow deer'),
            cs('GAME_MAMMAL', 47503, false, 1, 'hirvi', 'älg', 'moose'),
            cs('GAME_MAMMAL', 47507, false, 3, 'metsäkauris', 'rådjur', 'roe deer'),
            cs('GAME_MAMMAL', 47629, false, 2, 'valkohäntäpeura', 'vitsvanshjort', 'white-tailed deer'),
            cs('GAME_MAMMAL', 47774, false, null, 'mufloni', 'mufflon', 'mufflon'),
            cs('GAME_MAMMAL', 47926, false, 6, 'villisika', 'vildsvin', 'wild boar'),
            cs('GAME_MAMMAL', 48089, false, null, 'orava', 'ekorre', 'red squirrel'),
            cs('GAME_MAMMAL', 48250, false, null, 'kanadanmajava', 'amerikansk bäver', 'canadian beaver'),
            cs('GAME_MAMMAL', 48251, false, null, 'euroopanmajava', 'bäver', 'european beaver'),
            cs('GAME_MAMMAL', 48537, false, null, 'piisami', 'bisam', 'muskrat'),
            cs('GAME_MAMMAL', 50106, false, null, 'metsäjänis', 'skogshare', 'mountain hare'),
            cs('GAME_MAMMAL', 50114, true, null, 'villikani', 'vildkanin', 'rabbit'),
            cs('GAME_MAMMAL', 50336, false, null, 'rämemajava', 'sumpbäver', 'nutria'),
            cs('GAME_MAMMAL', 50386, false, null, 'rusakko', 'fälthare', 'brown hare'),
            cs('UNPROTECTED', 53004, false, null, 'villiintynyt kissa', 'förvildad katt', 'wild cat'),
            cs('UNPROTECTED', 200535, true, null, 'kesykyyhky', 'tamduva', 'feral pigeon'),
            cs('GAME_MAMMAL', 200555, false, null, 'itämerennorppa', 'östersjövikare', 'ringed seal'),
            cs('GAME_MAMMAL', 200556, false, 4, 'metsäpeura', 'skogsren', 'wild forest reindeer')
        ];

        var speciesMapping = _.keyBy(speciesList, 'code');

        this.getSpeciesMapping = function () {
            return speciesMapping;
        };

        this.getPermitBasedMooselike = function () {
            return _.filter(speciesList, function (v) {
                return GameSpeciesCodes.isPermitBasedMooselike(v.code);
            });
        };

        this.getBirdPermitSpecies = function () {
            return _.filter(speciesList, function (v) {
                return v.code !== 53004 && (v.category === 'FOWL' || v.category === 'UNPROTECTED');
            });
        };

        function cs(category, code, multipleSpecimenAllowedOnHarvest, srvaOrdinal, fi, sv, en) {
            return {
                category: category,
                code: code,
                fi: fi,
                sv: sv,
                en: en,
                srvaOrdinal: srvaOrdinal,
                multipleSpecimenAllowedOnHarvest: multipleSpecimenAllowedOnHarvest
            };
        }
    })

    .service('TranslatedSpecies', function (Species, $filter) {
        var i18n = $filter('rI18nNameFilter');

        this.translateSpecies = function (species) {
            return {
                code: species.code,
                name: i18n(species),
                category: i18nCategory(species.category),
                sortOrder: getSortOrder(species.category)
            };
        };

        var categoryFowl = {fi: 'Riistalinnut', sv: 'Viltfågel', en: 'Fowl'};
        var categoryGameMammal = {fi: 'Riistanisäkkäät', sv: 'Viltdäggdjur', en: 'Game mammal'};
        var categoryUnprotected = {fi: 'Rauhoittamattomat eläimet', sv: 'Icke fredade djur', en: 'Unprotected'};

        function i18nCategory(category) {
            switch (category) {
                case 'GAME_MAMMAL':
                    return i18n(categoryGameMammal);
                case 'UNPROTECTED':
                    return i18n(categoryUnprotected);
                case 'FOWL':
                    return i18n(categoryFowl);
                default:
                    return '';
            }
        }

        function getSortOrder(category) {
            switch (category) {
                case 'GAME_MAMMAL':
                    return 0;
                case 'UNPROTECTED':
                    return 1;
                case 'FOWL':
                    return 2;
                default:
                    return 3;
            }
        }
    })

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

        var BEAN_GOOSE = 26287;

        _.assign(this, {
            'BEAR': BEAR,
            'FALLOW_DEER': FALLOW_DEER,
            'GREY_SEAL': GREY_SEAL,
            'LYNX': LYNX,
            'MOOSE': MOOSE,
            'ROE_DEER': ROE_DEER,
            'WHITE_TAILED_DEER': WHITE_TAILED_DEER,
            'WILD_BOAR': WILD_BOAR,
            'WILD_FOREST_REINDEER': WILD_FOREST_REINDEER,
            'WOLF': WOLF,
            'WOLVERINE': WOLVERINE,
            'BEAN_GOOSE': BEAN_GOOSE
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

        this.isCarnivoreSpecies = function (gameSpeciesCode) {
            return gameSpeciesCode === this.BEAR ||
                gameSpeciesCode === this.WOLF ||
                gameSpeciesCode === this.LYNX ||
                gameSpeciesCode === this.WOLVERINE;
        };
    })

    .service('MooselikeSpecies', function (GameDiaryParameters, GameSpeciesCodes) {
        // TODO: Remove and use Species.getPermitBased()
        this.getPermitBased = function () {
            return GameDiaryParameters.query().$promise.then(function (parameters) {
                return _.filter(parameters.species, function (species) {
                    return GameSpeciesCodes.isPermitBasedMooselike(species.code);
                });
            });
        };
    })

    .service('SpeciesNameService', function ($filter, $translate, Species) {
        var getTranslatedName = $filter('rI18nNameFilter');

        this.translateSpeciesCode = function (speciesCode) {
            var lang = $translate.use() || 'fi';

            if (speciesCode === 0) {
                var other = {'fi': 'muu', 'sv': 'andra', 'en': 'other'};
                return other[lang];
            }
            if (!speciesCode) {
                return;
            }
            return _.chain(Species.getSpeciesMapping()).get(speciesCode).get(lang).value();
        };

        this.addSpeciesNameFunctions = function (obj) {

            var getGameName = function (code, species) {
                if (!code && code !== 0) {
                    return null;
                }

                return _.chain(obj.species)
                    .filter({'code': code})
                    .map(function (s) {
                        var translated = getTranslatedName(s.name);

                        // Side-effect
                        if (species) {
                            species.translatedName = translated;
                        }

                        return translated;
                    })
                    .head()
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
                var cat = categoryId ? _.find(obj.categories, {
                    code: categoryId
                }) : null;
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
                $ctrl.selectedSpecies = _($ctrl.availableSpecies).head();
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
