/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2015, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package org.mobicents.servlet.sip.undertow;

import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.core.ManagedServlet;
import io.undertow.servlet.spec.ServletContextImpl;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.mobicents.servlet.sip.core.MobicentsSipServlet;

/**
 *
 * This class is based on org.mobicents.servlet.sip.catalina.SipServletImpl class from sip-servlet-as7 project, re-implemented
 * for jboss as8 (wildfly) by:
 *
 * @author kakonyi.istvan@alerant.hu
 *
 */
public class SipServletImpl extends ManagedServlet implements MobicentsSipServlet {

    private static final long serialVersionUID = 1L;
    /**
     * The descriptive information string for this implementation.
     */
    protected static final String INFO = "org.mobicents.servlet.sip.startup.loading.SipServletImpl/1.0";

    static final String[] DEFAULT_SIP_SERVLET_METHODS = new String[] { "INVITE", "ACK", "BYE", "CANCEL", "INFO", "MESSAGE",
            "SUBSCRIBE", "NOTIFY", "OPTIONS", "PRACK", "PUBLISH", "REFER", "REGISTER", "UPDATE", "SUCCESS_RESPONSE",
            "ERROR_RESPONSE", "BRANCH_RESPONSE", "REDIRECT_RESPONSE", "PROVISIONAL_RESPONSE" };

    private String icon;
    private String servletName;
    private String displayName;
    private String description;

    public SipServletImpl(ServletInfo servletInfo, ServletContextImpl servletContext) {
        super(servletInfo, servletContext);
    }

    /**
     * @return the icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * @return the servletName
     */
    public String getServletName() {
        return servletName;
    }

    /**
     * @param servletName the servletName to set
     */
    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    // FIXME: kakonyii: no registerJMX method in ManagedServlet superclass, so we will have to find another way to register this
    // servlet in JMX...
    // copied over from super class changing the JMX name being registered j2eeType is now SipServlet instead of Servlet
    // protected void registerJMX(StandardContext ctx) { ObjectName oname;
    // String parentName = ctx.getName(); parentName = ("".equals(parentName)) ? "/" : parentName;
    // String hostName = ctx.getParent().getName(); hostName = (hostName==null) ? "DEFAULT" : hostName;
    // String domain = ctx.getDomain();
    // String webMod= "//" + hostName + parentName; String onameStr = domain + ":j2eeType=SipServlet,name=" + getName()
    // + ",WebModule=" + webMod + ",J2EEApplication=" + ctx.getJ2EEApplication() + ",J2EEServer=" + ctx.getJ2EEServer();
    // try {
    // oname=new ObjectName(onameStr);
    // controller=oname; // GVAG: no more controller in the org.apache.catalina.core.ContainerBase
    // Registry.getRegistry(null, null).registerComponent(this, oname, null );
    // Send j2ee.object.created notification
    // if (this.getObjectName() != null) {
    // Notification notification = new Notification( "j2ee.object.created", this.getObjectName(), sequenceNumber++);
    // broadcaster.sendNotification(notification);
    // }
    // } catch( Exception ex ) {
    // super.getLogger().info("Error registering servlet with jmx " + this);
    // }
    // }

    /**
     * Return descriptive information about this Container implementation and the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {
        return (INFO);
    }

    /**
     * Gets the names of the methods supported by the underlying servlet.
     *
     * This is the same set of methods included in the Allow response header in response to an OPTIONS request method processed
     * by the underlying servlet.
     *
     * @return Array of names of the methods supported by the underlying servlet
     */
    public String[] getServletMethods() throws ServletException {
        return DEFAULT_SIP_SERVLET_METHODS;
    }

    @Override
    public String getName() {
        return super.getServletInfo().getName();
    }

    @Override
    public Servlet allocate() throws ServletException {
        return super.getServlet().getInstance();
    }

    @Override
    public void deallocate(Servlet servlet) throws ServletException {
        if (super.getServlet().getInstance() == servlet) {
            super.getServlet().release();
        }
    }

    @Override
    public boolean isUnavailable() {
        return super.isPermanentlyUnavailable();
    }

    @Override
    public int getLoadOnStartup() {
        return super.getServletInfo().getLoadOnStartup();
    }

}
