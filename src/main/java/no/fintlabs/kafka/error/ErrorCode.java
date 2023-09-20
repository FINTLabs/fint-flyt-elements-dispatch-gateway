package no.fintlabs.kafka.error;

public enum ErrorCode {
    GENERAL_SYSTEM_ERROR,
    INSTANCE_DISPATCH_DECLINED_ERROR;

    private static final String ERROR_PREFIX = "FINT_FLYT_DISPATCH_GATEWAY_";

    public String getCode() {
        return ERROR_PREFIX + name();
    }

}
