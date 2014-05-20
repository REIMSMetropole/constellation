package org.constellation.ws.rest;

import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.constellation.engine.register.repository.DomainRepository;

@Path("/1/userXdomain")
public class UserXDomainService {

    @Inject
    private DomainRepository domainRepository;

    @POST
    @Path("/{domainId}/{userId}")
    public Response insert(@PathParam("userId") String userId,@PathParam("domainId") int domainId, Set<String> roles) {
        domainRepository.addUserToDomain(userId, domainId, roles);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{domainId}/{userId}")
    public Response delete(@PathParam("userId") String userId, @PathParam("domainId") int domainId) {
        domainRepository.removeUserFromDomain(userId, domainId);
        return Response.noContent().build();
    }

}
