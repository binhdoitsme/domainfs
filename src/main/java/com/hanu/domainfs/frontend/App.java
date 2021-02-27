package com.hanu.domainfs.frontend;

import java.lang.reflect.Modifier;
import java.lang.reflect.Field;
import java.util.*;

import com.hanu.domainfs.examples.services.student.model.Student;
import com.hanu.domainfs.frontend.models.*;
import com.hanu.domainfs.frontend.models.ViewAPI.APICallMethod;
import com.hanu.domainfs.frontend.models.components.*;

import org.modeshape.common.text.Inflector;

import domainapp.basics.model.meta.DAttr;

public final class App {

    static ViewComponent generateInputFormGroup(Field field) {
        final Class<?> fieldType = field.getType();
        final DAttr attrInfo = field.getAnnotation(DAttr.class);
        if (attrInfo == null) return null;

        final ViewComponent label = ViewComponentGenerator.generate(
            "label", Map.of(), Inflector.getInstance().humanize(attrInfo.name()));
        
        final Map<String, Object> inputAttrs = new LinkedHashMap<>();

        if (!attrInfo.optional()) {
            inputAttrs.put("required", true);
        }

        InputType inputType;
        if (fieldType == String.class) {
            inputType = InputType.Type.TEXT;
            inputAttrs.put("maxLength", attrInfo.length());
        } else if (fieldType == Integer.TYPE || fieldType == Integer.class
                || fieldType == Short.TYPE || fieldType == Short.class
                || fieldType == Byte.TYPE || fieldType == Byte.class
                || fieldType == Long.TYPE || fieldType == Long.class) {
            inputType = InputType.Type.NUMBER;
            inputAttrs.put("min", attrInfo.min());
            inputAttrs.put("max", attrInfo.max());
        } else if (fieldType == Double.TYPE || fieldType == Double.class
                || fieldType == Float.TYPE || fieldType == Float.class) {
            inputType = InputType.Type.NUMBER;
            inputAttrs.put("min", attrInfo.min());
            inputAttrs.put("max", attrInfo.max());
            inputAttrs.put("step", 0.01); // Default hardcoding step value
        } else if (fieldType == Date.class) {
            inputType = InputType.Type.DATE;
        } else if (fieldType == Boolean.class || fieldType == Boolean.TYPE) {
            // not yet supported
            return null;
        } else {
            return null;
        }
        inputAttrs.put("value", "{" + field.getName() + "}");

        final ViewComponent input = ViewComponentGenerator.generate("input", inputAttrs, inputType);

        final ViewLayout formGroup = (ViewLayout) ViewComponentGenerator.generate("formGroup", Map.of());
        formGroup.add(label);
        formGroup.add(input);
        return formGroup;
    }

    static Map<String, Object> initialStatesFrom(Field[] fields) {
        final Map<String, Object> initialStates = new LinkedHashMap<>();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            Class<?> fieldType = field.getType();
            if (fieldType == String.class) {
                initialStates.put(field.getName(), "");
            } else if (Number.class.isAssignableFrom(fieldType)) {
                initialStates.put(field.getName(), 0);
            } else if (fieldType == Boolean.TYPE || fieldType == Boolean.class) {
                initialStates.put(field.getName(), false);
            } else {
                initialStates.put(field.getName(), "undefined");
            }
        }
        return initialStates;
    }

    public static void main(String[] args) {
        final Map<String, Object> attrs = new HashMap<>();
        attrs.put("key", "id");
        attrs.put("onClick", "{this.handleShow}");
        ViewComponent button = ViewComponentGenerator.generate("button", attrs, "+ Create");

        Field[] declaredFields = Student.class.getDeclaredFields();
        final Map<String, Object> initialStates = initialStatesFrom(declaredFields);
        // modal show/hide support
        initialStates.put("modalShowing", false);
        final Map<String, Object> submittingStates = new TreeMap<>();
        for (String key : initialStates.keySet()) {
            if (key.equals("modalShowing")) continue;
            submittingStates.put(key, "this.state." + key);
        }
        // ViewClass viewCreateClass;
        ViewLayout modal = new Modal.Builder()
                .withAttributes(Map.of(
                    "show", "{this.state.modalShowing}",
                    "onHide", "{this.handleHide}"))
                .withTitle("Create new student")
                .withFooterItems(
                    ViewComponentGenerator.generate("button", Map.of("onClick", "{this.handleHide}", "variant", "secondary"), "Cancel"),
                    ViewComponentGenerator.generate("button", Map.of("onClick", "{this.handleSubmit}"), "Submit")
                ).build();
        
        SourceSegment resetState = new StateResetMethod(initialStates);
        SourceSegment handleShowModal = new DefaultInstanceMethod("handleShow", new String[] {}, 
                new StatementList(
                    new MethodCall("setState", "this", Map.of("modalShowing", true))
                ));
        SourceSegment handleHideModal = new DefaultInstanceMethod("handleHide", new String[] {}, 
                new StatementList(
                    new MethodCall("setState", "this", Map.of("modalShowing", false))
                ));
        SourceSegment getSubmitBody = new DefaultInstanceMethod("getSubmitBody", new String[] {}, 
                new ReturnStatement(new ObjectRep(submittingStates)));
        SourceSegment handleSubmit = new DefaultInstanceMethod("handleSubmit", new String[] {}, 
                new StatementList(
                    new MethodCall("handleSubmit", "this.props", "this.getSubmitBody()", "this.props.onSuccess", "this.props.onFailure")
                ));

        SourceSegment reactImport = new ImportStatement("react", (Object)"React", "Component");
        SourceSegment reactBootstrapImport = new ImportStatement("react-bootstrap", (Object)"", "Button", "Form", "FormControl", "FormGroup", "Modal");
        
        ViewClass viewCreateClass = new ViewCreate(
            "StudentCreate",
            "React.Component", 
            initialStates, 
            List.of(
                resetState,
                handleShowModal,
                handleHideModal,
                getSubmitBody,
                handleSubmit
            ), List.of(
                reactImport,
                reactBootstrapImport
            )
        );
        ViewLayout form = (ViewLayout) ViewComponentGenerator.generate("form", null);
        
        for (Field declaredField : declaredFields) {
            final ViewComponent formGroup = generateInputFormGroup(declaredField);
            if (formGroup == null) continue;
            form.add(formGroup);
        }

        modal.add(form);
        viewCreateClass.add(button);
        viewCreateClass.add(modal);
        System.out.println(viewCreateClass.toSourceCode());



        // SourceSegment getById = new APICallMethod("getById",
        //     new String[] { "id" }, "/students/{id}", false);
        // SourceSegment create = new APICallMethod("create",
        //     new String[] { }, "/students", true);
        // SourceSegment updateById = new APICallMethod("updateById",
        //     new String[] { "id" }, "/students", true);
        // SourceSegment getByPage = new APICallMethod("getByPage", 
        //     new String[] { "page" }, "/students?page={page}", false);
        // ViewAPI viewAPI = new ViewAPI("StudentAPI", 
        //     List.of(getById, create, updateById, getByPage));
        // System.out.println(viewAPI.toSourceCode());
        
    }
}
