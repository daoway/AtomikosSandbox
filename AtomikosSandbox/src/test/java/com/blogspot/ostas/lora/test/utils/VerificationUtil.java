package com.blogspot.ostas.lora.test.utils;

import com.blogspot.ostas.lora.model.User;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.*;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class VerificationUtil {
    public static List<User> getObjectsFromDatabase(DataSource dataSource) {
        JdbcTemplate template = new JdbcTemplate(dataSource);
        return template.query("select * from myUsers", new ParameterizedRowMapper<User>() {
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

    public static void clearDatabase(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("delete from myusers");
    }
    public static Message getMessageFromQueue(){
        final ActiveMQConnectionFactory activeMQConnectionFactory =
                new ActiveMQConnectionFactory("vm://localhost?marshal=false&broker.persistent=false&broker.useJmx=false");
        Queue queue = new ActiveMQQueue("updatesQueue");
        JmsTemplate jmsTemplate = new JmsTemplate(activeMQConnectionFactory);
        jmsTemplate.setDefaultDestination(queue);
        //jmsTemplate.setReceiveTimeout(JmsTemplate.RECEIVE_TIMEOUT_NO_WAIT);
        jmsTemplate.setReceiveTimeout(1000);
        Message message = jmsTemplate.receive();
        return message;
    }
    public static void deleteAllMessagesFromQueue() throws Exception {
        final ActiveMQConnectionFactory activeMQConnectionFactory =
                new ActiveMQConnectionFactory("vm://localhost?marshal=false&broker.persistent=false&broker.useJmx=false");
        Queue queue = new ActiveMQQueue("updatesQueue");
        JmsTemplate jmsTemplate = new JmsTemplate(activeMQConnectionFactory);
        jmsTemplate.setDefaultDestination(queue);
        jmsTemplate.setReceiveTimeout(JmsTemplate.RECEIVE_TIMEOUT_NO_WAIT);
        Message message;
        do {
            message = jmsTemplate.receive();
        }
        while (message != null);
    }
}
