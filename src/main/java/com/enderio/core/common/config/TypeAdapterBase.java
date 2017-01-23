package com.enderio.core.common.config;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.ArrayUtils;

import com.enderio.core.common.config.ConfigProcessor.ITypeAdapter;
import com.enderio.core.common.util.NullHelper;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Property.Type;

@SuppressWarnings({ "serial", "unchecked" })
public abstract class TypeAdapterBase<ACTUAL, BASE> implements ITypeAdapter<ACTUAL, BASE> {
  private final TypeToken<ACTUAL> actualType;
  private final Property.Type type;
  private final Class<?> primitiveType;

  public TypeAdapterBase(TypeToken<ACTUAL> actualType, Property.Type type, Class<?> primitiveType) {
    this.actualType = actualType;
    this.type = type;
    this.primitiveType = primitiveType;
  }

  public TypeAdapterBase(TypeToken<ACTUAL> actualType, Property.Type baseType) {
    this(actualType, baseType, null);
  }

  @Override
  public TypeToken<ACTUAL> getActualType() {
    return actualType;
  }

  @Override
  public Property.Type getType() {
    return type;
  }

  @Override
  public Class<?> getPrimitiveType() {
    return primitiveType;
  }

  public static final class TypeAdapterSame<TYPE> extends TypeAdapterBase<TYPE, TYPE> {
    public TypeAdapterSame(TypeToken<TYPE> actual, Property.Type base) {
      super(actual, base);
    }

    public TypeAdapterSame(TypeToken<TYPE> actual, Property.Type base, Class<?> primitiveType) {
      super(actual, base, primitiveType);
    }

    @Override
    public TYPE createActualType(TYPE base) {
      return base;
    }

    @Override
    public @Nonnull TYPE createBaseType(@Nonnull TYPE actual) {
      return actual;
    }
  }

  // @formatter:off
    public static final TypeAdapterSame<Integer> INTEGER = new TypeAdapterSame<Integer>(TypeToken.of(Integer.class), Type.INTEGER, int.class);
    public static final TypeAdapterSame<int[]> INTEGER_ARR = new TypeAdapterSame<int[]>(TypeToken.of(int[].class), Type.INTEGER);
    public static final TypeAdapterSame<Double> DOUBLE = new TypeAdapterSame<Double>(TypeToken.of(Double.class), Type.DOUBLE, double.class);
    public static final TypeAdapterSame<double[]> DOUBLE_ARR = new TypeAdapterSame<double[]>(TypeToken.of(double[].class), Type.DOUBLE);
    public static final TypeAdapterSame<Boolean> BOOLEAN = new TypeAdapterSame<Boolean>(TypeToken.of(Boolean.class), Type.BOOLEAN, boolean.class);
    public static final TypeAdapterSame<boolean[]> BOOLEAN_ARR = new TypeAdapterSame<boolean[]>(TypeToken.of(boolean[].class), Type.BOOLEAN);
    public static final TypeAdapterSame<String> STRING = new TypeAdapterSame<String>(TypeToken.of(String.class), Type.STRING);
    public static final TypeAdapterSame<String[]> STRING_ARR = new TypeAdapterSame<String[]>(TypeToken.of(String[].class), Type.STRING);

    static DecimalFormat Floatfmt = new DecimalFormat();
    static
    {
        Floatfmt.setMaximumFractionDigits(5);
    }

    public static final TypeAdapterBase<Float, Double> FLOAT =
            new TypeAdapterBase<Float, Double>(TypeToken.of(Float.class), Type.DOUBLE, float.class)
            {
                @Override
                public Float createActualType(Double data)
                {
                    return data.floatValue();
                }

                @Override
                public @Nonnull Double createBaseType(@Nonnull Float actual)
                {
                    return Double.parseDouble(Floatfmt.format(actual));
                }
            };

    public static final TypeAdapterBase<float[], double[]> FLOAT_ARR =
            new TypeAdapterBase<float[], double[]>(TypeToken.of(float[].class), Type.DOUBLE)
            {

                @Override
                public float[] createActualType(double[] base)
                {
                    float[] ret = new float[base.length];
                    for (int i = 0; i < ret.length; i++)
                    {
                        ret[i] = (float) base[i];
                    }
                    return ret;
                }

                @Override
                public @Nonnull double[] createBaseType(@Nonnull float[] actual)
                {
                    double[] ret = new double[actual.length];
                    for (int i = 0; i < ret.length; i++)
                    {
                        ret[i] = Double.parseDouble(Floatfmt.format(actual[i]));
                    }
                    return ret;
                }
            };

