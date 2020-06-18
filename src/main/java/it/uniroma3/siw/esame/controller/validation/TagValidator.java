package it.uniroma3.siw.esame.controller.validation;

import it.uniroma3.siw.esame.model.Tag;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Arrays;
import java.util.List;

@Component
public class TagValidator implements Validator {
	
	final Integer MAX_NAME_LENGTH = 15;
	final Integer MIN_NAME_LENGTH = 2;
	final Integer MAX_DESCRIPTION_LENGTH = 50;
	final Integer MIN_DESCRIPTION_LENGTH = 4;
	static final List<Integer> ALLOWED_COLOR_SIZES = Arrays.asList(4, 7, 9); // #111, #222333, #44455566
	
	@Override
	public void validate(Object o, Errors errors) {
		Tag tag = (Tag) o;
		String name = tag.getName().trim();
		String description = tag.getDescription().trim();
		String color = tag.getColor();
		
		if (name.isEmpty())
			errors.rejectValue("name", "required");
		else if (name.length() < MIN_NAME_LENGTH || name.length() > MAX_NAME_LENGTH)
			errors.rejectValue("name", "size");
		
		if (description.isEmpty())
			errors.rejectValue("description", "required");
		else if (description.length() < MIN_DESCRIPTION_LENGTH || description.length() > MAX_DESCRIPTION_LENGTH)
			errors.rejectValue("description", "size");
		
		if (color.isEmpty())
			errors.rejectValue("color", "required");
		else if (!color.startsWith("#") || !ALLOWED_COLOR_SIZES.contains(color.length()))
			errors.rejectValue("color", "invalid");
	}
	
	@Override
	public boolean supports(Class<?> clazz) {
		return Tag.class.equals(clazz);
	}
	
}

