/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.addresshierarchy.config;

import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.test.Verifies;
import org.openmrs.util.OpenmrsUtil;

public class ConfigDirUtilTest {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private ConfigDirUtil util;
	
	@Before
	public void setup() throws IOException {
		System.setProperty("user.home", Files.createTempDirectory(null).toString()); // see OpenmrsUtil.getApplicationDataDirectory()
		
		String configDirPath = new StringBuilder(OpenmrsUtil.getApplicationDataDirectory()).append("configuration")
		        .toString();
		String checksumsDirPath = new StringBuilder(OpenmrsUtil.getApplicationDataDirectory())
		        .append("configuration_checksums").toString();
		
		util = new ConfigDirUtil(configDirPath, checksumsDirPath, "addresshierarchy");
	}
	
	@Test
	@Verifies(value = "should read the latest written checksum and handle invalid checksum situations", method = "writeChecksum(String configFileName, String checksum)")
	public void writeChecksum_shouldHandleValidAndInvalidChecksums() {
		String configFileName = "foo.config";
		
		String checksum = "ad6821757a52c";
		util.writeChecksum(configFileName, checksum);
		Assert.assertEquals(checksum, util.readLatestChecksum(configFileName));
		
		checksum = ConfigDirUtil.NOT_COMPUTABLE_CHECKSUM;
		util.writeChecksum(configFileName, checksum);
		Assert.assertEquals(ConfigDirUtil.NOT_READABLE_CHECKSUM, util.readLatestChecksum(configFileName));
	}
}
