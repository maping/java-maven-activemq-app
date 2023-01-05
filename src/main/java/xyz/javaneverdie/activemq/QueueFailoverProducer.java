package xyz.javaneverdie.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class QueueFailoverProducer {

    private static final String BROKER_URL = "failover:(tcp://10.10.71.1.4:61616,tcp://10.71.1.5:61616,tcp://10.71.1.6:61616";
    private static final String QUEUE_NAME = "queue-failover";
    private static final Boolean NON_TRANSACTED = false;
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
        //6 发送消息到Queue
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
