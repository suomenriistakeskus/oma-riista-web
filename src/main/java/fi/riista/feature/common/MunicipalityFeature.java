package fi.riista.feature.common;

import fi.riista.api.municipality.MunicipalityDTO;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.common.repository.MunicipalityRepository;
import fi.riista.feature.pub.municipality.PublicMunicipalityDTO;
import fi.riista.util.F;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;

@Component
public class MunicipalityFeature {

    @Resource
    private MunicipalityRepository municipalityRepository;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    @Transactional(readOnly = true)
    public List<MunicipalityDTO> listMunicipalities() {
        final List<Municipality> municipalities = municipalityRepository.findActiveMunicipalities();
        return F.mapNonNullsToList(municipalities, MunicipalityDTO::from);
    }

    @Transactional(readOnly = true)
    public List<PublicMunicipalityDTO> listMunicipalities(final Locale locale) {
        final List<Municipality> municipalities = municipalityRepository.findActiveMunicipalities();
        return F.mapNonNullsToList(municipalities, m -> PublicMunicipalityDTO.from(m, locale));
    }

    @Transactional(readOnly = true)
    public PublicMunicipalityDTO findMunicipality(final String municipalityCode, final Locale locale) {
        final Municipality municipality = municipalityRepository.getOne(municipalityCode);
        return PublicMunicipalityDTO.from(municipality, locale);
    }
}
