var RoleSelectionPage = function () {
    'use strict';

    this.roles = element.all(by.repeater('role in roles'));

    this.selectNormalUser = function () {
        return element(by.cssContainingText('.r-roleselection', 'Yksityishenkilö')).click();
    };
};

module.exports = RoleSelectionPage;
