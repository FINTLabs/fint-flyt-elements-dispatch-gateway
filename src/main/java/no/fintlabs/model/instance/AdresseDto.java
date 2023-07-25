package no.fintlabs.model.instance;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Builder
@Jacksonized
public class AdresseDto {
    private final Collection<@NotNull String> adresselinje;
    private final String postnummer;
    private final String poststed;

    @JsonDeserialize(using = AdresselinjeDeserializer.class)
    public Optional<Collection<String>> getAdresselinje() {
        return Optional.ofNullable(adresselinje);
    }

    public Optional<String> getPostnummer() {
        return Optional.ofNullable(postnummer);
    }

    public Optional<String> getPoststed() {
        return Optional.ofNullable(poststed);
    }

    public static class AdresselinjeDeserializer extends JsonDeserializer<Collection<String>> {

        @Override
        public Collection<String> deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException {
            JsonNode node = jp.getCodec().readTree(jp);
            List<String> addresses = new ArrayList<>();
            if (node.isArray()) {
                for (JsonNode n : node) {
                    addresses.add(n.asText());
                }
            } else if (node.isTextual()) {
                addresses.add(node.asText());
            }
            return addresses;
        }
    }

}
