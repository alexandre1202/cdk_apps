package com.myorg;

import software.amazon.awscdk.App;

public class CdkAppsApp {
    public static void main(final String[] args) {
        App app = new App();

        VPCAGCStack vpc = new VPCAGCStack(app, "VpcAAB");

        ClusterStack clusterStack = new ClusterStack(app, "ClusterAAB", vpc.getVpc());
        clusterStack.addDependency(vpc);

        RDSStack rdsStack = new RDSStack(app, "Rds", vpc.getVpc());
        rdsStack.addDependency(vpc);

        SnsStack snsStack = new SnsStack(app, "Sns");

        Service01Stack service01Stack = new Service01Stack(app, "ServiceAAB", clusterStack.getCluster(), snsStack.getProductEventsTopic());
        service01Stack.addDependency(clusterStack);
        service01Stack.addDependency(rdsStack);
        service01Stack.addDependency(snsStack);

        Service02Stack service02Stack = new Service02Stack(app, "ServiceAAB02", clusterStack.getCluster(), snsStack.getProductEventsTopic());
        service02Stack.addDependency(clusterStack);
        service02Stack.addDependency(snsStack);

        app.synth();
    }
}

