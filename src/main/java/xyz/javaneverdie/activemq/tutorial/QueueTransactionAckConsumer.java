package xyz.javaneverdie.activemq.tutorial;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class QueueTransactionAckConsumer {

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String QUEUE_NAME = "queue-transacted";
    private static final Boolean TRANSACTED = true;
    private static final long TIMEOUT = 3000;

    public static void main(String[] args) {
        String url = BROKER_URL;
        if (args.length > 0) {
            url = args[0].trim();
        }
        System.out.println("\nWaiting to receive messages... will timeout after " + TIMEOUT / 1000 + "s");

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("admin", "password", url);
        Connection connection = null;
        Session session = null;

        try {
            connection = connectionFactory.createConnection();
            connection.start();
            // 创建带事务的 session 对象
            session = connection.createSession(TRANSACTED, Session.CLIENT_ACKNOWLEDGE);
            Destination destination = session.createQueue(QUEUE_NAME);
            MessageConsumer consumer = session.createConsumer(destination);

            int i = 0;
            while (true) {
                Message message = consumer.receive(TIMEOUT);
                if (message != null) {
                    if (message instanceof TextMessage) {
                        String text = ((TextMessage) message).getText();
                        System.out.println("Got " + i++ + ". message: " + text);
                        // 由于已经开启事务，事务的优先级高于 Ack，因此 acknowledge 执不执行不重要了
                        //message.acknowledge();
                    }
                } else {
                    break;
                }
            }
            consumer.close();
            // 带事务的 session 必须执行 commit 才能真正的消费并删除消息，否则消息会一直在，导致重复消费消息
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
