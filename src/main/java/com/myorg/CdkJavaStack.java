package com.myorg;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Fn;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.CfnFunction;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.s3.assets.Asset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CdkJavaStack extends Stack {
    private final String apiName = "CDK-OpenAPI-RestApi";
    private final String openApiPath = "./api/openapi-rest-lambda-cdk.yaml";
    private final String lambdaNameAsInOpenAPI = "MyCustomLambda";
    private final String lambdaApiRoleAsInOpenAPI = "apiRole";

    public CdkJavaStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public CdkJavaStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);

        final Function myFunction = Function.Builder.create(this, "HelloHandler")
                .functionName(lambdaNameAsInOpenAPI)
                .runtime(Runtime.NODEJS_12_X)
                .code(Code.fromAsset("lambda"))  // code loaded from the "lambda" directory
                .handler("hello.handler")        // file is "hello", function is "handler"
                .build();

        Role apiRole = Role.Builder.create(this,lambdaApiRoleAsInOpenAPI)
                .roleName(lambdaApiRoleAsInOpenAPI)
                .assumedBy(ServicePrincipal.Builder.create("apigateway.amazonaws.com").build())
                .build();
        List<String> access = new ArrayList<>();
        List<String> action = new ArrayList<>();
        access.add("*"); //only for demo purposes
        action.add("lambda:InvokeFunction");
        apiRole.addToPolicy(PolicyStatement.Builder.create().resources(access).actions(action).build());

        CfnFunction forcedLambdaId = (CfnFunction) myFunction.getNode().getDefaultChild();
        if (forcedLambdaId != null) {
            forcedLambdaId.overrideLogicalId(lambdaNameAsInOpenAPI);
        }

        Asset asset = Asset.Builder.create(this,"SampleAsset").path(openApiPath).build();
        Map<String, String> variables = new HashMap<>();
        variables.put("Location",asset.getS3ObjectUrl());
        Object data = Fn.transform("AWS::Include",variables);

        InlineApiDefinition apiDef = AssetApiDefinition.fromInline(data);
        SpecRestApi openapiRestApi = SpecRestApi.Builder.create(this, apiName)
                .restApiName(apiName)
                .apiDefinition(apiDef)
                .deploy(true)
                .build();

        PopulateApiKey populateApiKey = new PopulateApiKey();
        populateApiKey.populate(openapiRestApi,this);

    }
}
