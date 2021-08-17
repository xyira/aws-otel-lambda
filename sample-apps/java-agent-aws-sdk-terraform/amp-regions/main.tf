data "aws_region" "current" {}

module "amp-app" {
  source = "../"

  collector_config_layer_arn = aws_lambda_layer_version.collector_config_layer.arn
}

resource "aws_prometheus_workspace" "test_amp_workspace" {}

data "archive_file" "init" {
  type       = "zip"
  depends_on = [aws_prometheus_workspace.test_amp_workspace, data.aws_region.current]
  source {
    content  = <<EOT
receivers:
  otlp:
    protocols:
      grpc:
      http:

exporters:
  logging:
  awsxray:
  awsprometheusremotewrite:
    endpoint: "${aws_prometheus_workspace.test_amp_workspace.prometheus_endpoint}api/v1/remote_write"
    aws_auth:
      service: "aps"
      region: "${data.aws_region.current.name}"

service:
  pipelines:
    traces:
      receivers: [otlp]
      exporters: [awsxray]
    metrics:
      receivers: [otlp]
      exporters: [logging, awsprometheusremotewrite]
EOT
    filename = "config.yaml"
  }

  output_path = "${path.module}/build/custom-config-layer.zip"
}

resource "aws_lambda_layer_version" "collector_config_layer" {
  depends_on          = [data.archive_file.init]
  layer_name          = "custom-config-layer"
  filename            = "${path.module}/build/custom-config-layer.zip"
  compatible_runtimes = ["java8", "java8.al2", "java11"]
  license_info        = "Apache-2.0"
}
