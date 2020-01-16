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
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.asm.transformers.DeobfuscationTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

public class NoClassloadClassWriter extends ClassWriter {

  private static class ClassInfo extends ClassVisitor {

    public int access;
    public String name, superName;

    public ClassInfo() {
      super(Opcodes.ASM5);
    }

    @Override
    public void visit(int version, @SuppressWarnings("hiding") int access, @SuppressWarnings("hiding") String name, String signature,
        @SuppressWarnings("hiding") String superName, String[] interfaces) {
      super.visit(version, access, name, signature, superName, interfaces);
      this.access = access;
      this.name = name;
      this.superName = superName;
    }

    boolean isValid() {
      return name != null && superName != null;
    }

    boolean isSuper(ClassInfo other) {
      return superName.equals(other.name);
    }

    boolean isInterface() {
      return (access & Opcodes.ACC_INTERFACE) != 0;
    }
  }

  private static final Logger logger = LogManager.getLogger();

  private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("legacy.debugClassLoading", "false"));
  private static final String OBJECT = "java/lang/Object";

  private static final Map<String, ClassInfo> nodeCache = new HashMap<>();

  public NoClassloadClassWriter(int flags) {
    super(flags);
  }

  @Override
  protected String getCommonSuperClass(String type1, String type2) {
    if (type1.equals(OBJECT) || type2.equals(OBJECT)) { // Short circuit, don't bother loading Object from disk or cache
      return OBJECT;
    }

    ClassInfo class1 = getClassInfo(type1);
    ClassInfo class2 = getClassInfo(type2);

    if (class1 == null || class2 == null) {
      if (DEBUG)
        logger.info("Class info was null");
      return OBJECT;
    }

    String res = getCommonSuperClass(class1, class2);
    if (DEBUG)
      logger.info("Common supertype of {} and {}: {}", type1, type2, res);
    return res;
  }

  private String getCommonSuperClass(ClassInfo class1, ClassInfo class2) {
    if (class1 == null || class1.superName == null || class2 == null || class2.superName == null) {
      if (DEBUG)
        logger.info("Input was Object or null");
      return OBJECT;
    }

    if (class1.isSuper(class2)) {
      if (DEBUG)
        logger.info("type1 super == type2");
      return class2.name;
    } else if (class2.isSuper(class1)) {
      if (DEBUG)
        logger.info("type2 super == type1");
      return class1.name;
    } else if (class1.isInterface() || class2.isInterface()) {
      if (DEBUG)
        logger.info("Input was interface");
      return OBJECT;
    }

    if (DEBUG)
      logger.info("Walking hierarchy");
    ClassInfo parent = class1;
    do {
      class1 = getClassInfo(class1.superName);
      if (class1 == null || class1.superName == null) {
        if (DEBUG)
          logger.info("Reached object or null, recursing with {} and {}", parent.name, class2.superName);
        class2 = getClassInfo(class2.superName);
        if (class2 == null || !class2.isValid()) {
          if (DEBUG)
            logger.info("Class 2 super was object or null");
          return OBJECT;
        }
        return getCommonSuperClass(parent, class2);
      }
      if (DEBUG)
        logger.info("Comparing {} to {}", class2.superName, class1.name);
    } while (!class2.isSuper(class1));

    return class1.name;
  }

  private ClassInfo getClassInfo(String name) {
    ClassInfo cn = nodeCache.get(name);
    if (cn != null) {
      return cn;
    }
    cn = new ClassInfo();
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
   * Licensed under MIT: https://github.com/SpongePowered/Mixin/blob/master/LICENSE.txt
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
    final String deobfResourcePath = transformedName.replace('.', '/').concat(".class");
    if (DEBUG)
      logger.info("Attempting to load class {} with classloader {}", deobfResourcePath, classLoader);
    URL resource = classLoader.getResource(resourcePath);
    if (resource == null && !resourcePath.equals(deobfResourcePath)) {
      resource = classLoader.getResource(deobfResourcePath);
    }
    if (resource == null) {
      if (DEBUG)
        logger.info("No resource found");
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
