package com.enderio.core.common.transform;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

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
        
        for (MethodNode m : sourceNode.methods) {
          ListIterator<AbstractInsnNode> instructions = m.instructions.iterator();
          while (instructions.hasNext()) {
            AbstractInsnNode node = instructions.next();
            if (node instanceof MethodInsnNode) {
              MethodInsnNode call = (MethodInsnNode) node;
              if (call.owner.replace('/', '.').equals(data.source)) {
                call.owner = data.target.replace('.', '/');
                if (call.getOpcode() == INVOKEINTERFACE) {
                  call.setOpcode(INVOKEVIRTUAL);
                  call.itf = false;
                }
              }
            }
          }
          
          if (m.localVariables.size() > 0) {
            LocalVariableNode n = m.localVariables.get(0);
            n.desc = "L" + data.target.replace('.', '/') + ";";
          }
        }
        
        targetNode.interfaces.addAll(sourceNode.interfaces.stream()
            .filter(s -> !targetNode.interfaces.contains(s))
            .collect(Collectors.toList()));
        
        targetNode.methods.addAll(sourceNode.methods.stream()
            .filter(m -> !m.name.equals("<init>"))
            .filter(m -> (m.access & ACC_ABSTRACT) == 0)
            .collect(Collectors.toList()));

        ClassWriter cw = new ClassWriter(COMPUTE_MAXS | COMPUTE_FRAMES);
        targetNode.accept(cw);
        logger.info("Patched interfaces from {} onto {}.", data.source, data.target);
        return cw.toByteArray();
      }
    }
    return targetClass;
  }

}
