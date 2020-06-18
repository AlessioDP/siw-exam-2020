package it.uniroma3.siw.esame;

import it.uniroma3.siw.esame.model.Comment;
import it.uniroma3.siw.esame.model.Credentials;
import it.uniroma3.siw.esame.model.Project;
import it.uniroma3.siw.esame.model.Tag;
import it.uniroma3.siw.esame.model.Task;
import it.uniroma3.siw.esame.model.User;
import it.uniroma3.siw.esame.repository.CommentRepository;
import it.uniroma3.siw.esame.repository.TagRepository;
import it.uniroma3.siw.esame.repository.TaskRepository;
import it.uniroma3.siw.esame.service.CredentialsService;
import it.uniroma3.siw.esame.service.ProjectService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EsameApplication extends SpringBootServletInitializer {
	private static Class<EsameApplication> applicationClass = EsameApplication.class;
	
	@Autowired
	private CredentialsService credentialsService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private CommentRepository commentRepository;
	@Autowired
	private TaskRepository taskRepository;
	@Autowired
	private TagRepository tagRepository;
	
	public static void main(String[] args) {
		SpringApplication.run(applicationClass, args);
	}
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(applicationClass);
	}
	
	@Bean
	public InitializingBean populateDatabaseIfEmpty() {
		return () -> {
			if (credentialsService.countAll() == 0) {
				// Users
				Credentials credAlessioDP = credentialsService.saveCredentials(new Credentials(
						"AlessioDP",
						"123456",
						Credentials.ADMIN_ROLE,
						new User("Alessio", "De Pauli"))
				);
				
				Credentials credTest = credentialsService.saveCredentials(new Credentials(
						"test",
						"123456",
						Credentials.DEFAULT_ROLE,
						new User("Nome", "Cognome"))
				);
				
				Credentials credDemo = credentialsService.saveCredentials(new Credentials(
						"demo",
						"123456",
						Credentials.DEFAULT_ROLE,
						new User("Demo", "Account"))
				);
				
				Project project = new Project("Example", "Example project");
				project.setOwner(credAlessioDP.getUser());
				project = projectService.saveProject(project);
				project = projectService.shareProjectWithUser(project, credTest.getUser());
				
				Tag tag = new Tag("WIP", "Work in progress", "#00FF00");
				tag.setProject(project);
				tag = tagRepository.save(tag);
				Tag tag2 = new Tag("Important", "Important task!", "#FF0000");
				tag2.setProject(project);
				tag2 = tagRepository.save(tag2);
				project = projectService.saveProject(project);
				
				project.addTask(new Task("Publish", "Publish the project", true, credAlessioDP.getUser(), project));
				project.addTask(new Task("Improve", "Improve code", false, credTest.getUser(), project, tag, tag2));
				project.addTask(new Task("Deploy", "Deploy the project", false, credAlessioDP.getUser(), project, tag2));
				project = projectService.saveProject(project);
				project.getTasks().forEach(t -> {
					if (t.getName().equals("Deploy")) {
						t.addComment(new Comment("The project must be deployed on Tomcat", credTest.getUser(), t));
						t.addComment(new Comment("Okay", credAlessioDP.getUser(), t));
						taskRepository.save(t);
					}
				});
				
				
				project = new Project("AnotherProject", "Private project");
				project.setOwner(credAlessioDP.getUser());
				projectService.saveProject(project);
				
				project = new Project("Demos", "My demos");
				project.setOwner(credDemo.getUser());
				project = projectService.saveProject(project);
				project = projectService.shareProjectWithUser(project, credAlessioDP.getUser());
				project = projectService.shareProjectWithUser(project, credTest.getUser());
				project.addTask(new Task("Publish", "Publish the project", false, credDemo.getUser(), project));
				projectService.saveProject(project);
				
				project = new Project("Demo1", "Demo1 project");
				project.setOwner(credDemo.getUser());
				project = projectService.saveProject(project);
				projectService.shareProjectWithUser(project, credTest.getUser());
				
				project = new Project("Example", "Example project");
				project.setOwner(credDemo.getUser());
				projectService.saveProject(project);
			}
		};
	}
}
