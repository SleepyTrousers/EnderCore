package com.enderio.core.common.transform;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

@MCVersion(value = "1.11")
public class EnderCoreTransformer implements IClassTransformer {

  // The EnderCore class cannot be referenced from the transformer,
  // as it improperly triggers classloading of FML event classes.
  // The solution is to copy the logger into this class, instead of referencing
  // the static 'logger' field from EnderCore
  public static final Logger logger = LogManager.getLogger("EnderCore");

  protected static class ObfSafeName {
    final String deobf, srg;

    public ObfSafeName(String deobf, String srg) {
      this.deobf = deobf;
      this.srg = srg;
    }

    public String getName() {
      return EnderCorePlugin.runtimeDeobfEnabled ? srg : deobf;
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

  static final String anvilContainerClass = "net.minecraft.inventory.ContainerRepair";
  static final ObfSafeName anvilContainerMethod = new ObfSafeName("updateRepairOutput", "func_82848_d");

  static final String anvilGuiClass = "net.minecraft.client.gui.GuiRepair";
  static final ObfSafeName anvilGuiMethod = new ObfSafeName("drawGuiContainerForegroundLayer", "func_146979_b");

  static final String containerFurnaceClass = "net.minecraft.inventory.ContainerFurnace";
  static final ObfSafeName containerFurnaceMethod = new ObfSafeName("transferStackInSlot", "func_82846_b");
  static final String containerFurnaceMethodSig = "(Lnet/minecraft/inventory/ContainerFurnace;Lnet/minecraft/entity/player/EntityPlayer;I)Lnet/minecraft/item/ItemStack;";

  static final String renderItemClass = "net.minecraft.client.renderer.RenderItem";
  static final ObfSafeName renderItemOverlayIntoGUIMethod = new ObfSafeName("renderItemOverlayIntoGUI", "func_180453_a");
  static final ObfSafeName renderItemAndEffectIntoGUI = new ObfSafeName("renderItemAndEffectIntoGUI", "func_184391_a");
  static final ObfSafeName renderItemDisplayName = new ObfSafeName("renderItemAndEffectIntoGUI, renderItemOverlayIntoGUI", "func_180453_a, func_184391_a");

  static final String entityPlayerClass = "net.minecraft.entity.player.EntityPlayer";

  static final String entityAICreeperSwellClass = "net.minecraft.entity.ai.EntityAICreeperSwell";
  static final ObfSafeName updateTaskMethod = new ObfSafeName("updateTask", "func_75246_d");

  static final String extendedBlockStorageClass = "net.minecraft.world.chunk.storage.ExtendedBlockStorage";
  static final ObfSafeName isEmptyMethod = new ObfSafeName("isEmpty", "func_76663_a");

  @Override
  public byte[] transform(String name, String transformedName, byte[] basicClass) {

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
            logger.info("Transforming failed.");
          }
        }
      });
    }

    // Anvil max level
    if (transformedName.equals(anvilContainerClass) || transformedName.equals(anvilGuiClass)) {
      // 1.10 tested and works
      return transform(basicClass, transformedName, transformedName.equals(anvilContainerClass) ? anvilContainerMethod : anvilGuiMethod, new Transform() {
        @Override
        void transform(Iterator<MethodNode> methods) {
          int done = 0;
          while (methods.hasNext()) {
            MethodNode m = methods.next();
            if (anvilContainerMethod.equals(m.name) || anvilGuiMethod.equals(m.name)) {
              for (int i = 0; i < m.instructions.size(); i++) {
                AbstractInsnNode next = m.instructions.get(i);

                next = m.instructions.get(i);
                if (next instanceof IntInsnNode && ((IntInsnNode) next).operand == 40) {
                  m.instructions.set(next, new VarInsnNode(ALOAD, 0));
                  next = m.instructions.get(i);
                  m.instructions.insert(next, new MethodInsnNode(INVOKESTATIC, "com/enderio/core/common/transform/EnderCoreMethods", "getMaxAnvilCost",
                      "(Ljava/lang/Object;)I", false));
                  done++;
                }
              }
            }
          }
          if (done > 2 || done < 1) {
            logger.info("Transforming failed. (" + done + ")");
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
            logger.info("Transforming failed.");
          }
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

              boolean primed = false, applied = false;
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
                  m.instructions.insert(next, toAdd);
                  done++;
                  applied = true;
                  break;
                }
              }
              if (!applied) {
                logger.info("Transforming failed. Applying ersatz patch...");
                m.instructions.insert(toAdd);
                logger.warn("Ersatz patch applied, things may break!");
                done++;
              }
              break;
            }
          }
          if (done != 2) {
            logger.info("Transforming failed.");
          }
        }
      });
    }

    // Elytra flying
    if (transformedName.equals(entityPlayerClass)) {
      ClassNode classNode = new ClassNode();
      ClassReader classReader = new ClassReader(basicClass);
      classReader.accept(classNode, 0);

      boolean deObf = false;
      for (MethodNode method : classNode.methods) {
        if ("onUpdate".equals(method.name)) {
          deObf = true;
        }
      }

      MethodNode n = new MethodNode(Opcodes.ASM4, Opcodes.ACC_PUBLIC, deObf ? "isElytraFlying" : "func_184613_cA", "()Z", null, null);

      n.instructions = new InsnList();
      n.instructions.add(new VarInsnNode(ALOAD, 0));
      n.instructions
          .add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/entity/EntityLivingBase", deObf ? "isElytraFlying" : "func_184613_cA", "()Z", false));
      LabelNode l1 = new LabelNode(new Label());
      n.instructions.add(new JumpInsnNode(Opcodes.IFNE, l1));
      n.instructions.add(new VarInsnNode(ALOAD, 0));
      n.instructions.add(new MethodInsnNode(INVOKESTATIC, "com/enderio/core/common/transform/EnderCoreMethods", "isElytraFlying",
          "(Lnet/minecraft/entity/EntityLivingBase;)Z", false));
      n.instructions.add(new JumpInsnNode(Opcodes.IFNE, l1));
      n.instructions.add(new InsnNode(Opcodes.ICONST_0));
      n.instructions.add(new InsnNode(Opcodes.IRETURN));
      n.instructions.add(l1);
      n.instructions.add(new InsnNode(Opcodes.ICONST_1));
      n.instructions.add(new InsnNode(Opcodes.IRETURN));

      classNode.methods.add(n);

      ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
      classNode.accept(cw);
      logger
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

              m.instructions.insert(list);

              done = true;
            }
          }
          if (!done) {
            logger.info("Transforming failed.");
          }
        }
      });
    }

    return basicClass;
  }

  protected final static byte[] transform(byte[] classBytes, String className, ObfSafeName methodName, Transform transformer) {
    logger.info("Transforming Class [" + className + "], Method [" + methodName.getName() + "]");

    ClassNode classNode = new ClassNode();
    ClassReader classReader = new ClassReader(classBytes);
    classReader.accept(classNode, 0);

    Iterator<MethodNode> methods = classNode.methods.iterator();

    transformer.transform(methods);

    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS /* | ClassWriter.COMPUTE_FRAMES */);
    classNode.accept(cw);
    logger.info("Transforming " + className + " Finished.");
    return cw.toByteArray();
  }

}
