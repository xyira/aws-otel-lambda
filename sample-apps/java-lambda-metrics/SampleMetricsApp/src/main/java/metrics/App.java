package metrics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

        System.out.println("[I!]Launching sample app from lambda handler...");
        long requestStartTime = System.currentTimeMillis();
        MetricEmitter metricEmitter = buildMetricEmitter();
        System.out.println("[I!]Emitting latency metric...");
        long latency = System.currentTimeMillis() - requestStartTime;
        metricEmitter.emitReturnTimeMetric(latency, "/lambda-sample-app", "200");
        System.out.println("[I!]Returning from lambda handler...");

        try {
          System.out.println("[I!]Sleeping for 15s...");
          Thread.sleep(1000 * 15);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        
        return response.withStatusCode(200).withBody("Status Code 200");
    }
}
