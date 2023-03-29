package no.fintlabs.model.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DokumentbeskrivelseDto {
    private String tittel;
    private String dokumentstatus;
    private String dokumentType;
    private String tilknyttetRegistreringSom;
    private Collection<DokumentobjektDto> dokumentobjekt;

    public Optional<String> getTittel() {
        return Optional.ofNullable(tittel);
    }

    public Optional<String> getDokumentType() {
        return Optional.ofNullable(dokumentType);
    }

    public Optional<String> getTilknyttetRegistreringSom() {
        return Optional.ofNullable(tilknyttetRegistreringSom);
    }

    public Optional<String> getDokumentstatus() {
        return Optional.ofNullable(dokumentstatus);
    }

    public Optional<Collection<DokumentobjektDto>> getDokumentobjekt() {
        return Optional.ofNullable(dokumentobjekt);
    }

}
