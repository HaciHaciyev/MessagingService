package core.project.messaging.infrastructure.exceptions;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ExceptionHandler implements ExceptionMapper<IllegalArgumentException> {

  @Override
  public Response toResponse(IllegalArgumentException e) {
    return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
  }
}