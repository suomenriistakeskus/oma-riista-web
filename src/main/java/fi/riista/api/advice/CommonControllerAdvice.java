package fi.riista.api.advice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@ControllerAdvice
public class CommonControllerAdvice {
    private final Collection<Validator> validators;

    @Autowired
    public CommonControllerAdvice(final Collection<Validator> validators) {
        this.validators = validators;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));

        Object target = binder.getTarget();
        if (target != null) {
            binder.replaceValidators(supportedValidatorsFor(target.getClass()));
        }
    }

    private Validator[] supportedValidatorsFor(Class<?> clazz) {
        final Set<Validator> supportedValidators = new HashSet<>();
        for (Validator validator : validators) {
            if (validator.supports(clazz)) {
                supportedValidators.add(validator);
            }
        }
        return supportedValidators.toArray(new Validator[supportedValidators.size()]);
    }
}
