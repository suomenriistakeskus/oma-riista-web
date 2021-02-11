package fi.riista.config.jpa;

import org.hibernate.spatial.JTSGeometryType;
import org.hibernate.spatial.dialect.h2geodb.GeoDBDialect;
import org.hibernate.spatial.dialect.h2geodb.GeoDBGeometryTypeDescriptor;
import org.hibernate.type.StandardBasicTypes;

import java.sql.Types;

public class CustomH2Dialect extends GeoDBDialect {
    public static final JTSGeometryType JTS_GEOMETRY_TYPE = new JTSGeometryType(GeoDBGeometryTypeDescriptor.INSTANCE);

    public CustomH2Dialect() {
        super();

        registerColumnType(Types.TIMESTAMP_WITH_TIMEZONE, "timestamp with timezone");
        registerColumnType(Types.OTHER, "GEOMETRY");

        registerHibernateType(Types.TIMESTAMP_WITH_TIMEZONE, StandardBasicTypes.TIMESTAMP.getName());
        registerHibernateType(Types.OTHER, JTS_GEOMETRY_TYPE.getName());
    }
}
