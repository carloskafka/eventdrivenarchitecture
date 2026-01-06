package br.com.libintegration.adapters.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "integration.kafka")
public class IntegrationProperties {

    /**
     * List of topic specs. Each spec format: name or name:partitions or name:partitions:replicas
     * Examples: payment-events  or payment-events:3  or payment-events:3:1
     */
    private List<String> topics = new ArrayList<>();

    /**
     * Whether to auto-create topics defined above. Defaults to true.
     */
    private boolean autoCreate = true;

    /**
     * Profiles in which auto-creation should run. When empty, auto-create is always allowed.
     * Example: [dev]
     */
    private List<String> autoCreateProfiles = new ArrayList<>();

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public boolean isAutoCreate() {
        return autoCreate;
    }

    public void setAutoCreate(boolean autoCreate) {
        this.autoCreate = autoCreate;
    }

    public List<String> getAutoCreateProfiles() {
        return autoCreateProfiles;
    }

    public void setAutoCreateProfiles(List<String> autoCreateProfiles) {
        this.autoCreateProfiles = autoCreateProfiles;
    }
}

