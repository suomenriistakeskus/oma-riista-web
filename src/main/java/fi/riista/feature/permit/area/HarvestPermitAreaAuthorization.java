package fi.riista.feature.permit.area;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.QHuntingClub;
import fi.riista.feature.huntingclub.area.QHuntingClubArea;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.area.partner.QHarvestPermitAreaPartner;
import fi.riista.security.EntityPermission;
import fi.riista.security.UserInfo;
import fi.riista.security.authorization.AbstractEntityAuthorization;
import fi.riista.security.authorization.AuthorizationTokenCollector;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import static fi.riista.feature.account.user.SystemUser.Role.ROLE_ADMIN;
import static fi.riista.feature.account.user.SystemUser.Role.ROLE_MODERATOR;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_JASEN;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;

@Component
public class HarvestPermitAreaAuthorization extends AbstractEntityAuthorization<HarvestPermitArea> {

    private enum Role {
        PARTNER
    }

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    public HarvestPermitAreaAuthorization() {
        allowCRUD(ROLE_ADMIN, ROLE_MODERATOR);

        allow(EntityPermission.CREATE, SEURAN_YHDYSHENKILO);
        allow(EntityPermission.READ, SEURAN_YHDYSHENKILO, SEURAN_JASEN, Role.PARTNER);
        allow(EntityPermission.UPDATE, SEURAN_YHDYSHENKILO);
        allow(EntityPermission.DELETE, SEURAN_YHDYSHENKILO);
    }

    @Override
    protected void authorizeTarget(@Nonnull final AuthorizationTokenCollector collector,
                                   @Nonnull final HarvestPermitArea harvestPermitArea,
                                   @Nonnull final UserInfo userInfo) {
        final HuntingClub club = harvestPermitArea.getClub();

        if (club == null) {
            return;
        }

        userAuthorizationHelper.getPerson(userInfo).ifPresent(activePerson -> {
            collector.addAuthorizationRole(SEURAN_YHDYSHENKILO, () ->
                    userAuthorizationHelper.isClubContact(club, activePerson));

            collector.addAuthorizationRole(SEURAN_JASEN, () ->
                    userAuthorizationHelper.isClubMember(club, activePerson));

            collector.addAuthorizationRole(Role.PARTNER, () -> {
                return isMemberOfPartner(harvestPermitArea, activePerson);
            });

        });
    }

    private boolean isMemberOfPartner(final HarvestPermitArea harvestPermitArea, final Person activePerson) {
        final QHarvestPermitArea AREA = QHarvestPermitArea.harvestPermitArea;
        final QHarvestPermitAreaPartner PARTNER = QHarvestPermitAreaPartner.harvestPermitAreaPartner;
        final QHuntingClubArea SOURCE = QHuntingClubArea.huntingClubArea;
        final QHuntingClub CLUB = QHuntingClub.huntingClub;
        final QOccupation OCCUPATION = QOccupation.occupation;

        final long count = jpqlQueryFactory.select(OCCUPATION.count())
                .from(AREA)
                .join(AREA.partners, PARTNER)
                .join(PARTNER.sourceArea, SOURCE)
                .join(SOURCE.club, CLUB)
                .join(CLUB.occupations, OCCUPATION)
                .where(AREA.eq(harvestPermitArea),
                        OCCUPATION.person.eq(activePerson),
                        OCCUPATION.validAndNotDeleted()
                ).fetchOne();
        return count > 0;
    }
}
