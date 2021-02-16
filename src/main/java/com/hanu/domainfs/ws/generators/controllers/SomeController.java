package com.hanu.domainfs.ws.generators.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/some")
public class SomeController {
    private RequestMappingHandlerMapping mapper;

    @Autowired
    public SomeController(RequestMappingHandlerMapping mapper) {
        this.mapper = mapper;
    }

    @GetMapping
    public Collection<String> getSome() {
        return mapper.getHandlerMethods().keySet().stream().map(x -> x.toString()).collect(Collectors.toList());
    }
}
