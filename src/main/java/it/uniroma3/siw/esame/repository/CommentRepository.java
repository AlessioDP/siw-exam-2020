package it.uniroma3.siw.esame.repository;

import it.uniroma3.siw.esame.model.Comment;
import it.uniroma3.siw.esame.model.Task;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CommentRepository extends CrudRepository<Comment, Long> {
	List<Comment> findByTaskOrderByCreationTimestampAsc(Task task);
}
