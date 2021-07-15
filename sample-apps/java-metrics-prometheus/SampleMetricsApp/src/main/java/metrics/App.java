package metrics;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

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
        metricEmitter.emitQueueSizeChangeMetric(1, "/lambda-sample-app", "200");
        System.out.println("[I!]Returning from lambda handler...");

        // TODO: Call forceFlush() once Java Metrics SDK includes it, 
        // Temporary hack: 
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return response.withStatusCode(200).withBody("Status Code 200");
    }
}
