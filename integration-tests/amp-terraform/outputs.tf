output "amp_endpoint" {
  value = aws_prometheus_workspace.lambda-test.prometheus_endpoint
}

output "remotewrite_endpoint" {
  value = "${aws_prometheus_workspace.lambda-test.prometheus_endpoint}api/v1/remote_write"
}

output "aws_region" {
  value = data.aws_region.current.name
}
