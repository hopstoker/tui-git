package org.piotrhosa.tui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awscdk.CfnResource;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.IntegrationType;
import software.amazon.awscdk.services.apigatewayv2.CfnIntegration;
import software.amazon.awscdk.services.apigatewayv2.CfnRoute;
import software.amazon.awscdk.services.apigatewayv2.HttpApi;
import software.amazon.awscdk.services.ec2.ISubnet;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecr.assets.DockerImageAsset;
import software.amazon.awscdk.services.ecr.assets.Platform;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.secretsmanager.Secret;
import software.constructs.Construct;

import java.util.Map;
import java.util.stream.Collectors;

public class CdkStack extends Stack {
    public CdkStack(final Construct scope, final String id) throws JsonProcessingException {
        this(scope, id, null);
    }

    public CdkStack(final Construct scope, final String id, final StackProps props) throws JsonProcessingException {
        super(scope, id, props);

        var githubTokenSecret = Secret.Builder.create(this, "tui/github/token")
                .removalPolicy(RemovalPolicy.RETAIN)
                .build();

        var vpc = Vpc.Builder.create(this, "TuiGithubVpc").build();

        var cluster = Cluster.Builder.create(this, "TuiGithubVpcCluster")
                .vpc(vpc)
                .build();

        var privateSubnets = vpc.getPrivateSubnets().stream()
                .map(ISubnet::getSubnetId)
                .collect(Collectors.toList());

        var vpcLink = CfnResource.Builder.create(this, "TuiGithubVpcLink")
                .type("AWS::ApiGatewayV2::VpcLink")
                .properties(Map.of(
                        "Name", "V2 VPC Link",
                        "SubnetIds", privateSubnets
                ))
                .build();

        var api = HttpApi.Builder.create(this, "TuiGitApi")
                .apiName("TUI Git API")
                .build();

        DockerImageAsset tuiGithubAsset = DockerImageAsset.Builder.create(this, "TuiGithubDockerAsset")
                .directory("../")
                .platform(Platform.LINUX_AMD64)
                .build();

        var secrets = Map.of(
                "GITHUB_TOKEN", githubTokenSecret.getSecretValue().unsafeUnwrap()
        );

        var service = ApplicationLoadBalancedFargateService.Builder.create(this, "TuiGithubService")
                .taskImageOptions(ApplicationLoadBalancedTaskImageOptions.builder()
                        .image(ContainerImage.fromDockerImageAsset(tuiGithubAsset))
                        .environment(secrets)
                        .containerPort(8080)
                        .build())
                .cluster(cluster)
                .desiredCount(1)
                .listenerPort(8080)
                .build();

        service.getTargetGroup().configureHealthCheck(
                HealthCheck.builder()
                        .path("/actuator/health")
                        .port("8080")
                        .build()
        );

        var cfnIntegration = CfnIntegration.Builder.create(this, "TuiGithubCfnIntegration")
                .apiId(api.getApiId())
                .connectionId(vpcLink.getRef())
                .connectionType("VPC_LINK")
                .description("API integration with Fargate")
                .integrationUri(service.getListener().getListenerArn())
                .integrationType(IntegrationType.HTTP_PROXY.name())
                .integrationMethod("ANY")
                .payloadFormatVersion("1.0")
                .build();

        CfnRoute.Builder.create(this, "TuiGithubGetReposRoute")
                .apiId(api.getApiId())
                .routeKey("ANY /{proxy+}")
                .target("integrations/" + cfnIntegration.getRef())
                .build();
    }
}
