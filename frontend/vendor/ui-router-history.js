"use strict";

// source: http://plnkr.co/edit/DJH6mQUCbTFfSbdCBYUo?p=preview
angular.module("ui.router.history", ["ui.router"])
    .service("$history", ['$state', '$q', function ($state, $q) {
        var history = [];

        angular.extend(this, {
            push: function (state, params) {
                history.push({state: state, params: params});
            },
            all: function () {
                return history;
            },
            go: function (step) {
                var prev = this.previous(step || -1);

                if (prev) {
                    return $state.go(prev.state, prev.params, {reload: true});
                } else {
                    return $q.reject("no history");
                }
            },
            previous: function (step) {
                if (history.length > 1) {
                    return history[history.length - Math.abs(step || -1)];
                } else {
                    return null;
                }
            },
            back: function () {
                return this.go(-1);
            }
        });

    }]).run(['$history', '$state', '$rootScope', function ($history, $state, $rootScope) {
        $rootScope.$on("$stateChangeSuccess", function (event, to, toParams, from, fromParams) {
            if (from.abstract !== true) {
                $history.push(from, fromParams);
            }
        });

        $history.push($state.current, $state.params);
    }]);