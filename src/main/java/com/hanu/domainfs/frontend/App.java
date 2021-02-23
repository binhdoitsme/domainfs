package com.hanu.domainfs.frontend;

import java.util.*;

import com.hanu.domainfs.frontend.models.*;
import com.hanu.domainfs.frontend.models.ViewAPI.APICallMethod;
import com.hanu.domainfs.frontend.models.components.*;

public final class App {
    public static void main(String[] args) {
        final Map<String, Object> attrs = new HashMap<>();
        attrs.put("key", "id");
        attrs.put("onClick", "{handleClick}");
        ViewComponent button = ViewComponentGenerator.generate("button", attrs, "+ Create");
        System.out.println(button.toSourceCode());
        System.out.println();

        ViewComponent label = ViewComponentGenerator.generate("label", attrs, "+ Create");
        System.out.println(label.toSourceCode());
        System.out.println();

        // ViewComponent input = ViewComponentGenerator.generate("input", attrs, null, InputType.Type.NUMBER);
        // System.out.println(input.toSourceCode());
        // System.out.println();

        final Map<String, Object> initialStates = new TreeMap<>();
        initialStates.put("items", new Object[] {});
        initialStates.put("currentPage", "undefined");
        initialStates.put("pageCount", "undefined");
        // ViewClass viewCreateClass;
        System.out.println(new Modal("Title", Map.of(), List.of()).toSourceCode());



        SourceSegment getById = new APICallMethod("getById",
            new String[] { "id" }, "/students/{id}", false);
        SourceSegment create = new APICallMethod("create",
            new String[] { }, "/students", true);
        SourceSegment updateById = new APICallMethod("updateById",
            new String[] { "id" }, "/students", true);
        SourceSegment getByPage = new APICallMethod("getByPage", 
            new String[] { "page" }, "/students?page={page}", false);
        ViewAPI viewAPI = new ViewAPI("StudentAPI", 
            List.of(getById, create, updateById, getByPage));
        System.out.println(viewAPI.toSourceCode());
        
    }
}
