package metrics;

import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.metrics.GlobalMetricsProvider;
import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.api.metrics.LongValueRecorder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.IntervalMetricReader;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.autoconfigure.OpenTelemetrySdkAutoConfiguration;
import io.opentelemetry.api.metrics.LongUpDownCounter;

import java.util.Collections;

public class MetricEmitter {

  static final String DIMENSION_API_NAME = "apiName";
  static final String DIMENSION_STATUS_CODE = "statusCode";

  LongUpDownCounter queueSizeCounter;

  String latencyMetricName;
  IntervalMetricReader reader;

  public MetricEmitter() {
    String otelExporterOtlpEndpoint = System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT") != null ? System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT") : "127.0.0.1:55680";
    MetricExporter metricExporter =
            OtlpGrpcMetricExporter.builder()
                    .setChannel(
                            ManagedChannelBuilder.forTarget(otelExporterOtlpEndpoint).usePlaintext().build())
                    .build();

    reader = IntervalMetricReader.builder()
            .setMetricProducers(
                    Collections.singleton(SdkMeterProvider.builder().setResource(OpenTelemetrySdkAutoConfiguration.getResource()).buildAndRegisterGlobal()))
            .setExportIntervalMillis(5000)
            .setMetricExporter(metricExporter)
            .build();
    Meter meter = GlobalMetricsProvider.getMeter("aws-otel", "1.0");

    queueSizeCounter =
            meter
                .longUpDownCounterBuilder("queueSizeChange")
                .setDescription("Queue Size change")
                .setUnit("one")
                .build();
  }

  /**
   * emit http request queue size metrics
   *
   * @param returnTime
   * @param apiName
   * @param statusCode
   */
  public void emitQueueSizeChangeMetric(int queueSizeChange, String apiName, String statusCode) {
        System.out.println(
            "emit metric with queue size change " + queueSizeChange + "," + apiName + "," + statusCode);
        queueSizeCounter.add(
            queueSizeChange, Labels.of(DIMENSION_API_NAME, apiName, DIMENSION_STATUS_CODE, statusCode));
      }
}
