package br.com.backend.model.payment;

public enum PaymentStatus {

    CREATED {
        @Override
        boolean canTransitionTo(PaymentStatus target) {
            return target == AUTHORIZED || target == FAILED;
        }
    },
    AUTHORIZED {
        @Override
        boolean canTransitionTo(PaymentStatus target) {
            return target == APPROVED || target == FAILED;
        }
    },
    APPROVED {
        @Override
        boolean canTransitionTo(PaymentStatus target) {
            return target == REFUNDED;
        }
    },
    FAILED {
        @Override
        boolean canTransitionTo(PaymentStatus target) {
            return false;
        }
    }, REFUNDED {
        @Override
        boolean canTransitionTo(PaymentStatus target) {
            return false;
        }
    };

    abstract boolean canTransitionTo(PaymentStatus target);
}
