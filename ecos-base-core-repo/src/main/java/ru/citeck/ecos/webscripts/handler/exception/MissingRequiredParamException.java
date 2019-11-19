package ru.citeck.ecos.webscripts.handler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MissingRequiredParamException extends RuntimeException {

    public MissingRequiredParamException(String parameterName) {
        super("Required parameter is missing: " + parameterName);
    }
}
