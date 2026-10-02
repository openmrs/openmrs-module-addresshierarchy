/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.addresshierarchy.i18n;

import java.util.HashMap;
import java.util.Map;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openmrs.GlobalProperty;
import org.openmrs.api.LocationService;
import org.openmrs.api.OpenmrsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.AdvicePoint;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.module.exti18n.ExtI18nConstants;
import org.openmrs.module.exti18n.api.TestWithAOP;
import org.openmrs.module.exti18n.api.TestsMessageSource;
import org.openmrs.module.exti18n.icpt.AddressValuesAOPInterceptor;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

/**
 * Extend this class to run context sensitive tests with i18n enabled, including Spring AOP.
 * <p>
 * The AOP set up used to be inherited from exti18n's JUnit 4 AOPModuleContextSensitiveTest, it is inlined here because
 * JUnit 4 base classes no longer exist in OpenMRS Platform 3.0.
 */
abstract public class I18nModuleContextSensitiveTest extends BaseModuleContextSensitiveTest implements TestWithAOP {
	
	private Class<?> interceptorClass;
	
	private Map<Class<?>, Advice> servicesMap = new HashMap<Class<?>, Advice>();
	
	@Override
	public void setInterceptor(Class<? extends MethodInterceptor> interceptorClass) {
		this.interceptorClass = interceptorClass;
	}
	
	@Override
	public void addService(Class<? extends OpenmrsService> serviceClass) {
		servicesMap.put(serviceClass, null);
	}
	
	private void setupAOP() throws Exception {
		setInterceptorAndServices(this);
		
		for (Class<?> serviceClass : servicesMap.keySet()) {
			Advice advice = (Advice) (new AdvicePoint(serviceClass.getCanonicalName(),
			        Context.loadClass(interceptorClass.getCanonicalName()))).getClassInstance();
			servicesMap.put(serviceClass, advice);
			Context.addAdvice(Context.loadClass(serviceClass.getCanonicalName()), advice);
		}
	}
	
	private void tearDownAOP() throws Exception {
		for (Class<?> serviceClass : servicesMap.keySet()) {
			Context.removeAdvice(Context.loadClass(serviceClass.getCanonicalName()), servicesMap.get(serviceClass));
		}
	}
	
	protected final Log log = LogFactory.getLog(getClass());
	
	protected static final String XML_DATASET_PACKAGE_PATH = "org/openmrs/module/addresshierarchy/include/addressHierarchy-i18n-dataset.xml";
	
	protected AddressHierarchyService ahService;
	
	protected TestsMessageSource getTestsMessageSource() {
		return (TestsMessageSource) Context.getMessageSourceService().getActiveMessageSource();
	}
	
	/*
	 * pre-Spring loading setup
	 */
	public I18nModuleContextSensitiveTest() {
		super();
		ModuleFactory.getStartedModulesMap().put("exti18n", new Module("", "exti18n", "", "", "", "1.0.0", ""));
	}
	
	protected void setInterceptorAndServices(TestWithAOP testCase) {
		testCase.setInterceptor(AddressValuesAOPInterceptor.class);
		testCase.addService(LocationService.class);
		testCase.addService(PersonService.class);
		testCase.addService(PatientService.class);
	}
	
	@BeforeEach
	public void setupI18n() throws Exception {
		setupAOP();
		
		initializeInMemoryDatabase();
		authenticate();
		executeDataSet(INITIAL_XML_DATASET_PACKAGE_PATH);
		executeDataSet(EXAMPLE_XML_DATASET_PACKAGE_PATH);
		executeDataSet(XML_DATASET_PACKAGE_PATH);
		
		// Loading message properties files
		getTestsMessageSource()
		        .addMessageProperties("org/openmrs/module/addresshierarchy/include/addresshierarchy.properties");
		getTestsMessageSource()
		        .addMessageProperties("org/openmrs/module/addresshierarchy/include/addresshierarchy_fr.properties");
		getTestsMessageSource().refreshCache();
		
		Context.getAdministrationService()
		        .saveGlobalProperty(new GlobalProperty(ExtI18nConstants.GLOBAL_PROP_REV_I18N_SUPPORT, "true"));
		
		ahService = Context.getService(AddressHierarchyService.class);
		ahService.initI18nCache();
	}
	
	@AfterEach
	public void tearDownI18n() throws Exception {
		Context.getAdministrationService()
		        .saveGlobalProperty(new GlobalProperty(ExtI18nConstants.GLOBAL_PROP_REV_I18N_SUPPORT, "false"));
		ahService.resetI18nCache();
		ModuleFactory.getStartedModulesMap().clear();
		
		tearDownAOP();
	}
}
