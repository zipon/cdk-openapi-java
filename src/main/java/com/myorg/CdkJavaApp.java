package com.myorg;

import software.amazon.awscdk.core.App;

public final class CdkJavaApp {
    public static void main(final String[] args) {
        App app = new App();

        new CdkJavaStack(app, "CdkJavaOpenAPIRestStack");

        app.synth();
    }
}
