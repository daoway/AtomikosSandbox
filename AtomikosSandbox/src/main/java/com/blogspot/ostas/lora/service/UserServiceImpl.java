package com.blogspot.ostas.lora.service;

import com.blogspot.ostas.lora.database.IUserDao;
import com.blogspot.ostas.lora.jms.IUpdateSender;
import com.blogspot.ostas.lora.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.Serializable;

@Service("userService")
public class UserServiceImpl implements IUserService{
    @Autowired
    private IUserDao userDao;
    @Autowired
    private IUpdateSender updateSender;

    @Override
    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.DEFAULT,readOnly = false)
    public Serializable saveAndNotify(User user) {
        if(!TransactionSynchronizationManager.isActualTransactionActive()){
            throw new RuntimeException("ooo sheet");
        }
        Serializable id = userDao.save(user);
        //if(1==1) throw new RuntimeException("it's fail baby");
        updateSender.send("Updated : "+id);
        return id;
    }
}
