package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;

import java.util.List;
import java.util.Map;

public interface HuntingControlEventRepositoryCustom {
    Map<Organisation, Map<Riistanhoitoyhdistys, List<HuntingControlEvent>>> findByYear(final int year);
}
