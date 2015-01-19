package org.constellation.engine.register.i18n;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.constellation.configuration.CstlConfigurationRuntimeException;
import org.constellation.engine.register.jooq.tables.pojos.Style;
import org.constellation.engine.register.jooq.tables.pojos.StyleI18n;

public class StyleWithI18N extends Style {
    
    private Map<String, StyleI18n> styleI18ns;

    public StyleWithI18N(Style style, Map<String, StyleI18n> styleI18ns) {
        copyFrom(style);
        this.styleI18ns = styleI18ns;
    }

    
    private void copyFrom(Style style) {
//        setBody(style.getBody());
//        setDate(style.getDate());
//        setId(style.getId());
        try {
            BeanUtils.copyProperties(this,style);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new CstlConfigurationRuntimeException(e);
        }
        
    }


    public void setStyleI18ns(Map<String, StyleI18n> styleI18ns) {
        this.styleI18ns = styleI18ns;
    }
    
    public Map<String, StyleI18n> getStyleI18ns() {
        return styleI18ns;
    }
    
}
