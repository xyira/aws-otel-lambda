data "aws_region" "current" {}

resource "aws_prometheus_workspace" "lambda-test" {
    alias = "lambda-test"
}