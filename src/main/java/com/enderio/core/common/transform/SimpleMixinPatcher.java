package com.enderio.core.common.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.enderio.core.common.transform.EnderCorePlugin.MixinData;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.launchwrapper.IClassTransformer;

import static com.enderio.core.common.transform.EnderCorePlugin.mixinLogger;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class SimpleMixinPatcher implements IClassTransformer {
  
  private final EnderCorePlugin plugin = EnderCorePlugin.instance();
  
  private final Map<String, byte[]> capturedSources = new HashMap<>();
  private final Multimap<String, MixinData> interfaceTargets = HashMultimap.create();

  @Override
  public byte[] transform(String name, String transformedName, byte[] targetClass) {
    if (targetClass == null) {
      return targetClass;
    }
    List<MixinData> patches = new ArrayList<>();
    for (MixinData d : plugin.mixins) {
      if (d.target.equals(transformedName)) {
        patches.add(d);
      }
      if (d.source.equals(transformedName)) {
        capturedSources.put(transformedName, targetClass);
        mixinLogger.info("Found mixin source class data for {}.", transformedName);
      }
    }
    if (patches.isEmpty() && interfaceTargets.isEmpty()) {
      return targetClass;
    }

    ClassNode targetNode = new ClassNode();
    ClassReader targetReader = new ClassReader(targetClass);
    targetReader.accept(targetNode, 0);
    
    // If this is a directly targeted interface, track it for later
    if (!patches.isEmpty() && (targetNode.access & Opcodes.ACC_INTERFACE) != 0) {
      interfaceTargets.putAll(targetNode.name, patches);
      mixinLogger.info("Found interface target {}", transformedName);
      return targetClass;
    }
    
    // Check for tracked interfaces on this class, if we know of any
    if (!interfaceTargets.isEmpty()) {
      int patchCount = patches.size();
      targetNode.interfaces.stream()
                           .map(interfaceTargets::get)
                           .filter(Objects::nonNull)
                           .forEach(patches::addAll);
      if (patches.size() > patchCount) {
        int count = patches.size() - patchCount;
        mixinLogger.info("Found {} {} to apply to class {} from implemented interfaces: {}", count, count > 1 ? "patches" : "patch", transformedName, targetNode.interfaces.stream().filter(interfaceTargets::containsKey).toArray());
      }
    }
    
    if (!patches.isEmpty()) {
      
      if (targetNode.visibleAnnotations != null && (targetNode.visibleAnnotations.stream()
          .anyMatch(n -> n.desc.contains("com/enderio/core/common/transform/SimpleMixin") || n.desc.contains("com/enderio/core/common/mixin/SimpleMixin")))) {
        mixinLogger.info("Not mixing into class {} because it is itself a mixin", transformedName);
        return targetClass;
      }
      
      mixinLogger.info("Patching {} mixins onto class {} ({})", patches.size(), transformedName,
          patches.stream().map(data -> data.source.replaceFirst("^.*\\.", "")).collect(Collectors.joining(", ")));
      
      for (MixinData data : patches) {
        byte[] sourceClass = capturedSources.get(data.source);
        if (sourceClass == null) {
          mixinLogger.error("Skipping mixin patch due to unloaded class: " + data.source);
        } else {
  
          ClassNode sourceNode = new ClassNode();
          ClassReader sourceReader = new ClassReader(sourceClass);
          sourceReader.accept(sourceNode, 0);
          
          Type targetType = Type.getObjectType(targetNode.name);
          
          for (MethodNode m : sourceNode.methods) {
            ListIterator<AbstractInsnNode> instructions = m.instructions.iterator();
            while (instructions.hasNext()) {
              AbstractInsnNode node = instructions.next();
              if (node instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) node;
                if (Type.getObjectType(call.owner).getClassName().equals(data.source)) {
                  call.owner = targetType.getInternalName();
                  if (call.getOpcode() == INVOKEINTERFACE) {
                    call.setOpcode(INVOKEVIRTUAL);
                    call.itf = false;
                  }
                }
              }
            }
            
            if (m.localVariables.size() > 0) {
              LocalVariableNode n = m.localVariables.get(0);
              n.desc = targetType.getDescriptor();
            }
          }
          
          List<String> newInterfaces = sourceNode.interfaces.stream()
              .filter(s -> !targetNode.interfaces.contains(s))
              .collect(Collectors.toList());
          
          if (!newInterfaces.isEmpty()) {
            targetNode.interfaces.addAll(newInterfaces);
            mixinLogger.info("Added {} new {}: {}", newInterfaces.size(), newInterfaces.size() == 1 ? "interface" : "interfaces", newInterfaces);
          }

          List<MethodNode> newMethods = sourceNode.methods.stream()
              .filter(m -> !m.name.equals("<init>"))
              .filter(m -> (m.access & ACC_ABSTRACT) == 0)
              .filter(m -> targetNode.methods.stream().filter(m2 -> m2.name.equals(m.name) && m2.desc.equals(m.desc)).count() == 0)
              .collect(Collectors.toList());
          
          if (!newMethods.isEmpty()) {
            targetNode.methods.addAll(newMethods);
            mixinLogger.info("Added {} new {}: {}", newMethods.size(), newMethods.size() == 1 ? "method" : "methods", newMethods.stream().map(m -> m.name + m.desc).toArray());
          }
        }
      }

      ClassWriter cw = new NoClassloadClassWriter(COMPUTE_MAXS | COMPUTE_FRAMES);
      targetNode.accept(cw);
      mixinLogger.info("Successfully patched.");
      return cw.toByteArray();
    }
    return targetClass;
  }

}
