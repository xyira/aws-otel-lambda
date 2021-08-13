 #!/bin/bash   


# terraform init # run by CI 
# terraform apply --auto-approve  # run by CI 

AMP_ENDPOINT="$(terraform output "remotewrite_endpoint")"
AMP_REGION="$(terraform output "aws_region")"
sed "s@endpoint.*\"@endpoint: $AMP_ENDPOINT@g; s@region.*\"@region: $AMP_REGION@g"  preformatted_config.yaml > config.yaml

# maybe can append to existing default collector config in the future, but for now will just replace the endpoint in an already formatted collector config.yaml file
# AMP_CONFIG="
# \nawsprometheusremotewrite:\n
#     endpoint: \"$AMP_ENDPOINT\"\n
#     aws_auth:\n
#       service: \"aps\"\n
#       region: \us-east-1\"\n
#       "
# echo $AMP_CONFIG

zip custom-config-layer.zip config.yaml

# terraform destroy --auto-approve  # run by CI 