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
package org.mobicents.as8;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServer;





//import org.jboss.as.clustering.web.DistributedCacheManagerFactory;
import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.controller.services.path.PathManager;
import org.jboss.as.controller.services.path.PathManagerService;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.server.deployment.Phase;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder.DependencyType;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
//import org.mobicents.as7.clustering.sip.MockDistributedCacheManagerFactoryService;
import org.mobicents.as8.deployment.AttachSipServerServiceProcessor;
import org.mobicents.as8.deployment.SipAnnotationDeploymentProcessor;
import org.mobicents.as8.deployment.SipComponentProcessor;
import org.mobicents.as8.deployment.SipContextFactoryDeploymentProcessor;
import org.mobicents.as8.deployment.SipJndiBindingProcessor;
import org.mobicents.as8.deployment.SipParsingDeploymentProcessor;
import org.mobicents.as8.deployment.SipWarDeploymentProcessor;
import org.mobicents.as8.deployment.UndertowSipConnectorActivateProcessor;
import org.mobicents.as8.deployment.UndertowSipDeploymentInfoProcessor;
import org.mobicents.as8.deployment.UndertowSipDeploymentProcessor;
import org.mobicents.ext.javax.sip.dns.DefaultDNSServerLocator;
import org.wildfly.extension.undertow.UndertowService;

/**
 * Adds the sip subsystem.
 *
 * @author Emanuel Muckenhuber
 * @author josemrecio@gmail.com
 *
 *         This class is based on the contents of org.mobicents.as7 package from jboss-as7-mobicents project, re-implemented for
 *         jboss as8 (wildfly) by:
 * @author kakonyi.istvan@alerant.hu
 */
@SuppressWarnings({ "deprecation"})
class SipSubsystemAdd extends AbstractBoottimeAddStepHandler {

    // FIXME: these priorities should be substituted by values from with org.jboss.as.server.deployment.Phase
    // aligned with those used by web subsystem
    static int PARSE_SIP_DEPLOYMENT_PRIORITY = 0x4000;
    static int SIP_ANNOTATION_DEPLOYMENT_PRIORITY = 0x5000;
    static int SIP_CONTEXT_FACTORY_DEPLOYMENT_PRIORITY = 0x6000;

    static final SipSubsystemAdd INSTANCE = new SipSubsystemAdd();

    // private static final String DEFAULT_VIRTUAL_SERVER = "default-host";
    // private static final boolean DEFAULT_NATIVE = true;

    private SipSubsystemAdd() {
        //
    }

