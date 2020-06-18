package it.uniroma3.siw.esame.repository;

import it.uniroma3.siw.esame.model.Credentials;
import it.uniroma3.siw.esame.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CredentialsRepository extends CrudRepository<Credentials, Long> {
	
	Optional<Credentials> findByUserNameIgnoreCase(String userName);
	
	Optional<Credentials> findByUser(User user);
}
