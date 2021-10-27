'use strict';

angular.module('app.harvestpermit.decision', [])
    .factory('PermitDecision', function ($resource) {
        var apiPrefix = 'api/v1/decision/:id';

        return $resource(apiPrefix, {id: '@id', spaId: '@spaId'}, {
            hasArea: {method: 'GET', url: apiPrefix + '/hasarea'},
            hasNatura: {method: 'GET', url: apiPrefix + '/hasnatura'},
            getApplication: {method: 'GET', url: apiPrefix + '/application'},
            getDocument: {method: 'GET', url: apiPrefix + '/document'},
            updateDocument: {method: 'PUT', url: apiPrefix + '/document'},
            generateText: {
                method: 'GET',
                url: apiPrefix + '/generate/:sectionId',
                params: {id: '@id', sectionId: '@sectionId'}
            },
            generateAndPersistText: {
                method: 'POST',
                url: apiPrefix + '/generate/:sectionId',
                params: {id: '@id', sectionId: '@sectionId'}
            },
            generateAdjustedAreaSizeAction: {method: 'GET', url : apiPrefix + '/generate-area-action'},
            getCompleteStatus: {method: 'GET', url: apiPrefix + '/complete'},
            updateCompleteStatus: {method: 'PUT', url: apiPrefix + '/complete'},
            getPaymentOptions: {method: 'GET', url: apiPrefix + '/payment', isArray: true},
            updatePayment: {method: 'PUT', url: apiPrefix + '/payment'},
            getReference: {method: 'GET', url: apiPrefix + '/reference'},
            updateReference: {method: 'PUT', url: apiPrefix + '/reference'},
            searchReferences: {method: 'POST', url: apiPrefix + '/search/references'},
            getRevisions: {method: 'GET', url: apiPrefix + '/revisions', isArray: true},
            assign: {method: 'POST', url: apiPrefix + '/assign'},
            unassign: {method: 'POST', url: apiPrefix + '/unassign'},
            setForbiddenMethods: {method: 'POST', url: apiPrefix + '/set-forbidden-methods', params: {forbiddenMethodsOnly: '@forbiddenMethodsOnly'}},
            lock: {method: 'POST', url: apiPrefix + '/lock'},
            unlock: {method: 'POST', url: apiPrefix + '/unlock'},
            updatePosted: {
                method: 'POST',
                url: apiPrefix + '/revisions/:revisionId/posted',
                params: {id: '@id', revisionId: '@revisionId'}
            },
            updateNotPosted: {
                method: 'POST',
                url: apiPrefix + '/revisions/:revisionId/notposted',
                params: {id: '@id', revisionId: '@revisionId'}
            },
            getLegalFields: {method: 'GET', url: apiPrefix + '/legal'},
            updateLegalFields: {method: 'POST', url: apiPrefix + '/legal'},
            getAttachments: {method: 'GET', url: apiPrefix + '/attachment', isArray: true},
            addDefaultMooseAttachment: {method: 'POST', url: apiPrefix + '/moose-attachment'},
            updateAttachmentOrder: {method: 'PUT', url: apiPrefix + '/attachment-order'},
            getDocumentSettings: {method: 'GET', url: apiPrefix + '/document-settings'},
            updateDocumentSettings: {method: 'PUT', url: apiPrefix + '/document-settings'},
            getPublishSettings: {method: 'GET', url: apiPrefix + '/publish-settings'},
            updatePublishSettings: {method: 'PUT', url: apiPrefix + '/publish-settings'},
            getAppealSettings: {method: 'GET', url: apiPrefix + '/appeal-settings'},
            updateAppealSettings: {method: 'PUT', url: apiPrefix + '/appeal-settings'},
            getDeliveries: {method: 'GET', url: apiPrefix + '/delivery', isArray: true},
            updateDeliveries: {method: 'POST', url: apiPrefix + '/delivery'},
            getAuthorities: {method: 'GET', url: apiPrefix + '/authorities'},
            updateAuthorities: {method: 'POST', url: apiPrefix + '/authorities'},
            updateGrantStatus: {method: 'PUT', url: apiPrefix + '/grantstatus'}
        });
    })
    .factory('PermitDecisionDerogation', function ($resource) {
        var apiPrefix = '/api/v1/decision/derogation/:id';

        return $resource(apiPrefix, {id: '@id'}, {
            getReasons: {method: 'GET', url: apiPrefix + '/reasons'},
            updateReasons: {method: 'POST', url: apiPrefix + '/reasons'},
            getProtectedAreaTypes: {method: 'GET', url: apiPrefix + '/area'},
            updateProtectedAreaTypes: {method: 'POST', url: apiPrefix + '/area'}
        });
    })
    .factory('PermitDecisionRkaAuthority', function ($resource) {
        var apiPrefix = 'api/v1/decision/rkaauthority/:id';

        return $resource(apiPrefix, {id: '@id'}, {
            listByPermitDecision: {method: 'GET', url: apiPrefix + '/permitdecision/:decisionId', isArray: true}
        });
    })
    .config(function ($stateProvider) {
        $stateProvider
            .state('jht.decision', {
                url: '/decision/{decisionId:[0-9]{1,8}}',
                templateUrl: 'harvestpermit/decision/layout.html',
                abstract: true,
                resolve: {
                    decisionId: function ($stateParams) {
                        return _.parseInt($stateParams.decisionId);
                    },
                    hasArea: function (PermitDecision, decisionId) {
                        return PermitDecision.hasArea({id: decisionId}).$promise.then(function (res) {
                            return res.hasArea;

                        });
                    },
                    hasNatura: function (PermitDecision, decisionId) {
                        return PermitDecision.hasNatura({id: decisionId}).$promise.then(function (res) {
                            return res.hasNatura;
                        });
                    }

                },
                controllerAs: '$ctrl',
                controller: function (hasArea, hasNatura) {
                    var $ctrl = this;
                    $ctrl.hasArea = hasArea;
                    $ctrl.hasNatura = hasNatura;
                }
            });
    })
    .component('permitDecisionNavHeader', {
        templateUrl: 'harvestpermit/decision/decision-nav-header.html',
        bindings: {
            decision: '<'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                if ($ctrl.decision && $ctrl.decision.permitHolder) {
                    $ctrl.permitHolderName = $ctrl.decision.permitHolder.name + getCodeSuffix($ctrl.decision.permitHolder);
                }

                $ctrl.isLocked = $ctrl.decision && $ctrl.decision.status !== 'DRAFT';
            };

            function getCodeSuffix(h) {
                return h.code ? (' ' + h.code) : '';
            }
        }
    });
