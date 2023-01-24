package xyz.javaneverdie.activemq.advanced;

import java.util.UUID;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import org.apache.activemq.ActiveMQMessageProducer;
import org.apache.activemq.AsyncCallback;

public class QueueAsyncProducer {

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String QUEUE_NAME = "queue-async";
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
        // 使用异步投递
        connectionFactory.setUseAsyncSend(true);
        //2 通过连接工厂，获得连接，并启动
        Connection connection = connectionFactory.createConnection();
        connection.start();
        //3 创建会话
        Session session = connection.createSession(NON_TRANSACTED, Session.AUTO_ACKNOWLEDGE);
        //4 创建目的地：Queue
        Destination destination = session.createQueue(QUEUE_NAME);
        //5 创建消息的生产者，这里必须用 ActiveMQMessageProducer
        ActiveMQMessageProducer producer = (ActiveMQMessageProducer) session.createProducer(destination);
        //6 发送消息到Queue
        for (int i = 0; i < NUM_MESSAGES_TO_SEND; i++) {
            TextMessage message = session.createTextMessage("Message #" + i);
            message.setJMSMessageID(UUID.randomUUID().toString() + "---order");
            String messageID = message.getJMSMessageID();
            System.out.println("Sending message #" + i);
            // 使用回调函数确认消息是否发送成功
            producer.send(message, new AsyncCallback() {
                @Override
                public void onSuccess() {
                    System.out.println(messageID + " has been sent successfully.");
                }

                @Override
                public void onException(JMSException exception) {
                    System.out.println(messageID + " sent failed.");
                }
            });

            Thread.sleep(DELAY);
        }
        producer.close();
        session.close();
        connection.close();
    }
}
