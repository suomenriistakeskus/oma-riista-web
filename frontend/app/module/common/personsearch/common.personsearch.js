'use strict';

angular.module('app.common.personsearch', [])
    .service('PersonSearchService', function (HttpPost) {
        this.findByHunterNumber = function (hunterNumber) {
            return HttpPost.post('api/v1/search/person/hunternumber', {hunterNumber: hunterNumber});
        };

        this.findBySSN = function (ssn) {
            return HttpPost.post('api/v1/search/person/ssn', {ssn: ssn});
        };

        this.findByPermitNumber = function (permitNumber) {
            return HttpPost.post('api/v1/search/person/permitnumber', {permitNumber: permitNumber});
        };

        this.findByPersonName = function (name) {
            return HttpPost.post('api/v1/search/person/name', {name: name});
        };

        this.findByHunterNumberOrPersonName = function (searchTerm) {
            return HttpPost.post('api/v1/search/person', {searchTerm: searchTerm});
        };
    })

    .service('PersonSearchModal', function ($q, $uibModal, PersonSearchService) {
        this.searchPerson = function (showSsnSearch, showPermitNumberSearch) {
            return $uibModal.open({
                size: 'lg',
                templateUrl: 'common/personsearch/findperson.html',
                controller: ModalController,
                controllerAs: '$ctrl',
                bindToController: true,
                resolve: {
                    showSsnSearch: _.constant(showSsnSearch),
                    showPermitNumberSearch: _.constant(showPermitNumberSearch)
                }
            }).result;
        };

        function ModalController($uibModalInstance,
                                 showSsnSearch, showPermitNumberSearch) {
            var $ctrl = this;

            $ctrl.$onInit = function () {
                $ctrl.personWithHunterNumber = null;
                $ctrl.isPersonSearchLoading = false;
                $ctrl.notFound = false;
                $ctrl.showSsnSearch = showSsnSearch;
                $ctrl.showPermitNumberSearch = showPermitNumberSearch;
            };

            var found = function (response) {
                $ctrl.personWithHunterNumber = response.data;
                $ctrl.notFound = false;
            };

            var notFound = function () {
                $ctrl.personWithHunterNumber = null;
                $ctrl.notFound = true;
            };

            $ctrl.onHunterNumberChange = function (hunterNumber) {
                $ctrl.personWithHunterNumber = null;
                $ctrl.notFound = false;

                if (hunterNumber) {
                    $ctrl.ssn = null;
                    $ctrl.permitNumber = null;
                    $ctrl.person = null;

                    PersonSearchService.findByHunterNumber(hunterNumber).then(found, notFound);
                }
            };

            $ctrl.onSsnChange = function (ssn) {
                $ctrl.personWithHunterNumber = null;
                $ctrl.notFound = false;

                if (ssn) {
                    $ctrl.hunterNumber = null;
                    $ctrl.permitNumber = null;
                    $ctrl.person = null;

                    PersonSearchService.findBySSN(ssn).then(found, notFound);
                }
            };

            $ctrl.onPermitNumberChange = function (permitNumber) {
                $ctrl.personWithHunterNumber = null;
                $ctrl.notFound = false;

                if (permitNumber) {
                    $ctrl.ssn = null;
                    $ctrl.hunterNumber = null;
                    $ctrl.person = null;

                    PersonSearchService.findByPermitNumber(permitNumber).then(found, notFound);
                }
            };

            $ctrl.searchByName = function (name) {
                $ctrl.personWithHunterNumber = null;
                $ctrl.ssn = null;
                $ctrl.hunterNumber = null;
                $ctrl.permitNumber = null;

                return PersonSearchService.findByPersonName(name).then(function (response) {
                    return _.size(response.data) > 0 ? response.data : $q.reject();
                }).catch(function () {
                    notFound();
                });
            };

            $ctrl.getName = function (person) {
                if (person) {
                    return person.extendedName;
                }
            };

            $ctrl.onPersonSelect = function ($item, $model, $label) {
                $ctrl.personWithHunterNumber = $item;
                $ctrl.notFound = false;
            };

            $ctrl.cancel = function () {
                $uibModalInstance.dismiss('cancel');
            };

            $ctrl.ok = function () {
                $uibModalInstance.close($ctrl.personWithHunterNumber);
            };
        }
    });
