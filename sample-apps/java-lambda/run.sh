
#!/bin/bash

set -e
set -u

echo_usage () {
    echo "usage: Deploy Java Lambda Application from scratch"
    echo " -r <aws region>"
    echo " -t <cloudformation template>"
    echo " -b <sam build>"
    echo " -d <sam deploy>"
    echo " -l <layer only>"
    echo " -n <invoke lambda 1 times>"
    echo " -s <stack name>"
    echo " -p <ECR repo name>"
}

main () {
    echo "running..."
    saved_args="$@"
    template='template.yml'
    build=false
    deploy=false
    debug=false
    invoke=false
    layeronly=false
    stack=${STACK-"otel-java-sample"}
    repo="lambda-sample-app"
    region=${AWS_REGION-$(aws configure get region)}

    while getopts "hbdxnlr:t:s:p:" opt; do
        case "${opt}" in
            h) echo_usage
                exit 0
                ;;
            b) build=true
                ;;
            x) debug=true
                ;;
            d) deploy=true
                ;;
            n) invoke=true
                ;;
            l) layeronly=true
                ;;
            r) region="${OPTARG}"
                ;;
            t) template="${OPTARG}"
                ;;
            s) stack="${OPTARG}"
                ;;
            p) repo="${OPTARG}"
              ;;
            \?) echo "Invalid option: -${OPTARG}" >&2
                exit 1
                ;;
            :)  echo "Option -${OPTARG} requires an argument" >&2
                exit 1
                ;;
        esac
    done

    echo "Invoked with: ${saved_args}"

    if [[ $build == false && $deploy == false && $invoke == false ]]; then
        build=true
        deploy=true
    fi

    if [[ $build == true ]]; then
        rm -rf .aws-sam
        echo "building collector..."
        rm -rf opentelemetry/opentelemetry_collector
        mkdir -p opentelemetry/opentelemetry_collector
        ls
        rm -rf opentelemetry-lambda
        git clone https://github.com/open-telemetry/opentelemetry-lambda.git
        ls
        cp -r opentelemetry-lambda/collector/* opentelemetry/opentelemetry_collector
        rm -rf opentelemetry/build
        mkdir -p opentelemetry/build
        cd opentelemetry && make build
        if [[ $layeronly == false ]]; then
          echo "building sam..."
          cd ../ && sam build -u -t $template
        fi

    fi

    if [[ $deploy == true ]]; then
        echo "sam deploying..."
        rm -rf ecr_repo_name.tmp
        aws ecr describe-repositories --repository-name $repo > ecr_repo_name.tmp ||aws ecr create-repository --repository-name $repo --image-scanning-configuration scanOnPush=true > ecr_repo_name.tmp
        repoUri=$(cat ecr_repo_name.tmp |grep "repositoryUri" |cut -f 4 -d '"')
        sam deploy --stack-name "$stack" --region "$region" --capabilities CAPABILITY_NAMED_IAM --resolve-s3  --image-repository "$repoUri"
        rm -rf opentelemetry/opentelemetry_collector
        rm -rf ecr_repo_name.tmp
    fi

    if [[ $invoke == true ]]; then
        apiid=$(aws cloudformation describe-stack-resource --stack-name $stack --region $region --logical-resource-id api --query 'StackResourceDetail.PhysicalResourceId' --output text)
        curl https://$apiid.execute-api.$region.amazonaws.com/api/ -v
    fi

}


main "$@"
