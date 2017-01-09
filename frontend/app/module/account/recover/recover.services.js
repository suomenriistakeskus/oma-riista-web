'use strict';

angular.module('app.account.recover.services', ['ngResource'])
    .factory('PasswordResetService', function ($http) {
        return {
            requestPasswordResetEmail: function (email) {
                var data = {
                    "email": email
                };
                return $http.post("api/v1/password/forgot", data);
            },
            resetPasswordUsingToken: function (resetToken, newPassword) {
                var data = {
                    "token": resetToken,
                    "password": newPassword
                };
                return $http.post("api/v1/password/reset", data);
            },
            verifyToken: function (resetToken) {
                return $http.post("api/v1/password/verifytoken", resetToken);
            }
        };
    });
