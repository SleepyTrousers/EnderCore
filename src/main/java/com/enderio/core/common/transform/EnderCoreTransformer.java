package com.enderio.core.common.transform;

import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;

import static com.enderio.core.common.transform.EnderCorePlugin.mainLogger;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;

public class EnderCoreTransformer implements IClassTransformer {

  protected static class ObfSafeName {
    final String deobf, srg;

    public ObfSafeName(String deobf, String srg) {
      this.deobf = deobf;
      this.srg = srg;
    }

    public String getName() {
      return EnderCorePlugin.runtimeDeobfEnabled ? srg : deobf;
    }

    public boolean equals(String obj) {
      if (obj != null) {
        return obj.equals(deobf) || obj.equals(srg);
      }
      return false;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof String) {
        return obj.equals(deobf) || obj.equals(srg);
      } else if (obj instanceof ObfSafeName) {
        return ((ObfSafeName) obj).deobf.equals(deobf) && ((ObfSafeName) obj).srg.equals(srg);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

  }

  protected static abstract class Transform {
    abstract void transform(Iterator<MethodNode> methods);
  }

  static final String containerFurnaceClass = "net.minecraft.inventory.ContainerFurnace";
  static final ObfSafeName containerFurnaceMethod = new ObfSafeName("transferStackInSlot", "func_82846_b");
  static final String containerFurnaceMethodSig = "(Lnet/minecraft/inventory/ContainerFurnace;Lnet/minecraft/entity/player/EntityPlayer;I)Lnet/minecraft/item/ItemStack;";

  static final String renderItemClass = "net.minecraft.client.renderer.RenderItem";
  static final ObfSafeName renderItemOverlayIntoGUIMethod = new ObfSafeName("renderItemOverlayIntoGUI", "func_180453_a");
  static final ObfSafeName renderItemAndEffectIntoGUI = new ObfSafeName("renderItemAndEffectIntoGUI", "func_184391_a");
  static final ObfSafeName renderItemDisplayName = new ObfSafeName("renderItemAndEffectIntoGUI, renderItemOverlayIntoGUI", "func_180453_a, func_184391_a");

  static final String itemFrameClass = "net.minecraft.entity.item.EntityItemFrame";
  static final ObfSafeName processInitialInteractMethod = new ObfSafeName("processInitialInteract", "func_184230_a");

  static final String entityPlayerClass = "net.minecraft.entity.player.EntityPlayer";

  static final String entityAICreeperSwellClass = "net.minecraft.entity.ai.EntityAICreeperSwell";
  static final ObfSafeName updateTaskMethod = new ObfSafeName("updateTask", "func_75246_d");

  static final String extendedBlockStorageClass = "net.minecraft.world.chunk.storage.ExtendedBlockStorage";
  static final ObfSafeName isEmptyMethod = new ObfSafeName("isEmpty", "func_76663_a");

  private final boolean inDev = System.getProperty("INDEV") != null;

