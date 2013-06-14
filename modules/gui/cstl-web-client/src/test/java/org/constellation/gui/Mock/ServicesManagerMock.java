package org.constellation.gui.mock;

import org.constellation.dto.Service;
import org.constellation.gui.service.ServicesManager;

import javax.enterprise.inject.Specializes;
import java.util.logging.Logger;

/**
 * ServiceManager mock
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 *
 */
@Specializes
public class ServicesManagerMock extends ServicesManager{

    private static final Logger LOGGER = Logger.getLogger(ServicesManagerMock.class.getName());

    /**
     * create service with {@link org.constellation.dto.Service} capabilities information
     *
     * @param createdService {@link org.constellation.dto.Service} object which contain capability service information
     * @param service        service type as {@link String}
     * @return <code>true</code> if succeded, <code>false</code> if not succeded
     */
    @Override
    public boolean createServices(Service createdService, String service) {
        return true;
    }
}
