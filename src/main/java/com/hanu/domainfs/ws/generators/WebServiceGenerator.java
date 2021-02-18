package com.hanu.domainfs.ws.generators;

import static net.bytebuddy.matcher.ElementMatchers.is;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hanu.domainfs.ws.utils.ClassAssocUtils;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;

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
    private Runnable generateCompleteCallback;

    public WebServiceGenerator() {
        this.webControllerGenerator = WebControllerGenerator.instance();
        this.serviceTypeGenerator = new ServiceTypeGenerator();
    }

    public void setGenerateCompleteCallback(Runnable generateCompleteCallback) {
        this.generateCompleteCallback = generateCompleteCallback;
    }

    private static String[] getIgnoredFields(Class<?>[] defined) {
        final List<String> result = new ArrayList<>();
        for (Class<?> cls : defined) {
            for (Field f : cls.getDeclaredFields()) {
                if (isDefinedTypeField(f)) {
                    result.add(f.getName());
                }
            }
        }
        return result.toArray(new String[result.size()]);
    }

    private static boolean isDefinedTypeField(Field f) {
        Class<?> type = f.getType();
        Class<?> declaringType = f.getDeclaringClass();
        String rootPackage = Stream
            .of(declaringType.getClassLoader().getDefinedPackages())
            .map(p -> p.getName())
            .filter(declaringType.getName()::contains)
            .sorted()
            .findFirst().orElse("");
        return type.getName().contains(rootPackage) && !type.isPrimitive();
    }

    private void generateCircularAnnotations(Class<?> cls, Class<?>[] defined) {
        ByteBuddyAgent.install();
        Builder<?> builder = new ByteBuddy().rebase(cls);
        final String[] ignoredFields = getIgnoredFields(defined);
        for (Field f : cls.getDeclaredFields()) {
            if (!isDefinedTypeField(f)) continue;
            builder = builder.field(is(f))
                .annotateField(AnnotationDescription.Builder
                    .ofType(JsonIgnoreProperties.class)
                    .defineArray("value", ignoredFields)
                    .build());
        }
        builder.make()
            .load(cls.getClassLoader(), ClassReloadingStrategy.fromInstalledAgent());
    }

    public void generateWebService(Class... classes) {
        for (Class<?> cls : classes) {
            generateCircularAnnotations(cls, classes);
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

    private void onGenerateComplete() {
        generateCompleteCallback.run();
    }
}
