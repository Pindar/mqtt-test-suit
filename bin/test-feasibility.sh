#!/bin/bash

# Feasibility test which simulates the real situation: many clients (500) publishing messages (count 45)
# in 20 sec interval. Therefore, the minimal time of the test is 15 min.

ip=11.22.33.44
port=1883
jar=../target/mqtt-test-suite-0.3-SNAPSHOT-jar-with-dependencies.jar
topic="test/topic"
message="Test message"
msgCount=45
clientCount=500
sleep=20000

control_c()
# run if user hits control-c
{
  echo "\n*** Exiting ***\n"
  exit $?
}

# trap keyboard interrupt (control-c)
trap control_c SIGINT

warmMsg=100
warmClients=1000
echo "Warming up he broker, sent ${warmMsg} messages with ${warmClients} clients"
java -Xmx1g -cp ${jar} "com.tado.mqtt.suite.cli.Publishers" \
            -h "tcp://$ip:$port" -t "$topic" -m "$message" --msg-count ${warmMsg} -pc --csv \
            --client-count ${warmClients}

java -Xmx1g -cp ${jar} "com.tado.mqtt.suite.cli.Publishers" \
            -h "tcp://$ip:$port" -t "$topic" -m "$message" --msg-count ${msgCount} --client-sleep ${sleep} -pc --csv \
            --client-count ${clientCount}

echo "\n*** Feasibility test finished ***\n"
