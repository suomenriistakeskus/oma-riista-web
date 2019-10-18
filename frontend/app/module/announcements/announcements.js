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
            controller: function ($state, ActiveRoleService, AnnouncementFormModal) {
                var $ctrl = this;
                $ctrl.canSend = false;

                $ctrl.$onInit = function () {
                    var fromOrganisationType = $ctrl.fromOrganisation.organisationType;

                    var fromClub = fromOrganisationType === 'CLUB';
                    var fromRhy = fromOrganisationType === 'RHY';
                    var fromRk = fromOrganisationType === 'RK';

                    $ctrl.canSend = ActiveRoleService.isClubContact() && fromClub ||
                        ActiveRoleService.isCoordinator() && fromRhy ||
                        ActiveRoleService.isModerator() && (fromRk || fromRhy);
                };

                $ctrl.openSendModal = function () {
                    AnnouncementFormModal.openModal($ctrl.fromOrganisation, {
                        visibleToAll: false,
                        visibleToRhyMembers: false
                    }).then(function () {
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
                                  Announcements, AnnouncementFormModal) {
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
                        return AnnouncementFormModal.openModal($ctrl.organisation, dto);
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

        .service('AnnouncementFormModal', function ($uibModal, $translate, NotificationService, ActiveRoleService,
                                                    Announcements, AnnouncementSubscriberType) {
            this.openModal = function (fromOrganisation, announcement) {
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
            };

            function ModalController($uibModalInstance, $q, TranslatedBlockUI, AvailableRoleService, ModeratorPrivileges,
                                     announcement, fromOrganisation, occupationTypeChoices) {

                var $ctrl = this;

                $ctrl.fromOrganisation = fromOrganisation;
                $ctrl.announcement = announcement;
                $ctrl.availableChoices = [];
                $ctrl.selectedOrganisations = announcement.subscriberOrganisations || [];

                var fromRiistakeskus = $ctrl.fromOrganisation.organisationType === 'RK';

                $ctrl.showOrganisationSelect = fromRiistakeskus;
                $ctrl.showVisibleToAllOption = fromRiistakeskus && (ActiveRoleService.isAdmin() ||
                    ActiveRoleService.isPrivilegedModerator(ModeratorPrivileges.bulkMessagePrivilege));
                $ctrl.showVisibleToRhyMembersOption = $ctrl.fromOrganisation.organisationType === 'RHY';

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
                    return $ctrl.announcement.visibleToAll ||
                        $ctrl.announcement.visibleToRhyMembers ||
                        (!$ctrl.occupationTypeMissing() && !$ctrl.organisationMissing());
                }

                $ctrl.submit = function () {
                    if (!recipientSelected()) {
                        return;
                    }

                    function pickOrganisation(org) {
                        return _.pick(org, ['organisationType', 'officialCode']);
                    }

                    if ($ctrl.announcement.visibleToAll || $ctrl.announcement.visibleToRhyMembers) {
                        $ctrl.selectedOrganisations = [];
                        $ctrl.announcement.occupationTypes = [];
                    }

                    $ctrl.announcement.fromOrganisation = pickOrganisation($ctrl.fromOrganisation);
                    $ctrl.announcement.subscriberOrganisations = $ctrl.showOrganisationSelect
                        ? _.map($ctrl.selectedOrganisations, pickOrganisation)
                        : [];

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

                function showSendEmailDialog() {
                    if ($ctrl.announcement.visibleToAll) {
                        // Email is not supported for sending to all
                        return $q.when(false);
                    }

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
        });
})();
