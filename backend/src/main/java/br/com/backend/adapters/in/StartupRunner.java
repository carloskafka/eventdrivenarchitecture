package br.com.backend.adapters.in;

import br.com.backend.support.TestEventFactory;
import br.com.libdomain.model.Event;
import br.com.libdomain.router.EventRouter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StartupRunner implements CommandLineRunner {

    private final EventRouter router;

    public StartupRunner(EventRouter router) {
        this.router = router;
    }

    @Override
    public void run(String... args) {
        System.out.println(">>> Sending test event");

        router.route(TestEventFactory.orderCreated("1234"));
        router.route(TestEventFactory.paymentApproved("1234"));
    }
}
