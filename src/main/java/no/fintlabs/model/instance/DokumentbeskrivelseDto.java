package no.fintlabs.model.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

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
}
