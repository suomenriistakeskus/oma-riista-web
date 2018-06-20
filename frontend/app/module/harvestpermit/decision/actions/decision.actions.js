'use strict';

angular.module('app.harvestpermit.decision.actions', [])
    .constant('PermitDecisionActionConstants', {
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
        var apiPrefix = 'api/v1/decision/:decisionId';

        return $resource(apiPrefix, {id: '@id', decisionId: '@decisionId'}, {
            listActions: {method: 'GET', url: apiPrefix + '/actions', isArray: true},
            createAction: {method: 'POST', url: apiPrefix + '/action'},
            updateAction: {method: 'PUT', url: apiPrefix + '/action/:id'},
            deleteAction: {method: 'DELETE', url: apiPrefix + '/action/:id'}
        });
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
                        return PermitDecisionAction.listActions({decisionId: decisionId}).$promise;
                    }
                }
            }).result;
        };

        function ModalController($uibModalInstance, NotificationService,
                                 PermitDecisionActionEditModal, PermitDecisionAction,
                                 decisionId, actions) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.actions = actions;
            };

            $ctrl.close = function () {
                $uibModalInstance.close();
            };

            $ctrl.add = function () {
                edit();
            };

            $ctrl.edit = function (action) {
                edit(action);
            };

            $ctrl.remove = function (action) {
                PermitDecisionAction.deleteAction({decisionId: decisionId, id: action.id}).$promise.then(function () {
                    reload();
                });
            };

            function edit(action) {
                PermitDecisionActionEditModal.open(action).then(function (action) {
                    saveOrUpdate(action).then(function () {
                        NotificationService.showDefaultSuccess();
                        reload();
                    }, function () {
                        NotificationService.showDefaultFailure();
                    });
                });
            }

            function saveOrUpdate(action) {
                if (action.id) {
                    return PermitDecisionAction.updateAction({decisionId: decisionId, id: action.id}, action).$promise;
                } else {
                    return PermitDecisionAction.createAction({decisionId: decisionId}, action).$promise;
                }
            }

            function reload() {
                PermitDecisionAction.listActions({decisionId: decisionId}).$promise.then(function (res) {
                    $ctrl.actions = res;
                });
            }
        }
    })
    .service('PermitDecisionActionEditModal', function ($uibModal) {
        this.open = function (action) {
            return $uibModal.open({
                templateUrl: 'harvestpermit/decision/actions/action-edit.html',
                size: 'lg',
                controllerAs: '$ctrl',
                controller: ModalController,
                resolve: {
                    action: _.constant(action)
                }
            }).result;
        };

        function ModalController($uibModalInstance, $filter, Helpers, PermitDecisionActionConstants, action) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.actionTypes = PermitDecisionActionConstants.actionType;
                $ctrl.communicationTypes = PermitDecisionActionConstants.communicationTypes;

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

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        }
    })

    .service('PermitDecisionAdjustedAreaSize', function ($q, dialogs, Helpers, NotificationService,
                                                         PermitDecisionAction, PermitDecision) {
        this.create = function (decisionId) {
            return PermitDecision.generateText({
                id: decisionId,
                sectionId: 'adjustedAreaSizeAction'

            }).$promise.then(function (res) {
                if (!res.content) {
                    NotificationService.showMessage('Korjattua pinta-alaa ei ole saatavilla', 'warn');
                    return $q.reject();
                }

                var dialogTitle = 'Luo välitoimenpide';
                var actionTextPlain = res.content;
                var actionTextHtml = res.content.replace(/\n\n/g, '<br/><br/>').replace(/\n/g, '<br/>');
                var dialogMessage = '<p><strong>Lisätäänkö päätökselle seuraava välitoimenpide?</strong></p>' +
                    '<hr><p>' + actionTextHtml + '</p>';

                return dialogs.confirm(dialogTitle, dialogMessage).result.then(function () {
                    var action = {
                        actionType: 'MUU',
                        pointOfTime: Helpers.dateTimeToString(new Date()),
                        text: actionTextPlain,
                        decisionText: actionTextPlain
                    };

                    return PermitDecisionAction.createAction({decisionId: decisionId}, action).$promise;
                });
            });

        };
    });
