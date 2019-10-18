package fi.riista.feature.huntingclub.register;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gis.hta.GISHirvitalousalueRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.lupahallinta.LHOrganisation;
import fi.riista.feature.organization.lupahallinta.LHOrganisationRepository;
import fi.riista.feature.organization.rhy.MergedRhyMapping;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.util.F;
import io.vavr.Tuple;
import io.vavr.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class RegisterHuntingClubService {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterHuntingClubService.class);

    @Resource
    private LHOrganisationRepository lhOrganisationRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Resource
    private HuntingClubRepository huntingClubRepository;

    @Resource
    private RiistanhoitoyhdistysRepository rhyRepository;

    @Resource
    private GISHirvitalousalueRepository hirvitalousalueRepository;

    @Nullable
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public HuntingClub findExistingOrCreate(@Nonnull String officialCode) {
        final HuntingClub club = huntingClubRepository.findByOfficialCode(officialCode);
        if (club != null) {
            return club;
        }
        final List<LHOrganisation> lhOrgs = lhOrganisationRepository.findByOfficialCode(Objects.requireNonNull(officialCode));
        if (lhOrgs.isEmpty()) {
            // for some reason club is not in lh_orgs
            return null;
        }
        // There are duplicate lh_org rows with same officialCode but
        // different contact person. It shouldn't matter which one to use.
        final LHOrganisation lhOrganisation = lhOrgs.get(0);
        return findExistingOrCreate(officialCode, lhOrganisation)._1;
    }

    @Nonnull
    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Tuple3<HuntingClub, Organisation, Boolean> findExistingOrCreate(@Nonnull String officialCode,
                                                                           @Nonnull LHOrganisation lhOrganisation) {
        Objects.requireNonNull(officialCode);
        Objects.requireNonNull(lhOrganisation);

        final HuntingClub existingClub = huntingClubRepository.findByOfficialCode(officialCode);
        final boolean clubExists = existingClub != null;
        final Organisation existingRhy = clubExists ? existingClub.getParentOrganisation() : null;

        final Organisation rhy = findRhy(lhOrganisation, existingRhy);

        // If club exists do not update it from lh_org. Club is kept in sync with lh_orgs elsewhere appropriately.
        final HuntingClub club = clubExists ? existingClub : createClub(lhOrganisation, rhy);
        return Tuple.of(club, rhy, clubExists);
    }

    @Nonnull
    private HuntingClub createClub(final @Nonnull LHOrganisation lhOrganisation, final @Nonnull Organisation rhy) {
        final HuntingClub club = new HuntingClub();
        club.setOfficialCode(Objects.requireNonNull(lhOrganisation.getOfficialCode()));
        updateClubProperties(lhOrganisation, club, rhy);
        return organisationRepository.save(club);
    }

    @Nonnull
    private HuntingClub updateClubProperties(@Nonnull LHOrganisation lhOrganisation,
                                                    @Nonnull HuntingClub club,
                                                    @Nonnull Organisation rhy) {

        club.setParentOrganisation(Objects.requireNonNull(rhy));
        club.setNameFinnish(Objects.requireNonNull(lhOrganisation.getNameFinnish()));
        club.setNameSwedish(F.firstNonNull(lhOrganisation.getNameSwedish(), lhOrganisation.getNameFinnish()));

        if (lhOrganisation.getLatitude() != null && lhOrganisation.getLongitude() != null) {
            club.setGeoLocation(new GeoLocation(lhOrganisation.getLatitude(), lhOrganisation.getLongitude()));
        }

        club.setMooseArea(hirvitalousalueRepository.findByNumber(lhOrganisation.getMooseAreaCode()));
        club.setHuntingAreaSize(Optional.ofNullable(lhOrganisation.getAreaSize()).map(Number::doubleValue).orElse(null));
        return club;
    }

    @Nonnull
    private Organisation findRhy(final @Nonnull LHOrganisation lhOrganisation, final Organisation existingRhy) {
        return F.optionalFromSuppliers(
                () -> findRhy(lhOrganisation.getRhyOfficialCode()),
                // Fallback: use contact person RHY
                () -> findRhy(lhOrganisation.getContactPersonRhy()),
                // Fallback: use existing known RHY
                () -> {
                    if (existingRhy != null) {
                        LOG.warn("Could not find RHY {} or {}. Using existing RHY {} for HuntingClub {}",
                                lhOrganisation.getRhyOfficialCode(),
                                lhOrganisation.getContactPersonRhy(),
                                existingRhy.getOfficialCode(),
                                lhOrganisation.getOfficialCode());
                    }
                    return Optional.ofNullable(existingRhy);
                }).orElseThrow(() -> RegisterHuntingClubException.missingRhy(lhOrganisation.getOfficialCode()));
    }

    private Optional<Organisation> findRhy(String officialCode) {
        return Optional.ofNullable(officialCode)
                .map(MergedRhyMapping::translateIfMerged)
                .map(rhyRepository::findByOfficialCode);
    }

}
