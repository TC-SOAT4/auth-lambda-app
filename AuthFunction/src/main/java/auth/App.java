package auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.plaf.synth.Region;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private String POOL_ID = "us-east-1_J0znZkNqF";
    private String CLIENT_ID = "1tontnuuutfv33u6jvnisclb10";

    private String ACCESS_KEY = "ASIA6ODU62N6BC2RHUJV";
    private String SECRET_KEY = "IjSRkM/WAxz3txzFJEAj2xP81M5SRAS/3YSTjDFB";

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);

        try {
            String output = signin("01367610389", 123456);

            return response
                    .withStatusCode(200)
                    .withBody(output);
        } catch (IOException e) {
            return response
                    .withBody("{}")
                    .withStatusCode(500);
        }
    }

    private signin(String username, String password) {

        CognitoIdentityProviderClient providerClient = buildCognitoIdentityProviderClient();
        InitiateAuthRequest authRequest = buildInitiateAuthRequest(username, password);

        Map<String, String> responseHash = new HashMap<>();

         // Enviar a solicitação de autenticação e obter a resposta
         InitiateAuthResponse authResponse = providerClient.initiateAuth(authRequest);
            
         // Obter o token de acesso e outros detalhes da resposta de autenticação
         AuthenticationResultType authenticationResult = authResponse.authenticationResult();
         String accessToken = authenticationResult.accessToken();
         String idToken = authenticationResult.idToken();
         String refreshToken = authenticationResult.refreshToken();

         responseHash.put("idToken", idToken);
         responseHash.put("accessToken", accessToken);
         
        //  responseHash.put("refreshToken", refreshToken);

        output = objectMapper.writeValueAsString(responseHash);
    }

    private String getPageContents(String address) throws IOException {
        URL url = new URL(address);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    private buildBredentialsProvider() {
        return  StaticCredentialsProvider.create(
            AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY));
    }

    private CognitoIdentityProviderClient buildCognitoIdentityProviderClient() {
        return CognitoIdentityProviderClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(buildBredentialsProvider())
                .build();
    }

    private InitiateAuthRequest buildInitiateAuthRequest(String username, String password) {

        Map<String, String> authParams = new HashMap<>();
        authParams.put("USERNAME", username);
        authParams.put("PASSWORD", password);

        return InitiateAuthRequest.builder()
                .clientId(CLIENT_ID)
                .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                .authParameters(authParams)
                .build();
    }

}
