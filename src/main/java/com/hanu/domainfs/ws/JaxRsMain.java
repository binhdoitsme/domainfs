package com.hanu.domainfs.ws;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.hanu.domainfs.examples.services.coursemodule.model.CompulsoryModule;
import com.hanu.domainfs.examples.services.coursemodule.model.CourseModule;
import com.hanu.domainfs.examples.services.coursemodule.model.ElectiveModule;
import com.hanu.domainfs.examples.services.enrolment.model.Enrolment;
import com.hanu.domainfs.examples.services.sclass.model.SClass;
import com.hanu.domainfs.examples.services.student.model.City;
import com.hanu.domainfs.examples.services.student.model.Student;
import com.hanu.domainfs.utils.InheritanceUtils;
import com.hanu.domainfs.ws.generators.WebServiceGenerator;
import com.hanu.domainfs.ws.generators.annotations.bridges.TargetType;
import com.hanu.domainfs.ws.generators.controllers.ServiceRegistry;
import com.hanu.domainfs.ws.generators.services.CrudService;
import domainapp.basics.exceptions.DataSourceException;
import domainapp.basics.exceptions.NotFoundException;
import domainapp.basics.exceptions.NotPossibleException;
import domainapp.software.SoftwareFactory;
import domainapp.softwareimpl.SoftwareImpl;
import org.apache.logging.log4j.LogManager;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JaxRsMain {
    static List<Class> resourceClasses = new LinkedList<>();

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

    private static SoftwareImpl sw;

    private static CrudService<?> createDefault(Class<CrudService> cls,
                                                SoftwareImpl arg0) {
        if (cls.getConstructors().length < 2) {
            String className = ((Qualifier)cls.getConstructors()[0].getParameterAnnotations()[1][0]).value();
            try {
                return createInherited(cls, arg0, InheritanceUtils.getSubtypeMapFor(Class.forName(className)));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            return cls.getDeclaredConstructor(SoftwareImpl.class)
                .newInstance(arg0);
        } catch (NoSuchMethodException | InvocationTargetException
                | InstantiationException | IllegalAccessException ex) {

            throw new RuntimeException(ex);
        }
    }

    private static CrudService<?> createInherited(Class<CrudService> cls,
                                                  SoftwareImpl arg0,
                                                  Map<String, String> arg1) {
        try {
            return (CrudService<?>) cls.getConstructors()[0].newInstance(arg0, arg1);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        System.out.println("------------");

        WebServiceGenerator generator = new WebServiceGenerator(TargetType.JAX_RS);
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

            generator.getGeneratedServiceClasses().forEach((k, v) -> {
                registry.put(k, createDefault(v, sw));
            });
            resourceClasses.addAll(generator.getGeneratedControllerClasses());

            startJettyServer();
        });
        generator.generateWebService(model);
        System.out.println("------------");
    }
    static {
        ((Logger) LoggerFactory.getLogger("org.eclipse.jetty")).setLevel(Level.INFO);
    }

    static void startJettyServer() {
        System.setProperty("rootLogger.level", "INFO");
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8080);
        server.setConnectors(new Connector[] { connector });

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        ServletHolder jerseyServlet = context.addServlet(ServletContainer.class.getCanonicalName(), "/*");
        jerseyServlet.setInitOrder(1);
        jerseyServlet.setInitParameter("jakarta.ws.rs.Application", JaxRsApp.class.getCanonicalName());
        jerseyServlet.setInitParameter(ServerProperties.PROVIDER_CLASSNAMES,
            JaxRsApp.class.getCanonicalName() + ";" + JaxRsApp.MyObjectMapperProvider.class.getCanonicalName());
        jerseyServlet.setInitParameter(ServerProperties.MOXY_JSON_FEATURE_DISABLE, "true");
        jerseyServlet.setInitParameter(ServerProperties.PROVIDER_PACKAGES, "com.hanu.domainfs.examples.services");
        try {
            server.start();
            server.join();
        } catch (Exception ex) {
        } finally {
            server.destroy();
        }
    }
}
