'use strict';

angular.module('app.jht.nominationdecision.actions', [])

    .factory('NominationDecisionAction', function ($resource) {
        var apiPrefix = 'api/v1/nominationdecision/:decisionId/action/:id';

        return $resource(apiPrefix, {id: '@id', decisionId: '@decisionId', attachmentId: '@attachmentId'}, {
            update: {method: 'PUT'},
            delete: {method: 'DELETE'},
            listAttachments: {method: 'GET', url: apiPrefix + '/attachment', isArray: true},
            getAttachment: {method: 'POST', url: apiPrefix + '/attachment/:attachmentId'},
            deleteAttachment: {method: 'DELETE', url: apiPrefix + '/attachment/:attachmentId'},
        });
    })

    .service('NominationDecisionActionReadonlyListModal', function ($uibModal, NominationDecisionActionAttachmentModal) {
        this.open = function (decisionId) {
            return $uibModal.open({
                templateUrl: 'jht/nominationdecision/actions/action-readonly-list.html',
                size: 'lg',
                controllerAs: '$ctrl',
                controller: ModalController,
                resolve: {
                    decisionId: _.constant(decisionId),
                    actions: function (NominationDecisionAction) {
                        return NominationDecisionAction.query({decisionId: decisionId}).$promise;
                    }
                }
            }).result;
        };

        function ModalController($uibModalInstance, decisionId, actions) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                // 2 actions at a time fit in the read only dialog
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
                NominationDecisionActionAttachmentModal.open(action, decisionId, true);
            };

            $ctrl.close = function () {
                $uibModalInstance.close();
            };
        }
    })

    .service('NominationDecisionActionListModal', function ($uibModal) {
        this.open = function (decisionId) {
            return $uibModal.open({
                templateUrl: 'jht/nominationdecision/actions/action-list.html',
                size: 'lg',
                controllerAs: '$ctrl',
                controller: ModalController,
                resolve: {
                    decisionId: _.constant(decisionId),
                    actions: function (NominationDecisionAction) {
                        return NominationDecisionAction.query({decisionId: decisionId}).$promise;
                    }
                }
            }).result;
        };

        function ModalController($uibModalInstance, NotificationService, NominationDecisionAction,
                                 NominationDecisionActionEditModal, NominationDecisionActionAttachmentModal,
                                 decisionId, actions) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.actions = actions;
            };

            $ctrl.close = function () {
                $uibModalInstance.close();
            };

            $ctrl.editAttachments = function (action) {
                NominationDecisionActionAttachmentModal.open(action, decisionId, false).finally(function () {
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
                NominationDecisionActionEditModal.open(action, decisionId).then(function (action) {
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
                    return NominationDecisionAction.update({decisionId: decisionId, id: action.id}, action).$promise;
                } else {
                    return NominationDecisionAction.save({decisionId: decisionId}, action).$promise;
                }
            }

            function reload() {
                NominationDecisionAction.query({decisionId: decisionId}).$promise.then(function (res) {
                    $ctrl.actions = res;
                });
            }
        }
    })
    .service('NominationDecisionActionEditModal', function ($uibModal) {
        this.open = function (action, decisionId) {
            return $uibModal.open({
                templateUrl: 'jht/nominationdecision/actions/action-edit.html',
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
                                 NominationDecisionAction, DecisionActionConstants,
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
                NominationDecisionAction.delete({
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

    .service('NominationDecisionActionAttachmentModal', function ($uibModal, NominationDecisionAction) {
        this.open = function (action, decisionId, readOnly) {
            var reloadAttachments = function () {
                return NominationDecisionAction.listAttachments({decisionId: decisionId, id: action.id}).$promise;
            };

            return $uibModal.open({
                templateUrl: 'jht/nominationdecision/actions/action-attachments.html',
                size: 'lg',
                controllerAs: '$ctrl',
                controller: ModalController,
                resolve: {
                    actionId: _.constant(action.id),
                    decisionId: _.constant(decisionId),
                    readOnly: _.constant(readOnly),
                    reloadAttachments: _.constant(reloadAttachments),
                    attachments: function () {
                        return reloadAttachments();
                    }
                }
            }).result;
        };

        function ModalController($uibModalInstance, $filter, Helpers, FormPostService,
                                 NominationDecisionAction, DecisionActionConstants,
                                 decisionId, actionId, readOnly, attachments, reloadAttachments) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.attachments = attachments;
                $ctrl.readOnly = readOnly;
                $ctrl.baseUri = '/api/v1/nominationdecision/' + decisionId + '/action/' + actionId + '/attachment';
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
                NominationDecisionAction.deleteAttachment({
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
                reloadAttachments().then(function (result) {
                    $ctrl.attachments = result;
                });
            }
        }
    });
