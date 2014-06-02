package com.tado.mqtt.suite.util;

import com.tado.mqtt.suite.cli.Publishers;
import com.tado.mqtt.suite.values.Configuration;
import com.tado.mqtt.suite.view.CommandLindInterface;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedList;

import static com.tado.mqtt.suite.values.Configuration.Builder;

/**
 * Created by simon on 30/05/14.
 */
public class ArgumentParser {

    private String[] args;
    private Publishers main;

    public ArgumentParser(String[] args, Publishers main) {
        this.args = args;
        this.main = main;
    }

    private static String shift(LinkedList<String> args) {
        if (args.isEmpty()) {
            CommandLindInterface.stderr("Invalid usage: Missing argument");
            CommandLindInterface.displayHelpAndExit(1);
        }
        return args.removeFirst();
    }

    public Configuration invoke() throws URISyntaxException, IOException {
        Builder configBuilder = new Builder();
        MQTT mqtt = new MQTT();

        // Process the arguments
        LinkedList<String> argl = new LinkedList<String>(Arrays.asList(args));
        while (!argl.isEmpty()) {
            String arg = argl.removeFirst();
            try {
                if ("--help".equals(arg)) {
                    CommandLindInterface.displayHelpAndExit(0);
                } else if ("-v".equals(arg)) {
                    mqtt.setVersion(shift(argl));
                } else if ("-h".equals(arg)) {
                    mqtt.setHost(shift(argl));
                } else if ("-k".equals(arg)) {
                    mqtt.setKeepAlive(Short.parseShort(shift(argl)));
                } else if ("-c".equals(arg)) {
                    mqtt.setCleanSession(false);
                } else if ("-i".equals(arg)) {
                    mqtt.setClientId(shift(argl));
                } else if ("-u".equals(arg)) {
                    mqtt.setUserName(shift(argl));
                } else if ("-p".equals(arg)) {
                    mqtt.setPassword(shift(argl));
                } else if ("--will-topic".equals(arg)) {
                    mqtt.setWillTopic(shift(argl));
                } else if ("--will-payload".equals(arg)) {
                    mqtt.setWillMessage(shift(argl));
                } else if ("--will-qos".equals(arg)) {
                    int v = Integer.parseInt(shift(argl));
                    if (v > QoS.values().length) {
                        CommandLindInterface.stderr("Invalid qos value : " + v);
                        CommandLindInterface.displayHelpAndExit(1);
                    }
                    mqtt.setWillQos(QoS.values()[v]);
                } else if ("--will-retain".equals(arg)) {
                    mqtt.setWillRetain(true);
                } else if ("-d".equals(arg)) {
                    configBuilder.debug(true);
                } else if ("--client-count".equals(arg)) {
                    configBuilder.clientCount(Integer.parseInt(shift(argl)));
                } else if ("--msg-count".equals(arg)) {
                    configBuilder.messageCount(Integer.parseInt(shift(argl)));
                } else if ("--client-sleep".equals(arg)) {
                    configBuilder.sleep(Long.parseLong(shift(argl)));
                } else if ("-q".equals(arg)) {
                    int v = Integer.parseInt(shift(argl));
                    if (v > QoS.values().length) {
                        CommandLindInterface.stderr("Invalid qos value : " + v);
                        CommandLindInterface.displayHelpAndExit(1);
                    }
                    configBuilder.qos(QoS.values()[v]);
                } else if ("-r".equals(arg)) {
                    configBuilder.retain(true);
                } else if ("-t".equals(arg)) {
                    configBuilder.topic(new UTF8Buffer(shift(argl)));
                } else if ("-m".equals(arg)) {
                    configBuilder.body(new UTF8Buffer(shift(argl) + "\n"));
                } else if ("-z".equals(arg)) {
                    configBuilder.body(new UTF8Buffer(""));
                } else if ("-f".equals(arg)) {
                    File file = new File(shift(argl));
                    RandomAccessFile raf = new RandomAccessFile(file, "r");
                    try {
                        byte data[] = new byte[(int) raf.length()];
                        raf.seek(0);
                        raf.readFully(data);
                        configBuilder.body(new Buffer(data));
                    } finally {
                        raf.close();
                    }
                } else if ("-pc".equals(arg)) {
                    configBuilder.prefixCounter(true);
                } else if ("--csv".equals(arg)) {
                    configBuilder.csv(true);
                } else {
                    CommandLindInterface.stderr("Invalid usage: unknown option: " + arg);
                    CommandLindInterface.displayHelpAndExit(1);
                }
            } catch (NumberFormatException e) {
                CommandLindInterface.stderr("Invalid usage: argument " + arg + " not a number");
                CommandLindInterface.displayHelpAndExit(1);
            }
        }

        return configBuilder.build();
    }

}
