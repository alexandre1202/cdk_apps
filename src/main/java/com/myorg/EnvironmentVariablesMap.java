package com.myorg;

import java.util.HashMap;
import java.util.Map;
import software.amazon.awscdk.Fn;
import software.amazon.awscdk.services.events.targets.SnsTopic;

public class EnvironmentVariablesMap {
    public static Map<String, String> buildEnv(SnsTopic productEventsTopic) {
        return new HashMap<String, String>() {{
            put("SPRING_DATASOURCE_URL", "jdbc:mariadb://"
                    + Fn.importValue("rds-endpoint")
                    + ":3306/aws1-rds-schema?createDatabaseIfNotExist=true");
            put("SPRING_DATASOURCE_USERNAME", "admin");
            put("SPRING_DATASOURCE_PASSWORD", Fn.importValue("rds-password"));
            put("AWS_REGION", "us-east-1");
            put("AWS_SNS_TOPIC_PRODUCT_EVENTS_ARN", productEventsTopic.getTopic().getTopicArn());
        }};
    }
}
