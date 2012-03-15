package com.blogspot.ostas.lora.test.cases;

import com.blogspot.ostas.lora.test.base.XAResourceFailBaseTC;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.transaction.SystemException;

public class DistributedTransactionWithoutFailure extends XAResourceFailBaseTC {
    private static final Logger LOGGER = Logger.getLogger(DistributedTransactionWithoutFailure.class);
    @Test
    @Transactional
    public void save() throws SystemException {
        userService.saveAndNotify(user);
        LOGGER.info("Transaction status : " +jtaTransactionManager.getTransactionManager().getStatus());
    }
}
