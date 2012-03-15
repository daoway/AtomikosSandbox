package com.blogspot.ostas.lora.database;

import com.blogspot.ostas.lora.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

@Repository("userDaoImpl")
@Profile("prod")
public class UserDaoImpl implements IUserDao{
    @Autowired
    private HibernateTemplate hibernateTemplate;
    @Override
    @Transactional(readOnly = false,propagation = Propagation.REQUIRED,isolation = Isolation.DEFAULT)
    public Serializable save(User user){
        return hibernateTemplate.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getById(Serializable id) {
        return hibernateTemplate.get(User.class,id);
    }
}
