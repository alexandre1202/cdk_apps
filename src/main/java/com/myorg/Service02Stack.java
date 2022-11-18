package com.myorg;

import java.util.Map;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecs.AwsLogDriverProps;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.LogDriver;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.events.targets.SnsTopic;
import software.amazon.awscdk.services.logs.LogGroup;
import software.constructs.Construct;

public class Service02Stack extends Stack {

    private final int SERVICEPORT = 9090;

    public Service02Stack(final Construct scope, final String id, Cluster cluster, SnsTopic productEventsTopic) {
        this(scope, id, null, cluster, productEventsTopic);
    }

    public Service02Stack(final Construct scope, final String id, final StackProps props, Cluster cluster, SnsTopic productEventsTopic) {
        super(scope, id, props);

        final Map<String, String> envVariables = EnvironmentVariablesMap.buildEnv(productEventsTopic);

        final LogDriver logDriver = LogDriver
                .awsLogs(AwsLogDriverProps
                        .builder()
                        .logGroup(LogGroup.Builder
                                .create(this, "ServiceAABLogGroup")
                                .logGroupName("ServiceAAB02")
                                .removalPolicy(RemovalPolicy.DESTROY).build())
                        .streamPrefix("ServiceAAB02")
                        .build());

        final ApplicationLoadBalancedTaskImageOptions albTask = ApplicationLoadBalancedTaskImageOptions
                .builder()
                .containerName("aws2")
                .containerPort(SERVICEPORT)
                .image(ContainerImage.fromRegistry("alexandre1202/aws2:1.0.0"))
                .logDriver(logDriver)
                .environment(envVariables)
                .build();
        final ApplicationLoadBalancedFargateService serviceAAB = ApplicationLoadBalancedFargateService
                .Builder.create(this, "LoadBalancedAAB")
                .serviceName("ServiceAAB02")
                .cluster(cluster)
                .cpu(512)
                .memoryLimitMiB(1024)
                .desiredCount(2)
                .listenerPort(SERVICEPORT)
                .assignPublicIp(true)
                .taskImageOptions(albTask)
                .publicLoadBalancer(true)
                .build();
        serviceAAB.getTargetGroup().configureHealthCheck(new HealthCheck
                .Builder()
                .path("/actuator/health")
                .port(String.valueOf(SERVICEPORT))
                .healthyHttpCodes("200")
                .build());

    }
}
