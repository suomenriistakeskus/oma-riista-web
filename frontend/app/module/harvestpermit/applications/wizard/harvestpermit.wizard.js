'use strict';

angular.module('app.harvestpermit.application.wizard', ['app.metadata'])
    .config(function ($stateProvider) {
        $stateProvider
            .state('profile.newpermit', {
                url: '/newpermit',
                templateUrl: 'harvestpermit/applications/wizard/new.html',
                controller: 'HarvestPermitWizardTypeController',
                controllerAs: '$ctrl',
                resolve: {
                    applicationTypes: function (HarvestPermitApplications) {
                        return HarvestPermitApplications.listTypes().$promise;
                    },
                    personId: function ($stateParams, ActiveRoleService) {
                        return ActiveRoleService.isModerator() ? $stateParams.id : null;
                    }
                }
            })
            .state('profile.permitwizard', {
                url: '/wizard/{applicationId:[0-9]{1,8}}',
                template: '<div ui-view autoscroll="false"></div>',
                abstract: true,
                controllerAs: '$ctrl',
                controller: function (applicationBasicDetails) {
                    this.applicationBasicDetails = applicationBasicDetails;
                },
                resolve: {
                    applicationId: function ($stateParams) {
                        return _.parseInt($stateParams.applicationId);
                    },
                    applicationBasicDetails: function (HarvestPermitApplications, applicationId) {
                        return HarvestPermitApplications.get({id: applicationId}).$promise;
                    }
                }
            });
    })
    .service('HarvestPermitWizardSelectorService', function () {
        this.getWizardName = function (applicationType) {
            switch (applicationType) {
                case 'MOOSELIKE':
                    return 'mooselike';
                case 'MOOSELIKE_NEW':
                    return 'amendment';
                case 'BIRD':
                    return 'bird';
                case 'LARGE_CARNIVORE_BEAR':
                case 'LARGE_CARNIVORE_LYNX':
                case 'LARGE_CARNIVORE_LYNX_PORONHOITO':
                case 'LARGE_CARNIVORE_WOLF':
                    return 'carnivore';
                default:
                    console.log("Unsupported application type: " + applicationType);
                    throw Error('Unknown permit type');
            }
        };
    })
    .service('HarvestPermitApplicationSummaryService', function (MooselikePermitApplication, BirdPermitApplication,
                                                                 CarnivorePermitApplication, HarvestPermitWizardSelectorService) {
        this.getApplicationSummary = function (applicationId, applicationType) {
            var wizard = HarvestPermitWizardSelectorService.getWizardName(applicationType);
            switch (wizard) {
                case 'mooselike':
                case 'amendment':
                    return MooselikePermitApplication.getFullDetails({id: applicationId}).$promise;
                case 'bird':
                    return BirdPermitApplication.getFullDetails({id: applicationId}).$promise;
                case 'carnivore':
                    return CarnivorePermitApplication.getFullDetails({id: applicationId}).$promise;
                default:
                    console.log("Unsupported application type: " + applicationType);
                    throw Error('Unknown permit type');
            }
        };
    })
    .factory('ApplicationWizardNavigationHelper', function ($translate, dialogs, UnsavedChangesConfirmationService) {
        function showConfirmationDialog(titleKey, bodyKey) {

            var modalTitle = $translate.instant(titleKey);
            var modalBody = $translate.instant(bodyKey);

            return dialogs.confirm(modalTitle, modalBody).result;
        }

        function performWithConfirmation(predicate, save, performedFunction, titleKey, bodyKey) {
            if (predicate) {
                showConfirmationDialog(titleKey, bodyKey).then(function () {
                    UnsavedChangesConfirmationService.setChanges(false);
                    performedFunction();

                }, function () {

                });
            } else {
                UnsavedChangesConfirmationService.setChanges(false);
                save().then(function () {
                    performedFunction();
                });
            }
        }

        return {
            previous: function (predicate, save, previousFunction) {
                var titleKey = 'harvestpermit.wizard.navigation.previousConfirmation.title';
                var bodyKey = 'harvestpermit.wizard.navigation.previousConfirmation.body';
                performWithConfirmation(predicate, save, previousFunction, titleKey, bodyKey);
            },
            exit: function (predicate, save, exitFunction) {
                var titleKey = 'harvestpermit.wizard.navigation.exitConfirmation.title';
                var bodyKey = 'harvestpermit.wizard.navigation.exitConfirmation.body';
                performWithConfirmation(predicate, save, exitFunction, titleKey, bodyKey);
            }

        };
    })
    .component('permitApplicationWizardNavigation', {
        templateUrl: 'harvestpermit/applications/wizard/wizard-navigation.html',
        bindings: {
            previous: '&',
            previousDisabled: '&',
            next: '&',
            nextDisabled: '&',
            nextTitle: '@',
            exit: '&'
        }
    })

    .component('permitApplicationWizardContactPerson', {
        templateUrl: 'harvestpermit/applications/wizard/contact-person.html',
        bindings: {
            person: '<'
        }
    })

    .component('permitApplicationWizardApplicantType', {
        templateUrl: 'harvestpermit/applications/wizard/applicant-type.html',
        bindings: {
            subtype: '<'
        }
    })
    .component('permitApplicationSelectType', {
        templateUrl: 'harvestpermit/applications/wizard/select-application-type.html',
        bindings: {
            applicationTypes: '<',
            onSelect: '&'
        },
        controller: function () {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.moosetypes = _.filter($ctrl.applicationTypes, _.matchesProperty('category', 'MOOSELIKE'));
                $ctrl.derogationTypes = _.filter($ctrl.applicationTypes, function (t) {
                    return t.category !== 'MOOSELIKE';
                });

            };
        }
    })
    .component('permitApplicationSelectName', {
        templateUrl: 'harvestpermit/applications/wizard/select-application-name.html',
        bindings: {
            selectedCategory: '<',
            applicationName: '<',
            selectedCategoryPrice: '<',
            onCreate: '&'
        },
        controller: function (HarvestPermitWizardSelectorService) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.wizardType = HarvestPermitWizardSelectorService.getWizardName($ctrl.selectedCategory);
            };
        }
    })
    .component('permitApplicationDeliverySelect', {
        templateUrl: 'harvestpermit/applications/wizard/select-delivery-method.html',
        bindings: {
            deliveryByMail: '<',
            onSelectMethod: '&'
        }
    })
    .component('permitApplicationLanguageSelect', {
        templateUrl: 'harvestpermit/applications/wizard/select-delivery-language.html',
        bindings: {
            selectedLanguage: '<',
            onSelectLanguage: '&'
        }
    })
    .component('applicationType', {
        templateUrl: 'harvestpermit/applications/wizard/application-type.html',
        bindings: {
            type: '<',
            onSelectType: '&'
        },
        controllerAs: '$ctrl',
        controller: function (ActiveRoleService, GameSpeciesCodes, isProductionEnvironment) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                var isModerator = ActiveRoleService.isModerator();
                $ctrl.speciesCode = resolveSpeciesCode($ctrl.type.category);
                $ctrl.isActive = function () {
                    return isModerator || $ctrl.type.active || !isProductionEnvironment;
                };
            };

            function resolveSpeciesCode(category) {
                if (category === 'MOOSELIKE') {
                    return GameSpeciesCodes.MOOSE;
                } else if (category === 'BIRD') {
                    return GameSpeciesCodes.BEAN_GOOSE;
                } else if (category === 'LARGE_CARNIVORE_BEAR') {
                    return GameSpeciesCodes.BEAR;
                } else if (category === 'LARGE_CARNIVORE_LYNX') {
                    return GameSpeciesCodes.LYNX;
                } else if (category === 'LARGE_CARNIVORE_LYNX_PORONHOITO') {
                    return GameSpeciesCodes.LYNX;
                } else if (category === 'LARGE_CARNIVORE_WOLF') {
                    return GameSpeciesCodes.WOLF;
                } else {
                    console.log('Illegal type ' + category);
                    return null;
                }
            }

        }
    })
    .controller('HarvestPermitWizardTypeController', function ($state, $window, HarvestPermitApplications,
                                                               HarvestPermitWizardSelectorService,
                                                               $translate, applicationTypes, personId) {
        var $ctrl = this;

        $ctrl.$onInit = function () {
            $ctrl.applicationTypes = applicationTypes;
            $ctrl.typeSelected = false;
            $ctrl.selectedCategory = '';
            $ctrl.applicationName = '';
        };

        $ctrl.selectType = function (applicationType) {
            $ctrl.selectedCategory = applicationType.category;
            $ctrl.selectedCategoryPrice = applicationType.price;
            $ctrl.selectedCategoryHuntingYear = applicationType.huntingYear;
            $ctrl.applicationName = getDefaultApplicationNameForType(applicationType);

            $ctrl.typeSelected = true;
            $window.scrollTo(0, 0);
        };

        $ctrl.createApplication = function (name) {
            HarvestPermitApplications.save({
                applicationName: name,
                category: $ctrl.selectedCategory,
                huntingYear: $ctrl.selectedCategoryHuntingYear,
                personId: personId
            }).$promise.then(function (res) {
                $state.go('profile.permitwizard.' +
                    HarvestPermitWizardSelectorService.getWizardName($ctrl.selectedCategory) + '.applicant', {
                    applicationId: res.id
                });
            });
        };

        function getDefaultApplicationNameForType(applicationType) {
            var name = $translate.instant('harvestpermit.mine.applications.permitCategory.' + applicationType.category);
            return name + ' ' + (applicationType.huntingYear);
        }


    });
