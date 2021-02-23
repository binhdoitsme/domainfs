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
    public List<SourceSegment> getImportStatements() {
        return List.of(
            new StringBasedSourceSegment("import toBackend from '../commons/APIUtils';"));
    }

    @Override
    public List<SourceSegment> getMethods() {
        return this.methods;
    }
    
    public static class APICallMethod extends InstanceMethod {

        private final String endpointPlaceholder;

        public APICallMethod(String name, String[] paramNames,
                             String endpointPlaceholder, boolean hasBody) {
            super(name, actualParams(paramNames, hasBody));
            this.endpointPlaceholder = endpointPlaceholder;
        }

        @Override
        public SourceSegment getBody() {
            String paramStr = String.join(", ", getParamNames());
            return new StringBasedSourceSegment(
                String.format("toBackend.makeRequest(%s, %s);",
                    "\"" + endpointPlaceholder + "\"", paramStr));
        }

        @Override
        public ImplementationStrategy getImplementationStrategy() {
            return this;
        }

        private static String[] actualParams(String[] params, boolean hasBody) {
            if (hasBody) return paramsWithSuccessHandling(paramsWithBody(params));
            return paramsWithSuccessHandling(params);
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
