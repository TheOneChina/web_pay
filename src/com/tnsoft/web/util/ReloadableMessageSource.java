/*
 * Copyright (c) 2016 Shanghai TNSOFT Co. Ltd.
 * All right reserved.
 * 
 */
package com.tnsoft.web.util;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.*;

public class ReloadableMessageSource extends ReloadableResourceBundleMessageSource {

    public Properties getProperties(Locale locale) {
        return super.getMergedProperties(locale).getProperties();
    }
}
