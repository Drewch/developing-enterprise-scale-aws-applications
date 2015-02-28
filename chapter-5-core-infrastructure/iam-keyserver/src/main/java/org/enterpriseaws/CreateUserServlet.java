package org.enterpriseaws;

import java.io.*;
import java.util.UUID;
import javax.servlet.*;
import javax.servlet.http.*;
import org.codehaus.jackson.map.ObjectMapper;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.AddUserToGroupRequest;
import com.amazonaws.services.identitymanagement.model.CreateAccessKeyRequest;
import com.amazonaws.services.identitymanagement.model.CreateAccessKeyResult;
import com.amazonaws.services.identitymanagement.model.CreateUserRequest;
import com.amazonaws.services.identitymanagement.model.CreateUserResult;

public class CreateUserServlet extends HttpServlet {

  private static AWSCredentials credentials;
  private final String GROUP              = "group";
  private final int FAILURE               = -1;
  private final int SUCCESS               = 1;
  private final String APPLICATION_JSON   = "application/json";

  static {
    try {
      credentials = new PropertiesCredentials(CreateUserServlet.class.getResourceAsStream("/AwsCredentials.properties"));
    } catch (IOException ioe) {
      throw new RuntimeException("Could not load AwsCredentials.properties on classpath!");
    }
  }

  public static enum IAM {
    SYNCHRONOUS;

    private AmazonIdentityManagement client;

    private IAM() {
      client = new AmazonIdentityManagementClient(credentials);
    }

    public AmazonIdentityManagement getClient() {
      return client;
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    CreateUserResponse createUserResponse = new CreateUserResponse();

    // Set response type to json
    response.setContentType(APPLICATION_JSON);

    // Create user, make it a random uuid
    String userName = UUID.randomUUID().toString();
    IAM.SYNCHRONOUS.client.createUser(new CreateUserRequest(userName));

    // Check if group was provided
    if( request.getParameter(GROUP) != null ) {

      // Put user in a group based on get parameter for group
      try {
        IAM.SYNCHRONOUS.client.addUserToGroup(
          new AddUserToGroupRequest(
              request.getParameter(GROUP),
              userName
              ));

        // Create an access key for the user
        CreateAccessKeyResult createAccessKeyResult = IAM.SYNCHRONOUS.client.createAccessKey(
            new CreateAccessKeyRequest().withUserName(userName));

        // Build successful response object
        createUserResponse.setStatus(SUCCESS);

        createUserResponse.setAccessKeyId(createAccessKeyResult
            .getAccessKey().getAccessKeyId());
        createUserResponse.setSecretAccessKey(createAccessKeyResult
            .getAccessKey().getSecretAccessKey());

      } catch(AmazonServiceException ase) {
        createUserResponse.setStatus(FAILURE);
        createUserResponse.setErr(String.format("Group %s does not exist",
            request.getParameter(GROUP)));
      }

    } else {

      // build failure response object
      createUserResponse.setStatus(FAILURE);
      createUserResponse.setErr("No group provided");
    }

    response.getWriter().write(new ObjectMapper().writeValueAsString(createUserResponse));
  }
}