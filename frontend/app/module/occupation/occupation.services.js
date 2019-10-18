'use strict';

angular.module('app.occupation.services', [])
    .constant('CallOrderConfig', {
        callOrderValues: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],// _.range is tempting, but sometimes it is not found when loading the page
        isCallOrderType: function (occupationType) {
            return occupationType === 'SRVA_YHTEYSHENKILO' ||
                occupationType === 'PETOYHDYSHENKILO';
        }
    })
    .constant('JHTOccupationTypes', [
        'AMPUMAKOKEEN_VASTAANOTTAJA',
        'METSASTAJATUTKINNON_VASTAANOTTAJA',
        'METSASTYKSENVALVOJA',
        'RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA'
    ])
    .factory('Occupations', function ($resource) {
        return $resource('api/v1/organisation/:orgId/occupation/:id', {orgId: "@orgId", id: "@id"}, {
            query: {
                method: 'GET',
                isArray: true
            },
            listCandidatePersons: {
                url: 'api/v1/organisation/:orgId/candidates',
                method: 'GET',
                isArray: true
            },
            get: {method: 'GET'},
            update: {method: 'PUT'},
            delete: {method: 'DELETE'}
        });
    })
    .factory('OccupationTypes', function ($resource) {
        return $resource('api/v1/organisation/:orgId/occupationTypes', {orgId: "@orgId"}, {
            query: {method: 'GET'}
        });
    })
    .service('OccupationsFilterSorterService', function () {
        function isBoard(boardTypes, type) {
            if (boardTypes) {
                return boardTypes.indexOf(type) !== -1;
            }
        }

        function localDate(d) {
            var date = new Date(d);
            return new Date(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate());
        }

        function today() {
            return localDate(new Date());
        }

        function filterByOccupationType(occupations, type) {
            if (type) {
                occupations = _.filter(occupations, function (o) {
                    return o.occupationType === type;
                });
            }

            return occupations;
        }

        var current = function (boardTypes, allOccupations, occupationTypeFilter) {
            var occupations = _.filter(allOccupations, function (o) {
                return (!o.beginDate|| localDate(o.beginDate) <= today()) &&
                    (!o.endDate || localDate(o.endDate) >= today());
            });
            occupations = filterByOccupationType(occupations, occupationTypeFilter);
            var currentOccupations = _.filter(occupations, function (o) {
                return !isBoard(boardTypes, o.occupationType);
            });
            var board = _.filter(occupations, function (o) {
                return isBoard(boardTypes, o.occupationType);
            });
            return {
                occupations: currentOccupations,
                board: board
            };
        };

        var past = function (boardTypes, allOccupations, occupationTypeFilter) {
            var pastOccupations = _.filter(allOccupations, function (o) {
                return o.endDate !== null && localDate(o.endDate) < today();
            });
            pastOccupations = filterByOccupationType(pastOccupations, occupationTypeFilter);
            pastOccupations.sort(function (a, b) {
                return a.endDate - b.endDate;
            });
            return {
                occupations: pastOccupations,
                board: null
            };
        };

        var future = function (boardTypes, allOccupations, occupationTypeFilter) {
            var futureOccupations = _.filter(allOccupations, function (o) {
                return o.beginDate !== null && localDate(o.beginDate) > today();
            });
            futureOccupations = filterByOccupationType(futureOccupations, occupationTypeFilter);
            futureOccupations.sort(function (a, b) {
                return a.beginDate - b.beginDate;
            });
            return {
                occupations: futureOccupations,
                board: null
            };
        };

        return {
            current: current,
            past: past,
            future: future
        };
    })

    .factory('OccupationFindPerson', function (HttpPost) {
        return {
            findByHunterNumber: function (hunterNumber) {
                return HttpPost.post('api/v1/organisation/findperson/hunternumber', {hunterNumber: hunterNumber});
            },
            findBySsn: function (ssn) {
                return HttpPost.post('api/v1/organisation/findperson/ssn', {ssn: ssn});
            }
        };
    })

    .service('OccupationDialogService', function ($uibModal, Occupations, OccupationPermissionService) {
        function filterOccupations(occupationTypes, showBoardOnly) {
            return OccupationPermissionService.filterOccupationTypes(
                showBoardOnly && occupationTypes.board
                    ? occupationTypes.board
                    : occupationTypes.all);
        }

        this.addOccupation = function (orgId, occupationTypes, showBoardOnly) {
            return $uibModal.open({
                templateUrl: 'occupation/form.html',
                resolve: {
                    orgId: _.constant(orgId),
                    occupation: _.constant({}),
                    occupationTypes: _.constant(filterOccupations(occupationTypes, showBoardOnly)),
                    existingPersons: function () {
                        return Occupations.listCandidatePersons({orgId: orgId}).$promise;
                    }
                },
                controller: 'OccupationFormController'
            }).result;
        };

        this.showSelected = function (occupation, orgId, occupationTypes, showBoardOnly) {
            return $uibModal.open({
                templateUrl: 'occupation/form.html',
                resolve: {
                    orgId: _.constant(orgId),
                    occupation: _.constant(angular.copy(occupation)),
                    occupationTypes: _.constant(filterOccupations(occupationTypes, showBoardOnly)),
                    existingPersons: function () {
                        return Occupations.listCandidatePersons({orgId: orgId}).$promise;
                    }
                },
                controller: 'OccupationFormController'
            }).result;
        };

        this.removeSelected = function (occupation, orgId) {
            return $uibModal.open({
                templateUrl: 'occupation/remove.html',
                resolve: {
                    orgId: _.constant(orgId),
                    occupation: _.constant(occupation)
                },
                controller: 'OccupationRemoveController'
            }).result;
        };
    })

    .service('OccupationPermissionService', function (ActiveRoleService, JHTOccupationTypes) {
        function onlyModeratorCanModify(occupationType) {
            return occupationType === 'TOIMINNANOHJAAJA' || _.includes(JHTOccupationTypes, occupationType);
        }

        this.canModify = function (occupation) {
            return ActiveRoleService.isModerator() || !onlyModeratorCanModify(occupation.occupationType);
        };

        this.filterOccupationTypes = function (occupationTypes) {
            return !ActiveRoleService.isModerator()
                ? _.reject(occupationTypes, onlyModeratorCanModify)
                : occupationTypes;
        };
    });
