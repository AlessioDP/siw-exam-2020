package it.uniroma3.siw.esame;

import it.uniroma3.siw.esame.model.User;
import it.uniroma3.siw.esame.repository.ProjectRepository;
import it.uniroma3.siw.esame.repository.TaskRepository;
import it.uniroma3.siw.esame.repository.UserRepository;
import it.uniroma3.siw.esame.service.ProjectService;
import it.uniroma3.siw.esame.service.TaskService;
import it.uniroma3.siw.esame.service.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = {H2JpaConfig.class})
@RunWith(SpringRunner.class)
public class EsameApplicationTests {
	@Autowired
	private ProjectService projectService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private UserService userService;
	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private TaskRepository taskRepository;
	@Autowired
	private UserRepository userRepository;
	
	@Before
	public void setUp() {
		projectRepository.deleteAll();
		taskRepository.deleteAll();
		userRepository.deleteAll();
	}
	
	@Test
	public void testUpdateUser() {
		User user1 = new User("Mario", "Rossi");
		user1 = userService.saveUser(user1);
		Assert.assertEquals(user1.getId().longValue(), 1L);
		Assert.assertEquals(user1.getFirstName(), "Mario");
	}

}
