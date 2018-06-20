"use strict";

angular.module('app.account', [
    'app.account.services',
    'app.account.controllers',
    'app.account.directives',
    'app.account.profile',
    'app.account.announcements',
    'app.account.twofactor',
    'app.account.permit',
    'app.account.recover.services',
    'app.account.recover.controllers',
    'app.account.registration.services',
    'app.account.registration.controllers'
]);
