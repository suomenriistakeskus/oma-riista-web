'use strict';

angular.module('app.harvestreport.controllers', [])
    .config(function ($stateProvider) {
            $stateProvider
                .state('harvestreport', {
                    abstract: true,
                    templateUrl: 'account/layout.html',
                    url: '/harvest'
                })
                .state('harvestreport.main', {
                    abstract: true,
                    templateUrl: 'harvestreport/layout.html'
                })
                .state('harvestreport.main.listdone', {
                    url: '/list',
                    templateUrl: 'harvestreport/listdone.html',
                    controller: 'HarvestReportListDoneController',
                    resolve: {
                        harvestReports: function (HarvestReports) {
                            return HarvestReports.query().$promise;
                        },
                        harvestReportLocalityResolver: function (HarvestReportLocalityResolver) {
                            return HarvestReportLocalityResolver.get();
                        }
                    }
                })
                .state('harvestreport.main.todo', {
                    url: '/todo',
                    templateUrl: 'harvestreport/list.html',
                    controller: 'HarvestReportListToDoController',
                    resolve: {
                        fieldsAndSeasons: function (HarvestReportFieldsAndSeasons) {
                            return HarvestReportFieldsAndSeasons.valids();
                        },
                        todoHarvest: function (DiaryEntries) {
                            return DiaryEntries.todo().$promise;
                        },
                        parameters: function (GameDiaryParameters) {
                            return GameDiaryParameters.query().$promise;
                        },
                        harvestReportLocalityResolver: function (HarvestReportLocalityResolver) {
                            return HarvestReportLocalityResolver.get();
                        }
                    }
                })
                .state('harvestreport.main.selectspecies', {
                    url: '/selectspecies',
                    templateUrl: 'harvestreport/select-species.html',
                    controller: 'NewHarvestReportSelectSpeciesController',
                    resolve: {
                        fieldsAndSeasons: function (HarvestReportFieldsAndSeasons) {
                            return HarvestReportFieldsAndSeasons.validsForAllSeasonsAndPermits();
                        }
                    }
                })
                .state('harvestreport.main.form', {
                    url: '/form',
                    templateUrl: 'harvestreport/form-page.html',
                    controller: 'HarvestReportFormController',
                    resolve: {
                        report:
                            function (NewHarvestReportState, $timeout, $state) {
                                var harvestReport = NewHarvestReportState.getHarvestReport();
                                if (!harvestReport) {
                                    $timeout(function () {
                                        $state.go('harvestreport.main.todo', null, {reload: true});
                                    });
                                    return;
                                }
                                return harvestReport;
                            },
                        parameters: function (HarvestReportParameters) {
                            return HarvestReportParameters.query().$promise;
                        },
                        fieldAndSeason: function (NewHarvestReportState) {
                            return NewHarvestReportState.getFieldAndSeason();
                        },
                        $uibModalInstance: function ($state) {
                            var done = function () {
                                $state.go('harvestreport.main.todo', null, {reload: true});
                            };
                            return {close: done, dismiss: done};
                        }
                    }
                })
            ;
        })

    .controller('HarvestReportListToDoController',
        function ($scope, $state, fieldsAndSeasons, todoHarvest, FieldsAndSeasonsBySpeciesFactory, PermitAcceptHarvest,
                  parameters, HarvestReportService, DiaryEntryService, NotificationService, harvestReportLocalityResolver,
                  Harvest, ActiveRoleService) {

            $scope.moderatorView = ActiveRoleService.isModerator();
            $scope.getHuntingArea = harvestReportLocalityResolver.getHuntingArea;
            $scope.getAreaName = harvestReportLocalityResolver.getAreaName;
            $scope.getRhyName = harvestReportLocalityResolver.getRhyName;

            var required = [];
            var fieldsAndSeasonsBySpecies = FieldsAndSeasonsBySpeciesFactory.build(fieldsAndSeasons, todoHarvest.reportRequired);
            _.each(fieldsAndSeasonsBySpecies, function (f) {
                _.each(f.harvests, function (harvest) {
                    required.push({f: f, harvest: harvest});
                });
            });
            $scope.harvestsRequiringReport = required;
            $scope.pendingApprovalToPermit = todoHarvest.pendingApprovalToPermit;
            $scope.rejectedFromPermit = todoHarvest.rejectedFromPermit;

            $scope.getCategoryName = function (fieldBySpecies) {
                return parameters.$getCategoryName(fieldBySpecies.species.categoryId);
            };

            $scope.edit = function (report) {
                HarvestReportService.edit(report).finally(function () {
                    $state.reload();
                });
            };

            var onFailure = function (reason) {
                if (reason === 'error') {
                    NotificationService.showDefaultFailure();
                }
            };

            $scope.editHarvest = function (harvest) {
                DiaryEntryService.edit(new Harvest(harvest));
            };

            $scope.removeHarvest = function (harvest) {
                DiaryEntryService.openRemoveForm(new Harvest(harvest))
                    .then(NotificationService.showDefaultSuccess, onFailure)
                    .finally(function () {
                        $state.reload();
                    });
            };

            $scope.changeState = function (harvest, newState) {
                PermitAcceptHarvest.accept(harvest.id, harvest.rev, newState)
                    .then(function () {
                        $state.reload();
                    }, onFailure);
            };
        })

    .controller('HarvestReportListDoneController',
        function ($scope, $state, harvestReports, HarvestReportService, harvestReportLocalityResolver, PermitEndOfHuntingReportService) {
            $scope.getHuntingArea = harvestReportLocalityResolver.getHuntingArea;
            $scope.getAreaName = harvestReportLocalityResolver.getAreaName;
            $scope.getRhyName = harvestReportLocalityResolver.getRhyName;

            $scope.harvestReports = harvestReports;

            $scope.edit = function (report) {
                if (report.endOfHuntingReport) {
                    PermitEndOfHuntingReportService.viewById(report.permitId).finally(function () {
                        $state.reload();
                    });
                } else {
                    HarvestReportService.edit(report).finally(function () {
                        $state.reload();
                    });
                }
            };
        })

    .controller('HarvestReportInitSpeciesListController',
        function ($scope, $state, NewHarvestReportState) {
            $scope.init = function (fieldBySpecies, harvest) {
                NewHarvestReportState.store(fieldBySpecies, harvest);
                $state.go('harvestreport.main.selectspecies');
            };
        }
    )

    .controller('NewHarvestReportSelectSpeciesController',
        function ($scope, $state, fieldsAndSeasons, DiaryEntryService, HarvestReportService,
                  FieldsAndSeasonsBySpeciesFactory, NewHarvestReportState, ActiveRoleService, CheckPermitNumber) {
            $scope.moderator = ActiveRoleService.isModerator();
            $scope.harvest = NewHarvestReportState.getHarvest();
            $scope.viewState = {searchPermit: null, searchPermitErrorKey: null};

            if (NewHarvestReportState.getFieldBySpecies()) {
                $scope.fieldsAndSeasonsBySpecies = [NewHarvestReportState.getFieldBySpecies()];
                $scope.fieldBySpecies = NewHarvestReportState.getFieldBySpecies();
            } else {
                $scope.fieldsAndSeasonsBySpecies = FieldsAndSeasonsBySpeciesFactory.build(fieldsAndSeasons);
            }

            function _findPermit(permitNumber) {
                if (!permitNumber) {
                    return;
                }
                var permitFound = function (data) {
                    $scope.viewState.searchPermit = data.data;
                    $scope.viewState.searchPermitErrorKey = null;
                };
                var permitNotFound = function () {
                    $scope.viewState.searchPermit = null;
                    $scope.viewState.searchPermitErrorKey = 'harvestreport.form.permitNumberNotFound';
                };

                CheckPermitNumber.check(permitNumber).then(permitFound, permitNotFound);
            }
            $scope.$watch('viewState.searchPermitNumber', _findPermit);

            if ($scope.fieldsAndSeasonsBySpecies && $scope.fieldsAndSeasonsBySpecies.length === 1) {
                // Choose first option when only one choice and select input is disabled
                $scope.fieldBySpecies = $scope.fieldsAndSeasonsBySpecies[0];
            }

            $scope.createHarvestReport = function (fieldBySpecies) {
                if (fieldBySpecies.fields.harvestsAsList) {
                    DiaryEntryService.createHarvestForPermitByFields(fieldBySpecies.fields, $scope.harvest);
                } else {
                    HarvestReportService.create(fieldBySpecies, $scope.harvest, true);
                }
            };
        })

    .controller('HarvestReportFormController',
        function ($scope, $filter, $uibModal, $uibModalInstance, ActiveRoleService,
                  Helpers, NotificationService, MapDefaults, CheckPermitNumber, HarvestAreas, GIS, TranslatedBlockUI,
                  HarvestReports, HarvestReportReasonAsker, HarvestReportFieldsAndSeasons,
                  report, parameters, fieldAndSeason, NewHarvestReportState, HarvestPermitSpeciesAmountService) {

            $scope.harvest = NewHarvestReportState.getHarvest();
            $scope.report = report;
            $scope.report.fields = fieldAndSeason.fields;
            $scope.report.harvestSeason = fieldAndSeason.season;
            $scope.report.gameSpeciesCode = fieldAndSeason.fields.species.code;

            $scope.parameters = parameters;
            $scope.mapDefaults = MapDefaults.create();

            var dateFilter = $filter('date');

            $scope.viewState = {
                moderatorView: ActiveRoleService.isModerator(),
                // Available field options
                selectableFields: [],
                // Selected fields
                fieldAndSeason: fieldAndSeason,
                // Permit number search state
                searchPermitNumber: report.permitNumber,
                searchPermit: null,
                searchPermitError: null,
                // Intermediate date and time values
                date: dateFilter($scope.report.pointOfTime, 'yyyy-MM-dd'),
                time: dateFilter($scope.report.pointOfTime, 'HH:mm'),
                dateFieldHasFocus: false,
                // Looked up RHY by geoLocation
                rhy: null,
                // Looked up area by species and RHY
                harvestArea: null
            };

            $scope.isValidDate = function () {
                if (!$scope.viewState.date) {
                    return false;
                }

                var begin = _getBeginOrEndDate('beginDate');
                var begin2 = _getBeginOrEndDate('beginDate2');
                var end = _getBeginOrEndDate('endDate');
                var end2 = _getBeginOrEndDate('endDate2');

                if (!begin && !begin2 && !end && !end2) {
                    return true;
                }

                // dateWithinRange will return true if begin and end are nulls, therefore let's check that range exists
                if (begin && Helpers.dateWithinRange($scope.viewState.date, begin, end)) {
                    return true;
                }

                return begin2 && Helpers.dateWithinRange($scope.viewState.date, begin2, end2);
            };

            function _getBeginOrEndDate(prop) {
                var fields = fieldAndSeason;
                if (fields) {
                    var target = fields.season ? fields.season : fields.fields;
                    return target ? target[prop] : null;
                }
                return null;
            }

            function _getFieldRequired(fieldName) {
                var fs = fieldAndSeason;
                return fs && fs.fields ? fs.fields[fieldName] : 'NO';
            }

            $scope.isFieldVisible = function (fieldName) {
                return _getFieldRequired(fieldName) !== 'NO';
            };

            $scope.isFieldRequired = function (fieldName) {
                return _getFieldRequired(fieldName) === 'YES';
            };

            $scope.huntingPartyRequired = function () {
                return $scope.report.huntingAreaType === 'HUNTING_SOCIETY';
            };

            $scope.weightVisible = function () {
                return $scope.isFieldVisible('weight') && $scope.report.huntingMethod !== 'SHOT_BUT_LOST';
            };

            $scope.weightRequired = function () {
                return $scope.isFieldRequired('weight') && $scope.report.huntingMethod !== 'SHOT_BUT_LOST';
            };

            $scope.harvestAreaRequired = function () {
                return fieldAndSeason && fieldAndSeason.season && fieldAndSeason.season.quotas &&
                    fieldAndSeason.season.quotas.length > 0;
            };

            $scope.remove = function () {
                $uibModal.open({
                    templateUrl: 'harvestreport/remove.html',
                    resolve: {
                        report: Helpers.wrapToFunction(angular.copy($scope.report))
                    },
                    controller: 'HarvestReportRemoveController'
                }).result.then($uibModalInstance.close);
            };

            $scope.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };

            function _createPointOfTimeString() {
                var date = $scope.viewState.date;
                var time = $scope.viewState.time;
                var dateTime = moment(date).toDate();
                dateTime.setHours(time.slice(0, 2));
                dateTime.setMinutes(time.slice(3));

                return Helpers.dateTimeToString(dateTime);
            }

            function _createCleanCopy(report) {
                // Remove internal properties not supported by backend
                var newReport = angular.copy(report);

                if (newReport.authorInfo) {
                    newReport.authorInfo = { id: report.authorInfo.id };
                }

                if (newReport.hunterInfo) {
                    newReport.hunterInfo = { id: report.hunterInfo.id };
                }

                if (newReport.harvestSeason) {
                    newReport.harvestSeason = { id: report.harvestSeason.id };
                }
                if (newReport.fields) {
                    newReport.fields = { id: report.fields.id };
                }

                if (newReport.harvestQuota) {
                    delete newReport.harvestQuota;
                }

                if (newReport.transitions) {
                    delete newReport.transitions;
                }

                if (newReport.stateHistory) {
                    delete newReport.stateHistory;
                }

                delete newReport.canEdit;
                delete newReport.permittedSpecies;

                return newReport;
            }

            $scope.save = function () {
                $scope.report.pointOfTime = _createPointOfTimeString();

                var newReport = _createCleanCopy($scope.report);

                HarvestReportReasonAsker
                    .promptForReason()
                    .then(function (reason) {
                        var saveMethod = !report.id ? HarvestReports.save : HarvestReports.update;
                        TranslatedBlockUI.start("global.block.wait");
                        return saveMethod({reason: reason}, newReport).$promise;
                    })
                    .then(NotificationService.showDefaultSuccess, NotificationService.showDefaultFailure)
                    .finally(_.flow(TranslatedBlockUI.stop, $uibModalInstance.close));
            };

            $scope.isValid = function () {
                // location and rhy are always required
                // author is required for moderator
                var authorOk = $scope.viewState.moderatorView ? $scope.report.authorInfo : true;
                return $scope.report.geoLocation &&
                    $scope.report.geoLocation.latitude &&
                    $scope.report.geoLocation.longitude &&
                    authorOk &&
                    $scope.report.rhyId &&
                    !$scope.viewState.searchPermitError &&
                    !$scope.invalidSpeciesForDate() &&
                    $scope.isValidDate();
            };

            $scope.findAuthor = function () {
                _openFindPersonDialog(true, true).then(function (personInfo) {
                    $scope.report.authorInfo = personInfo;

                    if (!$scope.report.hunterInfo) {
                        $scope.report.hunterInfo = personInfo;
                    }
                });
            };

            $scope.findHunter = function (isModerator) {
                _openFindPersonDialog(isModerator, isModerator).then(function (personInfo) {
                    $scope.report.hunterInfo = personInfo;
                });
            };

            function _openFindPersonDialog(showSsnSearch, showPermitNumberSearch) {
                return $uibModal.open({
                    templateUrl: 'harvestreport/findperson.html',
                    resolve: {
                        showSsnSearch: Helpers.wrapToFunction(showSsnSearch),
                        showPermitNumberSearch: Helpers.wrapToFunction(showPermitNumberSearch)
                    },
                    controller: 'HarvestReportFindPersonController'
                }).result;
            }

            function _findRhyByGeoLocation(geoLocation) {
                return GIS.getRhyForGeoLocation(geoLocation)
                    .then(function (response) {
                        var rhyByGeoLocation = response.data;
                        $scope.viewState.rhy = rhyByGeoLocation;
                        $scope.report.rhyId = rhyByGeoLocation.id;
                        return rhyByGeoLocation;
                    });
            }

            function _findHarvestArea(rhyId, seasonId) {
                return HarvestAreas.findHarvestArea(rhyId, seasonId)
                    .success(function (area) {
                        $scope.viewState.harvestArea = area;
                    });
            }

            function _findRhyAndHarvestAreaForGeoLocation() {
                // Reset previous values
                $scope.viewState.rhy = null;
                $scope.viewState.harvestArea = null;

                var geoLocation = $scope.report.geoLocation;

                if (geoLocation) {
                    _findRhyByGeoLocation(geoLocation)
                        .then(function (rhy) {
                            if (fieldAndSeason && fieldAndSeason.season) {
                                _findHarvestArea(rhy.id, fieldAndSeason.season.id);
                            }
                        });
                }
            }

            function _findPropertyIdentifier() {
                $scope.propertyIdentifierLookupError = false;
                $scope.report.propertyIdentifier = null;

                var geoLocation = $scope.report.geoLocation;

                if (geoLocation) {
                    GIS.getPropertyIdentifierForGeoLocation(geoLocation)
                        .success(function (data) {
                            report.propertyIdentifier = data;
                        })
                        .error(function () {
                            report.propertyIdentifier = null;
                            $scope.propertyIdentifierLookupError = true;
                        });
                }
            }

            function _findPermit(permitNumber) {
                if (!permitNumber) {
                    return;
                }
                var _updateReportPermitAndView = function (permit, errorKey) {
                    $scope.report.permitNumber = permit ? permit.permitNumber : null;
                    $scope.viewState.searchPermit = permit;
                    $scope.viewState.searchPermitErrorKey = errorKey;
                };

                var noPermitAmounts = function (permit, speciesCode) {
                    return !_.any(permit.speciesAmounts, function (spa) {
                        return spa.amount > 0 && spa.gameSpecies.code === speciesCode;
                    });
                };

                CheckPermitNumber
                    .check(permitNumber)
                    .then(function (data) {
                        var permit = data.data;
                        if (permit.unavailable) {
                            _updateReportPermitAndView(permit, 'gamediary.form.permitUnusable');
                        } else if (permit.harvestsAsList || noPermitAmounts(permit, $scope.report.gameSpeciesCode)) {
                            _updateReportPermitAndView(permit, 'harvestreport.form.permitNotApplicable');
                        } else {
                            _updateReportPermitAndView(permit, null);
                        }
                    }, _.partial(_updateReportPermitAndView, null, 'harvestreport.form.permitNumberNotFound'));
            }

            $scope.invalidSpeciesForDate = function() {
                if ($scope.viewState.searchPermit && $scope.report.gameSpeciesCode) {
                    var validOnDate = $scope.viewState.date;

                    if (validOnDate) {
                        var amounts = $scope.viewState.searchPermit.speciesAmounts;
                        var speciesCode = $scope.report.gameSpeciesCode;

                        return !HarvestPermitSpeciesAmountService.findMatchingAmount(
                            amounts, speciesCode, validOnDate);
                    }
                }

                return false;
            };

            // trigger searches once, otherwise they are not resolved at all
            _findRhyAndHarvestAreaForGeoLocation();
            _findPropertyIdentifier();

            if ($scope.report.canEdit) {
                $scope.$watch('report.geoLocation', _findPropertyIdentifier, true);
                $scope.$watch('report.geoLocation', _findRhyAndHarvestAreaForGeoLocation, true);
                $scope.$watch('viewState.searchPermitNumber', _findPermit);
            }
        })

    .controller('HarvestReportYesNoController',
        function ($scope) {
            $scope.yesNo = [
                {'title': 'yes', 'value': true},
                {'title': 'no', 'value': false}
            ];
        })

    .controller('HarvestReportRemoveController',
        function ($scope, $uibModalInstance, HarvestReports, report) {
            $scope.report = report;
            $scope.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
            $scope.remove = function () {
                HarvestReports.delete({id: report.id}).$promise.then($uibModalInstance.close);
            };
        })

    .controller('HarvestReportStateChangeController',
        function ($scope, $uibModal, Helpers, NotificationService, HarvestReports, HarvestReportStateChange,//
                  HarvestReportReasonAsker, HarvestReportService, TranslatedBlockUI) {

            function _changeState(report, newState) {
                var doChange = function (reason) {
                    TranslatedBlockUI.start("global.block.wait");
                    return HarvestReportStateChange.changeState({
                        id: report.id,
                        rev: report.rev,
                        newstate: newState,
                        reason: reason,
                        propertyIdentifier: report.propertyIdentifier
                    }).$promise.finally(TranslatedBlockUI.stop);
                };

                if (newState === 'APPROVED') {
                    return doChange();
                } else {
                    return HarvestReportReasonAsker.promptForReason().then(doChange);
                }
            }

            $scope.changeState = function (report, newState) {
                _changeState(report, newState)
                    .then(NotificationService.showDefaultSuccess, NotificationService.showDefaultFailure)
                    .finally($scope.$close);
            };

            $scope.hasState = function (report, state) {
                return report.transitions && report.transitions.indexOf(state) !== -1;
            };

            $scope.edit = function (report) {
                var reassign = function (report, res) {
                    delete res.harvestSeason; // needs to be deleted, since it doesn't have $ methods
                    _.assign(report, res);
                };

                var reload = function () {
                    HarvestReports.get({id: report.id}).$promise.then(function (res) {
                        reassign(report, res);
                    });
                };

                var reportCopy = angular.copy(report);
                reportCopy.canEdit = true;
                HarvestReportService.edit(reportCopy).then(reload, reload);
            };

            $scope.delete = function (report) {
                HarvestReports.delete({id:report.id}).$promise
                    .then(NotificationService.showDefaultSuccess, NotificationService.showDefaultFailure)
                    .finally($scope.$close);
            };
        })

    .controller('HarvestReportFindPersonController',
        function ($scope, $uibModalInstance, $q, HarvestReportFindPerson, showSsnSearch, showPermitNumberSearch) {
            $scope.showSsnSearch = showSsnSearch;
            $scope.showPermitNumberSearch = showPermitNumberSearch;
            $scope.search = {};

            var found = function (res) {
                $scope.personWithHunterNumber = res;
                $scope.notFound = false;
            };

            var notFound = function () {
                $scope.personWithHunterNumber = null;
                $scope.notFound = true;
            };

            $scope.$watch('search.hunterNumber', function (hunterNumber) {
                $scope.personWithHunterNumber = null;
                $scope.notFound = false;

                if (hunterNumber) {
                    $scope.search.ssn = null;
                    $scope.search.permitNumber = null;
                    $scope.search.person = null;
                    HarvestReportFindPerson.findByHunterNumber(hunterNumber)
                        .success(found)
                        .error(notFound);
                }
            });

            $scope.$watch('search.ssn', function (ssn) {
                $scope.personWithHunterNumber = null;
                $scope.notFound = false;

                if (ssn) {
                    $scope.search.hunterNumber = null;
                    $scope.search.permitNumber = null;
                    $scope.search.person = null;

                    HarvestReportFindPerson.findBySSN(ssn)
                        .success(found)
                        .error(notFound);
                }
            });

            $scope.$watch('search.permitNumber', function (permitNumber) {
                $scope.personWithHunterNumber = null;
                $scope.notFound = false;

                if (permitNumber) {
                    $scope.search.ssn = null;
                    $scope.search.hunterNumber = null;
                    $scope.search.person = null;

                    HarvestReportFindPerson.findByPermitNumber(permitNumber)
                        .success(found)
                        .error(notFound);
                }
            });

            $scope.searchByName = function (name) {
                $scope.personWithHunterNumber = null;
                $scope.search.ssn = null;
                $scope.search.hunterNumber = null;
                $scope.search.permitNumber = null;

                var deferred = $q.defer();

                HarvestReportFindPerson.findByPersonName(name)
                    .success(function (res) {
                        $scope.notFound = res.length === 0;
                        deferred.resolve(res);
                    })
                    .error(notFound);

                return deferred.promise;
            };

            $scope.getName = function (person) {
                if (person) {
                    return person.extendedName;
                }
            };

            $scope.onPersonSelect = function ($item, $model, $label) {
                found($item);
            };

            $scope.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };

            $scope.ok = function () {
                $uibModalInstance.close($scope.personWithHunterNumber);
            };
        })

    .controller('HarvestReportShowMapController',
        function ($scope, $uibModal, Helpers, WGS84, $filter, $translate) {
            var createData = function (e) {
                var hunter = e.hunter || e.hunterInfo;
                var species = e.species || e.fields.species;
                species = $translate.use() === 'sv' ? species.name.sv : species.name.fi;

                return {
                    species: species,
                    date: $filter('date')(e.pointOfTime, 'd.M.yyyy HH:mm'),
                    hunter: hunter.byName + ' ' + hunter.lastName,
                    gender: $translate.instant('harvestreport.gender.' + e.gender),
                    age: $translate.instant('harvestreport.age.' + e.age)
                };
            };
            var createMarkers = function (reports) {
                var hunterTitle = $translate.instant('harvestreport.form.hunter');
                var genderTitle = $translate.instant('harvestreport.form.gender');
                var ageTitle = $translate.instant('harvestreport.form.age');
                var msgTemplate = _.template('<div><h5>${species} ${date}</h5></div>' +
                    '<div><strong>${hunterTitle}:</strong> ${hunter}</div>' +
                    '<div><strong>${genderTitle}:</strong> ${gender}</div>' +
                    '<div><strong>${ageTitle}:</strong> ${age}</div>');
                return _.map(reports, function (e) {
                    var marker = WGS84.fromETRS(e.geoLocation.latitude, e.geoLocation.longitude);
                    var data = createData(e);
                    data.hunterTitle = hunterTitle;
                    data.genderTitle = genderTitle;
                    data.ageTitle = ageTitle;
                    marker.message = msgTemplate(data);
                    marker.title = data.species + ' ' + data.date + ' ' + data.hunter;
                    return marker;
                });
            };
            var showMap = function (harvestReports) {
                var markers = createMarkers(harvestReports);
                $uibModal.open({
                    templateUrl: 'map/map.html',
                    size: 'lg',
                    resolve: {
                        markers: Helpers.wrapToFunction(markers)
                    },
                    controller: 'MapController'
                });
            };
            $scope.showOnMap = function (harvestReports) {
                showMap(harvestReports);
            };
        });
