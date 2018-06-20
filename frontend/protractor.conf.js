var HtmlScreenshotReporter = require('protractor-jasmine2-screenshot-reporter'),
    JasmineReporters = require('jasmine-reporters'),
    SpecReporter = require('jasmine-spec-reporter').SpecReporter;

var specReporter = new SpecReporter({displayStacktrace: true});

var junitReporter = new JasmineReporters.JUnitXmlReporter({
    savePath: '../target/protractor',
    consolidateAll: true,
    filePrefix: 'TEST-e2e'
});

var screenshotReporter = new HtmlScreenshotReporter({
    dest: '../target/screenshots',
    filename: 'protractor.html'
});

exports.config = {
    framework: 'jasmine2',
    specs: ['./**/*.spec.js'],
    
    /**
     *  Direct connect is only supported by Chrome and Firefox browsers.
     *  To test against any other browser, you need to set this to false,
     *  and set up a Selenium server. For more details, see the page
     *  https://github.com/angular/protractor/blob/master/docs/server-setup.md
     */
    directConnect: true,
    capabilities: {
        'browserName': 'chrome'
    },

    // Setup the report before any tests start
    beforeLaunch: function() {
        return new Promise(function(resolve){
            screenshotReporter.beforeLaunch(resolve);
        });
    },

    // Assign the test reporter to each running instance
    onPrepare: function () {
        jasmine.getEnv().addReporter(specReporter);
        jasmine.getEnv().addReporter(junitReporter);
        jasmine.getEnv().addReporter(screenshotReporter);
    },

    // Close the report after all tests finish
    afterLaunch: function(exitCode) {
        return new Promise(function(resolve){
            screenshotReporter.afterLaunch(resolve.bind(this, exitCode));
        });
    },

    jasmineNodeOpts: {
        showColors: true,
        defaultTimeoutInterval: 10000,
        isVerbose: false,
        includeStackTrace: false
    }
};
