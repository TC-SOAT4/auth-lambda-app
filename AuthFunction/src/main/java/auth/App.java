package auth;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    String ACCESS_KEY = System.getenv("ACCESS_KEY");
    String SECRET_KEY = System.getenv("SECRET_KEY");
    String CLIENT_ID = System.getenv("CLIENT_ID");

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Custom-Header", "application/json");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);

        try {
            HashMap<String, String> parameters = (HashMap<String, String>) input.getQueryStringParameters();

            String output = signin(parameters.get("username"), parameters.get("password"));

            return response
                    .withStatusCode(200)
                    .withBody(output);
        } catch (Exception e) {
            return response
                    .withBody(e.getMessage())
                    .withStatusCode(500);
        }
    }

    private String signin(String username, String password) throws JsonProcessingException {

        CognitoIdentityProviderClient providerClient = buildCognitoIdentityProviderClient();
        InitiateAuthRequest authRequest = buildInitiateAuthRequest(username, password);

        Map<String, String> responseHash = new HashMap<>();

         // Enviar a solicitação de autenticação e obter a resposta
         InitiateAuthResponse authResponse = providerClient.initiateAuth(authRequest);
            
         // Obter o token de acesso e outros detalhes da resposta de autenticação
         AuthenticationResultType authenticationResult = authResponse.authenticationResult();
         String accessToken = authenticationResult.accessToken();
         String idToken = authenticationResult.idToken();
        //  String refreshToken = authenticationResult.refreshToken();

         responseHash.put("idToken", idToken);
         responseHash.put("accessToken", accessToken);
         
        //  responseHash.put("refreshToken", refreshToken);
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.writeValueAsString(responseHash);
    }

    private StaticCredentialsProvider buildBredentialsProvider() {
       
        return StaticCredentialsProvider.create(
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
