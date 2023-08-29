package no.fintlabs.model.instance;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import no.fintlabs.model.CaseDispatchType;
import no.fintlabs.model.validation.groups.CaseByIdValidationGroup;
import no.fintlabs.model.validation.groups.CaseBySearchValidationGroup;
import no.fintlabs.model.validation.groups.NewCaseValidationGroup;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;


@Getter
@Builder
@Jacksonized
public class ArchiveInstance {

    @NotNull
    private final CaseDispatchType type;

    @Valid
    @NotNull(groups = CaseBySearchValidationGroup.class)
    private final CaseSearchParametersDto caseSearchParameters;

    @Valid
    @NotNull(groups = {NewCaseValidationGroup.class, CaseBySearchValidationGroup.class})
    private final SakDto newCase;

    @NotBlank(groups = CaseByIdValidationGroup.class)
    private final String caseId;

    @NotBlank(groups = CaseByIdValidationGroup.class)
    private final List<@NotNull @Valid JournalpostDto> journalpost;

    @Override
    public String toString() {
        return "Sensitive data omitted";
    }

}
