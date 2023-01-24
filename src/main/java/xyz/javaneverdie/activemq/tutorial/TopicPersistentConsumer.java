package xyz.javaneverdie.activemq.tutorial;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class TopicPersistentConsumer {

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String TOPIC_NAME = "topic-persistent";
    private static final Boolean NON_TRANSACTED = false;
    private static final long TIMEOUT = 3000;

    public static void main(String[] args) throws Exception {
        String url = BROKER_URL;
        if (args.length > 0) {
            url = args[0].trim();
        }
        System.out.println("\nWaiting to receive messages... will timeout after " + TIMEOUT / 1000 + "s");

        //1 创建连接工厂
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("admin", "password", url);
        //2 通过连接工厂，获得连接，并启动
        Connection connection = connectionFactory.createConnection();
        // 设置 ClientID
        connection.setClientID("Forrest");
        connection.start();
        //3 创建会话
        Session session = connection.createSession(NON_TRANSACTED, Session.AUTO_ACKNOWLEDGE);
        //4 创建目的地：Topic
        Topic destination = session.createTopic(TOPIC_NAME);
        //5 创建消息的消费者
        MessageConsumer consumer = session.createConsumer(destination);
        // 创建 Topic 持久化订阅
        TopicSubscriber subscriber = session.createDurableSubscriber(destination, "remark...");
        
        int i = 0;
        while (true) {
            Message message = subscriber.receive(TIMEOUT);
            if (message != null) {
                if (message instanceof TextMessage) {
                    String text = ((TextMessage) message).getText();
                    System.out.println("Got " + i++ + ". message: " + text);
                }
            } else {
                break;
            }
        }
        consumer.close();
        session.close();
        connection.close();
    }
}
