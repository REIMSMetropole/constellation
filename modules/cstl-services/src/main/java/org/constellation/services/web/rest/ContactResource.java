package org.constellation.services.web.rest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.constellation.engine.register.Property;
import org.constellation.engine.register.repository.PropertyRepository;
import org.constellation.utils.JSonUtils;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContactResource {

	@Inject
	private PropertyRepository propertyRepository;
	
	
	@RequestMapping(value = "/admin/contact", method = RequestMethod.GET, produces = "application/json")
	public Map<String, Object> get() {
		List<? extends Property> properties = propertyRepository.startWith("contact.%");
		Properties javaProperties = new Properties();
		for (Property property : properties)
			javaProperties.put(property.getKey(), property.getValue());

		
		
		
		return JSonUtils.toJSon(javaProperties);
	}

	@RequestMapping(value = "/admin/contact", method = RequestMethod.PUT)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Transactional
	public void put(@RequestBody HashMap<String, Object> contact) {

		Properties properties = JSonUtils.toProperties(contact);
		
		List<? extends Property> propertiesDB = propertyRepository.startWith("contact.%");
		for (Iterator iterator = propertiesDB.iterator(); iterator.hasNext();) {
			Property property = (Property) iterator.next();
			String posted = properties.getProperty(property.getKey());
			if(StringUtils.isNotBlank(posted)) {
				property.setValue(posted);
				properties.remove(property.getKey());
			}else {
				propertyRepository.delete(property);
			}
		}
		
		for (Entry<Object, Object> entry : properties.entrySet()) {
			Property prop = new Property((String)entry.getKey(), (String)entry.getValue());
			propertyRepository.save(prop);
		}
	}

}
