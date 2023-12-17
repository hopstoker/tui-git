package org.piotrhosa.tui;

import com.fasterxml.jackson.core.JsonProcessingException;
import software.amazon.awscdk.App;
import software.amazon.awscdk.StackProps;

public class CdkApp {
    public static void main(final String[] args) throws JsonProcessingException {
        App app = new App();

        new CdkStack(app, "TuiGithubStack", StackProps.builder()
                .build());

        app.synth();
    }
}

