package fi.riista.api.external;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.integration.metsastajarekisteri.innofactor.InnofactorImportRunner;
import fi.riista.integration.metsastajarekisteri.input.PendingImportFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/api/v1/import/metsastajarekisteri")
public class MetsastajaRekisteriController {
    private static final Logger LOG = LoggerFactory.getLogger(MetsastajaRekisteriController.class);

    @Resource
    private InnofactorImportRunner innofactorImportRunner;

    @Resource
    private ActiveUserService activeUserService;

    @PreAuthorize("hasPrivilege('IMPORT_METSASTAJAREKISTERI')")
    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> uploadImportFile(@RequestParam("file") MultipartFile multiPart) {
        try {
            LOG.info("Preparing upload filename={} username={}",
                    multiPart.getOriginalFilename(),
                    activeUserService.getActiveUserInfo().getUsername());

            final PendingImportFile pendingImportFile = innofactorImportRunner.prepareUpload(multiPart);

            LOG.info("Scheduling async processing");

            innofactorImportRunner.runAsync(pendingImportFile);

            return ResponseEntity.ok("ok");

        } catch (Exception ex) {
            LOG.error("Import error", ex);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }
}
