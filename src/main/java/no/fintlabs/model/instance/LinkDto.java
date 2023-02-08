package no.fintlabs.model.instance;

import com.fasterxml.jackson.annotation.JsonValue;
import no.fint.model.resource.Link;

public class LinkDto extends Link {

    @JsonValue
    @Override
    public void setVerdi(String verdi) {
        super.setVerdi(verdi);
    }

}
