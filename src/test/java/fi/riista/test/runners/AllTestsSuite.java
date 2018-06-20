package fi.riista.test.runners;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ UnitTestSuite.class, EmbeddedDatabaseTestSuite.class })
public class AllTestsSuite {

}
