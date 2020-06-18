package it.uniroma3.siw.esame.controller;

import it.uniroma3.siw.esame.controller.session.SessionData;
import it.uniroma3.siw.esame.controller.validation.CredentialsValidator;
import it.uniroma3.siw.esame.controller.validation.ProjectValidator;
import it.uniroma3.siw.esame.controller.validation.TaskValidator;
import it.uniroma3.siw.esame.model.Comment;
import it.uniroma3.siw.esame.model.Credentials;
import it.uniroma3.siw.esame.model.Project;
import it.uniroma3.siw.esame.model.Tag;
import it.uniroma3.siw.esame.model.Task;
import it.uniroma3.siw.esame.repository.CommentRepository;
import it.uniroma3.siw.esame.repository.CredentialsRepository;
import it.uniroma3.siw.esame.repository.ProjectRepository;
import it.uniroma3.siw.esame.repository.TagRepository;
import it.uniroma3.siw.esame.repository.UserRepository;
import it.uniroma3.siw.esame.service.CommentService;
import it.uniroma3.siw.esame.service.CredentialsService;
import it.uniroma3.siw.esame.service.ProjectService;
import it.uniroma3.siw.esame.service.TaskService;
import it.uniroma3.siw.esame.util.Pair;
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
public class TaskController {
	
	@Autowired
	CredentialsRepository credentialsRepository;
	@Autowired
	CommentRepository commentRepository;
	@Autowired
	ProjectRepository projectRepository;
	@Autowired
	TagRepository tagRepository;
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	CommentService commentService;
	@Autowired
	CredentialsService credentialsService;
	@Autowired
	ProjectService projectService;
	@Autowired
	TaskService taskService;
	
	
	@Autowired
	CredentialsValidator credentialsValidator;
	@Autowired
	ProjectValidator projectValidator;
	@Autowired
	TaskValidator taskValidator;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	@Autowired
	SessionData sessionData;
	
	
	@RequestMapping(value = { "/project/{id}/task/{taskId}" }, method = RequestMethod.GET)
	public String showProject(Model model, @PathVariable Long id, @PathVariable Long taskId) {
		Credentials visitor = sessionData.getLoggedCredentials();
		model.addAttribute("visitor", visitor);
		boolean found = false;
		
		Project project = projectService.getProject(id);
		Task task = taskService.getTask(taskId);
		if (project != null && task != null) {
			// If the project exists
			if (visitor.getRole().equals(ADMIN_ROLE)
					|| visitor.getUser().getId().equals(project.getOwner().getId())
					|| project.getMembers().contains(visitor.getUser())) {
				if (task.getUser() != null)
					model.addAttribute("assignedTo", credentialsRepository.findByUser(task.getUser()).orElse(null));
				
				List<Pair<Credentials, Comment>> comments = new ArrayList<>();
				for (Comment comment : commentRepository.findByTaskOrderByCreationTimestampAsc(task)) {
					comments.add(new Pair<>(credentialsService.getCredentials(comment.getUser()), comment));
				}
				model.addAttribute("comments", comments);
				found = true;
			}
		}
		model.addAttribute("found", found);
		model.addAttribute("project", project);
		model.addAttribute("task", task);
		return "project/task";
	}
	
	@RequestMapping(value = { "/project/{id}/task/create" }, method = RequestMethod.GET)
	public String showCreateTaskPrompt(Model model, @PathVariable Long id) {
		Credentials visitor = sessionData.getLoggedCredentials();
		model.addAttribute("visitor", visitor);
		
		Project project = projectService.getProject(id);
		if (project != null) {
			if (visitor.getRole().equals(ADMIN_ROLE)
					|| visitor.getUser().getId().equals(project.getOwner().getId())) {
				model.addAttribute("project", project);
				model.addAttribute("task", new Task());
				model.addAttribute("taskForm", new Task());
				
				List<Credentials> members = new ArrayList<>();
				credentialsRepository.findByUser(project.getOwner()).ifPresent(members::add);
				project.getMembers().forEach(u -> credentialsRepository.findByUser(u).ifPresent(members::add));
				model.addAttribute("members", members);
				
				model.addAttribute("editing", false);
				return "project/taskEdit";
			}
		}
		return "error/403";
	}
	
