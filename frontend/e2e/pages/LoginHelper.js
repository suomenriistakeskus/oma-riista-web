var LoginPage = require('./LoginPage')
    , PageHelper = require('./PageHelper');

var LoginHelper = function () {
    var self = this;

    this.pageHelper = new PageHelper();
    this.loginPage = new LoginPage();

    this.loginAsUser = function () {
        self.pageHelper.clearCookies();
        self.pageHelper.get('#/login');

        self.loginPage.setUsername('user');
        self.loginPage.setPassword('user');
        self.loginPage.clickSubmit();

        return self.pageHelper.waitForElement($('.r-roleselection'));
    };

    this.logout = function () {
        self.pageHelper.clearCookies();

        return self.pageHelper.executeSequence([
            self.pageHelper.get('#/logout'),
            self.pageHelper.waitForElement($('.r-login-container'))
        ]);
    };
};

module.exports = LoginHelper;
