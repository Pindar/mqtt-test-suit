package com.tado.mqtt.suite.values;

import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;

import java.util.concurrent.CountDownLatch;

/**
 * Created by simon on 30/05/14.
 */
public class Configuration {

    private final MQTT mqtt;
    private final UTF8Buffer topic;
    private final Buffer body;
    private final boolean debug;
    private final boolean prefixCounter;
    private final boolean retain;
    private final QoS qos;
    private final int messageCount;

    private final long sleep;
    private final int clientCount;
    private final boolean csv;


    public static class Builder {
        private MQTT mqtt = new MQTT();
        private UTF8Buffer topic;
        private Buffer body;
        private boolean debug;
        private boolean prefixCounter;
        private boolean retain;
        private QoS qos = QoS.AT_MOST_ONCE;
        private int messageCount = 1;
        private long sleep;
        private int clientCount = 1;
        private boolean csv = false;

        public Builder() {

        }

        public Builder mqtt(MQTT val) {
            mqtt = val;
            return this;
        }

        public Builder topic(UTF8Buffer val) {
            topic = val;
            return this;
        }

        public Builder body(Buffer val) {
            body = val;
            return this;
        }

        public Builder debug(boolean val) {
            debug = val;
            return this;
        }

        public Builder prefixCounter(boolean val) {
            prefixCounter = val;
            return this;
        }

        public Builder retain(boolean val) {
            retain = val;
            return this;
        }

        public Builder messageCount(int val) {
            messageCount = val;
            return this;
        }

        public Builder sleep(long val) {
            sleep = val;
            return this;
        }

        public Builder clientCount(int val) {
            clientCount = val;
            return this;
        }

        public Builder csv(boolean val) {
            csv = val;
            return this;
        }

        public Builder qos(QoS val) {
            qos = val;
            return this;
        }

        public Configuration build() {
            return new Configuration(this);
        }
    }


    private Configuration(Builder builder) {
        mqtt = builder.mqtt;
        topic = builder.topic;
        body = builder.body;
        debug = builder.debug;
        prefixCounter = builder.prefixCounter;
        retain = builder.retain;
        messageCount = builder.messageCount;
        sleep = builder.sleep;
        clientCount = builder.clientCount;
        csv = builder.csv;
        qos = builder.qos;
    }

    public CountDownLatch getCountDownLatch() {
        return new CountDownLatch(this.getClientCount() * this.getMessageCount());
    }

    public MQTT getMqtt() {
        return mqtt;
    }

    public UTF8Buffer getTopic() {
        return topic;
    }

    public boolean isTopicValid() {
        return topic != null && topic.length > 0;
    }

    public boolean isBodyValid() {
        return body != null;
    }

    public Buffer getBody() {
        return body;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isPrefixCounter() {
        return prefixCounter;
    }

    public boolean isRetain() {
        return retain;
    }

    public QoS getQos() {
        return qos;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public long getSleep() {
        return sleep;
    }

    public int getClientCount() {
        return clientCount;
    }

    public boolean isCsv() {
        return csv;
    }

}
