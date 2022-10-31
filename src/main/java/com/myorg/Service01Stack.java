package com.myorg;

import java.util.HashMap;
import java.util.Map;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Fn;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecs.AwsLogDriverProps;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.CpuUtilizationScalingProps;
import software.amazon.awscdk.services.ecs.LogDriver;
import software.amazon.awscdk.services.ecs.ScalableTaskCount;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.logs.LogGroup;
import software.constructs.Construct;

public class Service01Stack extends Stack {
    public Service01Stack(final Construct scope, final String id, Cluster cluster) {
        this(scope, id, null, cluster);
    }

    public Service01Stack(final Construct scope, final String id, final StackProps props, Cluster cluster) {
        super(scope, id, props);

        Map<String, String> envVariables = new HashMap<String, String>() {{
            put("SPRING_DATASOURCE_URL", "jdbc:mariadb://"
                    + Fn.importValue("rds-endpoint")
                    + "3306/aws1-rds-schema?createDatabaseIfNotExist=true");
            put("SPRING_DATASOURCE_USERNAME", "admin");
            put("SPRING_DATASOURCE_PASSWORD", Fn.importValue("rds-password"));
        }};

        final LogDriver logDriver = LogDriver
                .awsLogs(AwsLogDriverProps
                        .builder()
                        .logGroup(LogGroup.Builder
                                .create(this, "ServiceAABLogGroup")
                                .logGroupName("ServiceAAB")
                                .removalPolicy(RemovalPolicy.DESTROY).build())
                        .streamPrefix("ServiceAAB")
                        .build());
        final ApplicationLoadBalancedTaskImageOptions albTask = ApplicationLoadBalancedTaskImageOptions
                .builder()
                .containerName("aws1")
                .containerPort(8080)
                .image(ContainerImage.fromRegistry("alexandre1202/aws1:1.0.0"))
                .logDriver(logDriver)
                .environment(envVariables)
                .build();
        final ApplicationLoadBalancedFargateService serviceAAB = ApplicationLoadBalancedFargateService
                .Builder.create(this, "LoadBalancedAAB")
                .serviceName("ServiceAAB")
                .cluster(cluster)
                .cpu(512)
                .memoryLimitMiB(1024)
                .desiredCount(2)
                .listenerPort(8080)
                .assignPublicIp(true)
                .taskImageOptions(albTask)
                .publicLoadBalancer(true)
                .build();
        serviceAAB.getTargetGroup().configureHealthCheck(new HealthCheck
                .Builder()
                .path("/actuator/health")
                .port("8080")
                .healthyHttpCodes("200")
                .build());

        ScalableTaskCount taskCount = serviceAAB.getService().autoScaleTaskCount(EnableScalingProps.builder()
                .minCapacity(2)
                .maxCapacity(4)
                .build());
        taskCount.scaleOnCpuUtilization("AABAutoScaling", CpuUtilizationScalingProps.builder()
                        .targetUtilizationPercent(50)
                        .scaleInCooldown(Duration.seconds(60))
                        .scaleInCooldown(Duration.seconds(60))
                        .build());
    }
}
