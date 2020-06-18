package it.uniroma3.siw.esame.service;

import it.uniroma3.siw.esame.model.Credentials;
import it.uniroma3.siw.esame.model.User;
import it.uniroma3.siw.esame.repository.CredentialsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CredentialsService {
	
	@Autowired
	protected CredentialsRepository credentialsRepository;
	
	@Autowired
	protected PasswordEncoder passwordEncoder;
	
	@Transactional
	public Credentials getCredentials(long id) {
		Optional<Credentials> result = this.credentialsRepository.findById(id);
		return result.orElse(null);
	}
	
	@Transactional
	public Credentials getCredentials(String username) {
		Optional<Credentials> result = this.credentialsRepository.findByUserNameIgnoreCase(username);
		return result.orElse(null);
	}
	
	@Transactional
	public Credentials getCredentials(User user) {
		Optional<Credentials> result = this.credentialsRepository.findByUser(user);
		return result.orElse(null);
	}
	
	@Transactional
	public Credentials saveCredentials(Credentials credentials) {
		if (credentials.getRole() == null)
			credentials.setRole(Credentials.DEFAULT_ROLE);
		credentials.setPassword(this.passwordEncoder.encode(credentials.getPassword()));
		return this.credentialsRepository.save(credentials);
	}
	
	@Transactional
	public List<Credentials> getAllCredentials() {
		List<Credentials> result = new ArrayList<>();
		Iterable<Credentials> iterable = this.credentialsRepository.findAll();
		for(Credentials credentials : iterable)
			result.add(credentials);
		return result;
	}
	
	@Transactional
	public long countAll() {
		return credentialsRepository.count();
	}
}

