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
package org.constellation.engine.register.jpa.repository;

import org.constellation.engine.register.Provider;
import org.constellation.engine.register.jpa.ProviderEntity;
import org.constellation.engine.register.repository.ProviderRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProviderJpaRepository extends JpaRepository<ProviderEntity, Integer>, ProviderRepository {

    List<? extends Provider> findByImpl(String serviceName);
}
