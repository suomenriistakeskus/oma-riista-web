(function () {
    "use strict";

    angular.module('app.clubarea.proposal', ['ngResource'])
        .config(function ($stateProvider) {
            $stateProvider.state('club.area.proposals', {
                url: '/areaproposals?{areaId:[0-9]{1,8}}',
                templateUrl: 'club/area/proposal/club-proposals.html',
                controller: 'AreaProposalListController',
                controllerAs: '$ctrl',
                bindToController: true,
                wideLayout: true,
                reloadOnSearch: false,
                params: {
                    areaId: null
                },
                resolve: {
                    selectedAreaId: function ($stateParams) {
                        return _.parseInt($stateParams.areaId);
                    },
                    huntingYears: function (AreaProposalListService, clubId) {
                        return AreaProposalListService.listHuntingYears(clubId);
                    }
                }
            });
        })
        .factory('AreaProposals', function ($resource) {
            return $resource('/api/v1/harvestpermitarea/:id', {"clubId": "@clubId", "id": "@id"}, {
                'query': {
                    method: 'GET',
                    params: {year: "@year"},
                    isArray: true
                },
                'get': {method: 'GET'},
                'status': {
                    method: 'GET',
                    url: '/api/v1/harvestpermitarea/:id/status'
                },
                'update': {method: 'PUT'},
                'geometry': {
                    method: 'GET',
                    url: '/api/v1/harvestpermitarea/:id/geometry'
                },
                'ready': {
                    method: 'POST',
                    url: '/api/v1/harvestpermitarea/:id/ready'
                },
                'incomplete': {
                    method: 'POST',
                    url: '/api/v1/harvestpermitarea/:id/incomplete'
                },
                'huntingYears': {
                    method: 'GET',
                    url: '/api/v1/harvestpermitarea/huntingyears/:clubId',
                    isArray: true
                }
            });
        })
        .factory('AreaProposalPartners', function ($resource) {
            return $resource('/api/v1/harvestpermitarea/:id/areas', {"id": "@id"}, {
                'add': {
                    method: 'POST',
                    url: '/api/v1/harvestpermitarea/:id/areas/:externalId',
                    params: {'id': '@id', 'externalId': '@externalId'}
                },
                'remove': {
                    method: 'DELETE',
                    url: '/api/v1/harvestpermitarea/:id/areas/:partnerId',
                    params: {'id': '@id', 'partnerId': '@partnerId'}
                },
                'updateGeometry': {
                    method: 'POST',
                    url: '/api/v1/harvestpermitarea/:id/areas/:partnerId/geometry',
                    params: {'id': '@id', 'partnerId': '@partnerId'}
                }
            });
        })
        .service('AreaProposalService', function ($uibModal, AreaProposals, FormSidebarService, HuntingYearService) {
            var nextHuntingYear = HuntingYearService.getCurrent() + 1;
            var formSidebar = createFormSidebar();

            this.addAreaProposal = function (clubId) {
                return formSidebar.show({
                    area: {
                        clubId: clubId,
                        huntingYear: nextHuntingYear
                    }
                });
            };

            function createAreaCopyForAPI(area) {
                var copy = {};
                _.assign(copy, area);
                delete copy.isOpen;
                return copy;
            }

            this.editAreaProposal = function (area) {
                return formSidebar.show({
                    id: area.id,
                    area: createAreaCopyForAPI(area)
                });
            };

            function createFormSidebar() {
                var modalOptions = {
                    controller: 'AreaProposalFormController',
                    templateUrl: 'club/area/proposal/form.html',
                    largeDialog: false,
                    resolve: {}
                };

                function parametersToResolve(parameters) {
                    var area = parameters.area;

                    return {
                        area: _.constant(area),
                        huntingYears: function () {
                            var huntingYears = [nextHuntingYear];

                            if (area) {
                                huntingYears.push(area.huntingYear);
                            }

                            return _(huntingYears)
                                .filter()
                                .uniq()
                                .map(HuntingYearService.toObj)
                                .value();
                        }
                    };
                }

                return FormSidebarService.create(modalOptions, AreaProposals, parametersToResolve);
            }
        })
        .service('AreaProposalListService', function ($filter, AreaProposals, HuntingYearService) {
            var i18nFilter = $filter('rI18nNameFilter');

            this.list = function (clubId, huntingYear) {
                var nextHuntingYear = HuntingYearService.getCurrent() + 1;

                return AreaProposals.query({
                    clubId: clubId,
                    year: huntingYear || nextHuntingYear
                }).$promise.then(function (areas) {
                        return _.sortBy(areas, function (area) {
                            return i18nFilter(area).toLowerCase();
                        });
                    }
                );
            };

            this.listHuntingYears = function (clubId) {
                return AreaProposals.huntingYears({clubId: clubId}).$promise.then(function (result) {
                    return _.map(result, HuntingYearService.toObj);
                });
            };

            this.selectActiveArea = function (areas, selectedAreaId) {
                var area = _.find(areas, 'id', selectedAreaId);
                return area || _.chain(areas).first().value();
            };
        })

        .controller('AreaProposalListController', function ($location, $q,
                                                            ActiveRoleService, NotificationService, TranslatedBlockUI,
                                                            AreaProposals, AreaProposalPartners,
                                                            AreaProposalService, AreaProposalListService,
                                                            selectedAreaId, clubId, huntingYears, rhyBounds) {
            var $ctrl = this;

            $ctrl.rhyBounds = rhyBounds;
            $ctrl.huntingYears = huntingYears;
            $ctrl.selectedAreaId = selectedAreaId;
            $ctrl.selectedYear = null;
            $ctrl.areas = [];
            $ctrl.partners = [];
            $ctrl.featureCollection = null;

            // Activate map view if no area initially selected.
            $ctrl.mapTabActivated = !selectedAreaId;

            $ctrl.$onInit = function () {
                $ctrl.reloadAreas();
            };

            $ctrl.isMapViewActivated = function () {
                return $ctrl.mapTabActivated;
            };

            $ctrl.showMap = function () {
                $ctrl.mapTabActivated = true;

                if ($ctrl.selectedAreaId) {
                    TranslatedBlockUI.start("global.block.wait");

                    AreaProposals.geometry({id: $ctrl.selectedAreaId}).$promise.then(function (featureCollection) {
                        $ctrl.featureCollection = featureCollection;
                        $ctrl.partners = [];
                    }).finally(function () {
                        TranslatedBlockUI.stop();
                    });
                }
            };

            $ctrl.showPartnerList = function () {
                $ctrl.mapTabActivated = false;

                if ($ctrl.selectedAreaId) {
                    AreaProposalPartners.query({id: $ctrl.selectedAreaId}).$promise.then(function (partners) {
                        $ctrl.partners = partners;
                        $ctrl.featureCollection = null;
                    });
                }
            };

            $ctrl.isContactPerson = function () {
                return ActiveRoleService.isClubContact() || ActiveRoleService.isModerator();
            };

            $ctrl.addAreaProposal = function () {
                AreaProposalService.addAreaProposal(clubId).then(function (area) {
                    $ctrl.onAreaChanged(area);
                });
            };

            $ctrl.onAreaSelect = function (area) {
                if (area && $ctrl.selectedAreaId !== area.id) {
                    showArea(area);
                }
            };

            $ctrl.onAreaChanged = function (area) {
                $ctrl.selectedAreaId = area.id;
                $ctrl.selectedArea = area;
                $ctrl.selectedYear = area.huntingYear;

                $ctrl.reloadAreas();

                // Reload huntingYears if area year does not exist in option list.
                if (!_.some($ctrl.huntingYears, 'year', $ctrl.selectedYear)) {
                    AreaProposalListService.listHuntingYears(clubId).then(function (result) {
                        $ctrl.huntingYears = result;
                    });
                }
            };

            $ctrl.reloadAreas = function () {
                AreaProposalListService.list(clubId, $ctrl.selectedYear).then(function (areas) {
                        $ctrl.areas = areas;

                        var selectedArea = AreaProposalListService.selectActiveArea(areas, $ctrl.selectedAreaId);

                        if (selectedArea) {
                            showArea(selectedArea);
                        }
                    }
                );
            };

            function showArea(area) {
                area.isOpen = true;

                $ctrl.selectedYear = area.huntingYear;
                $ctrl.selectedAreaId = area.id;
                $ctrl.selectedArea = area;

                $location.search({areaId: area.id});

                $ctrl.showPartnerList();
            }
        })
        .component('areaProposalList', {
            templateUrl: 'club/area/proposal/area-proposal-list.html',
            bindings: {
                areas: '<',
                mapViewActive: '<',
                reloadAreas: '&',
                onAreaSelect: '&',
                onAreaChanged: '&',
                showMap: '&',
                showPartnerList: '&'
            },
            controller: function (ActiveRoleService, NotificationService, FormPostService,
                                  AreaProposalService, AreaProposals,
                                  AreaPrintModal, PendingAreaProposalModal) {
                var $ctrl = this;

                function isAreaInState(area, state) {
                    return _.get(area, 'status', null) === state;
                }

                $ctrl.getAreaToggleClasses = function (area) {
                    return {
                        'glyphicon': true,
                        'glyphicon-chevron-down': area.isOpen,
                        'glyphicon-chevron-right': !area.isOpen
                    };
                };

                $ctrl.getStatusClasses = function (status) {
                    return {
                        'text-warning': status === 'INCOMPLETE',
                        'text-danger': status === 'PROCESSING' || status === 'PROCESSING' || status === 'PENDING',
                        'text-success': status === 'READY',
                        'text-info': status === 'LOCKED'
                    };
                };

                $ctrl.isContactPerson = function () {
                    return ActiveRoleService.isClubContact() || ActiveRoleService.isModerator();
                };

                $ctrl.selectArea = function (area) {
                    $ctrl.onAreaSelect({'area': area});
                };

                // EDIT

                $ctrl.isEditAreaButtonActivated = function (area) {
                    return $ctrl.isContactPerson() && isAreaInState(area, 'INCOMPLETE');
                };

                $ctrl.editAreaProposal = function (area) {
                    AreaProposalService.editAreaProposal(area).then(function (area) {
                        $ctrl.onAreaChanged({'area': area});
                    });
                };

                // INCOMPLETE

                $ctrl.isIncompleteButtonActivated = function (area) {
                    return $ctrl.isContactPerson() &&
                        (isAreaInState(area, 'READY') || isAreaInState(area, 'PROCESSING_FAILED'));
                };

                $ctrl.setIncomplete = function (area) {
                    $ctrl.reloadAreas();

                    AreaProposals.incomplete({id: area.id}).$promise.then(function () {
                        $ctrl.reloadAreas();
                    }, function () {
                        NotificationService.showDefaultFailure();
                    });
                };

                // READY

                $ctrl.isReadyButtonActivated = function (area) {
                    return $ctrl.isContactPerson() && isAreaInState(area, 'INCOMPLETE') && area.partnerCount > 0;
                };

                $ctrl.setReady = function (area) {
                    function onSuccess() {
                        NotificationService.showDefaultSuccess();
                        $ctrl.reloadAreas();
                    }

                    function onFailure() {
                        NotificationService.showDefaultFailure();
                        $ctrl.reloadAreas();
                    }

                    AreaProposals.ready({id: area.id}).$promise.then(function (res) {
                        if (res.result === 'pending') {
                            PendingAreaProposalModal.open(area.id).then(onSuccess, onFailure);
                        } else {
                            onSuccess();
                        }
                    }, onFailure);
                };

                // PRINT

                $ctrl.isPrintAreaButtonActivated = function (area) {
                    return isAreaInState(area, 'READY') || isAreaInState(area, 'LOCKED');
                };

                $ctrl.printArea = function (area) {
                    AreaPrintModal.printArea('/api/v1/harvestpermitarea/' + area.id + '/print');
                };

                // EXPORT

                $ctrl.exportPartnersToExcel = function (area) {
                    FormPostService.submitFormUsingBlankTarget('/api/v1/harvestpermitarea/' + area.id + '/excel/partners', {});
                };
            }
        })
        .component('areaProposalListDetails', {
            templateUrl: 'club/area/proposal/area-proposal-details.html',
            bindings: {
                area: '<'
            },
            controller: function () {
                var $ctrl = this;

                $ctrl.showIncompleteWarning = function () {
                    return $ctrl.area.status === 'INCOMPLETE' ||
                        $ctrl.area.status === 'PENDING' ||
                        $ctrl.area.status === 'PROCESSING' ||
                        $ctrl.area.status === 'PROCESSING_FAILED';
                };

                function isReadyOrLocked() {
                    return $ctrl.area && ($ctrl.area.status === 'READY' || $ctrl.area.status === 'LOCKED');
                }

                $ctrl.showExternalId = function () {
                    return $ctrl.area && isReadyOrLocked();
                };

                $ctrl.showAreaSize = function () {
                    return $ctrl.area && isReadyOrLocked() && $ctrl.area.computedAreaSize >= 0;
                };

                $ctrl.landAreaSize = function () {
                    return $ctrl.area ? $ctrl.area.computedAreaSize - $ctrl.area.waterAreaSize : null;
                };

                $ctrl.showRhyAndHta = function () {
                    return isReadyOrLocked();
                };
            }
        })

        .controller('AreaProposalFormController', function ($scope, area, huntingYears) {
            $scope.area = area;
            $scope.huntingYears = huntingYears;

            $scope.save = function () {
                $scope.$close($scope.area);
            };

            $scope.cancel = $scope.$dismiss;
        })

        .component('rAreaProposalPartnerList', {
            templateUrl: 'club/area/proposal/partners.html',
            bindings: {
                area: '<',
                partners: '<',
                isContactPerson: '<',
                onPartnerChanged: '&'
            },
            controller: function ($translate, dialogs, AreaProposalAddPartnerModal, AreaProposalPartners) {
                var $ctrl = this;

                $ctrl.isAreaIncomplete = function () {
                    return $ctrl.area && $ctrl.area.status === 'INCOMPLETE';
                };

                $ctrl.showAddPartner = function () {
                    AreaProposalAddPartnerModal.open($ctrl.area.id).then(function () {
                        $ctrl.onPartnerChanged();
                    });
                };

                $ctrl.deletePartner = function (id) {
                    var dialogTitle = $translate.instant('global.dialog.confirmation.title');
                    var dialogMessage = $translate.instant('global.dialog.confirmation.text');
                    var dialog = dialogs.confirm(dialogTitle, dialogMessage);

                    dialog.result.then(function () {
                        AreaProposalPartners.remove({
                            id: $ctrl.area.id,
                            partnerId: id
                        }).$promise.then($ctrl.onPartnerChanged);
                    });
                };

                $ctrl.updateGeometry = function (id) {
                    AreaProposalPartners.updateGeometry({
                        id: $ctrl.area.id,
                        partnerId: id
                    }).$promise.then($ctrl.onPartnerChanged);
                };
            }
        })
        .service('PendingAreaProposalModal', function ($uibModal, $interval, AreaProposals) {
            this.open = function (permitAreaId) {
                var modalInstance = $uibModal.open({
                    templateUrl: 'club/area/proposal/area-pending-modal.html',
                    resolve: {
                        permitAreaId: _.constant(permitAreaId)
                    },
                    controllerAs: '$ctrl',
                    controller: ModalController
                });
                return modalInstance.result;
            };

            function ModalController($uibModalInstance, permitAreaId) {
                var $ctrl = this;
                $ctrl.status = 'PENDING';
                $ctrl.progressBarWidth = '0';

                var progress = 0;

                var intervalPromise = $interval(function () {
                    AreaProposals.status({id: permitAreaId}).$promise.then(function (res) {
                        progress++;
                        $ctrl.status = res.status;
                        $ctrl.progressBarWidth = Math.round(99.0 * (1.0 - 1.0 / Math.exp(progress / 100.0)));

                        if (res.status === 'PENDING' || res.status === 'PROCESSING') {
                            return;
                        }

                        $interval.cancel(intervalPromise);

                        if (res.status === 'READY') {
                            $uibModalInstance.close();
                        } else {
                            $uibModalInstance.dismiss();
                        }
                    });
                }, 1000);
            }
        })
        .service('AreaProposalAddPartnerModal', function ($uibModal, NotificationService, AreaProposalPartners) {
            this.open = function (permitAreaId) {
                var modalInstance = $uibModal.open({
                    templateUrl: 'club/area/proposal/add-club-area.html',
                    resolve: {},
                    controllerAs: '$ctrl',
                    controller: ModalController
                });

                return modalInstance.result.then(function (areaExternalId) {
                    return AreaProposalPartners.add({
                        id: permitAreaId,
                        externalId: areaExternalId
                    }).$promise.then(function () {
                        NotificationService.showDefaultSuccess();
                    }, function (response) {
                        if (response.status === 404) {
                            NotificationService.showMessage('club.area.proposal.addClubArea.notFound', 'warn');
                        } else {
                            NotificationService.showDefaultFailure();
                        }
                    });
                });
            };

            function ModalController($uibModalInstance) {
                var $ctrl = this;

                $ctrl.areaExternalId = null;

                $ctrl.ok = function () {
                    $uibModalInstance.close($ctrl.areaExternalId);
                };

                $ctrl.cancel = function () {
                    $uibModalInstance.dismiss('cancel');
                };
            }
        });
})();
