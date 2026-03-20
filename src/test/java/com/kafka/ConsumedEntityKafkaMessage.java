package com.kafka;

public class ConsumedEntityKafkaMessage {
    private final String topic;
    private final String key;
    private final int partition;
    private final long offset;
    private final String rawValue;
    private final EntityEvent event;

    public ConsumedEntityKafkaMessage(String topic, String key, int partition, long offset,
                                      String rawValue, EntityEvent event) {
        this.topic = topic;
        this.key = key;
        this.partition = partition;
        this.offset = offset;
        this.rawValue = rawValue;
        this.event = event;
    }

    public String getTopic() {
        return topic;
    }

    public String getKey() {
        return key;
    }

    public int getPartition() {
        return partition;
    }

    public long getOffset() {
        return offset;
    }

    public String getRawValue() {
        return rawValue;
    }

    public EntityEvent getEvent() {
        return event;
    }
}
