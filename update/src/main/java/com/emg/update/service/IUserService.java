package com.emg.update.service;

import java.util.List;

import com.emg.update.dto.User;

public interface IUserService {
public User getUserById(int userId);    
    
    public void insertUser(User user);    
    
    public void addUser(User user);    
    
    public List<User> getAllUser();    
}
