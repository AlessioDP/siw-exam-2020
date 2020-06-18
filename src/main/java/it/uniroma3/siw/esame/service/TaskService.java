package it.uniroma3.siw.esame.service;

import it.uniroma3.siw.esame.model.Task;
import it.uniroma3.siw.esame.repository.ProjectRepository;
import it.uniroma3.siw.esame.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
	
	@Autowired
	protected ProjectRepository projectRepository;
	@Autowired
	protected TaskRepository taskRepository;
	
	@Transactional
	public Task getTask(long id) {
		Optional<Task> result = this.taskRepository.findById(id);
		return result.orElse(null);
	}
	
	@Transactional
	public Task saveTask(Task task) {
		return this.taskRepository.save(task);
	}
	
	@Transactional
	public void deleteTask(Task task) {
		task.getProject().removeTask(task);
		projectRepository.save(task.getProject());
		this.taskRepository.delete(task);
	}
	
	@Transactional
	public List<Task> getAllTasks() {
		List<Task> result = new ArrayList<>();
		Iterable<Task> iterable = this.taskRepository.findAll();
		for(Task task : iterable)
			result.add(task);
		return result;
	}
	
	@Transactional
	public long countAll() {
		return taskRepository.count();
	}
}

