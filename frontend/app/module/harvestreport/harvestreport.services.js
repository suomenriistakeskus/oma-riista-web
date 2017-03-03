'use strict';

angular.module('app.harvestreport.services', ['ngResource'])
    .factory('HarvestReports', function ($resource) {
        return $resource('api/v1/harvestreport/:id', {id: '@id'}, {
            query: {method: 'GET', isArray: true},
            get: {method: 'GET'},
            getReadOnly: {method: 'GET', url: 'api/v1/harvestreport/:id/readOnly'},
            save: {method: 'POST'},
            update: {method: 'PUT'},
            delete: {method: 'DELETE'},
            createForPermit: {method: 'POST', url: 'api/v1/harvestreport/permit', params: {permitId: '@permitId', permitRev: '@permitRev'}},
            createEndOfHuntForPermit: {method: 'POST', url: 'api/v1/harvestreport/endofhunt', params: {permitId: '@permitId', permitRev: '@permitRev'}}
        });
    })
    .factory('HarvestReportSearch', function($http) {
        var _findPage = function(url, searchParams, pager) {
            return $http({
                method: 'POST',
                url: url,
                params: pager,
                data: searchParams
            });
        };

        this.findPageForAdmin = function(searchParams, pager) {
            return _findPage('api/v1/harvestreport/admin/search', searchParams, pager);
        };

        this.findAllForRhy = function(searchParams) {
            return _findPage('api/v1/harvestreport/rhy/search', searchParams, {});
        };

        return this;
    })
    .service('HarvestAreas', function ($http) {
        var findHarvestArea = function (rhyId, harvestSeasonId) {
            return $http.get('/api/v1/harvestreport/findarea', {params: {
                rhyId: rhyId,
                harvestSeasonId: harvestSeasonId}
            });
        };
        return {findHarvestArea: findHarvestArea};
    })
    .factory('ActiveHarvestSeasons', function ($resource) {
        return $resource('api/v1/harvestreport/activeseasons', {}, {
            query: {method: 'GET', isArray: true}
        });
    })
    .service('ActiveHarvestSeasonsService', function (ActiveHarvestSeasons, ActiveRoleService, Helpers) {
        this.query = function () {
            // For normal users request active seasons, which limits seasons to only those
            // where today is between begin-date and end-of-reporting-date.
            // For moderators and coordinators request all, even expired seasons.
            var params = ActiveRoleService.isModerator() || ActiveRoleService.isCoordinator() ? {} : {date: Helpers.dateToString(new Date())};
            return ActiveHarvestSeasons.query(params);
        }
    })
    .factory('ActivePermitsFields', function ($resource) {
        return $resource('api/v1/harvestreport/activepermits', {}, {
            query: {method: 'GET', isArray: true}
        });
    })
    .factory('HarvestReportFieldsAndSeasons',
        function ($q, ActiveHarvestSeasonsService, ActivePermitsFields, $filter, $translate, Helpers, HarvestPermits) {
            var i18nNameFilter = $filter('rI18nNameFilter');
            var typeQuota = $translate.instant('harvestreport.reportType.OTHERS');
            var typePermit = $translate.instant('harvestreport.reportType.PERMIT');

            var translateName = function (season, fields) {
                if (season) {
                    var name = i18nNameFilter(season) + ' ' + Helpers.dateIntervalToString(season.beginDate, season.endDate);
                    if (season.beginDate2 && moment().isAfter(season.beginDate2)) {
                        return name + ', ' + Helpers.dateIntervalToString(season.beginDate2, season.endDate2);
                    }
                    return name;
                }
                return i18nNameFilter(fields.species.name) + ' - ' + typePermit;
            };

            var _resolveSelectableFields = function () {
                var seasonsPromise = ActiveHarvestSeasonsService.query().$promise;
                var permitFieldsPromise = ActivePermitsFields.query().$promise;
                var promises = $q.all([seasonsPromise, permitFieldsPromise]);
                return promises.then(function (r) {
                    var seasons = r[0];
                    var permitFields = r[1];
                    var selectableFields = [];
                    _.each(seasons, function (season) {
                        var name = translateName(season, null);
                        selectableFields.push({name: name, type: typeQuota, season: season, fields: season.fields});
                    });
                    _.each(permitFields, function (f) {
                        var name = translateName(null, f);
                        selectableFields.push({name: name, type: typePermit, season: null, fields: f});
                    });
                    return selectableFields;
                });
            };
            var validsFiltered = function () {
                return _resolveSelectableFields().then(function (fieldsAndSeasons) {
                    return _.filter(fieldsAndSeasons, function (fs) {
                        return fs.season !== null || !fs.fields.freeHuntingAlso;
                    });
                });
            };
            var validsForAllSeasonsAndPermits = function () {
                return _resolveSelectableFields().then(function (fieldsAndSeasons) {
                    return HarvestPermits.species().$promise.then(function (species) {
                        //filter to have only those which have season or has permit
                        var speciesCodeHavingPermit = _.map(species, function (s) {
                            return s.code;
                        });

                        return  _.filter(fieldsAndSeasons, function (fs) {
                            if (fs.season !== null) {
                                return true;
                            }
                            return speciesCodeHavingPermit.indexOf(fs.fields.species.code) !== -1;
                        });
                    });
                });
            };
            return {
                valids: _resolveSelectableFields,
                validsFiltered: validsFiltered,
                validsForAllSeasonsAndPermits: validsForAllSeasonsAndPermits,
                translateName: translateName
            };
        })

    .factory('HarvestReportParameters', function ($resource) {
        return $resource('api/v1/harvestreport/parameters', null, {
            query: { method: 'GET' }
        });
    })
    .factory('HarvestReportStateChange', function ($resource) {
        return $resource('api/v1/harvestreport/changestate/:id', {id: '@id'}, {
            changeState: { method: 'PUT', params: {rev: '@rev', newstate: '@newstate', reason: '@reason', propertyIdentifier: '@propertyIdentifier'}}
        });
    })
    .factory('HarvestReportFindPerson', function (HttpPost) {
        return {
            findByHunterNumber: function (hunterNumber) {
                return HttpPost.post('api/v1/harvestreport/findperson/hunternumber', {hunterNumber: hunterNumber});
            },
            findBySSN: function (ssn) {
                return HttpPost.post('api/v1/harvestreport/findperson/ssn', {ssn: ssn});
            },
            findByPermitNumber: function (permitNumber) {
                return HttpPost.post('api/v1/harvestreport/findperson/permitnumber', {permitNumber: permitNumber});
            },
            findByPersonName: function (name) {
                return HttpPost.post('api/v1/harvestreport/findperson/name', {name: name});
            }
        };
    })

    .factory('HarvestReportReasonAsker', function ($q, $uibModal, ActiveRoleService) {
        var showAskReasonPrompt = function () {
            var modalInstance = $uibModal.open({
                templateUrl: 'harvestreport/askreason.html',
                controller: function ($scope, $uibModalInstance) {
                    $scope.cancel = function () {
                        $uibModalInstance.dismiss('cancel');
                    };

                    $scope.ok = function (reason) {
                        if (reason) {
                            $uibModalInstance.close(reason);
                        } else {
                            $uibModalInstance.dismiss('Reason was not given');
                        }
                    };
                }
            });

            return modalInstance.result;
        };

        var promptForReason = function () {
            if (ActiveRoleService.isModerator()) {
                return showAskReasonPrompt();

            } else {
                // No reason is required
                return $q.when(null);
            }
        };

        return {promptForReason: promptForReason};
    })
    .directive('harvestReportStateToClass', function () {
        return {
            restrict: 'A',
            scope: false,
            link: function (scope, element, attrs) {
                var state = scope.$eval(attrs.harvestReportStateToClass);
                if (state === null) {
                    element.addClass('r-harvestreport-create');
                }
                else if (state === 'REJECTED') {
                    element.addClass('r-harvestreport-rejected');
                }
                else if (state === 'APPROVED' || state === 'ACCEPTED') {
                    element.addClass('r-harvestreport-approved');
                }
                else if (state === 'SENT_FOR_APPROVAL') {
                    element.addClass('r-harvestreport-sent-for-approval');
                }
                else if (state === 'PROPOSED') {
                    element.addClass('r-harvestreport-proposed');
                }
            }
        };
    })

    .factory('HarvestReportService',
        function ($rootScope, $uibModal, $q, $state, $timeout, Helpers, HarvestReports, HarvestReportParameters, //
                  HarvestReportFieldsAndSeasons, NewHarvestReportState, PermitEndOfHuntingReportService, ActiveRoleService,
                  HarvestPermits) {

            function createFieldAndSeason(report) {
                var name = HarvestReportFieldsAndSeasons.translateName(report.harvestSeason, report.fields);
                return {name: name, fields: report.fields, season: report.harvestSeason};
            }

            function _getHarvestReportParameters() {
                return HarvestReportParameters.query().$promise;
            }

            function _openModal(report, fieldAndSeason) {
                return $uibModal.open({
                    templateUrl: 'harvestreport/form-modal.html',
                    controller: 'HarvestReportFormController',
                    size: 'lg',
                    resolve: {
                        report: Helpers.wrapToFunction(report),
                        parameters: _getHarvestReportParameters,
                        fieldAndSeason: Helpers.wrapToFunction(fieldAndSeason)
                    }
                }).result;
            }

            function _show(reportPromise) {
                var deferred = $q.defer();
                reportPromise.then(function (report) {
                    if (report.endOfHuntingReport) {
                        return PermitEndOfHuntingReportService.viewById(report.permitId);
                    } else {
                        var fieldAndSeason = createFieldAndSeason(report);
                        _openModal(report, fieldAndSeason)
                            .then(deferred.resolve, deferred.reject);
                    }
                });
                return deferred.promise;
            }

            function create(fieldAndSeason, entry, asPage) {
                entry = entry || {};

                if (!ActiveRoleService.isModerator() && !entry.authorInfo) {
                    entry.authorInfo = {byName: $rootScope.account.byName, lastName: $rootScope.account.lastName};
                }

                var age;
                var gender;
                var weight;

                if (entry.specimens && entry.specimens[0]) {
                    var specimen = entry.specimens[0];
                    age = specimen.age ? specimen.age : 'UNKNOWN';
                    gender = specimen.gender ? specimen.gender : 'UNKNOWN';
                    weight = specimen.weight ? specimen.weight : undefined;
                }

                var newReport = {
                    gameSpeciesCode: entry.gameSpeciesCode,
                    pointOfTime: entry.pointOfTime,
                    geoLocation: entry.geoLocation,
                    age: age,
                    gender: gender,
                    weight: weight,
                    gameDiaryEntryId: entry.id,
                    gameDiaryEntryRev: entry.rev,
                    canEdit: true,
                    authorInfo: entry.authorInfo,
                    hunterInfo: entry.actorInfo || entry.authorInfo,
                    permitNumber: entry.permitNumber,
                    huntingAreaType: entry.huntingAreaType,
                    huntingParty: entry.huntingParty,
                    huntingAreaSize: entry.huntingAreaSize,
                    huntingMethod: entry.huntingMethod,
                    reportedWithPhoneCall: entry.reportedWithPhoneCall
                };
                if (asPage) {
                    NewHarvestReportState.storeHarvestReport(newReport, fieldAndSeason);
                    $state.go(ActiveRoleService.isModerator() ? 'reporting.harvestreport.form' : 'harvestreport.main.form');
                } else {
                    return _openModal(newReport, fieldAndSeason);
                }
            }

            function editById(id) {
                return _show(HarvestReports.get({id: id}).$promise);
            }

            function showById(id) {
                return _show(HarvestReports.getReadOnly({id: id}).$promise);
            }

            function edit(report) {
                if (report.endOfHuntingReport) {
                    return PermitEndOfHuntingReportService.viewById(report.permitId);
                } else {
                    var fieldAndSeason = createFieldAndSeason(report);
                    return _openModal(report, fieldAndSeason);
                }
            }

            return {
                create: create,
                editById: editById,
                showById: showById,
                edit: edit
            };
        })
    .factory('NewHarvestReportState', function () {
        var _fieldBySpecies, _harvest, _harvestReport, _fieldAndSeason;
        return {
            store: function (fieldBySpecies, harvest) {
                _fieldBySpecies = fieldBySpecies;
                _harvest = harvest;
            },
            storeHarvestReport: function (harvestReport, fieldAndSeason) {
                _harvestReport = harvestReport;
                _fieldAndSeason = fieldAndSeason;
            },
            getFieldBySpecies: function () {
                return _fieldBySpecies;
            },
            getHarvest: function () {
                return _harvest;
            },
            getHarvestReport: function () {
                return _harvestReport;
            },
            getFieldAndSeason: function () {
                return _fieldAndSeason;
            }
        };
    })
    .factory('FieldsAndSeasonsBySpeciesFactory', function ($filter) {
        var i18NFilter = $filter('rI18nNameFilter');
        var bySpeciesName = function (f) {
            return i18NFilter(f.fields.species.name);
        };

        function build(fieldsAndSeasons, harvestsRequiringReport) {
            function getHarvestsForSpecies(species) {
                return _.filter(harvestsRequiringReport, function (harvest) {
                    return harvest.gameSpeciesCode === species.code;
                });
            }

            var groupedBySpeciesName = _.groupBy(fieldsAndSeasons, bySpeciesName);
            var fieldsAndSeasonsBySpecies = [];
            _.forOwn(groupedBySpeciesName, function (val, key) {
                var species = val[0].fields.species;
                fieldsAndSeasonsBySpecies.push({
                    name: key,
                    values: val,
                    species: species,
                    harvests: getHarvestsForSpecies(species)
                });
            });

            // order of sorts is meaningful. Sort by name first, then sort first those which have entries
            var sorted = _(fieldsAndSeasonsBySpecies).sortBy('name').sortBy(function (f) {
                return f.harvests.length > 0 ? 0 : 1;
            }).value();
            return sorted;
        }

        return {build: build};
    });
