package ru.citeck.ecos.graphql.exceptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

import java.util.List;

/**
 * Citeck graph-ql exception
 */
@JsonIgnoreProperties()
public class CiteckGraphQLException implements GraphQLError {

    private String message;

    private List<SourceLocation> locations;

    private ErrorType errorType;

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return locations;
    }

    public void setLocations(List<SourceLocation> locations) {
        this.locations = locations;
    }

    @Override
    public ErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }
}
