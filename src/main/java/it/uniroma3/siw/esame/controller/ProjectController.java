package it.uniroma3.siw.esame.controller;

import it.uniroma3.siw.esame.controller.session.SessionData;
import it.uniroma3.siw.esame.controller.validation.CredentialsValidator;
import it.uniroma3.siw.esame.controller.validation.ProjectValidator;
import it.uniroma3.siw.esame.model.Credentials;
import it.uniroma3.siw.esame.model.Project;
import it.uniroma3.siw.esame.repository.CredentialsRepository;
import it.uniroma3.siw.esame.repository.ProjectRepository;
import it.uniroma3.siw.esame.repository.TagRepository;
import it.uniroma3.siw.esame.repository.TaskRepository;
import it.uniroma3.siw.esame.repository.UserRepository;
import it.uniroma3.siw.esame.service.CredentialsService;
import it.uniroma3.siw.esame.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

import java.util.ArrayList;
import java.util.List;

import static it.uniroma3.siw.esame.model.Credentials.ADMIN_ROLE;

@Controller
public class ProjectController {
	
	@Autowired
	CredentialsRepository credentialsRepository;
	@Autowired
	ProjectRepository projectRepository;
	@Autowired
	TagRepository tagRepository;
	@Autowired
	TaskRepository taskRepository;
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	CredentialsService credentialsService;
	@Autowired
	ProjectService projectService;
	
	
	@Autowired
	CredentialsValidator credentialsValidator;
	@Autowired
	ProjectValidator projectValidator;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	@Autowired
	SessionData sessionData;
	
	@RequestMapping(value = { "/project/{id}" }, method = RequestMethod.GET)
	public String showProject(Model model, @PathVariable Long id) {
		Credentials visitor = sessionData.getLoggedCredentials();
		model.addAttribute("visitor", visitor);
		
		Project project = projectService.getProject(id);
		model.addAttribute("project", project);
		if (project != null) {
			// If the project exists
			model.addAttribute("owner", credentialsRepository.findByUser(project.getOwner()).orElse(null));
			model.addAttribute("tasks", taskRepository.findByProjectOrderByCreationTimestamp(project));
			model.addAttribute("tags", tagRepository.findByProjectOrderByCreationTimestamp(project));
			
			List<Credentials> members = new ArrayList<>();
			project.getMembers().forEach(u -> credentialsRepository.findByUser(u).ifPresent(members::add));
			model.addAttribute("members", members);
			
			model.addAttribute("credentialsRepository", credentialsRepository);
		}
		
		return "project/project";
	}
	
	@RequestMapping(value = { "/project/create" }, method = RequestMethod.GET)
	public String showCreateProjectPrompt(Model model) {
		Credentials visitor = sessionData.getLoggedCredentials();
		model.addAttribute("visitor", visitor);
		
		model.addAttribute("projectForm", new Project());
		model.addAttribute("editing", false);
		return "project/projectEdit";
	}
	
	@RequestMapping(value = { "/project/create" }, method = RequestMethod.POST)
	public String createProject(@Valid @ModelAttribute("projectForm") Project project,
								BindingResult projectBindingResult,
								Model model) {
		Credentials visitor = sessionData.getLoggedCredentials();
		model.addAttribute("visitor", visitor);
		
		this.projectValidator.validate(project, projectBindingResult);
		
		if(!projectBindingResult.hasErrors()) {
			project.setOwner(visitor.getUser());
			Project savedProject = projectService.saveProject(project);
			
			return "redirect:/project/" + savedProject.getId();
		}
		return "project/projectEdit";
	}
	
	@RequestMapping(value = { "/project/{id}/delete" }, method = RequestMethod.GET)
	public String deleteProject(Model model, @PathVariable Long id) {
		Credentials visitor = sessionData.getLoggedCredentials();
		
		Project project = projectService.getProject(id);
		if (project != null) {
			if (visitor.getRole().equals(ADMIN_ROLE)
					|| visitor.getUser().getId().equals(project.getOwner().getId())) {
				// Delete the project
				projectService.deleteProject(project);
			}
		}
		return "redirect:/";
	}
	
	@RequestMapping(value = { "/project/{id}/edit" }, method = RequestMethod.GET)
	public String showEditProjectPrompt(Model model, @PathVariable Long id) {
		Credentials visitor = sessionData.getLoggedCredentials();
		model.addAttribute("visitor", visitor);
		
		Project project = projectService.getProject(id);
		if (project != null) {
			if (visitor.getRole().equals(ADMIN_ROLE) || visitor.getUser().getId().equals(project.getOwner().getId())) {
				// Only admins/owners can edit project
				model.addAttribute("projectForm", project);
				model.addAttribute("editing", true);
				
				return "project/projectEdit";
			}
		}
		return "error/403";
	}
	
