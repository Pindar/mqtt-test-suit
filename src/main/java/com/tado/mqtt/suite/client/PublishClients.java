package com.tado.mqtt.suite.client;

import org.fusesource.hawtbuf.AsciiBuffer;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.ByteArrayOutputStream;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.hawtdispatch.Dispatch;
import org.fusesource.hawtdispatch.DispatchQueue;
import org.fusesource.hawtdispatch.Task;
import org.fusesource.mqtt.client.*;

import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.fusesource.hawtdispatch.Dispatch.createQueue;

/**
 * Created by kuceram on 19/05/14.
 */
public class PublishClients {

    private MQTT mqtt = new MQTT();
    private QoS qos = QoS.AT_MOST_ONCE;
    private UTF8Buffer topic;
    private Buffer body;
    private boolean debug;
    private boolean retain;
    private int clientCount = 1;
    private long messageCountPerClient = 1;
    private long sleep = 0;
    private boolean prefixCounter;

    private final ConcurrentHashMap<Integer, CallbackConnection> connections =
        new ConcurrentHashMap<Integer, CallbackConnection>();

    public MQTT getMqtt() {
        return mqtt;
    }

    public void setMqtt(MQTT mqtt) {
        this.mqtt = mqtt;
    }

    public QoS getQos() {
        return qos;
    }

    public void setQos(QoS qos) {
        this.qos = qos;
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

    public boolean isRetain() {
        return retain;
    }

    public void setRetain(boolean retain) {
        this.retain = retain;
    }

    public long getClientCount() {
        return clientCount;
    }

    public void setClientCount(int clientCount) {
        this.clientCount = clientCount;
    }

    public long getMessageCountPerClient() {
        return messageCountPerClient;
    }

    public void setMessageCountPerClient(long messageCountPerClient) {
        this.messageCountPerClient = messageCountPerClient;
    }

    public long getSleep() {
        return sleep;
    }

    public void setSleep(long sleep) {
        this.sleep = sleep;
    }

    public boolean isPrefixCounter() {
        return prefixCounter;
    }

    public void setPrefixCounter(boolean prefixCounter) {
        this.prefixCounter = prefixCounter;
    }

    public void execute() {
        final CountDownLatch done = new CountDownLatch(clientCount);
        DispatchQueue queue = createQueue("mqtt clients queue");

        for (int i=0; i<clientCount; i++) {
            final int clientId = i;
            queue.execute(new Task() {
                @Override
                public void run() {
                    // create new mqtt client
                    MQTT newMqtt = new MQTT(mqtt);
                    final CallbackConnection connection = newMqtt.callbackConnection();
                    connections.put(clientId, connection);

                    // connect
                    connection.listener(new Listener() {

                        public void onConnected() {
                            if (debug) {
                                System.out.println(String.format("[client %d] connected", clientId));
                            }
                        }

                        public void onDisconnected() {
                            if (debug) {
                                System.out.println(String.format("[client %d] disconnected", clientId));
                            }
                        }

                        public void onPublish(UTF8Buffer topic, Buffer body, Runnable ack) {
                        }

                        public void onFailure(Throwable value) {
                            if (debug) {
                                value.printStackTrace();
                            } else {
                                System.err.println(value);
                            }
                            done.countDown();
                        }
                    });

                    connection.resume();
                    connection.connect(new Callback<Void>() {
                        public void onFailure(Throwable value) {
                            if (debug) {
                                value.printStackTrace();
                            } else {
                                System.err.println(value);
                            }
                            done.countDown();
                        }
                        public void onSuccess(Void value) {
                        }
                    });

                    // send the messages
                    new Task() {
                        long sent = 0;
                        public void run() {
                            final Task publish = this;
                            Buffer message  = body;
                            if(prefixCounter) {
                                long id = sent + 1;
                                ByteArrayOutputStream os = new ByteArrayOutputStream(message.length + 15);
                                os.write(new AsciiBuffer(String.format("[client %d]", clientId)));
                                os.write(new AsciiBuffer(String.format("[msg #%d]", id)));
                                os.write(':');
                                os.write(body);
                                message = os.toBuffer();
                            }
                            connection.publish(topic, message, qos, retain, new Callback<Void>() {
                                public void onSuccess(Void value) {
                                    sent ++;
                                    if(debug) {
                                        System.out.println(String.format("[client %d] sent message #%d", clientId, sent));
                                    }
                                    if( sent < messageCountPerClient ) {
                                        if(sleep>0) {
                                            if(debug) {
                                                System.out.println(String.format("[client %d] sleeping...", clientId));
                                            }
                                            connection.getDispatchQueue().executeAfter(sleep, TimeUnit.MILLISECONDS, publish);
                                        } else {
                                            connection.getDispatchQueue().execute(publish);
                                        }
                                    } else {
                                        connection.disconnect(new Callback<Void>() {
                                            public void onSuccess(Void value) {
                                                done.countDown();
                                            }
                                            public void onFailure(Throwable value) {
                                                done.countDown();
                                            }
                                        });
                                    }
                                }
                                public void onFailure(Throwable value) {
                                    System.out.println(String.format("[client %d] publish failed: %s", value.toString()));
                                    if(debug) {
                                        value.printStackTrace();
                                    }
                                    connection.disconnect(new Callback<Void>() {
                                        public void onSuccess(Void value) {
                                            done.countDown();
                                        }
                                        public void onFailure(Throwable value) {
                                            done.countDown();
                                        }
                                    });
                                }
                            });
                        }
                    }.run();
                }
            });
        }

        try {
            done.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void interrupt() {
        // close all connections
        for(final Integer key : connections.keySet()) {
            final CallbackConnection connection = connections.get(key);
            connection.getDispatchQueue().execute(new Task() {
                @Override
                public void run() {
                    connection.disconnect(new Callback<Void>() {
                        public void onSuccess(Void value) {}

                        public void onFailure(Throwable value) {}
                    });
                }
            });
        }
    }
}
