package com.hanu.domainfs.frontend.models;

import java.util.LinkedList;
import java.util.List;

public class ViewAPI extends SourceImpl {
    private String name;
    private List<APICallMethod> methods;

    public ViewAPI(String name, List<APICallMethod> methods) {
        this.name = name;
        this.methods = new LinkedList<>(methods);
    }

    @Override
    public String getTarget() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ImplementationStrategy getImplementationStrategy() {
        return new Implementation();
    }

    private class Implementation implements ImplementationStrategy {
        @Override
        public String implement(SourceSegment src) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("import * from '../commons/APIUtils';\n");
            stringBuilder.append("export default class ").append(name).append(" {\n");
            for (APICallMethod method : methods) {
                stringBuilder.append("\s\s").append(method.toSourceCode()).append("\n");
            }
            stringBuilder.append("};");
            return stringBuilder.toString();
        }
    }
    
    public static class APICallMethod implements SourceSegment {
        
        private static final String TEMPLATE = 
            "%s(%sonSuccess, onFailure) { makeRequest(%s, %sonSuccess, onFailure); }";

        private final String name;
        private final String[] paramNames;
        private final String endpointPlaceholder;

        public APICallMethod(String name, String[] paramNames,
                             String endpointPlaceholder, boolean hasBody) {
            this.name = name;
            this.endpointPlaceholder = endpointPlaceholder;
            if (hasBody) {
                this.paramNames = paramsWithBody(paramNames);
            } else {
                this.paramNames = paramNames;
            }
        }

        @Override
        public ImplementationStrategy getImplementationStrategy() {
            return new Implementation();
        }

        @Override
        public String toSourceCode() {
            return getImplementationStrategy().implement(this);   
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

        private final class Implementation implements ImplementationStrategy {

            @Override
            public String implement(SourceSegment src) {
                String paramStr = String.join(", ", paramNames);
                paramStr = paramStr.isEmpty() ? "" : paramStr + ", ";
                return String.format(TEMPLATE, name,
                    paramStr,
                    "\"" + endpointPlaceholder + "\"", paramStr);
            }
            
        }        
    }
}
