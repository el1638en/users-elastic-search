package com.syscom.service.impl;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.syscom.beans.User;
import com.syscom.dao.UserESRepository;
import com.syscom.service.UserESService;

@Service
public class UserESServiceImpl implements UserESService {

	private final Logger logger = LoggerFactory.getLogger(UserESServiceImpl.class);
	@Autowired
	private UserESRepository userESRepository;

	@Override
	public User create(User user) {
		logger.info("Création de l'utilisateur {}.", user);
		return userESRepository.save(user);
	}

	@Override
	public User findOne(String login) {
		logger.info("Recherche un utilisateur à partir du login : {}.", login);
		Optional<User> optionalUser = userESRepository.findById(login);
		if (optionalUser.isPresent()) {
			return optionalUser.get();
		}
		return null;
	}

	@Override
	public Iterable<User> findAll() {
		logger.info("Recherche de tous les utilisateurs");
		return userESRepository.findAll();
	}

	@Override
	public Page<User> findByUserName(String name, Pageable pageable) {
		logger.info("Recherche des utilisateurs ayant pour nom {}.", name);
		return userESRepository.findByName(name, pageable);
	}

	@Override
	public Page<User> findByUserNameUsingCustomQuery(String name, Pageable pageable) {
		logger.info("Recherche des utilisateurs ayant pour nom {}.", name);
		return userESRepository.findByNameUsingCustomQuery(name, pageable);
	}

	@Override
	public long count() {
		logger.info("Nombre total d'utilisateurs.");
		return userESRepository.count();
	}

	@Override
	public void delete(String login) {
		logger.info("Suppression de l'utilisateur ayant pour login : {}.", login);
		User user = findOne(login);
		if (user != null) {
			userESRepository.delete(user);
		}
	}

}
