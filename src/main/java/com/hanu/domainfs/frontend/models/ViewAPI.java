package com.hanu.domainfs.frontend.models;

import java.util.LinkedList;
import java.util.List;

public class ViewAPI extends SourceImpl implements ClassComponent {
    private String name;
    private List<SourceSegment> methods;

    public ViewAPI(String name, List<SourceSegment> methods) {
        this.name = name;
        this.methods = new LinkedList<>(methods);
    }

    @Override
    public String getTarget() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getSuperClass() {
        return null;
    }

    @Override
    public List<SourceSegment> getImportStatements() {
        return List.of(
            new ImportStatement("../commons/APIUtils", (Object)"toBackend"));
    }

    @Override
    public List<SourceSegment> getMethods() {
        return this.methods;
    }
    
    public static class APICallMethod extends InstanceMethod {

        public APICallMethod(String name, String[] paramNames,
                             String destination, boolean hasBody) {
            super(name, actualParams(paramNames, destination, hasBody));
        }

        @Override
        public SourceSegment getBody() {
            return new MethodCall(
                "makeRequest", "toBackend", (Object[]) getParamNames());
        }

        @Override
        public ImplementationStrategy getImplementationStrategy() {
            return this;
        }

        private static String[] actualParams(String[] params, String destination, boolean hasBody) {
            String[] newParams;
            if (hasBody) newParams = paramsWithSuccessHandling(paramsWithBody(params));
            else newParams = paramsWithSuccessHandling(params);
            List<String> newParamList = new LinkedList<>();
            newParamList.add("\"" + destination + "\"");
            for (String param : newParams) {
                newParamList.add(param);
            } 
            return newParamList.toArray(new String[newParamList.size()]);
        }

        private static String[] paramsWithSuccessHandling(String[] originalParams) {
            final int originalLength = originalParams.length;
            String[] paramNames = new String[originalLength + 2];
            for (int i = 0; i < originalLength; i++) {
                paramNames[i] = originalParams[i];
            }
            paramNames[originalLength] = "onSuccess";
            paramNames[originalLength + 1] = "onFailure";
            return paramNames;
        }

        private static String[] paramsWithBody(String[] originalParams) {
            final int originalLength = originalParams.length;
            String[] paramNames = new String[originalLength + 1];
            for (int i = 0; i < originalLength; i++) {
                paramNames[i] = originalParams[i];
            }
            paramNames[originalLength] = "data";
            return paramNames;
        }

        
    }
}
