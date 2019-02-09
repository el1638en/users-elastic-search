package com.syscom.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.syscom.beans.User;

public interface UserESService {
	
	User create(User user);

    User findOne(String login);

    Iterable<User> findAll();

    Page<User> findByUserName(String name, Pageable pageable);

    Page<User> findByUserNameUsingCustomQuery(String name, Pageable pageable);

    long count();

    void delete(String login);
}
