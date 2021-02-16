package com.hanu.domainfs;

import java.io.IOException;

import com.hanu.domainfs.ws.examples.services.coursemodule.model.CompulsoryModule;
import com.hanu.domainfs.ws.examples.services.coursemodule.model.CourseModule;
import com.hanu.domainfs.ws.examples.services.coursemodule.model.ElectiveModule;
import com.hanu.domainfs.ws.examples.services.enrolment.model.Enrolment;
import com.hanu.domainfs.ws.examples.services.sclass.model.SClass;
import com.hanu.domainfs.ws.examples.services.student.model.City;
import com.hanu.domainfs.ws.examples.services.student.model.Student;
import com.hanu.domainfs.ws.examples.services.student.reports.StudentsByCityJoinReport;
import com.hanu.domainfs.ws.examples.services.student.reports.StudentsByNameReport;
import com.hanu.domainfs.ws.generators.ServiceTypeGenerator;
import com.hanu.domainfs.ws.generators.WebControllerGenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import domainapp.basics.exceptions.DataSourceException;
import domainapp.basics.exceptions.NotFoundException;
import domainapp.basics.exceptions.NotPossibleException;
import domainapp.software.SoftwareFactory;
import domainapp.softwareimpl.SoftwareImpl;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 *
 */
@SpringBootApplication
@EntityScan
public class App {

    // 1. initialise the model
    static final Class<?>[] model = {
        CourseModule.class,
        CompulsoryModule.class,
        ElectiveModule.class,
        Enrolment.class,
        Student.class,
        City.class,
        SClass.class,
        // reports
        StudentsByNameReport.class,
        StudentsByCityJoinReport.class
    };

    private static SoftwareImpl sw;
    static {
        sw = SoftwareFactory.createDefaultDomSoftware();
        sw.init();
        try {
            sw.addClasses(model);
        } catch (NotPossibleException | NotFoundException | DataSourceException e) {
            throw new RuntimeException(e);
        }

        System.out.println("------------");
        WebControllerGenerator generator = WebControllerGenerator.instance();
        ServiceTypeGenerator generator2 = new ServiceTypeGenerator();
        generator2.generateAutowiredServiceType(Enrolment.class);
        generator2.generateAutowiredServiceType(Student.class);
        generator2.generateAutowiredServiceType(CourseModule.class);
        generator.getNestedRestfulController(Student.class, Enrolment.class);
        generator.getRestfulController(Enrolment.class);
        generator.getRestfulController(Student.class);
        generator.getRestfulController(CourseModule.class);
        System.out.println("------------");
    }

    /**
     *
     * @param args The arguments of the program.
     * @throws IOException
     * @throws IllegalAccessException
     */
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Configuration
    public static class BeanConfig {
        @Bean
        public SoftwareImpl getSoftwareImpl() {
            return sw;
        }
    }


}
