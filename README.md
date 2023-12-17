# TUI GitHub service

This service acts as a facade of the GitHub API. It provides a single endpoint that lists branches for a given user.

## Pre-requisites

* Java 17
* Gradle
* Docker
* AWS CDK

## Documentation

There are two sources:

* http://localhost:8080/swagger-ui/index.html#/ when the service is running
* `openapi.yaml` in the project root

## Testing

### Unit

`gradle test`

### Integration

`gradle integrationtest`

### Manual

Against AWS: `http://{LB DNS}.us-east-1.elb.amazonaws.com:8080/owners/hopstoker/repos`
e.g. `http://TuiGit-TuiGi-fms9UzhvfBOr-543994114.us-east-1.elb.amazonaws.com:8080/owners/hopstoker/repos` or
locally: `http://localhost:8080/owners/{owner}/repos`

## Running service locally

Use the script `./docker-start.sh`.

## Implementation

1. I chose to implement this service in Spring Boot because it is the industry standard.
2. I chose Java since it says it is the preferred language in the spec (I debated between Java and Kotlin)
3. I split the responsibilities in the service between controller, service and client classes.
4. Initially, I used `WebClient` but that caused issues with mocking in integration tests, so I fell back
   to `RestTemplate` with `MockRestServiceServer`.
5. Rather than defining a CloudFormation template in yaml I chose AWS CDK. It is an IaC solution that provides
   validation
   and enables infra code testing.

## Deploying

### AWS and GitHub setup

This is required in the Jenkin, local CDK deployment and local Docker cases:

1. Create an IAM user with an inline policy same as below
2. Obtain `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY`
3. Generate a GitHub personal access token (https://github.com/settings/tokens -> Personal access tokens -> Generate new token)
4. Put token in application.yaml under `client.github.token`

```
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "cloudformation:*",
                "ecr:*",
                "ssm:*",
                "s3:*",
                "iam:*"
            ],
            "Resource": "*"
        }
    ]
}
```

### From Jenkins

The Jenkins pipeline is almost complete - the one bit I could not figure out was using a Jenkins agent that is based
on a custom Docker image. I need a custom image that has Java 17, Gradle and AWS CDK. For development, I used a
Dockerised
instance of Jenkins. This setup required a number of preparation steps once the instance was up:

1. Install Docker plugin + Docker Pipeline plugin
2. Install AWS Credentials plugin
3. Add AWS Credentials; see AWS setup section (Manage Jenskins -> Credentials -> System store -> Global credentials ->
   Add credentials with name `aws_credentials` and kind `AWS Credentials`)
4. Add Docker repository credentials
5. Create a new Jenkins pipeline with a script based on Jenkinsfile from this repo

### From local

These steps are what I used for deployment before the attempt at the Jenkins pipeline (this is pretty much identical to
./Jenkinsfile):

1. `gradle clean test integrationtest` in root
2. `gradle clean bootjar` in root
3. `gradle clean shadowjar` in ./cdk
4. Set `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` and `AWS_DEFAULT_REGION` env vars
5. `cdk synth` in ./cdk (creates CloudFormation template)
6. `cdk bootstrap --app cdk.out` in ./cdk (creates an auxiliary stack)
7. `cdk deploy --app cdk.out` in ./cdk (creates the service stack)
8. Service address is printed out once deploy has finished.

## Requirements

1. API consumer accepts json
2. Endpoint accepts GitHub username
3. Endpoint returns a list of repositories (that are not forks) for the user
4. Endpoint response has:
    * repo name
    * owner login
    * branch name and latest commit SHA for each branch
5. For non-existent users, endpoint returns 404 with a response in the
   format `{"status": ${responseCode} "Message": ${whyHasItHappened} }`
6. For received header of `Accept: application/xml`, endpoint returns 406 with a response in the
   format `{"status": ${responseCode} "Message": ${whyHasItHappened} }`
7. Service uses `https://developer.github.com/v3` to get the needed data
8. Endpoints are documented in Swagger yaml format
9. Service is implemented using any reactive framework, preferably in Java or Node.js
10. There is unit and integration test coverage
11. Service is containerised
12. CloudFormation template is available
13. Service runs in Fargate or ECS
14. API Gateway exposes the endpoint
15. Jenkins builds and deploys the service in AWS

## Improvements

1. Store the GitHub token in Secrets Manager (currently it is stored in application.yaml but does not pose a significant
   security threat because it has minimal permissions)
2. Extend integration tests to run against an instance in AWS or local Docker (it makes a more complete test by covering
   AWS/Docker networking)
3. Add CDK tests
4. Add a record in Route 53 to have a constant API address
5. This is a simple service but it might benefit from logging, metrics and tracing in the future
