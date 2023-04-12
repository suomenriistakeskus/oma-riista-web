'use strict';

angular.module('app.jht.otherwisedeceased.services', [])
    .service('OtherwiseDeceasedService', function ($q, GameSpeciesCodes, OtherwiseDeceasedApi, AttachmentService,
                                                   FetchAndSaveBlob) {
        var self = this;

        self.getSpeciesCodes = _.constant(
            [
                GameSpeciesCodes.WOLF,
                GameSpeciesCodes.LYNX,
                GameSpeciesCodes.BEAR,
                GameSpeciesCodes.WOLVERINE,
                GameSpeciesCodes.OTTER,
                GameSpeciesCodes.WILD_FOREST_REINDEER
            ]);

        self.searchPage = function (filters, page, pageSize) {
            return OtherwiseDeceasedApi.getBriefSlice(filters, {page: page, size: pageSize}).$promise;
        };

        self.getDetails = function (itemId) {
            return OtherwiseDeceasedApi.getDetails({itemId: itemId}).$promise;
        };

        self.save = function (item) {
            if (AttachmentService.hasAttachments()) {
                return AttachmentService.sendAttachments(item);
            }
            return OtherwiseDeceasedApi.save(item).$promise;
        };

        self.reject = function (itemId) {
            return OtherwiseDeceasedApi.reject({itemId: itemId}).$promise;
        };

        self.restore = function (itemId) {
            return OtherwiseDeceasedApi.restore({itemId: itemId}).$promise;
        };

        self.downloadAttachment = function (attachmentId) {
            return FetchAndSaveBlob.get('/api/v1/deceased/attachment/' + attachmentId);
        };

        self.deleteAttachment = function (attachmentId) {
            return OtherwiseDeceasedApi.deleteAttachment({itemId: attachmentId}).$promise;
        };
    })
    .factory('OtherwiseDeceasedApi', function ($resource) {
        var apiPrefix = 'api/v1/deceased';

        function getMethod(suffix, isArray) {
            return {method: 'GET', url: apiPrefix + suffix, isArray: isArray};
        }

        function postMethod(suffix) {
            return {method: 'POST', url: apiPrefix + suffix};
        }

        function putMethod(suffix) {
            return {method: 'PUT', url: apiPrefix + suffix};
        }

        function deleteMethod(suffix) {
            return {method: 'DELETE', url: apiPrefix + suffix};
        }

        return $resource(apiPrefix, {itemId: '@itemId', year: '@year'}, {
            getDetails: getMethod('/:itemId', false),
            save: postMethod('/save'),
            reject: putMethod('/:itemId/reject'),
            restore: putMethod('/:itemId/restore'),
            deleteAttachment: deleteMethod('/attachment/:itemId')
        });

    })
    .service('OtherwiseDeceasedSearch', function ($http) {
        function _findPage(url, searchParams, pager) {
            return $http({
                method: 'POST',
                url: url,
                params: pager,
                data: searchParams
            });
        }

        this.findPage = function (searchParams, pager) {
            return _findPage('api/v1/deceased/list', searchParams, pager);
        };
    });