	@RequestMapping(value = { "/project/{id}/task/create" }, method = RequestMethod.POST)
	public String createTask(Model model,
							 @PathVariable Long id,
							 @RequestParam("assigned") Long assigned,
							 @Valid @ModelAttribute("taskForm") Task task,
							 BindingResult taskBindingResult) {
		Credentials visitor = sessionData.getLoggedCredentials();
		model.addAttribute("visitor", visitor);
		
		Project project = projectService.getProject(id);
		if (project != null) {
			if (visitor.getRole().equals(ADMIN_ROLE)
					|| visitor.getUser().getId().equals(project.getOwner().getId())) {
				this.taskValidator.validate(task, taskBindingResult);
				
				if (!taskBindingResult.hasErrors()) {
					if (assigned > 0) {
						Credentials credentialsAssigned = credentialsService.getCredentials(assigned);
						if (credentialsAssigned != null
								&& (project.getOwner().equals(credentialsAssigned.getUser())
								|| project.getMembers().contains(credentialsAssigned.getUser()))) {
							task.setUser(credentialsAssigned.getUser());
						}
					}
					
					task.setProject(project);
					project.addTask(task);
					projectService.saveProject(project);
					
					return "redirect:/project/" + project.getId();
				}
				model.addAttribute("project", project);
				model.addAttribute("task", task);
				model.addAttribute("taskForm", task);
				model.addAttribute("editing", false);
				return "project/taskEdit";
			}
		}
		return "error/403";
	}
	
	@RequestMapping(value = { "/project/{id}/task/{taskId}/delete" }, method = RequestMethod.GET)
	public String deleteTask(Model model, @PathVariable Long id, @PathVariable Long taskId) {
		Credentials visitor = sessionData.getLoggedCredentials();
		
		Project project = projectService.getProject(id);
		Task task = taskService.getTask(taskId);
		if (project != null && task != null) {
			if (visitor.getRole().equals(ADMIN_ROLE)
					|| visitor.getUser().getId().equals(project.getOwner().getId())) {
				// Delete the task
				taskService.deleteTask(task);
				return "redirect:/project/" + project.getId();
			}
		}
		return "error/403";
	}
	
	@RequestMapping(value = { "/project/{id}/task/{taskId}/edit" }, method = RequestMethod.GET)
	public String showEditTaskPrompt(Model model, @PathVariable Long id, @PathVariable Long taskId) {
		Credentials visitor = sessionData.getLoggedCredentials();
		
		Project project = projectService.getProject(id);
		Task task = taskService.getTask(taskId);
		if (project != null && task != null) {
			if (visitor.getRole().equals(ADMIN_ROLE)
					|| visitor.getUser().getId().equals(project.getOwner().getId())) {
				model.addAttribute("project", project);
				model.addAttribute("task", task);
				model.addAttribute("taskForm", task);
				model.addAttribute("members", getListMembers(project));
				
				model.addAttribute("editing", true);
				return "project/taskEdit";
			}
		}
		return "error/403";
	}
	
