package xyz.javaneverdie.activemq;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class QueueListenerConsumer {

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String QUEUE_NAME = "queue-quickstart";
    private static final Boolean NON_TRANSACTED = false;
    private static final long TIMEOUT = 3000;

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
        //5 创建消息的消费者
        MessageConsumer consumer = session.createConsumer(destination);
        // 通过监听方式来消费消息
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                try {
                    if (null != message && message instanceof TextMessage) {
                        TextMessage txtMsg = (TextMessage) message;
                        String msg = txtMsg.getText();
                        System.out.println("Consumer:->Received: " + msg);
                    } else {
                        System.out.println("Consumer:->Received: " + message);
                    }
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        });
        // 保证控制台退出，防止太快关闭，而无法接收到消息
        System.in.read();
        consumer.close();
        session.close();
        connection.close();
    }
}
