'use strict';

angular.module('app.reporting.controllers', [])
    .config(function ($stateProvider) {
            $stateProvider
                .state('reporting', {
                    abstract: true,
                    templateUrl: 'reporting/layout.html',
                    url: '/moderator'
                })
                .state('reporting.home', {
                    url: '/home',
                    templateUrl: 'main/main.html'
                })
                .state('reporting.contacts', {
                    abstract: true,
                    url: '/contacts',
                    templateUrl: 'reporting/contacts-layout.html'
                })
                .state('reporting.contacts.occupation', {
                    url: '/occupation',
                    templateUrl: 'reporting/contacts-occupations.html',
                    controller: 'OccupationContactsSearchController',
                    resolve: {
                        allOccupationTypes: function (OccupationTypes) {
                            return OccupationTypes.query().$promise;
                        },
                        areas: function (Areas) {
                            return Areas.query().$promise;
                        }
                    }
                })
                .state('reporting.contacts.rhy', {
                    url: '/rhy',
                    templateUrl: 'reporting/contacts-organisations.html',
                    controller: 'RhyContactsSearchController',
                    resolve: {
                        allOccupationTypes: function (OccupationTypes) {
                            return OccupationTypes.query().$promise;
                        },
                        areas: function (Areas) {
                            return Areas.query().$promise;
                        }
                    }
                })
                .state('reporting.announcements', {
                    url: '/announcements',
                    controllerAs: '$ctrl',
                    templateUrl: 'reporting/announcements.html',
                    controller: function () {
                        this.rk = {
                            organisationType: 'RK',
                            officialCode: '850'
                        };
                    }
                })
                .state('reporting.nomination', {
                    url: '/jht?occupationType&rhyCode',
                    templateUrl: 'occupation/nomination/list.html',
                    controller: 'OccupationNominationListController',
                    controllerAs: '$ctrl',
                    params: {
                        rhyCode: {
                            value: null
                        },
                        occupationType: {
                            value: null
                        }
                    },
                    resolve: {
                        activeRhy: _.constant(null),
                        searchParams: function (OccupationNominationService, activeRhy, $stateParams) {
                            var searchParameters = OccupationNominationService.createSearchParameters(activeRhy);

                            // Process email link selection
                            if ($stateParams.rhyCode && $stateParams.occupationType) {
                                searchParameters.nominationStatus = 'ESITETTY';
                                searchParameters.rhyCode = $stateParams.rhyCode;
                                searchParameters.occupationType = $stateParams.occupationType;
                            }

                            return searchParameters;
                        },
                        resultList: function (OccupationNominationService, searchParams) {
                            return searchParams ? OccupationNominationService.search(searchParams) : [];
                        }
                    }
                })
                .state('reporting.harvestreport', {
                    abstract: true,
                    url: '/harvestreport',
                    template: '<ui-view autoscroll="false"/>'
                })
                .state('reporting.harvestreport.list', {
                    url: '/list',
                    templateUrl: 'reporting/harvestreports.html',
                    controller: 'ProcessHarvestReportsController',
                    resolve: {
                        fieldsAndSeasons: function (HarvestReportFieldsAndSeasons) {
                            return HarvestReportFieldsAndSeasons.validsForAllSeasonsAndPermits();
                        },
                        areas: function (Areas) {
                            return Areas.query().$promise;
                        },
                        harvestReportLocalityResolver: function (HarvestReportLocalityResolver) {
                            return HarvestReportLocalityResolver.get();
                        }
                    }
                })
                .state('reporting.harvestreport.selectspecies', {
                    url: '/selectspecies',
                    templateUrl: 'harvestreport/select-species.html',
                    controller: 'NewHarvestReportSelectSpeciesController',
                    resolve: {
                        fieldsAndSeasons: function (HarvestReportFieldsAndSeasons) {
                            return HarvestReportFieldsAndSeasons.validsFiltered();
                        },
                        harvestsRequiringReport: function () {
                            return [];
                        }
                    }
                })
                .state('reporting.harvestreport.form', {
                    url: '/form',
                    templateUrl: 'harvestreport/form-page.html',
                    controller: 'HarvestReportFormController',
                    resolve: {
                        report:
                            function (NewHarvestReportState, $timeout, $state) {
                                var harvestReport = NewHarvestReportState.getHarvestReport();
                                if (!harvestReport) {
                                    $timeout(function () {
                                        $state.go('reporting.harvestreport.list', null, {reload: true});
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
                                $state.go('reporting.harvestreport.list', null, {reload: true});
                            };
                            return {close: done, dismiss: done};
                        }
                    }
                })
                .state('reporting.harvestpermits', {
                    url: '/permits',
                    templateUrl: 'reporting/harvestpermits.html',
                    controller: 'ModeratorPermitListController',
                    resolve: {
                        species: function (HarvestPermits) {
                            return HarvestPermits.species().$promise;
                        },
                        areas: function (Areas) {
                            return Areas.query().$promise;
                        }
                    }
                })
                .state('reporting.harvestpermit', {
                    url: '/permit/{id:[0-9]{1,8}}',
                    templateUrl: 'harvestpermit/show.html',
                    controller: 'PermitShowController',
                    resolve: {
                        permit: function (HarvestPermits, $stateParams) {
                            return HarvestPermits.get({id: $stateParams.id}).$promise;
                        }
                    }
                })
                .state('reporting.moosedatacard', {
                    url: '/moosedatacard',
                    templateUrl: 'reporting/moose-data-card.html',
                    controller: 'MooseDataCardImportController',
                    resolve: {
                    }
                })
                .state('reporting.huntingsummary', {
                    url: '/huntingsummary/permit/{permitId:[0-9]{1,8}}/species/{speciesCode:[0-9]{5,6}}',
                    templateUrl: 'reporting/moderator-hunting-summary.html',
                    controller: 'ModeratorHuntingSummaryController',
                    controllerAs: '$ctrl',
                    resolve: {
                        permitId: function ($stateParams) {
                            return $stateParams.permitId;
                        },
                        permit: function (HarvestPermits, permitId) {
                            return HarvestPermits.get({id: permitId}).$promise;
                        },
                        speciesCode: function ($stateParams) {
                            return _.parseInt($stateParams.speciesCode);
                        },
                        huntingSummaries: function (HarvestPermits, permitId, speciesCode) {
                            var params = { permitId: permitId, speciesCode: speciesCode };

                            return HarvestPermits.getClubHuntingSummariesForModeration(params).$promise;
                        },
                        getGameSpeciesName: function (GameDiaryParameters) {
                            return GameDiaryParameters.query().$promise.then(function (parameters) {
                                return parameters.$getGameName;
                            });
                        }
                    }
                })
            ;
        })

    .controller('OccupationContactsSearchController',
        function ($scope, Areas, areas, allOccupationTypes, OccupationContactSearch) {
            var orgs = [];

            var addOrgOccupations = function (orgs, orgType, areas, rhySelection) {
                if (allOccupationTypes[orgType] && allOccupationTypes[orgType].length > 0) {
                    orgs.push({ type: orgType, areas: areas, occupationTypes: allOccupationTypes[orgType], rhySelection: rhySelection });
                }
            };

            addOrgOccupations(orgs, 'RK', undefined);
            addOrgOccupations(orgs, 'VRN', undefined);
            addOrgOccupations(orgs, 'ARN', areas);
            addOrgOccupations(orgs, 'RKA', areas);
            addOrgOccupations(orgs, 'RHY', areas, true);

            $scope.orgs = orgs;
            $scope.opts = [
                { org: orgs[0], area: undefined, rhy: undefined, occupationType: undefined }
            ];

            $scope.addOpt = function (opt) {
                var newOpt = { org: opt.org, area: opt.area, rhy: opt.rhy, occupationType: undefined };
                var i = $scope.opts.indexOf(opt);
                $scope.opts.splice(i + 1, 0, newOpt);
            };
            var remove = function (from, item) {
                return _.filter(from, function (p) {
                    return p !== item;
                });
            };
            $scope.removeOpt = function (opt) {
                if ($scope.opts.length > 1) {
                    $scope.opts = remove($scope.opts, opt);
                }
            };
            $scope.clearOpt = function (opts) {
                opts.area = undefined;
                opts.rhy = undefined;
                opts.occupationType = undefined;
            };

            $scope.getCount = function () {
                if ($scope.pager) {
                    return { count: $scope.pager.total };
                }
            };

            var updatePager = function () {
                if (!$scope.pager) {
                    return;
                }
                var page = $scope.pager.currentPage - 1;
                var begin = page * $scope.pager.pageSize;
                var end = begin + $scope.pager.pageSize;
                $scope.page = $scope.pager.data.slice(begin, end);
            };
            $scope.$watch('pager.currentPage', function () {
                updatePager();
            });

            $scope.submit = function () {
                var data = [];
                _.each($scope.opts, function (opt) {
                    var areaCode = opt.area ? opt.area.officialCode : undefined;
                    var rhyCode = opt.rhy ? opt.rhy.officialCode : undefined;
                    var v = { organisationType: opt.org.type, areaCode: areaCode, rhyCode: rhyCode, occupationType: opt.occupationType };
                    data.push(v);
                });

                // Fill-in form submit data for Excel export file generation
                $scope.postData = angular.toJson(data);

                OccupationContactSearch.search(data).$promise.then(function (data) {
                    $scope.pager = {
                        currentPage: 1,
                        pageSize: 100,
                        total: data.length,
                        data: data
                    };
                    updatePager();

                }).catch(function (data, status, headers, config) {
                    console.log('error', data);
                });
            };
        })

    .controller('RhyContactsSearchController',
        function ($scope, Areas, areas, RhyContactSearch) {
            var orgs = [];

            var addOrgOccupations = function (orgs, orgType, areas, rhySelection) {
                orgs.push({ type: orgType, areas: areas, rhySelection: rhySelection });
            };

            addOrgOccupations(orgs, 'RHY', areas, true);

            $scope.orgs = orgs;
            $scope.opts = [
                { org: orgs[0], area: undefined, rhy: undefined}
            ];

            $scope.addOpt = function (opt) {
                var newOpt = { org: opt.org, area: opt.area, rhy: opt.rhy};
                var i = $scope.opts.indexOf(opt);
                $scope.opts.splice(i + 1, 0, newOpt);
            };
            var remove = function (from, item) {
                return _.filter(from, function (p) {
                    return p !== item;
                });
            };
            $scope.removeOpt = function (opt) {
                if ($scope.opts.length > 1) {
                    $scope.opts = remove($scope.opts, opt);
                }
            };
            $scope.clearOpt = function (opts) {
                opts.area = undefined;
                opts.rhy = undefined;
            };

            $scope.getCount = function () {
                if ($scope.pager) {
                    return { count: $scope.pager.total };
                }
            };

            var updatePager = function () {
                if (!$scope.pager) {
                    return;
                }
                var page = $scope.pager.currentPage - 1;
                var begin = page * $scope.pager.pageSize;
                var end = begin + $scope.pager.pageSize;
                $scope.page = $scope.pager.data.slice(begin, end);
            };
            $scope.$watch('pager.currentPage', function () {
                updatePager();
            });

            $scope.submit = function () {
                var data = [];
                _.each($scope.opts, function (opt) {
                    var areaCode = opt.area ? opt.area.officialCode : undefined;
                    var rhyCode = opt.rhy ? opt.rhy.officialCode : undefined;
                    var v = {areaCode: areaCode, rhyCode: rhyCode};
                    data.push(v);
                });

                // Fill-in form submit data for Excel export file generation
                $scope.postData = angular.toJson(data);

                RhyContactSearch.search(data).$promise.then(function (data) {
                    $scope.pager = {
                        currentPage: 1,
                        pageSize: 100,
                        total: data.length,
                        data: data
                    };
                    updatePager();

                }).catch(function (data, status, headers, config) {
                    console.log('error', data);
                });
            };
        })

    .controller('ContactSearchEmailCopyController',
        function ($scope, $uibModal) {
            var extractEmail = function (d) {
                return d.email;
            };
            var nonEmptyStr = function (s) {
                return s && s.length > 0;
            };
            var extractEmails = function (data) {
                return _(data).map(extractEmail).filter(nonEmptyStr).uniq().value().join('; ');
            };
            var showEmails = function (data) {
                var emails = extractEmails(data);
                $uibModal.open({
                    templateUrl: 'reporting/copyemails.html',
                    controller: ['$scope', function ($scope) {
                        $scope.emails = emails;
                    }]
                });
            };
            $scope.copyEmails = function (data) {
                showEmails(data);
            };
        })

    .controller('ProcessHarvestReportsController',
        function ($state, $stateParams, $scope, fieldsAndSeasons, areas, $translate, HarvestReportService, HarvestReportSearch, harvestReportLocalityResolver, Helpers) {
            $scope.harvestReports = null;
            $scope.fieldsAndSeasons = fieldsAndSeasons;
            $scope.areas = areas;
            $scope.getHuntingArea = harvestReportLocalityResolver.getHuntingArea;
            $scope.getAreaName = harvestReportLocalityResolver.getAreaName;
            $scope.getRhyName = harvestReportLocalityResolver.getRhyName;
            $scope.states = {'PROPOSED': true, 'SENT_FOR_APPROVAL': true, 'REJECTED': false, 'APPROVED': true};
            $scope.dateRange = {
                beginDate: null,
                endDate: null
             };

            $scope.pager = {
                page: 1,
                pageSize: 10,
                total: 0,
                sort: 'id,DESC'
            };

            var getStates = function () {
                return _.filter(_.keys($scope.states), function (key) {
                    return $scope.states[key];
                });
            };

            function createSearchParams() {
                var idOrNull = function (v) {
                    return v ? v.id : null;
                };
                var s = $scope.selectedFieldOrSeason || {};

                return {
                    seasonId: idOrNull(s.season),
                    fieldsId: idOrNull(s.fields),
                    harvestAreaId: idOrNull($scope.harvestArea),
                    areaId: idOrNull($scope.area),
                    rhyId: idOrNull($scope.rhy),
                    states: getStates(),
                    text: $scope.textSearch,
                    permitNumber: $scope.permitNumberSearch,
                    beginDate: Helpers.dateToString($scope.dateRange.beginDate),
                    endDate: Helpers.dateToString($scope.dateRange.endDate)
                };
            }

            $scope.canSearch = function () {
                return getStates().length > 0;
            };

            $scope.search = function (resetPager) {
                if (resetPager) {
                    $scope.pager.page = 1;
                }

                var pageRequest = {
                    page: $scope.pager.page - 1,
                    size: $scope.pager.pageSize,
                    sort: $scope.pager.sort
                };

                var searchParams = createSearchParams();

                // Fill-in form submit data for Excel export file generation
                $scope.postData = angular.toJson(searchParams);

                HarvestReportSearch.findPageForAdmin(searchParams, pageRequest).then(function (response) {
                    $scope.harvestReports = response.data.content;
                    $scope.pager.total = response.data.total;
                });
            };

            $scope.updateHarvestAreas = function (fields) {
                $scope.harvestArea = null;
                var f = fields || {};
                var quotas = f.season ? fields.season.quotas : [];
                $scope.harvestAreas = _.map(quotas, function (quota) {
                    return quota.harvestArea;
                });
            };

            $scope.updateRhys = function (area) {
                $scope.rhy = null;
                $scope.rhys = _(areas).map(function (a) {
                    if (!area || a === area) {
                        return a.subOrganisations;
                    }
                }).flatten().compact().value();
            };

            $scope.updateRhys(null);

            $scope.show = function (report) {
                var reloadPage = $scope.search;
                HarvestReportService.edit(angular.copy(report)).then(reloadPage, reloadPage);
            };

            $scope.createHarvestReportOnBehalf = function () {
                $state.go('reporting.harvestreport.selectspecies');
            };
        })

    .controller('ModeratorPermitListController',
        function ($scope, $uibModal, HarvestPermits, $translate, NotificationService, Helpers, areas, species) {

            $scope.allSpecies = species;
            $scope.speciesSortProperty = 'name.' + $translate.use();
            $scope.areas = areas;

            function search() {
                var params = {};
                params.permitNumber = $scope.permitNumber;
                params.year = $scope.year;
                if ($scope.area) {
                    params.areaId = $scope.area.id;
                }
                if ($scope.species) {
                    params.speciesCode = $scope.species.code;
                }
                HarvestPermits.search(params).$promise.then(function (data) {
                    $scope.permits = data;
                });
            }

            $scope.search = search;

            $scope.canSearch = function () {
                return $scope.species || $scope.area || $scope.permitNumber || $scope.year;
            };

            $scope.reloadCallback = search;
        })

    .controller('MooseDataCardImportController', function ($scope, $timeout, $translate, NotificationService) {

        var acceptedFileExtensions = ['.xml', '.pdf'];

        var errorCodes = {
            duplicate: "duplicate-file-to-upload",
            fileTooBig: "file-too-big",
            unacceptedExtension: "unaccepted-extension"
        };

        $scope.successfulUploads = [];
        $scope.failedUploads = [];
        $scope.dropzone = null;

        $scope.dropzoneConfig = {
            acceptedFiles: acceptedFileExtensions.join(', '),
            addRemoveLinks: true,
            autoProcessQueue: false,
            maxFiles: 2,
            maxFilesize: 10, // MiB
            uploadMultiple: true,
            url: '/api/v1/moosedatacard/import',

            dictFileTooBig: errorCodes.fileTooBig,
            dictInvalidFileType: errorCodes.unacceptedExtension,
            dictRemoveFile: $translate.instant('reporting.mooseDataCardUpload.removeUploadFile')
        };

        $scope.dropzoneConfig.previewTemplate =
            '<div class="dz-preview dz-file-preview">\n' +
            '  <div class="dz-image"><img data-dz-thumbnail /></div>\n' +
            '  <div class="dz-details">\n' +
            '    <div class="dz-filename"><span data-dz-name></span></div>\n' +
            '    <div class="dz-size"><span data-dz-size></span></div>\n' +
            '  </div>\n' +
            '  <div class="dz-progress"><span class="dz-upload" data-dz-uploadprogress></span></div>\n' +
            '  <div class="dz-error-message"><span data-dz-errormessage></span></div>\n' +
            '  <div class="dz-success-mark">\n' +
            '  </div>\n' +
            '  <div class="dz-error-mark">\n' +
            '  </div>\n' +
            '</div>\n';

        // nameFn function takes filename and its last '.' index as parameter.
        var getFilenameTransformation = function (filename, nameFn, returnValueIfNoDotPresent) {
            if (!angular.isString(filename)) {
                return null;
            }

            var lastIndexOfDot = filename.lastIndexOf('.');
            return lastIndexOfDot >= 0 ? nameFn(filename, lastIndexOfDot) : returnValueIfNoDotPresent;
        };

        var getFileBasename = function (filename) {
            return getFilenameTransformation(filename, function (name, lastIndexOfDot) {
                return filename.substring(0, lastIndexOfDot);
            }, filename);
        };

        var getFileExtension = function (filename) {
            return getFilenameTransformation(filename, function (name, lastIndexOfDot) {
                return filename.substring(lastIndexOfDot + 1);
            }, null);
        };

        $scope.dropzoneConfig.accept = function (file, done) {
            var basename, counterpart,
                dz = $scope.dropzone,
                queuedFiles = dz.getQueuedFiles();

            if (queuedFiles.length > 0 && _.pluck(queuedFiles, 'name').indexOf(file.name) >= 0) {
                done(errorCodes.duplicate);
                return;
            }

            done();

            // Custom queueing/processing logic needed to batch xml+pdf file pairs.
            basename = getFileBasename(file.name);
            counterpart = _.find(queuedFiles, function (queuedFile) {
                return basename === getFileBasename(queuedFile.name);
            });

            if (counterpart) {

                if (getFileExtension(counterpart.name) === 'xml') {
                    dz.processFiles([counterpart, file]);
                } else {
                    dz.processFiles([file, counterpart]);
                }
            }
        };

        $scope.dropzoneConfig.paramName = function (fileIndex) {
            switch (fileIndex) {
            case 0:
                return 'xmlFile';
            case 1:
                return 'pdfFile';
            default:
                return 'unknownFile';
            }
        };

        var storeUploadResult = function (xmlFileName, messages, addToCollection, removeFromCollection) {
            var findUpload = function (upload) {
                return upload.filename === xmlFileName;
            };

            _.remove(removeFromCollection, findUpload);

            var existingUpload = _.find(addToCollection, findUpload);

            if (existingUpload) {
                existingUpload.messages = messages;
            } else {
                addToCollection.push({ filename: xmlFileName, messages: messages });
            }
        };

        var uploadSuccessful = function (xmlFileName, messages) {
            storeUploadResult(xmlFileName, messages, $scope.successfulUploads, $scope.failedUploads);
        };

        var uploadFailed = function (xmlFileName, messages) {
            storeUploadResult(xmlFileName, messages, $scope.failedUploads, $scope.successfulUploads);
        };

        $scope.dropzoneEventHandlers = {
            maxfilesexceeded: function (file) {
                NotificationService.showMessage('reporting.mooseDataCardUpload.errorMessages.maxFilesExceeded', 'warn');
                $scope.dropzone.removeFile(file);
            },
            error: function (file, response, xhr) {
                var msg,
                    doRemoveFile = true;

                switch (response) {
                case errorCodes.duplicate:
                    break;
                case errorCodes.fileTooBig:
                    msg = $translate.instant('reporting.mooseDataCardUpload.errorMessages.fileTooBig', {
                        filesize: Math.round(file.size / 1024 / 10.24) / 100,
                        maxFilesize: $scope.dropzoneConfig.maxFilesize
                    });
                    NotificationService.showMessage(msg, 'warn');
                    break;
                case errorCodes.unacceptedExtension:
                    msg = $translate.instant('reporting.mooseDataCardUpload.errorMessages.unacceptedFileExtension', {
                        extension: getFileExtension(file.name)
                    });
                    NotificationService.showMessage(msg, 'warn');
                    break;
                default:
                    doRemoveFile = false;
                }

                if (doRemoveFile) {
                    $scope.dropzone.removeFile(file);
                }
            },
            errormultiple: function (files, response, xhr) {
                var xmlFileName;

                if (files.length === 2) {
                    xmlFileName = getFileExtension(files[0].name) === 'xml' ? files[0].name : files[1].name;
                    uploadFailed(xmlFileName, response.messages);
                }
            },
            successmultiple: function (files, response, e) {
                var xmlFileName;

                if (files.length === 2) {
                    xmlFileName = getFileExtension(files[0].name) === 'xml' ? files[0].name : files[1].name;
                    uploadSuccessful(xmlFileName, response.messages);
                }
            },
            completemultiple: function (files) {
                // Delay removal of completed files from drop-zone for sleeker user experience.
                $timeout(function () {
                    var acceptedFilenames,
                        dz = $scope.dropzone,
                        acceptedFiles = dz.getAcceptedFiles();

                    if (acceptedFiles.length > 0) {
                        acceptedFilenames = _.pluck(acceptedFiles, 'name');

                        _.forEach(files, function (file) {
                            if (acceptedFilenames.indexOf(file.name) >= 0) {
                                dz.removeFile(file);
                            }
                        });
                    }
                }, 900);
            }
        };
    })
    .controller('ModeratorHuntingSummaryController', function ($state, $history, $timeout, $translate,
                                                               ActiveRoleService, HarvestPermits, NotificationService,
                                                               GameSpeciesCodes,
                                                               getGameSpeciesName, huntingSummaries, permit,
                                                               speciesCode) {
        var $ctrl = this;
        var maxSpecimenCount = 99999;

        $ctrl.permit = permit;
        $ctrl.huntingSummaries = huntingSummaries;
        $ctrl.isPermitBasedDeer = GameSpeciesCodes.isPermitBasedDeer(speciesCode);
        $ctrl.completeHuntingOfPermit = true;

        var huntingAreaDefined = function (summary) {
            var totalArea = summary.totalHuntingArea;
            var effectiveArea = summary.effectiveHuntingArea;

            return _.isFinite(totalArea) && totalArea >= 0 ||
                    _.isFinite(effectiveArea) && effectiveArea >= 0;
        };

        var remainingPopulationDefined = function (summary) {
            var totalRemaining = summary.remainingPopulationInTotalArea;
            var effectiveRemaining = summary.remainingPopulationInEffectiveArea;

            return _.isFinite(totalRemaining) && totalRemaining >= 0 ||
                    _.isFinite(effectiveRemaining) && effectiveRemaining >= 0;
        };

        var idsOfClubsRequiringModeration = [];
        var idsOfClubsOriginallyModerated = _($ctrl.huntingSummaries)
            .filter('moderatorOverridden')
            .map('clubId')
            .value();

        function setModeratorOverriddenFlagForSummariesMissingRequiredData(completeHuntingOfPermit) {
            _.forEach($ctrl.huntingSummaries, function (summary) {
                if (completeHuntingOfPermit) {
                    if (summary.moderatorOverridden || !huntingAreaDefined(summary) || !remainingPopulationDefined(summary)) {
                        summary.moderatorOverridden = true;
                        idsOfClubsRequiringModeration.push(summary.clubId);
                    }
                } else {
                    summary.moderatorOverridden = $ctrl.moderatedOriginally(summary);
                }
            });
        }
        setModeratorOverriddenFlagForSummariesMissingRequiredData($ctrl.completeHuntingOfPermit);

        $ctrl.anyPersistentModeratorOverriddenSummariesPresent = idsOfClubsOriginallyModerated.length > 0;


        $ctrl.completeHuntingOfPermitChanged = function () {
            setModeratorOverriddenFlagForSummariesMissingRequiredData($ctrl.completeHuntingOfPermit);
        };

        $ctrl.moderationRequired = function (clubData) {
            return idsOfClubsRequiringModeration.indexOf(clubData.clubId) >= 0;
        };

        $ctrl.moderatedOriginally = function (clubData) {
            return idsOfClubsOriginallyModerated.indexOf(clubData.clubId) >= 0;
        };

        $ctrl.getGameSpeciesName = function () {
            return getGameSpeciesName(speciesCode);
        };

        $ctrl.countTotalSumOf = function (key) {
            return _.sum($ctrl.huntingSummaries, key);
        };

        $ctrl.isEitherHuntingAreaPresent = function (clubData) {
            return _.isFinite(clubData.totalHuntingArea) || _.isFinite(clubData.effectiveHuntingArea);
        };

        $ctrl.isRemainingPopulationForTotalAreaRequired = function (clubData) {
            var effectiveHuntingAreaDefined = _.isFinite(clubData.effectiveHuntingArea);

            return !_.isFinite(clubData.remainingPopulationInTotalArea) &&
                !effectiveHuntingAreaDefined ||
                !_.isFinite(clubData.remainingPopulationInEffectiveArea) &&
                _.isFinite(clubData.totalHuntingArea) &&
                effectiveHuntingAreaDefined;
        };

        $ctrl.isRemainingPopulationForEffectiveAreaRequired = function (clubData) {
            var totalHuntingAreaDefined = _.isFinite(clubData.totalHuntingArea);

            return !_.isFinite(clubData.remainingPopulationInEffectiveArea) &&
                !totalHuntingAreaDefined ||
                !_.isFinite(clubData.remainingPopulationInTotalArea) &&
                totalHuntingAreaDefined &&
                _.isFinite(clubData.effectiveHuntingArea);
        };

        $ctrl.getMaxForEffectiveHuntingArea = function (clubData) {
            return clubData.totalHuntingArea || clubData.permitAreaSize;
        };

        $ctrl.getMaxForNonEdibleAdults = function (clubData) {
            return (clubData.adultMales || 0) + (clubData.adultFemales || 0);
        };

        $ctrl.getMaxForNonEdibleYoungs = function (clubData) {
            return (clubData.youngMales || 0) + (clubData.youngFemales || 0);
        };

        $ctrl.getMaxForRemainingPopulationInEffectiveHuntingArea = function (clubData) {
            return clubData.remainingPopulationInTotalArea || maxSpecimenCount;
        };

        $ctrl.isValid = function (form) {
            var summaries = _.filter($ctrl.huntingSummaries, 'moderatorOverridden');
            return summaries.length > 0 && form.$valid &&
                _.every(summaries, huntingAreaDefined) &&
                _.every(summaries, remainingPopulationDefined);
        };

        var goToPreviousState = function () {
            $history.back().catch(function (error) {
                $state.go(ActiveRoleService.isModerator() ? 'reporting.home' : 'profile.diary');
            });
        };

        $ctrl.submit = function () {
            var pathVariables = {
                permitId: permit.id,
                speciesCode: speciesCode,
                complete: $ctrl.completeHuntingOfPermit ? 1 : 0
            };

            var requestBody = angular.toJson($ctrl.huntingSummaries);

            HarvestPermits.massOverrideClubHuntingSummaries(pathVariables, requestBody).$promise.then(function () {
                NotificationService.showMessage('reporting.huntingSummary.onSavedNotification', 'success');
                goToPreviousState();
            });
        };

        $ctrl.delete = function () {
            var pathVariables = {
                permitId: permit.id,
                speciesCode: speciesCode
            };

            HarvestPermits.deleteModeratorOverriddenClubHuntingSummaries(pathVariables).$promise.then(function () {
                NotificationService.showMessage('reporting.huntingSummary.onDeletedNotification', 'success');
                goToPreviousState();
            });
        };

        $ctrl.cancel = function () {
            goToPreviousState();
        };
    })
;
