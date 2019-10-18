package fi.riista.integration.metsastajarekisteri;

import fi.riista.integration.metsastajarekisteri.person.MetsastajaRekisteriPerson;
import fi.riista.integration.metsastajarekisteri.person.MetsastajaRekisteriPersonFormatter;
import fi.riista.integration.metsastajarekisteri.person.finnish.MetsastajaRekisteriFinnishPersonValidator;
import org.junit.Test;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;

public class InnofactorImportFileFieldSetMapperTest {

    @Test
    public void test() throws Exception {
        final String line = "11111111;250357-982X;19000101;;;Meikäläinen;Matti Esko;fi;fi;osoite@email.fi;123;123;" +
                "20160101;20170101;2016;2017;20160801;20170731;19900101;20200101;;;Kadunnimi 123;01110;Kaupunki;" +
                "FI;Suomi;1;1;1;12345678901;12345678902;2017;2016";

        final DefaultLineMapper<InnofactorImportFileLine> lineMapper = new DefaultLineMapper<>();
        lineMapper.setFieldSetMapper(new InnofactorImportFileFieldSetMapper());
        lineMapper.setLineTokenizer(new DelimitedLineTokenizer(";"));

        final InnofactorImportFileLine res = lineMapper.mapLine(line, 0);
        final MetsastajaRekisteriPersonFormatter formatter = new MetsastajaRekisteriPersonFormatter();
        final MetsastajaRekisteriPerson person = formatter.process(res);
        final MetsastajaRekisteriFinnishPersonValidator validator = new MetsastajaRekisteriFinnishPersonValidator();
        validator.process(person);
    }
}
