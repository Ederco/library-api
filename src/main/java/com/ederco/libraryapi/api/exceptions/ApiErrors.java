package com.ederco.libraryapi.api.exceptions;

import com.ederco.libraryapi.exception.BusinessException;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiErrors {
    private List<String> errors;

    public ApiErrors(BindingResult bindingResult) {
        this.errors = new ArrayList<>();
        bindingResult.getAllErrors().forEach(error -> this.errors.add(error.getDefaultMessage()));
    }

    public ApiErrors(BusinessException ex) {
        this.errors = Arrays.asList(ex.getMessage());
    }

    public List<String> getErrors() {
        return errors;
    }

    public ApiErrors(ResponseStatusException ex) {
        this.errors = Arrays.asList(ex.getReason());
    }
}
