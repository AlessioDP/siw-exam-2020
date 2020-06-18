package it.uniroma3.siw.esame.service;

import it.uniroma3.siw.esame.model.Comment;
import it.uniroma3.siw.esame.repository.CommentRepository;
import it.uniroma3.siw.esame.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentService {
	
	@Autowired
	protected CommentRepository commentRepository;
	@Autowired
	protected TaskRepository taskRepository;
	
	@Transactional
	public Comment getComment(long id) {
		return this.commentRepository.findById(id).orElse(null);
	}
	
	@Transactional
	public Comment saveComment(Comment comment) {
		return this.commentRepository.save(comment);
	}
	
	
	@Transactional
	public void deleteComment(Comment comment) {
		comment.getTask().removeComment(comment);
		taskRepository.save(comment.getTask());
		this.commentRepository.delete(comment);
	}
	
	@Transactional
	public List<Comment> getAllComments() {
		List<Comment> result = new ArrayList<>();
		Iterable<Comment> iterable = this.commentRepository.findAll();
		for(Comment comment : iterable)
			result.add(comment);
		return result;
	}
	
	@Transactional
	public long countAll() {
		return commentRepository.count();
	}
}

