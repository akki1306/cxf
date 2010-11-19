/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.transport.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.xml.namespace.QName;

import org.apache.cxf.configuration.Configurer;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.transport.AbstractTransportFactory;
import org.apache.cxf.wsdl.http.AddressType;
import org.apache.cxf.wsdl11.WSDLEndpointFactory;


/**
 *
 */
public abstract class AbstractHTTPTransportFactory 
    extends AbstractTransportFactory 
    implements WSDLEndpointFactory {

    /**
     * This constant holds the prefixes served by this factory.
     */
    private static final Set<String> URI_PREFIXES = new HashSet<String>();
    static {
        URI_PREFIXES.add("http://");
        URI_PREFIXES.add("https://");
    }

    /**
     * This call is used by CXF ExtensionManager to inject the activationNamespaces
     * @param ans The transport ids.
     */
    
    public void setActivationNamespaces(Collection<String> ans) {
        setTransportIds(new ArrayList<String>(ans));
    }

    public EndpointInfo createEndpointInfo(
        ServiceInfo serviceInfo, 
        BindingInfo b, 
        List<?>     ees
    ) {
        if (ees != null) {
            for (Iterator itr = ees.iterator(); itr.hasNext();) {
                Object extensor = itr.next();
    
                if (extensor instanceof HTTPAddress) {
                    final HTTPAddress httpAdd = (HTTPAddress)extensor;
    
                    EndpointInfo info = new HttpEndpointInfo(serviceInfo, 
                                "http://schemas.xmlsoap.org/wsdl/http/");
                    info.setAddress(httpAdd.getLocationURI());
                    info.addExtensor(httpAdd);
                    return info;
                } else if (extensor instanceof AddressType) {
                    final AddressType httpAdd = (AddressType)extensor;
    
                    EndpointInfo info = 
                        new HttpEndpointInfo(serviceInfo, 
                                "http://schemas.xmlsoap.org/wsdl/http/");
                    info.setAddress(httpAdd.getLocation());
                    info.addExtensor(httpAdd);
                    return info;
                }
            }
        }
        
        HttpEndpointInfo hei = new HttpEndpointInfo(serviceInfo, 
            "http://schemas.xmlsoap.org/wsdl/http/");
        AddressType at = new HttpAddressType();
        hei.addExtensor(at);
        
        return hei;
    }

    public void createPortExtensors(EndpointInfo ei, Service service) {
        // TODO
    }

    public Set<String> getUriPrefixes() {
        return URI_PREFIXES;
    }

    /**
     * This call uses the Configurer from the bus to configure
     * a bean.
     * 
     * @param bean
     */
    protected void configure(Object bean) {
        configure(bean, null, null);
    }
    
    protected void configure(Object bean, String name, String extraName) {
        Configurer configurer = bus.getExtension(Configurer.class);
        if (null != configurer) {
            configurer.configureBean(name, bean);
            if (extraName != null) {
                configurer.configureBean(extraName, bean);
            }
        }
    }
    
    private static class HttpEndpointInfo extends EndpointInfo {
        AddressType saddress;
        HttpEndpointInfo(ServiceInfo serv, String trans) {
            super(serv, trans);
        }
        public void setAddress(String s) {
            super.setAddress(s);
            if (saddress != null) {
                saddress.setLocation(s);
            }
        }

        public void addExtensor(Object el) {
            super.addExtensor(el);
            if (el instanceof AddressType) {
                saddress = (AddressType)el;
            }
        }
    }    
    
    private static class HttpAddressType extends AddressType 
        implements HTTPAddress, SOAPAddress {
        public HttpAddressType() {
            super();
            setElementType(new QName("http://schemas.xmlsoap.org/wsdl/soap/", "address"));
        }
        
        public String getLocationURI() {
            return getLocation();
        }

        public void setLocationURI(String locationURI) {
            setLocation(locationURI);
        }
        
    }

}
