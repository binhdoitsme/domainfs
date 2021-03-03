package com.hanu.domainfs.frontend.generators;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.hanu.domainfs.frontend.models.ViewClass;
import com.hanu.domainfs.frontend.models.ViewComponent;
import com.hanu.domainfs.frontend.models.ViewLayout;
import com.hanu.domainfs.frontend.models.components.InputType;
import com.hanu.domainfs.frontend.models.components.ViewComponentGenerator;
import com.hanu.domainfs.utils.IdentifierUtils;
import com.hanu.domainfs.utils.InheritanceUtils;

import org.modeshape.common.text.Inflector;

import domainapp.basics.model.meta.DAssoc;
import domainapp.basics.model.meta.DAssoc.AssocEndType;
import domainapp.basics.model.meta.DAssoc.AssocType;
import domainapp.basics.model.meta.DAttr;

public final class ViewWithDetailFieldsGenerator {
    public enum Type {
        CREATE, UPDATE
    }
    private static final Inflector inflector = Inflector.getInstance();

    private static ViewWithDetailFieldsGenerator instance;
    public static ViewWithDetailFieldsGenerator getInstance() {
        if (instance == null) instance = new ViewWithDetailFieldsGenerator();
        return instance;
    }

    private ViewWithDetailFieldsGenerator() { }

    public ViewClass generate(Type type, Class<?> cls, boolean isReadonly) {
        Map<String, String> subtypes = InheritanceUtils.getSubtypeMapFor(cls);
        if (!subtypes.isEmpty()) {
            return generateWithTypeSelect(type, cls, isReadonly, subtypes);
        }

        List<ViewComponent> coreFormGroups = generateCoreFormGroups(cls, isReadonly);
        // TODO: 
        return null;
    }

    private List<ViewComponent> generateCoreFormGroups(Class<?> cls, boolean isReadonly) {
        Field[] declaredFields = cls.getDeclaredFields();
        final List<ViewComponent> formGroups = new ArrayList<>();
        for (Field field : declaredFields) {
            ViewComponent vc = generateFormGroupFor(field, false, isReadonly, true);
            if (vc != null) formGroups.add(vc);
        }
        return formGroups;
    }

    /**
     * @param isForeign whether this field is an identifier field from a referenced type (e.g. Student.id)
     * @param coreOnly whether to generate without or with ID
     */
    private ViewComponent generateFormGroupFor(Field field, boolean isForeign, 
            boolean isReadonly, boolean coreOnly) {
        final Class<?> fieldType = field.getType();
        final DAttr attrInfo = field.getAnnotation(DAttr.class);
        if (attrInfo == null) return null;
        if (coreOnly && attrInfo.id() && !isForeign) return null;
        if (coreOnly && !attrInfo.id() && attrInfo.auto()) return null;

        final DAssoc assocInfo = field.getAnnotation(DAssoc.class);
        if (assocInfo != null 
                && assocInfo.ascType() == AssocType.One2Many 
                && assocInfo.endType() == AssocEndType.Many) {
            return generateAssociatedFormGroup(field, isReadonly);
        }

        final InputType inputType = getInputTypeBy(fieldType);

        ViewComponent label = generateFormLabel(field, attrInfo, isForeign);
        ViewComponent input = generateInputFieldOf(field, attrInfo, inputType, isForeign, isReadonly);

        final ViewLayout formGroup = (ViewLayout) ViewComponentGenerator.generate("formGroup", Map.of());
        formGroup.add(label);
        formGroup.add(input);
        return formGroup;
    }

    private ViewComponent generateInputFieldOf(Field field, DAttr attrInfo,
            InputType inputType, boolean isForeign, boolean isReadonly) {
        final Map<String, Object> inputAttrs = new LinkedHashMap<>();

        if (!attrInfo.optional()) {
            inputAttrs.put("required", true);
        }

        if (attrInfo.auto() && !isForeign) {
            inputAttrs.put("disabled", true);
        }

        if (inputType == InputType.Type.TEXT) {
            inputAttrs.put("maxLength", attrInfo.length());
        } else if (inputType == InputType.Type.DECIMAL 
                || inputType == InputType.Type.NUMBER) {
            if (Double.isFinite(attrInfo.min())) inputAttrs.put("min", attrInfo.min());
            if (Double.isFinite(attrInfo.max())) inputAttrs.put("max", attrInfo.max());
            if (inputType == InputType.Type.DECIMAL) inputAttrs.put("step", 0.01); // Default hardcoding step value
        }

        String stateFieldName = 
            inflector.camelCase(inflector.underscore(field.getName()).toLowerCase(), false);
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
        final Class<?> fieldType = field.getType();
        final ViewComponent input = ViewComponentGenerator
            .generate("input", inputAttrs, inputType, fieldType, fieldType.getEnumConstants());

        return input;
    }

    private ViewComponent generateAssociatedFormGroup(Field field, boolean isReadonly) {
        Field idField = IdentifierUtils.getIdField(field.getType());
        // generate 2 sets of inputs: xxxId and xxx (representation)
        ViewLayout formGroup = (ViewLayout) ViewComponentGenerator.generate("formGroup", Map.of());
        formGroup.add(generateFormGroupFor(idField, true, isReadonly, true));

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

    private ViewComponent generateFormLabel(Field field, DAttr attrInfo, boolean isForeign) {
        final String labelStr = isForeign ? 
            inflector.humanize(
                inflector.underscore(field.getDeclaringClass().getSimpleName())
                    + " " + inflector.capitalize(attrInfo.name())).replace("id", "ID")
            : inflector.humanize(inflector.underscore(attrInfo.name()));

        final ViewComponent label = ViewComponentGenerator.generate(
            "label", Map.of(), labelStr + ":");
        
        return label;
    }

    private static InputType getInputTypeBy(Class<?> fieldType) {
        if (fieldType == String.class) {
            return InputType.Type.TEXT;
        } else if (fieldType == Integer.TYPE || fieldType == Integer.class
                || fieldType == Short.TYPE || fieldType == Short.class
                || fieldType == Byte.TYPE || fieldType == Byte.class
                || fieldType == Long.TYPE || fieldType == Long.class) {
            return InputType.Type.NUMBER;
        } else if (fieldType == Double.TYPE || fieldType == Double.class
                || fieldType == Float.TYPE || fieldType == Float.class) {
            return InputType.Type.DECIMAL;
        } else if (fieldType.isAssignableFrom(Date.class)) {
            return InputType.Type.DATE;
        } else if (fieldType == Boolean.class || fieldType == Boolean.TYPE) {
            // generate checkbox input
            return InputType.Type.CHECKBOX;
        } else if (fieldType.isEnum()) {
            // generate select/option input
            return InputType.Type.SELECT_OPTION;
        } else {
            return null;
        }
    }

    private static Map<String, Object> generateInitialStates(Class<?> cls) {
        // TODO: 
        return null;
    }

    private ViewClass generateWithTypeSelect(Type type, Class<?> cls,
            boolean isReadonly, Map<String, String> subtypes) {
        // TODO:
        return null;
    }
}
