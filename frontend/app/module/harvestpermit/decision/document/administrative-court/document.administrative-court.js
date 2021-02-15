'use strict';

angular.module('app.harvestpermit.decision.document.administrativecourt', [])
    .config(function ($stateProvider) {
        $stateProvider.state('jht.decision.document.administrative-court', {
            url: '/administrative-court',
            templateUrl: 'harvestpermit/decision/document/administrative-court/document.administrative-court.html',
            controllerAs: '$ctrl',
            controller: function (PermitDecision, PermitDecisionUtils, PermitDecisionSection,
                                  NotificationService, RefreshDecisionStateService,
                                  PermitDecisionChangeAdministrativeCourtModal,
                                  decision, reference) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.sectionId = PermitDecisionSection.ADMINISTRATIVE_COURT;
                    $ctrl.decision = decision;
                    $ctrl.reference = reference;
                    $ctrl.sectionContent = PermitDecisionUtils.getSectionContent(decision, $ctrl.sectionId);
                };

                $ctrl.canEditContent = function () {
                    return PermitDecisionUtils.canEditContent(decision, $ctrl.sectionId);
                };

                $ctrl.editSection = function () {
                    PermitDecisionUtils.reloadSectionContent($ctrl.sectionId, $ctrl.decision.id).then(function (textContent) {
                        return editContent(textContent).then(storeTextContent);
                    });
                };

                function editContent(textContent) {
                    return PermitDecisionChangeAdministrativeCourtModal.open(textContent, $ctrl.decision.locale);
                }

                function storeTextContent(editedTextContent) {
                    PermitDecision.updateDocument({id: $ctrl.decision.id}, {
                        sectionId: $ctrl.sectionId,
                        content: editedTextContent

                    }).$promise.then(function () {
                        NotificationService.showDefaultSuccess();

                        RefreshDecisionStateService.refresh();

                    }, function () {
                        NotificationService.showDefaultFailure();
                    });
                }
            }
        });
    })

    .constant('PermitDecisionAdminstrativeCourts', [{
        fi: [
            'Postiosoite: Helsingin hallinto-oikeus, Radanrakentajantie 5, 00520 Helsinki',
            'Käyntiosoite: Helsingin hallinto-oikeus, Radanrakentajantie 5, Helsinki',
            'Sähköposti: helsinki.hao@oikeus.fi',
            'Puhelin: 029 56 42000 Faksi: 029 56 42079'].join('<br>'),
        sv: [
            'Postadress: Helsingfors förvaltningsdomstol, Banbyggarvägen 5, 00520 Helsingfors',
            'Besöksadress:Helsingfors förvaltningsdomstol, Banbyggarvägen 5, Helsingfors',
            'E-post: helsinki.hao@oikeus.fi',
            'Telefonnummer: 029 56 42000 Fax: 029 56 42079'].join('<br>')
    }, {
        fi: [
            'Postiosoite: Hämeenlinnan hallinto-oikeus, Raatihuoneenkatu 1, 13100 Hämeenlinna',
            'Käyntiosoite: Hämeenlinnan hallinto-oikeus, Hämeenlinnan oikeustalo, Arvi Kariston katu 5, Hämeenlinna',
            'Sähköposti: hameenlinna.hao@oikeus.fi',
            'Puhelin: 029 56 42210 Faksi: 029 56 42269'].join('<br>'),
        sv: [
            'Postadress: Tavastehus förvaltningsdomstol, Raatihuoneenkatu 1, 13100 Tavastehus',
            'Besöksadress: Tavastehus förvaltningsdomstol, Hämeenlinnan oikeustalo, Arvi Kariston katu 1, Tavastehus',
            'E-post: hameenlinna.hao@oikeus.fi',
            'Telefonnummer: 029 56 42210 Fax: 029 56 42269'].join('<br>')
    }, {
        fi: [
            'Postiosoite: Itä-Suomen hallinto-oikeus, PL 1744, 70101 Kuopio',
            'Käyntiosoite: Itä-Suomen hallinto-oikeus, Minna Canthin katu 64, Kuopio',
            'Sähköposti: ita-suomi.hao@oikeus.fi',
            'Puhelin: 029 56 42502 Faksi: 029 56 42501'].join('<br>'),
        sv: [
            'Postadress: Östra Finlands förvaltningsdomstol, PL 1744, 70101 Kuopio',
            'Besöksadress:Östra Finlands förvaltningsdomstol, Minna Canthin katu 64, Kuopio',
            'E-post: ita-suomi.hao@oikeus.fi',
            'Telefonnummer: 029 56 42502 Fax: 029 56 42501'].join('<br>')
    }, {
        fi: [
            'Postiosoite: Pohjois-Suomen hallinto-oikeus, PL 189, 90101 Oulu',
            'Käyntiosoite: Pohjois-Suomen hallinto-oikeus, Isokatu 4, 3 krs., Oulu',
            'Sähköposti: pohjois-suomi.hao@oikeus.fi',
            'Puhelin: 029 56 42800 Faksi: 029 56 42841'].join('<br>'),
        sv: [
            'Postadress: Norra Finlands förvaltningsdomstol, PL 189, 90101 Oulu',
            'Besöksadress: Norra Finlands förvaltningsdomstol, Isokatu 4, 3 vån., Oulu',
            'E-post: pohjois-suomi.hao@oikeus.fi',
            'Telefonnummer: 029 56 42800 Fax: 029 56 42841'].join('<br>')
    }, {
        fi: [
            'Postiosoite: Turun hallinto-oikeus, PL 32, 20101 Turku',
            'Käyntiosoite: Turun hallinto-oikeus, Sairashuoneenkatu 2-4, Turku',
            'Sähköposti: turku.hao@oikeus.fi',
            'Puhelin: 029 56 42410 Faksi: 029 56 42414'].join('<br>'),
        sv: [
            'Postadress: Åbo förvaltningsdomstol, PB 32, 20101 Åbo',
            'Besöksadress: Åbo förvaltningsdomstol, Lasarettsgatan 2-4, Åbo',
            'E-post: turku.hao@oikeus.fi',
            'Telefonnummer: 029 56 42410 Fax: 029 56 42414'].join('<br>')
    }, {
        fi: [
            'Postiosoite: Vaasan hallinto-oikeus, PL 204, 65101 Vaasa',
            'Käyntiosoite: Vaasan hallinto-oikeus, Korsholmanpuistikko 43, 4. krs., Vaasa',
            'Sähköposti: vaasa.hao@oikeus.fi',
            'Puhelin: 029 56 42780 Faksi: 029 56 42760'].join('<br>'),
        sv: [
            'Postadress: Vasa förvaltningsdomstol, PB 204, 65101 Vasa',
            'Besöksadress: Vasa förvaltningsdomstol, Korsholmsesplanaden 43, 4. vån., Vasa',
            'E-post: vaasa.hao@oikeus.fi',
            'Telefonnummer: 029 56 42780 Fax: 029 56 42760'].join('<br>')
    }])
    .service('PermitDecisionChangeAdministrativeCourtModal', function ($uibModal, $translate,
                                                                       PermitDecisionAdminstrativeCourts) {
        var courts = PermitDecisionAdminstrativeCourts;

        this.open = function (selectedCourt, locale) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/document/administrative-court/select-administrative-court-modal.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                resolve: {
                    selectedCourt: _.constant(selectedCourt),
                    locale: _.constant(locale)
                }
            }).result;
        };

        function ModalController($uibModalInstance, selectedCourt, locale) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.selectedCourt = selectedCourt;

                var swedishLocale = locale === 'sv_FI';

                $ctrl.courts = _.map(courts, function (c) {
                    return swedishLocale ? c.sv : c.fi;
                });
            };

            $ctrl.save = function () {
                $uibModalInstance.close($ctrl.selectedCourt);
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }
    });
