package fi.riista.feature.gis.verotuslohko;

import java.util.Collection;
import java.util.List;

public interface GISVerotusLohkoRepositoryCustom {
    List<GISVerotusLohkoDTO> findWithoutGeometry(int huntingYear, Collection<String> officialCodes);
}
