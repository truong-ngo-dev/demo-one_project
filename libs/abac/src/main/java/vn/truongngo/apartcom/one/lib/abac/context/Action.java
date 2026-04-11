package vn.truongngo.apartcom.one.lib.abac.context;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents an action in the ABAC system.
 * @author Truong Ngo
 */
@Data
public class Action {

    private HttpRequest request;
    private Map<String, Object> attributes;

    public Action(HttpServletRequest httpRequest) {
        request = HttpRequest.parse(httpRequest);
        attributes = new LinkedHashMap<>();
    }

    public Action() {
        this.attributes = new LinkedHashMap<>();
    }

    public static Action semantic(String actionName) {
        Action action = new Action();
        action.addAttribute("name", actionName);
        return action;
    }

    public void addAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Object getAttribute(String attributeName) {
        return attributes.get(attributeName);
    }
}
