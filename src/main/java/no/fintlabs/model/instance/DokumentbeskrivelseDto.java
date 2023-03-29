package no.fintlabs.model.instance;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Optional;

@Builder
@Jacksonized
public class DokumentbeskrivelseDto {
    private final String tittel;
    private final String dokumentstatus;
    private final String dokumentType;
    private final String tilknyttetRegistreringSom;
    private final Collection<@NotNull @Valid DokumentobjektDto> dokumentobjekt;

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
