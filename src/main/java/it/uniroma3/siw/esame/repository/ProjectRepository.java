package it.uniroma3.siw.esame.repository;

import it.uniroma3.siw.esame.model.Project;
import it.uniroma3.siw.esame.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProjectRepository extends CrudRepository<Project, Long> {
	
	List<Project> findByMembers(User member);
	
	List<Project> findByOwner(User owner);
}