	@RequestMapping(value = { "/project/{id}/task/{taskId}/edit" }, method = RequestMethod.POST)
	public String editTask(Model model,
						   @PathVariable Long id,
						   @PathVariable Long taskId,
						   @RequestParam("assigned") Long assigned,
						   @Valid @ModelAttribute("taskForm") Task task,
						   BindingResult taskBindingResult) {
		Credentials visitor = sessionData.getLoggedCredentials();
		
		Project project = projectService.getProject(id);
		Task selectedTask = taskService.getTask(taskId);
		if (project != null && selectedTask != null) {
			if (visitor.getRole().equals(ADMIN_ROLE)
					|| visitor.getUser().getId().equals(project.getOwner().getId())) {
				this.taskValidator.validate(task, taskBindingResult);
				
				if (!taskBindingResult.hasErrors()) {
					if (assigned > 0) {
						Credentials credentialsAssigned = credentialsService.getCredentials(assigned);
						if (credentialsAssigned != null
								&& (project.getOwner().equals(credentialsAssigned.getUser())
								|| project.getMembers().contains(credentialsAssigned.getUser()))) {
							selectedTask.setUser(credentialsAssigned.getUser());
						}
					} else {
						selectedTask.setUser(null);
					}
					selectedTask.setName(task.getName());
					selectedTask.setDescription(task.getDescription());
					taskService.saveTask(selectedTask);
					
					return "redirect:/project/" + project.getId();
				}
				model.addAttribute("project", project);
				model.addAttribute("task", selectedTask);
				model.addAttribute("members", getListMembers(project));
				model.addAttribute("editing", true);
				return "project/taskEdit";
			}
		}
		return "error/403";
	}
	
	@RequestMapping(value = { "/project/{id}/task/{taskId}/assignTag" }, method = RequestMethod.POST)
	public String shareProjectAdd(Model model, @PathVariable Long id, @PathVariable Long taskId, @RequestParam("assignedTag") Long assignedTag) {
		Credentials visitor = sessionData.getLoggedCredentials();
		model.addAttribute("visitor", visitor);
		
		Project project = projectService.getProject(id);
		Task task = taskService.getTask(taskId);
		if (project != null && task != null) {
			if (visitor.getRole().equals(ADMIN_ROLE) || visitor.getUser().getId().equals(project.getOwner().getId())) {
				Tag tagToAdd = tagRepository.findById(assignedTag).orElse(null);
				if (tagToAdd == null || !project.getTags().contains(tagToAdd)) {
					model.addAttribute("error", "html.project.task.edit.tag.error.notfound");
				} else {
					if (task.getTags().contains(tagToAdd)) {
						model.addAttribute("error", "html.project.task.edit.tag.error.already");
					} else {
						task.addTag(tagToAdd);
						task = taskService.saveTask(task);
						
						model.addAttribute("result", "html.project.task.edit.tag.result.assigned");
					}
				}
				
				model.addAttribute("project", project);
				model.addAttribute("task", task);
				model.addAttribute("taskForm", task);
				model.addAttribute("members", getListMembers(project));
				model.addAttribute("editing", true);
				return "project/taskEdit";
			}
		}
		return "error/403";
	}
	
	@RequestMapping(value = { "/project/{id}/task/{taskId}/unassignTag" }, method = RequestMethod.POST)
	public String shareProjectRemove(Model model, @PathVariable Long id, @PathVariable Long taskId, @RequestParam("unassignedTag") Long unassignedTag) {
		Credentials visitor = sessionData.getLoggedCredentials();
		model.addAttribute("visitor", visitor);
		
		Project project = projectService.getProject(id);
		Task task = taskService.getTask(taskId);
		if (project != null && task != null) {
			if (visitor.getRole().equals(ADMIN_ROLE) || visitor.getUser().getId().equals(project.getOwner().getId())) {
				Tag tagToRemove = tagRepository.findById(unassignedTag).orElse(null);
				if (tagToRemove == null || !project.getTags().contains(tagToRemove)) {
					model.addAttribute("error", "html.project.task.edit.tag.error.notfound");
				} else {
					if (!task.getTags().contains(tagToRemove)) {
						model.addAttribute("error", "html.project.task.edit.tag.error.notassigned");
					} else {
						task.removeTag(tagToRemove);
						task = taskService.saveTask(task);
						
						model.addAttribute("result", "html.project.task.edit.tag.result.unassigned");
					}
				}
				
				model.addAttribute("project", project);
				model.addAttribute("task", task);
				model.addAttribute("taskForm", task);
				model.addAttribute("members", getListMembers(project));
				model.addAttribute("editing", true);
				return "project/taskEdit";
			}
		}
		return "error/403";
	}
	
