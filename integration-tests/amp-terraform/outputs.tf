output "amp_endpoint" {
  value = aws_prometheus_workspace.lambda-test.prometheus_endpoint
}

output "remotewrite_endpoint" {
  value = "${aws_prometheus_workspace.lambda-test.prometheus_endpoint}api/v1/remote_write"
}

output "collector_config" {
  value = yamlencode({
    "receivers" : {
      "otlp": {
        "protocols": {
          "grpc": ""
          "http": ""
        }
      }
    },
    "exporters" : {
      "logging": ""
      "awsxray": ""
      "awsprometheusremotewrite": {
        "endpoint": "${aws_prometheus_workspace.lambda-test.prometheus_endpoint}api/v1/remote_write"
      }
    },
    "service": {
      "pipelines": {
        "traces": {
          "receivers": ["otlp"]
          "exporters": ["awsxray"]
        },
        "metrics": {
          "receivers": ["otlp"]
          "exporters": ["logging", "awsprometheusremotewrite"] 
        }
      }
    }
  })
}

