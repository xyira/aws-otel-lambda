variable "function_name" {
  type        = string
  description = "Name of sample app function / API gateway"
  default     = "hello-java-awssdk-agent"
}

variable "collector_config_layer_arn" {
  type        = string
  description = "ARN for the Lambda layer containing the OpenTelemetry collector configuration file"
  default = null
}
