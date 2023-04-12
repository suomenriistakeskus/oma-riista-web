'use strict';

angular.module('app.common.huntingclubsearch', [])
    .service('HuntingClubSearchService', function (HttpPost) {
        this.findNameById = function (huntingClubId) {
            return HttpPost.post('api/v1/search/club/huntingclubid', {huntingClubId: huntingClubId});
        };

        this.findNameByOfficialCode = function (officialCode) {
            return HttpPost.post('api/v1/search/club/officialcode', {officialCode: officialCode});
        };
    });
