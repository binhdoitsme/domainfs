package com.hanu.domainfs.ws;

import com.hanu.domainfs.examples.services.coursemodule.model.CompulsoryModule;
import com.hanu.domainfs.examples.services.coursemodule.model.CourseModule;
import com.hanu.domainfs.examples.services.coursemodule.model.ElectiveModule;
import com.hanu.domainfs.examples.services.enrolment.model.Enrolment;
import com.hanu.domainfs.examples.services.sclass.model.SClass;
import com.hanu.domainfs.examples.services.student.model.City;
import com.hanu.domainfs.examples.services.student.model.Student;
import com.hanu.domainfs.ws.generators.WebServiceGenerator;
import com.hanu.domainfs.ws.generators.annotations.bridges.TargetType;
import com.hanu.domainfs.ws.generators.controllers.ServiceRegistry;
import com.hanu.domainfs.ws.generators.services.CrudService;

import domainapp.basics.exceptions.DataSourceException;
import domainapp.basics.exceptions.NotFoundException;
import domainapp.basics.exceptions.NotPossibleException;
import domainapp.software.SoftwareFactory;
import domainapp.softwareimpl.SoftwareImpl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 *
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.hanu.domainfs.examples",
    "com.hanu.domainfs.ws"})
public class SpringApp {

    // 1. initialise the model
    static final Class<?>[] model = {
        CourseModule.class,
        CompulsoryModule.class,
        ElectiveModule.class,
        Enrolment.class,
        Student.class,
        City.class,
        SClass.class,
    };

    static {
        try {
            Class.forName("com.hanu.domainfs.DummyApp");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    private static SoftwareImpl sw;

    /**
     *
     * @param args The arguments of the program.
     */
    public static void main(final String[] args) {
        System.out.println("------------");

        WebServiceGenerator generator = new WebServiceGenerator(TargetType.SPRING);
        generator.setGenerateCompleteCallback(() -> {
            sw = SoftwareFactory.createDefaultDomSoftware();
            sw.init();
            try {
                sw.addClasses(model);
            } catch (NotPossibleException | NotFoundException | DataSourceException e) {
                throw new RuntimeException(e);
            }
            // populate the service registry
            final ServiceRegistry registry = ServiceRegistry.getInstance();
            ApplicationContext ctx = SpringApplication.run(SpringApp.class, args);
            ctx.getBeansOfType(CrudService.class).forEach((k, v) -> {
                registry.put(k, v);
            });

        });
        generator.generateWebService(model);
        System.out.println("------------");
    }

    @Configuration
    public static class BeanConfig {
        @Bean
        public SoftwareImpl getSoftwareImpl() {
            return sw;
        }
    }


}
