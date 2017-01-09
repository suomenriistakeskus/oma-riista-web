var PageHelper = require('./PageHelper');

var DiaryPage = function () {
    'use strict';

    var pageHelper = new PageHelper();

    this.addHarvestButton = element(by.css('.r-gamediary-map-button-area a.btn:nth-child(1)'));

    this.waitForLoad = function () {
        return pageHelper.waitForUrl(/profile\/me\/diary/);
    };

    this.waitForAddHarvestLoad = function () {
        return pageHelper.waitForUrl(/profile\/me\/diary\/add_harvest/);
    };

    this.waitForReturnFromAdd = function () {
        return pageHelper.waitForElement($('.r-gamediary-layout'));
    };

    this.clickAddHarvest = function () {
        this.addHarvestButton.click();
    };
};

module.exports = DiaryPage;
