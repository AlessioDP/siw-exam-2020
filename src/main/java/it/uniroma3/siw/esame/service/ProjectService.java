package it.uniroma3.siw.esame.service;

import it.uniroma3.siw.esame.model.Project;
import it.uniroma3.siw.esame.model.User;
import it.uniroma3.siw.esame.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {
	
	@Autowired
	protected ProjectRepository projectRepository;
	
	@Transactional
	public Project getProject(long id) {
		Optional<Project> result = this.projectRepository.findById(id);
		return result.orElse(null);
	}
	
	@Transactional
	public Project saveProject(Project project) {
		return this.projectRepository.save(project);
	}
	
	@Transactional
	public void deleteProject(Project project) {
		this.projectRepository.delete(project);
	}
	
	@Transactional
	public Project shareProjectWithUser(Project project, User user) {
		project.addMember(user);
		return this.projectRepository.save(project);
	}
	
	@Transactional
	public Project unshareProjectWithUser(Project project, User user) {
		project.removeMember(user);
		// Remove the member from any task
		project.getTasks().forEach(t -> {
			if (t.getUser().equals(user)) {
				t.setUser(null);
			}
		});
		return this.projectRepository.save(project);
	}
	
	@Transactional
	public List<Project> getAllProjects() {
		List<Project> result = new ArrayList<>();
		Iterable<Project> iterable = this.projectRepository.findAll();
		for(Project project : iterable)
			result.add(project);
		return result;
	}
	
	@Transactional
	public long countAll() {
		return projectRepository.count();
	}
}

