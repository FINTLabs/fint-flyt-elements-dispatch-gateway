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
    private LinkDto saksmappetype;
    private LinkDto saksstatus;
    private LinkDto journalenhet;
    private LinkDto administrativenhet;
    private LinkDto saksansvarlig;
    private LinkDto arkivdel;
    private SkjermingDto skjerming;
    private List<KlasseDto> klasse;

    public Optional<String> getTittel() {
        return Optional.ofNullable(tittel);
    }

    public Optional<String> getOffentligTittel() {
        return Optional.ofNullable(offentligTittel);
    }

    public Optional<LinkDto> getSaksmappetype() {
        return Optional.ofNullable(saksmappetype);
    }

    public Optional<LinkDto> getSaksstatus() {
        return Optional.ofNullable(saksstatus);
    }

    public Optional<LinkDto> getJournalenhet() {
        return Optional.ofNullable(journalenhet);
    }

    public Optional<LinkDto> getAdministrativenhet() {
        return Optional.ofNullable(administrativenhet);
    }

    public Optional<LinkDto> getSaksansvarlig() {
        return Optional.ofNullable(saksansvarlig);
    }

    public Optional<LinkDto> getArkivdel() {
        return Optional.ofNullable(arkivdel);
    }

    public Optional<SkjermingDto> getSkjerming() {
        return Optional.ofNullable(skjerming);
    }

    public Optional<List<KlasseDto>> getKlasse() {
        return Optional.ofNullable(klasse);
    }

}
