import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import java.util.Properties;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import java.sql.Timestamp;

import org.json.*;

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
                String message = new String(it.next().message());
                IReceived receiver = new Received();
                receiver.doOperation(getObtainedDataFromMessage(message));
            }
        }
    }

    private ObtainedData getObtainedDataFromMessage(String message) {
      // TODO: right now it works only for Twitter messages. Make it work for all.
      JSONObject jsonDataObject  = new JSONObject(message); // json with all data
      JSONObject twitterData = jsonDataObject.getJSONObject("SocialNetworksMonitoredData"); // get SocialNetworksMonitoredData object (twitterCase)
      int configId = twitterData.getInt("confId");
      int numDataItems = twitterData.getInt("numDataItems");
      int idOutput = twitterData.getInt("idOutput");
      Timestamp searchTimeStamp = Timestamp.valueOf( twitterData.getString("searchTimeStamp") );
      ObtainedData od = new ObtainedData(configId, numDataItems, idOutput, searchTimeStamp);
      JSONArray dataItemsArray = twitterData.getJSONArray("DataItems");
      for (int i = 0; i < numDataItems; i++) {
        JSONObject jsonDataItem = (JSONObject) dataItemsArray.get(0);
        Timestamp timestamp = Timestamp.valueOf( jsonDataItem.getString("timeStamp") );
        String author = jsonDataItem.getString("author");
        String link = jsonDataItem.getString("link");
        Long id = jsonDataItem.getLong("id");
        String twitterMessage = jsonDataItem.getString("message");
        DataItem dataItem = new DataItemTwitter(timestamp, author, link, id, twitterMessage);
        od.addDataItem(dataItem);
      }
      return od;
      // (int configId, int numDataItems, ArrayList<DataItem> dataItems, int idOutput, Timestamp searchTimeStamp)
      //DataItem = new DataItemTwitter(); //Timestamp timeStamp, String author, String link, Long id, String message
      //JSONObject dataItem = (JSONObject) data.getJSONArray("DataItems").get(0);
      //String id = dataItem.getString("id"); // get the name from data.
    }
}


/*
{"SocialNetworksMonitoredData":
{  "confId": 1,  "numDataItems": 1,  "DataItems":
  [    { "timeStamp": "2017-11-12 17:33:01.364", "author": "@K6BsiXfLZj", "link": "https://twitter.com/K6BsiXfLZj/status/89422480079779389",
        "id": "89422480079779389", "message": "xlk9C3aZnr6yFdyzIwObOJMEcyA3RH4kR8jkHpSPWuoq0kFz7rhbWV1" }
  ],
"idOutput": 46,  "searchTimeStamp": "2017-11-12 17:33:01.364"}}
*/
