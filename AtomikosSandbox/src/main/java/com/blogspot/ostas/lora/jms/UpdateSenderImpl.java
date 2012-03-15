package com.blogspot.ostas.lora.jms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

@Repository("updateSender")
@Profile("prod")
public class UpdateSenderImpl implements IUpdateSender{
    @Autowired
    private JmsTemplate jmsTemplate;
    @Override
    @Transactional(readOnly = false,propagation = Propagation.REQUIRED,isolation = Isolation.DEFAULT)
    public void send(final String messageText) {
        jmsTemplate.send(new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage tm = session.createTextMessage();
                tm.setText(messageText);
                return tm;
            }
        });
    }
}
