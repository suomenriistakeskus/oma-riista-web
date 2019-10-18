'use strict';

angular.module('app.config.cache', [])
    .run(function (CacheFactory) {
        CacheFactory.createCache('diaryParameterCache', {
            storageMode: 'sessionStorage',
            maxAge: 30 * 60 * 1000, // 30 min
            deleteOnExpire: 'aggressive'
        });
        CacheFactory.createCache('diarySrvaParameterCache', {
            storageMode: 'sessionStorage',
            maxAge: 30 * 60 * 1000, // 30 min
            deleteOnExpire: 'aggressive'
        });
        CacheFactory.createCache('accountPermitTodoCountCache', {
            storageMode: 'memory',
            maxAge: 2 * 1000, // 2 seconds
            deleteOnExpire: 'aggressive'
        });
        CacheFactory.createCache('accountInvitationTodoCountCache', {
            storageMode: 'memory',
            maxAge: 2 * 1000, // 2 seconds
            deleteOnExpire: 'aggressive'
        });
        CacheFactory.createCache('accountSrvaTodoCountCache', {
            storageMode: 'memory',
            maxAge: 2 * 1000, // 2 seconds
            deleteOnExpire: 'aggressive'
        });
        CacheFactory.createCache('accountShootingTestTodoCountCache', {
            storageMode: 'memory',
            maxAge: 2 * 1000, // 2 seconds
            deleteOnExpire: 'aggressive'
        });
        CacheFactory.createCache('areasContactSearchCache', {
            storageMode: 'sessionStorage',
            maxAge: 30 * 60 * 1000, // 30 min
            deleteOnExpire: 'aggressive'
        });
        CacheFactory.createCache('harvestPermitPermitTypesCache', {
            storageMode: 'sessionStorage',
            maxAge: 30 * 60 * 1000, // 30 min
            deleteOnExpire: 'aggressive'
        });
    });
