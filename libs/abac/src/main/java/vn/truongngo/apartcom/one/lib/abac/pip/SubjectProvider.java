package vn.truongngo.apartcom.one.lib.abac.pip;

import vn.truongngo.apartcom.one.lib.abac.context.Subject;

import java.security.Principal;

/**
 * Interface for providing a Subject based on a given Principal.
 * @author Truong Ngo
 */
public interface SubjectProvider {

    Subject getSubject(Principal principal);
}
