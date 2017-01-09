'use strict';

angular.module('app.account.registration.services', [])
    .factory('AccountRegistrationService', function ($http, $sce, HttpPost) {
        return {
            requestAccountRegistrationEmail: function (data) {
                return $http.post("/api/v1/register/send-email", data);
            },
            processTokenFromEmail: function (data) {
                return $http.post("/api/v1/register/from-email", data).then(
                    function (response) {
                        if (!response.data.status) {
                            response.data.status = 'error';
                        }

                        // Encode URL as trusted for Angular, so that it can be used as form action URL
                        if (response.data.vetumaLoginUrl) {
                            response.data.vetumaLoginUrl = $sce.trustAsResourceUrl(response.data.vetumaLoginUrl);
                        } else {
                            console.log("Vetuma URL is missing");
                        }

                        return response.data;
                    });
            },
            requestDataToConfirm: function (trid) {
                return $http.post("/api/v1/register/data", {trid: trid});
            },
            completeRegistration: function (data) {
                return $http.post("/api/v1/register/confirm", data);
            }
        };
    });
