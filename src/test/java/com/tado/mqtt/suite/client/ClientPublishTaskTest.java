package com.tado.mqtt.suite.client;

import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.MQTT;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.net.SocketException;
import java.net.URISyntaxException;

/**
 * Created by kuceram on 22/05/14.
 */
@RunWith(JUnit4.class)
public class ClientPublishTaskTest {

    @Test
    public void testExecute() {
//        MQTT mqtt = new MQTT();
//        try {
//            mqtt.setHost("tcp://localhost:1883");
//        } catch (URISyntaxException e) {
//            System.out.print("Wrong host url");
//        }
//
//        ClientPublishTask clientPublishTask = new ClientPublishTask(mqtt);
//        clientPublishTask.setTopic(new UTF8Buffer("test-topic"));
//        clientPublishTask.setBody(new UTF8Buffer("test-msg"));
//
//        clientPublishTask.run();
    }
}
