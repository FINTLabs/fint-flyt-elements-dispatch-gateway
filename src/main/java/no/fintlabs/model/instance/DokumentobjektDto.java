package no.fintlabs.model.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.fint.model.resource.Link;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DokumentobjektDto {
    private Link variantformat;
    private Link filformat;
    private FileReference fileReference;
}
