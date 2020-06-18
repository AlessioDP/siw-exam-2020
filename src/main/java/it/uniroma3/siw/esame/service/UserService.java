package it.uniroma3.siw.esame.service;

import it.uniroma3.siw.esame.model.User;
import it.uniroma3.siw.esame.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
	
	@Autowired
	protected UserRepository userRepository;
	
	@Transactional
	public User getUser(long id) {
		Optional<User> result = this.userRepository.findById(id);
		return result.orElse(null);
	}
	
	@Transactional
	public User saveUser(User user) throws DataIntegrityViolationException {
		return this.userRepository.save(user);
	}
	
	@Transactional
	public List<User> getAllUsers() {
		List<User> result = new ArrayList<>();
		Iterable<User> iterable = this.userRepository.findAll();
		for(User user : iterable)
			result.add(user);
		return result;
	}
}