	@RequestMapping(value = { "/project/{id}/task/{taskId}/comment/add" }, method = RequestMethod.POST)
	public String addComment(Model model, @PathVariable Long id, @PathVariable Long taskId, @RequestParam("text") String text) {
		Credentials visitor = sessionData.getLoggedCredentials();
		model.addAttribute("visitor", visitor);
		
		Project project = projectService.getProject(id);
		Task task = taskService.getTask(taskId);
		if (project != null && task != null) {
			if (visitor.getRole().equals(ADMIN_ROLE) || project.getMembers().contains(visitor.getUser()) || project.getOwner().equals(visitor.getUser())) {
				Comment comment = new Comment(text, visitor.getUser(), task);
				task.addComment(comment);
				taskService.saveTask(task);
				
				return "redirect:/project/" + project.getId() + "/task/" + task.getId();
			}
		}
		return "error/403";
	}
	
	@RequestMapping(value = { "/project/{id}/task/{taskId}/comment/{commentId}/delete" }, method = RequestMethod.GET)
	public String addComment(Model model, @PathVariable Long id, @PathVariable Long taskId, @PathVariable Long commentId) {
		Credentials visitor = sessionData.getLoggedCredentials();
		model.addAttribute("visitor", visitor);
		
		Project project = projectService.getProject(id);
		Task task = taskService.getTask(taskId);
		Comment comment = commentRepository.findById(commentId).orElse(null);
		if (project != null && task != null && comment != null
				&& project.getTasks().contains(comment.getTask())
				&& task.getComments().contains(comment)) {
			if (visitor.getRole().equals(ADMIN_ROLE)
					|| project.getOwner().equals(visitor.getUser())) {
				commentService.deleteComment(comment);
				
				return "redirect:/project/" + project.getId() + "/task/" + task.getId();
			}
		}
		return "error/403";
	}
	
	@RequestMapping(value = { "/project/{id}/task/{taskId}/markCompleted" }, method = RequestMethod.GET)
	public String markCompleted(Model model, @PathVariable Long id, @PathVariable Long taskId) {
		Credentials visitor = sessionData.getLoggedCredentials();
		model.addAttribute("visitor", visitor);
		
		Project project = projectService.getProject(id);
		Task task = taskService.getTask(taskId);
		if (project != null && task != null) {
			if (visitor.getRole().equals(ADMIN_ROLE) || visitor.getUser().getId().equals(project.getOwner().getId())) {
				task.setCompleted(true);
				taskService.saveTask(task);
				
				return "redirect:/project/" + project.getId() + "/task/" + task.getId();
			}
		}
		return "error/403";
	}
	
	@RequestMapping(value = { "/project/{id}/task/{taskId}/markUncompleted" }, method = RequestMethod.GET)
	public String markUnompleted(Model model, @PathVariable Long id, @PathVariable Long taskId) {
		Credentials visitor = sessionData.getLoggedCredentials();
		model.addAttribute("visitor", visitor);
		
		Project project = projectService.getProject(id);
		Task task = taskService.getTask(taskId);
		if (project != null && task != null) {
			if (visitor.getRole().equals(ADMIN_ROLE) || visitor.getUser().getId().equals(project.getOwner().getId())) {
				task.setCompleted(false);
				taskService.saveTask(task);
				
				return "redirect:/project/" + project.getId() + "/task/" + task.getId();
			}
		}
		return "error/403";
	}
	
	private List<Credentials> getListMembers(Project project) {
		List<Credentials> members = new ArrayList<>();
		credentialsRepository.findByUser(project.getOwner()).ifPresent(members::add);
		project.getMembers().forEach(u -> credentialsRepository.findByUser(u).ifPresent(members::add));
		return members;
	}
}
