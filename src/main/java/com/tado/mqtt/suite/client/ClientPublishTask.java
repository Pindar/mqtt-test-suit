package com.tado.mqtt.suite.client;

import org.fusesource.hawtbuf.AsciiBuffer;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.ByteArrayOutputStream;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.hawtdispatch.DispatchQueue;
import org.fusesource.hawtdispatch.Task;
import org.fusesource.mqtt.client.*;

import java.util.concurrent.TimeUnit;

import static org.fusesource.hawtdispatch.Dispatch.createQueue;

/**
 * Created by kuceram on 22/05/14.
 */
public class ClientPublishTask extends Task {

    private MQTT mqtt;
    private String clientId;
    private UTF8Buffer topic;
    private Buffer body;
    private boolean debug;
    private boolean prefixCounter;
    private boolean retain;
    private QoS qos = QoS.AT_MOST_ONCE;
    private int messageCount = 1;
    private long sleep = 0;
    private Callback<Integer> publishCallback;
    private Callback<Void> connectionCallback;
    private final CallbackConnection connection;

    private boolean interruped = false;

    public ClientPublishTask(MQTT mqtt) {
        this.mqtt = new MQTT(mqtt);
        this.connection = mqtt.callbackConnection();
    }

    public MQTT getMqtt() {
        return mqtt;
    }

    public void setMqtt(MQTT mqtt) {
        this.mqtt = mqtt;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public UTF8Buffer getTopic() {
        return topic;
    }

    public void setTopic(UTF8Buffer topic) {
        this.topic = topic;
    }

    public Buffer getBody() {
        return body;
    }

    public void setBody(Buffer body) {
        this.body = body;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isPrefixCounter() {
        return prefixCounter;
    }

    public void setPrefixCounter(boolean prefixCounter) {
        this.prefixCounter = prefixCounter;
    }

    public boolean isRetain() {
        return retain;
    }

    public void setRetain(boolean retain) {
        this.retain = retain;
    }

    public QoS getQos() {
        return qos;
    }

    public void setQos(QoS qos) {
        this.qos = qos;
    }

    public long getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public long getSleep() {
        return sleep;
    }

    public void setSleep(long sleep) {
        this.sleep = sleep;
    }

    public Callback<Integer> getPublishCallback() {
        return publishCallback;
    }

    public Callback<Void> getConnectionCallback() {
        return connectionCallback;
    }

    public void setConnectionCallback(Callback<Void> connectionCallback) {
        this.connectionCallback = connectionCallback;
    }

    public void setPublishCallback(Callback<Integer> publishCallback) {
        this.publishCallback = publishCallback;
    }

    @Override
    public void run() {
        // connect
        connection.connect(new Callback<Void>() {
            public void onSuccess(Void value) {
                if (debug) {
                    System.out.println(String.format("[client %s] connected", clientId));
                }
                connectionCallback.onSuccess(null);
                // send messages
                DispatchQueue queue = createQueue("mqtt client message queue");
                for (int i=0; i<messageCount; i++) {
                    final int messagePosition = i;

                    Task sendMessageTask = new Task() {
                        @Override
                        public void run() {
                            if (!interruped) {
                                final int messageSize;
                                Buffer message = body;

                                if (debug) {
                                    System.out.println(String.format("[client %s] publish message id %d - start", clientId, messagePosition));
                                }
                                if (prefixCounter) {
                                    ByteArrayOutputStream os = new ByteArrayOutputStream(message.length + 15);
                                    os.write(new AsciiBuffer(String.format("[client %s]", clientId)));
                                    os.write(new AsciiBuffer(String.format("[msg id %d]", messagePosition)));
                                    os.write(':');
                                    os.write(body);
                                    message = os.toBuffer();
                                }
                                messageSize = message.length;
                                connection.publish(topic, message, qos, retain, new Callback<Void>() {
                                    public void onSuccess(Void value) {
                                        if (debug) {
                                            System.out.println(String.format("[client %s] publish message id %d - sent", clientId, messagePosition));
                                        }
                                        publishCallback.onSuccess(messageSize);
                                    }

                                    public void onFailure(Throwable value) {
                                        System.out.println(String.format("[client %s] publish failed: %s", clientId, value.toString()));
                                        if (debug) {
                                            value.printStackTrace();
                                        }
                                        publishCallback.onFailure(value);
                                    }
                                });
                            }
                        }
                    };

                    if (sleep > 0) {
                        Long actualSleep = sleep*(i+1);
                        if(debug) {
                            System.out.println(String.format("[client %s] [message %d] will sleep for %d ms", clientId, i, actualSleep));
                        }
                        queue.executeAfter(actualSleep, TimeUnit.MILLISECONDS, sendMessageTask);
                    } else {
                        queue.execute(sendMessageTask);
                    }
                }
            }
            public void onFailure(Throwable value) {
                System.out.println(String.format("[client %s] connection failure", clientId));
                if (debug) {
                    System.out.println(value);
                }
                connectionCallback.onFailure(value);
            }
        });
    }

    public void interrupt(Callback<Void> callback) {
        interruped = true;
        connection.disconnect(callback);
    }
}
