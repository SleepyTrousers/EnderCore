package com.enderio.core.common.transform;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.asm.transformers.DeobfuscationTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

public class NoClassloadClassWriter extends ClassWriter {
  
  private static final Logger logger = LogManager.getLogger();
  
  private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("legacy.debugClassLoading", "false"));
  private static final String OBJECT = "java/lang/Object";
  
  private static final Map<String, ClassNode> nodeCache = new HashMap<>();

  public NoClassloadClassWriter(int flags) {
    super(flags);
  }

  @Override
  protected String getCommonSuperClass(String type1, String type2) {
    String res = getCommonSuperClassImpl(type1, type2);
    if (DEBUG) logger.info("Common supertype of {} and {}: {}", type1, type2, res);
    return res;
  }
  
  private String getCommonSuperClassImpl(String type1, String type2) {
    if (type1.equals(OBJECT) || type2.equals(OBJECT)) { // Short circuit, don't bother loading Object from disk or cache
      return OBJECT;
    }
    
    ClassNode class1 = getClassNode(type1);
    ClassNode class2 = getClassNode(type2);
    
    if (class1 == null || class1.superName == null || class2 == null || class2.superName == null) {
      if (DEBUG) logger.info("Input was Object or null");
      return OBJECT;
    }

    if (class1.superName.equals(type2)) {
      if (DEBUG) logger.info("type1 super == type2");
      return type2;
    } else if (class2.superName.equals(type1)) {
      if (DEBUG) logger.info("type2 super == type1");
      return type1;
    } else if ((class1.access & Opcodes.ACC_INTERFACE) != 0 || (class2.access & Opcodes.ACC_INTERFACE) != 0) {
      if (DEBUG) logger.info("Input was interface");
      return OBJECT;
    }

    if (DEBUG) logger.info("Walking hierarchy");
    do {
      class1 = getClassNode(class1.superName);
      if (class1 == null || class1.superName == null) {
        if (DEBUG) logger.info("Reached object or null, recursing with {} and {}", type1, class2.superName);
        return getCommonSuperClassImpl(type1, class2.superName);
      }
      if (DEBUG) logger.info("Comparing {} to {}", class2.superName, class1.name);
    } while (!class2.superName.equals(class1.name));

    return class1.name;
  }
  
  private ClassNode getClassNode(String name) {
    ClassNode cn = nodeCache.get(name);
    if (cn != null) {
      return cn;
    }
    cn = new ClassNode();
    ClassReader cr;
    try {
      cr = new ClassReader(getClassBytes(name));
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      return null;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
    cr.accept(cn, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    nodeCache.put(name, cn);
    return cn;
  }
  
  private static final DeobfuscationTransformer deobf = new DeobfuscationTransformer();

  /*
   * Code from Mixin: https://github.com/SpongePowered/Mixin/
   * 
   * Licensed under MIT:
   * https://github.com/SpongePowered/Mixin/blob/master/LICENSE.txt
   * 
   * Modified to simplify for pure forge environment.
   */
  public byte[] getClassBytes(String className) throws ClassNotFoundException, IOException {
    String name = FMLDeobfuscatingRemapper.INSTANCE.unmap(className);
    String transformedName = className.replace('/', '.');

    byte[] classBytes = this.getClassBytes(name, transformedName);

    classBytes = deobf.transform(className, name, classBytes);

    if (classBytes == null) {
      throw new ClassNotFoundException(String.format("The specified class '%s' was not found", transformedName));
    }

    return classBytes;
  }
  
  private byte[] getClassBytes(String name, String transformedName) throws IOException {
    byte[] classBytes = getClassBytes(name, transformedName, Launch.classLoader);
    if (classBytes == null) {
      classBytes = getClassBytes(name, transformedName, (URLClassLoader) Launch.class.getClassLoader());
    }
    return classBytes;
  }
  
  private byte[] getClassBytes(String name, String transformedName, URLClassLoader classLoader) throws IOException {
    final String resourcePath = name.replace('.', '/').concat(".class");
    final String deobfResourcePath = transformedName.replace('.',  '/').concat(".class");
    if (DEBUG) logger.info("Attempting to load class {} with classloader {}", deobfResourcePath, classLoader);
    URL resource = classLoader.getResource(resourcePath);
    if (resource == null && !resourcePath.equals(deobfResourcePath)) {
      resource = classLoader.getResource(deobfResourcePath);
    }
    if (resource == null) {
      if (DEBUG) logger.info("No resource found");
      return null;
    }
    try (InputStream classStream = resource.openStream()) {
      return IOUtils.toByteArray(classStream);
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }
}