	@RequestMapping(value = { "/project/{id}/edit" }, method = RequestMethod.POST)
	public String editProject(@Valid @ModelAttribute("projectForm") Project projectForm,
							  BindingResult projectBindingResult,
							  Model model,
							  @PathVariable Long id) {
		this.projectValidator.validate(projectForm, projectBindingResult);
		
		Project project = projectService.getProject(id);
		Credentials visitor = sessionData.getLoggedCredentials();
		if (project != null) {
			if (!projectBindingResult.hasErrors()) {
				if (visitor.getRole().equals(ADMIN_ROLE) || project.getOwner().getId().equals(visitor.getUser().getId())) {
					// Only admins/owners can edit project
					project.setName(projectForm.getName());
					project.setDescription(projectForm.getDescription());
					projectService.saveProject(project);
				}
				return "redirect:/project/" + id;
			}
			model.addAttribute("visitor", visitor);
			return "project/projectEdit";
		}
		return "redirect:/project/" + id;
	}
	
	@RequestMapping(value = { "/project/{id}/share" }, method = RequestMethod.GET)
	public String showShareProjectPrompt(Model model, @PathVariable Long id) {
		Credentials visitor = sessionData.getLoggedCredentials();
		model.addAttribute("visitor", visitor);
		model.addAttribute("error", null);
		model.addAttribute("result", null);
		
		Project project = projectService.getProject(id);
		if (project != null) {
			if (visitor.getRole().equals(ADMIN_ROLE) || visitor.getUser().getId().equals(project.getOwner().getId())) {
				// Only admins/owners can edit project
				model.addAttribute("project", project);
				
				List<Credentials> members = new ArrayList<>();
				project.getMembers().forEach(u -> credentialsRepository.findByUser(u).ifPresent(members::add));
				model.addAttribute("members", members);
				
				return "project/projectShare";
			}
		}
		return "error/403";
	}
	
	@RequestMapping(value = { "/project/{id}/share/add" }, method = RequestMethod.POST)
	public String shareProjectAdd(Model model, @PathVariable Long id, @RequestParam("username") String memberUsername) {
		Credentials visitor = sessionData.getLoggedCredentials();
		model.addAttribute("visitor", visitor);
		
		Project project = projectService.getProject(id);
		if (project != null) {
			if (visitor.getRole().equals(ADMIN_ROLE) || visitor.getUser().getId().equals(project.getOwner().getId())) {
				model.addAttribute("project", project);
				
				Credentials memberToAdd = credentialsService.getCredentials(memberUsername);
				if (memberToAdd == null) {
					model.addAttribute("error", "html.project.share.error.notfound");
				} else {
					if (project.getMembers().contains(memberToAdd.getUser())) {
						model.addAttribute("error", "html.project.share.error.already");
					} else {
						projectService.shareProjectWithUser(project, memberToAdd.getUser());
						
						model.addAttribute("result", "html.project.share.result.added");
					}
				}
				
				List<Credentials> members = new ArrayList<>();
				project.getMembers().forEach(u -> credentialsRepository.findByUser(u).ifPresent(members::add));
				model.addAttribute("members", members);
				
				return "project/projectShare";
			}
		}
		return "error/403";
	}
	
	@RequestMapping(value = { "/project/{id}/share/remove" }, method = RequestMethod.POST)
	public String shareProjectRemove(Model model, @PathVariable Long id, @RequestParam("userId") Long memberId) {
		Credentials visitor = sessionData.getLoggedCredentials();
		model.addAttribute("visitor", visitor);
		
		Project project = projectService.getProject(id);
		if (project != null) {
			if (visitor.getRole().equals(ADMIN_ROLE) || visitor.getUser().getId().equals(project.getOwner().getId())) {
				model.addAttribute("project", project);
				
				List<Credentials> members = new ArrayList<>();
				project.getMembers().forEach(u -> credentialsRepository.findByUser(u).ifPresent(members::add));
				model.addAttribute("members", members);
				
				Credentials memberToRemove = members.stream().filter(m -> m.getId().equals(memberId)).findFirst().orElse(null);
				if (memberToRemove == null) {
					model.addAttribute("error", "html.project.share.error.notfound");
				} else {
					projectService.unshareProjectWithUser(project, memberToRemove.getUser());
					
					model.addAttribute("result", "html.project.share.result.removed");
				}
				
				return "project/projectShare";
			}
		}
		return "error/403";
	}
}
