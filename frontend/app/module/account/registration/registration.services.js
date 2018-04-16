'use strict';

angular.module('app.account.registration.services', [])
    .factory('AccountRegistrationService', function ($http) {
        return {
            requestAccountRegistrationEmail: function (data) {
                return $http.post("/api/v1/register/send-email", data);
            },
            processTokenFromEmail: function (data) {
                return $http.post("/api/v1/register/from-email", data);
            },
            requestDataToConfirm: function (trid) {
                return $http.post("/api/v1/register/data", {trid: trid});
            },
            completeRegistration: function (data) {
                return $http.post("/api/v1/register/confirm", data);
            }
        };
    });
