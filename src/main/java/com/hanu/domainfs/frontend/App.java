package com.hanu.domainfs.frontend;

import java.lang.reflect.Modifier;
import java.lang.reflect.Field;
import java.util.*;

import com.hanu.domainfs.examples.services.enrolment.model.Enrolment;
import com.hanu.domainfs.examples.services.student.model.Gender;
import com.hanu.domainfs.examples.services.student.model.Student;
import com.hanu.domainfs.frontend.models.*;
import com.hanu.domainfs.frontend.models.ViewAPI.APICallMethod;
import com.hanu.domainfs.frontend.models.components.*;
import com.hanu.domainfs.utils.IdentifierUtils;

import org.modeshape.common.text.Inflector;

import domainapp.basics.model.meta.DAssoc;
import domainapp.basics.model.meta.DAttr;
import domainapp.basics.model.meta.DAssoc.AssocEndType;
import domainapp.basics.model.meta.DAssoc.AssocType;

public final class App {

    static ViewComponent generateInputFormGroup(Field field, boolean isForeign) {
        final Class<?> fieldType = field.getType();
        final DAttr attrInfo = field.getAnnotation(DAttr.class);
        if (attrInfo == null) return null;
        if (attrInfo.id() && !isForeign) return null;
        if (!attrInfo.id() && attrInfo.auto()) return null;

        final DAssoc assocInfo = field.getAnnotation(DAssoc.class);
        if (assocInfo != null 
                && assocInfo.ascType() == AssocType.One2Many 
                && assocInfo.endType() == AssocEndType.Many) {
            return generateAssociatedFormGroup(field);
        }
        final Inflector inflector = Inflector.getInstance();
        final String labelStr = isForeign ? 
            inflector.humanize(
                inflector.underscore(field.getDeclaringClass().getSimpleName())
                    + " " + inflector.capitalize(attrInfo.name())).replace("id", "ID")
            : inflector.humanize(inflector.underscore(attrInfo.name()));

        final ViewComponent label = ViewComponentGenerator.generate(
            "label", Map.of(), labelStr + ":");
        
        final Map<String, Object> inputAttrs = new LinkedHashMap<>();

        if (!attrInfo.optional()) {
            inputAttrs.put("required", true);
        }

        if (attrInfo.auto() && !isForeign) {
            inputAttrs.put("disabled", true);
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
            if (Double.isFinite(attrInfo.min())) inputAttrs.put("min", attrInfo.min());
            if (Double.isFinite(attrInfo.max())) inputAttrs.put("max", attrInfo.max());
        } else if (fieldType == Double.TYPE || fieldType == Double.class
                || fieldType == Float.TYPE || fieldType == Float.class) {
            inputType = InputType.Type.NUMBER;
            if (Double.isFinite(attrInfo.min())) inputAttrs.put("min", attrInfo.min());
            if (Double.isFinite(attrInfo.max())) inputAttrs.put("max", attrInfo.max());
            inputAttrs.put("step", 0.01); // Default hardcoding step value
        } else if (fieldType == Date.class) {
            inputType = InputType.Type.DATE;
        } else if (fieldType == Boolean.class || fieldType == Boolean.TYPE) {
            // generate checkbox input
            inputType = InputType.Type.CHECKBOX;
        } else if (fieldType.isEnum()) {
            // generate select/option input
            inputType = InputType.Type.SELECT_OPTION;
        } else {
            return null;
        }

        String stateFieldName = 
            inflector.camelCase(labelStr.replace(" ", "_").toLowerCase(), false);
        String valuePlace;
        
        if (!inputAttrs.containsKey("disabled")) {
            valuePlace = "{this.state." + stateFieldName + "}";
            inputAttrs.put("onChange", "{(e) => this.handleSingleStateChange(\"" 
                + stateFieldName + "\", e.target.value, " + (isForeign ? "true" : "false") 
                + ")}");
        } else {
            valuePlace = "{this.props." + stateFieldName + "}";
        }
        
        inputAttrs.put("value", valuePlace);
        final ViewComponent input = ViewComponentGenerator
            .generate("input", inputAttrs, inputType, fieldType, fieldType.getEnumConstants());

        final ViewLayout formGroup = (ViewLayout) ViewComponentGenerator.generate("formGroup", Map.of());
        formGroup.add(label);
        formGroup.add(input);
        return formGroup;
    }

    static ViewComponent generateInputFormGroup(Field field) {
        return generateInputFormGroup(field, false);
    }

