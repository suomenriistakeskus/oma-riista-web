var LoginPage = function () {
    'use strict';

    this.usernameField = element(by.model('credentials.username'));
    this.passwordField = element(by.model('credentials.password'));
    this.loginButton = element(by.css('button[type=submit]'));

    this.setUsername = function (username) {
        this.usernameField.clear();
        this.usernameField.sendKeys(username);
    };

    this.setPassword = function (password) {
        this.passwordField.clear();
        this.passwordField.sendKeys(password);
    };

    this.clickSubmit = function () {
        this.loginButton.click();
    };
};

module.exports = LoginPage;
