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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.stream.XMLStreamException;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.StringList;
import org.constellation.dto.TaskStatus;
import org.constellation.scheduler.CstlScheduler;
import org.constellation.scheduler.Task;
import org.constellation.scheduler.TaskState;
import org.constellation.scheduler.Tasks;
import org.geotoolkit.feature.type.DefaultName;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.process.quartz.ProcessJobDetail;
import org.geotoolkit.xml.parameter.ParameterValueReader;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TriggerBuilder;

/**
 * RestFull API for task management/operations.
 * 
 * @author Johann Sorel (Geomatys)
 */
@Path("/1/task")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public final class TaskRest {
    
    /**
     * List running tasks.
     */
    @GET
    @Path("listTasks")
    public Response listTasks() {
        
        final CstlScheduler scheduler = CstlScheduler.getInstance();
        final List<Task> tasks = scheduler.listTasks();
        
        final Map<String, TaskStatus> lst = new HashMap<>();
        
        for(Task t : tasks){
            final TaskState state = scheduler.getaskState(t.getId());
            final TaskStatus status = new TaskStatus();
            status.setId(t.getId());
            status.setMessage(state.getMessage());
            status.setPercent(state.getPercent());
            status.setStatus(state.getStatus().name());
            status.setTitle(t.getTitle());
            lst.put(status.getId(), status);
        }
                
        return Response.ok(lst).build();
    }
    
    /**
     * Returns a list of all process available in the current factories.
     */
    @GET
    @Path("listProcesses")
    public Response listProcess(){
        final List<Name> names = Tasks.listProcess();
        final StringList lst = new StringList();
        for(Name n : names){
            lst.getList().add(DefaultName.toJCRExtendedForm(n));
        }
        return Response.ok(lst).build();
    }
    
    /**
     * Returns a list of all process available for the specified factory.
     * 
     * @param authorityCode
     */
    @GET
    @Path("process/factory/{authorityCode}")
    public Response listProcessForFactory(final @PathParam("authorityCode") String authorityCode) {
        return Response.ok(new StringList(Tasks.listProcessForFactory(authorityCode))).build();
    }
    
    /**
     * Returns a list of all process available in the current factories.
     */
    @Path("listProcessFactories")
    public Response listProcessFactories(){
        final List<String> names = Tasks.listProcessFactory();
        return Response.ok(new StringList(names)).build();
    }
    
    /**
     * Returns a description of the process parameters.
     */
    @GET
    @Path("process/descriptor/{authority}/{code}")
    public Response getProcessDescriptor(final @PathParam("authority") String authority, final @PathParam("code") String code) throws ConfigurationException {
        final ParameterDescriptorGroup idesc = getDescriptor(authority, code);
        return Response.ok(idesc).build();
    }
    
    @GET
    @Path("{id}")
    public Response getTaskParameters(final @PathParam("id") String id) throws ConfigurationException {
        final Task task = CstlScheduler.getInstance().getTask(id);

        if(task == null){
            return Response.ok(new AcknowlegementType("Failure", "Could not find task for given id.")).build();
        }

        final ParameterValueGroup origParam = task.getDetail().getParameters();

        //change the description, always encapsulate in the same namespace and name
        //jaxb object factory can not reconize changing names without a namespace
        ParameterDescriptorGroup idesc = origParam.getDescriptor();
        idesc = new DefaultParameterDescriptorGroup("input", idesc.descriptors().toArray(new GeneralParameterDescriptor[0]));
        final ParameterValueGroup iparams = idesc.createValue();
        iparams.values().addAll(origParam.values());

        return Response.ok(iparams).build();
    }

    @POST
    @Path("{id}/{authority}/{code}/{title}/{step}")
    public AcknowlegementType createTask(final @PathParam("authority") String authority, final @PathParam("code") String code, @PathParam("title") String title,
            final @PathParam("step") int step, final @PathParam("id") String id, final Object objectRequest) throws ConfigurationException, XMLStreamException {
        
        if(title == null || title.trim().isEmpty()){
            title = id;
        }

        final GeneralParameterDescriptor retypedDesc = getDescriptor(authority, code);

        final ParameterValueGroup params;
        final ParameterValueReader reader = new ParameterValueReader(retypedDesc);
        try {
            reader.setInput(objectRequest);
            params = (ParameterValueGroup) reader.read();
            reader.dispose();
        } catch (XMLStreamException | IOException ex) {
            throw new ConfigurationException(ex);
        }

        //rebuild original values since we have changed the namespace
        final ParameterDescriptorGroup originalDesc;
        try {
            originalDesc = ProcessFinder.getProcessDescriptor(authority,code).getInputDescriptor();
        } catch (NoSuchIdentifierException ex) {
            return new AcknowlegementType("Failure", "No process for given id.");
        }  catch (InvalidParameterValueException ex) {
            throw new ConfigurationException(ex);
        }
        final ParameterValueGroup orig = originalDesc.createValue();
        orig.values().addAll(params.values());


        final Task task = new Task(id);
        task.setTitle(title);
        task.setTrigger(TriggerBuilder.newTrigger()
                .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(step*60))
                .build());
        ProcessJobDetail detail = new ProcessJobDetail(authority, code, orig);
        task.setDetail(detail);
        
        try{
            CstlScheduler.getInstance().addTask(task);
        }catch(ConfigurationException ex){
            return new AcknowlegementType("Failure", "Failed to create task : "+ex.getMessage());
        }

        return new AcknowlegementType("Success", "The task has been created");
    }
    
    @PUT
    @Path("{id}/{authority}/{code}/{title}/{step}")
    public Object updateTask(final @PathParam("authority") String authority, final @PathParam("code") String code, @PathParam("title") String title,
            final @PathParam("step") int step, final @PathParam("id") String id, final Object objectRequest) throws ConfigurationException {

        if(title == null || title.trim().isEmpty()){
            title = id;
        }

        final GeneralParameterDescriptor retypedDesc = getDescriptor(authority, code);


        final ParameterValueGroup params;
        final ParameterValueReader reader = new ParameterValueReader(retypedDesc);
        try {
            reader.setInput(objectRequest);
            params = (ParameterValueGroup) reader.read();
            reader.dispose();
        } catch (XMLStreamException | IOException ex) {
            throw new ConfigurationException(ex);
        }

        //rebuild original values since we have changed the namespace
        final ParameterDescriptorGroup originalDesc;
        try {
            originalDesc = ProcessFinder.getProcessDescriptor(authority,code).getInputDescriptor();
        } catch (NoSuchIdentifierException ex) {
            return new AcknowlegementType("Failure", "No process for given id.");
        } catch (InvalidParameterValueException ex) {
            throw new ConfigurationException(ex);
        }
        final ParameterValueGroup orig = originalDesc.createValue();
        orig.values().addAll(params.values());


        final Task task = new Task(id);
        task.setTitle(title);
        task.setTrigger(TriggerBuilder.newTrigger()
                .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(step*60))
                .build());
        ProcessJobDetail detail = new ProcessJobDetail(authority, code, orig);
        task.setDetail(detail);

        try{
            if(CstlScheduler.getInstance().updateTask(task)){
                return new AcknowlegementType("Success", "The task has been updated.");
            }else{
                return new AcknowlegementType("Failure", "Could not find task for given id.");
            }
        }catch(ConfigurationException ex){
            return new AcknowlegementType("Failure", "Could not find task for given id : "+ex.getMessage());
        }
    }
    
    @DELETE
    @Path("{id}")
    public Object deleteTask(final @PathParam("id") String id) {
        try{
            if( CstlScheduler.getInstance().removeTask(id)){
                return new AcknowlegementType("Success", "The task has been deleted");
            }else{
                return new AcknowlegementType("Failure", "Could not find task for given id.");
            }
        }catch(ConfigurationException ex){
            return new AcknowlegementType("Failure", "Could not find task for given id : "+ex.getMessage());
        }
    }
    
    private ParameterDescriptorGroup getDescriptor(final String authority, final String code) throws ConfigurationException {
        final ProcessDescriptor desc;
        try {
            desc = ProcessFinder.getProcessDescriptor(authority,code);
        } catch (NoSuchIdentifierException ex) {
            throw new ConfigurationException("No Process for id : {" + authority + "}"+code+" has been found");
        } catch (InvalidParameterValueException ex) {
            throw new ConfigurationException(ex);
        }
        if(desc == null){
            throw new ConfigurationException("No Process for id : {" + authority + "}"+code+" has been found");
        }

        //change the description, always encapsulate in the same namespace and name
        //jaxb object factory can not reconize changing names without a namespace
        ParameterDescriptorGroup idesc = desc.getInputDescriptor();
        idesc = new DefaultParameterDescriptorGroup("input", idesc.descriptors().toArray(new GeneralParameterDescriptor[0]));
        return idesc;
    }
}