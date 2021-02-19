package com.hanu.domainfs;

import com.hanu.domainfs.ws.examples.services.coursemodule.model.CompulsoryModule;
import com.hanu.domainfs.ws.examples.services.coursemodule.model.CourseModule;
import com.hanu.domainfs.ws.examples.services.coursemodule.model.ElectiveModule;
import com.hanu.domainfs.ws.examples.services.enrolment.model.Enrolment;
import com.hanu.domainfs.ws.examples.services.sclass.model.SClass;
import com.hanu.domainfs.ws.examples.services.student.model.City;
import com.hanu.domainfs.ws.examples.services.student.model.Student;
import com.hanu.domainfs.ws.generators.WebServiceGenerator;
import domainapp.basics.exceptions.DataSourceException;
import domainapp.basics.exceptions.NotFoundException;
import domainapp.basics.exceptions.NotPossibleException;
import domainapp.software.SoftwareFactory;
import domainapp.softwareimpl.SoftwareImpl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 */
@SpringBootApplication
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
//        StudentsByNameReport.class,
//        StudentsByCityJoinReport.class
    };

    private static SoftwareImpl sw;

    /**
     *
     * @param args The arguments of the program.
     */
    public static void main(final String[] args) {
        System.out.println("------------");

        WebServiceGenerator generator = WebServiceGenerator.instance();
        generator.setGenerateCompleteCallback(() -> {
            sw = SoftwareFactory.createDefaultDomSoftware();
            sw.init();
            try {
                sw.addClasses(model);
            } catch (NotPossibleException | NotFoundException | DataSourceException e) {
                throw new RuntimeException(e);
            }
            SpringApplication.run(App.class, args);
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
