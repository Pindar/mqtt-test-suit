package com.tado.mqtt.suite.cli;

import com.tado.mqtt.suite.client.PublishClients;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.QoS;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by kuceram on 19/05/14.
 */
public class Publishers {

    private final PublishClients publishClients = new PublishClients();

    private static void displayHelpAndExit(int exitCode) {
        stdout("");
        stdout("This is a simple mqtt client that will publish to a topic.");
        stdout("");
        stdout("Arguments: [-h host] [-k keepalive] [-c] [-i id] [-u username [-p password]]");
        stdout("           [--will-topic topic [--will-payload payload] [--will-qos qos] [--will-retain]]");
        stdout("           [--client-count count] [--msg-count count] [--client-sleep sleep]");
        stdout("           [-d] [-q qos] [-r] -t topic ( -pc | -m message | -z | -f file )");
        stdout("");
        stdout("");
        stdout(" -h : mqtt host uri to connect to. Defaults to tcp://localhost:1883.");
        stdout(" -k : keep alive in seconds for this client. Defaults to 60.");
        stdout(" -c : disable 'clean session'.");
        stdout(" -i : id to use for this client. Defaults to a random id.");
        stdout(" -u : provide a username (requires MQTT 3.1 broker)");
        stdout(" -p : provide a password (requires MQTT 3.1 broker)");
        stdout(" --will-topic : the topic on which to publish the client Will.");
        stdout(" --will-payload : payload for the client Will, which is sent by the broker in case of");
        stdout("                  unexpected disconnection. If not given and will-topic is set, a zero");
        stdout("                  length message will be sent.");
        stdout(" --will-qos : QoS level for the client Will.");
        stdout(" --will-retain : if given, make the client Will retained.");
        stdout(" -d : display debug info on stderr");
        stdout(" -q : quality of service level to use for the publish. Defaults to 0.");
        stdout(" -r : message should be retained.");
        stdout(" -t : mqtt topic to publish to.");
        stdout(" -m : message payload to send.");
        stdout(" -z : send a null (zero length) message.");
        stdout(" -f : send the contents of a file as the message.");
        stdout(" -pc : prefix a message counter to the message together with client number");
        stdout(" -v : MQTT version to use 3.1 or 3.1.1. (default: 3.1)");
        stdout(" --client-count : the number of simultaneously connected publishClients");
        stdout(" --msg-count : the number of messages to publish per client");
        stdout(" --client-sleep : the number of milliseconds to sleep between publish operations (defaut: 0)");
        stdout("");
        System.exit(exitCode);
    }

    private static void stdout(Object x) {
        System.out.println(x);
    }
    private static void stderr(Object x) {
        System.err.println(x);
    }

    private static String shift(LinkedList<String> argl) {
        if(argl.isEmpty()) {
            stderr("Invalid usage: Missing argument");
            displayHelpAndExit(1);
        }
        return argl.removeFirst();
    }

    public static void main(String[] args) throws Exception {
        Publishers main = new Publishers();

        // Process the arguments
        LinkedList<String> argl = new LinkedList<String>(Arrays.asList(args));
        while (!argl.isEmpty()) {
            try {
                String arg = argl.removeFirst();
                if ("--help".equals(arg)) {
                    displayHelpAndExit(0);
                } else if ("-v".equals(arg)) {
                    main.publishClients.getMqtt().setVersion(shift(argl));
                } else if ("-h".equals(arg)) {
                    main.publishClients.getMqtt().setHost(shift(argl));
                } else if ("-k".equals(arg)) {
                    main.publishClients.getMqtt().setKeepAlive(Short.parseShort(shift(argl)));
                } else if ("-c".equals(arg)) {
                    main.publishClients.getMqtt().setCleanSession(false);
                } else if ("-i".equals(arg)) {
                    main.publishClients.getMqtt().setClientId(shift(argl));
                } else if ("-u".equals(arg)) {
                    main.publishClients.getMqtt().setUserName(shift(argl));
                } else if ("-p".equals(arg)) {
                    main.publishClients.getMqtt().setPassword(shift(argl));
                } else if ("--will-topic".equals(arg)) {
                    main.publishClients.getMqtt().setWillTopic(shift(argl));
                } else if ("--will-payload".equals(arg)) {
                    main.publishClients.getMqtt().setWillMessage(shift(argl));
                } else if ("--will-qos".equals(arg)) {
                    int v = Integer.parseInt(shift(argl));
                    if( v > QoS.values().length ) {
                        stderr("Invalid qos value : " + v);
                        displayHelpAndExit(1);
                    }
                    main.publishClients.getMqtt().setWillQos(QoS.values()[v]);
                } else if ("--will-retain".equals(arg)) {
                    main.publishClients.getMqtt().setWillRetain(true);
                } else if ("-d".equals(arg)) {
                    main.publishClients.setDebug(true);
                } else if ("--client-count".equals(arg)) {
                    main.publishClients.setClientCount(Integer.parseInt(shift(argl)));
                } else if ("--msg-count".equals(arg)) {
                    main.publishClients.setMessageCountPerClient(Long.parseLong(shift(argl)));
                } else if ("--client-sleep".equals(arg)) {
                    main.publishClients.setSleep(Long.parseLong(shift(argl)));
                } else if ("-q".equals(arg)) {
                    int v = Integer.parseInt(shift(argl));
                    if( v > QoS.values().length ) {
                        stderr("Invalid qos value : " + v);
                        displayHelpAndExit(1);
                    }
                    main.publishClients.setQos(QoS.values()[v]);
                } else if ("-r".equals(arg)) {
                    main.publishClients.setRetain(true);
                } else if ("-t".equals(arg)) {
                    main.publishClients.setTopic(new UTF8Buffer(shift(argl)));
                } else if ("-m".equals(arg)) {
                    main.publishClients.setBody(new UTF8Buffer(shift(argl)+"\n"));
                } else if ("-z".equals(arg)) {
                    main.publishClients.setBody(new UTF8Buffer(""));
                } else if ("-f".equals(arg)) {
                    File file = new File(shift(argl));
                    RandomAccessFile raf = new RandomAccessFile(file, "r");
                    try {
                        byte data[] = new byte[(int) raf.length()];
                        raf.seek(0);
                        raf.readFully(data);
                        main.publishClients.setBody(new Buffer(data));
                    } finally {
                        raf.close();
                    }
                } else if ("-pc".equals(arg)) {
                    main.publishClients.setPrefixCounter(true);
                } else {
                    stderr("Invalid usage: unknown option: " + arg);
                    displayHelpAndExit(1);
                }
            } catch (NumberFormatException e) {
                stderr("Invalid usage: argument not a number");
                displayHelpAndExit(1);
            }
        }

        if (main.publishClients.getTopic() == null) {
            stderr("Invalid usage: no topic specified.");
            displayHelpAndExit(1);
        }
        if (main.publishClients.getBody() == null) {
            stderr("Invalid usage: -z -m or -f must be specified.");
            displayHelpAndExit(1);
        }

        main.execute();
        System.exit(0);
    }

    private void execute() {
        // Handle a Ctrl-C event cleanly.
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                stdout("");
                stdout("MQTT publishClients shutdown...");
                publishClients.interrupt();
            }
        });

        // execute client calls
        publishClients.execute();
    }
}
