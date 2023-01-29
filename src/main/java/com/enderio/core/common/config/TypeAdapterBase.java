package com.enderio.core.common.config;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.List;

import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Property.Type;

import org.apache.commons.lang3.ArrayUtils;

import com.enderio.core.common.config.ConfigProcessor.ITypeAdapter;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;

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
        public TYPE createBaseType(TYPE actual) {
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

    private static DecimalFormat Floatfmt = new DecimalFormat();
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
                
                public Double createBaseType(Float actual) 
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
                public double[] createBaseType(float[] actual)
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
                    return Lists.newArrayList(ArrayUtils.toObject(data));
                }
                
                @Override
                public int[] createBaseType(List<Integer> actual)
                {
                    return ArrayUtils.toPrimitive(actual.toArray(new Integer[actual.size()]));
                }
            };
            
    public static final TypeAdapterBase<List<Double>, double[]> DOUBLE_LIST = 
            new TypeAdapterBase<List<Double>, double[]>(new TypeToken<List<Double>>(){}, Type.DOUBLE)
            {
                @Override
                public List<Double> createActualType(double[] data)
                {
                    return Lists.newArrayList(ArrayUtils.toObject(data));
                }
                
                @Override
                public double[] createBaseType(List<Double> actual)
                {
                    return ArrayUtils.toPrimitive(actual.toArray(new Double[actual.size()]));
                }
            };
          
    public static final TypeAdapterBase<List<Float>, double[]> FLOAT_LIST = 
            new TypeAdapterBase<List<Float>, double[]>(new TypeToken<List<Float>>(){}, Type.DOUBLE)
            {
                @Override
                public List<Float> createActualType(double[] data)
                {
                    return Lists.newArrayList(ArrayUtils.toObject(FLOAT_ARR.createActualType(data)));
                }
                        
                @Override
                public double[] createBaseType(List<Float> actual)
                {
                    float[] temp = ArrayUtils.toPrimitive(actual.toArray(new Float[actual.size()]));
                    return FLOAT_ARR.createBaseType(temp);
                }
            };
            
    public static final TypeAdapterBase<List<Boolean>, boolean[]> BOOLEAN_LIST = 
            new TypeAdapterBase<List<Boolean>, boolean[]>(new TypeToken<List<Boolean>>(){}, Type.BOOLEAN)
            {
                @Override
                public List<Boolean> createActualType(boolean[] data)
                {
                    return Lists.newArrayList(ArrayUtils.toObject(data));
                }
                
                @Override
                public boolean[] createBaseType(List<Boolean> actual)
                {
                    return ArrayUtils.toPrimitive(actual.toArray(new Boolean[actual.size()]));
                }
            };
 
    public static final TypeAdapterBase<List<String>, String[]> STRING_LIST = 
            new TypeAdapterBase<List<String>, String[]>(new TypeToken<List<String>>(){}, Type.STRING)
            {
                @Override
                public List<String> createActualType(String[] data)
                {
                    return Lists.newArrayList(data);
                }
                        
                @Override
                public String[] createBaseType(List<String> actual)
                {
                    return actual.toArray(new String[actual.size()]);
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
