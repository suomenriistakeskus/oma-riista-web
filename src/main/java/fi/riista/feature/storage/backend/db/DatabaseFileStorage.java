package fi.riista.feature.storage.backend.db;

import com.google.common.io.ByteStreams;
import fi.riista.feature.storage.backend.FileStorageSpi;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.feature.storage.metadata.StorageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@Component
public class DatabaseFileStorage implements FileStorageSpi {
    private JdbcOperations jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public StorageType getType() {
        return StorageType.LOCAL_DATABASE;
    }

    @Override
    @Transactional(rollbackFor = IOException.class)
    public void retrieveFile(final PersistentFileMetadata metadata, final OutputStream outputStream) {
        final Object[] params = {metadata.getId().toString()};

        jdbcTemplate.query("SELECT file_content FROM file_content WHERE file_metadata_uuid = ?", params, rs -> {
            try (final InputStream binaryStream = rs.getBinaryStream(1)) {
                ByteStreams.copy(binaryStream, outputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    @Transactional(rollbackFor = IOException.class)
    public void storeFile(final FileType fileType,
                          final PersistentFileMetadata metadata,
                          final InputStream inputStream) {
        jdbcTemplate.update("INSERT INTO file_content (file_metadata_uuid, file_content) VALUES (?, ?)", ps -> {
            ps.setString(1, metadata.getId().toString());
            ps.setBinaryStream(2, inputStream);
        });
    }

    @Override
    public void removeFromStorage(final PersistentFileMetadata metadata) {
        jdbcTemplate.update("DELETE FROM file_content WHERE file_metadata_uuid = ?", metadata.getId().toString());
    }

    @Override
    public boolean isConfigured() {
        return true;
    }
}
