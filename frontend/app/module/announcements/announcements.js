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

                var fromOrganisationType = $ctrl.fromOrganisation.organisationType;

                $ctrl.canSend = ActiveRoleService.isClubContact() && fromOrganisationType === 'CLUB' ||
                    ActiveRoleService.isCoordinator() && fromOrganisationType === 'RHY' ||
                    ActiveRoleService.isModerator() && fromOrganisationType === 'RK';

                $ctrl.openSendModal = function () {
                    AnnouncementFormModal.openModal($ctrl.fromOrganisation, {}).then(function () {
                        $state.reload();
                    });
                };
            }
        })

        .component('announcementList', {
            templateUrl: 'announcements/list.html',
            bindings: {
                organisation: '<',
                filter: '<'
            },
            controller: function (Announcements) {
                var $ctrl = this;

                $ctrl.loadPage = function (page) {
                    Announcements.query({
                        organisationType: $ctrl.organisation.organisationType,
                        officialCode: $ctrl.organisation.officialCode,
                        direction: $ctrl.filter || 'RECEIVED',
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
            }
        })

        .filter('announcementSender', function ($filter) {
            var rI18nNameFilter = $filter('rI18nNameFilter');
            var translateFilter = $filter('translate');

            return function (announcement) {
                if (!announcement || !announcement.fromOrganisation) {
                    return '';
                }

                var result = '';
                var organisationType = announcement.fromOrganisation.organisationType;

                if (organisationType === 'CLUB' || organisationType === 'RHY') {
                    result += translateFilter('announcements.senderType.' + announcement.senderType);
                    result += ' - ';
                }

                return result + rI18nNameFilter(announcement.fromOrganisation.name);
            };
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
                $ctrl.showContent = !$ctrl.initiallyHidden;

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

                $ctrl.edit = function (announcement) {
                    Announcements.get({
                        id: announcement.id
                    }).$promise.then(function (dto) {
                        return AnnouncementFormModal.openModal($ctrl.organisation, dto);
                    }).then(function () {
                        $ctrl.refresh();
                    });
                };

                $ctrl.delete = function (announcement) {
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

        .service('AnnouncementFormModal', function ($uibModal, $translate, NotificationService,
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

            function ModalController($uibModalInstance, $q, announcement, fromOrganisation, occupationTypeChoices) {
                var $modalCtrl = this;

                $modalCtrl.fromOrganisation = fromOrganisation;
                $modalCtrl.announcement = announcement;
                $modalCtrl.availableChoices = [];
                $modalCtrl.selectedOrganisations = announcement.subscriberOrganisations || [];
                $modalCtrl.showOrganisationSelect = function () {
                    return $modalCtrl.fromOrganisation.organisationType === 'RK';
                };

                function updateOccupationTypeChoices() {
                    $modalCtrl.occupationTypeChoices = _.filter(occupationTypeChoices, function (choice) {
                        return !_.includes($modalCtrl.announcement.occupationTypes, choice.key);
                    });
                }

                updateOccupationTypeChoices();

                $modalCtrl.addOccupationType = function (occupationType) {
                    if (!occupationType) {
                        return;
                    }

                    var result = $modalCtrl.announcement.occupationTypes || [];

                    if (!_.contains(result, occupationType)) {
                        result.push(occupationType);
                    }

                    $modalCtrl.announcement.occupationTypes = result;
                    updateOccupationTypeChoices();
                };

                $modalCtrl.addOrganisation = function (rka, rhy) {
                    if (rhy) {
                        $modalCtrl.selectedOrganisations.push({
                            organisationType: 'RHY',
                            officialCode: rhy
                        });
                    } else if (rka) {
                        $modalCtrl.selectedOrganisations.push({
                            organisationType: 'RKA',
                            officialCode: rka
                        });
                    } else {
                        $modalCtrl.selectedOrganisations.push({
                            organisationType: 'RK',
                            officialCode: 850
                        });
                    }

                    $modalCtrl.selectedOrganisations = _($modalCtrl.selectedOrganisations).uniq(function (o) {
                        return o.organisationType + ':' + o.officialCode;
                    }).value();
                };

                $modalCtrl.removeOccupationType = function (occupationType) {
                    $modalCtrl.announcement.occupationTypes = _.difference($modalCtrl.announcement.occupationTypes, [occupationType]);
                    updateOccupationTypeChoices();
                };

                $modalCtrl.removeOrganisation = function (organisation) {
                    $modalCtrl.selectedOrganisations = _.difference($modalCtrl.selectedOrganisations, [organisation]);
                };

                $modalCtrl.occupationTypeMissing = function () {
                    return _.isEmpty($modalCtrl.announcement.occupationTypes);
                };

                $modalCtrl.organisationMissing = function () {
                    return $modalCtrl.showOrganisationSelect() && _.isEmpty($modalCtrl.selectedOrganisations);
                };

                $modalCtrl.canNotSubmit = function (form) {
                    return form.$invalid || $modalCtrl.occupationTypeMissing() || $modalCtrl.organisationMissing();
                };

                $modalCtrl.submit = function () {
                    if ($modalCtrl.occupationTypeMissing() || $modalCtrl.organisationMissing()) {
                        return;
                    }

                    function pickOrganisation(org) {
                        return _.pick(org, ['organisationType', 'officialCode']);
                    }

                    $modalCtrl.announcement.fromOrganisation = pickOrganisation($modalCtrl.fromOrganisation);
                    $modalCtrl.announcement.subscriberOrganisations = $modalCtrl.showOrganisationSelect()
                        ? _.map($modalCtrl.selectedOrganisations, pickOrganisation)
                        : [];

                    showSendEmailDialog()
                        .then(function (sendEmail) {
                            announcement.sendEmail = sendEmail;

                            return announcement.id
                                ? Announcements.update(announcement).$promise
                                : Announcements.save(announcement).$promise;
                        })
                        .then(function () {
                            $uibModalInstance.close($modalCtrl.announcement);
                            NotificationService.showDefaultSuccess();
                        }, function () {
                            NotificationService.showDefaultFailure();
                        });
                };

                $modalCtrl.cancel = function () {
                    $uibModalInstance.dismiss('cancel');
                };

                function showSendEmailDialog() {
                    if ($modalCtrl.fromOrganisation.organisationType !== 'CLUB') {
                        // Email is disabled
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
