package com.hejiale.common.constants;

public interface MqConstants {
    String LOGRECORD_QUEUE = "logRecord-queue";
    String MONITOR_EXCHANGE = "Monitor-exchange";
    String LOGRECORD_ROUTING_KEY = "logRecord.routing.key";

    String LOGRECORD_RETRY_QUEUE = "logRecord-retry-queue";
    String RETRY_EXCHANGE = "Monitor-retry-exchange";
    String LOGRECORD_RETRY_ROUTING_KEY = "logRecord.retry.routing.key";

    String LOGRECORD_DLQ_QUEUE = "logRecord-dlq-queue";
    String DLX_EXCHANGE = "Monitor-dlx-exchange";
    String LOGRECORD_DLQ_ROUTING_KEY = "logRecord.dlq.routing.key";

    String RETRY_COUNT_HEADER = "x-retry-count";
    int MAX_RETRY_COUNT = 3;
    int RETRY_TTL_MILLIS = 10_000;
}
