package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.Vpc;
import software.constructs.Construct;

public class VPCAGCStack extends Stack {
    private Vpc vpc;

    public VPCAGCStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public VPCAGCStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        vpc = Vpc.Builder.create(this, "VpcAAB")
                .maxAzs(3)
                .natGateways(0)
                .build();
    }

    public Vpc getVpc() {
        return vpc;
    }
}
