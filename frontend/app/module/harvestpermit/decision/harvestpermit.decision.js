'use strict';

angular.module('app.harvestpermit.decision', [])
    .factory('PermitDecision', function ($resource) {
        var apiPrefix = 'api/v1/decision/:id';

        return $resource(apiPrefix, {id: '@id', spaId: '@spaId'}, {
            getApplication: {method: 'GET', url: apiPrefix + '/application'},
            getDocument: {method: 'GET', url: apiPrefix + '/document'},
            updateDocument: {method: 'PUT', url: apiPrefix + '/document'},
            generateText: {
                method: 'GET',
                url: apiPrefix + '/generate/:sectionId',
                params: {id: '@id', sectionId: '@sectionId'}
            },
            getCompleteStatus: {method: 'GET', url: apiPrefix + '/complete'},
            updateCompleteStatus: {method: 'PUT', url: apiPrefix + '/complete'},
            updatePayment: {method: 'PUT', url: apiPrefix + '/payment'},
            getReference: {method: 'GET', url: apiPrefix + '/reference'},
            updateReference: {method: 'PUT', url: apiPrefix + '/reference'},
            searchReferences: {method: 'POST', url: apiPrefix + '/search/references', isArray: true},
            getRevisions: {method: 'GET', url: apiPrefix + '/revisions', isArray: true},
            assign: {method: 'POST', url: apiPrefix + '/assign'},
            lock: {method: 'POST', url: apiPrefix + '/lock'},
            unlock: {method: 'POST', url: apiPrefix + '/unlock'},
            updatePosted:  {
                method: 'POST',
                url: apiPrefix + '/revisions/:revisionId/posted',
                params: {id: '@id', revisionId: '@revisionId'}
            },
            updateNotPosted:  {
                method: 'POST',
                url: apiPrefix + '/revisions/:revisionId/notposted',
                params: {id: '@id', revisionId: '@revisionId'}
            },
            getAttachments: {method: 'GET', url: apiPrefix + '/attachment', isArray: true},
            addDefaultMooseAttachment: {method: 'POST', url: apiPrefix + '/moose-attachment'},
            updateAttachmentOrder: {method: 'PUT', url: apiPrefix + '/attachment-order'},
            getPublishSettings: {method: 'GET', url: apiPrefix + '/publish-settings'},
            updatePublishSettings: {method: 'PUT', url: apiPrefix + '/publish-settings'},
            getDeliveries: {method: 'GET', url: apiPrefix + '/delivery', isArray: true},
            updateDeliveries: {method: 'POST', url: apiPrefix + '/delivery'},
            getAuthorities: {method: 'GET', url: apiPrefix + '/authorities'},
            updateAuthorities: {method: 'POST', url: apiPrefix + '/authorities'}
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
                    }
                }
            })
            .state('jht.decision.conflicts', {
                url: '/conflicts?firstApplicationId&secondApplicationId',
                templateUrl: 'harvestpermit/applications/conflict/conflict-resolution.html',
                controllerAs: '$ctrl',
                controller: 'HarvestPermitApplicationConflictResolutionController',
                wideLayout: true,
                resolve: {
                    firstApplicationId: function ($stateParams) {
                        return _.parseInt($stateParams.firstApplicationId);
                    },
                    secondApplicationId: function ($stateParams) {
                        return _.parseInt($stateParams.secondApplicationId);
                    }
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
                $ctrl.decisionName = 'Hirvieläinten pyyntilupa';

                if ($ctrl.decision) {
                    $ctrl.permitHolderName = $ctrl.decision.permitHolder
                        ? formatClub($ctrl.decision.permitHolder)
                        : formatContactPerson($ctrl.decision.contactPerson);
                }

                $ctrl.isLocked = $ctrl.decision && $ctrl.decision.status !== 'DRAFT';
            };

            function formatClub(permitHolder) {
                return permitHolder.nameFI + ' - ' + permitHolder.officialCode;
            }

            function formatContactPerson(contactPerson) {
                return contactPerson.firstName + ' ' + contactPerson.lastName;
            }
        }
    });
