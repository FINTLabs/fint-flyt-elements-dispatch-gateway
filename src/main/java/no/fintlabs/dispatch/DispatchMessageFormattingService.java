package no.fintlabs.dispatch;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

@Service
public class DispatchMessageFormattingService {

    public Optional<String> createFunctionalWarningMessage(
            String objectDisplayName,
            String refDisplayName,
            List<String> objectRefs
    ) {
        if (objectRefs.isEmpty()) {
            return Optional.empty();
        }
        if (objectRefs.size() == 1) {
            return Optional.of(
                    objectDisplayName + " with " + refDisplayName + "='" + objectRefs.get(0) + "'"
            );
        }
        return Optional.of(
                objectDisplayName + "s with " + refDisplayName + "s=" + objectRefs
                        .stream()
                        .collect(joining("', '", "['", "']"))
        );
    }

}
