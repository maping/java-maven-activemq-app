package xyz.javaneverdie.activemq.tutorial;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class QueueNonPersistentProducer {

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final Boolean NON_TRANSACTED = false;
    private static final String QUEUE_NAME = "queue-non-persistent";
    private static final int NUM_MESSAGES_TO_SEND = 3;
    private static final long DELAY = 100;

    public static void main(String[] args) throws Exception {
        String url = BROKER_URL;
        if (args.length > 0) {
            url = args[0].trim();
        }
        //1 创建连接工厂
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("admin", "password", url);
        //2 通过连接工厂，获得连接，并启动
        Connection connection = connectionFactory.createConnection();
        connection.start();
        //3 创建会话
        Session session = connection.createSession(NON_TRANSACTED, Session.AUTO_ACKNOWLEDGE);
        //4 创建目的地：Queue
        Destination destination = session.createQueue(QUEUE_NAME);
        //5 创建消息的生产者
        MessageProducer producer = session.createProducer(destination);
        // 默认队列生产者发送消息的交付模式为持久化，发送消息后，如果 ActiveMQ 宕机，消息依然存在
        // 以下设置队列生产者发送消息的交付模式为非持久化，发送消息后，如果 ActiveMQ 宕机，消息就丢失了
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        //6 发送消息到 Queue
        for (int i = 0; i < NUM_MESSAGES_TO_SEND; i++) {
            TextMessage message = session.createTextMessage("Message #" + i);     
            System.out.println("Sending message #" + i);
            producer.send(message);
            Thread.sleep(DELAY);
        }
        producer.close();
        session.close();
        connection.close();
    }
}
