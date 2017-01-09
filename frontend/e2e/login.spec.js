'use strict';

var PageHelper = require('./pages/PageHelper')
    , LoginHelper = require('./pages/LoginHelper')
    , RoleSelectionPage = require('./pages/RoleSelectionPage');

describe("login", function () {
    var pageHelper = new PageHelper();
    var loginHelper = new LoginHelper();
    var roleSelectionPage = new RoleSelectionPage();

    it('should login and logout', function () {
        pageHelper.executeSequence([
            loginHelper.loginAsUser(),
            loginHelper.logout()
        ]);
    });

    it('should allow select normal user role', function () {
        loginHelper.loginAsUser();
        roleSelectionPage.selectNormalUser();

        return pageHelper.waitForUrl(/profile\/me\/diary/);
    });
});
