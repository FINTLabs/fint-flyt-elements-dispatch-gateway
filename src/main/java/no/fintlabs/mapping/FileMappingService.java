package no.fintlabs.mapping;

import no.fint.model.resource.arkiv.noark.DokumentfilResource;
import no.fintlabs.model.File;
import org.springframework.stereotype.Service;

@Service
public class FileMappingService {

    public DokumentfilResource mapToDokumentfilResource(File file) {
        DokumentfilResource dokumentfilResource = new DokumentfilResource();
        dokumentfilResource.setFilnavn(file.getName());
        dokumentfilResource.setFormat(file.getType());
        dokumentfilResource.setData(file.getBase64Contents());
        return dokumentfilResource;
    }

}
