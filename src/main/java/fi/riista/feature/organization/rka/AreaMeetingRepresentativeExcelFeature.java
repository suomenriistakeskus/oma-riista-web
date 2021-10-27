package fi.riista.feature.organization.rka;

import com.google.common.base.Preconditions;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.Organisation_;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.util.F;
import org.springframework.context.MessageSource;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static fi.riista.feature.organization.OrganisationType.RHY;
import static fi.riista.feature.organization.OrganisationType.RKA;
import static fi.riista.feature.organization.occupation.OccupationType.ALUEKOKOUKSEN_EDUSTAJA;

@Service
public class AreaMeetingRepresentativeExcelFeature {

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private AreaMeetingRepresentativeDTOTransformer transformer;

    @Resource
    private MessageSource messageSource;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public AreaMeetingRepresentativeExcelView export(final long areaId, final Locale locale) {
        final Organisation rka = organisationRepository.getOne(areaId);
        Preconditions.checkArgument(rka.getOrganisationType() == RKA);

        final List<Organisation> rhys = organisationRepository.findActiveByParentOrganisationAndOrganisationType(
                rka, RHY, JpaSort.of(Organisation_.officialCode, Organisation_.id));

        final List<Occupation> occupations =
                occupationRepository.findActiveByOrganisationsAndTypes(F.getUniqueIds(rhys), Collections.singleton(ALUEKOKOUKSEN_EDUSTAJA));

        return new AreaMeetingRepresentativeExcelView(
                new EnumLocaliser(messageSource, locale),
                OrganisationNameDTO.create(rka),
                transformer.transform(occupations));
    }
}
