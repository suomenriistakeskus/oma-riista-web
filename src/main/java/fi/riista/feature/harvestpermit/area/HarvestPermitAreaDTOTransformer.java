package fi.riista.feature.harvestpermit.area;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gis.zone.AbstractAreaDTOTransformer;
import fi.riista.feature.gis.zone.GISZoneWithoutGeometryDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.util.DateUtil;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import fi.riista.util.jpa.CriteriaUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Component
public class HarvestPermitAreaDTOTransformer
        extends AbstractAreaDTOTransformer<HarvestPermitArea, HarvestPermitAreaDTO> {

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private UserRepository userRepository;

    @Resource
    private PersonRepository personRepository;

    @Nonnull
    @Override
    protected List<HarvestPermitAreaDTO> transform(@Nonnull final List<HarvestPermitArea> list) {
        if (list.isEmpty()) {
            return emptyList();
        }

        final Function<HarvestPermitArea, GISZoneWithoutGeometryDTO> areaToZoneMapping = createZoneDTOFunction(list);
        final Function<HarvestPermitArea, Long> areaToPartnerCount = cretePartnerCountMapping(list);
        final Map<Long, SystemUser> modifierUsers = findModifierUsers(list);
        final Function<SystemUser, Person> userToPerson = findUserToPerson(modifierUsers.values());

        return list.stream().map(area -> {
            final GISZoneWithoutGeometryDTO zone = areaToZoneMapping.apply(area);

            final HarvestPermitAreaDTO dto = new HarvestPermitAreaDTO();
            DtoUtil.copyBaseFields(area, dto);

            dto.setClubId(F.getId(area.getClub()));
            dto.setNameFI(area.getNameFinnish());
            dto.setNameSV(area.getNameSwedish());
            dto.setExternalId(area.getExternalId());
            dto.setHuntingYear(area.getHuntingYear());
            dto.setStatus(area.getStatus());
            dto.setPartnerCount(areaToPartnerCount.apply(area));
            dto.setLastModifiedDate(DateUtil.toLocalDateTimeNullSafe(area.getLifecycleFields().getModificationTime()));

            final SystemUser user = modifierUsers.get(area.getModifiedByUserId());

            if (user != null) {
                final Person person = userToPerson.apply(user);

                if (person != null) {
                    dto.setLastModifierName(person.getFullName());
                    dto.setLastModifierRiistakeskus(false);
                } else {
                    dto.setLastModifierName(user.getFullName());
                    dto.setLastModifierRiistakeskus(true);
                }
            } else {
                dto.setLastModifierRiistakeskus(true);
            }

            if (zone != null) {
                dto.setComputedAreaSize(zone.getComputedAreaSize());
                dto.setWaterAreaSize(zone.getWaterAreaSize());
            } else {
                dto.setComputedAreaSize(0);
                dto.setWaterAreaSize(0);
            }
            dto.setRhy(area.getRhy().stream().map(HarvestPermitAreaRhyDTO::create).collect(toList()));
            dto.setHta(area.getHta().stream().map(HarvestPermitAreaHtaDTO::create).collect(toList()));

            return dto;
        }).collect(toList());
    }

    private Function<HarvestPermitArea, Long> cretePartnerCountMapping(final Iterable<HarvestPermitArea> areas) {
        return CriteriaUtils.createAssociationCountFunction(areas, HarvestPermitAreaPartner.class,
                HarvestPermitAreaPartner_.harvestPermitArea, entityManager);
    }

    private Map<Long, SystemUser> findModifierUsers(final List<HarvestPermitArea> list) {
        return F.indexById(userRepository.findAll(F.mapNonNullsToSet(list, LifecycleEntity::getModifiedByUserId)));
    }

    private Function<SystemUser, Person> findUserToPerson(final Collection<SystemUser> users) {
        return CriteriaUtils.singleQueryFunction(users, SystemUser::getPerson, personRepository, false);
    }
}
