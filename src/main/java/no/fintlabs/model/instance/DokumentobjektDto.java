package no.fintlabs.model.instance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.fint.model.resource.Link;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DokumentobjektDto {
    private Link variantformat;
    private Link filformat;
    @JsonProperty(value = "file")
    private UUID fileId;
}
