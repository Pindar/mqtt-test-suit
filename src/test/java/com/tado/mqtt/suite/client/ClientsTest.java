package com.tado.mqtt.suite.client;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.net.SocketException;
import java.net.URISyntaxException;

/**
 * Created by kuceram on 21/05/14.
 */
@RunWith(JUnit4.class)
public class ClientsTest {

    @Test
    public void testExecute() {
        // set clients
//        PublishClients clients = new PublishClients();
//        try {
//            clients.getMqtt().setHost("tcp://localhost:61613");
//        } catch (URISyntaxException e) {
//            System.out.print("Wrong host url");
//        }
//
//        // execute queries
//        try {
//            clients.execute();
//        } catch(SocketException e) {
//            e.printStackTrace();
//            System.err.println("Too many clients, increase the system limit for open files.");
//        }
    }
}