    public static final TypeAdapterBase<List<Integer>, int[]> INTEGER_LIST =
            new TypeAdapterBase<List<Integer>, int[]>(new TypeToken<List<Integer>>(){}, Type.INTEGER)
            {
                @Override
                public List<Integer> createActualType(int[] data)
                {
                    return Lists.newArrayList(NullHelper.notnull(ArrayUtils.toObject(data),"LOC07929044"));
                }

                @Override
                public @Nonnull int[] createBaseType(@Nonnull List<Integer> actual)
                {
                    return NullHelper.notnullJ(ArrayUtils.toPrimitive(actual.toArray(new Integer[actual.size()])), "ArrayUtils.toPrimitive()");
                }
            };

    public static final TypeAdapterBase<List<Double>, double[]> DOUBLE_LIST =
            new TypeAdapterBase<List<Double>, double[]>(new TypeToken<List<Double>>(){}, Type.DOUBLE)
            {
                @Override
                public List<Double> createActualType(double[] data)
                {
                    return Lists.newArrayList(NullHelper.notnull(ArrayUtils.toObject(data),"LOC07933087"));
                }

                @Override
                public @Nonnull double[] createBaseType(@Nonnull List<Double> actual)
                {
                    return NullHelper.notnullJ(ArrayUtils.toPrimitive(actual.toArray(new Double[actual.size()])), "ArrayUtils.toPrimitive()");
                }
            };

    public static final TypeAdapterBase<List<Float>, double[]> FLOAT_LIST =
            new TypeAdapterBase<List<Float>, double[]>(new TypeToken<List<Float>>(){}, Type.DOUBLE)
            {
                @Override
                public List<Float> createActualType(double[] data)
                {
                    return Lists.newArrayList(NullHelper.notnull(ArrayUtils.toObject(FLOAT_ARR.createActualType(data)),"LOC07936094"));
                }

                @Override
                public @Nonnull double[] createBaseType(@Nonnull List<Float> actual)
                {
                    final @Nonnull float[] temp = NullHelper.notnullJ(ArrayUtils.toPrimitive(actual.toArray(new Float[actual.size()])), "ArrayUtils.toPrimitive()");
                    return FLOAT_ARR.createBaseType(temp);
                }
            };

    public static final TypeAdapterBase<List<Boolean>, boolean[]> BOOLEAN_LIST =
            new TypeAdapterBase<List<Boolean>, boolean[]>(new TypeToken<List<Boolean>>(){}, Type.BOOLEAN)
            {
                @Override
                public List<Boolean> createActualType(boolean[] data)
                {
                    return Lists.newArrayList(NullHelper.notnull(ArrayUtils.toObject(data),"LOC07943002"));
                }

                @Override
                public @Nonnull boolean[] createBaseType(@Nonnull List<Boolean> actual)
                {
                    return NullHelper.notnullJ(ArrayUtils.toPrimitive(actual.toArray(new Boolean[actual.size()])), "ArrayUtils.toPrimitive()");
                }
            };

    public static final TypeAdapterBase<List<String>, String[]> STRING_LIST =
            new TypeAdapterBase<List<String>, String[]>(new TypeToken<List<String>>(){}, Type.STRING)
            {
                @Override
                public List<String> createActualType(String[] data)
                {
                    return Lists.newArrayList(NullHelper.notnull(data,"LOC07949017"));
                }

                @Override
                public @Nonnull String[] createBaseType(@Nonnull List<String> actual)
                {
                    return NullHelper.notnullJ(actual.toArray(new String[actual.size()]), "List.toArray()");
                }
            };

    public static final List<TypeAdapterBase<?, ? extends Serializable>> all = Lists.newArrayList(
            INTEGER,
            INTEGER_ARR,
            DOUBLE,
            DOUBLE_ARR,
            BOOLEAN,
            BOOLEAN_ARR,
            STRING,
            STRING_ARR,
            FLOAT,
            FLOAT_ARR,
            INTEGER_LIST,
            DOUBLE_LIST,
            FLOAT_LIST,
            BOOLEAN_LIST,
            STRING_LIST
    );
}
