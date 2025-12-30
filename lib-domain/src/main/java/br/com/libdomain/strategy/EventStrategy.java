
package br.com.libdomain.strategy;

import br.com.libdomain.model.Event;

public interface EventStrategy {
    void execute(Event event);
    boolean supports(Event event);
}
