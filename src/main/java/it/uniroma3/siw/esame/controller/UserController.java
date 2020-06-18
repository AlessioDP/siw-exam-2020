package it.uniroma3.siw.esame.controller;

import it.uniroma3.siw.esame.controller.session.SessionData;
import it.uniroma3.siw.esame.controller.validation.CredentialsValidator;
import it.uniroma3.siw.esame.controller.validation.UserValidator;
import it.uniroma3.siw.esame.model.Credentials;
import it.uniroma3.siw.esame.model.User;
import it.uniroma3.siw.esame.repository.CredentialsRepository;
import it.uniroma3.siw.esame.repository.UserRepository;
import it.uniroma3.siw.esame.service.CredentialsService;
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

import java.util.Optional;

import static it.uniroma3.siw.esame.model.Credentials.ADMIN_ROLE;
import static it.uniroma3.siw.esame.model.Credentials.DEFAULT_ROLE;

/**
 * The UserController handles all interactions involving User data.
 */
@Controller
public class UserController {
	
	@Autowired
	CredentialsRepository credentialsRepository;
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	CredentialsService credentialsService;
	
	@Autowired
	CredentialsValidator credentialsValidator;
	@Autowired
	UserValidator userValidator;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	SessionData sessionData;
	
	@RequestMapping(value = { "/profile" }, method = RequestMethod.GET)
	public String profileMe(Model model) {
		Credentials visitor = sessionData.getLoggedCredentials();
		model.addAttribute("visitor", visitor);
		
		Credentials credentials = credentialsRepository.findById(visitor.getId()).orElse(visitor);
		model.addAttribute("credentials", credentials);
		
		return "profile/profile";
	}
	
	@RequestMapping(value = { "/profile/{userName}" }, method = RequestMethod.GET)
	public String profileOthers(Model model, @PathVariable String userName) {
		Credentials visitor = sessionData.getLoggedCredentials();
		model.addAttribute("visitor", visitor);
		
		credentialsRepository.findByUserNameIgnoreCase(userName).ifPresent(c -> {
			model.addAttribute("credentials", c);
		});
		
		return "profile/profile";
	}
	
	@RequestMapping(value = { "/profile/{userName}/delete" }, method = RequestMethod.GET)
	public String deleteProfile(Model model, @PathVariable String userName) {
		Credentials visitor = sessionData.getLoggedCredentials();
		
		if (visitor.getRole().equals(ADMIN_ROLE)) {
			credentialsRepository.findByUserNameIgnoreCase(userName).ifPresent(c -> {
				credentialsRepository.delete(c);
			});
		}
		return "redirect:/";
	}
	
	@RequestMapping(value = { "/profile/{userName}/edit" }, method = RequestMethod.GET)
	public String editProfile(Model model, @PathVariable String userName) {
		Credentials visitor = sessionData.getLoggedCredentials();
		model.addAttribute("visitor", visitor);
		
		Credentials credentials = credentialsRepository.findByUserNameIgnoreCase(userName).orElse(null);
		if (credentials != null) {
			if (visitor.getRole().equals(ADMIN_ROLE) || visitor.getId().equals(credentials.getId())) {
				model.addAttribute("userForm", credentials.getUser());
				model.addAttribute("credentialsForm", credentials);
				
				model.addAttribute("credentials", credentials);
				return "profile/profileEdit";
			}
		}
		return "error/403";
	}
	
	@RequestMapping(value = { "/profile/{userName}/edit" }, method = RequestMethod.POST)
	public String editProfile(@Valid @ModelAttribute("userForm") User user,
							  BindingResult userBindingResult,
							  Model model,
							  @PathVariable String userName,
							  @RequestParam(value = "role", required = false) Optional<String> role) {
		// validate user fields
		this.userValidator.validate(user, userBindingResult);
		
		Credentials credentialsProfile = credentialsRepository.findByUserNameIgnoreCase(userName).orElse(null);
		Credentials visitor = sessionData.getLoggedCredentials();
		if (credentialsProfile != null) {
			if (!userBindingResult.hasErrors()) {
				if (role.isPresent()
						&& visitor.getRole().equals(ADMIN_ROLE)
						&& (DEFAULT_ROLE.equals(role.get()) || ADMIN_ROLE.equals(role.get()))) {
					// Only admins can edit roles
					credentialsProfile.setRole(role.get());
				}
				// set the user and store the credentials;
				credentialsProfile.getUser().setFirstName(user.getFirstName());
				credentialsProfile.getUser().setLastName(user.getLastName());
				credentialsRepository.save(credentialsProfile);
				
				return "redirect:/profile/" + userName;
			}
			model.addAttribute("visitor", visitor);
			
			model.addAttribute("userForm", user);
			model.addAttribute("credentials", credentialsProfile);
			return "profile/profileEdit";
		}
		return "redirect:/profile/" + userName;
	}
}
