(function () {
    'use strict';

    angular.module('app.announcements', [])
        .factory('Announcements', function ($resource) {
            return $resource('api/v1/announcements/:id', {"id": "@id"}, {
                query: {method: 'GET', isArray: false},
                update: {method: 'PUT'}
            });
        })

        .service('AnnouncementSubscriberType', function ($http, $translate) {
            this.getOccupationTypeChoices = function (fromOrganisationType) {
                return $http({
                    method: 'GET',
                    url: 'api/v1/announcements/occupationTypes',
                    params: {
                        fromOrganisationType: fromOrganisationType
                    }
                }).then(function (result) {
                    return _(result.data)
                        .map(function (value, key) {
                            return _.map(value, function (occupationType) {
                                return {
                                    key: occupationType,
                                    group: $translate.instant('global.organisationType.' + key),
                                    value: $translate.instant('occupation.type.' + occupationType)
                                };
                            });
                        })
                        .flatten()
                        .value();
                });
            };
        })

        .component('announcementSendButton', {
            templateUrl: 'announcements/send-button.html',
            bindings: {
                fromOrganisation: '<'
            },
            controller: function ($state, ActiveRoleService, AnnouncementFormModal, ModeratorPrivileges) {
                var $ctrl = this;
                $ctrl.canSend = false;

                $ctrl.$onInit = function () {
                    var fromOrganisationType = $ctrl.fromOrganisation.organisationType;

                    var fromClub = fromOrganisationType === 'CLUB';
                    var fromRhy = fromOrganisationType === 'RHY';
                    var fromRk = fromOrganisationType === 'RK';

                    $ctrl.canSendToRhy = ActiveRoleService.isCoordinator() && fromRhy ||
                        ActiveRoleService.isModerator() && (fromRk || fromRhy);

                    $ctrl.canSendToAll = fromRk && (ActiveRoleService.isAdmin() ||
                        ActiveRoleService.isPrivilegedModerator(ModeratorPrivileges.bulkMessagePrivilege));

                    $ctrl.canSend = ActiveRoleService.isClubContact() && fromClub ||
                        $ctrl.canSendToRhy;

                };

                $ctrl.openSendModal = function (mode) {
                    var announcement = {
                        visibleToAll: mode === 'ALL',
                        visibleToRhyMembers: mode === 'RHY'
                    };

                    AnnouncementFormModal.openModal($ctrl.fromOrganisation, announcement, mode)
                        .then(function () {
                            $state.reload();
                        });
                };
            }
        })

        .component('announcementList', {
            templateUrl: 'announcements/list.html',
            bindings: {
                organisation: '<'
            },
            controller: function (Announcements) {
                var $ctrl = this;

                $ctrl.loadPage = function (page) {
                    Announcements.query({
                        organisationType: $ctrl.organisation.organisationType,
                        officialCode: $ctrl.organisation.officialCode,
                        page: page || 0,
                        size: 10,
                        sort: 'id,DESC'
                    }).$promise.then(function (result) {
                        $ctrl.currentPage = result;
                    });
                };

                $ctrl.refresh = function () {
                    $ctrl.loadPage(0);
                };

                $ctrl.$onInit = function () {
                    $ctrl.loadPage(0);
                };
            }
        })

        .filter('announcementSender', function ($filter) {
            var rI18nNameFilter = $filter('rI18nNameFilter');
            var translateFilter = $filter('translate');

            return function (announcement) {
                if (announcement && announcement.fromOrganisation && announcement.senderType) {
                    return getName(announcement.fromOrganisation, announcement.senderType);
                }

                return '';
            };

            function getName(fromOrganisation, senderType) {
                var organisationName = rI18nNameFilter(fromOrganisation.name);
                var organisationType = fromOrganisation.organisationType;

                if (organisationType === 'RK' || organisationType === 'RHY' && senderType === 'RIISTAKESKUS') {
                    return organisationName;
                }

                return translateFilter('announcements.senderType.' + senderType) + ' - ' + organisationName;
            }
        })

        .component('announcementListItem', {
            templateUrl: 'announcements/list-item.html',
            bindings: {
                announcement: '<',
                organisation: '<',
                showActions: '<',
                initiallyHidden: '<',
                refresh: '&'
            },
            controller: function (dialogs, $translate, $filter,
                                  Announcements, AnnouncementFormModal, AnnouncementSubscriberMode) {
                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.showContent = !$ctrl.initiallyHidden;
                };

                $ctrl.canEdit = function (announcement) {
                    var from = announcement.fromOrganisation;
                    var current = $ctrl.organisation;

                    if (!from || !current) {
                        return false;
                    } else if (from.officialCode && current.officialCode &&
                        from.organisationType && current.organisationType) {
                        return from.officialCode === current.officialCode &&
                            from.organisationType === current.organisationType;
                    }

                    return false;
                };

                $ctrl.edit = function (announcement, $event) {
                    $event.stopPropagation();
                    Announcements.get({
                        id: announcement.id
                    }).$promise.then(function (dto) {
                        var mode = AnnouncementSubscriberMode.CUSTOM;
                        if (!!dto.visibleToAll) {
                            mode = AnnouncementSubscriberMode.ALL;
                        } else if (!!dto.visibleToRhyMembers) {
                            mode = AnnouncementSubscriberMode.RHY;
                        }
                        return AnnouncementFormModal.openModal($ctrl.organisation, dto, mode);
                    }).then(function () {
                        $ctrl.refresh();
                    });
                };

                $ctrl.delete = function (announcement, $event) {
                    $event.stopPropagation();
                    dialogs.confirm().result.then(function () {
                        removeAnnouncement(announcement);
                    });
                };

                function removeAnnouncement(announcement) {
                    Announcements.remove({
                        id: announcement.id
                    }).$promise.then(function () {
                        $ctrl.refresh();
                    });
                }
            }
        })

        .constant('AnnouncementSubscriberMode', {
            ALL: 'ALL',
            RHY: 'RHY',
            CUSTOM: 'CUSTOM'
        })

        .service('AnnouncementFormModal', function ($uibModal, $translate, NotificationService, ActiveRoleService,
                                                    Announcements, AnnouncementSubscriberType, AnnouncementSubscriberMode) {
            this.openModal = function (fromOrganisation, announcement, mode) {

                switch (mode) {
                    case AnnouncementSubscriberMode.ALL:
                        return $uibModal.open({
                            templateUrl: 'announcements/form-all.html',
                            controllerAs: '$ctrl',
                            size: 'lg',
                            controller: ModalControllerForSendToAll,
                            resolve: {
                                announcement: _.constant(announcement),
                                fromOrganisation: _.constant(fromOrganisation),
                                occupationTypeChoices: function () {
                                    return AnnouncementSubscriberType.getOccupationTypeChoices(fromOrganisation.organisationType);
                                }
                            }
                        }).result;
                    case AnnouncementSubscriberMode.RHY:
                        return $uibModal.open({
                            templateUrl: 'announcements/form-rhy.html',
                            controllerAs: '$ctrl',
                            size: 'lg',
                            controller: ModalControllerForSendToRhy,
                            resolve: {
                                announcement: _.constant(announcement),
                                fromOrganisation: _.constant(fromOrganisation),
                                occupationTypeChoices: function () {
                                    return AnnouncementSubscriberType.getOccupationTypeChoices(fromOrganisation.organisationType);
                                }
                            }
                        }).result;
                    case AnnouncementSubscriberMode.CUSTOM:
                        return $uibModal.open({
                            templateUrl: 'announcements/form.html',
                            controllerAs: '$ctrl',
                            size: 'lg',
                            controller: ModalController,
                            resolve: {
                                announcement: _.constant(announcement),
                                fromOrganisation: _.constant(fromOrganisation),
                                occupationTypeChoices: function () {
                                    return AnnouncementSubscriberType.getOccupationTypeChoices(fromOrganisation.organisationType);
                                }
                            }
                        }).result;

                }
            };


            function showSendEmailDialog() {
                var modalInstance = $uibModal.open({
                    animation: false,
                    backdrop: false,
                    size: 'sm',
                    templateUrl: 'announcements/send-email.html',
                    controllerAs: '$ctrl',
                    controller: SendEmailModalController
                });

                return modalInstance.result.then(_.constant(true), _.constant(false));
            }

            function SendEmailModalController($uibModalInstance) {
                var $modalCtrl = this;

                $modalCtrl.yes = function () {
                    $uibModalInstance.close(true);
                };

                $modalCtrl.no = function () {
                    $uibModalInstance.dismiss(false);
                };
            }

            function pickOrganisation(org) {
                return _.pick(org, ['organisationType', 'officialCode']);
            }

            function ModalControllerForSendToAll($uibModalInstance, TranslatedBlockUI, announcement, fromOrganisation) {
                var $ctrl = this;

                $ctrl.fromOrganisation = fromOrganisation;
                $ctrl.announcement = announcement;

                $ctrl.canNotSubmit = function (form) {
                    return form.$invalid;
                };

                $ctrl.submit = function () {
                    $ctrl.announcement.fromOrganisation = pickOrganisation($ctrl.fromOrganisation);

                    TranslatedBlockUI.start("global.block.wait");
                    var promise = announcement.id
                        ? Announcements.update(announcement).$promise
                        : Announcements.save(announcement).$promise;

                    promise.then(function () {
                        $uibModalInstance.close($ctrl.announcement);
                        NotificationService.showDefaultSuccess();
                    }, function () {
                        NotificationService.showDefaultFailure();
                    })
                        .finally(TranslatedBlockUI.stop);

                };

                $ctrl.cancel = function () {
                    $uibModalInstance.dismiss('cancel');
                };
            }

            function ModalControllerForSendToRhy($uibModalInstance, $q, TranslatedBlockUI, AvailableRoleService, ModeratorPrivileges,
                                                 announcement, fromOrganisation) {

                var $ctrl = this;

                $ctrl.$onInit = function () {
                    $ctrl.fromOrganisation = fromOrganisation;
                    $ctrl.announcement = announcement;
                    $ctrl.rhyCode = announcement.rhyMembershipSubscriber
                        ? announcement.rhyMembershipSubscriber.officialCode
                        : null;
                    $ctrl.rkaCode = announcement.rhyMembershipSubscriber
                        ? null
                        : '000'; // Preselect area if rhy not selected already
                    var fromRiistakeskus = $ctrl.fromOrganisation.organisationType === 'RK';
                    $ctrl.showOrganisationSelection = fromRiistakeskus;

                    // Prohibit rhy selection on update
                    $ctrl.disableOrganisationSelection = !!announcement.rhyMembershipSubscriber;

                };

                $ctrl.canNotSubmit = function (form) {
                    return form.$invalid || ($ctrl.showOrganisationSelection && !$ctrl.rhyCode);
                };


                $ctrl.submit = function () {

                    $ctrl.selectedOrganisations = [];
                    $ctrl.announcement.occupationTypes = [];


                    $ctrl.announcement.fromOrganisation = pickOrganisation($ctrl.fromOrganisation);

                    // Update on create, skip on update
                    if (!$ctrl.announcement.rhyMembershipSubscriber) {
                        $ctrl.announcement.rhyMembershipSubscriber = $ctrl.showOrganisationSelection
                            ? {organisationType: 'RHY', officialCode: $ctrl.rhyCode}
                            : null;

                    }

                    showSendEmailDialog()
                        .then(function (sendEmail) {
                            announcement.sendEmail = sendEmail;
                            TranslatedBlockUI.start("global.block.wait");
                            return announcement.id
                                ? Announcements.update(announcement).$promise
                                : Announcements.save(announcement).$promise;
                        })
                        .then(function () {
                            $uibModalInstance.close($ctrl.announcement);
                            NotificationService.showDefaultSuccess();
                        }, function () {
                            NotificationService.showDefaultFailure();
                        })
                        .finally(TranslatedBlockUI.stop);
                };


                $ctrl.cancel = function () {
                    $uibModalInstance.dismiss('cancel');
                };

            }

            function ModalController($uibModalInstance, TranslatedBlockUI, AvailableRoleService,
                                     announcement, fromOrganisation, occupationTypeChoices) {

                var $ctrl = this;
                $ctrl.fromOrganisation = fromOrganisation;
                $ctrl.announcement = announcement;
                $ctrl.selectedOrganisations = announcement.subscriberOrganisations || [];

                var fromRiistakeskus = $ctrl.fromOrganisation.organisationType === 'RK';

                $ctrl.showOrganisationSelect = fromRiistakeskus;

                function updateOccupationTypeChoices() {
                    $ctrl.occupationTypeChoices = _.filter(occupationTypeChoices, function (choice) {
                        return !_.includes($ctrl.announcement.occupationTypes, choice.key);
                    });
                }

                updateOccupationTypeChoices();

                $ctrl.addOccupationType = function (occupationType) {
                    if (!occupationType) {
                        return;
                    }

                    var result = $ctrl.announcement.occupationTypes || [];

                    if (!_.includes(result, occupationType)) {
                        result.push(occupationType);
                    }

                    $ctrl.announcement.occupationTypes = result;
                    updateOccupationTypeChoices();
                };

                $ctrl.addOrganisation = function (rka, rhy) {
                    if (rhy) {
                        $ctrl.selectedOrganisations.push({
                            organisationType: 'RHY',
                            officialCode: rhy
                        });
                    } else if (rka) {
                        $ctrl.selectedOrganisations.push({
                            organisationType: 'RKA',
                            officialCode: rka
                        });
                    } else {
                        $ctrl.selectedOrganisations.push({
                            organisationType: 'RK',
                            officialCode: 850
                        });
                    }

                    $ctrl.selectedOrganisations = _($ctrl.selectedOrganisations).uniqBy(function (o) {
                        return o.organisationType + ':' + o.officialCode;
                    }).value();
                };

                $ctrl.removeOccupationType = function (occupationType) {
                    $ctrl.announcement.occupationTypes = _.difference($ctrl.announcement.occupationTypes, [occupationType]);
                    updateOccupationTypeChoices();
                };

                $ctrl.removeOrganisation = function (organisation) {
                    $ctrl.selectedOrganisations = _.difference($ctrl.selectedOrganisations, [organisation]);
                };

                $ctrl.occupationTypeMissing = function () {
                    return _.isEmpty($ctrl.announcement.occupationTypes);
                };

                $ctrl.organisationMissing = function () {
                    return $ctrl.showOrganisationSelect && _.isEmpty($ctrl.selectedOrganisations);
                };

                $ctrl.canNotSubmit = function (form) {
                    return form.$invalid || !recipientSelected();
                };

                function recipientSelected() {
                    return !$ctrl.occupationTypeMissing() && !$ctrl.organisationMissing();
                }

                $ctrl.submit = function () {
                    if (!recipientSelected()) {
                        return;
                    }

                    $ctrl.announcement.fromOrganisation = pickOrganisation($ctrl.fromOrganisation);
                    $ctrl.announcement.subscriberOrganisations = _.map($ctrl.selectedOrganisations, pickOrganisation);

                    showSendEmailDialog()
                        .then(function (sendEmail) {
                            announcement.sendEmail = sendEmail;
                            TranslatedBlockUI.start("global.block.wait");
                            return announcement.id
                                ? Announcements.update(announcement).$promise
                                : Announcements.save(announcement).$promise;
                        })
                        .then(function () {
                            $uibModalInstance.close($ctrl.announcement);
                            NotificationService.showDefaultSuccess();
                        }, function () {
                            NotificationService.showDefaultFailure();
                        })
                        .finally(TranslatedBlockUI.stop);
                };

                $ctrl.cancel = function () {
                    $uibModalInstance.dismiss('cancel');
                };

            }

        })

        .component('announcementSenderInfo', {
            templateUrl: 'announcements/sender-info.html',
            controllerAs: '$ctrl',
            bindings: {
                fromOrganisation: '<'
            }
        })

        .component('announcementMessageForm', {
            templateUrl: 'announcements/announcement-message.html',
            controllerAs: '$ctrl',
            bindings: {
                announcement: '<'
            }
        });
})();
