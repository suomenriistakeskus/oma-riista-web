'use strict';

angular.module('app.jht.harvestseasons', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('jht.harvestseasons', {
                url: '/harvestseasons',
                templateUrl: 'jht/harvestseasons/harvestseasons.html',
                resolve: {
                    harvestSeasons: function (HuntingYearService, HarvestSeason) {
                        return HarvestSeason.list({huntingYear: HuntingYearService.getCurrent()}).$promise;
                    },
                    quotaAreas: function (HarvestSeason) {
                        return HarvestSeason.listQuotaAreas().$promise;
                    }
                },
                controllerAs: '$ctrl',
                controller: function ($state, NotificationService, Helpers, Species, HuntingYearService, ConfirmationDialogService,
                                      HarvestSeason, harvestSeasons, quotaAreas, QuotaSpecies, HarvestSeasonModal) {
                    var $ctrl = this;

                    $ctrl.$onInit = function () {
                        var currentHuntingYear = HuntingYearService.getCurrent();
                        $ctrl.selectedYear = currentHuntingYear;
                        $ctrl.years = _.range(currentHuntingYear - 3, currentHuntingYear + 3);
                        $ctrl.harvestSeasons = harvestSeasons;

                        $ctrl.filterList = ['all', 'quota', 'fowl', 'mammal'];
                        $ctrl.filter = 'all';

                        $ctrl.filterList = createSpeciesFilter();
                        $ctrl.availableSpecies = createAvailableSpecies();

                        createSeasons();
                    };

                    $ctrl.yearChanged = function () {
                        refresh();
                    };

                    $ctrl.speciesChanged = function () {
                        if ($ctrl.selectedSpecies) {
                            if (_.some($ctrl.quotaFilter, matchSelectedSpecies)) {
                                $ctrl.filter = 'quota';
                            } else if (_.some($ctrl.birdFilter, matchSelectedSpecies)) {
                                $ctrl.filter = 'fowl';
                            } else if (_.some($ctrl.mammalFilter, matchSelectedSpecies)) {
                                $ctrl.filter = 'mammal';
                            }
                        } else {
                            $ctrl.filter = 'all';
                        }
                        createSeasons($ctrl.selectedSpecies);
                    };

                    $ctrl.filterChanged = function (filter) {
                        $ctrl.filter = filter;
                        $ctrl.selectedSpecies = null;
                        createSeasons();
                    };

                    $ctrl.edit = function (season) {
                        HarvestSeasonModal.open(season, $ctrl.selectedYear, $ctrl.availableSpecies).then(function () {
                            NotificationService.showDefaultSuccess();
                            refresh();
                        }, function (status) {
                            if (status === 'error') {
                                NotificationService.showDefaultFailure();
                            }
                            refresh();
                        });
                    };

                    $ctrl.isQuotaShown = function () {
                        return $ctrl.filter === 'all' || $ctrl.filter === 'quota';
                    };

                    $ctrl.isFowlShown = function () {
                        return $ctrl.filter === 'all' || $ctrl.filter === 'fowl';
                    };

                    $ctrl.isMammalShown = function () {
                        return $ctrl.filter === 'all' || $ctrl.filter === 'mammal';
                    };

                    $ctrl.add = function () {
                        HarvestSeasonModal.open(null, $ctrl.selectedYear, $ctrl.availableSpecies).then(function () {
                            NotificationService.showDefaultSuccess();
                            refresh();
                        }, function (status) {
                            if (status === 'error') {
                                NotificationService.showDefaultFailure();
                            }
                            refresh();
                        });
                    };

                    $ctrl.copy = function () {
                        ConfirmationDialogService.showConfirmationDialogWithPrimaryAccept(
                            'jht.harvestSeason.list.copyConfirmation.title',
                            'jht.harvestSeason.list.copyConfirmation.body')
                            .then(function () {
                                HarvestSeason.copy({huntingYear: $ctrl.selectedYear}).$promise.then(function () {
                                    NotificationService.showDefaultSuccess();
                                    refresh();
                                }, function (status) {
                                    if (status === 'error') {
                                        NotificationService.showDefaultFailure();
                                    }
                                    refresh();
                                });
                            });

                    };

                    $ctrl.remove = function (season) {
                        ConfirmationDialogService.showConfirmationDialogWithPrimaryAccept(
                            'jht.harvestSeason.list.removeConfirmation.title',
                            'jht.harvestSeason.list.removeConfirmation.body')
                            .then(function () {
                                HarvestSeason.delete({id: season.id}).$promise.then(function () {
                                    NotificationService.showDefaultSuccess();
                                    refresh();
                                }, function (status) {
                                    if (status === 'error') {
                                        NotificationService.showDefaultFailure();
                                    }
                                    refresh();
                                });
                            });
                    };

                    function refresh() {
                        HarvestSeason.list({huntingYear: $ctrl.selectedYear}).$promise.then(function (seasons) {
                            $ctrl.harvestSeasons = seasons;
                            $ctrl.filterList = createSpeciesFilter();
                            $ctrl.availableSpecies = createAvailableSpecies();
                            createSeasons($ctrl.selectedSpecies);
                        });
                    }

                    function createSeasons(speciesFilter) {
                        $ctrl.quotaSeasons = createQuotaSeasons($ctrl.quotaFilter, $ctrl.harvestSeasons, speciesFilter);

                        $ctrl.birdSeasons = _.chain($ctrl.harvestSeasons)
                            .filter(function (season) {
                                return !speciesFilter || season.species.code === speciesFilter.code;
                            })
                            .filter(function (season) {
                                return Species.isBirdPermitSpecies(season.species.code);
                            })
                            .value();

                        $ctrl.mammalSeasons = _.chain($ctrl.harvestSeasons)
                            .filter(function (season) {
                                return !speciesFilter || season.species.code === speciesFilter.code;
                            })
                            .filter(function (season) {
                                return Species.isMammalPermitSpecies(season.species.code) && !_.includes(QuotaSpecies, season.species.code);
                            })
                            .value();
                    }

                    function createQuotaSeasons(species, seasons, speciesFilter) {
                        var filteredSpecies = speciesFilter ? _.filter(species, ['code', speciesFilter.code]) : species;

                        return _.map(filteredSpecies, function (s) {
                            var index = _.findIndex(seasons, function (season) {
                                return season.species.code === s.code;
                            });

                            var season;
                            if (index !== -1) {
                                season = seasons[index];
                            } else {
                                season = createEmptySeason(s);
                            }

                            season.quotas = getOrCreateQuotaAreas(s, season.quotas);

                            return season;
                        });
                    }

                    function createEmptySeason(species) {
                        var speciesName = _.pick(species, ['fi', 'sv', 'en']);

                        return {
                            species: {
                                code: species.code,
                                name: speciesName
                            },
                            name: {
                                fi: $ctrl.selectedYear + ' ' + speciesName.fi + ' kiintiömetsästys',
                                sv: $ctrl.selectedYear + ' ' + speciesName.sv + ' kvotjakt'
                            },
                            gameSpeciesCode: species.code
                        };
                    }

                    function getOrCreateQuotaAreas(species, quotas) {
                        var quotaAreaKey;
                        switch (species.code) {
                            // Bear
                            case 47348:
                                quotaAreaKey = 'PORONHOITOALUE';
                                break;
                            // Grey seal
                            case 47282:
                                quotaAreaKey = 'HALLIALUE';
                                break;
                            // Ringed seal
                            case 200555:
                                quotaAreaKey = 'NORPPAALUE';
                                break;
                        }

                        return _.map(quotaAreas[quotaAreaKey], function (area) {
                            var index = _.findIndex(quotas, function (quota) {
                                return quota.harvestArea &&
                                    quota.harvestArea.harvestAreaType === area.harvestAreaType &&
                                    quota.harvestArea.officialCode === area.officialCode;
                            });

                            if (index !== -1) {
                                return quotas[index];
                            } else {
                                return {
                                    harvestArea: area
                                };
                            }
                        });
                    }

                    function createSpeciesFilter() {
                        $ctrl.quotaFilter = Species.getSpeciesByCode(QuotaSpecies);
                        $ctrl.quotaFilter = _.map($ctrl.quotaFilter, function (species) {
                            return addCategory(species, 'quota');
                        });

                        $ctrl.birdFilter = _.chain($ctrl.harvestSeasons)
                            .filter(function (season) {
                                return Species.isBirdPermitSpecies(season.species.code);
                            })
                            .flatMap(function (season) {
                                return Species.getSpeciesByCode(season.species.code);
                            })
                            .map(function (species) {
                                return addCategory(species, 'fowl');
                            })
                            .value();

                        $ctrl.mammalFilter = _.chain($ctrl.harvestSeasons)
                            .filter(function (season) {
                                return Species.isMammalPermitSpecies(season.species.code) &&
                                    !_.includes(QuotaSpecies, season.species.code);
                            })
                            .flatMap(function (season) {
                                return Species.getSpeciesByCode(season.species.code);
                            })
                            .map(function (species) {
                                return addCategory(species, 'mammal');
                            })
                            .value();

                        return _.concat($ctrl.quotaFilter, $ctrl.birdFilter, $ctrl.mammalFilter);
                    }

                    function matchSelectedSpecies(species) {
                        return species.code === $ctrl.selectedSpecies.code;
                    }

                    function createAvailableSpecies() {
                        var birdSpecies = _.chain(Species.getBirdPermitSpecies())
                            .filter(function (species) {
                                var birdFilterCodes = _.map($ctrl.birdFilter, function (filter) {
                                    return filter.code;
                                });
                                return !_.includes(birdFilterCodes, species.code);

                            })
                            .map(function (species) {
                                return addCategory(species, 'fowl');
                            })
                            .value();

                        var mammalSpecies = _.chain(Species.getMammalPermitSpecies())
                            .filter(function (species) {
                                var mammalFilterCodes = _.map($ctrl.mammalFilter, function (filter) {
                                    return filter.code;
                                });
                                return !_.includes(mammalFilterCodes, species.code) && !_.includes(QuotaSpecies, species.code);
                            })
                            .map(function (species) {
                                return addCategory(species, 'mammal');
                            })
                            .value();

                        return _.concat(birdSpecies, mammalSpecies);
                    }

                    function addCategory(species, category) {
                        var categoryPrefix = 'jht.harvestSeason.gameCategory.';
                        return _.assign({}, species, {gameCategory: categoryPrefix + category});
                    }
                }
            });
    })

    .component('harvestseasonsQuotas', {
        templateUrl: 'jht/harvestseasons/harvestseasons-quotas.html',
        bindings: {
            seasons: '<',
            edit: '&'
        },
        controllerAs: '$ctrl',
        controller: function ($translate) {
            var $ctrl = this;

            $ctrl.speciesSort = function (s1, s2) {
                if (s1.type !== 'object' || s2.type !== 'object') {
                    return (s1.index < s2.index) ? -1 : 1;
                }

                var lang = $translate.use();
                var val1 = s1.value[lang];
                var val2 = s2.value[lang];
                return val1.localeCompare(val2);
            };
        }
    })

    .component('harvestseasonsSeasons', {
        templateUrl: 'jht/harvestseasons/harvestseasons-seasons.html',
        bindings: {
            seasons: '<',
            edit: '&',
            remove: '&'
        },
        controllerAs: '$ctrl',
        controller: function ($translate) {
            var $ctrl = this;

            $ctrl.speciesSort = function (s1, s2) {
                if (s1.type !== 'object' || s2.type !== 'object') {
                    return (s1.index < s2.index) ? -1 : 1;
                }

                var lang = $translate.use();
                var val1 = s1.value[lang];
                var val2 = s2.value[lang];
                return val1.localeCompare(val2);
            };
        }
    })

    .service('HarvestSeasonModal', function ($uibModal) {
        this.open = function (season, huntingYear, availableSpecies) {
            return $uibModal.open({
                templateUrl: 'jht/harvestseasons/harvestseason-modal.html',
                size: 'lg',
                controllerAs: '$ctrl',
                controller: ModalController,
                resolve: {
                    season: _.constant(season),
                    huntingYear: _.constant(huntingYear),
                    availableSpecies: _.constant(availableSpecies)
                }
            }).result;
        };

        function ModalController($uibModalInstance, TranslatedBlockUI, HarvestSeason, season, huntingYear, availableSpecies) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.season = season || {};
                $ctrl.huntingYear = huntingYear;
                $ctrl.availableSpecies = availableSpecies;
                var minBeginDate = new Date($ctrl.huntingYear, 7, 1);
                var initBeginDate = new Date() < minBeginDate ? minBeginDate : new Date();
                $ctrl.beginDateOptions = {
                    'minDate': minBeginDate,
                    'maxDate': new Date($ctrl.huntingYear + 1, 6, 31),
                    'initDate': initBeginDate
                };

                $ctrl.dateChanged();

                $ctrl.isNew = !season;
            };

            $ctrl.dateChanged = function () {
                var minEndDate = new Date($ctrl.huntingYear, 7, 1);
                var initDate = new Date();
                if (!_.isNil($ctrl.season.beginDate)) {
                    minEndDate = new Date($ctrl.season.beginDate);
                    minEndDate.setDate(minEndDate.getDate() + 1);
                    initDate = minEndDate;
                }
                $ctrl.endDateOptions = {
                    'minDate': minEndDate,
                    'initDate': initDate
                };

                var minEndOfReportingDate = new Date(minEndDate);
                if (!_.isNil($ctrl.season.endDate)) {
                    minEndOfReportingDate = new Date($ctrl.season.endDate);
                    initDate = minEndOfReportingDate;
                }
                $ctrl.endOfReportingDateOptions = {
                    'minDate': minEndOfReportingDate,
                    'initDate': initDate
                };

                // The second period should be after the first one
                var minBeginDate2 = new Date(minEndOfReportingDate);
                minBeginDate2.setDate(minBeginDate2.getDate() + 1);
                $ctrl.beginDate2Options = {
                    'minDate': minBeginDate2,
                    'initDate': initDate
                };

                var minEndDate2 = new Date(minBeginDate2);
                if (!_.isNil($ctrl.season.beginDate2)) {
                    minEndDate2 = new Date($ctrl.season.beginDate2);
                    minEndDate2.setDate(minEndDate2.getDate() + 1);
                    initDate = minEndDate2;
                }
                $ctrl.endDate2Options = {
                    'minDate': minEndDate2,
                    'initDate': initDate
                };

                var minEndOfReportingDate2 = new Date(minEndDate2);
                if (!_.isNil($ctrl.season.endDate2)) {
                    minEndOfReportingDate2 = new Date($ctrl.season.endDate2);
                    initDate = minEndOfReportingDate2;
                }
                $ctrl.endOfReportingDate2Options = {
                    'minDate': minEndOfReportingDate2,
                    'initDate': initDate
                };
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };

            $ctrl.save = function () {
                TranslatedBlockUI.start('global.block.wait');

                // Send only game species code to backend, remove other species information
                var savedSeason = _.omit($ctrl.season, ['species']);

                var saveMethod;
                if ($ctrl.season.id) {
                    saveMethod = HarvestSeason.update;
                } else {
                    saveMethod = HarvestSeason.save;

                    if ($ctrl.selectedSpecies) {
                        var nameFi = $ctrl.huntingYear + ' ' + $ctrl.selectedSpecies.fi;
                        var nameSv = $ctrl.huntingYear + ' ' + $ctrl.selectedSpecies.sv;
                        var season = {
                            name: {
                                fi: nameFi,
                                sv: nameSv
                            },
                            gameSpeciesCode: $ctrl.selectedSpecies.code
                        };

                        savedSeason = _.assign({}, savedSeason, season);
                    }
                }

                saveMethod({id: $ctrl.season.id}, savedSeason).$promise.then(function () {
                    $uibModalInstance.close();
                }, function () {
                    $uibModalInstance.dismiss('error');
                }).finally(function () {
                    TranslatedBlockUI.stop();
                });
            };

            $ctrl.hasQuotas = function () {
                return !_.isEmpty($ctrl.season.quotas);
            };

            $ctrl.isSecondPeriodDisabled = function () {
                return _.some([$ctrl.season.beginDate, $ctrl.season.endDate, $ctrl.season.endOfReportingDate], function (item) {
                    return _.isNil(item);
                });
            };

            $ctrl.isSecondPeriodRequired = function () {
                return _.some([$ctrl.season.beginDate2, $ctrl.season.endDate2, $ctrl.season.endOfReportingDate2], function (item) {
                    return !_.isNil(item);
                });
            };
        }
    })

    .factory('HarvestSeason', function ($http, $resource) {
        var API_PREFIX = '/api/v1/harvestseason';
        return $resource(API_PREFIX, {huntingYear: '@huntingYear', id: '@id'}, {
            list: {
                method: 'GET',
                url: API_PREFIX + '/list/:huntingYear',
                isArray: true
            },
            listQuotaAreas: {
                method: 'GET',
                url: API_PREFIX + '/list-quota-areas'
            },
            update: {
                method: 'PUT',
                url: API_PREFIX + '/:id'
            },
            delete: {
                method: 'DELETE',
                url: API_PREFIX + '/:id'
            },
            copy: {
                method: 'POST',
                url: API_PREFIX + '/copy/:huntingYear'
            }
        });
    })

    .constant('QuotaSpecies', [47348, 47282, 200555]);
