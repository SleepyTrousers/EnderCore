package com.enderio.core.common.transform;

import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import com.enderio.core.common.transform.EnderCorePlugin.InterfacePatchData;

import net.minecraft.launchwrapper.IClassTransformer;

public class SimpleMixinPatcher implements IClassTransformer {
  
  public static final Logger logger = LogManager.getLogger();
  
  private final EnderCorePlugin plugin = EnderCorePlugin.instance();
  
  private final Map<String, byte[]> capturedSources = new HashMap<>();

  @Override
  public byte[] transform(String name, String transformedName, byte[] targetClass) {
    InterfacePatchData data = null;
    for (InterfacePatchData d : plugin.ifacePatches) {
      if (d.target.equals(transformedName)) {
        data = d;
        break;
      }
      if (d.source.equals(transformedName)) {
        capturedSources.put(transformedName, targetClass);
        break;
      }
    }
    if (data != null) {
      byte[] sourceClass = capturedSources.get(data.source);
      if (sourceClass == null) {
        logger.error("[EnderCore ASM] Skipping interface patch due to unloaded class: " + data.source);
      } else {
        ClassNode targetNode = new ClassNode();
        ClassReader targetReader = new ClassReader(targetClass);
        targetReader.accept(targetNode, 0);

        ClassNode sourceNode = new ClassNode();
        ClassReader sourceReader = new ClassReader(sourceClass);
        sourceReader.accept(sourceNode, 0);
        
        targetNode.interfaces.addAll(sourceNode.interfaces);
        targetNode.methods.addAll(sourceNode.methods.stream()
            .filter(m -> !m.name.equals("<init>"))
            .filter(m -> (m.access & ACC_ABSTRACT) == 0)
            .collect(Collectors.toList()));

        ClassWriter cw = new ClassWriter(0);
        targetNode.accept(cw);
        logger.info("Patched interfaces from {} onto {}.", data.source, data.target);
        return cw.toByteArray();
      }
    }
    return targetClass;
  }

}
