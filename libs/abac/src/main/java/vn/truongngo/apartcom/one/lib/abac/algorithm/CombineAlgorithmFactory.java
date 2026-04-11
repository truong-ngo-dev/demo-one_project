package vn.truongngo.apartcom.one.lib.abac.algorithm;

import vn.truongngo.apartcom.one.lib.abac.domain.AbstractPolicy;
import vn.truongngo.apartcom.one.lib.abac.domain.Principle;
import vn.truongngo.apartcom.one.lib.abac.domain.Rule;

/**
 * Factory class for instantiating CombineAlgorithm instances.
 * @author Truong Ngo
 */
public class CombineAlgorithmFactory {

    public static <E extends Principle> CombineAlgorithm<E> from(CombineAlgorithmName name, Class<E> clazz) {
        return switch (name) {
            case DENY_OVERRIDES -> new DenyOverridesCombineAlgorithm<>();
            case PERMIT_OVERRIDES -> new PermitOverridesCombineAlgorithm<>();
            case DENY_UNLESS_PERMIT -> new DenyUnlessPermitCombineAlgorithm<>();
            case PERMIT_UNLESS_DENY -> new PermitUnlessDenyCombineAlgorithm<>();
            case FIRST_APPLICABLE -> new FirstApplicableCombineAlgorithm<>();
            case ONLY_ONE_APPLICABLE -> getOnlyOneCombineAlg(clazz);
        };
    }

    private static <E extends Principle> CombineAlgorithm<E> getOnlyOneCombineAlg(Class<E> clazz) {
        if (clazz == AbstractPolicy.class) {
            return new OnlyOneApplicableCombineAlgorithm<>();
        } else {
            throw new IllegalArgumentException("Only one applicable combine algorithm is not available for Rule.");
        }
    }
}
