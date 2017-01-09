## Testing

### Unit tests
Executing the complete unit test suite can be done using the command

    mvn -P ci,all-tests clean verify

which executes both unit and integration tests. To execute indivual unit tests or test suites, you can define the
variable `test` to equal the name of the test or test suite, e.g.

    mvn -D test=UnitTestSuite test

The frontend unit tests can be executed by running the command

    npm test

or by navigating to `{{projectDirectory}}/frontend/` and running the individual gulp tasks.


### End-to-end tests
End to end tests can be executed either locally or using an external Selenium hub. Both modes use the e2e-test profile
which contains separate database configuration for the e2e-testing database. Before executing the tests, you should
verify that your local user has sufficient access privileges to Postgres and the ability to create databases. This is
because the build sets up local PostgreSQL databases under the name `riistakeskus_testdb` and
`riistakeskus_testdb_template`. The build also sets up the necessary schemas and populates the database with sample
data, which it downloads from the Internet. Execute the command

    mvn -P e2e-test clean verify

to execute the e2e-test suite locally as a complete maven handled process. Alternatively execute the commands

    mvn -P e2e-test jetty:run
    npm run e2e

to first start the web server and then run tests on demand using npm. The latter method is also the recommended one
for testing during development, since the web server is not required to be restarted between the runs.

If at any point there is a reason to reset and repopulate the test database template, that can be done
by defining `resettemplate`, e.g.

    mvn -P e2e-test -D resettemplate jetty:run

To connect to an external Selenium server, edit the configuration in
`{{projectDirectory}}/frontend/build.config.js` and set the `e2e:hub:ci`  to point to the desired Selenium hub, and the
`e2e:baseUrl:ci` to point to the base url of the web server where the application will be deployed, e.g. your personal
IP address that is reachable by the Selenium. Execute the command

	mvn -P ci,e2e-test clean verify

to start the tests. Same process also applies to the included self-configuring Vagrant server that is capable of
connecting to a similarly specified Selenium hub, and can be used for CI deployments.
