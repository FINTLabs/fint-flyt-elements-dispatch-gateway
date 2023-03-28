package no.fintlabs.template.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import java.util.Map;

@Getter
@Builder
@EqualsAndHashCode
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UrlBuilder {

    @NotBlank
    private final String urlTemplate;

    private final Map<String, @NotBlank String> valueRefPerPathParamKey;

    private final Map<String, @NotBlank String> valueRefPerRequestParamKey;

}
