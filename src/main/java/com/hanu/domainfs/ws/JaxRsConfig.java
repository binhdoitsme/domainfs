package com.hanu.domainfs.ws;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.ConstructorDetector;
import com.hanu.domainfs.utils.PackageUtils;
import com.hanu.domainfs.ws.svcdesc.ServiceDescriptionController;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jackson.internal.DefaultJacksonJaxbJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;
import java.text.SimpleDateFormat;

@ApplicationPath("")
public class JaxRsConfig extends ResourceConfig {
    public JaxRsConfig() {
        packages(PackageUtils.basePackageOf(JaxRsApp.resourceClasses.get(0)));
        for (Class<?> cls : JaxRsApp.resourceClasses) {
            register(cls);
        }
        register(ServiceDescriptionController.class);
        register(MyObjectMapperProvider.class); // No need to register this provider if no special configuration is required.
        var provider = new DefaultJacksonJaxbJsonProvider();
        provider.setMapper(createDefaultMapper());
        register(provider);
        register(JacksonFeature.class);
    }

    @Provider
    public class MyObjectMapperProvider implements ContextResolver<ObjectMapper> {

        final ObjectMapper defaultObjectMapper;

        public MyObjectMapperProvider() {
            defaultObjectMapper = createDefaultMapper();
        }

        @Override
        public ObjectMapper getContext(Class<?> type) {
            return defaultObjectMapper;
        }
    }

    static ObjectMapper createDefaultMapper() {
        return new ObjectMapper()
            .setDateFormat(new SimpleDateFormat("dd-MM-yyyy"))
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setConstructorDetector(ConstructorDetector.USE_PROPERTIES_BASED);
    }

}