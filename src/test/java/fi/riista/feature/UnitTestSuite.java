package fi.riista.feature;

import org.junit.runner.RunWith;

@RunWith(BaseClassFilteringTestSuite.class)
@BaseTypeFilter(excludes = { EmbeddedDatabaseTest.class })
public class UnitTestSuite {

}
