package com.enderio.core.common.transform;

import java.util.Iterator;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.enderio.core.common.config.ConfigHandler;
import com.google.common.collect.Sets;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

@MCVersion(value = "1.10")
public class EnderCoreTransformer implements IClassTransformer {

  // The EnderCore class cannot be referenced from the transformer,
  // as it improperly triggers classloading of FML event classes. 
  // The solution is to copy the logger into this class, instead of referencing
  // the static 'logger' field from EnderCore
  public static final Logger logger = LogManager.getLogger("EnderCore");

  // These classloader exclusions ensure that this transformer is not re-entrant 
  static {
    Launch.classLoader.addTransformerExclusion("com.enderio.core.common.config.");
    Launch.classLoader.addTransformerExclusion("com.enderio.core.api.common.config.");
    Launch.classLoader.addTransformerExclusion("com.enderio.core.common.Lang");
  }

  protected static class ObfSafeName {
    private String deobf, srg;

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

    // no hashcode because I'm naughty
  }

  protected static abstract class Transform {
    abstract void transform(Iterator<MethodNode> methods);
  }

//  private static final String worldTypeClass = "net.minecraft.world.WorldType";
//  private static final ObfSafeName voidFogMethod = new ObfSafeName("hasVoidParticles", "func_76564_j");
//  private static final String voidFogMethodSig = "(Lnet/minecraft/world/WorldType;Z)Z";
//
//  private static final String anvilContainerClass = "net.minecraft.inventory.ContainerRepair";
//  private static final ObfSafeName anvilContainerMethod = new ObfSafeName("updateRepairOutput", "func_82848_d");
//
//  private static final String anvilGuiClass = "net.minecraft.client.gui.GuiRepair";
//  private static final ObfSafeName anvilGuiMethod = new ObfSafeName("drawGuiContainerForegroundLayer", "func_146979_b");
//
//  private static final String enchantHelperClass = "net.minecraft.enchantment.EnchantmentHelper";
//  private static final String enchantHelperMethodSig = "(Lnet/minecraft/item/ItemStack;I)I";
//  private static final ObfSafeName buildEnchantListMethod = new ObfSafeName("buildEnchantmentList", "func_77513_b");
//  private static final ObfSafeName calcEnchantabilityMethod = new ObfSafeName("calcItemStackEnchantability", "func_77514_a");
//
//  private static final String itemStackClass = "net.minecraft.item.ItemStack";
//  private static final ObfSafeName itemStackMethod = new ObfSafeName("getRarity", "func_77953_t");
//  private static final String itemStackMethodSig = "(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/EnumRarity;";
//
//  private static final String entityArrowClass = "net.minecraft.entity.projectile.EntityArrow";
//  private static final ObfSafeName entityArrowMethod = new ObfSafeName("onUpdate", "func_70071_h_");
//  private static final String entityArrowMethodSig = "(Lnet/minecraft/entity/projectile/EntityArrow;)V";
//
  private static final String containerFurnaceClass = "net.minecraft.inventory.ContainerFurnace";
  private static final ObfSafeName containerFurnaceMethod = new ObfSafeName("transferStackInSlot", "func_82846_b");
  private static final String containerFurnaceMethodSig = "(Lnet/minecraft/inventory/ContainerFurnace;Lnet/minecraft/entity/player/EntityPlayer;I)Lnet/minecraft/item/ItemStack;";

  private static final String renderItemClass = "net.minecraft.client.renderer.RenderItem";
  private static final ObfSafeName renderItemOverlayIntoGUIMethod = new ObfSafeName("renderItemOverlayIntoGUI", "func_180453_a");
//  private static final String renderItemOverlayIntoGUIMethodSig = "(Lnet/minecraft/client/gui/FontRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V";

//  private static final Set<String> transformableClasses = Sets.newHashSet(worldTypeClass, anvilContainerClass, anvilGuiClass, enchantHelperClass,
//      itemStackClass, entityArrowClass, containerFurnaceClass, renderItemClass);

  private static final Set<String> transformableClasses = Sets.newHashSet(containerFurnaceClass, renderItemClass);

