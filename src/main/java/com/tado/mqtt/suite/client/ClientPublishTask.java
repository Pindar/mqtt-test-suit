package com.tado.mqtt.suite.client;

import com.tado.mqtt.suite.values.Configuration;
import org.fusesource.hawtbuf.AsciiBuffer;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.ByteArrayOutputStream;
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
    private QoS qos = QoS.AT_MOST_ONCE;
    private Callback<Integer> publishCallback;
    private Callback<Void> connectionCallback;
    private final CallbackConnection connection;
    private Configuration configuration;

    private boolean interrupted = false;

    public ClientPublishTask(Configuration config) {
        this.mqtt = new MQTT(config.getMqtt());
        this.connection = config.getMqtt().callbackConnection();
        configuration = config;
    }

    public MQTT getMqtt() {
        return mqtt;
    }

    public void setMqtt(MQTT mqtt) {
        this.mqtt = mqtt;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
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
                if (configuration.isDebug()) {
                    System.out.println(String.format("[client %s] connected", clientId));
                }
                connectionCallback.onSuccess(null);
                // send messages
                DispatchQueue queue = createQueue("mqtt client message queue");
                for (int i = 0; i < configuration.getMessageCount(); i++) {
                    final int messagePosition = i;

                    Task sendMessageTask = new Task() {
                        @Override
                        public void run() {
                            if (!interrupted) {
                                final int messageSize;
                                Buffer message = configuration.getBody();

                                if (configuration.isDebug()) {
                                    System.out.println(String.format("[client %s] publish message id %d - start", clientId, messagePosition));
                                }
                                if (configuration.isPrefixCounter()) {
                                    ByteArrayOutputStream os = new ByteArrayOutputStream(message.length + 15);
                                    os.write(new AsciiBuffer(String.format("[client %s]", clientId)));
                                    os.write(new AsciiBuffer(String.format("[msg id %d]", messagePosition)));
                                    os.write(':');
                                    os.write(configuration.getBody());
                                    message = os.toBuffer();
                                }
                                messageSize = message.length;
                                connection.publish(configuration.getTopic(), message, qos, configuration.isRetain(), new Callback<Void>() {
                                    public void onSuccess(Void value) {
                                        if (configuration.isDebug()) {
                                            System.out.println(String.format("[client %s] publish message id %d - sent", clientId, messagePosition));
                                        }
                                        publishCallback.onSuccess(messageSize);
                                    }

                                    public void onFailure(Throwable value) {
                                        System.out.println(String.format("[client %s] publish failed: %s", clientId, value.toString()));
                                        if (configuration.isDebug()) {
                                            value.printStackTrace();
                                        }
                                        publishCallback.onFailure(value);
                                    }
                                });
                            }
                        }
                    };

                    if (configuration.getSleep() > 0) {
                        Long actualSleep = configuration.getSleep()*(i+1);
                        if(configuration.isDebug()) {
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
                if (configuration.isDebug()) {
                    value.printStackTrace();
                }
                connectionCallback.onFailure(value);
            }
        });
    }

    public void interrupt(Callback<Void> callback) {
        interrupted = true;
        connection.disconnect(callback);
    }
}
