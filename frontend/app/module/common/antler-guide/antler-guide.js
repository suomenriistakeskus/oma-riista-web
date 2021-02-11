'use strict';

angular.module('app.common.antler-guide', [])
    .service('AntlerGuide', function (offCanvasStack) {

       this.show = function (entry) {
           var template;

           if (entry.isMoose()) {
               template = 'common/antler-guide/moose.html';
           } else if (entry.isRoeDeer()) {
               template = 'common/antler-guide/roedeer.html';
           } else if (entry.isWhiteTailedDeer()) {
               template = 'common/antler-guide/wtd.html';
           } else {
               return;
           }

           offCanvasStack.open({ templateUrl: template, largeDialog: false });
       };

        this.isVisible = function (entry, fields) {
            var specimen = !!entry ? entry.specimens[0] : null;

            return !!entry && !!fields
                && fields.isVisibleSpecimenField('antlersLost', specimen)
                && (entry.isMoose()
                    || entry.isRoeDeer()
                    || entry.isWhiteTailedDeer())
                && (fields.isVisibleSpecimenField('antlersWidth', specimen)
                    || fields.isVisibleSpecimenField('antlersGirth', specimen)
                    || fields.isVisibleSpecimenField('antlersLength', specimen)
                    || fields.isVisibleSpecimenField('antlersInnerWidth', specimen)
                    || fields.isVisibleSpecimenField('antlerShaftWidth', specimen));
        };

    })
    .component('rAntlerGuideButton', {
        templateUrl: 'common/antler-guide/button.html',
        bindings: {
            entry: '<'
        },
        controller: function (AntlerGuide) {
            var $ctrl = this;

            $ctrl.click = function () {
                AntlerGuide.show($ctrl.entry);
            };

        }
    });