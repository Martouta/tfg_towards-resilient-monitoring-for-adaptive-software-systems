import java.lang.reflect.InvocationTargetException;

public class Main {
    public static void main(String[] args) throws InterruptedException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        TwitterMonitorSimulator.simulate();
        KafkaService kafkaService = new KafkaService("localhost", "twitter");
        kafkaService.readMessages(DataItemTwitter.class);
    }
}


// TODO: be able to use either localhost or the real twitter one
// private final static String DEFAULT_KAFKA_URL = "localhost";
// String kafkaUrl = DEFAULT_KAFKA_URL; //(args.length > 0) ? args[0] : DEFAULT_KAFKA_URL;
