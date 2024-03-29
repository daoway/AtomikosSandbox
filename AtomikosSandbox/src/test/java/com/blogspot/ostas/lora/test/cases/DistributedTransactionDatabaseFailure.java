package com.blogspot.ostas.lora.test.cases;

import com.blogspot.ostas.lora.model.User;
import com.blogspot.ostas.lora.test.base.XAResourceFailBaseTC;
import com.blogspot.ostas.lora.test.utils.VerificationUtil;
import org.apache.log4j.Logger;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Message;
import javax.transaction.SystemException;
import java.util.List;

public class DistributedTransactionDatabaseFailure extends XAResourceFailBaseTC {
    private static final Logger LOGGER = Logger.getLogger(DistributedTransactionDatabaseFailure.class);
    @Test(expected = Exception.class)
    @Transactional
    @BMRule(name="Database goes down",
            isInterface = true,
            targetClass = "com.blogspot.ostas.lora.database.IUserDao",
            targetMethod = "save",
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
        final List<User> userList = VerificationUtil.getObjectsFromDatabase();
        LOGGER.info("User_list : "+userList);
        //in case of database failure no data in database!
        //and should be no messages too
        Assert.assertEquals(userList.size(), 0);
    }
    @Override
    public void afterTransactionJmsStateChecks(){
        Message message = VerificationUtil.getMessageFromQueue();
        //since database xa resource had been failed,
        // we shouldn't have any message in queue
        Assert.assertNull(message);
    }
}
