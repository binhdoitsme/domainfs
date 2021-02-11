package com.hanu.domainfs;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import com.hanu.domainfs.ws.examples.services.coursemodule.model.CourseModule;
import com.hanu.domainfs.ws.examples.services.enrolment.model.Enrolment;
import com.hanu.domainfs.ws.examples.services.student.model.Student;
import com.hanu.domainfs.ws.generators.WebControllerGenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import domainapp.software.SoftwareFactory;
import domainapp.softwareimpl.SoftwareImpl;
import javassist.CannotCompileException;
import javassist.NotFoundException;

/**
 * 
 */
@SpringBootApplication
public class App {
    private static SoftwareImpl sw = SoftwareFactory.createDefaultDomSoftware();

    /**
     * 
     * @param args The arguments of the program.
     * @throws CannotCompileException
     * @throws NotFoundException
     * @throws IOException
     * @throws IllegalAccessException
     */
    public static void main(String[] args)
            throws NotFoundException, CannotCompileException, 
                IllegalAccessException, IOException,
                InvocationTargetException, InstantiationException {

        System.out.println("------------");
        WebControllerGenerator generator = WebControllerGenerator.instance();
        generator.generateAutowiredServiceType(Enrolment.class);
        generator.generateAutowiredServiceType(Student.class);
        generator.generateAutowiredServiceType(CourseModule.class);
        generator.getRestfulController(Enrolment.class);
        generator.getRestfulController(Student.class);
        generator.getRestfulController(CourseModule.class);
            

        System.out.println("------------");
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
