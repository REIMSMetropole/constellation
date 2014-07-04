/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.rest.api;

import org.constellation.admin.MapContextBusiness;
import org.constellation.engine.register.Mapcontext;
import org.constellation.engine.register.repository.MapContextRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;


/**
 * Map context REST API.
 *
 * @author Cédric Briançon (Geomatys)
 */
@Path("/1/context")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class MapContextRest {
    @Inject
    private MapContextBusiness contextBusiness;

    @Inject
    private MapContextRepository contextRepository;

    @GET
    @Path("/list")
    public Response findAll() {
        return Response.ok(contextRepository.findAll()).build();
    }

    @PUT
    @Path("/")
    @Transactional
    public Response create(final Mapcontext mapContext) {
        contextRepository.create(mapContext);
        return Response.status(201).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") final int contextId) {
        contextRepository.delete(contextId);
        return Response.status(204).build();
    }
}