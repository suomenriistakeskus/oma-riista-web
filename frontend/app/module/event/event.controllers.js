'use strict';

angular.module('app.event.controllers', ['ui.router', 'app.event.services'])
    .controller('EventListController',
        function ($scope, $uibModal, Helpers, NotificationService, Events, EventTypes, Venues, orgId) {
            $scope.events = [];
            var reloadPage = function () {
                Events.query({orgId: orgId}).$promise.then(function (data) {
                    $scope.events = data;
                });
            };
            reloadPage();

            var onSuccess = function() {
                reloadPage();

                NotificationService.showDefaultSuccess();
            };

            var onFailure = function(reason) {
                if (reason === 'error') {
                    NotificationService.showDefaultFailure();
                }
            };

            $scope.addEvent = function () {
                $uibModal.open({
                    templateUrl: 'event/event_form.html',
                    resolve: {
                        orgId: Helpers.wrapToFunction(orgId),
                        eventTypes: Helpers.wrapToFunction(EventTypes),
                        event: Helpers.wrapToFunction({}),
                        venues: function () {
                            return Venues.query({orgId: orgId});
                        }
                    },
                    controller: 'EventFormController'
                }).result.then(onSuccess, onFailure);

            };

            $scope.clone = function (event) {
                var newEvent = angular.copy(event);
                delete newEvent.id;
                delete newEvent.rev;
                delete newEvent.date;
                $scope.show(newEvent);
            };

            $scope.show = function (event) {
                $uibModal.open({
                    templateUrl: 'event/event_form.html',
                    resolve: {
                        orgId: Helpers.wrapToFunction(orgId),
                        eventTypes: Helpers.wrapToFunction(EventTypes),
                        event: Helpers.wrapToFunction(angular.copy(event)),
                        venues: function () {
                            return Venues.query({orgId: orgId}).$promise;
                        }
                    },
                    controller: 'EventFormController'
                }).result.then(onSuccess, onFailure);

            };
            $scope.remove = function (event) {
                $uibModal.open({
                    templateUrl: 'event/event_remove.html',
                    resolve: {
                        orgId: Helpers.wrapToFunction(orgId),
                        event: Helpers.wrapToFunction(event)
                    },
                    controller: 'EventRemoveController'
                }).result.then(onSuccess, onFailure);
            };
        })

    .controller('EventFormController',
        function ($scope, $uibModalInstance, Events, venues, eventTypes, orgId, event, Helpers) {
            $scope.venues = venues;
            $scope.event = event;

            for (var i = 0; i < venues.length; i++) {
                if (venues[i].id === event.venue.id) {
                    event.venue = venues[i];
                    break;
                }
            }

            $scope.eventTypes = eventTypes.data;
            $scope.save = function (event) {
                event = angular.copy(event);// prevent ui showing updated object properties on save
                event.date = Helpers.dateToString(event.date);
                var saveMethod = !event.id ? Events.save : Events.update;

                saveMethod({orgId: orgId}, event).$promise
                    .then(function() {
                        $uibModalInstance.close();
                    }, function() {
                        $uibModalInstance.dismiss('error');
                    });
            };
            $scope.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        })
    .controller('EventRemoveController',
        function ($scope, $uibModalInstance, Events, orgId, event) {
            $scope.event = event;
            $scope.remove = function () {
                Events.delete({orgId: orgId}, event).$promise
                    .then(function() {
                        $uibModalInstance.close();
                    }, function() {
                        $uibModalInstance.dismiss('error');
                    });
            };
            $scope.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        })
    .controller('VenueListController',
        function ($scope, $uibModal, Helpers, NotificationService, Venues, orgId) {
            var reloadPage = function () {
                $scope.venues = Venues.query({orgId: orgId});
            };

            reloadPage();

            var onSuccess = function() {
                reloadPage();

                NotificationService.showDefaultSuccess();
            };

            var onFailure = function(reason) {
                if (reason === 'error') {
                    NotificationService.showDefaultFailure();
                }
            };

            $scope.addVenue = function () {
                $uibModal.open({
                    templateUrl: 'event/venue_add.html',
                    resolve: {
                        orgId: Helpers.wrapToFunction(orgId)
                    },
                    controller: 'VenueAddController'
                }).result.then(onSuccess, onFailure);

            };
            $scope.show = function (venue) {
                $uibModal.open({
                    templateUrl: 'event/venue_form.html',
                    resolve: {
                        orgId: Helpers.wrapToFunction(orgId),
                        venue: Helpers.wrapToFunction(angular.copy(venue))
                    },
                    controller: 'VenueFormController'
                }).result.then(onSuccess, onFailure);
            };
            $scope.remove = function (venue) {
                $uibModal.open({
                    templateUrl: 'event/venue_remove.html',
                    resolve: {
                        orgId: Helpers.wrapToFunction(orgId),
                        venue: Helpers.wrapToFunction(venue)
                    },
                    controller: 'VenueRemoveController'
                }).result.then(onSuccess, onFailure);
            };
        })
    .controller('VenueAddController',
        function ($scope, $uibModalInstance, $uibModal, Helpers, Venues, VenueSearchByName, orgId) {
            $scope.venue = {};
            var updatePager = function () {
                $scope.pager = {
                    currentPage: $scope.page.pageable.page ? $scope.page.pageable.page + 1 : 0,
                    pageSize: $scope.page.pageable.size,
                    total: $scope.page.total
                };
            };
            var noContent = function () {
                $scope.page = {content: [], pageable: {size: 5}, total: 0};
                updatePager();
            };
            noContent();
            var cb = function (page) {
                $scope.page = page;
                updatePager();
            };
            var reloadPage = function (keyTyped) {
                if ($scope.venue.name) {
                    var params = {
                        page: ($scope.pager.currentPage - 1),
                        size: ($scope.pager.pageSize),
                        sort: 'name',
                        term: $scope.venue.name
                    };
                    if (keyTyped) {
                        params.page = 0;
                        VenueSearchByName.search(params, cb);
                    } else {
                        VenueSearchByName.searchImmediately(params, cb);
                    }
                }
            };
            $scope.$watch('venue.name', function () {
                reloadPage(true);
            });
            $scope.$watch('pager.currentPage', function () {
                reloadPage(false);
            });

            $scope.use = function (venue) {
                Venues.attach({orgId: orgId, id: venue.id}).$promise
                    .then(function() {
                        $uibModalInstance.close();
                    }, function() {
                        $uibModalInstance.dismiss('error');
                    });
            };
            $scope.addNew = function () {
                $uibModal.open({
                    templateUrl: 'event/venue_form.html',
                    resolve: {
                        orgId: Helpers.wrapToFunction(orgId),
                        venue: Helpers.wrapToFunction($scope.venue)
                    },
                    controller: 'VenueFormController'
                }).result
                    .then(function() {
                        $uibModalInstance.close();
                    }, function() {
                        $uibModalInstance.dismiss('error');
                    });
            };
            $scope.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        })
    .controller('VenueFormController',
        function ($scope, $uibModalInstance, Venues, venue, orgId) {
            $scope.venue = venue;
            $scope.save = function (venue) {
                var saveMethod = !venue.id ? Venues.save : Venues.update;

                saveMethod({orgId: orgId}, venue).$promise
                    .then(function() {
                        $uibModalInstance.close();
                    }, function() {
                        $uibModalInstance.dismiss('error');
                    });

                $scope.cancel = function () {
                    $uibModalInstance.dismiss('cancel');
                };
            };
            $scope.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        })
    .controller('VenueRemoveController',
        function ($scope, $uibModalInstance, Venues, orgId, venue) {
            $scope.venue = venue;
            $scope.remove = function () {
                Venues.detach({orgId: orgId, id: venue.id}).$promise
                    .then(function() {
                        $uibModalInstance.close();
                    }, function() {
                        $uibModalInstance.dismiss('error');
                    });
            };
            $scope.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };
        });
