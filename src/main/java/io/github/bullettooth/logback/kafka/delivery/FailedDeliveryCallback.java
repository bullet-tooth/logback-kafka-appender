package io.github.bullettooth.logback.kafka.delivery;

/**
 * @since 0.0.1
 */
public interface FailedDeliveryCallback<E> {
    void onFailedDelivery(E evt, Throwable throwable);
}
