
#### Dev Mode on local machine against AWS
* $ cd uploader
* $ ./mvnw compile quarkus:dev
* Open index.html in a browser from Intellij (backend.js defaults to http://localhost:8080 which quarkus is running the REST endpoint)
* Browse to 


#### AWS Deployment

Reference: https://quarkus.io/guides/amazon-lambda-http

Prerequisites: 
- Install AWS SAM: https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/what-is-sam.html
- Install the AWS cli. I used version 2.0 - i.e. all cli commands are `> aws2 --stuff--`

**Notes:**
This approach uses the AWS Gateway Proxy (i.e. the Gateway passes through everything). Quarkus has a HTTP handler embedded in the Lambda the calls on the RX Rest endpoint locally.

1. Setup credentials for AWS account so that SAM user so that Cloudformation etc is available. Brute force unsecure json as follows: <br>
    **IAM -> User (x) -> inline-policy **
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "VisualEditor0",
            "Effect": "Allow",
            "Action": [
                "iam:*",
                "apigateway:*",
                "lambda:*",
                "cloudformation:*",
                "dynamodb:*"
            ],
            "Resource": "*"
        }
    ]
}
```
1. Create an S3 bucket for lambda-code archive deployment <YOUR_S3_BUCKET>
1. Create the logscape fat jar <br>
 `$ mvn clean install` ->
 `target/logscape-0.1-SNAPSHOT-runner.jar`<br>
1. Package the deployment:<br>
 `$ sam package --template-file sam.jvm.yaml --output-template-file packaged.yaml --s3-bucket <YOUR_S3_BUCKET>`
1. Deploy it using any stack-name<br>
`$ sam deploy --template-file packaged.yaml --capabilities CAPABILITY_IAM --stack-name <YOUR_STACK_NAME>`<br>
The output will display the API-Gateway URL for the front end

1. List deployments looking for your stack (and others)<br>
`$ aws2 cloudformation list-stacks`<br>
> to delete: 
> `$ aws2 cloudformation delete-stack --stack-name <YOUR_STACK_NAME>`

```json
 { "StackSummaries": [
        {
            "StackId": "arn:aws:cloudformation:eu-west-2:001814218767:stack/logscape-faas/6a5712b0-3d2f-11ea-8738-021679f87d94",
            "StackName": "logscape-faas",
            "TemplateDescription": "AWS Serverless Quarkus HTTP - liquidlabs::logscape",
            "CreationTime": "2020-01-22T15:54:04.530000+00:00",
            "LastUpdatedTime": "2020-01-22T15:54:09.927000+00:00",
            "StackStatus": "CREATE_COMPLETE",
            "DriftInformation": {
                "StackDriftStatus": "NOT_CHECKED"
            }
        }
    ]
}
```
1. Note the API-Gateway-Proxy REST URL<br>
`$ aws2 cloudformation describe-stacks --stack-name logscape-faas`
```json
{
    "Stacks": [
        {
            "StackId": "arn:aws:cloudformation:eu-west-2:001814218767:stack/logscape-faas/6a5712b0-3d2f-11ea-8738-021679f87d94",
            "StackName": "logscape-faas",
            "ChangeSetId": "arn:aws:cloudformation:eu-west-2:001814218767:changeSet/samcli-deploy1579708443/2f0ce821-122f-40b5-ad6f-26861bc64daf",
            "Description": "Serverless log analysis- liquidlabs::logscape",
            "CreationTime": "2020-01-22T15:54:04.530000+00:00",
            "LastUpdatedTime": "2020-01-22T15:54:09.927000+00:00",
            "RollbackConfiguration": {},
            "StackStatus": "CREATE_COMPLETE",
            "DisableRollback": false,
            "NotificationARNs": [],
            "Capabilities": [
                "CAPABILITY_IAM"
            ],
            "Outputs": [
                {
                    "OutputKey": "LogscapeApi",
                    "OutputValue": "https://3dwlmgnks6.execute-api.eu-west-2.amazonaws.com/Prod/",
                    "Description": "URL for application",
                    "ExportName": "LogscapeApi"
                }
            ],
            "Tags": [],
            "EnableTerminationProtection": false,
            "DriftInformation": {
                "StackDriftStatus": "NOT_CHECKED"
            }
        }
    ]
}
```
1. From above see that the API-Gateway Proxy is available on <br>
`https://3dwlmgnks6.execute-api.eu-west-2.amazonaws.com/Prod/`
1. Check the API Gateway proxies requests to the Lambda function - **Note - append** `query/id` to the url. It will suffer a cold start, but subsequent invocations will be much quicker
 
```
Browser URL: https://3dwlmgnks6.execute-api.eu-west-2.amazonaws.com/Prod/query/id
Response:    com.liquidlabs.logscape.uploader.QueryResource
```
1. Configure the User interface URL to point to the API-Gateway address