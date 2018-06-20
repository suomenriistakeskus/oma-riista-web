'use strict';

angular.module('app.harvestpermit.decision.document', [])
    .service('PermitDecisionSectionList', function () {
        var sections = {
            'application': {editable: false, generate: true, reference: true},
            'applicationReasoning': {editable: true, generate: true, reference: true},
            'processing': {editable: true, generate: true, reference: true},
            'decision': {editable: true, generate: true, reference: true},
            'restriction': {editable: true, generate: true, reference: true},
            'decisionReasoning': {editable: true, generate: true, reference: true},
            'execution': {editable: true, generate: false, reference: true},
            'legalAdvice': {editable: true, generate: true, reference: true},
            'notificationObligation': {editable: true, generate: true, reference: true},
            'appeal': {editable: true, generate: true, reference: true},
            'additionalInfo': {editable: true, generate: true, reference: true},
            'delivery': {editable: true, generate: true, reference: true},
            'payment': {editable: true, generate: false, reference: false},
            'administrativeCourt': {editable: true, generate: true, reference: true},
            'attachments': {editable: true, generate: true, reference: true}
        };

        this.getAll = function () {
            return _.keys(sections);
        };

        this.getTextContentEditable = function () {
            return _.chain(sections).pick(function (value) {
                return value.editable;
            }).keys().value();
        };

        this.canEditTextContent = function (sectionId) {
            return _.get(sections, [sectionId, 'editable'], false);
        };

        this.generatePossible = function (sectionId) {
            return _.get(sections, [sectionId, 'generate'], false);
        };

        this.referencePossible = function (sectionId) {
            if (sectionId === 'restrictionExtra') {
                return _.get(sections, ['restriction', 'reference'], false);
            }
            return _.get(sections, [sectionId, 'reference'], false);
        };
    })
    .service('PermitDecisionActiveSection', function (LocalStorageService, PermitDecisionSectionList) {
        var sectionIds = PermitDecisionSectionList.getAll();

        this.get = function () {
            var sectionId = LocalStorageService.getKey('selectedDecisionSection');
            if (_.isString(sectionId) && sectionIds.indexOf(sectionId) !== -1) {
                return sectionId;
            }
            return 'general';
        };

        this.set = function (sectionId) {
            LocalStorageService.setKey('selectedDecisionSection', sectionId);
        };
    })
    .component('permitDecisionDocumentNav', {
        templateUrl: 'harvestpermit/decision/document/document-nav.html',
        bindings: {
            completeStatus: '<',
            publishDate: '<',
            grantStatus: '<',
            onFocus: '&'
        },
        controller: function (PermitDecisionSectionList, PermitDecisionActiveSection) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.sectionIds = PermitDecisionSectionList.getAll();
            };

            $ctrl.isComplete = function (sectionId) {
                return !PermitDecisionSectionList.canEditTextContent(sectionId)
                    || _.get($ctrl.completeStatus, sectionId, false);
            };

            var activeSectionId = PermitDecisionActiveSection.get();

            $ctrl.isActive = function (sectionId) {
                return sectionId === activeSectionId;
            };

            $ctrl.isSectionVisible = function (sectionId) {
                if ($ctrl.grantStatus === 'REJECTED' && (sectionId === 'restriction' || sectionId === 'execution')) {
                    return false;
                }
                return true;
            };

            $ctrl.isPublishDateSet = function () {
                return !!$ctrl.publishDate;
            };

            $ctrl.focus = function (sectionId) {
                activeSectionId = sectionId;
                $ctrl.onFocus({sectionId: sectionId});
                PermitDecisionActiveSection.set(sectionId);
            };
        }
    })
    .config(function ($stateProvider) {
        $stateProvider.state('jht.decision.document', {
            url: '/document',
            templateUrl: 'harvestpermit/decision/document/document.html',
            controllerAs: '$ctrl',
            hideFooter: false,
            resolve: {
                decision: function (PermitDecision, decisionId) {
                    return PermitDecision.get({id: decisionId}).$promise;
                },
                reference: function (PermitDecision, decisionId) {
                    return PermitDecision.getReference({id: decisionId}).$promise.then(_.identity(), function () {
                        return null;
                    });
                }
            },
            controller: function (PermitDecisionSectionList, PermitDecisionActiveSection, PermitDecision,
                                  PermitDecisionDocumentEditModal, PermitDecisionChangeReferenceModal,
                                  PermitDecisionActionListModal, PermitDecisionAttachmentsModal,
                                  PermitDecisionPublishDateModal, PermitDecisionChangeAdministrativeCourtModal,
                                  PermitDecisionPaymentModal, PermitDecisionDeliveryModal, PermitDecisionSpeciesModal,
                                  PermitDecisionAdjustedAreaSize,
                                  PermitDecisionAuthoritiesModal, ActiveRoleService, $rootScope, $translate,
                                  $state, $timeout, FormPostService, NotificationService, TranslatedBlockUI, ReasonAsker,
                                  dialogs, decisionId, decision, reference) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.scrollTo = null;
                    $ctrl.previewEnabled = false;
                    $ctrl.sectionIds = PermitDecisionSectionList.getAll();
                    $ctrl.decision = decision;
                    $ctrl.reference = reference;

                    $timeout(function () {
                        $ctrl.focus(PermitDecisionActiveSection.get());
                    });
                };

                $ctrl.focus = function (sectionId) {
                    $ctrl.scrollTo = sectionId;
                };

                $ctrl.canBeCompleted = function (sectionId) {
                    var isSwedish = $ctrl.decision.locale === 'sv_FI';
                    if (sectionId === 'restriction') {
                        return _.size($ctrl.decision.document.restrictionExtra) > 0;
                    }
                    if (sectionId === 'decisionReasoning') {
                        return _.size($ctrl.decision.document.decisionReasoning) > 0;
                    }
                    if (sectionId === 'execution') {
                        return _.size($ctrl.decision.document.execution) > 0;
                    }
                    if (sectionId === 'legalAdvice') {
                        return _.size($ctrl.decision.document.legalAdvice) > (isSwedish ? 66 : 65);
                    }
                    if (sectionId === 'appeal') {
                        return _.size($ctrl.decision.document.appeal) > 0;
                    }
                    if (sectionId === 'additionalInfo') {
                        var byMail = $ctrl.decision.deliveryByMail;
                        return _.size($ctrl.decision.document.additionalInfo) > (isSwedish ?
                            (byMail ? 287 : 301) : (byMail ? 264 : 281));
                    }
                    if (sectionId === 'delivery') {
                        return _.size($ctrl.decision.document.delivery) > (isSwedish ? 23 : 17);
                    }
                    if (sectionId === 'administrativeCourt') {
                        return _.size($ctrl.decision.document.administrativeCourt) > 0;
                    }
                    if (sectionId === 'attachments') {
                        return $ctrl.decision.grantStatus === 'REJECTED' || _.size($ctrl.decision.document.attachments) > 0;
                    }
                    return true;
                };

                $ctrl.toggleComplete = function (sectionId) {
                    var completeStatus = $ctrl.decision.completeStatus;
                    var completeValue = completeStatus[sectionId];

                    PermitDecision.updateCompleteStatus({id: decisionId}, {
                        sectionId: sectionId,
                        complete: completeValue
                    }).$promise.then(function () {
                        completeStatus[sectionId] = completeValue;
                    });
                };

                $ctrl.isSectionVisible = function (sectionId) {
                    if ($ctrl.decision.grantStatus === 'REJECTED' && (sectionId === 'restriction' || sectionId === 'execution')) {
                        return false;
                    }
                    return true;
                };

                var editableFields = PermitDecisionSectionList.getTextContentEditable();

                $ctrl.canLockDecision = function () {
                    return $ctrl.isHandler() &&
                        $ctrl.decision.publishDate &&
                        $ctrl.decision.applicationStatus === 'ACTIVE' &&
                        _.all(editableFields, function (sectionId) {
                            if ($ctrl.decision.grantStatus === 'REJECTED' && (sectionId === 'restriction' || sectionId === 'execution')) {
                                return true;
                            }
                            return $ctrl.decision.completeStatus[sectionId];
                        });
                };

                $ctrl.canUnlockDecision = function () {
                    return $ctrl.isHandler() && ($ctrl.decision.status === 'LOCKED' || $ctrl.decision.status === 'PUBLISHED');
                };

                $ctrl.lockDecision = function () {
                    confirmLock().then(function () {
                        TranslatedBlockUI.start('global.block.wait');

                        PermitDecision.lock({id: decisionId}).$promise.then(function () {
                            NotificationService.showDefaultSuccess();
                            $state.go('jht.decision.revisions', {decisionId: decisionId});

                        }).finally(function () {
                            TranslatedBlockUI.stop();
                        });
                    });
                };

                $ctrl.unlockDecision = function () {
                    ReasonAsker.openModal({
                        titleKey: 'harvestpermit.decision.unlockConfirm.title',
                        messageKey: 'harvestpermit.decision.unlockConfirm.message'
                    }).then(function (reason) {
                        PermitDecision.unlock({id: decisionId, unlockReason: reason}).$promise.then(function () {
                            NotificationService.showDefaultSuccess();
                            $state.reload();
                        });
                    });
                };

                function confirmLock() {
                    var modalTitle = $translate.instant('harvestpermit.decision.lockConfirm.title');
                    var modalBody = $translate.instant('harvestpermit.decision.lockConfirm.message');

                    return dialogs.confirm(modalTitle, modalBody, {
                        size: 'md', animation: false
                    }).result;
                }

                $ctrl.changeReference = function () {
                    PermitDecisionChangeReferenceModal.open(decisionId).then(function (reference) {
                        PermitDecision.updateReference({
                            id: decisionId,
                            referenceId: reference.id
                        }).$promise.then(function () {
                            NotificationService.showDefaultSuccess();
                            $ctrl.reference = reference;
                        });
                    });
                };

                $ctrl.createAdjustedAreaAction = function () {
                    PermitDecisionAdjustedAreaSize.create(decisionId).then(function () {
                        NotificationService.showDefaultSuccess();

                        $ctrl.scrollTo = 'processing';
                        reloadDecision();
                    });
                };

                $ctrl.previewUrl = function () {
                    return '/api/v1/decision/' + decisionId + '/print/html?sectionId=' + ($ctrl.scrollTo || '');
                };

                $ctrl.isHandler = function () {
                    return $ctrl.decision.userIsHandler;
                };

                $ctrl.canEditContent = function (sectionId) {
                    return $ctrl.isHandler() && decision.status === 'DRAFT' && PermitDecisionSectionList.canEditTextContent(sectionId);
                };

                $ctrl.getSectionContent = function (sectionId) {
                    if (sectionId === 'restriction') {
                        return _([$ctrl.decision.document.restriction, $ctrl.decision.document.restrictionExtra])
                            .filter().join('\n');
                    }
                    return $ctrl.decision.document[sectionId];
                };

                $ctrl.refreshSection = function (sectionId) {
                    PermitDecision.generateText({id: decisionId, sectionId: sectionId}).$promise.then(function (res) {
                        if (!_.size(res.content)) {
                            return;
                        }

                        PermitDecision.updateDocument({id: decisionId}, {
                            sectionId: sectionId,
                            content: res.content

                        }).$promise.then(function () {
                            NotificationService.showDefaultSuccess();
                            reloadDecision();

                        }, function () {
                            NotificationService.showDefaultFailure();
                        });
                    });
                };

                $ctrl.editSection = function (sectionId) {
                    $ctrl.scrollTo = sectionId;

                    switch (sectionId) {
                        case 'general':
                            return editPublishDate();
                        case 'decision':
                            return editDecision();
                        case 'payment':
                            return editPaymentAmount();
                        case 'delivery':
                            return editDelivery();
                        case 'processing':
                            return editProcessing();
                        case 'attachments':
                            return editAttachments();
                        case 'additionalInfo':
                            return editAuthorities();
                        case 'restriction':
                            return editOtherSection('restrictionExtra');
                        default:
                            return editOtherSection(sectionId);
                    }
                };

                function editOtherSection(sectionId) {
                    var referenceContent = $ctrl.reference && $ctrl.reference.document
                        ? ($ctrl.reference.document[sectionId] || '')
                        : null;

                    editDocumentSectionAndPersist(sectionId, function (decisionId, sectionId, textContent) {
                        if (sectionId === 'administrativeCourt') {
                            return PermitDecisionChangeAdministrativeCourtModal.open(textContent, $ctrl.decision.locale);
                        }
                        return PermitDecisionDocumentEditModal.open(decisionId, sectionId, textContent, referenceContent);
                    });
                }

                function editDecision() {
                    PermitDecisionSpeciesModal.open(decisionId).then(function () {
                        reloadDecision();
                    });
                }

                function editPaymentAmount() {
                    PermitDecisionPaymentModal.open($ctrl.decision.paymentAmount).then(function (amount) {
                        PermitDecision.updatePayment({
                            id: decisionId,
                            paymentAmount: amount
                        }).$promise.then(function () {
                            NotificationService.showDefaultSuccess();
                            reloadDecision();
                        }, function () {
                            NotificationService.showDefaultFailure();
                        });
                    });
                }

                function editPublishDate() {
                    PermitDecisionPublishDateModal.open(decisionId).then(function () {
                        reloadDecision();
                    });
                }

                function editProcessing() {
                    PermitDecisionActionListModal.open(decisionId).then(function () {
                        reloadDecision();
                    });
                }

                function editDelivery() {
                    var referenceDecisionId = _.get($ctrl.reference, 'id');

                    PermitDecisionDeliveryModal.open(decisionId, referenceDecisionId)
                        .then(function (deliveries) {
                            return PermitDecision.updateDeliveries({id: decisionId, deliveries: deliveries})
                                .$promise.then(function () {
                                    NotificationService.showDefaultSuccess();
                                    reloadDecision();

                                }, function () {
                                    NotificationService.showDefaultFailure();
                                });
                        });
                }

                function editAuthorities() {
                    var referenceDecisionId = _.get($ctrl.reference, 'id');
                    var decisionLocale = $ctrl.decision.locale;

                    PermitDecisionAuthoritiesModal.open(decisionId, referenceDecisionId, decisionLocale)
                        .then(function (authorities) {
                            authorities.id = decisionId;
                            return PermitDecision.updateAuthorities(authorities)
                                .$promise.then(function () {
                                    NotificationService.showDefaultSuccess();
                                    reloadDecision();

                                }, function () {
                                    NotificationService.showDefaultFailure();
                                });
                        });
                }

                function editAttachments() {
                    PermitDecisionAttachmentsModal.open(decisionId).then(function () {
                        reloadDecision();
                    });
                }

                function editDocumentSectionAndPersist(sectionId, editCallback) {
                    reloadSectionContent(sectionId).then(function (textContent) {
                        editCallback(decisionId, sectionId, textContent).then(function (editedTextContent) {
                            PermitDecision.updateDocument({id: decisionId}, {
                                sectionId: sectionId,
                                content: editedTextContent

                            }).$promise.then(function () {
                                NotificationService.showDefaultSuccess();
                                reloadDecision();

                            }, function () {
                                NotificationService.showDefaultFailure();
                            });
                        });
                    });
                }

                function reloadSectionContent(sectionId) {
                    return PermitDecision.getDocument({id: decisionId}).$promise.then(function (doc) {
                        return doc[sectionId] || '';
                    });
                }

                function reloadDecision() {
                    PermitDecision.get({id: decisionId}).$promise.then(function (res) {
                        $ctrl.decision = res;
                    });
                }
            }
        });
    })
    .service('PermitDecisionDocumentEditModal', function ($uibModal) {
        this.open = function (decisionId, sectionId, textContent, referenceContent) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/document/edit-modal.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'lg',
                resolve: {
                    decisionId: _.constant(decisionId),
                    sectionId: _.constant(sectionId),
                    textContent: _.constant(textContent),
                    referenceContent: _.constant(referenceContent)
                }
            }).result;
        };

        function ModalController($uibModalInstance, $timeout, PermitDecision, PermitDecisionSectionList,
                                 sectionId, decisionId, textContent, referenceContent) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.sectionId = sectionId;
                $ctrl.textContent = (textContent || '').trim();
                $ctrl.generatePossible = PermitDecisionSectionList.generatePossible(sectionId);
                $ctrl.referenceEnabled = _.isString(referenceContent) && PermitDecisionSectionList.referencePossible(sectionId);
                $ctrl.referenceShown = $ctrl.referenceEnabled;
                $ctrl.referenceContent = referenceContent || '';
                $ctrl.splitView = !!referenceContent;
                $ctrl.showDiff = false;
                $ctrl.contentChanged();

            };

            $ctrl.overwriteWithReference = function () {
                $ctrl.textContent = $ctrl.referenceContent;
                $ctrl.contentChanged();
            };

            $ctrl.generateText = function () {
                PermitDecision.generateText({id: decisionId, sectionId: sectionId}).$promise.then(function (res) {
                    $ctrl.textContent = res.content;
                    $ctrl.contentChanged();
                });
            };

            $ctrl.save = function () {
                $uibModalInstance.close($ctrl.textContent);
            };

            $ctrl.toggleReference = function () {
                $ctrl.referenceShown = !$ctrl.referenceShown;
                $ctrl.splitView = !$ctrl.splitView;
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };

            $ctrl.contentChanged = function () {
                updateDiff();
            };

            function updateDiff() {
                if (!$ctrl.showDiff) {
                    return;
                }
                var diff = JsDiff.diffWordsWithSpace($ctrl.referenceContent, $ctrl.textContent);
                if (diff && diff.length === 1 && !diff[0].added && !diff[0].removed) {
                    $ctrl.diff = '<strong>Ei eroa referenssiin</strong>';
                    return;
                }

                var nodes = '';
                for (var i = 0; i < diff.length; i++) {
                    var node;
                    if (diff[i].removed) {
                        node = '<span class="del" title="Poistettu">' + _makeSpacesVisible(diff[i].value) + '</span>';
                    } else if (diff[i].added) {
                        node = '<span class="ins" title="Lisätty">' + _makeSpacesVisible(diff[i].value) + '</span>';
                    } else {
                        node = diff[i].value;
                    }
                    nodes += node;
                }
                $ctrl.diff = _makeNewLinesVisible(nodes);
            }

            function _makeSpacesVisible(str) {
                // This is to handle changes in whitespace
                // Make sure there really is a space before nbsp, otherwise text will not wrap as expected
                return str.replace(/ /gi, ' &nbsp;');
            }

            function _makeNewLinesVisible(str) {
                return str.replace(/\n/gi, '&nbsp;<br>');
            }
        }
    })
    .service('PermitDecisionPublishDateModal', function ($uibModal, PermitDecision) {
        this.open = function (decisionId) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/document/select-publish-settings.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'md',
                resolve: {
                    decisionId: _.constant(decisionId),
                    data: function () {
                        return PermitDecision.getPublishSettings({id: decisionId}).$promise;
                    }
                }
            }).result.then(function (dto) {
                return PermitDecision.updatePublishSettings({id: decisionId}, dto).$promise;
            });
        };

        function ModalController($uibModalInstance, decisionId, data) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.publishDate = data.publishDate;
                $ctrl.publishTime = data.publishTime || '08:00';
                $ctrl.locale = data.locale;
                $ctrl.localeOptions = [{
                    code: 'fi_FI', name: 'suomi'
                }, {
                    code: 'sv_FI', name: 'ruotsi'
                }];
            };

            $ctrl.save = function () {
                $uibModalInstance.close({
                    decisionId: decisionId,
                    publishDate: $ctrl.publishDate,
                    publishTime: $ctrl.publishTime,
                    locale: $ctrl.locale
                });
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }
    })
    .service('PermitDecisionChangeReferenceModal', function ($uibModal) {
        this.open = function (decisionId) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/document/select-reference-modal.html',
                size: 'lg',
                controllerAs: '$ctrl',
                controller: ModalController,
                resolve: {
                    decisionId: _.constant(decisionId),
                    availableSpecies: function (MooselikeSpecies) {
                        return MooselikeSpecies.getPermitBased();
                    },
                    handlers: function (HarvestPermitApplications) {
                        return HarvestPermitApplications.listHandlers().$promise;
                    }
                }
            }).result;
        };

        function ModalController($uibModalInstance, HuntingYearService, ApplicationStatusList, PermitDecision,
                                 decisionId, availableSpecies, handlers) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.statusList = ApplicationStatusList.decision();
                $ctrl.availableSpecies = availableSpecies;
                $ctrl.handlers = handlers;

                $ctrl.filters = {};

                var beginYear = 2018;
                var endYear = new Date().getFullYear();
                var years = _.range(beginYear, endYear + 1);

                $ctrl.huntingYears = _.map(years, function (year) {
                    return HuntingYearService.toObj(year);
                });

                $ctrl.previewEnabledId = null;
            };

            $ctrl.search = function () {
                var f = angular.copy($ctrl.filters);
                if (f.statusText) {
                    f.status = [f.statusText];
                } else {
                    f.status = null;
                }
                delete f.statusText;

                PermitDecision.searchReferences(f).$promise.then(function (res) {
                    $ctrl.results = _.filter(res, function (r) {
                        return r.id !== decisionId;
                    });
                });
            };

            $ctrl.togglePreview = function (id) {
                if ($ctrl.previewEnabledId === id) {
                    $ctrl.previewEnabledId = null;
                } else {
                    $ctrl.previewEnabledId = id;
                }
            };

            $ctrl.previewUrl = function (id) {
                return '/api/v1/decision/' + id + '/print/html';
            };


            $ctrl.resolveUnifiedStatus = function (application) {
                if (application.status === 'AMENDING') {
                    return 'AMENDING';
                }

                if (application.status === 'ACTIVE' && !application.handler) {
                    return 'ACTIVE';
                }
                return application.decisionStatus;
            };


            $ctrl.selectReference = function (reference) {
                $uibModalInstance.close(reference);
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }
    })
    .constant('PermitDecisionAdminstrativeCourts', [{
        fi: [
            'Postiosoite: Helsingin hallinto-oikeus, Radanrakentajantie 5, 00520 Helsinki',
            'Käyntiosoite: Helsingin hallinto-oikeus, Tuomioistuimet-talo, Radanrakentajantie 5, Helsinki',
            'Sähköposti: helsinki.hao@oikeus.fi',
            'Puhelin: 029 56 42069 Faksi: 029 56 42079'].join('<br>'),
        sv: [
            'Postadress: Helsingfors förvaltningsdomstol, Banbyggarvägen 5, 00520 Helsingfors',
            'Besöksadress:Helsingfors förvaltningsdomstol, Domstolarnas hus, Banbyggarvägen 5, Helsingfors',
            'E-post: helsinki.hao@oikeus.fi',
            'Telefonnummer: 029 56 42069 Fax: 029 56 42079'].join('<br>')
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
            'Puhelin: 029 56 42500 Faksi: 029 56 42501'].join('<br>'),
        sv: [
            'Postadress: Östra Finlands förvaltningsdomstol, PL 1744, 70101 Kuopio',
            'Besöksadress:Östra Finlands förvaltningsdomstol, Minna Canthin katu 64, Kuopio',
            'E-post: ita-suomi.hao@oikeus.fi',
            'Telefonnummer: 029 56 42500 Fax: 029 56 42501'].join('<br>')
    }, {
        fi: [
            'Postiosoite: Pohjois-Suomen hallinto-oikeus, PL 189, 90101 Oulu',
            'Käyntiosoite: Pohjois-Suomen hallinto-oikeus, Isokatu 4, Oulu',
            'Sähköposti: pohjois-suomi.hao@oikeus.fi',
            'Puhelin: 029 56 42800 Faksi: 029 56 42841'].join('<br>'),
        sv: [
            'Postadress: Pohjois-Suomen hallinto-oikeus, PL 189, 90101 Oulu',
            'Besöksadress: Pohjois-Suomen hallinto-oikeus, Isokatu 4, Oulu',
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
            'Käyntiosoite: Vaasan hallinto-oikeus, Korsholmanpuistikko 43, Vaasa',
            'Sähköposti: vaasa.hao@oikeus.fi',
            'Puhelin: 029 56 42780 Faksi: 029 56 42760'].join('<br>'),
        sv: [
            'Postadress: Vasa förvaltningsdomstol, PB 204, 65101 Vasa',
            'Besöksadress: Vasa förvaltningsdomstol, Korsholmsesplanaden 43, Vasa',
            'E-post: vaasa.hao@oikeus.fi',
            'Telefonnummer: 029 56 42780 Fax: 029 56 42760'].join('<br>')
    }])
    .service('PermitDecisionChangeAdministrativeCourtModal', function ($uibModal, $translate,
                                                                       PermitDecisionAdminstrativeCourts) {
        var courts = PermitDecisionAdminstrativeCourts;

        this.open = function (selectedCourt, locale) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/document/select-administrative-court-modal.html',
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
    })
    .service('PermitDecisionPaymentModal', function ($uibModal) {
        this.open = function (currentAmount) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/document/select-payment-modal.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                resolve: {
                    amount: _.constant(currentAmount)
                }
            }).result;
        };

        function ModalController($uibModalInstance, amount) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.amount = amount;
                $ctrl.amounts = [0, 90];//TODO define prices by permit type
            };

            $ctrl.save = function () {
                $uibModalInstance.close($ctrl.amount);
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }
    })
    .service('PermitDecisionDeliveryModal', function ($uibModal, PermitDecision) {
        this.open = function (decisionId, referenceId) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/document/select-delivery-modal.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'lg',
                resolve: {
                    decisionId: _.constant(decisionId),
                    deliveries: function () {
                        return PermitDecision.getDeliveries({id: decisionId}).$promise;
                    },
                    referenceReliveries: function () {
                        return referenceId ? PermitDecision.getDeliveries({id: referenceId}).$promise : null;
                    },
                    rkaDeliveryList: function (DecisionRkaRecipient) {
                        return DecisionRkaRecipient.query().$promise;
                    }
                }
            }).result;
        };

        function ModalController($uibModalInstance, $filter,
                                 decisionId, deliveries, referenceReliveries, rkaDeliveryList) {
            var $ctrl = this;

            var rI18nNameFilter = $filter('rI18nNameFilter');

            function calculateAvailableDeliveries() {
                $ctrl.availableDeliveries = _(rkaDeliveryList)
                    .map(function (delivery) {
                        return {
                            rka: rI18nNameFilter(delivery.rka),
                            name: rI18nNameFilter(delivery),
                            email: delivery.email
                        };
                    })
                    .filter(function (delivery) {
                        return delivery.rka === $ctrl.rka;
                    })
                    .filter(function (delivery) {
                        return !_.find($ctrl.deliveries, function (d) {
                            return d.name === delivery.name;
                        });
                    })
                    .sortBy('name')
                    .value();
            }

            $ctrl.$onInit = function () {
                $ctrl.selectedTab = 'a';

                $ctrl.deliveries = deliveries;
                $ctrl.referenceReliveries = referenceReliveries;
                $ctrl.typeaheadModel = null;

                $ctrl.referenceEnabled = referenceReliveries && referenceReliveries.length;
                $ctrl.referenceContent = referenceReliveries;

                $ctrl.fromList = true;
                $ctrl.adhoc = {};

                $ctrl.rkas = _(rkaDeliveryList).map(function (d) {
                    return rI18nNameFilter(d.rka);
                }).uniq().sort().value();

                calculateAvailableDeliveries();
            };

            $ctrl.overwriteWithReference = function () {
                $ctrl.deliveries = referenceReliveries;
            };

            $ctrl.rkaSelected = function () {
                calculateAvailableDeliveries();
            };

            $ctrl.addAdhoc = function () {
                $ctrl.add($ctrl.adhoc);
                $ctrl.adhoc = {};
            };

            $ctrl.add = function (item) {
                if (item) {
                    var delivery = angular.copy(item);
                    delete delivery.rka;
                    if (!_.find($ctrl.deliveries, delivery)) {
                        $ctrl.deliveries.push(delivery);
                    }
                    calculateAvailableDeliveries();
                }
            };

            $ctrl.remove = function (delivery) {
                _.remove($ctrl.deliveries, delivery);
                calculateAvailableDeliveries();
            };

            $ctrl.save = function () {
                $uibModalInstance.close($ctrl.deliveries);
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }
    })
    .component('permitDecisionDeliveryListing', {
        templateUrl: 'harvestpermit/decision/document/delivery-listing.html',
        bindings: {
            deliveries: '<',
            remove: '&'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                var removeFn = $ctrl.remove();
                $ctrl.editable = !!removeFn;

                $ctrl.remove = function (delivery) {
                    removeFn(delivery);
                };
            };
        }
    })
    .service('PermitDecisionAuthoritiesModal', function ($uibModal, PermitDecision) {
        this.open = function (decisionId, referenceId, locale) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/document/select-authorities-modal.html',
                controllerAs: '$ctrl',
                controller: ModalController,
                size: 'lg',
                resolve: {
                    decisionId: _.constant(decisionId),
                    decisionAuthorities: function () {
                        return PermitDecision.getAuthorities({id: decisionId}).$promise;
                    },
                    referenceAuthorities: function () {
                        return referenceId ? PermitDecision.getAuthorities({id: referenceId}).$promise : null;
                    },
                    rkaAuthorities: function (DecisionRkaAuthority) {
                        var swedishLocale = locale === 'sv_FI';

                        return DecisionRkaAuthority.listByDecision({decisionId: decisionId}).$promise.then(function(res) {
                            return _.map(res, function (a) {
                                var res = _.pick(a, ['firstName', 'lastName', 'phoneNumber', 'email']);
                                res.title = swedishLocale ? a.titleSwedish : a.titleFinnish;
                                return res;
                            });
                        });
                    }
                }
            }).result;
        };

        function ModalController($uibModalInstance, $translate,
                                 decisionId, decisionAuthorities, rkaAuthorities, referenceAuthorities) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.selectedTab = 'a';
                $ctrl.decisionAuthorities = decisionAuthorities;
                $ctrl.rkaAuthorities = rkaAuthorities;
                $ctrl.referenceEnabled = referenceAuthorities && (referenceAuthorities.presenter || referenceAuthorities.decisionMaker);
                $ctrl.referenceAuthorities = referenceAuthorities;
                $ctrl.selectedAuthorities = {
                    presenter: null,
                    decisionMaker: null
                };
                updateAvailableAuthorities();
            };

            $ctrl.save = function () {
                $uibModalInstance.close($ctrl.decisionAuthorities);
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };

            $ctrl.formatAuthority = function (a) {
                return a === null ? '' : a.firstName + ' ' + a.lastName + ', ' + a.title + ', ' + a.phoneNumber + ', ' + a.email;
            };

            $ctrl.onPresenterSelected = function (presenter) {
                if (presenter) {
                    $ctrl.decisionAuthorities.presenter = angular.copy(presenter);
                    $ctrl.selectedAuthorities.presenter = null;
                    updateAvailableAuthorities();
                }
            };

            $ctrl.onDecisionMakerSelected = function (decisionMaker) {
                if (decisionMaker) {
                    $ctrl.decisionAuthorities.decisionMaker = angular.copy(decisionMaker);
                    $ctrl.selectedAuthorities.decisionMaker = null;
                    updateAvailableAuthorities();
                }
            };

            $ctrl.clear = function (attr) {
                $ctrl.decisionAuthorities[attr] = null;
                updateAvailableAuthorities();
            };

            $ctrl.overwriteWithReference = function () {
                $ctrl.decisionAuthorities.presenter = $ctrl.referenceAuthorities.presenter;
                $ctrl.decisionAuthorities.decisionMaker = $ctrl.referenceAuthorities.decisionMaker;
                updateAvailableAuthorities();
            };

            function updateAvailableAuthorities() {
                $ctrl.availableRkaAuthorities = _.filter($ctrl.rkaAuthorities, function (a) {
                    var aa = $ctrl.formatAuthority(a);
                    return aa !== $ctrl.formatAuthority($ctrl.decisionAuthorities.presenter) &&
                        aa !== $ctrl.formatAuthority($ctrl.decisionAuthorities.decisionMaker);
                });
            }
        }
    })
;

