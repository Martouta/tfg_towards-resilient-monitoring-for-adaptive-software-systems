# tfg_towards-resilient-monitoring-for-adaptive-software-systems

## Requirements

### Kafka Server running (only if the kafka server is localhost)
Go to the Kafka folder.

Stop & Run Zookeeper:
```sh
sudo service zookeeper stop
bin/zookeeper-server-start.sh config/zookeeper.properties
```

Run Kafka server:
```sh
bin/kafka-server-start.sh config/server.properties
```

## Build & Run the project

Clean and compile:
```sh
gradle clean && gradle build
```

Run:
```sh
java -jar build/libs/tfg_monitor.jar
```

If you want to see the output through console, go to the kafka folder and do as the following example (assuming the kafka url is localhost):
```sh
bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic twitter --from-beginning
```
