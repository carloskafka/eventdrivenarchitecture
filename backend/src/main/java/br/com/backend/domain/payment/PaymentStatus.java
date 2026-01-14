package br.com.backend.domain.payment;

public enum PaymentStatus {

    CREATED {
        @Override
        public boolean canTransitionTo(PaymentStatus target) {
            return target == this || target == AUTHORIZED || target == FAILED ;
        }
    },
    AUTHORIZED {
        @Override
        public boolean canTransitionTo(PaymentStatus target) {
            return target == this || target == APPROVED || target == FAILED;
        }
    },
    APPROVED {
        @Override
        public boolean canTransitionTo(PaymentStatus target) {
            return target == this; // aprovado é terminal
        }
    },
    FAILED {
        @Override
        public boolean canTransitionTo(PaymentStatus target) {
            return target == this; // failed é terminal
        }
    };

    public abstract boolean canTransitionTo(PaymentStatus target);
}
