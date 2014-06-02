package com.tado.mqtt.suite.view;

import com.tado.mqtt.suite.values.Configuration;
import com.tado.mqtt.suite.values.Result;
import org.apache.commons.io.FileUtils;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

/**
 * Created by simon on 30/05/14.
 */
public class CommandLindInterface implements Notification {

    public static void debug(Configuration config, Object x) {
        if (!config.isDebug())
            return;
        stdout(x);
    }

    public static void stdout(Object x) {
        System.out.println(x);
    }

    public static void stderr(Object x) {
        System.err.println(x);
    }

    public static void displayHelpAndExit(int exitCode) {
        displayHelp();
        System.exit(exitCode);
    }

    private static void displayHelp() {
        stdout("");
        stdout("This is a simple mqtt client that will publish to a topic.");
        stdout("");
        stdout("Arguments: [-h host] [-k keepalive] [-c] [--csv] [-i id] [-u username [-p password]]");
        stdout("           [--will-topic topic [--will-payload payload] [--will-qos qos] [--will-retain]]");
        stdout("           [--client-count count] [--msg-count count] [--client-sleep sleep]");
        stdout("           [-d] [-q qos] [-r] -t topic ( -pc | -m message | -z | -f file )");
        stdout("");
        stdout("");
        stdout(" -h : mqtt host uri to connect to. Defaults to tcp://localhost:1883.");
        stdout(" -k : keep alive in seconds for this client. Defaults to 60.");
        stdout(" -c : disable 'clean session'.");
        stdout(" --csv : print results in csv row format.");
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
        stdout(" --client-sleep : the number of milliseconds to sleep after publish operation (defaut: 0)");
        stdout("");
    }

    @Override
    public void display(Configuration configuration, Result result) {

        Period executionTime = result.calculateExecutionTime();
        CommandLindInterface.stdout("");
        CommandLindInterface.stdout("------------------------------------------");
        CommandLindInterface.stdout("Statistic of Publishers");
        CommandLindInterface.stdout("------------------------------------------");
        CommandLindInterface.stdout("Messages successfully sent: " + result.messagesSent.toString());
        CommandLindInterface.stdout("Total time elapsed: " + PeriodFormat.getDefault().print(executionTime));
        if (executionTime.toStandardSeconds().getSeconds() > 0)
            CommandLindInterface.stdout("Message rate: " + (result.messagesSent.get() / executionTime.toStandardSeconds().getSeconds()) + " msg/sec");
        else
            CommandLindInterface.stdout("Message rate: " + result.messagesSent.get() + " msg/sec");
        CommandLindInterface.stdout("------------------------------------------");
        CommandLindInterface.stdout("Clients could not connect (failure): " + result.errorConnections.toString());
        CommandLindInterface.stdout("Messages could not publish (failure): " + result.errorMessages.toString());
        CommandLindInterface.stdout("------------------------------------------");
        CommandLindInterface.stdout("Total data sent: " + FileUtils.byteCountToDisplaySize(result.size.get()));
        CommandLindInterface.stdout("------------------------------------------");
        CommandLindInterface.stdout("");

    }

    @Override
    public void shutdown() {
        CommandLindInterface.stdout("");
        CommandLindInterface.stdout("MQTT publishClients shutdown...");
    }
}
