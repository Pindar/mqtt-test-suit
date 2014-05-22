## Overview
This project is intended to be a suitable test tool for testing MQTT brokers. It uses
[fusesource mqtt-client library](https://github.com/fusesource/mqtt-client) and its command line interface for
MQTT clients - publishers and subscribers. This project adds the ability to run huge amount of simultaneously
connected clients publishing messages to a broker. This simulates the real load on the broker.

## Usage
Download [executable jar file](TODO) and run the clients from command line.

### Publisher
Publisher gives the ability to publish *one* message to the broker. It allows to specify different MQTT variables like
retain message, QoS, etc. (see the help running the command without any parameters)

Example:
    java -cp mqtt-test-suite-0.1-SNAPSHOT-jar-with-dependencies.jar com.tado.mqtt.suite.cli.Publisher -h "tcp://localhost:61613" -t "test/topic" -m "Test message"

### Concurrent Publishers
Concurrent publishers allow to publish messages by multiple clients. It has the same options as publisher (see above)
and it adds few more additional:
* `--client-count` : Sets the amount of clients who are going to publish.
* `--msg-count` : Sets the amount of messages each client is going to send.
* `--client-sleep` : the number of milliseconds to sleep between publish operations (defaut: 0).

Example:
    java -cp mqtt-test-suite-0.1-SNAPSHOT-jar-with-dependencies.jar com.tado.mqtt.suite.cli.Publishers -h "tcp://localhost:61613" -t "test/topic" -m "Test message" --client-count 5000 --msg-count 1 -pc --will-topic "dead/topic" --will-payload "I am dead, sorry"

### Subscriber
The subscriber is capable to subscribe for certain topic.

Example:
    java -cp mqtt-test-suite-0.1-SNAPSHOT-jar-with-dependencies.jar com.tado.mqtt.suite.cli.Subscriber -h "tcp://localhost:61613" -t "test/topic"

## Enhancements
In order to be able to create huge amount of simultaneously connected clients on one machine, you need to adjust
os variables which restrict the maximum opened socket connections per process. Also, we need to give more memory
to the java process which is going to maintain all these clients.

### Increase Maximal Opened Connections
Each system has its own specific ways how to increase number of opened files. You can read
[an interesting article](http://krypted.com/mac-os-x/maximum-files-in-mac-os-x/) how to set this
number in OS X, however it could be a good starting point for all unix systems.

### Increase Heap Space
To increase the memory used by the java process we need to set the heap space size:
    java -Xmx1024m -cp mqtt-tes...
