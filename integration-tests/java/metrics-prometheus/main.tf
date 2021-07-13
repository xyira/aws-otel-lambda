module "test" {
  source = "../../../opentelemetry-lambda/java/integration-tests/metrics-prometheus"

  enable_collector_layer = true
  collector_layer_name = var.collector_layer_name
  function_name = var.function_name
}

resource "aws_iam_role_policy_attachment" "test_metrics_prometheus" {
  role       = module.test.function_role_name
  policy_arn = "arn:aws:iam::aws:policy/AWSManagedPrometheusWriteAccessPolicy"
}
