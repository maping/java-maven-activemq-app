package xyz.javaneverdie.activemq.advanced;

import xyz.javaneverdie.activemq.tutorial.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import org.apache.activemq.RedeliveryPolicy;

public class QueueRedeliveryConsumer {

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String QUEUE_NAME = "queue-redelivery";
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
            // 修改默认的重试次数为 3 次
            RedeliveryPolicy queuePolicy = new RedeliveryPolicy();
            queuePolicy.setMaximumRedeliveries(3);
            connectionFactory.setRedeliveryPolicy(queuePolicy);
            connection = connectionFactory.createConnection();
            connection.start();
            // 创建带事务的 session 对象
            session = connection.createSession(TRANSACTED, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(QUEUE_NAME);
            MessageConsumer consumer = session.createConsumer(destination);

            int i = 0;
            while (true) {
                Message message = consumer.receive(TIMEOUT);
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
            // 带事务的 session 必须执行 commit 才能真正的消费并删除消息，否则消息会一直在，导致重复消费消息
            // 注释掉 session.commit()，模拟触发消息重试机制
            //session.commit();
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
