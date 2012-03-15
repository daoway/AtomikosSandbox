package com.blogspot.ostas.lora.context;

import com.atomikos.icatch.config.UserTransactionService;
import com.atomikos.icatch.config.UserTransactionServiceImp;
import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.atomikos.jms.AtomikosConnectionFactoryBean;
import com.blogspot.ostas.lora.model.User;
import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.jms.XAConnectionFactory;
import javax.sql.XADataSource;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = "com.blogspot.ostas.lora", excludeFilters = {@ComponentScan.Filter(Configuration.class)})
public class MyConfigH2Node {
    @Bean(name = "userTransactionServiceProperties")
    public Properties userTransactionServiceProperties() {
        final Properties properties = new Properties();
        properties.setProperty("com.atomikos.icatch.service", "com.atomikos.icatch.standalone.UserTransactionServiceFactory");
        return properties;
    }

    @Bean(name = "userTransactionService", initMethod = "init", destroyMethod = "shutdownForce")
    public UserTransactionService userTransactionService() {
        return new UserTransactionServiceImp(userTransactionServiceProperties());
    }

    @Bean(name = "atomikosTransactionManager", initMethod = "init", destroyMethod = "close")
    @DependsOn("userTransactionService")
    public UserTransactionManager atomikosTransactionManager() {
        UserTransactionManager userTransactionManager = new UserTransactionManager();
        userTransactionManager.setForceShutdown(false);
        return userTransactionManager;
    }

    @Bean(name = "atomikosUserTransaction")
    @DependsOn("userTransactionService")
    public UserTransaction atomikosUserTransaction() throws SystemException {
        UserTransaction userTransaction = new UserTransactionImp();
        userTransaction.setTransactionTimeout(300);
        return userTransaction;
    }

    @Bean(name = "jtaTransactionManager")
    @DependsOn("userTransactionService")
    public JtaTransactionManager jtaTransactionManager() throws SystemException {
        JtaTransactionManager jtaTransactionManager = new JtaTransactionManager();
        jtaTransactionManager.setTransactionManager(atomikosTransactionManager());
        jtaTransactionManager.setUserTransaction(atomikosUserTransaction());
        return jtaTransactionManager;
    }

    @Bean(name="xaDatabseDataSource")
    public XADataSource xaDatabseDataSource() throws SQLException {
        XADataSource xaDataSource = new JdbcDataSource();
        ((JdbcDataSource)xaDataSource).setURL("jdbc:h2:mem:testXA;DB_CLOSE_DELAY=-1");
        ((JdbcDataSource)xaDataSource).setUser("sa");
        ((JdbcDataSource)xaDataSource).setPassword("");
        return xaDataSource;
    }
    @Bean(name="dataSource",initMethod = "init",destroyMethod = "close")
    public AtomikosDataSourceBean dataSource() throws SQLException {
        AtomikosDataSourceBean atomikosDataSourceBean = new AtomikosDataSourceBean();
        atomikosDataSourceBean.setXaDataSource(xaDatabseDataSource());
        atomikosDataSourceBean.setUniqueResourceName("atomikosDbXA");
        return atomikosDataSourceBean;
    }

    @Bean(name="hibernateProperties")
    public Properties hibernateProperties(){
        final Properties properties = new Properties();
        properties.setProperty("hibernate.dialect","org.hibernate.dialect.HSQLDialect");
        properties.setProperty("hibernate.hbm2ddl.auto","create");
        properties.setProperty("hibernate.transaction.factory_class","com.atomikos.icatch.jta.hibernate3.AtomikosJTATransactionFactory");
        properties.setProperty("hibernate.transaction.manager_lookup_class","com.atomikos.icatch.jta.hibernate3.TransactionManagerLookup");
        return properties;
    }

    @Bean(name="sessionFactory") @DependsOn("hibernateProperties")
    public AnnotationSessionFactoryBean sessionFactory() throws SQLException {
        AnnotationSessionFactoryBean sessionFactoryBean = new AnnotationSessionFactoryBean();
        sessionFactoryBean.setAnnotatedClasses(new Class[]{User.class});
        sessionFactoryBean.setDataSource(dataSource());
        sessionFactoryBean.setHibernateProperties(hibernateProperties());
        return sessionFactoryBean;
    }

    @Bean
    public HibernateTemplate hibernateTemplate() throws SQLException {
        return new HibernateTemplate(sessionFactory().getObject());
    }
    @Bean
    public XAConnectionFactory jmsXaConnectionFactory(){
        final ActiveMQXAConnectionFactory activeMQXAConnectionFactory =
                new ActiveMQXAConnectionFactory("vm://localhost?marshal=false&broker.persistent=false&broker.useJmx=false");
        activeMQXAConnectionFactory.setUseAsyncSend(true);
        return activeMQXAConnectionFactory;
    }

    @Bean(name="amqConnectionFactory",initMethod = "init")
    public ConnectionFactory amqConnectionFactory(){
        final AtomikosConnectionFactoryBean atomikosConnectionFactoryBean = new AtomikosConnectionFactoryBean();
        atomikosConnectionFactoryBean.setUniqueResourceName("atomikosMQ");
        atomikosConnectionFactoryBean.setXaConnectionFactory(jmsXaConnectionFactory());
        atomikosConnectionFactoryBean.setPoolSize(5);
        return atomikosConnectionFactoryBean;
    }
    @Bean
    public Queue queue(){
        return new ActiveMQQueue("updatesQueue");
    }
    @Bean(name="jmsTemplate")
    public JmsTemplate jmsTemplate(){
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(amqConnectionFactory());
        jmsTemplate.setReceiveTimeout(2000);
        jmsTemplate.setDefaultDestination(queue());
        jmsTemplate.setSessionTransacted(true);
        return jmsTemplate;
    }
}
