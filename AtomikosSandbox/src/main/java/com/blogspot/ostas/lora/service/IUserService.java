package com.blogspot.ostas.lora.service;

import com.blogspot.ostas.lora.model.User;

import java.io.Serializable;

public interface IUserService {
    Serializable saveAndNotify(User user);
}
