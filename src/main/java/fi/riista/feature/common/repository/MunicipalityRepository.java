package fi.riista.feature.common.repository;

import fi.riista.feature.common.entity.Municipality;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MunicipalityRepository extends BaseRepository<Municipality, String> {

    @Query(value = "SELECT * FROM municipality WHERE official_code = (SELECT ktunnus FROM palstaalue WHERE ST_Intersects(geom, ST_SetSRID(ST_MakePoint(:lng, :lat), 3067)) LIMIT 1) LIMIT 1", nativeQuery = true)
    Municipality findMunicipality(@Param("lat") int latitude, @Param("lng") int longitude);
}
