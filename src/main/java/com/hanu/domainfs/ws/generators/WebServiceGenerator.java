package com.hanu.domainfs.ws.generators;

import java.util.List;
import java.util.stream.Stream;

import com.hanu.domainfs.ws.utils.ClassAssocUtils;
import com.hanu.domainfs.ws.utils.InheritanceUtils;

@SuppressWarnings({ "rawtypes" })
public class WebServiceGenerator {
    private static WebServiceGenerator INSTANCE;

    public static WebServiceGenerator instance() {
        if (INSTANCE == null) {
            INSTANCE = new WebServiceGenerator();
        }
        return INSTANCE;
    }

    private final WebControllerGenerator webControllerGenerator;
    private final ServiceTypeGenerator serviceTypeGenerator;
    private final AnnotationGenerator annotationGenerator;
    private Runnable generateCompleteCallback;

    public WebServiceGenerator() {
        this.webControllerGenerator = WebControllerGenerator.instance();
        this.serviceTypeGenerator = ServiceTypeGenerator.instance();
        this.annotationGenerator = AnnotationGenerator.instance();
    }

    public void setGenerateCompleteCallback(Runnable generateCompleteCallback) {
        this.generateCompleteCallback = generateCompleteCallback;
    }

    /**
     * Generate a simple RESTful Web Service from a number of domain classes.
     * @param classes
     */
    public void generateWebService(Class... classes) {
        List<Class<?>> ignored = getIgnoredClasses(classes);
        for (Class<?> cls : classes) {
            if (ignored.contains(cls)) continue;
            annotationGenerator.generateCircularAnnotations(cls, classes);
            annotationGenerator.generateInheritanceAnnotations(cls);
            serviceTypeGenerator.generateAutowiredServiceType(cls);
            webControllerGenerator.getRestfulController(cls);
            List<Class<?>> nestedClasses = ClassAssocUtils.getNested(cls);
            for (Class<?> nested : nestedClasses) {
                if (nested == cls) continue;
                webControllerGenerator.getNestedRestfulController(cls, nested);
            }
        }
        onGenerateComplete();
    }

    /**
     * Ignored classes are subclasses of others.
     */
    private static List<Class<?>> getIgnoredClasses(Class[] classes) {
        return Stream.of(classes).map(c -> InheritanceUtils.getSubtypesOf(c))
                .reduce((l1, l2) -> {
                    l1.addAll(l2);
                    return l1;
                }).orElse(List.of());
    }

    private void onGenerateComplete() {
        generateCompleteCallback.run();
    }
}
