'use strict';

angular.module('app.rhy.application', [])
    .controller('RhyApplicationsController', function ($q, $state, $stateParams, $scope, $timeout, $location,
                                                       ActiveRoleService, TranslatedBlockUI,
                                                       HarvestPermitApplications, MooselikePermitApplication,
                                                       HuntingYearService, FormPostService,
                                                       diaryParameters, selectedRhyOfficialCode, availableSpecies) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.diaryParameters = diaryParameters;
            $ctrl.isModerator = ActiveRoleService.isModerator();
            $ctrl.activeViewTab = $stateParams.tab;
            $ctrl.applications = [];
            $ctrl.partners = [];
            $ctrl.featureCollection = null;
            $ctrl.scrollToIndex = null;
            $ctrl.huntingYears = [];
            $ctrl.availableSpecies = availableSpecies;

            $ctrl.selectedApplication = null;
            $ctrl.selectedYear = $stateParams.year;
            $ctrl.selectedRhyOfficialCode = selectedRhyOfficialCode;

            if ($stateParams.species) {
                $ctrl.selectedSpecies = _.find($ctrl.availableSpecies, {code: _.parseInt($stateParams.species)});
            } else {
                $ctrl.selectedSpecies = null;
            }

            loadHuntingYears().then(function () {
                loadApplications().then(function () {
                    if ($ctrl.isMapViewActivated()) {
                        $ctrl.showMap($ctrl.activeViewTab.substring(4));
                    } else {
                        $ctrl.showPartnerList();
                    }
                });
            });
        };

        $scope.$watch('$ctrl.selectedRhyOfficialCode', function (current, previous) {
            if (current !== previous) {
                resetView();

                loadHuntingYears().then(function () {
                    $ctrl.showPartnerList();
                    loadApplications();
                });
            }
        });

        $ctrl.onSpeciesChange = function () {
            var speciesCode = _.get($ctrl.selectedSpecies, 'code');
            loadApplications();
            updateStateParameter('species', speciesCode);
        };

        $ctrl.onHuntingYearChange = function () {
            if ($ctrl.selectedYear) {
                resetView();
                loadApplications();
                updateStateParameter('year', $ctrl.selectedYear);
            }
        };

        function resetView() {
            $ctrl.applications = [];
            $ctrl.partners = [];
            $ctrl.selectedApplication = null;
            $ctrl.featureCollection = null;
            updateStateParameter('applicationId', null);
            $ctrl.showPartnerList();
        }

        function loadHuntingYears() {
            if (!$ctrl.selectedRhyOfficialCode) {
                return $q.reject();
            }

            return HarvestPermitApplications.listYears({
                rhyOfficialCode: $ctrl.selectedRhyOfficialCode

            }).$promise.then(function (result) {
                $ctrl.huntingYears = transformHuntingYears(result);

                if (!$ctrl.selectedYear || !_.find($ctrl.huntingYears, {year: $ctrl.selectedYear})) {
                    // Most likely the current year as hunting year is what user expects
                    // Instead of defaulting to current hunting year, default to current calendar year.
                    $ctrl.selectedYear = new Date().getUTCFullYear();
                    updateStateParameter('year', $ctrl.selectedYear);
                }
            });
        }

        function transformHuntingYears(huntingYears) {
            var nextHuntingYear = HuntingYearService.getCurrent() + 1;

            if (huntingYears.indexOf(nextHuntingYear) === -1) {
                huntingYears.push(nextHuntingYear);
            }

            huntingYears = _.sortBy(huntingYears, _.identity);

            return _.map(huntingYears, HuntingYearService.toObj);
        }

        function loadApplications() {
            var selectedApplicationId = _.parseInt($stateParams.applicationId);

            if (!$ctrl.selectedRhyOfficialCode || !$ctrl.selectedYear) {
                return;
            }

            return HarvestPermitApplications.query({
                rhyOfficialCode: $ctrl.selectedRhyOfficialCode,
                huntingYear: $ctrl.selectedYear,
                gameSpeciesCode: _.get($ctrl.selectedSpecies, 'code')

            }).$promise.then(function (result) {
                $ctrl.applications = result;

                if ($ctrl.applications && $ctrl.applications.length) {
                    var selectedApplication = _.find($ctrl.applications, {id: selectedApplicationId});
                    showApplication(selectedApplication ? selectedApplication : $ctrl.applications[0]);
                }
            });
        }

        function showApplication(application) {
            updateStateParameter('applicationId', application.id);
            $ctrl.selectedApplication = application;
            application.isOpen = true;

            $timeout(function () {
                var elementIndex = _.findIndex($ctrl.applications, ['id', application.id]);
                $ctrl.scrollToIndex = elementIndex === -1 ? null : elementIndex;
            });
        }

        $ctrl.selectApplication = function (application) {
            if ($ctrl.selectedApplication && $ctrl.selectedApplication.id === application.id) {
                return;
            }

            showApplication(application);

            if ($ctrl.isMapViewActivated() && $ctrl.hasOmaRiistaArea()) {
                $ctrl.showMap();
            } else {
                $ctrl.showPartnerList();
            }
        };

        $ctrl.isRelatedRhy = function (application) {
            return $ctrl.selectedRhyOfficialCode !== application.rhy.officialCode;
        };

        $ctrl.isPartnersViewActivated = function () {
            return $ctrl.activeViewTab === 'partners';
        };

        $ctrl.isMapViewActivated = function () {
            return $ctrl.activeViewTab && _.startsWith($ctrl.activeViewTab, 'map');
        };

        $ctrl.hasOmaRiistaArea = function () {
            return $ctrl.selectedApplication && $ctrl.selectedApplication.hasPermitArea;
        };

        $ctrl.showMap = function (mapStyle) {
            $ctrl.featureCollection = {crs: null, features: [], type: 'FeatureCollection'};
            $ctrl.mapStyle = mapStyle || $ctrl.mapStyle;
            setSelectedTab('map-' + $ctrl.mapStyle);

            if (!$ctrl.hasOmaRiistaArea() || $ctrl.mapStyle === 'vector') {
                return;
            }

            TranslatedBlockUI.start("global.block.wait");

            MooselikePermitApplication.getGeometry({
                id: $ctrl.selectedApplication.id,
                outputStyle: $ctrl.mapStyle
            }).$promise
                .then(function (featureCollection) {
                    $ctrl.featureCollection = featureCollection;
                })
                .finally(function () {
                    TranslatedBlockUI.stop();
                });
        };

        $ctrl.showPartnerList = function () {
            setSelectedTab('partners');

            if (!$ctrl.selectedApplication) {
                $ctrl.partners = [];
                return;
            }

            MooselikePermitApplication.listPartnerClubs({id: $ctrl.selectedApplication.id}).$promise.then(function (partners) {
                $ctrl.partners = partners;
            });
        };

        $ctrl.exportFragmentExcel = function () {
            FormPostService.submitFormUsingBlankTarget('/api/v1/harvestpermit/application/'
                + $ctrl.selectedApplication.id + '/geometry/fragments/excel');
        };

        function setSelectedTab(tab) {
            $ctrl.activeViewTab = tab;
            updateStateParameter('tab', tab);
        }

        function updateStateParameter(name, value) {
            $stateParams[name] = value;
            $state.params[name] = value;
            $state.go($state.current.name, $stateParams, {
                notify: false,
                reload: false,
                location: 'replace',
                inherit: true
            });
        }
    })
    .component('rhyApplicationDetails', {
        templateUrl: 'rhy/applications/details.html',
        bindings: {
            application: '<',
            diaryParameters: '<',
            showMap: '&',
            showPartnerList: '&'
        },
        controller: function (FormPostService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.speciesNames = '';

                if ($ctrl.diaryParameters && $ctrl.application) {
                    var getSpeciesName = $ctrl.diaryParameters.$getGameName;

                    $ctrl.speciesNames = _.map($ctrl.application.gameSpeciesCodes, function (gameSpeciesCode) {
                        return getSpeciesName(gameSpeciesCode);
                    }).join(', ');
                }
            };

            $ctrl.zip = function () {
                FormPostService.submitFormUsingBlankTarget('/api/v1/harvestpermit/application/'
                    + $ctrl.application.id + '/archive');
            };

            $ctrl.pdf = function () {
                FormPostService.submitFormUsingBlankTarget('/api/v1/harvestpermit/application/' +
                    $ctrl.application.id + '/print/pdf');
            };
        }
    })
    .component('rhyApplicationPartners', {
        templateUrl: 'rhy/applications/partners.html',
        bindings: {
            partners: '<'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.contactPersonsStr = function (contactPersons) {
                return _(contactPersons).map(function (p) {
                    return p.byName + ' ' + p.lastName;
                }).join(', ');
            };
        }
    });
