package fi.riista.feature.gamediary.harvest.mutation.basic;

import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.mutation.HarvestMutation;
import fi.riista.feature.gis.metsahallitus.MetsahallitusAreaLookupResult;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.integration.mml.dto.MMLRekisteriyksikonTietoja;

public class HarvestGISMutation implements HarvestMutation {
    private final Riistanhoitoyhdistys rhyByLocation;
    private final Municipality municipality;
    private final MMLRekisteriyksikonTietoja mmlRekisteriyksikonTietoja;
    private final MetsahallitusAreaLookupResult metsahallitusAreaLookupResult;

    public HarvestGISMutation(final Riistanhoitoyhdistys rhyByLocation,
                              final Municipality locationMunicipality,
                              final MMLRekisteriyksikonTietoja mmlRekisteriyksikonTietoja,
                              final MetsahallitusAreaLookupResult metsahallitusAreaLookupResult) {
        this.rhyByLocation = rhyByLocation;
        this.municipality = locationMunicipality;
        this.mmlRekisteriyksikonTietoja = mmlRekisteriyksikonTietoja;
        this.metsahallitusAreaLookupResult = metsahallitusAreaLookupResult;
    }

    @Override
    public void accept(final Harvest harvest) {
        harvest.setMunicipalityCode(municipality != null ? municipality.getOfficialCode() : null);
        harvest.setPropertyIdentifier(mmlRekisteriyksikonTietoja != null ? mmlRekisteriyksikonTietoja.getPropertyIdentifier() : null);
        harvest.setRhy(rhyByLocation);

        if (metsahallitusAreaLookupResult != null) {
            harvest.setMetsahallitusHirviAlueId(metsahallitusAreaLookupResult.getHirviAlueId());
            harvest.setMetsahallitusPienriistaAlueId(metsahallitusAreaLookupResult.getPienriistaAlueId());
        }
    }

    public Riistanhoitoyhdistys getRhyByLocation() {
        return rhyByLocation;
    }
}
