'use strict';

angular.module('app.diary.filters', [])
    .filter('gameAge', function ($translate) {
        return function (age, gameSpeciesCode) {
            age = gameSpeciesCode === 47348 && age === '_1TO2Y' ? 'ERAUS' : age;
            return $translate.instant('gamediary.age.' + age);
        };
    })

    .filter('stripUnknown', function () {
        return function (object) {
            if (_.isArray(object)) {
                return _.filter(object, function (arrayElem) {
                    return arrayElem !== 'UNKNOWN';
                });
            }

            return object !== 'UNKNOWN' ? object : null;
        };
    });
