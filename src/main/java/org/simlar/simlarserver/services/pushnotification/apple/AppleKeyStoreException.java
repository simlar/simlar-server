package org.simlar.simlarserver.services.pushnotification.apple;

final class AppleKeyStoreException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    AppleKeyStoreException(final String message) {
        super(message);
    }

    AppleKeyStoreException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
