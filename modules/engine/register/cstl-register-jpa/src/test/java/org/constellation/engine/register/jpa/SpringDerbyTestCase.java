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
package org.constellation.engine.register.jpa;

import org.constellation.engine.register.Provider;
import org.constellation.engine.register.repository.DataRepository;
import org.constellation.engine.register.repository.LayerRepository;
import org.constellation.engine.register.repository.ProviderRepository;
import org.constellation.engine.register.repository.ServiceRepository;
import org.constellation.engine.register.repository.StyleRepository;
import org.constellation.engine.register.repository.TaskRepository;
import org.constellation.engine.register.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.List;

//@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/spring/test-derby.xml")
public class SpringDerbyTestCase {

    private final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private StyleRepository styleRepository;

    @Autowired
    private DataRepository dataRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private LayerRepository layerRepository;

    @Autowired
    private TaskRepository taskRepository;

    

    @Test
    @Transactional
    public void listProvider() {
        dump(providerRepository.findAll());
        Provider provider = providerRepository.findOne(0);
        provider.getOwner();
    }

    @Test
    @Transactional
    public void listStyle() {
        dump(styleRepository.findAll());
    }

    @Test
    @Transactional
    public void listData() {
        dump(dataRepository.findAll());
    }

    @Test
    @Transactional
    public void listService() {
        dump(serviceRepository.findAll());
    }

    @Test
    @Transactional
    public void listLayer() {
        dump(layerRepository.findAll());
    }

    @Test
    @Transactional
    public void listTask() {
        dump(taskRepository.findAll());
    }

    @Test
    @Transactional
    public void saveUser() {
        UserEntity entity = new UserEntity();
        entity.setLogin("zoz");
        entity.setLastname("roro");
        entity.setFirstname("zozo");
        entity.setPassword("ppp");
        entity.setEmail("olivier.nouguier@gmail.com");
        userRepository.saveAndFlush(entity);
    }

    @Transactional
    public void deleteUser() {
        userRepository.delete("zozo");
    }

    private void dump(List<?> findAll) {
        for (Object object : findAll) {
            LOGGER.debug(object.toString());
        }
    }

}
