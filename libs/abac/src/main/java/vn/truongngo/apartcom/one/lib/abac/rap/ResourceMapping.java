package vn.truongngo.apartcom.one.lib.abac.rap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps a controller method to a semantic resource and action for ABAC enforcement.
 * Use alongside {@code @PreEnforce} or {@code @PostEnforce}.
 *
 * <pre>{@code
 * @GetMapping("/users/{id}")
 * @ResourceMapping(resource = "user", action = "READ")
 * @PreEnforce
 * public ResponseEntity<UserDetail> getUserById(...) { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourceMapping {
    String resource();
    String action();
}
