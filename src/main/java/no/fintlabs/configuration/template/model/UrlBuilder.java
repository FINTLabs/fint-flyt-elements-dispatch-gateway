package no.fintlabs.configuration.template.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Getter
@Builder
@EqualsAndHashCode
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UrlBuilder {
    private final String urlTemplate;
    private final Map<String, String> valueKeyPerPathParamKey;
    private final Map<String, String> valueKeyPerRequestParamKey;
}
