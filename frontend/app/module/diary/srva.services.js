'use strict';

angular.module('app.diary.services')
    .factory('Srva', function (DiaryEntryRepositoryFactory, DiaryEntryType, WGS84) {
        var Srva = DiaryEntryRepositoryFactory.create('api/v1/srva/srvaevent');

        /**
         * Create a new Srva object (not yet persisted).
         *
         * @param {{
         *   gameSpeciesCode : preselected official code of game species
         *   geoLocation: predefined geolocation
         * }} opts Options to populate Observation object with
         */
        Srva.createTransient = function (opts) {
            var geoLocation = opts.geoLocation;

            if (geoLocation) {
                var etrs = WGS84.toETRS(geoLocation.lat, geoLocation.lng);

                geoLocation = {
                    latitude: etrs.lat,
                    longitude: etrs.lng,
                    zoom: geoLocation.zoom
                };
            }

            var gameSpeciesCode = opts.gameSpeciesCode ? _.parseInt(opts.gameSpeciesCode) : null;

            return new Srva({
                id: null,
                type: DiaryEntryType.srva,
                gameSpeciesCode: gameSpeciesCode,
                geoLocation: geoLocation || {},
                canEdit: true
            });
        };

        return Srva;
    })

    .factory('GameDiarySrvaParameters', function ($resource, CacheFactory, SpeciesNameService, SrvaOtherSpeciesService) {

        return $resource('api/v1/srva/parameters', null, {
            query: {
                method: 'GET',
                isArray: false,
                cache: CacheFactory.get('diarySrvaParameterCache'),
                transformResponse: function (data, headers, status) {
                    var parameters = angular.fromJson(data);

                    if (status >= 400) {
                        return parameters;
                    }

                    SpeciesNameService.addSpeciesNameFunctions(parameters);
                    SrvaOtherSpeciesService.addOtherSpecies(parameters.species);
                    return parameters;
                }
            }
        });
    })

    .service("SrvaService", function (AuthenticationService, Account) {

        this.isEnableSrva = function () {
            var account = AuthenticationService.getAuthentication();
            return account && !!account.enableSrva;
        };

        this.updateSrvaStatus = function (enable) {
            var method = enable ? Account.srvaEnable : Account.srvaDisable;
            return method().$promise.then(AuthenticationService.reloadAuthentication);
        };
    })

    .service("SrvaOtherSpeciesService", function (DiaryEntryType) {
        var otherSpeciesCode = 0;
        var otherSpecies = {"code": otherSpeciesCode, "name": {"fi": "muu", "sv": "andra", "en": "other"}};

        this.getOtherSpeciesCode = function () {
            return otherSpeciesCode;
        };

        this.addOtherSpecies = function (species) {
            species.push(otherSpecies);
            return species;
        };

        this.replaceNullsWithOtherSpeciesCodeInEntries = function (entries) {
            entries.forEach(function (entry) {
                if (!entry.gameSpeciesCode && entry.type === DiaryEntryType.srva) {
                    entry.gameSpeciesCode = otherSpeciesCode;
                }
            });
            return entries;
        };

        this.replaceNullWithOtherSpeciesCode = function (entry) {
            if (!entry.gameSpeciesCode && entry.type === DiaryEntryType.srva) {
                entry.gameSpeciesCode = otherSpeciesCode;
            }
            return entry;
        };

        this.replaceOtherSpeciesCodeWithNull = function (entry) {
            if (entry.gameSpeciesCode === otherSpeciesCode && entry.type === DiaryEntryType.srva) {
                entry.gameSpeciesCode = null;
            }
            return entry;
        };
    })
;
