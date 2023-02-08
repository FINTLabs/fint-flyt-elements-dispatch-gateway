package no.fintlabs.model.instance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SkjermingDto {
    private LinkDto tilgangsrestriksjon;
    private LinkDto skjermingshjemmel;
}
