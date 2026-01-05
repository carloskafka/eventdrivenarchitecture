package br.com.backend.model.payment;

/**
 * Possible states of a payment and transition rules between them.
 */
public enum PaymentStatus {

    CREATED {
        @Override
        boolean canTransition(PaymentStatus target) {
            return target == AUTHORIZED || target == FAILED;
        }
    },
    AUTHORIZED {
        @Override
        boolean canTransition(PaymentStatus target) {
            return target == APPROVED || target == FAILED;
        }
    },
    APPROVED {
        @Override
        boolean canTransition(PaymentStatus target) {
            return target == REFUNDED;
        }
    },
    FAILED {
        @Override
        boolean canTransition(PaymentStatus target) {
            return false;
        }
    }, REFUNDED {
        @Override
        boolean canTransition(PaymentStatus target) {
            return false;
        }
    };

    /**
     * Public rule:
     * - Allows idempotency (same state)
     * - Delegates specific transition rule
     */
    public boolean canTransitionTo(PaymentStatus target) {
        if (this == target) {
            return true;
        }
        return canTransition(target);
    }

    /**
     * State-specific transition rule
     */
    abstract boolean canTransition(PaymentStatus target);
}
