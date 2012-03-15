package com.blogspot.ostas.lora.test.base;

import com.blogspot.ostas.lora.context.MyConfigH2Node;
import com.blogspot.ostas.lora.model.User;
import com.blogspot.ostas.lora.service.IUserService;
import com.blogspot.ostas.lora.test.DistributedTransactionTestCase;
import com.blogspot.ostas.lora.test.utils.VerificationUtil;
import org.apache.log4j.Logger;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.sql.DataSource;
import javax.transaction.SystemException;
import java.sql.SQLException;
import java.util.List;

@RunWith(BMUnitRunner.class)
@Transactional
@ActiveProfiles("prod")
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {MyConfigH2Node.class})
@TransactionConfiguration(transactionManager = "jtaTransactionManager", defaultRollback = false)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class XAResourceFailBaseTC {
    private static final Logger LOGGER = Logger.getLogger(DistributedTransactionTestCase.class);
    @Autowired
    protected IUserService userService;
    @Autowired
    protected JtaTransactionManager jtaTransactionManager;
    @Autowired
    protected DataSource dataSource;

    private TestContextManager testContextManager;
    protected User user;
    @Before
    public void setUpContext() throws Exception {
        user = new User();
        user.setUserId(0l);
        user.setName("me");
        user.setPasswd("dont_tell_ya");

        this.testContextManager = new TestContextManager(getClass());
        this.testContextManager.prepareTestInstance(this);

        VerificationUtil.clearDatabase();
        VerificationUtil.deleteAllMessagesFromQueue();
    }
    @After
    public void afterTransactionParanoidTest() throws JMSException, SystemException, SQLException {
        afterTransactionDatabaseStateChecks();
        afterTransactionJmsStateChecks();
    }

    protected void afterTransactionDatabaseStateChecks() throws SQLException, SystemException {
        List<User> userList = VerificationUtil.getObjectsFromDatabase();
        LOGGER.debug("List of users after transaction : "+userList);
        Assert.assertEquals(1, userList.size());
        Assert.assertEquals(userList.get(0).getUserId(),user.getUserId());
    }

    protected void afterTransactionJmsStateChecks() throws JMSException {
        Message message = VerificationUtil.getMessageFromQueue();
        Assert.assertNotNull(message);
        LOGGER.info("Message in normal environment : " + message.toString());
    }
}
