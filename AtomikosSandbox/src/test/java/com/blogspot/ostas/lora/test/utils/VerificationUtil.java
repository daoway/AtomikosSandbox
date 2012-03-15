package com.blogspot.ostas.lora.test.utils;

import com.blogspot.ostas.lora.model.User;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.log4j.Logger;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.*;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class VerificationUtil {
    private static final Logger LOGGER = Logger.getLogger(VerificationUtil.class);
    private static ConnectionFactory connectionFactory;
    private static Queue queue;
    private static JmsTemplate jmsTemplate;
    private static JdbcTemplate jdbcTemplate;
    static {
        connectionFactory =new ActiveMQConnectionFactory("vm://localhost?marshal=false&broker.persistent=false&broker.useJmx=false");
        queue = new ActiveMQQueue("updatesQueue");
        DataSource dataSource = new JdbcDataSource();
        ((JdbcDataSource)dataSource).setURL("jdbc:h2:mem:testXA;DB_CLOSE_DELAY=-1");
        ((JdbcDataSource)dataSource).setUser("sa");
        ((JdbcDataSource)dataSource).setPassword("");

        jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setDefaultDestination(queue);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }
    public static List<User> getObjectsFromDatabase() {
        return jdbcTemplate.query("select * from myUsers", new ParameterizedRowMapper<User>() {
            @Override
            public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                User user = new User();
                user.setUserId(rs.getLong(1));
                user.setName(rs.getString(2));
                user.setPasswd(rs.getString(3));
                return user;
            }
        });
    }

    public static void clearDatabase() {
        jdbcTemplate.execute("delete from myusers");
    }
    public static Message getMessageFromQueue(){
        jmsTemplate.setReceiveTimeout(1000);
        Message message = jmsTemplate.receive();
        return message;
    }
    public static void deleteAllMessagesFromQueue() throws Exception {
        jmsTemplate.setReceiveTimeout(JmsTemplate.RECEIVE_TIMEOUT_NO_WAIT);
        Message message;
        do {
            message = jmsTemplate.receive();
            LOGGER.debug("Taking message out of queue : "+message);
        }
        while (message != null);
    }
}
