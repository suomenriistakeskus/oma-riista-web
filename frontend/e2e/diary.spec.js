'use strict';

var PageHelper = require('./pages/PageHelper')
    , LoginHelper = require('./pages/LoginHelper')
    , RoleSelectionPage = require('./pages/RoleSelectionPage')
    , DiaryPage = require('./pages/DiaryPage')
    , DiaryEditSidebar = require('./pages/DiaryEditSidebar')
    , DiaryShowSidebar = require('./pages/DiaryShowSidebar');

describe("GameDiary", function () {
    var pageHelper = new PageHelper()
        , loginHelper = new LoginHelper()
        , roleSelectionPage = new RoleSelectionPage()
        , diaryPage = new DiaryPage()
        , editSidebar = new DiaryEditSidebar()
        , showSidebar = new DiaryShowSidebar();

    beforeEach(function () {
        loginHelper.loginAsUser();
        roleSelectionPage.selectNormalUser();

        return diaryPage.waitForLoad();
    });

    it('should allow adding harvest', function () {
        return pageHelper.executeSequence([
            diaryPage.clickAddHarvest(),
            diaryPage.waitForAddHarvestLoad(),

            editSidebar.selectGameSpecies('47503'),
            editSidebar.selectDate('21.5.2016'),
            editSidebar.selectTime('12:02'),
            editSidebar.selectGender('MALE'),
            editSidebar.selectAge('ADULT'),
            editSidebar.selectMapPosition(),
            editSidebar.clickSubmitEntry(),

            diaryPage.waitForReturnFromAdd()
        ]).then(function () {
            expect(showSidebar.getSpeciesName()).toBe('hirvi');
            expect(showSidebar.getText()).toContain('21.5.2016 12:02');
            expect(showSidebar.getText()).toContain('Pentti Mujunen');
            expect(showSidebar.getText()).toContain('Uros');
            expect(showSidebar.getText()).toContain('Aikuinen');
        });
    });
});
