package metrics;

import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.metrics.GlobalMeterProvider;
import io.opentelemetry.api.metrics.common.Labels;
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
        static final String DIMENSION_UUID = "uuid";

        LongUpDownCounter queueSizeCounter;

        IntervalMetricReader reader;
        MetricExporter metricExporter; 
        public MetricEmitter() {
                String otelExporterOtlpEndpoint = System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT") != null
                                ? System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT")
                                : "127.0.0.1:55680";
                MetricExporter metricExporter = OtlpGrpcMetricExporter.builder().setChannel(
                                ManagedChannelBuilder.forTarget(otelExporterOtlpEndpoint).usePlaintext().build())
                                .build();

                reader = IntervalMetricReader.builder()
                                .setMetricProducers(Collections.singleton(SdkMeterProvider.builder()
                                                .setResource(OpenTelemetrySdkAutoConfiguration.getResource())
                                                .buildAndRegisterGlobal()))
                                .setExportIntervalMillis(1000).setMetricExporter(metricExporter).build();
                Meter meter = GlobalMeterProvider.getMeter("aws-otel", "1.0");

                queueSizeCounter = meter.longUpDownCounterBuilder("queueSizeChange").setDescription("Queue Size change")
                                .setUnit("one").build();
        }

        /**
         * emit http request queue size metrics
         *
         * @param returnTime
         * @param apiName
         * @param statusCode
         */
        public void emitQueueSizeChangeMetric(int queueSizeChange, String apiName, String statusCode, String uuid) {

                queueSizeCounter.add(queueSizeChange, Labels.of(DIMENSION_API_NAME, apiName, DIMENSION_STATUS_CODE,
                                statusCode, DIMENSION_UUID, uuid));

                System.out.println("emitted metric queueSizeChange with " + queueSizeCounter.toString() + "," + apiName + ","
                                + statusCode + "," + uuid);
        }

        public void forceFlush() {
                System.out.println(reader.forceFlush());
                // System.out.println(metricExporter.flush());
                System.out.println("forceflush of metric from IntervalMetricReader");
        }

        public void shutdown() {
                System.out.println(reader.shutdown());
                // System.out.println(metricExporter.shutdown());
                System.out.println("shutdown of IntervalMetricReader");
        }
}
