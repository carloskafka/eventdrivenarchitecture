package br.com.backend.model.order;

/**
 * Order states and transitions.
 */
public enum OrderStatus {
    NEW {
        @Override
        boolean canTransition(OrderStatus target) {
            return target == CONFIRMED || target == CANCELLED;
        }
    },
    CONFIRMED {
        @Override
        boolean canTransition(OrderStatus target) {
            return target == SHIPPED || target == CANCELLED;
        }
    },
    SHIPPED {
        @Override
        boolean canTransition(OrderStatus target) {
            return false;
        }
    },
    CANCELLED {
        @Override
        boolean canTransition(OrderStatus target) {
            return false;
        }
    };

    public boolean canTransitionTo(OrderStatus target) {
        if (this == target) return true;
        return canTransition(target);
    }

    abstract boolean canTransition(OrderStatus target);
}

