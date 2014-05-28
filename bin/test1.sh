#!/bin/bash

ip=54.195.155.211
port=1883
jar=../target/mqtt-test-suite-0.1-SNAPSHOT-jar-with-dependencies.jar
topic="test/topic"
message="Test message"
msgCount=10
clientCount=(1 10 50 100 200 500 1000 2000 5000 10000)

#exited=false

control_c()
# run if user hits control-c
{
  echo "\n*** Exiting ***\n"
  exit $?
}

# trap keyboard interrupt (control-c)
trap control_c SIGINT

for i in ${clientCount[@]}
do
        java -Xmx1g -cp ${jar} "com.tado.mqtt.suite.cli.Publishers" \
            -h "tcp://$ip:$port" -t "$topic" -m "$message" --msg-count ${msgCount} -pc --csv \
            --client-count ${i}
done
