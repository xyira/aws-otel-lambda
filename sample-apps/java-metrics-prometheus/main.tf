data "aws_region" "current" {}

module "app" {
  source = "../../opentelemetry-lambda/java/sample-apps/metrics-prometheus/deploy"

  name                = var.function_name
  collector_layer_arn = lookup(local.collector_layer_arns, data.aws_region.current.name, "invalid")
  tracing_mode        = "Active"
}

resource "aws_iam_role_policy_attachment" "test_amp" { 
  role       = module.app.function_role_name
  policy_arn = "arn:aws:iam::aws:policy/AWSManagedPrometheusWriteAccessPolicy"
}