  @Override
  public byte[] transform(String name, String transformedName, byte[] basicClass) {

    if (inDev && FMLLaunchHandler.side() == Side.SERVER) {
      // Eclipse's compiler suffers from https://bugs.openjdk.java.net/browse/JDK-6695379
      // Filter out methods that are known to be effected from this in a declared development environment only.
      // When compiled with a proper compiler, this will not be needed.
      ClassNode classNode = new ClassNode();
      ClassReader classReader = new ClassReader(basicClass);
      classReader.accept(classNode, 0);
      Iterator<MethodNode> methods = classNode.methods.iterator();
      while (methods.hasNext()) {
        MethodNode methodNode = methods.next();
        if (methodNode.name.equals("getClientGuiElement") && methodNode.desc.contains("GuiScreen")) {
          methods.remove();
        }
      }
      ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
      classNode.accept(cw);
      basicClass = cw.toByteArray();
    }

    // "Light data eaten instead of sent to client" fix (MC-80966)
    if (transformedName.equals(extendedBlockStorageClass)) {
      return transform(basicClass, extendedBlockStorageClass, isEmptyMethod, new Transform() {
        @Override
        void transform(Iterator<MethodNode> methods) {
          boolean done = false;
          while (methods.hasNext()) {
            MethodNode m = methods.next();
            if (isEmptyMethod.equals(m.name)) {
              m.instructions.clear();
              m.visitInsn(Opcodes.ICONST_0);
              m.visitInsn(Opcodes.IRETURN);
              done = true;
              break;
            }
          }
          if (!done) {
            mainLogger.info("Transforming failed.");
          }
        }
      });
    }

    // Furnace Shift Click Fix
    if (transformedName.equals(containerFurnaceClass)) {
      return transform(basicClass, containerFurnaceClass, containerFurnaceMethod, new Transform() {
        @Override
        void transform(Iterator<MethodNode> methods) {
          boolean done = false;
          while (methods.hasNext()) {
            MethodNode m = methods.next();
            if (containerFurnaceMethod.equals(m.name)) {
              m.instructions.clear();

              m.instructions.add(new VarInsnNode(ALOAD, 0));
              m.instructions.add(new VarInsnNode(ALOAD, 1));
              m.instructions.add(new VarInsnNode(ILOAD, 2));
              m.instructions.add(new MethodInsnNode(INVOKESTATIC, "com/enderio/core/common/transform/EnderCoreMethods", "transferStackInSlot",
                  containerFurnaceMethodSig, false));
              m.instructions.add(new InsnNode(Opcodes.ARETURN));

              done = true;
              break;
            }
          }
          if (!done) {
            mainLogger.info("Transforming failed.");
          }
        }
      });
    }

    // ItemFrame firing ItemDestroyed Event bug patch
    if (transformedName.equals(itemFrameClass)) {
      return transform(basicClass, itemFrameClass, processInitialInteractMethod, new Transform() {
        @Override
        void transform(Iterator<MethodNode> methods) {
          while (methods.hasNext()) {
            MethodNode m = methods.next();
            if (processInitialInteractMethod.equals(m.name)) {
              InsnList toAdd = new InsnList();
              toAdd.add(new VarInsnNode(ALOAD, 1));
              toAdd.add(new VarInsnNode(ALOAD, 2));
              toAdd.add(new MethodInsnNode(INVOKESTATIC, "com/enderio/core/common/transform/EnderCoreMethods", "processInitialInteract",
                  "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/EnumHand;)V", false));
              m.instructions.insert(toAdd);
              return;
            }
          }
          mainLogger.info("Transforming failed.");
        }
      });
    }

    // Item Overlay Rendering hook
    if (transformedName.equals(renderItemClass)) {
      return transform(basicClass, renderItemClass, renderItemDisplayName, new Transform() {
        @Override
        void transform(Iterator<MethodNode> methods) {
          int done = 0;
          while (methods.hasNext()) {
            MethodNode m = methods.next();
            if (renderItemAndEffectIntoGUI.equals(m.name) && "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;II)V".equals(m.desc)) {
              InsnList toAdd = new InsnList();
              toAdd.add(new VarInsnNode(ALOAD, 2));
              toAdd.add(new VarInsnNode(ILOAD, 3));
              toAdd.add(new VarInsnNode(ILOAD, 4));
              toAdd.add(new MethodInsnNode(INVOKESTATIC, "com/enderio/core/common/transform/EnderCoreMethods", "renderItemAndEffectIntoGUI",
                  "(Lnet/minecraft/item/ItemStack;II)V", false));
              m.instructions.insert(toAdd);
              done++;
            }
            if (renderItemOverlayIntoGUIMethod.equals(m.name)) {

              InsnList toAdd = new InsnList();
              toAdd.add(new VarInsnNode(ALOAD, 2));
              toAdd.add(new VarInsnNode(ILOAD, 3));
              toAdd.add(new VarInsnNode(ILOAD, 4));
              toAdd.add(new MethodInsnNode(INVOKESTATIC, "com/enderio/core/common/transform/EnderCoreMethods", "renderItemOverlayIntoGUI",
                  "(Lnet/minecraft/item/ItemStack;II)V", false));

              boolean primed = false, onframe = false, applied = false;
              Label target = null;
              for (int i = 0; i < m.instructions.size(); i++) {
                AbstractInsnNode next = m.instructions.get(i);

                // (1) find "if (stack.getItem().showDurabilityBar(stack)) {"
                if (!primed && target == null && next.getOpcode() == INVOKEVIRTUAL && next instanceof MethodInsnNode) {
                  if ("showDurabilityBar".equals(((MethodInsnNode) next).name)) { // Forge method, never obf'ed
                    primed = true;
                  }
                }

                // (2) where is the matching "}"?
                if (primed && next.getOpcode() == IFEQ && next instanceof JumpInsnNode) {
                  target = ((JumpInsnNode) next).label.getLabel();
                  primed = false;
                }

                // (3) insert our callback there
                if (target != null && next instanceof LabelNode && ((LabelNode) next).getLabel() == target) {
                  onframe = true;
                  continue;
                }
                if (onframe && next instanceof FrameNode) {
                  m.instructions.insert(next, toAdd);
                  done++;
                  applied = true;
                  break;
                }
              }
              if (!applied) {
                mainLogger.info("Transforming failed. Applying ersatz patch...");
                m.instructions.insert(toAdd);
                mainLogger.warn("Ersatz patch applied, things may break!");
                done++;
              }
              break;
            }
          }
          if (done != 2) {
            mainLogger.info("Transforming failed.");
          }
        }
      });
    }

    // Elytra flying
    if (transformedName.equals(entityPlayerClass)) {

      boolean deObf = !EnderCorePlugin.runtimeDeobfEnabled;

      ClassReader classReader = new ClassReader(basicClass);
      ClassWriter cw = new ClassWriter(classReader, 0);

      MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, deObf ? "isElytraFlying" : "func_184613_cA", "()Z", null, null);
      mv.visitCode();
      Label l0 = new Label();
      mv.visitLabel(l0);
      mv.visitLineNumber(42000, l0);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESPECIAL, "net/minecraft/entity/EntityLivingBase", deObf ? "isElytraFlying" : "func_184613_cA", "()Z", false);
      Label l1 = new Label();
      mv.visitJumpInsn(IFNE, l1);
      mv.visitVarInsn(ALOAD, 0);
      mv.visitMethodInsn(INVOKESTATIC, "com/enderio/core/common/transform/EnderCoreMethods", "isElytraFlying", "(Lnet/minecraft/entity/EntityLivingBase;)Z",
          false);
      mv.visitJumpInsn(IFNE, l1);
      mv.visitInsn(ICONST_0);
      mv.visitInsn(IRETURN);
      mv.visitLabel(l1);
      mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
      mv.visitInsn(ICONST_1);
      mv.visitInsn(IRETURN);
      Label l2 = new Label();
      mv.visitLabel(l2);
      mv.visitLocalVariable("this", "Lnet/minecraft/entity/player/EntityPlayer;", null, l0, l2, 0);
      mv.visitMaxs(1, 1);
      mv.visitEnd();

