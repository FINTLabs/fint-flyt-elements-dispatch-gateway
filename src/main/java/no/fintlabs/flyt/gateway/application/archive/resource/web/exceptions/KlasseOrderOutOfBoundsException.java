package no.fintlabs.flyt.gateway.application.archive.resource.web.exceptions;

public class KlasseOrderOutOfBoundsException extends RuntimeException {
    public KlasseOrderOutOfBoundsException(int order) {
        super(String.format("Rekkefolge=%d is out of bounds. Rekkefolge must be 1, 2 or 3.", order));
    }
}
