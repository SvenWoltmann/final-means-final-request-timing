package io.metricstack.requesttiming;

import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Small helper to time a block of code. This is the only class version 1.0 of
 * the library ships, and the application uses it directly.
 */
public final class RequestTiming {

    private static final Logger log = LoggerFactory.getLogger(RequestTiming.class);

    private RequestTiming() {
    }

    public static <T> T measure(String label, Supplier<T> block) {
        long startNanos = System.nanoTime();
        try {
            return block.get();
        } finally {
            long micros = (System.nanoTime() - startNanos) / 1_000;
            log.info("{} took {} µs", label, micros);
        }
    }
}
