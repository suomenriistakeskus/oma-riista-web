package fi.riista.feature.organization.occupation.search;

import fi.riista.feature.gis.hta.GISHirvitalousalueDTO;
import fi.riista.feature.gis.hta.GISHirvitalousalueRepository;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.Organisation_;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.pub.occupation.PublicOrganisationDTO;
import fi.riista.feature.pub.PublicDTOFactory;
import fi.riista.util.F;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Component
public class ContactSearchAreaListFeature {

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private PublicDTOFactory dtoFactory;

    @Resource
    private GISHirvitalousalueRepository hirvitalousalueRepository;

    @Transactional(readOnly = true)
    public List<PublicOrganisationDTO> listAreas() {
        // Sort areas by officialCode which is natural and consistent ordering
        // criteria for this type of organisations.
        List<Organisation> areas = organisationRepository.findByOrganisationType(
                OrganisationType.RKA,
                new JpaSort(Organisation_.officialCode, Organisation_.id));

        return F.mapNonNullsToList(areas, area -> {
            return dtoFactory.create(area);
        });
    }

    @Transactional(readOnly = true)
    public List<GISHirvitalousalueDTO> listHtas() {
        return GISHirvitalousalueDTO.create(hirvitalousalueRepository.findAll());
    }
}
