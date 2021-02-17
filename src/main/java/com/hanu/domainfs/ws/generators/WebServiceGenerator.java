package com.hanu.domainfs.ws.generators;

import com.hanu.domainfs.ws.utils.ClassAssocUtils;

import java.util.List;

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

    public void generateWebService(Class... classes) {
        for (Class<?> cls : classes) {
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
