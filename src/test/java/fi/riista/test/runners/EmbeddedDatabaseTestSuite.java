package fi.riista.test.runners;

import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.runner.RunWith;

@RunWith(BaseClassFilteringTestSuite.class)
@BaseTypeFilter(includes = EmbeddedDatabaseTest.class)
public class EmbeddedDatabaseTestSuite {

}