    // @Override
    // protected void populateModel(ModelNode operation, final Resource resource) {
    // SipConfigurationHandlerUtils.initializeConfiguration(resource, operation);
    // }

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        SipDefinition.INSTANCE_ID.validateAndSet(operation, model);
        SipDefinition.APPLICATION_ROUTER.validateAndSet(operation, model);
        SipDefinition.SIP_STACK_PROPS.validateAndSet(operation, model);
        SipDefinition.SIP_PATH_NAME.validateAndSet(operation, model);
        SipDefinition.SIP_APP_DISPATCHER_CLASS.validateAndSet(operation, model);
        SipDefinition.PROXY_TIMER_SERVICE_IMPEMENTATION_TYPE.validateAndSet(operation, model);
        SipDefinition.SAS_TIMER_SERVICE_IMPEMENTATION_TYPE.validateAndSet(operation, model);
        SipDefinition.CONGESTION_CONTROL_INTERVAL.validateAndSet(operation, model);
        SipDefinition.CONGESTION_CONTROL_POLICY.validateAndSet(operation, model);
        SipDefinition.CONCURRENCY_CONTROL_MODE.validateAndSet(operation, model);
        SipDefinition.USE_PRETTY_ENCODING.validateAndSet(operation, model);
        SipDefinition.GATHER_STATISTICS.validateAndSet(operation, model);
        SipDefinition.ADDITIONAL_PARAMETERABLE_HEADERS.validateAndSet(operation, model);
        SipDefinition.BASE_TIMER_INTERVAL.validateAndSet(operation, model);
        SipDefinition.T2_INTERVAL.validateAndSet(operation, model);
        SipDefinition.T4_INTERVAL.validateAndSet(operation, model);
        SipDefinition.TIMER_D_INTERVAL.validateAndSet(operation, model);
        SipDefinition.DIALOG_PENDING_REQUEST_CHECKING.validateAndSet(operation, model);
        SipDefinition.DNS_SERVER_LOCATOR_CLASS.validateAndSet(operation, model);
        SipDefinition.DNS_TIMEOUT.validateAndSet(operation, model);
        SipDefinition.DNS_RESOLVER_CLASS.validateAndSet(operation, model);
        SipDefinition.CANCELED_TIMER_TASKS_PURGE_PERIOD.validateAndSet(operation, model);
        SipDefinition.MEMORY_THRESHOLD.validateAndSet(operation, model);
        SipDefinition.BACK_TO_NORMAL_MEMORY_THRESHOLD.validateAndSet(operation, model);
        SipDefinition.OUTBOUND_PROXY.validateAndSet(operation, model);
    }

    @Override
    protected void performBoottime(OperationContext context, ModelNode operation, ModelNode model,
            ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
            throws OperationFailedException {

        ModelNode fullModel = Resource.Tools.readModel(context.readResource(PathAddress.EMPTY_ADDRESS));

        final ModelNode instanceIdModel = SipDefinition.INSTANCE_ID.resolveModelAttribute(context, fullModel);
        final String instanceId = instanceIdModel.isDefined() ? instanceIdModel.asString() : null;

        final ModelNode sipAppRouterFileModel = SipDefinition.APPLICATION_ROUTER.resolveModelAttribute(context, fullModel);
        final String sipAppRouterFile = sipAppRouterFileModel.isDefined() ? sipAppRouterFileModel.asString() : null;

        final ModelNode sipStackPropertiesFileModel = SipDefinition.SIP_STACK_PROPS.resolveModelAttribute(context, fullModel);
        final String sipStackPropertiesFile = sipStackPropertiesFileModel.isDefined() ? sipStackPropertiesFileModel.asString()
                : null;

        final ModelNode sipPathNameModel = SipDefinition.SIP_PATH_NAME.resolveModelAttribute(context, fullModel);
        final String sipPathName = sipPathNameModel.isDefined() ? sipPathNameModel.asString() : null;

        final ModelNode sipAppDispatcherClassModel = SipDefinition.SIP_APP_DISPATCHER_CLASS.resolveModelAttribute(context,
                fullModel);
        final String sipAppDispatcherClass = sipAppDispatcherClassModel.isDefined() ? sipAppDispatcherClassModel.asString()
                : null;

        final ModelNode usePrettyEncodingModel = SipDefinition.USE_PRETTY_ENCODING.resolveModelAttribute(context, fullModel);
        final boolean usePrettyEncoding = usePrettyEncodingModel.isDefined() ? usePrettyEncodingModel.asBoolean() : true;

        final ModelNode additionalParameterableHeadersModel = SipDefinition.ADDITIONAL_PARAMETERABLE_HEADERS
                .resolveModelAttribute(context, fullModel);
        final String additionalParameterableHeaders = additionalParameterableHeadersModel.isDefined() ? additionalParameterableHeadersModel
                .asString() : null;

        final ModelNode proxyTimerServiceImplementationTypeModel = SipDefinition.PROXY_TIMER_SERVICE_IMPEMENTATION_TYPE.resolveModelAttribute(
                context, fullModel);
        final String proxyTimerServiceImplementationType = proxyTimerServiceImplementationTypeModel.isDefined() ? proxyTimerServiceImplementationTypeModel
                .asString() : Constants.STANDARD;

        final ModelNode sasTimerServiceImplementationTypeModel = SipDefinition.SAS_TIMER_SERVICE_IMPEMENTATION_TYPE.resolveModelAttribute(
                context, fullModel);
        final String sasTimerServiceImplementationType = sasTimerServiceImplementationTypeModel.isDefined() ? sasTimerServiceImplementationTypeModel
                .asString() : Constants.STANDARD;

        final ModelNode sipCongestionControlIntervalModel = SipDefinition.CONGESTION_CONTROL_INTERVAL.resolveModelAttribute(
                context, fullModel);
        final int sipCongestionControlInterval = sipCongestionControlIntervalModel.isDefined() ? sipCongestionControlIntervalModel
                .asInt() : -1;

        final ModelNode congestionControlPolicyModel = SipDefinition.CONGESTION_CONTROL_POLICY.resolveModelAttribute(context,
                fullModel);
        final String congestionControlPolicy = congestionControlPolicyModel.isDefined() ? congestionControlPolicyModel
                .asString() : "ErrorResponse";

        final ModelNode sipConcurrencyControlModeModel = SipDefinition.CONCURRENCY_CONTROL_MODE.resolveModelAttribute(context,
                fullModel);
        final String sipConcurrencyControlMode = sipConcurrencyControlModeModel.isDefined() ? sipConcurrencyControlModeModel
                .asString() : null;

        final ModelNode baseTimerIntervalModel = SipDefinition.BASE_TIMER_INTERVAL.resolveModelAttribute(context, fullModel);
        final int baseTimerInterval = baseTimerIntervalModel.isDefined() ? baseTimerIntervalModel.asInt() : 500;

        final ModelNode t2IntervalModel = SipDefinition.T2_INTERVAL.resolveModelAttribute(context, fullModel);
        final int t2Interval = t2IntervalModel.isDefined() ? t2IntervalModel.asInt() : 4000;

        final ModelNode t4IntervalModel = SipDefinition.T4_INTERVAL.resolveModelAttribute(context, fullModel);
        final int t4Interval = t4IntervalModel.isDefined() ? t4IntervalModel.asInt() : 5000;

        final ModelNode timerDIntervalModel = SipDefinition.TIMER_D_INTERVAL.resolveModelAttribute(context, fullModel);
        final int timerDInterval = timerDIntervalModel.isDefined() ? timerDIntervalModel.asInt() : 32000;

        final ModelNode gatherStatisticsModel = SipDefinition.GATHER_STATISTICS.resolveModelAttribute(context, fullModel);
        final boolean gatherStatistics = gatherStatisticsModel.isDefined() ? gatherStatisticsModel.asBoolean() : false;
        
        final ModelNode dialogPendingRequestCheckingModel = SipDefinition.DIALOG_PENDING_REQUEST_CHECKING
                .resolveModelAttribute(context, fullModel);
        final boolean dialogPendingRequestChecking = dialogPendingRequestCheckingModel.isDefined() ? dialogPendingRequestCheckingModel
                .asBoolean() : false;

        final ModelNode dnsServerLocatorClassModel = SipDefinition.DNS_SERVER_LOCATOR_CLASS.resolveModelAttribute(context,
                fullModel);
        final String dnsServerLocatorClass = dnsServerLocatorClassModel.isDefined() ? dnsServerLocatorClassModel.asString()
                : DefaultDNSServerLocator.class.getName();

        final ModelNode dnsTimeoutModel = SipDefinition.DNS_TIMEOUT.resolveModelAttribute(context, fullModel);
        final int dnsTimeout = dnsTimeoutModel.isDefined() ? dnsTimeoutModel.asInt() : null;

        final ModelNode dnsResolverClassModel = SipDefinition.DNS_RESOLVER_CLASS.resolveModelAttribute(context, fullModel);
        final String dnsResolverClass = dnsResolverClassModel.isDefined() ? dnsResolverClassModel.asString() : null;

        final ModelNode callIdMaxLengthModel = SipDefinition.CALL_ID_MAX_LENGTH.resolveModelAttribute(context, fullModel);
        final int callIdMaxLength = callIdMaxLengthModel.isDefined() ? callIdMaxLengthModel.asInt() : -1;

        final ModelNode tagHashMaxLengthModel = SipDefinition.TAG_HASH_MAX_LENGTH.resolveModelAttribute(context, fullModel);
        final int tagHashMaxLength = callIdMaxLengthModel.isDefined() ? tagHashMaxLengthModel.asInt() : -1;

        final ModelNode canceledTimerTasksPurgePeriodModel = SipDefinition.CANCELED_TIMER_TASKS_PURGE_PERIOD
                .resolveModelAttribute(context, fullModel);
        final int canceledTimerTasksPurgePeriod = canceledTimerTasksPurgePeriodModel.isDefined() ? canceledTimerTasksPurgePeriodModel
                .asInt() : -1;

        final ModelNode memoryThresholdModel = SipDefinition.MEMORY_THRESHOLD.resolveModelAttribute(context, fullModel);
        final int memoryThreshold = memoryThresholdModel.isDefined() ? memoryThresholdModel.asInt() : -1;

        final ModelNode backToNormalMemoryThresholdModel = SipDefinition.BACK_TO_NORMAL_MEMORY_THRESHOLD.resolveModelAttribute(
                context, fullModel);
        final int backToNormalMemoryThreshold = backToNormalMemoryThresholdModel.isDefined() ? backToNormalMemoryThresholdModel
                .asInt() : -1;

        final ModelNode outboundProxyModel = SipDefinition.OUTBOUND_PROXY.resolveModelAttribute(context, fullModel);
        final String outboundProxy = outboundProxyModel.isDefined() ? outboundProxyModel.asString() : null;

        // final String instanceId = operation.hasDefined(Constants.INSTANCE_ID) ?
        // operation.get(Constants.INSTANCE_ID).asString() : null;
        // final String sipAppRouterFile = operation.hasDefined(Constants.APPLICATION_ROUTER) ?
        // operation.get(Constants.APPLICATION_ROUTER).asString() : null;
        // final String sipStackPropertiesFile = operation.hasDefined(Constants.SIP_STACK_PROPS) ?
        // operation.get(Constants.SIP_STACK_PROPS).asString() : null;
        // final String sipPathName = operation.hasDefined(Constants.SIP_PATH_NAME) ?
        // operation.get(Constants.SIP_PATH_NAME).asString() : null;
        // final String sipAppDispatcherClass = operation.hasDefined(Constants.SIP_APP_DISPATCHER_CLASS) ?
        // operation.get(Constants.SIP_APP_DISPATCHER_CLASS).asString() : null;
        // final int sipCongestionControlInterval = operation.hasDefined(Constants.CONGESTION_CONTROL_INTERVAL) ?
        // operation.get(Constants.CONGESTION_CONTROL_INTERVAL).asInt() : -1;
        // final String sipConcurrencyControlMode = operation.hasDefined(Constants.CONCURRENCY_CONTROL_MODE) ?
        // operation.get(Constants.CONCURRENCY_CONTROL_MODE).asString() : null;
        // final boolean usePrettyEncoding = operation.hasDefined(Constants.USE_PRETTY_ENCODING) ?
        // operation.get(Constants.USE_PRETTY_ENCODING).asBoolean() : true;

        if(sasTimerServiceImplementationType!=null &&
                !Constants.DEFAULT.equalsIgnoreCase(sasTimerServiceImplementationType) && !Constants.STANDARD.equalsIgnoreCase(sasTimerServiceImplementationType)) {
            throw new OperationFailedException("Invalid value is set for "+Constants.SAS_TIMER_SERVICE_IMPEMENTATION_TYPE+" property: "+sasTimerServiceImplementationType+"! Valid values are: "+Constants.DEFAULT +", "+Constants.STANDARD+".");
        }
        if(sasTimerServiceImplementationType!=null &&
                !Constants.DEFAULT.equalsIgnoreCase(proxyTimerServiceImplementationType) && !Constants.STANDARD.equalsIgnoreCase(proxyTimerServiceImplementationType)) {
            throw new OperationFailedException("Invalid value is set for "+Constants.PROXY_TIMER_SERVICE_IMPEMENTATION_TYPE+" property: "+proxyTimerServiceImplementationType+"! Valid values are: "+Constants.DEFAULT +", "+Constants.STANDARD+".");
        }

        final SipServerService service = new SipServerService(sipAppRouterFile, sipStackPropertiesFile, sipPathName,
                sipAppDispatcherClass, additionalParameterableHeaders, proxyTimerServiceImplementationType, sasTimerServiceImplementationType, sipCongestionControlInterval, congestionControlPolicy,
                sipConcurrencyControlMode, usePrettyEncoding, baseTimerInterval, t2Interval, t4Interval, timerDInterval,
                dialogPendingRequestChecking, dnsServerLocatorClass, dnsTimeout, dnsResolverClass, callIdMaxLength,
                tagHashMaxLength, canceledTimerTasksPurgePeriod, memoryThreshold, backToNormalMemoryThreshold, outboundProxy,
                instanceId);
        newControllers.add(context
                .getServiceTarget()
                .addService(SipSubsystemServices.JBOSS_SIP, service)
                .addDependency(PathManagerService.SERVICE_NAME, PathManager.class, service.getPathManagerInjector())
                .addDependency(DependencyType.OPTIONAL, ServiceName.JBOSS.append("mbean", "server"), MBeanServer.class,
                        service.getMbeanServer()).setInitialMode(Mode.ACTIVE).install());


        //lets find undertow subsystem's servlet container elements:
        //FIXME: kakonyii: maybe we should use global undertow constants instead of hard coded strings:
        ModelNode undertowNode = Resource.Tools.readModel(context.readResourceFromRoot(PathAddress.pathAddress("subsystem", "undertow")));
        ModelNode servletContainerNode = undertowNode.get("servlet-container");

        Set<String> servletContainerNames = servletContainerNode.keys();
        List<ServiceName> servletConatinerServiceNames = new ArrayList<>();
        for(String name : servletContainerNames){
            servletConatinerServiceNames.add(UndertowService.SERVLET_CONTAINER.append(name));
        }

        //add ConvergedServletContainerService to replace servletContainer to convergedServletContainer during startup:
        final ConvergedServletContainerService convergedServletContainerService = new ConvergedServletContainerService(context,servletConatinerServiceNames);
        newControllers.add(context
                .getServiceTarget()
                .addService(ConvergedServletContainerService.SERVICE_NAME, convergedServletContainerService)
                //depends on undertow servlet container services:
                .addDependencies(servletConatinerServiceNames)
                .setInitialMode(Mode.ACTIVE).install());

        context.addStep(new AbstractDeploymentChainStep() {
            @Override
            protected void execute(DeploymentProcessorTarget processorTarget) {
                // sip.xml parsing
                processorTarget.addDeploymentProcessor(SipExtension.SUBSYSTEM_NAME, Phase.PARSE, PARSE_SIP_DEPLOYMENT_PRIORITY,
                        new SipParsingDeploymentProcessor());
                // handles annotations
                processorTarget.addDeploymentProcessor(SipExtension.SUBSYSTEM_NAME, Phase.PARSE,
                        SIP_ANNOTATION_DEPLOYMENT_PRIORITY, new SipAnnotationDeploymentProcessor());
                // creates component's configurations (mainly for resolving reference annotations)
                // FIXME: should this priority be Phase.PARSE_WEB_COMPONENTS + 1?
                processorTarget.addDeploymentProcessor(SipExtension.SUBSYSTEM_NAME, Phase.PARSE,
                        SIP_ANNOTATION_DEPLOYMENT_PRIORITY + 1, new SipComponentProcessor());
                // attach SipServer to deployment unit (so we can recover it from the sip context)
                processorTarget.addDeploymentProcessor(SipExtension.SUBSYSTEM_NAME, Phase.POST_MODULE,
                        SIP_CONTEXT_FACTORY_DEPLOYMENT_PRIORITY - 1, new AttachSipServerServiceProcessor(service));
                // plugs our context factory into web subsystem deployers
                processorTarget.addDeploymentProcessor(SipExtension.SUBSYSTEM_NAME, Phase.POST_MODULE,
                        SIP_CONTEXT_FACTORY_DEPLOYMENT_PRIORITY, SipContextFactoryDeploymentProcessor.INSTANCE);
                // binds sip resources to JNDI - Before POST_MODULE_INJECTION_ANNOTATION !!!
                processorTarget.addDeploymentProcessor(SipExtension.SUBSYSTEM_NAME, Phase.POST_MODULE,
                        Phase.POST_MODULE_INJECTION_ANNOTATION - 1, new SipJndiBindingProcessor());
                // change DeploymentInfo to ConvergedDeploymentInfo in case of sip deployment in DeploymentInfoService using
                // reflection API
                processorTarget.addDeploymentProcessor(SipExtension.SUBSYSTEM_NAME, Phase.INSTALL,
                        Phase.INSTALL_WAR_DEPLOYMENT, new UndertowSipDeploymentInfoProcessor());
                // setup management objects for servlets, etc.
                processorTarget.addDeploymentProcessor(SipExtension.SUBSYSTEM_NAME, Phase.INSTALL,
                        Phase.INSTALL_WAR_DEPLOYMENT, new SipWarDeploymentProcessor());
                // add DeploymentInfoService to UndertowSipDeploymentInfoProcessor
                processorTarget.addDeploymentProcessor(SipExtension.SUBSYSTEM_NAME, Phase.INSTALL,
                        Phase.INSTALL_WAR_DEPLOYMENT + 1, new UndertowSipDeploymentProcessor());
                // add sip connector activator service:
                processorTarget.addDeploymentProcessor(SipExtension.SUBSYSTEM_NAME, Phase.INSTALL,
                        Phase.INSTALL_WAR_DEPLOYMENT + 2, new UndertowSipConnectorActivateProcessor());

                // // Add the SIP specific deployment processor
                // processorTarget.addDeploymentProcessor(Phase.PARSE, DEPLOYMENT_PROCESS_PRIORITY,
                // SipMetaDataDeploymentProcessor.INSTANCE);
                // // Add the context in a different phase
                // processorTarget.addDeploymentProcessor(Phase.POST_MODULE, DEPLOYMENT_PROCESS_PRIORITY,
                // SipContextFactoryDeploymentProcessor.INSTANCE);

                // final SharedWebMetaDataBuilder sharedWebBuilder = new SharedWebMetaDataBuilder(config.clone());
                // final SharedTldsMetaDataBuilder sharedTldsBuilder = new SharedTldsMetaDataBuilder(config.clone());
                //
                // processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.STRUCTURE,
                // Phase.STRUCTURE_WAR_DEPLOYMENT_INIT, new WarDeploymentInitializingProcessor());
                // processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.STRUCTURE, Phase.STRUCTURE_WAR, new
                // WarStructureDeploymentProcessor(sharedWebBuilder.create(), sharedTldsBuilder));
                // processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.PARSE, Phase.PARSE_WEB_DEPLOYMENT,
                // new WebParsingDeploymentProcessor());
                // processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.PARSE,
                // Phase.PARSE_WEB_DEPLOYMENT_FRAGMENT, new WebFragmentParsingDeploymentProcessor());
                // processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.PARSE, Phase.PARSE_JSF_VERSION, new
                // JsfVersionProcessor());
                // processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.PARSE,
                // Phase.PARSE_JBOSS_WEB_DEPLOYMENT, new JBossWebParsingDeploymentProcessor());
                // processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.PARSE, Phase.PARSE_TLD_DEPLOYMENT,
                // new TldParsingDeploymentProcessor());
                // processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.PARSE, Phase.PARSE_ANNOTATION_WAR,
                // new WarAnnotationDeploymentProcessor());
                // processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.PARSE, Phase.PARSE_WEB_COMPONENTS,
                // new WebComponentProcessor());
                // processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.PARSE,
                // Phase.PARSE_EAR_CONTEXT_ROOT, new EarContextRootProcessor());
                // processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.PARSE,
                // Phase.PARSE_WEB_MERGE_METADATA, new WarMetaDataProcessor());
                // processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.PARSE,
                // Phase.POST_MODULE_JSF_MANAGED_BEANS, new JsfManagedBeanProcessor());
                // processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.PARSE,
                // Phase.PARSE_WEB_INITIALIZE_IN_ORDER, new WebInitializeInOrderProcessor(defaultVirtualServer));

                // processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.DEPENDENCIES,
                // Phase.DEPENDENCIES_WAR_MODULE, new WarClassloadingDependencyProcessor());

                // processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.POST_MODULE,
                // Phase.POST_MODULE_JSF_MANAGED_BEANS, new JsfManagedBeanProcessor());
                // processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.INSTALL,
                // Phase.INSTALL_SERVLET_INIT_DEPLOYMENT, new ServletContainerInitializerDeploymentProcessor());
                // processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.INSTALL,
                // Phase.INSTALL_JSF_ANNOTATIONS, new JsfAnnotationProcessor());
                // processorTarget.addDeploymentProcessor(WebExtension.SUBSYSTEM_NAME, Phase.INSTALL,
                // Phase.INSTALL_WAR_DEPLOYMENT, new WarDeploymentProcessor(defaultVirtualServer));
            }
        }, OperationContext.Stage.RUNTIME);

        // FIXME: kakonyii, no MockDistributedCacheManagerFactoryService in wildfly, need to find another solution to install
        // this service:
        // final ServiceTarget target = context.getServiceTarget();
        // final DistributedCacheManagerFactory factory = new MockDistributedCacheManagerFactoryService().getValue();
        // if (factory != null) {
        // final InjectedValue<SipServer> server = new InjectedValue<SipServer>();
        // newControllers.add(
        // target.addService(MockDistributedCacheManagerFactoryService.JVM_ROUTE_REGISTRY_ENTRY_PROVIDER_SERVICE_NAME, new
        // JvmRouteRegistryEntryProviderService(server))
        // .addDependency(SipSubsystemServices.JBOSS_SIP, SipServer.class, server)
        // .setInitialMode(Mode.ON_DEMAND)
        // .install()
        // );
        // newControllers.addAll(factory.installServices(target));
        // }
    }

    @Override
    protected boolean requiresRuntimeVerification() {
        return false;
    }
}
