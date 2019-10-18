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
import fi.riista.feature.permit.application.PermitHolder;

import java.util.function.Consumer;

public interface HuntingGroupFixtureMixin extends FixtureMixin {

    default void withMooseHuntingGroupFixture(final Consumer<HuntingGroupFixture> consumer) {
        consumer.accept(new HuntingGroupFixture(getEntitySupplier()));
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
        public final Occupation groupMemberOccupation;

        public HuntingGroupFixture(final EntitySupplier es) {
            this(es, es.newRiistanhoitoyhdistys(), es.newGameSpeciesMoose());
        }

        public HuntingGroupFixture(final EntitySupplier es,
                                   final Riistanhoitoyhdistys rhy,
                                   final GameSpecies species) {

            this(es, es.newHarvestPermitSpeciesAmount(es.newMooselikePermit(rhy), species), true);
        }

        public HuntingGroupFixture(final EntitySupplier es,
                                   final HarvestPermitSpeciesAmount speciesAmount,
                                   final boolean setClubAsPermitHolder) {

            this.speciesAmount = speciesAmount;

            species = speciesAmount.getGameSpecies();
            permit = speciesAmount.getHarvestPermit();
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
            es.newOccupation(club, groupLeader, OccupationType.SEURAN_JASEN);
            groupLeaderOccupation = es.newOccupation(group, groupLeader, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
            groupLeaderOccupation.setCallOrder(1);

            groupMember = es.newPerson();
            es.newOccupation(club, groupMember, OccupationType.SEURAN_JASEN);
            groupMemberOccupation = es.newOccupation(group, groupMember, OccupationType.RYHMAN_JASEN);
        }
    }
}
