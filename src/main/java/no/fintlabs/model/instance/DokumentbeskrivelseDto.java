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
    private LinkDto dokumentType;
    private LinkDto tilknyttetRegistreringSom;
    private LinkDto dokumentstatus;
    private Collection<DokumentobjektDto> dokumentobjekt;

    public Optional<String> getTittel() {
        return Optional.ofNullable(tittel);
    }

    public Optional<LinkDto> getDokumentType() {
        return Optional.ofNullable(dokumentType);
    }

    public Optional<LinkDto> getTilknyttetRegistreringSom() {
        return Optional.ofNullable(tilknyttetRegistreringSom);
    }

    public Optional<LinkDto> getDokumentstatus() {
        return Optional.ofNullable(dokumentstatus);
    }

    public Optional<Collection<DokumentobjektDto>> getDokumentobjekt() {
        return Optional.ofNullable(dokumentobjekt);
    }

}
