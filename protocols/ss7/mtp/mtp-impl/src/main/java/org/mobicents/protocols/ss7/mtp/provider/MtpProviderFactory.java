/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.protocols.ss7.mtp.provider;

import java.lang.reflect.Constructor;
import java.util.Properties;

import org.mobicents.protocols.ConfigurationException;
import org.mobicents.protocols.ss7.mtp.provider.m3ua.Provider;

/**
 * @author baranowb
 * @deprecated ..... no words
 */
public class MtpProviderFactory {
	
	/**
	 * Property which controls driver.
	 */
	public static final String PROPERTY_MTP_DRIVER = "mtp.driver";
	
	public static final String DRIVER_M3UA = "m3ua";
	
	private final static MtpProviderFactory instance = new MtpProviderFactory();
	/**
	 * 
	 */
	private MtpProviderFactory() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * @return the instance
	 */
	public static MtpProviderFactory getInstance() {
		return instance;
	}

	
	public MtpProvider getProvider(Properties props) throws ConfigurationException
	{
		MtpProvider p = null;
		//only TCP supported now
		String d = props.getProperty(PROPERTY_MTP_DRIVER, DRIVER_M3UA);
		if(d.toLowerCase().equals(DRIVER_M3UA))
		{
			p = new Provider();
		}else
		{
			//try as class
			
			try {
				Class clazz = Class.forName(d);
				Constructor<MtpProvider> c =  clazz.getConstructor(null);
				p =  c.newInstance(null);
				
			} catch (Exception e) {
				throw new UnsupportedOperationException("Failed to create driver for class: "+d,e);
			}
			
		}
		p.configure(props);
		return p;
		
	}
	
}