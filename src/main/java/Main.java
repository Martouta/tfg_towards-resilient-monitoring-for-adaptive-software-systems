public class Main {
    public static void main(String[] args) throws InterruptedException {
        TwitterMonitorSimulator.simulate();
        KafkaService kafkaService = new KafkaService("localhost", "twitter");
        kafkaService.readMessages();
    }
}