      classReader.accept(cw, 0);

      mainLogger
          .info("Transforming " + entityPlayerClass + " finished, added " + (deObf ? "isElytraFlying()" : "func_184613_cA()") + " overriding EntityLivingBase");
      return cw.toByteArray();
    }

    // Creeper exploding
    if (transformedName.equals(entityAICreeperSwellClass)) {
      return transform(basicClass, entityAICreeperSwellClass, updateTaskMethod, new Transform() {
        @Override
        void transform(Iterator<MethodNode> methods) {
          boolean done = false;
          while (methods.hasNext()) {
            MethodNode m = methods.next();
            if (updateTaskMethod.equals(m.name)) {
              boolean deObf = updateTaskMethod.deobf.equals(m.name);

              InsnList list = new InsnList();
              list.add(new VarInsnNode(ALOAD, 0));
              list.add(new FieldInsnNode(GETFIELD, "net/minecraft/entity/ai/EntityAICreeperSwell", deObf ? "swellingCreeper" : "field_75269_a",
                  "Lnet/minecraft/entity/monster/EntityCreeper;"));
              list.add(new VarInsnNode(ALOAD, 0));
              list.add(new FieldInsnNode(GETFIELD, "net/minecraft/entity/ai/EntityAICreeperSwell", deObf ? "creeperAttackTarget" : "field_75268_b",
                  "Lnet/minecraft/entity/EntityLivingBase;"));
              list.add(new MethodInsnNode(INVOKESTATIC, "com/enderio/core/common/transform/EnderCoreMethods", "isCreeperTarget",
                  "(Lnet/minecraft/entity/monster/EntityCreeper;Lnet/minecraft/entity/EntityLivingBase;)Z", false));
              LabelNode ldone = new LabelNode(new Label());
              list.add(new JumpInsnNode(Opcodes.IFNE, ldone));
              list.add(new VarInsnNode(ALOAD, 0));
              list.add(new FieldInsnNode(GETFIELD, "net/minecraft/entity/ai/EntityAICreeperSwell", deObf ? "swellingCreeper" : "field_75269_a",
                  "Lnet/minecraft/entity/monster/EntityCreeper;"));
              list.add(new InsnNode(Opcodes.ICONST_M1));
              list.add(
                  new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/entity/monster/EntityCreeper", deObf ? "setCreeperState" : "func_70829_a", "(I)V", false));
              list.add(new InsnNode(Opcodes.RETURN));
              list.add(ldone);
              list.add(new FrameNode(Opcodes.F_FULL, 1, new Object[] { "net/minecraft/entity/ai/EntityAICreeperSwell" }, 0, new Object[] {}));

              m.instructions.insert(list);

              done = true;
            }
          }
          if (!done) {
            mainLogger.info("Transforming failed.");
          }
        }
      });
    }

    return basicClass;
  }

  protected final static byte[] transform(byte[] classBytes, String className, ObfSafeName methodName, Transform transformer) {
    mainLogger.info("Transforming Class [" + className + "], Method [" + methodName.getName() + "]");

    ClassNode classNode = new ClassNode();
    ClassReader classReader = new ClassReader(classBytes);
    classReader.accept(classNode, 0);

    Iterator<MethodNode> methods = classNode.methods.iterator();

    transformer.transform(methods);

    ClassWriter cw = new ClassWriter(0);
    classNode.accept(cw);
    mainLogger.info("Transforming " + className + " Finished.");
    return cw.toByteArray();
  }

}
