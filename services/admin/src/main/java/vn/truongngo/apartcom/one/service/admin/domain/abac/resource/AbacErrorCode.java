package vn.truongngo.apartcom.one.service.admin.domain.abac.resource;

import vn.truongngo.apartcom.one.lib.common.domain.exception.ErrorCode;

public enum AbacErrorCode implements ErrorCode {

    RESOURCE_NOT_FOUND("30001", "Resource not found", "error.abac.resource.not_found", 404),
    RESOURCE_NAME_DUPLICATE("30002", "Resource name already exists", "error.abac.resource.name_duplicate", 409),
    RESOURCE_IN_USE("30003", "Resource is referenced by policies or UI elements", "error.abac.resource.in_use", 409),
    ACTION_NOT_FOUND("30004", "Action not found", "error.abac.action.not_found", 404),
    ACTION_NAME_DUPLICATE("30005", "Action name already exists in this resource", "error.abac.action.name_duplicate", 409),
    ACTION_IN_USE("30006", "Action is referenced by UI elements", "error.abac.action.in_use", 409),
    POLICY_SET_NOT_FOUND("30007", "Policy set not found", "error.abac.policy_set.not_found", 404),
    POLICY_SET_NAME_DUPLICATE("30008", "Policy set name already exists", "error.abac.policy_set.name_duplicate", 409),
    POLICY_NOT_FOUND("30009", "Policy not found", "error.abac.policy.not_found", 404),
    RULE_NOT_FOUND("30010", "Rule not found", "error.abac.rule.not_found", 404),
    INVALID_SPEL_EXPRESSION("30011", "Invalid SpEL expression", "error.abac.invalid_spel", 400),
    UI_ELEMENT_NOT_FOUND("30012", "UI element not found", "error.abac.ui_element.not_found", 404),
    UI_ELEMENT_ID_DUPLICATE("30013", "UI element ID already exists", "error.abac.ui_element.id_duplicate", 409);

    private final String code;
    private final String defaultMessage;
    private final String messageKey;
    private final int httpStatus;

    AbacErrorCode(String code, String defaultMessage, String messageKey, int httpStatus) {
        this.code           = code;
        this.defaultMessage = defaultMessage;
        this.messageKey     = messageKey;
        this.httpStatus     = httpStatus;
    }

    @Override public String code()           { return code; }
    @Override public String defaultMessage() { return defaultMessage; }
    @Override public String messageKey()     { return messageKey; }
    @Override public int httpStatus()        { return httpStatus; }
}
