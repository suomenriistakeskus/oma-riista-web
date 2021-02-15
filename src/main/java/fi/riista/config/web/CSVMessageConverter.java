package fi.riista.config.web;

import au.com.bytecode.opencsv.CSVWriter;
import fi.riista.config.Constants;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Optional;

public class CSVMessageConverter extends AbstractHttpMessageConverter<CSVHttpResponse> {

    public static final Charset DEFAULT_CHARSET = Constants.DEFAULT_CHARSET;
    public static final MediaType DEFAULT_MEDIA_TYPE = new MediaType("text", "csv", DEFAULT_CHARSET);

    public CSVMessageConverter() {
        super(DEFAULT_MEDIA_TYPE);
    }

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return CSVHttpResponse.class.isAssignableFrom(clazz);
    }

    @Override
    protected CSVHttpResponse readInternal(Class<? extends CSVHttpResponse> clazz, HttpInputMessage inputMessage) {

        throw new HttpMessageNotReadableException("Not implemented", inputMessage);
    }

    @Override
    protected void writeInternal(CSVHttpResponse response, HttpOutputMessage output) throws IOException {
        final Charset charset = getCharset(response);

        output.getHeaders().setContentType(createMediaType(charset));
        output.getHeaders().set("Content-Disposition", "attachment; filename=\"" + response.getFilename() + "\"");

        try (final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(output.getBody(), charset);
             final CSVWriter writer = new CSVWriter(outputStreamWriter, ';')) {

            if (response.getHeaderRow() != null) {
                writer.writeNext(response.getHeaderRow());
            }

            writer.writeAll(response.getRows());
            writer.flush();
        }
    }

    private static Charset getCharset(CSVHttpResponse response) {
        return Optional.ofNullable(response.getCharset()).orElse(DEFAULT_CHARSET);
    }

    private static MediaType createMediaType(Charset charset) {
        return charset.equals(DEFAULT_CHARSET) ? DEFAULT_MEDIA_TYPE : new MediaType("text", "csv", charset);
    }

}
