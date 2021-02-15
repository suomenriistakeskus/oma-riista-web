'use strict';

angular.module('app.harvestpermit.application.dogdisturbance.eventdetails', ['app.metadata'])
    .config(function ($stateProvider) {

        function sortContacts(event) {
            if (!_.isNil(event) && !_.isNil(event.contacts)) {
                event.contacts = _.sortBy(event.contacts, ['id']);
            }
            return event;
        }

        $stateProvider
            /**
             * Training event states
             */

            .state('profile.permitwizard.dogdisturbance.trainingdetails', {
                url: '/trainingdetails',
                templateUrl: 'harvestpermit/applications/dogdisturbance/eventdetails/eventdetails.html',
                controller: 'DogDisturbanceEventDetailsController',
                controllerAs: '$ctrl',
                resolve: {
                    event: function (applicationId, DogDisturbanceApplication, DogEventType) {
                        return DogDisturbanceApplication.getEventDetails({id: applicationId, eventType: DogEventType.DOG_TRAINING}).$promise
                            .then(sortContacts);
                    },
                    trainingEventInfo: function () {
                        // Just use constant values in this state.
                        return {
                            canBeSkipped: true,
                            contacts: []
                        };
                    },
                    eventType: function (DogEventType) {
                        return DogEventType.DOG_TRAINING;
                    },
                    states: function () {
                        return {
                            next: 'testdetails',
                            previous: 'mapdetails'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.dogdisturbance.trainingdetails', {
                url: '/trainingdetails',
                templateUrl: 'harvestpermit/applications/dogdisturbance/eventdetails/eventdetails.html',
                controller: 'DogDisturbanceEventDetailsController',
                controllerAs: '$ctrl',
                resolve: {
                    event: function (applicationId, DogDisturbanceApplication, DogEventType) {
                        return DogDisturbanceApplication.getEventDetails({id: applicationId, eventType: DogEventType.DOG_TRAINING}).$promise
                            .then(sortContacts);
                    },
                    trainingEventInfo: function () {
                        // Just use constant values in this state.
                        return {
                            canBeSkipped: true,
                            contacts: []
                        };
                    },
                    eventType: function (DogEventType) {
                        return DogEventType.DOG_TRAINING;
                    },
                    states: function () {
                        return {
                            next: 'testdetails',
                            previous: 'mapdetails'
                        };
                    }
                }
            })

            /**
             * Test event states
             */

            .state('profile.permitwizard.dogdisturbance.testdetails', {
                url: '/testdetails',
                templateUrl: 'harvestpermit/applications/dogdisturbance/eventdetails/eventdetails.html',
                controller: 'DogDisturbanceEventDetailsController',
                controllerAs: '$ctrl',
                resolve: {
                    event: function (applicationId, DogDisturbanceApplication, DogEventType) {
                        return DogDisturbanceApplication.getEventDetails({id: applicationId, eventType: DogEventType.DOG_TEST}).$promise
                            .then(sortContacts);
                    },
                    trainingEventInfo: function (applicationId, DogDisturbanceApplication, DogEventType) {
                        return DogDisturbanceApplication.getEventDetails({id: applicationId, eventType: DogEventType.DOG_TRAINING}).$promise
                            .then(function (event) {
                                return {
                                    canBeSkipped: !event.skipped,
                                    contacts: _.isNil(event.contacts) ? [] : event.contacts
                                };
                            });
                    },
                    eventType: function (DogEventType) {
                        return DogEventType.DOG_TEST;
                    },
                    states: function () {
                        return {
                            next: 'attachments',
                            previous: 'trainingdetails'
                        };
                    }
                }
            })
            .state('jht.decision.application.wizard.dogdisturbance.testdetails', {
                url: '/testdetails',
                templateUrl: 'harvestpermit/applications/dogdisturbance/eventdetails/eventdetails.html',
                controller: 'DogDisturbanceEventDetailsController',
                controllerAs: '$ctrl',
                resolve: {
                    event: function (applicationId, DogDisturbanceApplication, DogEventType) {
                        return DogDisturbanceApplication.getEventDetails({id: applicationId, eventType: DogEventType.DOG_TEST}).$promise
                            .then(sortContacts);
                    },
                    trainingEventInfo: function (applicationId, DogDisturbanceApplication, DogEventType) {
                        return DogDisturbanceApplication.getEventDetails({id: applicationId, eventType: DogEventType.DOG_TRAINING}).$promise
                            .then(function (event) {
                                return {
                                    canBeSkipped: !event.skipped,
                                    contacts: _.isNil(event.contacts) ? [] : event.contacts
                                };
                            });
                    },
                    eventType: function (DogEventType) {
                        return DogEventType.DOG_TEST;
                    },
                    states: function () {
                        return {
                            next: 'attachments',
                            previous: 'trainingdetails'
                        };
                    }
                }
            });
    })

    /**
     * Common for both events
     */

    .controller('DogDisturbanceEventDetailsController', function ($state, wizard, applicationId, event,
                                                                  trainingEventInfo, eventType, states,
                                                                  DogDisturbanceApplication, DogEventImportContactsModal,
                                                                  ApplicationWizardNavigationHelper,
                                                                  GameSpeciesCodes) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.eventType = eventType;
            $ctrl.importContactsEnabled = false;
            $ctrl.canBeSkipped = trainingEventInfo.canBeSkipped;
            $ctrl.contactsForImport = trainingEventInfo.contacts;
            var newEvent = {
                contacts: [{invalid: true}],
                eventType: eventType,
                speciesCode: GameSpeciesCodes.BEAR
            };

            $ctrl.event = _.isNil(event.id) ? newEvent : event;

            $ctrl.event.speciesCode = _.isNil($ctrl.event.speciesCode) ? GameSpeciesCodes.BEAR : $ctrl.event.speciesCode;
            // Add empty contact if event was skipped
            if (_.isNil($ctrl.event.contacts) || _.isEmpty($ctrl.event.contacts)) {
                $ctrl.event.contacts = [{invalid: true}];
            }

            $ctrl.species = [
                GameSpeciesCodes.BEAR,
                GameSpeciesCodes.LYNX,
                GameSpeciesCodes.OTTER,
                GameSpeciesCodes.WOLF
            ];
            $ctrl.beginDateOptions = { minDate: new Date() };
            $ctrl.endDateOptions = { minDate: new Date() };
            $ctrl.updateDatePickerLimits();
            $ctrl.updateImportContactsEnabled();
        };

        $ctrl.exit = function (form) {
            ApplicationWizardNavigationHelper.exit($ctrl.isNextDisabled(form), $ctrl.save, wizard.exit);
        };

        $ctrl.previous = function (form) {
            ApplicationWizardNavigationHelper.previous($ctrl.isNextDisabled(form), $ctrl.save, $ctrl.doGotoPrevious);
        };

        $ctrl.doGotoPrevious = function () {
            wizard.goto(states.previous);
        };

        $ctrl.isNextDisabled = function (form) {
            return !$ctrl.event.skipped
                && (hasNoContacts()
                    || hasInvalidContacts()
                    || form.$invalid)
                || ($ctrl.event.skipped && !$ctrl.canBeSkipped);
        };

        $ctrl.next = function () {
            $ctrl.save().then(function () {
                wizard.goto(states.next);
            });
        };

        $ctrl.save = function () {
            if ($ctrl.event.skipped) {
                $ctrl.event = _.pick($ctrl.event, ['id', 'eventType', 'skipped']);
            } else {
                $ctrl.event.contacts = $ctrl.event.contacts.map(function (c) {
                    return _.pick(c, ['id', 'name', 'mail', 'phone']);
                });
            }
            return DogDisturbanceApplication.updateEventDetails({id: applicationId}, $ctrl.event).$promise;
        };

        $ctrl.addEmptyContact = function () {
            $ctrl.event.contacts.push({invalid: true});
        };

        $ctrl.onDelete = function (index) {
            $ctrl.event.contacts.splice(index, 1);
            $ctrl.updateImportContactsEnabled();
        };

        $ctrl.deleteEnabled = function () {
            return $ctrl.event.contacts.length > 1;
        };

        $ctrl.importContacts = function () {
            DogEventImportContactsModal.open(
                $ctrl.nonImportedContacts($ctrl.event.contacts),
                onImport);
        };

        function onImport(contact) {
            var newContact = _.pick(contact, ['name', 'mail', 'phone']);

            // Remove initial, empty contact entry when the first contact is been imported.
            if ($ctrl.event.contacts.length === 1 && _.isEqual($ctrl.event.contacts[0], {})) {
                $ctrl.event.contacts = [];
            }
            $ctrl.event.contacts.push(newContact);
            removeEmptyContacts();
            $ctrl.updateImportContactsEnabled();
        }

        function removeEmptyContacts() {
            $ctrl.event.contacts = $ctrl.event.contacts.filter(function (contact) {
                return !(_.isEmpty(contact.name) && _.isEmpty(contact.email) && _.isEmpty(contact.phone));
            });
        }

        $ctrl.nonImportedContacts = function (contactList) {
            return _.differenceWith($ctrl.contactsForImport, contactList, function (value, other) {
                return value.name === other.name
                    && value.phone === other.phone
                    && value.mail === other.mail;
            });
        };

        $ctrl.updateImportContactsEnabled = function () {
            $ctrl.importContactsEnabled = $ctrl.nonImportedContacts($ctrl.event.contacts).length > 0;
        };

        $ctrl.updateDatePickerLimits = function () {
            $ctrl.endDateOptions.minDate = _.isNil($ctrl.event.beginDate) ? new Date() : new Date($ctrl.event.beginDate);
        };

        function hasNoContacts() {
            return _.isNull($ctrl.event) && $ctrl.event.contacts.length < 1;
        }

        function hasInvalidContacts() {
            return _.some($ctrl.event.contacts, 'invalid');
        }

        $ctrl.validateIsGreaterOrEqual = function (field, other) {
            var value = field.$modelValue;
            if (!_.isNil(value) && !_.isNil(other)) {
                field.$setValidity('greaterOrEqual', other <= value);
            }
        };
    })
    .component('rDogEventContact', {
        templateUrl: 'harvestpermit/applications/dogdisturbance/eventdetails/contact.html',
        bindings: {
            contact: '<',
            onChange: '&',
            onDelete: '&',
            deleteEnabled: '&'
        },
        controller: function ($scope) {
            var $ctrl = this;

            $ctrl.delete = function () {
                $ctrl.onDelete();
            };

            $ctrl.change = function () {
                $ctrl.contact.invalid = $scope.contactForm.$invalid;
                $ctrl.onChange();
            };
        }
    })
    .service('DogEventImportContactsModal', function ($uibModal) {

        this.open = function (contacts, onImport) {
            $uibModal.open({templateUrl: 'harvestpermit/applications/dogdisturbance/eventdetails/import.html',
                               controllerAs: '$modalCtrl',
                               resolve: { contacts: _.constant(contacts), onImport: _.constant(onImport) },
                               size: 'lg',
                               controller: ModalController});
        };

        function ModalController($uibModalInstance, contacts, onImport) {
            var $modalCtrl = this;
            $modalCtrl.contacts = contacts;

            $modalCtrl.ok = function () {
                $uibModalInstance.close();
            };

            $modalCtrl.import = function (contactId) {
                onImport(_.find($modalCtrl.contacts, {id: contactId}));
                $modalCtrl.contacts = _.filter($modalCtrl.contacts, function(c) { return c.id !== contactId; });
                if ($modalCtrl.contacts.length === 0) {
                    $uibModalInstance.close();
                }
            };

            $modalCtrl.importAll = function () {
                $modalCtrl.contacts.forEach(function (contact) {
                    onImport(contact);
                });
                $modalCtrl.contacts = [];
                $uibModalInstance.close();
            };
        }
    });
