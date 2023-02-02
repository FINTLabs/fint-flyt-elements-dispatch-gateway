package no.fintlabs.model.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.fint.model.resource.Link;

import java.util.Collection;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DokumentbeskrivelseDto {
    private String tittel;
    private Link dokumentType;
    private Link tilknyttetRegistreringSom;
    private Link dokumentstatus;
    private Collection<DokumentobjektDto> dokumentobjekt;
}
