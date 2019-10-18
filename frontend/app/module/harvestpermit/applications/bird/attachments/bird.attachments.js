'use strict';

angular.module('app.harvestpermit.application.bird.attachments', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.permitwizard.bird.attachments', {
                url: '/attachments',
                templateUrl: 'harvestpermit/applications/bird/attachments/attachments.html',
                controller: 'BirdPermitWizardAttachmentsController',
                controllerAs: '$ctrl',
                resolve: {
                    attachmentList: function (BirdPermitApplication, applicationId) {
                        return BirdPermitApplication.listAttachments({
                            id: applicationId,
                            typeFilter: 'OTHER'
                        }).$promise.then(function (res) {
                            return _.sortBy(res, 'id');
                        });
                    }
                }
            })
            .state('jht.decision.application.wizard.bird.attachments', {
                url: '/attachments',
                templateUrl: 'harvestpermit/applications/bird/attachments/attachments.html',
                controller: 'BirdPermitWizardAttachmentsController',
                controllerAs: '$ctrl',
                resolve: {
                    attachmentList: function (BirdPermitApplication, applicationId) {
                        return BirdPermitApplication.listAttachments({
                            id: applicationId,
                            typeFilter: 'OTHER'
                        }).$promise.then(function (res) {
                            return _.sortBy(res, 'id');
                        });
                    }
                }
            });
    })

    .controller('BirdPermitWizardAttachmentsController', function ($q, $http, $translate, dialogs,
                                                                   BirdPermitApplication,
                                                                   wizard, applicationId, attachmentList) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.attachmentList = attachmentList;
            $ctrl.attachmentBaseUri = '/api/v1/harvestpermit/application/' + applicationId + '/attachment';
            $ctrl.attachmentIdToRemove = [];
        };

        $ctrl.exit = function () {
            save().then(function () {
                wizard.exit();
            });
        };

        $ctrl.previous = function () {
            save().then(function () {
                wizard.goto('population');
            });
        };

        $ctrl.previousDisabled = function (form) {
            return form.$invalid;
        };

        $ctrl.next = function () {
            save().then(function () {
                wizard.goto('summary');
            });
        };

        $ctrl.nextDisabled = function (form) {
            return form.$invalid;
        };

        $ctrl.attachmentUploadComplete = function (fileName, response) {
            $ctrl.attachmentList.push({
                id: response.id,
                name: fileName,
                additionalInfo: ''
            });
        };

        $ctrl.removeAttachment = function (attachment) {
            var modalTitle = $translate.instant('harvestpermit.wizard.attachments.deleteConfirmation.title');
            var modalBody = $translate.instant('harvestpermit.wizard.attachments.deleteConfirmation.body');

            dialogs.confirm(modalTitle, modalBody).result.then(function () {
                var indexToRemove = _.findIndex($ctrl.attachmentList, ['id', attachment.id]);

                if (indexToRemove >= 0) {
                    $ctrl.attachmentList.splice(indexToRemove, 1);
                    $ctrl.attachmentIdToRemove.push(attachment.id);
                }
            });
        };

        function save() {
            var requestList = _.map($ctrl.attachmentList, function (a) {
                return {
                    id: a.id,
                    additionalInfo: a.additionalInfo
                };
            });

            return BirdPermitApplication.updateAttachmentAdditionalInfo({id: applicationId}, {
                list: requestList

            }).$promise.then(function () {
                var promiseArray = _.map($ctrl.attachmentIdToRemove, function (id) {
                    return $http.delete($ctrl.attachmentBaseUri + '/' + id);
                });

                return $q.all(promiseArray);
            });
        }
    });
