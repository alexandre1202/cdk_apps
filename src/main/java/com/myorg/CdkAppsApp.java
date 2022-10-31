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

        Service01Stack service01Stack = new Service01Stack(app, "ServiceAAB", clusterStack.getCluster());
        service01Stack.addDependency(clusterStack);

        // Ver se realmente eu posso colocar o Service como dependencia do RDS ou não pois o Service está exportando variaveis que o RDS Precisa

        app.synth();
    }
}

