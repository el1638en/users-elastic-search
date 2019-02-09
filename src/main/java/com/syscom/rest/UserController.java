package com.syscom.rest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.syscom.beans.User;
import com.syscom.service.UserESService;

/**
 * API utilisateurs
 *
 */
@RestController
@RequestMapping(UserController.PATH)
public class UserController {

	public static final String PATH = "/api/user";

	private final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserESService userESService;

	/**
	 * creer un nouvel utilisateur
	 *
	 * @param user {@link User}
	 */
	@PostMapping
	public void createUser(@RequestBody User user) {
		logger.info("Creation de l'utilisateur : {}", user);
		userESService.create(user);
	}

	/**
	 * Recherche par login
	 * @param login : login de l'utilisateur
	 * @return user
	 */
	@GetMapping(value = "/{login}")
	public User findByLogin(@PathVariable String login) {
		logger.info("Recherche d'utilisateur à partir du login : : {}", login);
		return userESService.findOne(login);
	}
	
	/**
	 * Supprimer un utilisateur
	 * 
	 * @param login de l'utilisateur
	 */
	@DeleteMapping(value = "/{login}")
	public void deleteByLogin(@PathVariable String login) {
		userESService.delete(login);
	}

	/**
	 * Liste de tous les utilisateurs.
	 * 
	 * @return liste des utilisateurs.
	 */
	@GetMapping(value = "/all")
	public List<User> getAll() {
		return StreamSupport.stream(userESService.findAll().spliterator(), false).collect(Collectors.toList());
	}

}