package com.monitor.bit.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DataConvertUtils {

    /**
     * String转int
     *
     * @param str 字符串
     * @return int
     */
    public static int strToInt(String str) {
        int i = 0;
        try {
            i = Integer.parseInt(str);
        } catch (Exception e) {
            // e.printStackTrace();
        }
        return i;
    }

    /**
     * String转Integer或null
     *
     * @param str 字符串
     * @return int
     */
    public static Integer strToIntegerOrNull(String str) {
        Integer i = null;
        try {
            i = Integer.valueOf(str);
        } catch (Exception e) {
            // e.printStackTrace();
        }
        return i;
    }

    /**
     * String转Long
     *
     * @param str 字符串
     * @return int
     */
    public static Long strToLong(String str) {
        Long l = null;
        try {
            l = Long.parseLong(str);
        } catch (Exception e) {
            // e.printStackTrace();
        }
        return l;
    }

    /**
     * String转String，返回字符串或空字符串
     *
     * @param str 字符串
     * @return String
     */
    public static String getStrOrEmpty(String str) {
        if (str == null) {
            return "";
        } else {
            return str;
        }
    }

    /**
     * JSON字符串转对象
     *
     * @param jsonStr jsonStr
     * @param tClass  tClass
     * @param <T>     T
     * @return T
     * @throws JsonProcessingException
     */
    public static <T> T jsonStrToObject(String jsonStr, Class<T> tClass) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonStr, tClass);
    }

    /**
     * 对象转JSON字符串
     *
     * @param source source
     * @return String
     */
    public static String objectToJsonStr(Object source) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(source);
    }

    /**
     * bean转map
     *
     * @param bean
     * @return
     */
    public static Map beanToMap(Object bean) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        Class type = bean.getClass();
        Map returnMap = new HashMap();
        BeanInfo beanInfo = Introspector.getBeanInfo(type);

        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < propertyDescriptors.length; i++) {
            PropertyDescriptor descriptor = propertyDescriptors[i];
            String propertyName = descriptor.getName();
            if (!propertyName.equals("class")) {
                Method readMethod = descriptor.getReadMethod();
                Object result = readMethod.invoke(bean, new Object[0]);
                if (result != null) {
                    returnMap.put(propertyName, result);
                } else {
                    returnMap.put(propertyName, "");
                }
            }
        }
        return returnMap;
    }

    public static <T> T mapToBean(Map<String, Object> map, Class<T> tClass) {
        try {
            T bean = tClass.getConstructor().newInstance();
            for (Object k : map.keySet()) {
                String key = (String) k;
                Object value = map.get(key);
                if (value != null) {
                    try {
                        Field field = tClass.getDeclaredField(key);
                        field.setAccessible(true);
                        field.set(bean, value);
                        field.setAccessible(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return bean;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
