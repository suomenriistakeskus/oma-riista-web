'use strict';

angular.module('app.harvestpermit.management.contactpersons', [])
    .factory('HarvestPermitContactPersons', function ($resource) {
        return $resource('api/v1/harvestpermit/:id/contactpersons', {id: '@id'}, {
            get: {
                isArray: true
            },
            save: {
                method: 'PUT'
            }
        });
    })
    .service('EditHarvestPermitContactPersonsModal', function ($uibModal, Helpers, NotificationService,
                                                               HarvestPermitContactPersons,
                                                               PersonSearchModal, ActiveRoleService) {
        this.showModal = function (permitId) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/management/contactpersons/edit-contactpersons.html',
                resolve: {
                    permitId: _.constant(permitId),
                    contactPersons: function () {
                        return HarvestPermitContactPersons.get({id: permitId}).$promise;
                    }
                },
                size: 'lg',
                controllerAs: '$ctrl',
                controller: ModalController,
                bindToController: true
            }).result;
        };

        function ModalController($uibModalInstance, permitId, contactPersons) {
            var $ctrl = this;

            $ctrl.contactPersons = contactPersons;
            $ctrl.changes = false;

            $ctrl.cancel = function () {
                $uibModalInstance.close();
            };

            $ctrl.save = function () {
                _.forEach($ctrl.contactPersons, function (p) {
                    delete p.$isNew;
                });

                HarvestPermitContactPersons.save({
                    id: permitId
                }, $ctrl.contactPersons).$promise.then(function () {
                    $uibModalInstance.close('ok');
                    NotificationService.showDefaultSuccess();
                }, function () {
                    NotificationService.showDefaultFailure();
                });
            };

            $ctrl.removePerson = function (person) {
                _.remove($ctrl.contactPersons, 'id', person.id);
                $ctrl.changes = true;
            };

            $ctrl.addNewPerson = function () {
                PersonSearchModal.searchPerson(ActiveRoleService.isModerator(), false).then(function (personInfo) {
                    $ctrl.changes = true;

                    var existing = _.find($ctrl.contactPersons, {id: personInfo.id});

                    if (existing) {
                        existing.$isNew = true;

                    } else {
                        delete personInfo.extendedName;
                        personInfo.canBeDeleted = true;
                        personInfo.$isNew = true;

                        $ctrl.contactPersons.push(personInfo);
                    }
                }, function (failure) {
                    if (failure === 'error' || failure.status) {
                        NotificationService.showDefaultFailure();
                    }
                });
            };
        }
    });
