package no.fintlabs.model.mappedinstance;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


@NoArgsConstructor
public class MappedInstance {

    private Map<String, MappedInstanceElement> elementPerKey;

    @Getter
    private Collection<Document> documents;

    public MappedInstanceElement getElement(String key) {
        return Optional.ofNullable(elementPerKey.get(key)).orElseThrow(NoSuchElementException::new);
    }

    public void setElements(Collection<MappedInstanceElement> elements) {
        this.elementPerKey = elements
                .stream()
                .collect(Collectors.toMap(
                        MappedInstanceElement::getKey,
                        Function.identity()
                ));
    }

}
