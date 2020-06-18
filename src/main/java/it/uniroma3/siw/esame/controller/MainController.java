package it.uniroma3.siw.esame.controller;

import it.uniroma3.siw.esame.controller.session.SessionData;
import it.uniroma3.siw.esame.model.Credentials;
import it.uniroma3.siw.esame.model.Project;
import it.uniroma3.siw.esame.model.User;
import it.uniroma3.siw.esame.service.CommentService;
import it.uniroma3.siw.esame.service.CredentialsService;
import it.uniroma3.siw.esame.service.ProjectService;
import it.uniroma3.siw.esame.service.TagService;
import it.uniroma3.siw.esame.service.TaskService;
import it.uniroma3.siw.esame.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;

@Controller
public class MainController {
	
	@Autowired
	CredentialsService credentialsService;
	@Autowired
	CommentService commentService;
	@Autowired
	ProjectService projectService;
	@Autowired
	TagService tagService;
	@Autowired
	TaskService taskService;
	
	@Autowired
	SessionData sessionData;
	
	public MainController() {}
	
	@RequestMapping(value = { "/siw" }, method = RequestMethod.GET)
	public String siw(Model model) {
		return "redirect:/";
	}
	
	@RequestMapping(value = { "/", "/index", "/home" }, method = RequestMethod.GET)
	public String index(Model model) {
		User visitor = sessionData.getLoggedUser();
		
		if (visitor != null) {
			// User logged in
			Credentials credentials = credentialsService.getCredentials(visitor);
			model.addAttribute("credentials", credentials);
			
			List<Pair<Project, Credentials>> visibleProjects = new ArrayList<>();
			for (Project p : credentials.getUser().getVisibleProjects()) {
				visibleProjects.add(new Pair<>(p, p.getOwner() != null ? credentialsService.getCredentials(p.getOwner()) : null));
			}
			
			model.addAttribute("ownedProjects", credentials.getUser().getOwnedProjects());
			model.addAttribute("visibleProjects", visibleProjects);
			return "home";
		}
		
		model.addAttribute("totalProjects", projectService.countAll());
		model.addAttribute("totalMembers", credentialsService.countAll());
		model.addAttribute("totalTasks", taskService.countAll());
		model.addAttribute("totalComments", commentService.countAll());
		return "index";
	}
	
	@RequestMapping(value = { "/projects" }, method = RequestMethod.GET)
	public String projects(Model model) {
		List<Pair<Project, Credentials>> projects = new ArrayList<>();
		for (Project p : projectService.getAllProjects()) {
			projects.add(new Pair<>(p, p.getOwner() != null ? credentialsService.getCredentials(p.getOwner()) : null));
		}
		model.addAttribute("projects", projects);
		return "projects";
	}
	
	@RequestMapping(value = { "/users" }, method = RequestMethod.GET)
	public String users(Model model) {
		model.addAttribute("credentials", credentialsService.getAllCredentials());
		return "users";
	}
	
	@RequestMapping(value = { "/admin" }, method = RequestMethod.GET)
	public String admin(Model model) {
		model.addAttribute("projects", projectService.getAllProjects());
		model.addAttribute("credentials", credentialsService.getAllCredentials());
		model.addAttribute("tasks", taskService.getAllTasks());
		model.addAttribute("tags", tagService.getAllTags());
		model.addAttribute("comments", commentService.getAllComments());
		return "admin";
	}
}
