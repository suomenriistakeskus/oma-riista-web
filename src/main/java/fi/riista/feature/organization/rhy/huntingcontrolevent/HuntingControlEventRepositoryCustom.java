package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface HuntingControlEventRepositoryCustom {
    Map<Organisation, Map<Riistanhoitoyhdistys, List<HuntingControlEvent>>> findByYear(int year);
    List<Integer> listEventYears(Riistanhoitoyhdistys rhy);
    List<Integer> listEventYears(Riistanhoitoyhdistys rhy, Person person);
    Map<Long, Set<Long>> mapInspectorPersonIdsByEventId(Collection<HuntingControlEvent> events);
    Map<Long, Set<HuntingControlCooperationType>> mapCooperationTypesByEventId(Collection<HuntingControlEvent> events);
    List<HuntingControlEvent> findByRhyAndInspectorAndModifiedAfterOrder(final Organisation org,
                                                                         final Person inspector,
                                                                         final DateTime modificationTime);
    List<HuntingControlEvent> findReportEvents(Riistanhoitoyhdistys rhy, HuntingControlEventReportQueryDTO filters);
    List<HuntingControlEvent> findReportEvents(HuntingControlEventSearchParametersDTO filters);

    }
