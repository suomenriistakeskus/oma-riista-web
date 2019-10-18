'use strict';

angular.module('app.harvestpermit.area', [])
    .component('harvestPermitAreaPartnerAreaList', {
        templateUrl: 'harvestpermit/area/permit-area-partner-list.html',
        bindings: {
            partners: '<',
            onRemovePartner: '&',
            onRefreshPartner: '&',
            isLocked: '<'
        },
        controller: function ($translate, dialogs) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.partners = $ctrl.partners || [];
                $ctrl.isLocked = $ctrl.isLocked || false;
            };

            $ctrl.deletePartner = function (id) {
                var dialogTitle = $translate.instant('harvestpermit.wizard.partners.deleteConfirmation.title');
                var dialogMessage = $translate.instant('harvestpermit.wizard.partners.deleteConfirmation.body');
                var dialog = dialogs.confirm(dialogTitle, dialogMessage);

                dialog.result.then(function () {
                    $ctrl.onRemovePartner({id: id});
                });
            };

            $ctrl.updateGeometry = function (id) {
                $ctrl.onRefreshPartner({id: id});
            };

        }
    })
    .controller('HarvestPermitAreaAddPartnerModalController', function ($uibModalInstance, $translate, $filter, CarnivorePermitApplication,
                                                                        ClubAreas,
                                                                        dialogs, areaList, availableClubs, huntingYear, showClubs) {
        var $ctrl = this;
        var i18n = $filter('rI18nNameFilter');

        $ctrl.$onInit = function () {
            $ctrl.availableClubs = _.map(availableClubs).map(function (club) {
                return {
                    id: club.id,
                    name: i18n(club)
                };
            });
            $ctrl.clubAreas = [];
            $ctrl.areaList = areaList;
            $ctrl.externalId = null;
            $ctrl.selectedClub = _.first($ctrl.availableClubs);
            $ctrl.showClubs = showClubs;
        };

        $ctrl.close = function () {
            $uibModalInstance.close($ctrl.externalId);
        };

        $ctrl.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };

        $ctrl.reloadAreas = function () {
            $ctrl.externalId = null;
            fetchHuntingClubAreas($ctrl.selectedClub);
        };

        function fetchHuntingClubAreas(huntingClubId) {
            var i18n = $filter('rI18nNameFilter');

            $ctrl.availableHuntingClubAreas = [];
            $ctrl.huntingClubAreaId = null;
            $ctrl.areaExternalId = null;
            if (!_.isFinite(huntingYear) || !_.isFinite(huntingClubId)) {
                return;
            }

            return ClubAreas.query({
                year: huntingYear,
                clubId: huntingClubId,
                activeOnly: true,
                includeEmpty: false

            }).$promise.then(function (result) {
                $ctrl.clubAreas = _.map(result, function (area) {
                    return {
                        id: area.id,
                        name: i18n(area),
                        externalId: area.externalId
                    };
                });
            });
        }

    })
    .service('HarvestPermitAddPartnerAreaErrorModal', function ($uibModal) {
        this.open = function () {
            $uibModal.open({
                templateUrl: 'harvestpermit/area/add-area-error-modal.html'
            });
        };
    });
