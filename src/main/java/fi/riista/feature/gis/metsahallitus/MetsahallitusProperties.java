package fi.riista.feature.gis.metsahallitus;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MetsahallitusProperties {
    @Value("${map.latest.metsahallitus.year}")
    private int latestMetsahallitusYear;

    public int getLatestMetsahallitusYear() {
        return latestMetsahallitusYear;
    }
}
