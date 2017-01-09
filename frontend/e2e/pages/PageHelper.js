module.exports = function () {
    'use strict';

    var flow = browser.controlFlow();
    var page = this;
    page.baseUrl = browser.baseUrl;

    /* * * * * * * * * * * * *
     * PROMISE / FLOW METHODS
     * * * * * * * * * * * * */

    /**
     * Execute a sequence of promises sequentially
     * @param {*[]} actionPromises
     * @returns {Promise}
     */
    page.executeSequence = function(actionPromises) {
        return protractor.promise.all(
            actionPromises.map(function(promise) {
                return flow.execute(function() { return promise; });
            })
        );
    };

    /* * * * * * * * * * * * *
     * PAGE RELATED METHODS
     * * * * * * * * * * * * */

    /**
     * Opens the page.baseUrl
     * @returns {Promise}
     */
    page.get = function() {
        return page.getUrl('');
    };

    /**
     * Opens the page.baseUrl appending the suffix parameter
     * @param {string} suffix
     * @returns {Promise}
     */
    page.getUrl = function(suffix) {
        browser.ignoreSynchronisation = false;
        return page.executeSequence([
            browser.get(page.baseUrl + suffix),
            page.pageIsOpen()
        ]);
    };

    /**
     * Scroll to an element
     * @param {WebElement} element
     * @returns {Promise}
     */
    page.scrollTo = function(element) {
        return browser.executeScript('arguments[0].scrollIntoView()', element.getWebElement());
    };

    /**
     * Waits for Angular to bootstrap before resolving
     * @returns {ElementFinder}
     */
    page.pageIsOpen = function () {
        browser.waitForAngular();
        return element(by.css('.r-main-nav-wrapper ng-scope')).isPresent();
    };
    
    page.clearCookies = function () {
        browser.driver.manage().deleteAllCookies();
    };

    /* * * * * * * * * * * * * * *
     * ELEMENT INTERACTION METHODS
     * * * * * * * * * * * * * * */

    /**
     * Click an element (uses browser.actions to avoid an IE bug)
     * @param {WebElement} element
     * @returns {Promise}
     */
    page.click = function(element) {
        return page.executeSequence([
            // clear focus first to avoid a rare condition where the click only clears
            // focus from another element instead of actually clicking the thing you want
            page.clearFocus(),
            browser.actions().click(element).perform()
        ]);
    };

    /**
     * Give focus to an element (just a descriptive alias for a click)
     * @param {WebElement} element
     * @returns {Promise}
     */
    page.focus = function(element) {
        return page.click(element);
    };

    /**
     * Clears focus from any element by clicking on the body
     * @returns {Promise}
     */
    page.clearFocus = function () {
        return browser.actions().mouseMove({x: 9999, y: 9999}).click().perform();
    };

    /**
     * Hover on an element
     * @param {WebElement} element
     * @returns {Promise}
     */
    page.hover = function(element) {
        return browser.actions().mouseMove(element).perform();
    };

    /**
     * Send a sequence of keystrokes to the element (e.g. for entering a value into an input)
     * @param {WebElement} element
     * @param {string} value
     * @returns {Promise}
     */
    page.fill = function (element, value) {
        return element.sendKeys(value);
    };

    /**
     * Clears any content from an input before entering a new value
     * @param {WebElement} element
     * @param {string} value
     * @returns {Promise}
     */
    page.clearAndFill = function(element, value) {
        return page.executeSequence([
            element.clear(),
            page.fill(element, value)
        ]);
    };

    /**
     * Gets the content of an element (value if an input)
     * @param {WebElement} element
     * @returns {Promise}
     */
    page.getContent = function(element) {
        return element.getAttribute('value')
            .then(function (val) {
                return (val === null) ?
                    element.getText() :
                    val;
            });
    };

    /**
     * Check to see if an element has a class assigned
     * @param {WebElement} element
     * @param {string} className
     * @returns {Promise}
     */
    page.hasClass = function(element, className) {
        return element.getAttribute('class').then(function (classes) {
            return !!classes && classes.split(' ').indexOf(className) !== -1;
        });
    };

    /**
     * Selects an item in dropdown by index
     * @param {WebElement} dropdown
     * @param {number} index
     */
    page.selectDropdownItemByIndex = function(dropdown, index) {
        return page.executeSequence([
            page.click(dropdown),
            dropdown.all(by.tagName('option')).get(index).click(),
            page.clearFocus()
        ]);
    };

    /**
     * Selects an item in dropdown by label
     * @param {WebElement} dropdown
     * @param {string} label
     */
    page.selectDropdownItemByLabel = function(dropdown, label) {
        return page.executeSequence([
            page.click(dropdown),
            dropdown.all(by.css('option[label="' + label + '"]')).first().click(),
            page.clearFocus()
        ]);
    };

    /**
     * Selects an item in dropdown by value
     * @param {WebElement} dropdown
     * @param {string} value
     */
    page.selectDropdownItemByValue = function(dropdown, value) {
        return page.executeSequence([
            // page.click(dropdown),
            dropdown.all(by.css('option[value="' + value + '"]')).first().click(),
            page.clearFocus()
        ]);
    };

    /* * * * * * * * * * * * * * *
     * WAITING FOR THINGS
     * * * * * * * * * * * * * * */
    
    page.waitForUrl = function(testPattern) {
        return browser.wait(function () {
            return browser.getCurrentUrl().then(function (url) {
                return testPattern.test(url);
            })
        })  
    };

    /**
     * Waits until a particular element is present on the page
     * @returns {Promise}
     */
    page.waitForElement = function(element) {
        var ec = protractor.ExpectedConditions;
        return browser.driver.wait(ec.presenceOf(element), 1000);
    };

    /**
     * Waits until there is no modal present
     * @returns {Promise}
     */
    page.waitUntilModalIsGone = function() {
        var modal = element(by.css('.my-modal-class'));
        var ec = protractor.ExpectedConditions;
        return browser.driver.wait(ec.not(page.ec.visibilityOf(modal)), 1000);
    };
};
