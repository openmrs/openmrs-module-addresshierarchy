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

public class ConfigLoaderUtilTest {

  protected final Log log = LogFactory.getLog(getClass());

  private ConfigLoaderUtil util;
  
  @Before
  public void setup() throws IOException {
    System.setProperty("user.home", Files.createTempDirectory(null).toString()); // see OpenmrsUtil.getApplicationDataDirectory()
    
    String configDirPath = new StringBuilder(OpenmrsUtil.getApplicationDataDirectory()).append("configuration").toString();
    
    util = new ConfigLoaderUtil(configDirPath);
  }

  @Test
  @Verifies(value = "should read the latest written checksum and handle invalid checksum situations", method = "writeChecksum(String configFileName, String checksum)")
  public void writeChecksum_shouldHandleValidAndInvalidChecksums() {
    String configFileName = "foo.config";

    String checksum = "ad6821757a52c";
    util.writeChecksum(configFileName, checksum);
    Assert.assertEquals(checksum, util.readLatestChecksum(configFileName));
    
    checksum = ConfigLoaderUtil.NOT_COMPUTABLE_CHECKSUM;
    util.writeChecksum(configFileName, checksum);
    Assert.assertEquals(ConfigLoaderUtil.NOT_READABLE_CHECKSUM, util.readLatestChecksum(configFileName));
  }
  
  @Test
  @Verifies(value = "should", method = "getChecksumFileName(String configFileName)")
  public void getChecksumFileName_should() {
    Assert.assertEquals("foo." + ConfigLoaderUtil.CHECKSUM_FILE_EXT, util.getChecksumFilePath("foo.config"));
    Assert.assertEquals("foo." + ConfigLoaderUtil.CHECKSUM_FILE_EXT, util.getChecksumFilePath("foo"));
    Assert.assertEquals("foo.bar." + ConfigLoaderUtil.CHECKSUM_FILE_EXT, util.getChecksumFilePath("foo.bar.config"));
    Assert.assertEquals("foo bar." + ConfigLoaderUtil.CHECKSUM_FILE_EXT, util.getChecksumFilePath("foo bar.config"));
    Assert.assertEquals(" foo bar  ." + ConfigLoaderUtil.CHECKSUM_FILE_EXT, util.getChecksumFilePath(" foo bar  .config"));
    
    Assert.assertEquals("subdir/foo." + ConfigLoaderUtil.CHECKSUM_FILE_EXT, util.getChecksumFilePath("subdir/foo.config"));
    Assert.assertEquals("subdir/foo." + ConfigLoaderUtil.CHECKSUM_FILE_EXT, util.getChecksumFilePath("subdir/foo"));
    Assert.assertEquals("subdir/foo.bar." + ConfigLoaderUtil.CHECKSUM_FILE_EXT, util.getChecksumFilePath("subdir/foo.bar.config"));
    Assert.assertEquals("subdir/foo bar." + ConfigLoaderUtil.CHECKSUM_FILE_EXT, util.getChecksumFilePath("subdir/foo bar.config"));
    Assert.assertEquals("subdir/ foo bar  ." + ConfigLoaderUtil.CHECKSUM_FILE_EXT, util.getChecksumFilePath("subdir/ foo bar  .config"));
  }
}
