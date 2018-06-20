package fi.riista.test.runners;

import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.runner.RunWith;

@RunWith(BaseClassFilteringTestSuite.class)
@BaseTypeFilter(excludes = { EmbeddedDatabaseTest.class })
public class UnitTestSuite {

}
