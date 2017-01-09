'use strict';

angular.module('app', [
    'ngLocale',
    'ngCookies',
    'ngResource',
    'ngSanitize',
    'ngMessages',
    'ngIdle',
    'pascalprecht.translate',
    'ui.bootstrap',
    'ui.bootstrap.showErrors',
    'ui.router',
    'ui.router.history',
    'ui.mask',
    'dialogs.main',
    'angular-growl',
    'angular-cache',
    'angular-loading-bar',
    'vs-repeat',
    'ui.select2',
    'blockUI',
    'lr.upload',
    'nemLogging',
    'ui-leaflet', 'app.custom-leaflet.directives',
    'textAngular',
    'sprout.off-canvas-stack',
    'ngDropzone',

    // Pre-compiled templates
    'templates',

    // Common
    'app.metadata',
    'app.common.config', 'app.common.validation', 'app.common.directives', 'app.common.services', 'app.common.filters',
    'app.common.pagination.slice',
    'app.common.components', 'app.map.services', 'app.map.directives', 'app.gamespecies',

    // Layout
    'app.layout.services', 'app.layout.controllers', 'app.layout.directives',
    'app.layout.idle', 'app.layout.language', 'app.layout.role', 'app.layout.search',

    // Login
    'app.login.services', 'app.login.controllers',

    // Main
    'app.main.controllers',

    // Admin
    'app.admin.controllers',

    // Announcements
    'app.announcements',

    // Reporting
    'app.reporting.services', 'app.reporting.controllers', 'app.reporting.club.dashboard',

    // Organisaatiot
    'app.organisation.services', 'app.organisation.controllers',
    'app.organisation.selection',

    // Riistanhoitoyhdistykset
    'app.rhy.services', 'app.rhy.controllers',
    'app.rhy.club',
    'app.rhy.harvestmap',
    'app.rhy.moosepermitstats',

    // Tehtävät
    'app.occupation.services', 'app.occupation.controllers',
    'app.occupation.nomination',

    // Tapahtumat ja tapahtumapaikat
    'app.event.services', 'app.event.controllers',

    // Account & Registration
    'app.account.services', 'app.account.controllers', 'app.account.directives',
    'app.account.profile',
    'app.account.announcements',
    'app.account.recover.services', 'app.account.recover.controllers',
    'app.account.registration.services', 'app.account.registration.controllers',

    // Users
    'app.user.controllers', 'app.user.services',

    // Game Diary
    'app.diary.controllers', 'app.diary.services', 'app.diary.filters',
    'app.diary.list.controllers', 'app.diary.list.services',

    // Harvest reports
    'app.harvestreport.controllers', 'app.harvestreport.services',

    // Harvest report admin
    'app.adminharvest.controllers', 'app.adminharvest.services',

    // Harvest permits
    'app.harvestpermit.controllers', 'app.harvestpermit.services', 'app.harvestpermit.directives',
    'app.listpermit.directives',

    // Hunting clubs
    'app.club.controllers', 'app.club.services',
    'app.club.members', 'app.clubgroup.members',
    'app.clubgroup.datacard',
    'app.clubgroup.controllers', 'app.clubgroup.services', 'app.clubgroup.directives',
    'app.clubarea.controllers', 'app.clubarea.services', 'app.clubarea.components', 'app.clubarea.print',
    'app.clubmap.controllers', 'app.clubmap.services', 'app.clubmap.directives', 'app.clubmap.filters',

    // Yhteislupa-alueet
    'app.clubarea.proposal',

    'app.clubhunting.controllers', 'app.clubhunting.services',
    'app.clubhunting.list', 'app.clubhunting.add', 'app.clubhunting.day', 'app.clubhunting.show',
    'app.clubpermit.controllers',

    // Moose permits
    'app.moosepermit.controllers', 'app.moosepermit.services', 'app.moosepermit.directives',
    'app.moosepermit.map', 'app.moosepermit.moosehuntingsummary', 'app.moosepermit.mooseharvetsreport',
    'app.moosepermit.deerhuntingsummary',

    // SRVA
    'app.srva.services', 'app.srva.controllers'
]);