    static ViewComponent generateAssociatedFormGroup(Field field) {
        Field idField = IdentifierUtils.getIdField(field.getType());
        // generate 2 sets of inputs: xxxId and xxx (representation)
        ViewLayout formGroup = (ViewLayout) ViewComponentGenerator.generate("formGroup", Map.of());
        formGroup.add(generateInputFormGroup(idField, true));

        final Inflector inflector = Inflector.getInstance();
        final String label = inflector.humanize(
            inflector.underscore(idField.getDeclaringClass().getSimpleName())) + " details:";
        ViewComponent detailsLabel = ViewComponentGenerator.generate("label", Map.of(), label);
        ViewComponent detailsInput = ViewComponentGenerator
            .generate("input", Map.of("disabled", true), InputType.Type.TEXT);

        formGroup.add(detailsLabel);
        formGroup.add(detailsInput);

        return formGroup;
    }

    static Map<String, Object> initialStatesFrom(Field[] fields) {
        final Map<String, Object> initialStates = new LinkedHashMap<>();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            DAssoc assocInfo = field.getAnnotation(DAssoc.class);
            if (assocInfo != null && assocInfo.endType() == AssocEndType.Many) {
                Field idField = IdentifierUtils.getIdField(field.getType());
                addInitialStateFromField(idField, initialStates, true);
            }
            if (field.getAnnotation(DAttr.class) == null) continue;
            if (field.getAnnotation(DAttr.class).auto()) continue;
            addInitialStateFromField(field, initialStates, false);
        }
        return initialStates;
    }

    private static void addInitialStateFromField(Field field,
            Map<String, Object> initialStates, boolean isForeign) {
        Class<?> fieldType = field.getType();
        final DAttr attrInfo = field.getAnnotation(DAttr.class);
        final Inflector inflector = Inflector.getInstance();
        final String fieldName = isForeign ? 
            inflector.camelCase(field.getDeclaringClass().getSimpleName() + inflector.capitalize(attrInfo.name()), false)
            : attrInfo.name();
        if (fieldType == String.class) {
            initialStates.put(fieldName, "\"\"");
        } else if (Number.class.isAssignableFrom(fieldType)) {
            initialStates.put(fieldName, 0);
        } else if (fieldType == Boolean.TYPE || fieldType == Boolean.class) {
            initialStates.put(fieldName, false);
        } else {
            initialStates.put(fieldName, "undefined");
        }
    }

    public static void main(String[] args) {
        final Map<String, Object> attrs = new HashMap<>();
        attrs.put("key", "id");
        attrs.put("onClick", "{this.handleShow}");
        ViewComponent button = ViewComponentGenerator.generate("button", attrs, "+ Create");

        final Class<?> cls = Enrolment.class;
        final Field[] declaredFields = cls.getDeclaredFields();

        final Inflector inflector = Inflector.getInstance();
        final String formLabel = "Create new " + inflector.humanize(inflector.underscore(cls.getSimpleName()));

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
                .withTitle(formLabel)
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

        // retrieveObjectById("student", 1801040022, onSuccess, onFailure)
        SourceSegment retrieveObjectById = new DefaultInstanceMethod("retrieveObjectById", 
                new String[] {
                    "name",
                    "id",
                    "onSuccess",
                    "onFailure"
                }, 
                new StatementList(
                    new StringBasedSourceSegment("this.props[name + \"API\"].getById(id, onSuccess, onFailure);")
                ));
        // handleStateChange("studentId", e.target.value, needsApiCall)
        SourceSegment handleStateChange = new DefaultInstanceMethod("handleSingleStateChange", 
                new String[] {
                    "stateName",
                    "newValue",
                    "needsApiCall"
                },
                new StatementList(
                    new StringBasedSourceSegment("let newState = {};"),
                    new StringBasedSourceSegment("newState[stateName] = newValue;"),
                    new StringBasedSourceSegment("if (needsApiCall) {"
                        + "\n  const stateObjName = stateName.replace(\"Id\", \"\");" 
                        + "\n  this.retrieveObjectById(stateObjName, newValue,"
                        + "\n    (result) => { newState[stateObjName] = result; this.setState(newState); });"),
                    new StringBasedSourceSegment("} else {"),
                    new StringBasedSourceSegment("  this.setState(newState);"),
                    new StringBasedSourceSegment("}")
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
                retrieveObjectById,
                handleStateChange,
                getSubmitBody,
                handleSubmit
            ), List.of(
                reactImport,
                reactBootstrapImport
            )
        );
        ViewLayout form = (ViewLayout) ViewComponentGenerator.generate("form", null);
        
        for (Field declaredField : declaredFields) {
            if (Modifier.isStatic(declaredField.getModifiers())) continue;
            final ViewComponent formGroup = generateInputFormGroup(declaredField);
            if (formGroup == null) continue;
            form.add(formGroup);
        }

        modal.add(form);
        viewCreateClass.add(button);
        viewCreateClass.add(modal);

        viewCreateClass.setTarget("/Volumes/Data/Resources/Study/java/courseman-react/src/ViewClass.js");
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
