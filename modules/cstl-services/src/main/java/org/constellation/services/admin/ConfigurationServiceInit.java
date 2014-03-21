package org.constellation.services.admin;

import java.lang.invoke.MethodHandles;

import org.constellation.admin.ConfigurationEngine;
import org.constellation.engine.register.ConfigurationService;
import org.constellation.security.SecurityManagerHolder;
import org.constellation.util.ReflectionUtilities;
import org.constellation.ws.DIEnhancer;
import org.constellation.ws.WSEngine;
import org.constellation.ws.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * This class wires spring managed beans with legacy code.
 * {@link ConfigurationServiceInit#init()} method will inject a {@link DIEnhancer} to {@link WSEngine}
 * to allow Spring instantiation of OGC Workers.
 * 
 * @author Olivier NOUGUIER
 *
 */
public class ConfigurationServiceInit implements ApplicationContextAware {
    
    
    private final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private ConfigurationService configurationService;
    
    /**
     * Spring applucation context.
     */
    private ApplicationContext applicationContext;

    public void init() {
        ConfigurationEngine.setConfigurationService(configurationService);
        ConfigurationEngine.setSecurityManager(SecurityManagerHolder.getInstance());

        WSEngine.setWorkerFactory(new DIEnhancer() {

            @Override
            public Worker enhance(Class<? extends Worker> workerClass, String identifier) {
                String[] beanNames = applicationContext.getBeanNamesForType(workerClass);
                if(beanNames==null || beanNames.length == 0) {
                    LOGGER.info(workerClass.getName() + " is not managed by spring" );
                    return (Worker) ReflectionUtilities.newInstance(workerClass, identifier);
                }
                if(beanNames.length > 1) {
                    LOGGER.warn("There is more than one bean definition for workerClass: " + workerClass.getName());
                }
                
                return (Worker) applicationContext.getBean(beanNames[0], identifier);

            }

            
        });

    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
