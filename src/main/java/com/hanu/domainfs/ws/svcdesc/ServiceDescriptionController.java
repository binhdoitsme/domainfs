package com.hanu.domainfs.ws.svcdesc;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller (entry point) for Service Description module
 * @author binh_dh
 */
@RestController
@RequestMapping("/services")
public class ServiceDescriptionController {

    private final String basePackageName;

    public ServiceDescriptionController(
            @Value("${basePackage}") String basePackageName) {
        this.basePackageName = basePackageName;
    }

    /**
     * Get the list of service descriptions.
     * @return the list of service descriptions
     */
    @GetMapping
    public List<ServiceDescription> getServiceDescriptions() {
        return ServiceDescriptor.getDescriber()
            .describePackage(basePackageName);
    }
}
