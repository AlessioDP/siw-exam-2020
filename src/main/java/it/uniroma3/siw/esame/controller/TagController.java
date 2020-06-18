package it.uniroma3.siw.esame.controller;

import it.uniroma3.siw.esame.controller.session.SessionData;
import it.uniroma3.siw.esame.controller.validation.CredentialsValidator;
import it.uniroma3.siw.esame.controller.validation.ProjectValidator;
import it.uniroma3.siw.esame.controller.validation.TagValidator;
import it.uniroma3.siw.esame.controller.validation.TaskValidator;
import it.uniroma3.siw.esame.model.Credentials;
import it.uniroma3.siw.esame.model.Project;
import it.uniroma3.siw.esame.model.Tag;
import it.uniroma3.siw.esame.model.Task;
import it.uniroma3.siw.esame.repository.TagRepository;
import it.uniroma3.siw.esame.repository.UserRepository;
import it.uniroma3.siw.esame.service.CredentialsService;
import it.uniroma3.siw.esame.service.ProjectService;
import it.uniroma3.siw.esame.service.TagService;
import it.uniroma3.siw.esame.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;

import static it.uniroma3.siw.esame.model.Credentials.ADMIN_ROLE;

@Controller
public class TagController {
	
	@Autowired
	TagRepository tagRepository;
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	CredentialsService credentialsService;
	@Autowired
	ProjectService projectService;
	@Autowired
	TagService tagService;
	@Autowired
	TaskService taskService;
	
	@Autowired
	CredentialsValidator credentialsValidator;
	@Autowired
	ProjectValidator projectValidator;
	@Autowired
	TagValidator tagValidator;
	@Autowired
	TaskValidator taskValidator;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	@Autowired
	SessionData sessionData;
	
	
	@RequestMapping(value = { "/project/{id}/tag/create" }, method = RequestMethod.GET)
	public String showCreateTagPrompt(Model model, @PathVariable Long id) {
		Credentials visitor = sessionData.getLoggedCredentials();
		
		Project project = projectService.getProject(id);
		if (project != null) {
			if (visitor.getRole().equals(ADMIN_ROLE) || visitor.getUser().getId().equals(project.getOwner().getId())) {
				// Only admins/owners can edit project
				model.addAttribute("project", project);
				model.addAttribute("tag", new Task());
				model.addAttribute("tagForm", new Tag());
				
				model.addAttribute("editing", false);
				return "project/tagEdit";
			}
		}
		return "error/403";
	}
	
	@RequestMapping(value = { "/project/{id}/tag/create" }, method = RequestMethod.POST)
	public String createTag(Model model,
							 @PathVariable Long id,
							 @Valid @ModelAttribute("tagForm") Tag tag,
							 BindingResult tagBindingResult) {
		Credentials visitor = sessionData.getLoggedCredentials();
		
		Project project = projectService.getProject(id);
		if (project != null) {
			if (visitor.getRole().equals(ADMIN_ROLE) || visitor.getUser().getId().equals(project.getOwner().getId())) {
				// Only admins/owners can edit project
				this.tagValidator.validate(tag, tagBindingResult);
				
				if (!tagBindingResult.hasErrors()) {
					tag.setProject(project);
					project.addTag(tag);
					projectService.saveProject(project);
					
					return "redirect:/project/" + project.getId();
				}
				model.addAttribute("project", project);
				model.addAttribute("tag", tag);
				model.addAttribute("tagForm", tag);
				model.addAttribute("editing", false);
				return "project/tagEdit";
			}
		}
		return "error/403";
	}
	
	@RequestMapping(value = { "/project/{id}/tag/{tagId}/delete" }, method = RequestMethod.GET)
	public String deleteTag(Model model, @PathVariable Long id, @PathVariable Long tagId) {
		Credentials visitor = sessionData.getLoggedCredentials();
		
		Project project = projectService.getProject(id);
		Tag tag = tagService.getTag(tagId);
		if (project != null && tag != null) {
			if (visitor.getRole().equals(ADMIN_ROLE) || visitor.getUser().getId().equals(project.getOwner().getId())) {
				// Delete the tag
				tagService.deleteTag(tag);
				return "redirect:/project/" + project.getId();
			}
		}
		return "error/403";
	}
	
	@RequestMapping(value = { "/project/{id}/tag/{tagId}/edit" }, method = RequestMethod.GET)
	public String showEditTagPrompt(Model model, @PathVariable Long id, @PathVariable Long tagId) {
		Credentials visitor = sessionData.getLoggedCredentials();
		
		Project project = projectService.getProject(id);
		Tag tag = tagService.getTag(tagId);
		if (project != null && tag != null) {
			if (visitor.getRole().equals(ADMIN_ROLE) || visitor.getUser().getId().equals(project.getOwner().getId())) {
				model.addAttribute("project", project);
				model.addAttribute("tag", tag);
				model.addAttribute("tagForm", tag);
				model.addAttribute("editing", true);
			}
			
			return "project/tagEdit";
		}
		return "error/403";
	}
	
	@RequestMapping(value = { "/project/{id}/tag/{tagId}/edit" }, method = RequestMethod.POST)
	public String editTag(Model model,
						   @PathVariable Long id,
						   @PathVariable Long tagId,
						   @Valid @ModelAttribute("tagForm") Tag tag,
						   BindingResult tagBindingResult) {
		Credentials visitor = sessionData.getLoggedCredentials();
		
		Project project = projectService.getProject(id);
		Tag selectedTag = tagService.getTag(tagId);
		if (project != null && selectedTag != null) {
			if (visitor.getRole().equals(ADMIN_ROLE) || visitor.getUser().getId().equals(project.getOwner().getId())) {
				this.tagValidator.validate(tag, tagBindingResult);
				
				if (!tagBindingResult.hasErrors()) {
					selectedTag.setName(tag.getName());
					selectedTag.setDescription(tag.getDescription());
					selectedTag.setColor(tag.getColor());
					tagService.saveTag(selectedTag);
					
					return "redirect:/project/" + project.getId();
				}
				model.addAttribute("project", project);
				model.addAttribute("tag", selectedTag);
				model.addAttribute("editing", true);
				return "project/tagEdit";
			}
		}
		return "error/403";
	}
}
