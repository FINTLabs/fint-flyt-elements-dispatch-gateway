package no.fintlabs.model.instance;

import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Optional;

@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArchiveInstance {

    @Valid
    @NotNull
    @Getter
    private SakDto sak;

    private JournalpostDto journalpost;

    public Optional<JournalpostDto> getJournalpost() {
        return Optional.ofNullable(journalpost);
    }

}
