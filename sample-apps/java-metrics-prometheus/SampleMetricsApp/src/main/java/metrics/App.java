package metrics;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.UUID;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static MetricEmitter buildMetricEmitter() {
        return new MetricEmitter();
    }

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

        MetricEmitter metricEmitter = buildMetricEmitter();
        for(int i = 0; i < 70; i++){
            // String uuid = UUID.randomUUID().toString();
            metricEmitter.emitQueueSizeChangeMetric(1, "/lambda-sample-app", "200", 1); // same uuid for all metrics
        }
        System.out.println("[I!]Returning from lambda handler...");

        metricEmitter.forceFlush();

        // Temporary hack:
        try {
        Thread.sleep(15000);
        } catch (InterruptedException e) {
        e.printStackTrace();
        }

        metricEmitter.shutdown();

        return response.withStatusCode(200).withBody("Status Code 200");
    }
}
