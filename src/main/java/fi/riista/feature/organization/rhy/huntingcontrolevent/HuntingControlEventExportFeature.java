package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.joda.time.LocalDate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static fi.riista.feature.organization.rhy.RiistanhoitoyhdistysAuthorization.RhyPermission.LIST_HUNTING_CONTROL_EVENTS;
import static fi.riista.security.EntityPermission.UPDATE;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Service
public class HuntingControlEventExportFeature {

    @Resource
    private HuntingControlEventRepository huntingControlEventRepository;

    @Resource
    private HuntingControlEventDTOTransformer dtoTransformer;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional(readOnly = true)
    public HuntingControlEventExportDTO export(final long rhyId, final int year) {
        final Riistanhoitoyhdistys rhy = requireEntityService.requireRiistanhoitoyhdistys(rhyId, UPDATE);
        final Organisation rka = rhy.getParentOrganisation();

        final LocalDate startDate = new LocalDate(year, 1, 1);
        final LocalDate endDate = new LocalDate(year, 12, 31);
        final List<HuntingControlEvent> events = huntingControlEventRepository.findByRhyAndDateBetweenOrderByDateAsc(rhy, startDate, endDate);

        return new HuntingControlEventExportDTO(OrganisationNameDTO.createWithOfficialCode(rka),
                OrganisationNameDTO.createWithOfficialCode(rhy),
                dtoTransformer.apply(events));
    }

    @Transactional(readOnly = true)
    public HuntingControlEventExportDTO exportForActiveUser(final long rhyId, final int year) {
        final Riistanhoitoyhdistys rhy = requireEntityService.requireRiistanhoitoyhdistys(rhyId, LIST_HUNTING_CONTROL_EVENTS);
        final Organisation rka = rhy.getParentOrganisation();
        final Person inspector = activeUserService.requireActivePerson();

        final List<HuntingControlEvent> events = huntingControlEventRepository.findByRhyAndYearAndInspectorOrderByDateDesc(rhy, year, inspector);

        return new HuntingControlEventExportDTO(OrganisationNameDTO.createWithOfficialCode(rka),
                OrganisationNameDTO.createWithOfficialCode(rhy),
                dtoTransformer.apply(events));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public List<HuntingControlEventExportDTO> exportAll(final int year) {
        final Map<Organisation, Map<Riistanhoitoyhdistys, List<HuntingControlEvent>>> rkaRhyEventMap =
                huntingControlEventRepository.findByYear(year);

        return doExport(rkaRhyEventMap);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasPrivilege('VIEW_HUNTING_CONTROL_EVENTS')")
    @Transactional(readOnly = true)
    public List<HuntingControlEventExportDTO> exportAll(final HuntingControlEventSearchParametersDTO filters) {
        final Function<HuntingControlEvent, Organisation> getRka = e -> e.getRhy().getParentOrganisation();
        final Function<HuntingControlEvent, Riistanhoitoyhdistys> getRhy = HuntingControlEvent::getRhy;
        final Map<Organisation, Map<Riistanhoitoyhdistys, List<HuntingControlEvent>>> rkaRhyEventMap =
                huntingControlEventRepository.findReportEvents(filters)
                        .stream()
                        .collect(Collectors.groupingBy(getRka, Collectors.groupingBy(getRhy)));
        return doExport(rkaRhyEventMap);
    }

    private List<HuntingControlEventExportDTO> doExport(final Map<Organisation, Map<Riistanhoitoyhdistys, List<HuntingControlEvent>>> rkaRhyEventMap) {
        final List<Organisation> rkas = rkaRhyEventMap.keySet().stream()
                .sorted(comparing(Organisation::getOfficialCode))
                .collect(toList());

        return rkas.stream()
                .map(rka -> collectExportDtos(rkaRhyEventMap.get(rka), rka))
                .flatMap(Collection::stream)
                .collect(toList());
    }

    private List<HuntingControlEventExportDTO> collectExportDtos(final Map<Riistanhoitoyhdistys, List<HuntingControlEvent>> rhyEventMap,
                                                                 final Organisation rka) {
        final List<Riistanhoitoyhdistys> rhys = rhyEventMap.keySet().stream()
                .sorted(comparing(Riistanhoitoyhdistys::getOfficialCode))
                .collect(toList());

        return rhys.stream()
                .map(rhy -> {
                    final OrganisationNameDTO rkaName = OrganisationNameDTO.createWithOfficialCode(rka);
                    final OrganisationNameDTO rhyName = OrganisationNameDTO.createWithOfficialCode(rhy);
                    final List<HuntingControlEvent> events = rhyEventMap.get(rhy);
                    return new HuntingControlEventExportDTO(rkaName, rhyName, dtoTransformer.apply(events));
                })
                .collect(toList());
    }
}
