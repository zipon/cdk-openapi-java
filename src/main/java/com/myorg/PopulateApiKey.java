package com.myorg;

import software.amazon.awscdk.services.apigateway.ApiKeyProps;
import software.amazon.awscdk.services.apigateway.UsagePlan;
import software.amazon.awscdk.services.apigateway.UsagePlanProps;
import software.amazon.awscdk.services.apigateway.UsagePlanPerApiStage;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.apigateway.ThrottleSettings;
import java.util.ArrayList;

public class PopulateApiKey {
    private static final String apiKeyName = "OpenAPI-ApiKey";
    private static final String usagePlanName = "OpenAPI-ApiUsagePlan";

    public void populate(SpecRestApi openapiRestApi, CdkJavaStack scope){
        ApiKeyProps apiKeyProps = new ApiKeyProps.Builder()
                .apiKeyName(apiKeyName)
                .description("API-key used in the OpenApi definition")
                .enabled(true)
                .build();

        ApiKey apiKey = new ApiKey(scope, apiKeyName, apiKeyProps);

        UsagePlanPerApiStage apiStage = new UsagePlanPerApiStage.Builder()
                .api(openapiRestApi.getRoot().getApi())
                .stage(openapiRestApi.getDeploymentStage())
                .build();

        ArrayList<UsagePlanPerApiStage> listOfStages = new ArrayList<>();
        listOfStages.add(apiStage);

        ThrottleSettings throttleSettings = new ThrottleSettings.Builder()
                .burstLimit(200)
                .rateLimit(100)
                .build();

        UsagePlanProps usagePlanProps = new UsagePlanProps.Builder()
                .apiKey(apiKey)
                .name(usagePlanName)
                .apiStages(listOfStages)
                .throttle(throttleSettings)
                .build();

        new UsagePlan(scope, usagePlanName, usagePlanProps);
    }

}
