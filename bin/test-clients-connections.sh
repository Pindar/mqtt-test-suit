#!/bin/bash

# Test which creates many connections to the broker and each connection (client) sents small amount of messages

ip=11.22.33.44
port=1883
jar=../target/mqtt-test-suite-0.3-SNAPSHOT-jar-with-dependencies.jar
topic="test/topic"
message="Test message"
msgCount=10
clientCount=(
    1 10 50 100 200 500 1000 2000 5000 10000 20000 30000 40000 50000
    1 10 50 100 200 500 1000 2000 5000 10000 20000 30000 40000 50000
    1 10 50 100 200 500 1000 2000 5000 10000 20000 30000 40000 50000
)

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

for i in ${clientCount[@]}
do
        java -Xmx1g -cp ${jar} "com.tado.mqtt.suite.cli.Publishers" \
            -h "tcp://$ip:$port" -t "$topic" -m "$message" --msg-count ${msgCount} -pc --csv \
            --client-count ${i}
done

echo "\n*** Test client connections finished ***\n"