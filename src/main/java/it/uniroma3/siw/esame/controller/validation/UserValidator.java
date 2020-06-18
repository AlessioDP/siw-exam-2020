package it.uniroma3.siw.esame.controller.validation;

import it.uniroma3.siw.esame.model.User;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UserValidator implements Validator {
	
	final Integer MAX_NAME_LENGTH = 100;
	final Integer MIN_NAME_LENGTH = 2;
	
	@Override
	public void validate(Object o, Errors errors) {
		User user = (User) o;
		String firstName = user.getFirstName().trim();
		String lastName = user.getLastName().trim();
		
		if (firstName.isEmpty())
			errors.rejectValue("firstName", "required");
		else if (firstName.length() < MIN_NAME_LENGTH || firstName.length() > MAX_NAME_LENGTH)
			errors.rejectValue("firstName", "size");
		
		if (lastName.isEmpty())
			errors.rejectValue("lastName", "required");
		else if (lastName.length() < MIN_NAME_LENGTH || lastName.length() > MAX_NAME_LENGTH)
			errors.rejectValue("lastName", "size");
	}
	
	@Override
	public boolean supports(Class<?> clazz) {
		return User.class.equals(clazz);
	}
	
}

