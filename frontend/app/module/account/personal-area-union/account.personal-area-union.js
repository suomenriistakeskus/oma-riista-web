'use strict';

angular
    .module('app.account.personal-area-union', [])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.areas.union', {
                url: '/union',
                templateUrl: 'account/personal-area-union/personal-area-union.html',
                resolve: {
                    huntingYear: function (HuntingYearService) {
                        return HuntingYearService.getCurrent();
                    },
                    areaList: function (PersonalAreaUnions, personId, huntingYear) {
                        return PersonalAreaUnions.listPageForPerson({
                            huntingYear: huntingYear,
                            personId: personId,
                            page: 0,
                            size: 1
                        }).$promise;
                    }
                },
                controllerAs: '$ctrl',
                controller: function ($state, ActiveRoleService, AccountAreas, PersonalAreaUnions,
                                      PersonalAreaUnionFormModal, HuntingYearService,
                                      personId, areaList, huntingYear) {
                    var $ctrl = this;

                    $ctrl.$onInit = function () {
                        $ctrl.slice = areaList;
                        $ctrl.moderatorView = ActiveRoleService.isModerator();
                        $ctrl.personId = personId;
                        $ctrl.huntingYears = HuntingYearService.createHuntingYearChoices(2019, true);
                        $ctrl.huntingYear = huntingYear;
                    };

                    $ctrl.loadPage = function (page) {
                        PersonalAreaUnions.listPageForPerson({
                            personId: $ctrl.personId,
                            huntingYear: $ctrl.huntingYear,
                            page: page,
                            size: 1
                        }).$promise.then(function (res) {
                            $ctrl.slice = res;
                        });
                    };

                    $ctrl.reload = function () {
                        $ctrl.loadPage($ctrl.slice.pageable.page);
                    };

                    $ctrl.createArea = function () {
                        PersonalAreaUnionFormModal.open($ctrl.personId, {}).then(function () {
                            $state.reload();
                        }, function () {});
                    };

                }

            });
    })

    .component('personalAreaUnionList', {
        templateUrl: 'account/personal-area-union/personal-area-union-list.html',
        bindings: {
            areaList: '<',
            personId: '<',
            reloadPage: '&'
        },
        controller: function (PersonalAreaUnionFormModal, PersonalAreaUnionAddPartnerAreaModal,
                              HarvestPermitAddPartnerAreaErrorModal, PersonalAreaUnions,
                              MapPdfModal, TranslatedBlockUI, PersonalAreaUnionProcessingModal) {
            var $ctrl = this;

            $ctrl.edit = function (area) {
                PersonalAreaUnionFormModal.open($ctrl.personId, area).then(function () {
                    $ctrl.reloadPage();
                });
            };

            $ctrl.addPartner = function (area) {
                PersonalAreaUnionAddPartnerAreaModal.open(area.huntingYear, $ctrl.personId)
                    .then(function (externalId) {
                            TranslatedBlockUI.start("global.block.wait");
                            PersonalAreaUnions.addPartner({
                                areaId: area.id
                            }, {
                                areaUnionId: area.id,
                                externalId: externalId
                            }).$promise.then(function () {
                                    $ctrl.reloadPage();
                                }, HarvestPermitAddPartnerAreaErrorModal.open
                            ).finally(TranslatedBlockUI.stop);
                        }
                    );
            };

            $ctrl.removePartner = function (area, partner) {

                PersonalAreaUnions.removePartner(
                    {
                        areaId: area.id,
                        partnerId: partner
                    }
                ).$promise.then(function () {
                        $ctrl.reloadPage();
                    }, HarvestPermitAddPartnerAreaErrorModal.open
                );
            };

            $ctrl.refreshPartner = function (area, partner) {
                PersonalAreaUnions.refreshPartner({
                        areaId: area.id,
                        partnerId: partner
                    },
                    {})
                    .$promise.then(function () {
                        $ctrl.reloadPage();
                    }, HarvestPermitAddPartnerAreaErrorModal.open
                );
            };

            $ctrl.printArea = function (area) {
                MapPdfModal.printArea('/api/v1/account/area-union/' + area.id + '/print');
            };

            $ctrl.lockArea = function (area) {
                PersonalAreaUnions.setAreaReady({areaId: area.id}, {}).$promise.then(
                    function () {
                        PersonalAreaUnionProcessingModal.open(area.id).finally( function () {
                            $ctrl.reloadPage();
                        });
                    });
            };

            $ctrl.unlockArea = function (area) {
                PersonalAreaUnions.setAreaIncomplete({areaId: area.id}, {}).$promise.then(
                    function () {
                        $ctrl.reloadPage();
                    });
            };

            $ctrl.isAreaLocked = function (area) {
                return area.status !== 'INCOMPLETE';
            };
        }
    })

    .component('personalAreaUnionInfo', {
        templateUrl: 'account/personal-area-union/personal-area-union-info.html',
        bindings: {
            area: '<'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.hasNoPartnerAreas = function () {
                return $ctrl.area.partners.length === 0;
            };

            $ctrl.partnerAreaHasChanged = function () {
                return !!_.find($ctrl.area.partners, 'sourceArea.hasChanged');
            };

        }
    })

    .service('PersonalAreaUnionFormModal', function ($uibModal, PersonalAreaUnions, HuntingYearService) {
        this.open = function (personId, area) {
            return $uibModal.open({
                templateUrl: 'account/personal-area-union/personal-area-union-form.html',
                controller: ModalController,
                controllerAs: '$ctrl',
                bindToController: true,
                resolve: {
                    areaId: _.constant(area.id),
                    area: _.constant(area),
                    personId: _.constant(personId)
                }
            }).result;
        };

        function ModalController($uibModalInstance, area, areaId, personId) {
            var $ctrl = this;

            $ctrl.isUpdate = !!areaId;
            $ctrl.areaName = area.name;
            $ctrl.personId = personId;

            $ctrl.$onInit = function () {
                $ctrl.huntingYears = HuntingYearService.currentAndNextObj();
                $ctrl.selectedYear = HuntingYearService.getCurrent();
            };

            $ctrl.save = function (form) {
                if (form.$valid) {
                    var saveMethod = areaId ?
                        PersonalAreaUnions.update({areaId: areaId}, {name: $ctrl.areaName}) :
                        PersonalAreaUnions.save(
                            {personId: $ctrl.personId},
                            {name: $ctrl.areaName, huntingYear: $ctrl.selectedYear});

                    saveMethod.$promise.then(function () {
                        $uibModalInstance.close();
                    });
                }
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss();
            };
        }
    })

    .service('PersonalAreaUnionAddPartnerAreaModal', function ($uibModal) {
        this.open = function (huntingYear, personId) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/area/add-area-modal.html',
                controllerAs: '$ctrl',
                controller: 'HarvestPermitAreaAddPartnerModalController',
                size: 'lg',
                resolve: {
                    huntingYear: function () {
                        return huntingYear;
                    },
                    areaList: function () {
                        return [];
                    },
                    availableClubs: function (PersonalAreaUnions) {
                        return PersonalAreaUnions.listAvailableClubs({personId: personId}).$promise;
                    },
                    showClubs: _.constant(true)
                }
            }).result;
        };

    })


    .service('PersonalAreaUnionProcessingModal', function ($uibModal, $q, $interval, PersonalAreaUnions ) {
        this.open = function (areaId) {
            var modalInstance = $uibModal.open({
                templateUrl: 'harvestpermit/applications/mooselike/map/processing.html',
                size: 'md',
                resolve: {
                    areaId: _.constant(areaId)
                },
                controllerAs: '$ctrl',
                controller: ModalController
            });
            return modalInstance.result;
        };

        function checkStatus(areaId) {
            return PersonalAreaUnions.getAreaStatus({areaId: areaId}).$promise.then(function (res) {
                if (res.status === 'PENDING' || res.status === 'PROCESSING') {
                    return $q.reject(res.status);
                }

                return res.status === 'READY' ? $q.when(true) : $q.when(false);
            });
        }

        function ModalController($uibModalInstance, areaId) {
            var $ctrl = this;
            $ctrl.status = 'PENDING';
            $ctrl.progress = 0;
            $ctrl.progressBarWidth = 7;

            var intervalPromise = $interval(updateProgress, 1000);

            updateProgress();

            function updateProgress() {
                checkStatus(areaId).then(function (success) {
                    $interval.cancel(intervalPromise);

                    if (success) {
                        $uibModalInstance.close();
                    } else {
                        $uibModalInstance.dismiss();
                    }

                }, function (status) {
                    $ctrl.status = status;

                    if (status === 'PROCESSING') {
                        $ctrl.progressBarWidth = 7 + Math.round(93.0 *
                            (1.0 - 1.0 / Math.exp(++$ctrl.progress / 100.0)));
                    }
                });
            }
        }
    })


    .factory('PersonalAreaUnions', function ($resource) {
        var apiPrefix = 'api/v1/account/area-union/:id';

        return $resource(apiPrefix, {"id": "@id"}, {
            save: {
                url: 'api/v1/account/area-union/:personId',
                method: 'POST'
            },
            update: {
                url: 'api/v1/account/area-union/:areaId',
                method: 'PUT'
            },
            copy: {
                url: apiPrefix + '/copy',
                method: 'POST'
            },
            listPageForPerson: {
                url: 'api/v1/account/area-union/page/:personId',
                method: 'GET',
                isArray: false
            },
            listReadyUnionsForPerson: {
                url: 'api/v1/account/area-union/:personId',
                method: 'GET',
                isArray: true
            },
            getFeatures: {
                url: apiPrefix + '/features',
                method: 'GET'
            },
            saveFeatures: {
                url: apiPrefix + '/features',
                method: 'PUT'
            },
            combinedFeatures: {
                url: apiPrefix + '/combinedFeatures',
                method: 'GET'
            },
            listAvailableClubs: {
                url: apiPrefix + '/available-clubs/:personId',
                method: 'GET',
                isArray: true
            },
            addPartner: {
                url: apiPrefix + '/:areaId/partner',
                method: 'POST'
            },
            removePartner: {
                url: apiPrefix + '/:areaId/partner/:partnerId',
                method: 'DELETE'
            },
            refreshPartner: {
                url: apiPrefix + '/:areaId/partner/:partnerId',
                method: 'POST'
            },
            setAreaReady:{
                url: 'api/v1/account/area-union/:areaId/area/ready',
                method: 'POST'
            },
            setAreaIncomplete:{
                url: 'api/v1/account/area-union/:areaId/area/incomplete',
                method: 'POST'
            },
            getAreaStatus:{
                url: 'api/v1/account/area-union/:areaId/area/status',
                method: 'GET'
            }
        });
    });

