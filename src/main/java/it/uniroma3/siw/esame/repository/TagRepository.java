package it.uniroma3.siw.esame.repository;

import it.uniroma3.siw.esame.model.Project;
import it.uniroma3.siw.esame.model.Tag;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TagRepository extends CrudRepository<Tag, Long> {
	List<Tag> findByProjectOrderByCreationTimestamp(Project project);
}
