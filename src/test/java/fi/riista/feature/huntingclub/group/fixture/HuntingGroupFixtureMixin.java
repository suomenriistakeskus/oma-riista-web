package fi.riista.feature.huntingclub.group.fixture;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.fixture.FixtureMixin;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.decision.PermitDecision;

import java.util.function.Consumer;

public interface HuntingGroupFixtureMixin extends FixtureMixin {

    default void withMooseHuntingGroupFixture(final Consumer<HuntingGroupFixture> consumer) {
        consumer.accept(new HuntingGroupFixture(getEntitySupplier()));
    }

    default void withDeerHuntingGroupFixture(final Consumer<HuntingGroupFixture> consumer) {
        withHuntingGroupFixture(getEntitySupplier().newRiistanhoitoyhdistys(),
                                getEntitySupplier().newGameSpeciesWhiteTailedDeer(),
                                consumer);
    }

    default void withHuntingGroupFixture(final GameSpecies species, final Consumer<HuntingGroupFixture> consumer) {
        withHuntingGroupFixture(getEntitySupplier().newRiistanhoitoyhdistys(), species, consumer);
    }

    default void withHuntingGroupFixture(final Riistanhoitoyhdistys rhy,
                                         final GameSpecies species,
                                         final Consumer<HuntingGroupFixture> consumer) {

        consumer.accept(new HuntingGroupFixture(getEntitySupplier(), rhy, species));
    }

    default void withHuntingGroupFixture(final HarvestPermitSpeciesAmount speciesAmount,
                                         final Consumer<HuntingGroupFixture> consumer) {

        consumer.accept(new HuntingGroupFixture(getEntitySupplier(), speciesAmount, false));
    }

    class HuntingGroupFixture {
        public final Riistanhoitoyhdistys rhy;
        public final GameSpecies species;

        public final HarvestPermit permit;
        public final HarvestPermitSpeciesAmount speciesAmount;

        public final HuntingClub club;
        public final HuntingClubGroup group;
        public final HuntingClubArea clubArea;
        public final GISZone zone;
        public final GeoLocation zoneCentroid;

        public final Person clubContact;
        public final Person clubMember;
        public final Person groupLeader;
        public final Person groupMember;

        public final Occupation clubContactOccupation;
        public final Occupation clubMemberOccupation;
        public final Occupation groupLeaderOccupation;
        public final Occupation groupLeaderClubOccupation;
        public final Occupation groupMemberOccupation;
        public final Occupation groupMemberClubOccupation;

        public final PermitDecision decision;

        public final HarvestPermitApplication application;
        public final HarvestPermitApplicationSpeciesAmount applicationSpeciesAmount;

        public HuntingGroupFixture(final EntitySupplier es) {
            this(es, es.newRiistanhoitoyhdistys(), es.newGameSpeciesMoose());
        }

        public HuntingGroupFixture(final EntitySupplier es,
                                   final Riistanhoitoyhdistys rhy,
                                   final GameSpecies species) {

            this(
                    es,
                    es.newHarvestPermitSpeciesAmount(es.newMooselikePermit(rhy), species),
                    es.newPermitDecision(rhy, species),
                    es.newHarvestPermitApplication(rhy, es.newHarvestPermitArea(), species),
                    species,
                    true
            );
        }

        public HuntingGroupFixture(final EntitySupplier es,
                                   final HarvestPermitSpeciesAmount speciesAmount,
                                   final boolean setClubAsPermitHolder) {
            this(
                    es,
                    speciesAmount,
                    es.newPermitDecision(speciesAmount.getHarvestPermit().getRhy(), speciesAmount.getGameSpecies()),
                    es.newHarvestPermitApplication(speciesAmount.getHarvestPermit().getRhy(), es.newHarvestPermitArea(), speciesAmount.getGameSpecies()),
                    speciesAmount.getGameSpecies(),
                    setClubAsPermitHolder
            );
        }

        public HuntingGroupFixture(final EntitySupplier es,
                                   final HarvestPermitSpeciesAmount speciesAmount,
                                   final PermitDecision decision,
                                   final HarvestPermitApplication application,
                                   final GameSpecies species,
                                   final boolean setClubAsPermitHolder) {
            this(
                    es,
                    speciesAmount,
                    decision,
                    application,
                    es.newHarvestPermitApplicationSpeciesAmount(application, species),
                    setClubAsPermitHolder
            );
        }

        public HuntingGroupFixture(final EntitySupplier es,
                                   final HarvestPermitSpeciesAmount speciesAmount,
                                   final PermitDecision decision,
                                   final HarvestPermitApplication application,
                                   final HarvestPermitApplicationSpeciesAmount applicationSpeciesAmount,
                                   final boolean setClubAsPermitHolder) {
            this.speciesAmount = speciesAmount;

            species = speciesAmount.getGameSpecies();
            permit = speciesAmount.getHarvestPermit();
            permit.setPermitDecision(decision);
            rhy = permit.getRhy();

            club = es.newHuntingClub(rhy);
            permit.getPermitPartners().add(club);
            if (setClubAsPermitHolder) {
                permit.setHuntingClub(club);
                permit.setPermitHolder(PermitHolder.createHolderForClub(club));
            } else {
                permit.setPermitHolder(PermitHolder.createHolderForPerson(permit.getOriginalContactPerson()));
            }

            group = es.newHuntingClubGroup(club, speciesAmount);
            group.updateHarvestPermit(permit);

            zoneCentroid = es.geoLocation(GeoLocation.Source.MANUAL);
            clubArea = es.newHuntingClubArea(club, "fi", "sv", group.getHuntingYear());
            zone = es.newGISZoneContaining(zoneCentroid);
            clubArea.setZone(zone);
            group.setHuntingArea(clubArea);

            clubContact = es.newPerson();
            clubContactOccupation = es.newOccupation(club, clubContact, OccupationType.SEURAN_YHDYSHENKILO);

            clubMember = es.newPerson();
            clubMemberOccupation = es.newOccupation(club, clubMember, OccupationType.SEURAN_JASEN);

            groupLeader = es.newPerson();
            groupLeaderClubOccupation = es.newOccupation(club, groupLeader, OccupationType.SEURAN_JASEN);
            groupLeaderOccupation = es.newOccupation(group, groupLeader, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
            groupLeaderOccupation.setCallOrder(1);

            groupMember = es.newPerson();
            groupMemberClubOccupation = es.newOccupation(club, groupMember, OccupationType.SEURAN_JASEN);
            groupMemberOccupation = es.newOccupation(group, groupMember, OccupationType.RYHMAN_JASEN);

            this.decision = decision;

            this.application = application;
            this.applicationSpeciesAmount = applicationSpeciesAmount;
        }
    }
}
