package it.uniroma3.siw.esame.controller;

import it.uniroma3.siw.esame.authentication.AuthConfiguration;
import it.uniroma3.siw.esame.controller.session.SessionData;
import it.uniroma3.siw.esame.controller.validation.CredentialsValidator;
import it.uniroma3.siw.esame.controller.validation.UserValidator;
import it.uniroma3.siw.esame.model.Credentials;
import it.uniroma3.siw.esame.model.User;
import it.uniroma3.siw.esame.service.CredentialsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;

@Controller
public class AuthenticationController {
	
	@Autowired
	CredentialsService credentialsService;
	
	@Autowired
	UserValidator userValidator;
	@Autowired
	CredentialsValidator credentialsValidator;
	
	@Autowired
	AuthConfiguration authConfiguration;
	@Autowired
	SessionData sessionData;
	
	@RequestMapping(value = { "/login" }, method = RequestMethod.GET)
	public String showLoginForm(Model model) {
		model.addAttribute("visitor", sessionData.getLoggedCredentials());
		model.addAttribute("credentialsForm", new Credentials());
		
		return "profile/login";
	}
	
	@RequestMapping(value = { "/register" }, method = RequestMethod.GET)
	public String showRegisterForm(Model model) {
		Credentials visitor = sessionData.getLoggedCredentials();
		if (visitor != null)
			return "error/403";
		model.addAttribute("userForm", new User());
		model.addAttribute("credentialsForm", new Credentials());
		
		return "profile/register";
	}
	
	@RequestMapping(value = { "/register" }, method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("userForm") User user,
							   BindingResult userBindingResult,
							   @Valid @ModelAttribute("credentialsForm") Credentials credentials,
							   BindingResult credentialsBindingResult,
							   Model model) {
		Credentials visitor = sessionData.getLoggedCredentials();
		if (visitor != null)
			return "error/403";
		
		this.userValidator.validate(user, userBindingResult);
		this.credentialsValidator.validate(credentials, credentialsBindingResult);
		
		if(!userBindingResult.hasErrors() && ! credentialsBindingResult.hasErrors()) {
			String plainPassword = credentials.getPassword();
			credentials.setUser(user);
			credentials = credentialsService.saveCredentials(credentials);
			
			authConfiguration.authenticate(credentials.getUserName(), plainPassword); // Auto login with plain password
			
			model.addAttribute("registered", true);
		}
		return "profile/register";
	}
}
