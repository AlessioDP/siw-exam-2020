package it.uniroma3.siw.esame.repository;

import it.uniroma3.siw.esame.model.Project;
import it.uniroma3.siw.esame.model.Task;
import it.uniroma3.siw.esame.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TaskRepository extends CrudRepository<Task, Long> {
	List<Task> findByProjectOrderByCreationTimestamp(Project project);
	List<Task> findByUser(User user);
}
