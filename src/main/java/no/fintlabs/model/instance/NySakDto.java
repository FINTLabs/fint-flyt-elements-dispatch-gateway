package no.fintlabs.model.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NySakDto {
    private String tittel;
    private String offentligTittel;
    private String saksmappetype;
    private String saksstatus;
    private String journalenhet;
    private String administrativenhet;
    private String saksansvarlig;
    private String arkivdel;
    private SkjermingDto skjerming;
    private List<KlasseDto> klasse;

    public Optional<String> getTittel() {
        return Optional.ofNullable(tittel);
    }

    public Optional<String> getOffentligTittel() {
        return Optional.ofNullable(offentligTittel);
    }

    public Optional<String> getSaksmappetype() {
        return Optional.ofNullable(saksmappetype);
    }

    public Optional<String> getSaksstatus() {
        return Optional.ofNullable(saksstatus);
    }

    public Optional<String> getJournalenhet() {
        return Optional.ofNullable(journalenhet);
    }

    public Optional<String> getAdministrativenhet() {
        return Optional.ofNullable(administrativenhet);
    }

    public Optional<String> getSaksansvarlig() {
        return Optional.ofNullable(saksansvarlig);
    }

    public Optional<String> getArkivdel() {
        return Optional.ofNullable(arkivdel);
    }

    public Optional<SkjermingDto> getSkjerming() {
        return Optional.ofNullable(skjerming);
    }

    public Optional<List<KlasseDto>> getKlasse() {
        return Optional.ofNullable(klasse);
    }

}
