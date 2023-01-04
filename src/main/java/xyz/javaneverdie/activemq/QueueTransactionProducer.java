package xyz.javaneverdie.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class QueueTransactionProducer {

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final Boolean TRANSACTED = true;
    private static final String QUEUE_NAME = "queue-transacted";
    private static final int NUM_MESSAGES_TO_SEND = 3;
    private static final long DELAY = 100;

    public static void main(String[] args) {
        String url = BROKER_URL;
        if (args.length > 0) {
            url = args[0].trim();
        }
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("admin", "password", url);
        Connection connection = null;
        Session session = null;
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            // 创建带事务的 session 对象
            session = connection.createSession(TRANSACTED, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(QUEUE_NAME);
            MessageProducer producer = session.createProducer(destination);
            for (int i = 0; i < NUM_MESSAGES_TO_SEND; i++) {
                TextMessage message = session.createTextMessage("Message #" + i);
                System.out.println("Sending message #" + i);
                producer.send(message);
                Thread.sleep(DELAY);
            }
            producer.close();
            // 带事务的 session 必须执行 commit 才能真正地生产消息
            session.commit();
        } catch (Exception e) {
            System.out.println("Caught exception! Try to rollback session!");
            try {
                // 遇到异常，带事务的 session 执行 rollback
                session.rollback();
            } catch (JMSException jmse) {
                System.out.println("Could not rollback session...");
            }
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException e) {
                    System.out.println("Could not close an open session...");
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    System.out.println("Could not close an open connection...");
                }
            }
        }
    }
}
