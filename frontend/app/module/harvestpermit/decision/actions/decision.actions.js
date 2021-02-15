'use strict';

angular.module('app.harvestpermit.decision.actions', [])
    .constant('DecisionActionConstants', {
        actionType: [
            'SELVITYSPYYNTO',
            'SELVITYS',
            'TIETOPYYNTO',
            'TIETOPYYNTOVASTAUS',
            'LAUSUNTOPYYNTO',
            'LAUSUNTO',
            'KUULEMINEN',
            'VASTASELITYSPYYNTO',
            'VASTASELITYS',
            'TAYDENNYS',
            'MUU'
        ],
        communicationTypes: ['TELEPHONE', 'EMAIL', 'MEETING', 'MAIL']
    })

    .factory('PermitDecisionAction', function ($resource) {
        var apiPrefix = 'api/v1/decision/:decisionId/action/:id';

        return $resource(apiPrefix, {id: '@id', decisionId: '@decisionId', attachmentId: '@attachmentId'}, {
            update: {method: 'PUT'},
            delete: {method: 'DELETE'},
            listAttachments: {method: 'GET', url: apiPrefix + '/attachment', isArray: true},
            getAttachment: {method: 'POST', url: apiPrefix + '/attachment/:attachmentId'},
            deleteAttachment: {method: 'DELETE', url: apiPrefix + '/attachment/:attachmentId'},
        });
    })

    .service('PermitDecisionActionReadonlyListModal', function ($uibModal, PermitDecisionActionAttachmentModal) {
        this.open = function (decisionId) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/actions/action-readonly-list.html',
                size: 'lg',
                controllerAs: '$ctrl',
                controller: ModalController,
                resolve: {
                    decisionId: _.constant(decisionId),
                    actions: function (PermitDecisionAction) {
                        return PermitDecisionAction.query({decisionId: decisionId}).$promise;
                    }
                }
            }).result;
        };

        function ModalController($uibModalInstance, decisionId, actions) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                var pageSize = 2;

                $ctrl.pager = {
                    data: actions,
                    total: actions.length,
                    currentPage: 1,
                    pageSize: pageSize
                };

                $ctrl.updatePager = function () {
                    var page = $ctrl.pager.currentPage - 1;
                    var begin = page * pageSize;
                    var end = begin + pageSize;
                    $ctrl.page = $ctrl.pager.data.slice(begin, end);
                };

                $ctrl.updatePager();
            };

            $ctrl.showAttachments = function (action) {
                PermitDecisionActionAttachmentModal.open(action, decisionId, true);
            };

            $ctrl.close = function () {
                $uibModalInstance.close();
            };
        }
    })

    .service('PermitDecisionActionListModal', function ($uibModal) {
        this.open = function (decisionId) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/actions/action-list.html',
                size: 'lg',
                controllerAs: '$ctrl',
                controller: ModalController,
                resolve: {
                    decisionId: _.constant(decisionId),
                    actions: function (PermitDecisionAction) {
                        return PermitDecisionAction.query({decisionId: decisionId}).$promise;
                    }
                }
            }).result;
        };

        function ModalController($uibModalInstance, NotificationService, PermitDecisionAction,
                                 PermitDecisionActionEditModal, PermitDecisionActionAttachmentModal,
                                 decisionId, actions) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.actions = actions;
            };

            $ctrl.close = function () {
                $uibModalInstance.close();
            };

            $ctrl.editAttachments = function (action) {
                PermitDecisionActionAttachmentModal.open(action, decisionId, false).finally(function () {
                    reload();
                });
            };

            $ctrl.add = function () {
                edit();
            };

            $ctrl.edit = function (action) {
                edit(action);
            };

            function edit(action) {
                PermitDecisionActionEditModal.open(action, decisionId).then(function (action) {
                    saveOrUpdate(action).then(function () {
                        NotificationService.showDefaultSuccess();
                        reload();
                    }, function () {
                        NotificationService.showDefaultFailure();
                    });
                }, function () {
                    reload();
                });
            }

            function saveOrUpdate(action) {
                if (action.id) {
                    return PermitDecisionAction.update({decisionId: decisionId, id: action.id}, action).$promise;
                } else {
                    return PermitDecisionAction.save({decisionId: decisionId}, action).$promise;
                }
            }

            function reload() {
                PermitDecisionAction.query({decisionId: decisionId}).$promise.then(function (res) {
                    $ctrl.actions = res;
                });
            }
        }
    })
    .service('PermitDecisionActionEditModal', function ($uibModal) {
        this.open = function (action, decisionId) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/actions/action-edit.html',
                size: 'lg',
                controllerAs: '$ctrl',
                controller: ModalController,
                resolve: {
                    action: _.constant(action),
                    decisionId: _.constant(decisionId)
                }
            }).result;
        };

        function ModalController($uibModalInstance, $filter, Helpers,
                                 PermitDecisionAction, DecisionActionConstants,
                                 action, decisionId) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.actionTypes = DecisionActionConstants.actionType;
                $ctrl.communicationTypes = DecisionActionConstants.communicationTypes;

                var initAction = function (action) {
                    var dateFilter = $filter('date');
                    var date = action ? Helpers.toMoment(action.pointOfTime).toDate() : new Date();
                    action = action || {};
                    action.date = dateFilter(date, 'yyyy-MM-dd');
                    action.time = dateFilter(date, 'HH:mm');
                    return action;
                };

                $ctrl.selectedAction = initAction(action);
            };

            $ctrl.save = function () {
                var data = angular.copy($ctrl.selectedAction);

                var dateTime = moment(data.date).toDate();
                dateTime.setHours(data.time.slice(0, 2));
                dateTime.setMinutes(data.time.slice(3));

                data.pointOfTime = Helpers.dateTimeToString(dateTime);
                delete data.date;
                delete data.time;
                $uibModalInstance.close(data);
            };

            $ctrl.remove = function () {
                PermitDecisionAction.delete({
                    decisionId: decisionId, id: action.id
                }).$promise.then(function () {
                    $uibModalInstance.dismiss('delete');
                });
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }
    })

    .service('PermitDecisionActionAttachmentModal', function ($uibModal, PermitDecisionAction) {
        this.open = function (action, decisionId, readOnly) {
            var reloadAttachmentsFn = function () {
                return PermitDecisionAction.listAttachments({decisionId: decisionId, id: action.id}).$promise;
            };

            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/actions/action-attachments.html',
                size: 'lg',
                controllerAs: '$ctrl',
                controller: ModalController,
                resolve: {
                    actionId: _.constant(action.id),
                    decisionId: _.constant(decisionId),
                    readOnly: _.constant(readOnly),
                    reloadAttachmentsFn: _.constant(reloadAttachmentsFn),
                    attachments: function () {
                        return reloadAttachmentsFn();
                    }
                }
            }).result;
        };

        function ModalController($uibModalInstance, $filter, Helpers, FormPostService,
                                 PermitDecisionAction, DecisionActionConstants,
                                 decisionId, actionId, readOnly, attachments, reloadAttachmentsFn) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.attachments = attachments;
                $ctrl.readOnly = readOnly;
                $ctrl.baseUri = '/api/v1/decision/' + decisionId + '/action/' + actionId + '/attachment';
                $ctrl.upload = {
                    url: $ctrl.baseUri,
                    acceptTypes: '*/*',
                    formdata: {},
                    status: null
                };
            };

            $ctrl.close = function () {
                $uibModalInstance.dismiss('cancel');
            };

            $ctrl.download = function (a) {
                FormPostService.submitFormUsingBlankTarget($ctrl.baseUri + '/' + a.id);
            };

            $ctrl.delete = function (attachment) {
                PermitDecisionAction.deleteAttachment({
                    decisionId: decisionId,
                    id: actionId,
                    attachmentId: attachment.id
                }).$promise.then(function () {
                    reloadAttachmentList();
                });
            };

            $ctrl.onUpload = function () {
                $ctrl.upload.status = 'UPLOADING';
            };

            $ctrl.onSuccess = function () {
                $ctrl.upload.status = 'SUCCESS';
                reloadAttachmentList();
            };

            $ctrl.onError = function () {
                $ctrl.upload.status = 'ERROR';
            };

            function reloadAttachmentList() {
                reloadAttachmentsFn().then(function (result) {
                    $ctrl.attachments = result;
                });
            }
        }
    });
