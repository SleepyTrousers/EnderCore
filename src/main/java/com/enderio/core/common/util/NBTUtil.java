package com.enderio.core.common.util;

import java.lang.reflect.Method;

import lombok.SneakyThrows;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;

import org.apache.commons.lang3.ClassUtils;

import cpw.mods.fml.relauncher.ReflectionHelper;

public class NBTUtil {

  public enum NBTType {

    BYTE(byte.class, NBTTagByte.class, "func_74774_a", "setByte"),
    SHORT(short.class, NBTTagShort.class, "func_74777_a", "setShort"),
    INT(int.class, NBTTagInt.class, "func_74768_a", "setInteger"),
    LONG(long.class, NBTTagLong.class, "func_74772_a", "setLong"),
    FLOAT(float.class, NBTTagFloat.class, "func_74776_a", "setFloat"),
    DOUBLE(double.class, NBTTagDouble.class, "func_74780_a", "setDouble"),
    BYTE_ARR(byte[].class, NBTTagByteArray.class, "func_74773_a", "setByteArray"),
    INT_ARR(int[].class, NBTTagIntArray.class, "func_74783_a", "setIntArray"),
    STRING(String.class, NBTTagString.class, "func_74778_a", "setString"),
    COMPOUND(null, NBTTagCompound.class);

    public static final NBTType[] VALUES = values();

    public final Class<?> type, nbtType;
    public final Method method;

    private NBTType(Class<?> type, Class<? extends NBTBase> nbtType, String... methodNames) {
      this.type = ClassUtils.primitiveToWrapper(type);
      this.nbtType = nbtType;
      this.method = ReflectionHelper.findMethod(NBTBase.class, null, methodNames, String.class, type);
    }

    @SneakyThrows
    protected boolean write(String key, Object obj, NBTTagCompound tag) {
      Class<?> c = ClassUtils.primitiveToWrapper(obj.getClass());
      if (type.isAssignableFrom(c)) {
        method.invoke(tag, key, obj);
        return true;
      }
      return false;
    }

    public static void writeToNBT(String key, Object obj, NBTTagCompound tag) {
      for (NBTType t : VALUES) {
        if (t != COMPOUND) {
          if (t.write(key, obj, tag)) {
            return;
          }
        }
      }
      if (obj instanceof NBTBase) {
        tag.setTag(key, (NBTBase) obj);
      }
    }

    //    public static <T> T readFromNBT(String key, NBTTagCompound tag) {
    //      NBTBase data = tag.getTag(key);
    //      Class<?> dataType = data.getClass();
    //      for (NBTType t : VALUES) {
    //        if (dataType == t.nbtType) {
    //          return (T) data.
    //        }
    //      }
    //    }
  }

}
