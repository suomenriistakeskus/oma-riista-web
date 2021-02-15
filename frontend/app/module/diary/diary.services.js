'use strict';

angular.module('app.diary.services', ['ngResource'])

    .constant('DiaryEntryType', {
        harvest: 'HARVEST',
        observation: 'OBSERVATION',
        srva: 'SRVA'
    })

    .factory('GameDiaryParameters', function ($resource, $http, CacheFactory, SpeciesNameService) {
        function appendTransform(defaults, transform) {
            defaults = angular.isArray(defaults) ? defaults : [defaults];
            return defaults.concat(transform);
        }

        function addFunctions(parameters) {
            parameters.isMultipleSpecimensAllowedForHarvestSpecies = function (gameSpeciesCode) {
                var selectedSpecies = gameSpeciesCode ? _.find(parameters.species, {code: gameSpeciesCode}) : null;
                return selectedSpecies && selectedSpecies.multipleSpecimenAllowedOnHarvests;
            };
        }

        return $resource('api/v1/gamediary/parameters', null, {
            query: {
                method: 'GET',
                isArray: false,
                cache: CacheFactory.get('diaryParameterCache'),
                transformResponse: appendTransform($http.defaults.transformResponse, function (data, headersGetter, status) {
                    if (status === 200 && angular.isObject(data)) {
                        SpeciesNameService.addSpeciesNameFunctions(data);
                        addFunctions(data);
                        return data;
                    } else {
                        return data || {};
                    }
                })
            },
            getObservationSpeciesWithinMooseHunting: {
                url: 'api/v1/gamediary/species/withinMooseHunting',
                method: 'GET',
                isArray: true
            }
        });
    })

    .factory('DiaryEntryRepositoryFactory', function ($resource, $http,
                                                      Helpers, DiaryEntryType, DiaryImageService,
                                                      GameSpeciesCodes, SrvaOtherSpeciesService,
                                                      ObservationCategory) {
        function appendTransform(defaults, transform) {
            defaults = angular.isArray(defaults) ? defaults : [defaults];
            return defaults.concat(transform);
        }

        function decorateRepository(repository) {
            angular.extend(repository.prototype, {
                setDateAndTime: function (date, time) {
                    var dateTime = moment(date).toDate();

                    dateTime.setHours(time.slice(0, 2));
                    dateTime.setMinutes(time.slice(3));

                    this.pointOfTime = Helpers.dateTimeToString(dateTime);
                },
                saveOrUpdate: function () {
                    if (_.has(this, 'geoLocation.zoom')) {
                        delete this.geoLocation.zoom;
                    }

                    return this.id ? this.$update() : this.$save();
                },
                isHarvest: function () {
                    return this.type === DiaryEntryType.harvest;
                },
                isObservation: function () {
                    return this.type === DiaryEntryType.observation;
                },
                isSrva: function () {
                    return this.type === DiaryEntryType.srva;
                },
                isOtherSpecies: function () {
                    return this.gameSpeciesCode === SrvaOtherSpeciesService.getOtherSpeciesCode();
                },
                isMoose: function () {
                    return GameSpeciesCodes.isMoose(this.gameSpeciesCode);
                },
                isDeer: function () {
                    return GameSpeciesCodes.isDeer(this.gameSpeciesCode);
                },
                isWildBoar: function () {
                    return GameSpeciesCodes.isWildBoar(this.gameSpeciesCode);
                },
                isRoeDeer: function () {
                    return GameSpeciesCodes.isRoeDeer(this.gameSpeciesCode);
                },
                isGreySeal: function () {
                    return GameSpeciesCodes.isGreySeal(this.gameSpeciesCode);
                },
                isMooselike: function () {
                    return GameSpeciesCodes.isMooselike(this.gameSpeciesCode);
                },
                isObservationWithinHunting: function () {
                    return this.isObservation() && ObservationCategory.isWithinHunting(this.observationCategory);
                },
                isObservationWithinDeerHunting: function () {
                    return this.isObservation() && ObservationCategory.isWithinDeerHunting(this.observationCategory);
                },
                isPermitBasedDeer: function () {
                    return GameSpeciesCodes.isPermitBasedDeer(this.gameSpeciesCode);
                },
                isPermitBasedMooselike: function () {
                    return GameSpeciesCodes.isPermitBasedMooselike(this.gameSpeciesCode);
                },
                getRepository: function () {
                    return repository;
                },
                getImageUrl: DiaryImageService.getUrl
            });

            return repository;
        }

        function createWithBaseUrlAndType(baseUrl) {
            var repository = $resource(baseUrl + '/:id', {"id": "@id"}, {
                get: {
                    method: 'GET',
                    transformResponse: appendTransform($http.defaults.transformResponse, function (data, headersGetter, status) {
                        if (status === 200 && angular.isObject(data)) {
                            return SrvaOtherSpeciesService.replaceNullWithOtherSpeciesCode(data);
                        } else {
                            return data || {};
                        }
                    })
                },
                save: {
                    method: 'POST',
                    transformRequest: function (data, header) {
                        SrvaOtherSpeciesService.replaceOtherSpeciesCodeWithNull(data);
                        return angular.toJson(data);
                    }
                },
                update: {
                    method: 'PUT',
                    transformRequest: function (data, header) {
                        SrvaOtherSpeciesService.replaceOtherSpeciesCodeWithNull(data);
                        return angular.toJson(data);
                    }
                },
                delete: {method: 'DELETE'}
            });

            // Add methods
            return decorateRepository(repository);
        }

        return {
            create: createWithBaseUrlAndType,
            decorateRepository: decorateRepository
        };
    });
