package no.fintlabs.flyt.gateway.application.archive.links;

import no.fint.model.resource.FintLinks;
import no.fint.model.resource.Link;

import java.util.List;
import java.util.stream.Collectors;

public class ResourceLinkUtil {

    public static List<String> getSelfLinks(FintLinks resource) {
        return resource.getSelfLinks()
                .stream()
                .map(Link::getHref)
                .collect(Collectors.toList());
    }

}
