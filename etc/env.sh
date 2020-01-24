#!/bin/bash

# Configured before any deployment
export S3_BUCKET=logscape-runtime-test
export S3_TENANT_BUCKET=logscape-test-userstore
export STACK_NAME=logscape-faas

# Configure after the backend is deployed and the REST API is known
export LOGSCAPE_API=https://AAAA.execute-api.eu-west-2.amazonaws.com/Prod/