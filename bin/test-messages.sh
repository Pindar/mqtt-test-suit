#!/bin/bash

# Test which creates certain amount of connections to the broker each connection (client) sends more messages

ip=11.22.33.44
port=1883
jar=../target/mqtt-test-suite-0.3-SNAPSHOT-jar-with-dependencies.jar
topic="test/topic"
message="Test message"
msgCount=(
    1 10 50 100 200 500 1000 2000 5000 10000 20000 30000 40000 50000
    1 10 50 100 200 500 1000 2000 5000 10000 20000 30000 40000 50000
    1 10 50 100 200 500 1000 2000 5000 10000 20000 30000 40000 50000
)
clientCount=500

control_c()
# run if user hits control-c
{
  echo "\n*** Exiting ***\n"
  exit $?
}

# trap keyboard interrupt (control-c)
trap control_c SIGINT

for i in ${msgCount[@]}
do
        java -Xmx1g -cp ${jar} "com.tado.mqtt.suite.cli.Publishers" \
            -h "tcp://$ip:$port" -t "$topic" -m "$message" --msg-count ${i} -pc --csv \
            --client-count ${clientCount}
done
