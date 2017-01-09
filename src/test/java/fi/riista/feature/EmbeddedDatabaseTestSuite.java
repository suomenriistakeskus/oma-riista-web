package fi.riista.feature;

import org.junit.runner.RunWith;

@RunWith(BaseClassFilteringTestSuite.class)
@BaseTypeFilter(includes = EmbeddedDatabaseTest.class)
public class EmbeddedDatabaseTestSuite {

}
