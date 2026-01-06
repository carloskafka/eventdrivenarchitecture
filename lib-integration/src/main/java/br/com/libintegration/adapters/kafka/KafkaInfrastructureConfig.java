package br.com.libintegration.adapters.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableConfigurationProperties(IntegrationProperties.class)
public class KafkaInfrastructureConfig {

    private final IntegrationProperties props;
    private final Environment env;

    public KafkaInfrastructureConfig(IntegrationProperties props, Environment env) {
        this.props = props;
        this.env = env;
    }

    @Bean
    public List<NewTopic> integrationTopics() {
        List<NewTopic> topics = new ArrayList<>();

        if (!props.isAutoCreate()) {
            return topics;
        }

        // If autoCreateProfiles is non-empty, only auto-create when active profile matches one of them
        if (!props.getAutoCreateProfiles().isEmpty()) {
            boolean match = false;
            for (String p : env.getActiveProfiles()) {
                if (props.getAutoCreateProfiles().contains(p)) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                return topics; // skip creation
            }
        }

        for (String spec : props.getTopics()) {
            if (spec == null || spec.trim().isEmpty()) continue;
            String[] parts = spec.trim().split(":");
            String name = parts[0];
            int partitions = 1;
            short replicas = 1;
            try {
                if (parts.length >= 2) partitions = Integer.parseInt(parts[1]);
                if (parts.length >= 3) replicas = Short.parseShort(parts[2]);
            } catch (NumberFormatException e) {
                // ignore and use defaults
            }
            topics.add(new NewTopic(name, partitions, replicas));
        }

        return topics;
    }
}