  @Override
  public byte[] transform(String name, String transformedName, byte[] basicClass) {
  
    //TODO: 1.10 All these need to be checked
    boolean doGameplayChanges = true;
    if (transformableClasses.contains(transformedName) && ConfigHandler.invisibleMode == 1) {
      doGameplayChanges = false;
    }
    
    // Void fog removal
//    if (doGameplayChanges && transformedName.equals(worldTypeClass)) {
//      basicClass = transform(basicClass, worldTypeClass, voidFogMethod, new Transform() { // TODO 1.9 fails
//        @Override
//        void transform(Iterator<MethodNode> methods) {
//          boolean done = false;
//          while (methods.hasNext()) {
//            MethodNode m = methods.next();
//            if (voidFogMethod.equals(m.name)) {
//              m.instructions.clear();
//
//              m.instructions.add(new VarInsnNode(ALOAD, 0));
//              m.instructions.add(new VarInsnNode(ILOAD, 1));
//              m.instructions.add(new MethodInsnNode(INVOKESTATIC, "com/enderio/core/common/transform/EnderCoreMethods", "hasVoidParticles", voidFogMethodSig,
//                  false));
//              m.instructions.add(new InsnNode(IRETURN));
//
//              done = true;
//              break;
//            }
//          }
//          if (!done) {
//            logger.info("Transforming failed.");
//          }
//        }
//      });
//    }
    // Anvil max level
//    else if (doGameplayChanges && (transformedName.equals(anvilContainerClass) || transformedName.equals(anvilGuiClass))) { // TODO 1.9 fails
//      basicClass = transform(basicClass, anvilContainerClass, anvilContainerMethod, new Transform() {
//        @Override
//        void transform(Iterator<MethodNode> methods) {
//          int done = 0;
//          while (methods.hasNext()) {
//            MethodNode m = methods.next();
//            if (anvilContainerMethod.equals(m.name) || anvilGuiMethod.equals(m.name)) {
//              for (int i = 0; i < m.instructions.size(); i++) {
//                AbstractInsnNode next = m.instructions.get(i);
//
//                next = m.instructions.get(i);
//                if (next instanceof IntInsnNode && ((IntInsnNode) next).operand == 40) {
//                  m.instructions.set(next, new MethodInsnNode(INVOKESTATIC, "com/enderio/core/common/transform/EnderCoreMethods", "getMaxAnvilCost", "()I",
//                      false));
//                  done++;
//                }
//              }
//            }
//          }
//          if (done != 2) {
//            logger.info("Transforming failed.");
//          }
//        }
//      });
//    }
    // Item Enchantability Event
//    else if (transformedName.equals(enchantHelperClass)) { // TODO 1.9 applies. test it!
//      final Map<String, int[]> data = new HashMap<String, int[]>();
//      data.put(buildEnchantListMethod.getName(), new int[] { 1, 4 });
//      data.put(calcEnchantabilityMethod.getName(), new int[] { 3, 5 });
//      Transform transformer = new Transform() {
//        @Override
//        void transform(Iterator<MethodNode> methods) {
//          boolean done = false;
//          while (methods.hasNext()) {
//            MethodNode m = methods.next();
//            if (data.keySet().contains(m.name)) {
//              int[] indeces = data.get(m.name);
//              for (int i = 0; i < m.instructions.size(); i++) {
//                AbstractInsnNode next = m.instructions.get(i);
//                if (next instanceof VarInsnNode) {
//                  VarInsnNode varNode = (VarInsnNode) next;
//                  if (varNode.getOpcode() == ISTORE && varNode.var == indeces[1]) {
//                    InsnList toAdd = new InsnList();
//                    toAdd.add(new VarInsnNode(ALOAD, indeces[0]));
//                    toAdd.add(new VarInsnNode(ILOAD, indeces[1]));
//                    toAdd.add(new MethodInsnNode(INVOKESTATIC, "com/enderio/core/common/transform/EnderCoreMethods", "getItemEnchantability",
//                        enchantHelperMethodSig, false));
//                    toAdd.add(new VarInsnNode(ISTORE, indeces[1]));
//                    m.instructions.insert(next, toAdd);
//                    done = true;
//                    break;
//                  }
//                }
//              }
//
//              break;
//            }
//          }
//          if (!done) {
//            logger.info("Transforming failed.");
//          }
//        }
//      };
//
//      basicClass = transform(basicClass, enchantHelperClass, buildEnchantListMethod, transformer);
//    }
    // ItemRarity Event
//    else if (transformedName.equals(itemStackClass)) {
//      basicClass = transform(basicClass, itemStackClass, itemStackMethod, new Transform() { // TODO 1.9 applies. test it!
//        @Override
//        void transform(Iterator<MethodNode> methods) {
//          boolean done = false;
//          while (methods.hasNext()) {
//            MethodNode m = methods.next();
//            if (itemStackMethod.equals(m.name)) {
//              m.instructions.clear();
//
//              m.instructions.add(new VarInsnNode(ALOAD, 0));
//              m.instructions.add(new MethodInsnNode(INVOKESTATIC, "com/enderio/core/common/transform/EnderCoreMethods", "getItemRarity", itemStackMethodSig,
//                  false));
//              m.instructions.add(new InsnNode(ARETURN));
//
//              done = true;
//              break;
//            }
//          }
//          if (!done) {
//            logger.info("Transforming failed.");
//          }
//        }
//      });
//    }
    // ArrowUpdate Event
//    else if (transformedName.equals(entityArrowClass)) {
//      basicClass = transform(basicClass, entityArrowClass, entityArrowMethod, new Transform() { // TODO 1.9 doesn't apply
//        @Override
//        void transform(Iterator<MethodNode> methods) {
//          boolean done = false;
//          while (methods.hasNext()) {
//            MethodNode m = methods.next();
//            if (entityArrowMethod.equals(m.name)) {
//              for (int i = 0; i < m.instructions.size(); i++) {
//                AbstractInsnNode next = m.instructions.get(i);
//                if (next instanceof MethodInsnNode) {
//                  InsnList toAdd = new InsnList();
//                  toAdd.add(new VarInsnNode(ALOAD, 0));
//                  toAdd
//                      .add(new MethodInsnNode(INVOKESTATIC, "com/enderio/core/common/transform/EnderCoreMethods", "onArrowUpdate", entityArrowMethodSig, false));
//                  m.instructions.insert(next, toAdd);
//                  done = true;
//                  break;
//                }
//              }
//              break;
//            }
//          }
//          if (!done) {
//            logger.info("Transforming failed.");
//          }
//        }
//      });
//    }
    // Furnace Shift Click Fix
    // else
    if (doGameplayChanges && transformedName.equals(containerFurnaceClass)) {
      basicClass = transform(basicClass, containerFurnaceClass, containerFurnaceMethod, new Transform() {
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
//    else 
      if (transformedName.equals(renderItemClass)) {
      basicClass = transform(basicClass, renderItemClass, renderItemOverlayIntoGUIMethod, new Transform() { // 1.9.4 works
        @Override
        void transform(Iterator<MethodNode> methods) {
          boolean done = false;
          while (methods.hasNext()) {
            MethodNode m = methods.next();
            if (renderItemOverlayIntoGUIMethod.equals(m.name)) {
              boolean primed = false;
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
                  InsnList toAdd = new InsnList();
                  toAdd.add(new VarInsnNode(ALOAD, 2));
                  toAdd.add(new VarInsnNode(ILOAD, 3));
                  toAdd.add(new VarInsnNode(ILOAD, 4));
                  toAdd.add(new MethodInsnNode(INVOKESTATIC, "com/enderio/core/common/transform/EnderCoreMethods", "renderItemOverlayIntoGUI",
                      "(Lnet/minecraft/item/ItemStack;II)V", false));
                  m.instructions.insert(next, toAdd);
                  done = true;
                  break;
                }
              }
              break;
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

  protected final byte[] transform(byte[] classBytes, String className, ObfSafeName methodName, Transform transformer) {
    logger.info("Transforming Class [" + className + "], Method [" + methodName.getName() + "]");

    ClassNode classNode = new ClassNode();
    ClassReader classReader = new ClassReader(classBytes);
    classReader.accept(classNode, 0);

    Iterator<MethodNode> methods = classNode.methods.iterator();

    transformer.transform(methods);

    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    classNode.accept(cw);
    logger.info("Transforming " + className + " Finished.");
    return cw.toByteArray();
  }
}
