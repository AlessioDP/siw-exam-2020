package it.uniroma3.siw.esame.service;

import it.uniroma3.siw.esame.model.Project;
import it.uniroma3.siw.esame.model.Tag;
import it.uniroma3.siw.esame.repository.ProjectRepository;
import it.uniroma3.siw.esame.repository.TagRepository;
import it.uniroma3.siw.esame.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TagService {
	
	@Autowired
	protected ProjectRepository projectRepository;
	@Autowired
	protected TagRepository tagRepository;
	@Autowired
	protected TaskRepository taskRepository;
	
	@Transactional
	public Tag getTag(long id) {
		Optional<Tag> result = this.tagRepository.findById(id);
		return result.orElse(null);
	}
	
	@Transactional
	public Tag saveTag(Tag tag) {
		return this.tagRepository.save(tag);
	}
	
	@Transactional
	public void deleteTag(Tag tag) {
		this.tagRepository.delete(tag);
		tag.getProject().removeTag(tag);
		Project project = projectRepository.save(tag.getProject());
		project.getTasks().forEach(t -> {
			if (t.getTags().contains(tag)) {
				t.removeTag(tag);
				taskRepository.save(t);
			}
		});
	}
	
	@Transactional
	public List<Tag> getAllTags() {
		List<Tag> result = new ArrayList<>();
		Iterable<Tag> iterable = this.tagRepository.findAll();
		for(Tag tag : iterable)
			result.add(tag);
		return result;
	}
	
	@Transactional
	public long countAll() {
		return tagRepository.count();
	}
}

