package com.tado.mqtt.suite.cli;

import com.tado.mqtt.suite.client.ClientPublishTask;
import com.tado.mqtt.suite.util.ArgumentParser;
import com.tado.mqtt.suite.values.Configuration;
import com.tado.mqtt.suite.values.Result;
import com.tado.mqtt.suite.view.CommaSeparatedValues;
import com.tado.mqtt.suite.view.CommandLindInterface;
import com.tado.mqtt.suite.view.Notification;
import org.fusesource.hawtdispatch.DispatchQueue;
import org.fusesource.mqtt.client.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.fusesource.hawtdispatch.Dispatch.createQueue;

/**
 * Created by kuceram on 19/05/14.
 */
public class Publishers {

    Configuration configuration;

    private Notification output;
    private Result result = new Result();


    public static void main(String[] args) throws Exception {
        Publishers main = new Publishers();
        main.configuration = new ArgumentParser(args, main).invoke();


        if (!main.configuration.isTopicValid()) {
            CommandLindInterface.stderr("Invalid usage: no topic specified.");
            CommandLindInterface.displayHelpAndExit(1);
        }
        if (!main.configuration.isBodyValid()) {
            CommandLindInterface.stderr("Invalid usage: -z -m or -f must be specified.");
            CommandLindInterface.displayHelpAndExit(1);
        }

        if (main.configuration.isCsv()) {
            main.output = new CommaSeparatedValues();
        } else {
            main.output = new CommandLindInterface();
        }

        main.execute(main.configuration);
        System.exit(0);
    }

    private void execute(Configuration configuration) {
        result.startTimeNanosec = System.nanoTime();

        // each client has its own thread and each message is sent in a separate thread
        final CountDownLatch done = configuration.getCountDownLatch();
        DispatchQueue queue = createQueue("mqtt clients queue");

        final List<ClientPublishTask> clients = new ArrayList<ClientPublishTask>();
        handleInterruption(clients);


        // create clients and send the messages
        for (int i = 0; i < configuration.getClientCount(); i++) {
            ClientPublishTask clientPublishTask = new ClientPublishTask(configuration);
            clients.add(clientPublishTask);

            // set client options
            clientPublishTask.setClientId(Integer.toString(i));

            clientPublishTask.setPublishCallback(handleMqttPublish(done));
            clientPublishTask.setConnectionCallback(handleMqttConnection(done));

            // add client to the queue
            queue.execute(clientPublishTask);
        }

        try {
            done.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Callback<Integer> handleMqttPublish(final CountDownLatch done) {
        return new Callback<Integer>() {
            @Override
            public void onSuccess(Integer messageSize) {
                result.messagesSent.incrementAndGet();
                result.size.addAndGet((long) messageSize);
                done.countDown();
            }

            @Override
            public void onFailure(Throwable value) {
                result.errorMessages.incrementAndGet();
                done.countDown();
            }
        };
    }

    private Callback<Void> handleMqttConnection(final CountDownLatch done) {
        return new Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
            }

            @Override
            public void onFailure(Throwable value) {
                result.errorConnections.incrementAndGet();
                for (int i = 0; i < Publishers.this.configuration.getMessageCount(); i++) {
                    done.countDown(); // discount all client messages if any failure
                }
            }
        };
    }

    private void handleInterruption(final List<ClientPublishTask> clients) {
        // Handle a Ctrl-C event cleanly.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Publishers.this.output.shutdown();

                final CountDownLatch clientClosed = new CountDownLatch(clients.size());
                for (ClientPublishTask client : clients) {
                    client.interrupt(new Callback<Void>() {
                        @Override
                        public void onSuccess(Void value) {
                            clientClosed.countDown();
                            CommandLindInterface.debug(Publishers.this.configuration, "Connection to broker successfully closed");
                        }

                        @Override
                        public void onFailure(Throwable value) {
                            clientClosed.countDown();
                            CommandLindInterface.stderr("Connection close to broker failure!");
                        }
                    });
                }

                try {
                    clientClosed.await(5000, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                result.endTimeNanosec = System.nanoTime();
                output.display(configuration, result);
            }
        });
    }

}
