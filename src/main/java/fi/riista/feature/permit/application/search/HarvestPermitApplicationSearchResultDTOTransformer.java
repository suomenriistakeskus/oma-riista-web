package fi.riista.feature.permit.application.search;

import com.querydsl.core.group.GroupBy;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.gis.zone.GISZoneSizeDTO;
import fi.riista.feature.gis.zone.GISZoneWithoutGeometryDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.PersonWithNameDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.species.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionRepository;
import fi.riista.feature.permit.decision.QPermitDecision;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import fi.riista.util.ListTransformer;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Component
public class HarvestPermitApplicationSearchResultDTOTransformer
        extends ListTransformer<HarvestPermitApplication, HarvestPermitApplicationSearchResultDTO> {

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private PermitDecisionRepository permitDecisionRepository;

    @Nonnull
    @Override
    protected List<HarvestPermitApplicationSearchResultDTO> transform(
            @Nonnull final List<HarvestPermitApplication> list) {

        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        final boolean moderatorOrAdmin = activeUserService.isModeratorOrAdmin();
        final ArrayList<HarvestPermitArea> areaList = F.mapNonNullsToList(list, HarvestPermitApplication::getArea);
        final Function<HarvestPermitArea, GISZoneWithoutGeometryDTO> areaMapping = gisZoneRepository.getAreaMapping(areaList);
        final Function<HarvestPermitApplication, SystemUser> handlerMapping =
                moderatorOrAdmin ? createApplicationHandlerMapping(list) : null;

        final Function<HarvestPermitApplication, PermitDecision.Status> decisionStatuses =
                createApplicationDecisionStatusMapping(list);


        return list.stream().map(application -> {
            final GISZoneSizeDTO areaSize = Optional.ofNullable(application.getArea())
                    .map(areaMapping)
                    .map(GISZoneWithoutGeometryDTO::getSize)
                    .orElse(null);

            final Set<Integer> gameSpeciesCodes = application.getSpeciesAmounts().stream()
                    .map(HarvestPermitApplicationSpeciesAmount::getGameSpecies)
                    .map(GameSpecies::getOfficialCode)
                    .collect(Collectors.toSet());

            final PersonWithNameDTO contactPerson = Optional.ofNullable(application.getContactPerson())
                    .map(PersonWithNameDTO::create).orElse(null);

            final OrganisationNameDTO permitHolder = Optional.ofNullable(application.getPermitHolder())
                    .map(OrganisationNameDTO::createWithOfficialCode).orElse(null);

            final OrganisationNameDTO rhy = Optional.ofNullable(application.getRhy())
                    .map(OrganisationNameDTO::createWithOfficialCode).orElse(null);

            final SystemUser decisionHandler = handlerMapping != null ? handlerMapping.apply(application) : null;

            final HarvestPermitApplicationSearchResultDTO dto = new HarvestPermitApplicationSearchResultDTO();
            DtoUtil.copyBaseFields(application, dto);

            dto.setStatus(application.getStatus());
            dto.setDecisionStatus(decisionStatuses.apply(application));
            dto.setHuntingYear(application.getHuntingYear());
            dto.setSubmitDate(application.getSubmitDate() != null ? application.getSubmitDate().toLocalDateTime() : null);
            dto.setContactPerson(contactPerson);
            dto.setPermitHolder(permitHolder);
            dto.setGameSpeciesCodes(gameSpeciesCodes);
            dto.setHasPermitArea(application.getArea() != null);
            dto.setAreaSize(areaSize);
            dto.setPermitTypeCode(application.getPermitTypeCode());
            dto.setApplicationNumber(application.getApplicationNumber());
            dto.setRhy(rhy);

            // Draft decision should be fetch joined

            if (decisionHandler != null) {
                final PersonWithNameDTO handlerDTO = new PersonWithNameDTO();
                handlerDTO.setByName(decisionHandler.getFirstName());
                handlerDTO.setLastName(decisionHandler.getLastName());
                dto.setHandler(handlerDTO);
            }

            return dto;

        }).collect(toList());
    }

    private Function<HarvestPermitApplication, SystemUser> createApplicationHandlerMapping(
            final List<HarvestPermitApplication> applications) {
        final QPermitDecision DECISION = QPermitDecision.permitDecision;

        final Map<Long, SystemUser> mapping = jpqlQueryFactory
                .select(DECISION.application.id, DECISION.handler)
                .from(DECISION)
                .where(DECISION.application.in(applications), DECISION.handler.isNotNull())
                .transform(GroupBy.groupBy(DECISION.application.id).as(DECISION.handler));

        return a -> mapping.get(a.getId());
    }

    private Function<HarvestPermitApplication, PermitDecision.Status> createApplicationDecisionStatusMapping(
            final List<HarvestPermitApplication> applications) {
        final QPermitDecision DECISION = QPermitDecision.permitDecision;

        final Map<Long, PermitDecision.Status> mapping = jpqlQueryFactory
                .select(DECISION.application.id, DECISION.status)
                .from(DECISION)
                .where(DECISION.application.in(applications))
                .transform(GroupBy.groupBy(DECISION.application.id).as(DECISION.status));

        return a -> mapping.get(a.getId());
    }
}
