package no.fintlabs.model.instance;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import no.fintlabs.model.CaseDispatchType;
import no.fintlabs.model.validation.groups.CaseByIdValidationGroup;
import no.fintlabs.model.validation.groups.CaseBySearchValidationGroup;
import no.fintlabs.model.validation.groups.NewCaseValidationGroup;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;


@Getter
@Builder
@Jacksonized
public class ArchiveInstance {

    @NotNull
    private CaseDispatchType type;

    @NotNull(groups = {NewCaseValidationGroup.class, CaseBySearchValidationGroup.class})
    private SakDto newCase;

    @NotBlank(groups = CaseByIdValidationGroup.class)
    private String caseId;

    @NotBlank(groups = CaseByIdValidationGroup.class)
    private List<JournalpostDto> journalpost;

}
