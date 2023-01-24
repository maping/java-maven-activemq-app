package xyz.javaneverdie.activemq.advanced;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import org.apache.activemq.ScheduledMessage;

public class QueueCronProducer {

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String QUEUE_NAME = "queue-cron";
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

        // 每分钟都会发生消息被投递10次，延迟1秒开始，每次间隔1秒:
        String cron = "* * * * *"; // Cron 表达式: 分钟 小时 日期 月份 星期
        long delay = 1000; //延迟投递时间
        long period = 1000; //重复投递间隔时间
        int repeat = 10; //重复投递次数

        //6 发送消息到Queue
        for (int i = 0; i < NUM_MESSAGES_TO_SEND; i++) {
            TextMessage message = session.createTextMessage("Message #" + i);
            System.out.println("Sending message #" + i);

            // 通过消息属性来设置延迟投递
            message.setStringProperty(ScheduledMessage.AMQ_SCHEDULED_CRON, cron);
            message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, delay);
            message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_PERIOD, period);
            message.setIntProperty(ScheduledMessage.AMQ_SCHEDULED_REPEAT, repeat);

            producer.send(message);
            Thread.sleep(DELAY);
        }
        producer.close();
        session.close();
        connection.close();
    }
}
