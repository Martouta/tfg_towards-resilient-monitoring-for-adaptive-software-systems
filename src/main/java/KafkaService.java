import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KafkaService {
    private String kafkaUrl;
    private String kafkaTopic;

    public KafkaService(String url, String topic) {
        kafkaUrl = url;
        kafkaTopic = topic;
    }

    private Properties createConsumerProperties() {
        Properties properties = new Properties();
        properties.put("zookeeper.connect", kafkaUrl+":2181");
        properties.put("group.id", "twitterGroup");
        properties.put("zookeeper.session.timeout.ms", "500");
        properties.put("zookeeper.sync.time.ms", "250");
        properties.put("auto.commit.interval.ms", "1000");
        return properties;
    }

    private Properties createProducerProperties() {
        Properties properties = new Properties();
        properties.put("metadata.broker.list", kafkaUrl+":9092");
        properties.put("serializer.class", "kafka.serializer.StringEncoder");
        properties.put("request.required.acks", "1");
        return properties;
    }

    public void writeMessage(String msg) {
        Properties properties = createProducerProperties();
        Producer<Integer, String> producer = new Producer<>(new ProducerConfig(properties));
        KeyedMessage<Integer, String> data = new KeyedMessage<>(kafkaTopic, msg);
        producer.send(data);
        producer.close();
    }

    public void readMessages() {
        Properties properties = createConsumerProperties();
        ConsumerConnector consumer = Consumer.createJavaConsumerConnector(new ConsumerConfig(properties));
        testConsumer(consumer);
        if (consumer != null) { consumer.shutdown(); }
    }

    private void testConsumer(ConsumerConnector consumer) {
        Map<String, Integer> topicCount = new HashMap<>();
        topicCount.put(kafkaTopic, 1);

        Map<String, List<KafkaStream<byte[], byte[]>>> consumerStreams = consumer.createMessageStreams(topicCount);
        List<KafkaStream<byte[], byte[]>> streams = consumerStreams.get(kafkaTopic);
        for (final KafkaStream stream : streams) {
            ConsumerIterator<byte[], byte[]> it = stream.iterator();
            while (it.hasNext()) {
                System.out.println("Message from Single Topic: " + new String(it.next().message()));
            }
        }
    }
}
