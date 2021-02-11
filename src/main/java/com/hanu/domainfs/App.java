package com.hanu.domainfs;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import com.hanu.domainfs.ws.examples.services.enrolment.model.Enrolment;
import com.hanu.domainfs.ws.generators.WebControllerGenerator;
import com.hanu.domainfs.ws.generators.controllers.RestfulController;
import com.hanu.domainfs.ws.generators.services.AbstractCrudService;
import com.hanu.domainfs.ws.generators.services.CrudService;

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
@SuppressWarnings({ "unchecked" })
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
        Class<RestfulController<Enrolment, Integer>> clazz = 
            WebControllerGenerator.instance()
                .getRestfulController(Enrolment.class, Integer.class);
            
        for (Object o : clazz.getAnnotations()) {
            System.out.println("....." + o);
        }
        System.out.println();

        for (Method o : clazz.getDeclaredMethods()) {
            System.out.print("...");
            System.out.print(Arrays.toString(o.getAnnotations()));
            System.out.println(o);
        }
        System.out.println();

        Arrays.asList(clazz.getInterfaces()).forEach(x -> System.out.println("..." + x));
        System.out.println("Constructors: ");
        // for (Constructor<?> o : clazz.getDeclaredConstructors()) {
        //     System.out.println("..." + o.getDeclaringClass().getSimpleName() + Arrays.toString(o.getParameterTypes()));
        //     System.out.println(Arrays.toString(o.getAnnotations()));
        //     // System.out.println("..." + o.getType().getName() + " " + o.getName());
        // }
        // for (Method o : clazz.getDeclaredMethods()) {
        //     System.out.println(o);
        // }

        for (Object f : clazz.getDeclaredConstructors()) {
            System.out.println("..." + f);
        }

        System.out.println();
        System.out.println(clazz.getPackageName());

        CrudService<Enrolment, Integer> enrolmentService = new AbstractCrudService<>(Enrolment.class, sw);
        
        RestfulController<Enrolment, Integer> controller = 
            (RestfulController<Enrolment, Integer>) clazz.getConstructors()[0].newInstance(enrolmentService);

        // Page<Enrolment> enrolments = controller.getEntityListByPage(1, 20);

        // City c = new City("cityName");
        // Student s = new Student("name", Gender.Male, Date.from(Instant.now()), c, "email");
        // CourseModule m = new ElectiveModule("deptName", "name", 1, 3);
        // Enrolment toCreate = new Enrolment(s, m);
        // Enrolment e = controller.createEntity(toCreate);
        // System.out.println("Created enrolment: " + e);

        System.out.println("------------");
        SpringApplication.run(App.class, args);
    }

    @Configuration
    public static class BeanConfig {
        @Bean
        public CrudService<Enrolment, Integer> get() {
            return new AbstractCrudService<>(Enrolment.class, sw);
        }
    }
}
