package fi.riista.cli;

import com.google.common.base.Stopwatch;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBHexFileReader;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTWriter;
import fi.riista.util.GISUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

public class UnionCli {
    private static final Logger LOG = LoggerFactory.getLogger(UnionCli.class);

    // psql -qA --tuples-only -d riistakeskus -c "SELECT ST_AsBinary(ST_SubDivide(geom, 2048)) FROM zone where zone_id in ()" | cut -c 3- > shapes.txt
    public static void main(String[] args) throws IOException {
        final Stopwatch sw = Stopwatch.createStarted();
        final Geometry union = GISUtils.computeUnionFaster(readGeometries("shapes.txt"));

        LOG.info("Result contains {} areas", union.getNumGeometries());
        LOG.info("Completed union at {} with areaSize {}", sw, union.getArea());

        writeGeometries(union, "shapes-out.txt");
    }

    private static List<Geometry> readGeometries(final String fileName) {
        try (final Reader reader = new FileReader(new File(fileName))) {
            final GeometryFactory geometryFactory = GISUtils.getGeometryFactory(GISUtils.SRID.ETRS_TM35FIN);
            final WKBReader wkbReader = new WKBReader(geometryFactory);
            final WKBHexFileReader wkbHexFileReader = new WKBHexFileReader(reader, wkbReader);
            return wkbHexFileReader.read();

        } catch (ParseException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeGeometries(final Geometry geom, final String fileName) throws IOException {
        try (final Writer writer = new FileWriter(new File(fileName))) {
            final WKTWriter wkbWriter = new WKTWriter(2);
            wkbWriter.write(geom, writer);
        }
    }
}
