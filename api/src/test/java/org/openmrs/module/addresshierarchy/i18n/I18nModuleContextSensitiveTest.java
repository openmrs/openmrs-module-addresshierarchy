package org.openmrs.module.addresshierarchy.i18n;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.openmrs.GlobalProperty;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.module.exti18n.ExtI18nConstants;
import org.openmrs.module.exti18n.api.AOPModuleContextSensitiveTest;
import org.openmrs.module.exti18n.api.TestWithAOP;
import org.openmrs.module.exti18n.api.TestsMessageSource;
import org.openmrs.module.exti18n.icpt.AddressValuesAOPInterceptor;

/**
 * Extend this class to run context sensitive tests with i18n enabled, including Spring AOP.
 */
abstract public class I18nModuleContextSensitiveTest extends AOPModuleContextSensitiveTest {
	
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
        ModuleFactory.getStartedModulesMap().put("exti18n", new Module("", "exti18n", "", "", "", "1.0.0", "") );
	}
	
	@Override
	protected void setInterceptorAndServices(TestWithAOP testCase) {
		testCase.setInterceptor(AddressValuesAOPInterceptor.class);
		testCase.addService(LocationService.class);
		testCase.addService(PersonService.class);
		testCase.addService(PatientService.class);
	}
	
	@Before
	public void setupI18n() throws Exception {
		
		// Loading message properties files
		getTestsMessageSource().addMessageProperties("org/openmrs/module/addresshierarchy/include/addresshierarchy.properties");
		getTestsMessageSource().addMessageProperties("org/openmrs/module/addresshierarchy/include/addresshierarchy_fr.properties");
		getTestsMessageSource().refreshCache();
		
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(ExtI18nConstants.GLOBAL_PROP_REV_I18N_SUPPORT, "true"));
		
		initializeInMemoryDatabase();
		authenticate();
		executeDataSet(XML_DATASET_PACKAGE_PATH);

		ahService = Context.getService(AddressHierarchyService.class);
		ahService.initI18nCache();
	}
	
	@After
	public void tearDownI18n() {
		Context.getAdministrationService().saveGlobalProperty(
		    new GlobalProperty(ExtI18nConstants.GLOBAL_PROP_REV_I18N_SUPPORT, "false"));
		ahService.resetI18nCache();
		ModuleFactory.getStartedModulesMap().clear();
	}
}
