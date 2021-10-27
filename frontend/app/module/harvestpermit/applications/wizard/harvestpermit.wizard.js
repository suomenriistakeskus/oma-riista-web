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
                case 'MAMMAL':
                    return 'mammal';
                case 'NEST_REMOVAL':
                    return 'nestremoval';
                case 'LAW_SECTION_TEN':
                    return 'lawsectionten';
                case 'WEAPON_TRANSPORTATION':
                    return 'weapontransportation';
                case 'DISABILITY':
                    return 'disability';
                case 'DOG_UNLEASH':
                    return 'dogunleash';
                case 'DOG_DISTURBANCE':
                    return 'dogdisturbance';
                case 'DEPORTATION':
                    return 'deportation';
                case 'RESEARCH':
                    return 'research';
                case 'IMPORTING':
                    return 'importing';
                case 'GAME_MANAGEMENT':
                    return "gamemanagement";
                default:
                    console.log("Unsupported application type: " + applicationType);
                    throw Error('Unknown permit type');
            }
        };
    })
    .service('HarvestPermitApplicationSummaryService', function (MooselikePermitApplication, BirdPermitApplication,
                                                                 CarnivorePermitApplication, MammalPermitApplication,
                                                                 NestRemovalPermitApplication, LawSectionTenPermitApplication,
                                                                 WeaponTransportationPermitApplication,
                                                                 HarvestPermitWizardSelectorService, DisabilityPermitApplication,
                                                                 DogDisturbanceApplication, DogUnleashApplication,
                                                                 DeportationPermitApplication, ResearchPermitApplication,
                                                                 ImportingPermitApplication, GameManagementPermitApplication) {
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
                case 'mammal':
                    return MammalPermitApplication.getFullDetails({id: applicationId}).$promise;
                case 'nestremoval':
                    return NestRemovalPermitApplication.getFullDetails({id: applicationId}).$promise;
                case 'lawsectionten':
                    return LawSectionTenPermitApplication.getFullDetails({id: applicationId}).$promise;
                case 'weapontransportation':
                    return WeaponTransportationPermitApplication.getFullDetails({id: applicationId}).$promise;
                case 'disability':
                    return DisabilityPermitApplication.getFullDetails({id: applicationId}).$promise;
                case 'dogdisturbance':
                    return DogDisturbanceApplication.getFullDetails({id: applicationId}).$promise;
                case 'dogunleash':
                    return DogUnleashApplication.getFullDetails({id: applicationId}).$promise;
                case 'deportation':
                    return DeportationPermitApplication.getFullDetails({id: applicationId}).$promise;
                case 'research':
                    return ResearchPermitApplication.getFullDetails({id: applicationId}).$promise;
                case 'importing':
                    return ImportingPermitApplication.getFullDetails({id: applicationId}).$promise;
                case 'gamemanagement':
                    return GameManagementPermitApplication.getFullDetails({id: applicationId}).$promise;
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
        controller: function (HarvestPermitCategoryType) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.moosetypes = HarvestPermitCategoryType.getMooseTypes($ctrl.applicationTypes);
                $ctrl.otherHarvestPermitTypes = HarvestPermitCategoryType.getOtherHarvestPermitTypes($ctrl.applicationTypes);
                $ctrl.damageBasedDerogations = HarvestPermitCategoryType.getDamageBasedDerogationTypes($ctrl.applicationTypes);
                $ctrl.otherDerogations = HarvestPermitCategoryType.getOtherDerogationTypes($ctrl.applicationTypes);
                $ctrl.otherPermitTypes = HarvestPermitCategoryType.getOtherPermitTypes($ctrl.applicationTypes);
                $ctrl.dogEventPermitTypes = HarvestPermitCategoryType.getDogEventPermitTypes($ctrl.applicationTypes);
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
        controller: function (HarvestPermitWizardSelectorService, HarvestPermitCategoryType) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.wizardType = HarvestPermitWizardSelectorService.getWizardName($ctrl.selectedCategory);
            };

            $ctrl.hasPermission = function () {
                return HarvestPermitCategoryType.hasPermission($ctrl.selectedCategory);
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
        controller: function (ActiveRoleService, GameSpeciesCodes, isProductionEnvironment, HarvestPermitCategoryType) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                var isModerator = ActiveRoleService.isModerator();
                $ctrl.imageFileName = resolveImageFileName($ctrl.type.category);
                $ctrl.imagePath = resolveImagePath($ctrl.type.category);
                $ctrl.isActive = function () {
                    return isModerator || $ctrl.type.active || !isProductionEnvironment;
                };
            };

            $ctrl.isLargeCarnivoreLynx = function () {
                return $ctrl.type.category === 'LARGE_CARNIVORE_LYNX';
            };

            $ctrl.hasPermission = function () {
                return HarvestPermitCategoryType.hasPermission($ctrl.type.category);
            };

            function resolveImageFileName(category) {
                switch (category) {
                    case 'MOOSELIKE':
                        return GameSpeciesCodes.MOOSE;
                    case 'BIRD':
                        return GameSpeciesCodes.BEAN_GOOSE;
                    case 'LARGE_CARNIVORE_BEAR':
                        return GameSpeciesCodes.BEAR;
                    case 'LARGE_CARNIVORE_LYNX':
                        return GameSpeciesCodes.LYNX;
                    case 'LARGE_CARNIVORE_LYNX_PORONHOITO':
                        return GameSpeciesCodes.LYNX;
                    case 'LARGE_CARNIVORE_WOLF':
                        return GameSpeciesCodes.WOLF;
                    case 'MAMMAL':
                        return GameSpeciesCodes.BROWN_HARE;
                    case 'NEST_REMOVAL':
                        return GameSpeciesCodes.EUROPEAN_BEAVER;
                    case 'LAW_SECTION_TEN':
                        return 'section10';
                    case 'WEAPON_TRANSPORTATION':
                        return 'weapon_transportation';
                    case 'DISABILITY':
                        return 'disability';
                    case 'DOG_UNLEASH':
                        return 'dog_unleash';
                    case 'DOG_DISTURBANCE':
                        return 'dog_disturbance';
                    case 'DEPORTATION':
                        return 'deportation';
                    case 'RESEARCH':
                        return 'research';
                    case 'IMPORTING':
                        return 'importing';
                    case 'GAME_MANAGEMENT':
                        return 'game_management';
                    default:
                        console.log('Illegal type ' + category);
                        return null;
                }
            }

            function resolveImagePath(category) {
                switch (category) {
                    case 'MOOSELIKE':
                    case 'BIRD':
                    case 'LARGE_CARNIVORE_BEAR':
                    case 'LARGE_CARNIVORE_LYNX':
                    case 'LARGE_CARNIVORE_LYNX_PORONHOITO':
                    case 'LARGE_CARNIVORE_WOLF':
                    case 'MAMMAL':
                    case 'NEST_REMOVAL':
                        return 'elainlajikuvat';
                    case 'DOG_UNLEASH':
                    case 'DOG_DISTURBANCE':
                    case 'WEAPON_TRANSPORTATION':
                    case 'DISABILITY':
                    case 'DEPORTATION':
                    case 'RESEARCH':
                    case 'IMPORTING':
                    case 'GAME_MANAGEMENT':
                    case 'LAW_SECTION_TEN':
                        return 'permitselectionimages';
                    default:
                        console.log('Illegal type ' + category);
                        return null;
                }
            }

        }
    })
    .component('applicationTypeLargeCarnivoreLynx', {
        templateUrl: 'harvestpermit/applications/wizard/application-type-lynx.html',
        bindings: {
            type: '<',
            onSelectType: '&'
        },
        controllerAs: '$ctrl',
        controller: function (ActiveRoleService, isProductionEnvironment) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                var isModerator = ActiveRoleService.isModerator();
                $ctrl.isActive = function () {
                    return isModerator || $ctrl.type.active || !isProductionEnvironment;
                };
            };
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


    })
    .directive('rValidateGreaterOrEqual', function () {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, elem, attrs, ctrl) {
                scope.$watchGroup([attrs.rValidateGreaterOrEqual, attrs.ngModel], function (newValues) {
                    if (_.isNil(newValues[0]) || _.isNil(newValues[1])) {
                        // One or both value is not defined and validity cannot be checked.
                        // Must set as valid, otherwise optional field can be invalid state when empty.
                        ctrl.$setValidity('greaterOrEqual', true);
                    } else {
                        // Compare values. Note, works with date strings (YYYY-MM-DD) too.
                        ctrl.$setValidity('greaterOrEqual', newValues[0] <= newValues[1]);
                    }
                });
            }
        };
    });
