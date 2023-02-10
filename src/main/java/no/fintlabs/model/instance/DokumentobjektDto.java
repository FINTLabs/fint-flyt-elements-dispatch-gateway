package no.fintlabs.model.instance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DokumentobjektDto {
    private LinkDto variantformat;
    private LinkDto filformat;
    @JsonProperty(value = "file")
    @NotNull
    private UUID fileId;

    public Optional<LinkDto> getVariantformat() {
        return Optional.ofNullable(variantformat);
    }

    public Optional<LinkDto> getFilformat() {
        return Optional.ofNullable(filformat);
    }

    public Optional<UUID> getFileId() {
        return Optional.ofNullable(fileId);
    }

}
