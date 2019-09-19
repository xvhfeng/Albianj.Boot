package test.org.albianj.framework.boot; 

import org.junit.Test; 
import org.junit.Before; 
import org.junit.After; 

/** 
* ApplicationContext Tester. 
* 
* @author <Authors name> 
* @since <pre>���� 8, 2019</pre> 
* @version 1.0 
*/ 
public class ApplicationContextTest { 

@Before
public void before() throws Exception { 
} 

@After
public void after() throws Exception { 
} 

/** 
* 
* Method: isWindows() 
* 
*/ 
@Test
public void testIsWindows() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: findRootBundleThread() 
* 
*/ 
@Test
public void testFindRootBundleThread() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: setAppStartupType(Class<?> appStartupType) 
* 
*/ 
@Test
public void testSetAppStartupType() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: setWorkFolder(String workFolder) 
* 
*/ 
@Test
public void testSetWorkFolder() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: setLoggerAttr(String logsPath, boolean isOpenConsole)
* 
*/ 
@Test
public void testSetRuntimeLogger() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: addBundle(Class<?> refType, String name, String workFolder, Class<?> bundleType, boolean isInstallSpxFile) 
* 
*/ 
@Test
public void testAddBundle() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: setLogger(ILogger logger) 
* 
*/ 
@Test
public void testSetLogger() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: findLogger() 
* 
*/ 
@Test
public void testFindLogger() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: attachBundle(Class<?> refType, String name, String workFolder, String bundleTypeName, boolean isInstallSpxFile, String[] args) 
* 
*/ 
@Test
public void testAttachBundle() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: isBundleExist(String bundleName) 
* 
*/ 
@Test
public void testIsBundleExist() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: findBundleContext(String bundleName, boolean isThrowIfBundleNotExit) 
* 
*/ 
@Test
public void testFindBundleContext() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: findCurrentBundleContext(Class<?> refType, boolean isThrowIfBundleNotExist) 
* 
*/ 
@Test
public void testFindCurrentBundleContext() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: run(String[] args) 
* 
*/ 
@Test
public void testRun() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: exitSystem(int st) 
* 
*/ 
@Test
public void testExitSystem() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getPhaseCode() 
* 
*/ 
@Test
public void testGetPhaseCode() throws Exception { 
//TODO: Test goes here... 
} 

/** 
* 
* Method: getDescription() 
* 
*/ 
@Test
public void testGetDescription() throws Exception { 
//TODO: Test goes here... 
} 


/** 
* 
* Method: buildApplicationRuntime(String[] args) 
* 
*/ 
@Test
public void testBuildApplicationRuntime() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = ApplicationContext.getClass().getMethod("buildApplicationRuntime", String[].class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

/** 
* 
* Method: loadAppConf(String sessionId, String confFolder) 
* 
*/ 
@Test
public void testLoadAppConf() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = ApplicationContext.getClass().getMethod("loadAppConf", String.class, String.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

/** 
* 
* Method: findConfigFile(String workFolder, String simpleFileName) 
* 
*/ 
@Test
public void testFindConfigFile() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = ApplicationContext.getClass().getMethod("findConfigFile", String.class, String.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

/** 
* 
* Method: parserAppBundleConf(XmlParserContext xmlParserCtx, String logsPath) 
* 
*/ 
@Test
public void testParserAppBundleConf() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = ApplicationContext.getClass().getMethod("parserAppBundleConf", XmlParserContext.class, String.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

/** 
* 
* Method: parserAppRuntimeLoggerConf(XmlParserContext xmlParserCtx, Node logNode, String logsPath) 
* 
*/ 
@Test
public void testParserAppRuntimeLoggerConf() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = ApplicationContext.getClass().getMethod("parserAppRuntimeLoggerConf", XmlParserContext.class, Node.class, String.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

/** 
* 
* Method: parserChildBundlesConf(XmlParserContext xmlParserCtx) 
* 
*/ 
@Test
public void testParserChildBundlesConfXmlParserCtx() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = ApplicationContext.getClass().getMethod("parserChildBundlesConf", XmlParserContext.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

/** 
* 
* Method: parserChildBundlesConf(XmlParserContext xmlParserCtx, Node bundleNode) 
* 
*/ 
@Test
public void testParserChildBundlesConfForXmlParserCtxBundleNode() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = ApplicationContext.getClass().getMethod("parserChildBundlesConf", XmlParserContext.class, Node.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

/** 
* 
* Method: repair(String sessionId, String logsPath, Map<String, BundleConf> attAttrs) 
* 
*/ 
@Test
public void testRepair() throws Exception { 
//TODO: Test goes here... 
/* 
try { 
   Method method = ApplicationContext.getClass().getMethod("repair", String.class, String.class, Map<String,.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/ 
} 

} 
