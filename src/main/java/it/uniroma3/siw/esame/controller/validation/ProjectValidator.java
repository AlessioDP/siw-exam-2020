package it.uniroma3.siw.esame.controller.validation;

import it.uniroma3.siw.esame.model.Project;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for Project
 */
@Component
public class ProjectValidator implements Validator {
	
	final Integer MAX_NAME_LENGTH = 20;
	final Integer MIN_NAME_LENGTH = 2;
	final Integer MAX_DESCRIPTION_LENGTH = 50;
	final Integer MIN_DESCRIPTION_LENGTH = 4;
	
	@Override
	public void validate(Object o, Errors errors) {
		Project project = (Project) o;
		String name = project.getName().trim();
		String description = project.getDescription().trim();
		
		if (name.isEmpty())
			errors.rejectValue("name", "required");
		else if (name.length() < MIN_NAME_LENGTH || name.length() > MAX_NAME_LENGTH)
			errors.rejectValue("name", "size");
		
		if (description.isEmpty())
			errors.rejectValue("description", "required");
		else if (description.length() < MIN_DESCRIPTION_LENGTH || description.length() > MAX_DESCRIPTION_LENGTH)
			errors.rejectValue("description", "size");
	}
	
	@Override
	public boolean supports(Class<?> clazz) {
		return Project.class.equals(clazz);
	}
	
}

