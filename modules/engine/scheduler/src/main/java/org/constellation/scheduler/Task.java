/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.scheduler;

import java.util.Date;
import java.util.UUID;

import org.geotoolkit.process.quartz.ProcessJobDetail;

import org.quartz.SimpleTrigger;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class Task {
        
    private String id = UUID.randomUUID().toString();
    private ProcessJobDetail detail = null;
    private SimpleTrigger trigger = null;
    private Date lastExecutionDate = null;
    private Exception lastFailedException = null;
    
    public Task(){
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public ProcessJobDetail getDetail() {
        return detail;
    }

    public void setDetail(final ProcessJobDetail detail) {
        this.detail = detail;
    }
    
    public Date getLastExecutionDate() {
        return lastExecutionDate;
    }

    public void setLastExecutionDate(final Date lastExecutionDate) {
        this.lastExecutionDate = lastExecutionDate;
    }
    
    public Exception getLastFailedException() {
        return lastFailedException;
    }

    public void setLastFailedException(final Exception lastFailedException) {
        this.lastFailedException = lastFailedException;
    }
    
    public SimpleTrigger getTrigger() {
        return trigger;
    }

    public void setTrigger(final SimpleTrigger trigger) {
        this.trigger = trigger;
    }

    @Override
    public String toString() {
        return super.toString();
    }
    
}
