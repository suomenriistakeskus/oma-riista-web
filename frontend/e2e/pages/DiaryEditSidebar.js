var PageHelper = require('./PageHelper');

function DiaryEditSidebar() {
    'use strict';

    var pageHelper = new PageHelper();

    this.gameSpeciesSelect = element(by.model('entry.gameSpeciesCode'));
    this.dateInput = element(by.model('viewState.date'));
    this.timeInput = element(by.model('viewState.time'));
    this.genderSelect = element(by.model('entry.specimens[0].gender'));
    this.ageSelect = element(by.model('entry.specimens[0].age'));
    this.addEntrySubmitButton = element(by.css('.off-canvas-panels .buttons .btn.btn-primary'));
    this.leafletLayer = element(by.css('.angular-leaflet-map'));

    this.selectGameSpecies = function (code) {
        return pageHelper.selectDropdownItemByValue(this.gameSpeciesSelect, 'number:' + code);
    };

    this.selectDate = function (day) {
        return pageHelper.fill(this.dateInput, day);
    };

    this.selectTime = function (time) {
        return pageHelper.fill(this.timeInput, time);
    };

    this.selectGender = function (gender) {
        return pageHelper.selectDropdownItemByValue(this.genderSelect, 'string:' + gender);
    };

    this.selectAge = function (age) {
        return pageHelper.selectDropdownItemByValue(this.ageSelect, 'string:' + age);
    };
    
    this.selectMapPosition = function () {
        var self = this;
        return browser.executeAsyncScript(function (cb) {
            return angular.element(document)
                .injector()
                .get('leafletData')
                .getMap()
                .then(function (map) {
                    cb();
                });
        }).then(function () {
            return browser.actions()
                .mouseMove(self.leafletLayer, {x: 500, y: 400})
                .click()
                .perform();
        });
    };

    this.clickSubmitEntry = function () {
        this.addEntrySubmitButton.click();
    }
}

module.exports = DiaryEditSidebar;
