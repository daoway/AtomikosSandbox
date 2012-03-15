package com.blogspot.ostas.lora.test.cases;

import com.blogspot.ostas.lora.model.User;
import com.blogspot.ostas.lora.test.base.XAResourceFailBaseTC;
import com.blogspot.ostas.lora.test.utils.VerificationUtil;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.log4j.Logger;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Message;
import javax.jms.Queue;
import javax.transaction.SystemException;
import java.util.List;

public class DistributedTransactionJmsFailure extends XAResourceFailBaseTC {
    private static final Logger LOGGER = Logger.getLogger(DistributedTransactionJmsFailure.class);
    @Test(expected = Exception.class)
    @Transactional
    @BMRule(name="JMS goes down",
            isInterface = true,
            targetClass = "com.blogspot.ostas.lora.jms.IUpdateSender",
            targetMethod = "send",
            action = "org.apache.log4j.Logger.getLogger(getClass()).debug(\"DB failure simulation\");throw new java.lang.RuntimeException(\"Simulated Database Failure\")")
    public void save(){
        userService.saveAndNotify(user);
        try {
            LOGGER.info("Transaction status : " +jtaTransactionManager.getTransactionManager().getStatus());
        } catch (SystemException e) {
            LOGGER.error(e);
        }
    }
    @Override
    public void afterTransactionDatabaseStateChecks(){
        final List<User> userList = VerificationUtil.getObjectsFromDatabase(dataSource);
        LOGGER.info("User_list : "+userList);
        //in case of database failure no data in database!
        //and should be no messages too
        Assert.assertEquals(userList.size(), 0);
    }
    @Override
    public void afterTransactionJmsStateChecks(){
        Message message = VerificationUtil.getMessageFromQueue();
        Assert.assertNull(message);
    }
}
