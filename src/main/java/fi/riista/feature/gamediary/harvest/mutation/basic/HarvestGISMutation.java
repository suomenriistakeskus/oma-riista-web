package fi.riista.feature.gamediary.harvest.mutation.basic;

import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.mutation.HarvestMutation;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.integration.mml.dto.MMLRekisteriyksikonTietoja;

public class HarvestGISMutation implements HarvestMutation {
    private final Riistanhoitoyhdistys rhyByLocation;
    private final Municipality municipality;
    private final MMLRekisteriyksikonTietoja mmlRekisteriyksikonTietoja;

    public HarvestGISMutation(final Riistanhoitoyhdistys rhyByLocation,
                              final Municipality locationMunicipality,
                              final MMLRekisteriyksikonTietoja mmlRekisteriyksikonTietoja) {
        this.rhyByLocation = rhyByLocation;
        this.municipality = locationMunicipality;
        this.mmlRekisteriyksikonTietoja = mmlRekisteriyksikonTietoja;
    }

    @Override
    public void accept(final Harvest harvest) {
        harvest.setMunicipalityCode(municipality != null ? municipality.getOfficialCode() : null);
        harvest.setPropertyIdentifier(mmlRekisteriyksikonTietoja != null ? mmlRekisteriyksikonTietoja.getPropertyIdentifier() : null);
        harvest.setRhy(rhyByLocation);
    }

    public Riistanhoitoyhdistys getRhyByLocation() {
        return rhyByLocation;
    }
}
