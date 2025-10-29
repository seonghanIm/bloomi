package com.han.bloomi.common.trace;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TraceIdHolder {
    private static final String TRACE_ID_KEY = "traceId";

    public String current() {
        String traceId = MDC.get(TRACE_ID_KEY);
        if (traceId == null) {
            traceId = generate();
            MDC.put(TRACE_ID_KEY, traceId);
        }
        return traceId;
    }

    public String generate() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public void set(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId);
    }

    public void clear() {
        MDC.remove(TRACE_ID_KEY);
    }
}