package com.blogspot.ostas.lora.database;

import com.blogspot.ostas.lora.model.User;

import java.io.Serializable;

public interface IUserDao {
    Serializable save(User user);
    User getById(Serializable id);
}
