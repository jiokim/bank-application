package com.bank.common.sensitive;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class SensitiveFieldProcessor {

    private final TextEncryptor textEncryptor;

    public SensitiveFieldProcessor(TextEncryptor textEncryptor) {
        this.textEncryptor = textEncryptor;
    }

    public <T> T processForStorage(T target) {
        traverse(target, Collections.newSetFromMap(new IdentityHashMap<>()), Mode.ENCRYPT);
        return target;
    }

    public <T> T processForRestore(T target) {
        traverse(target, Collections.newSetFromMap(new IdentityHashMap<>()), Mode.DECRYPT);
        return target;
    }

    private enum Mode { ENCRYPT, DECRYPT }

    private void traverse(Object target, Set<Object> visited, Mode mode) {
        if (target == null || !visited.add(target)) {
            return;
        }
        if (target instanceof Iterable<?> iterable) {
            iterable.forEach(item -> traverse(item, visited, mode));
            return;
        }
        if (target.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(target);
            for (int i = 0; i < length; i++) {
                traverse(java.lang.reflect.Array.get(target, i), visited, mode);
            }
            return;
        }
        if (isSimpleValue(target.getClass())) {
            return;
        }

        Class<?> type = target.getClass();
        while (type != null && type != Object.class) {
            for (Field field : type.getDeclaredFields()) {
                processField(target, field, visited, mode);
            }
            type = type.getSuperclass();
        }
    }

    private void processField(Object target, Field field, Set<Object> visited, Mode mode) {
        if (Modifier.isStatic(field.getModifiers())) {
            return;
        }

        Sensitive sensitive = field.getAnnotation(Sensitive.class);

        if (Modifier.isFinal(field.getModifiers())) {
            if (sensitive != null) {
                throw new IllegalArgumentException(
                        "@Sensitive cannot be applied to final field '" + field.getName()
                        + "' in " + field.getDeclaringClass().getSimpleName()
                        + " — use a mutable field or a separate storage DTO.");
            }
            return;
        }

        if (sensitive == null) {
            traverse(read(target, field), visited, mode);
            return;
        }

        if (sensitive.storagePolicy() != StoragePolicy.ENCRYPT) {
            throw new UnsupportedOperationException(
                    "StoragePolicy." + sensitive.storagePolicy() + " is not yet implemented.");
        }

        if (field.getType() != String.class) {
            throw new IllegalArgumentException(
                    "@Sensitive ENCRYPT supports only String fields: " + field.getName());
        }

        String value = (String) read(target, field);
        String result = (mode == Mode.ENCRYPT)
                ? textEncryptor.encrypt(value)
                : textEncryptor.decrypt(value);
        write(target, field, result);
    }

    private Object read(Object target, Field field) {
        try {
            field.setAccessible(true);
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to read sensitive field: " + field.getName(), e);
        }
    }

    private void write(Object target, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to write sensitive field: " + field.getName(), e);
        }
    }

    private boolean isSimpleValue(Class<?> type) {
        return type.isPrimitive()
                || type.isEnum()
                || type.getName().startsWith("java.")
                || type.getName().startsWith("jakarta.");
    }
}
