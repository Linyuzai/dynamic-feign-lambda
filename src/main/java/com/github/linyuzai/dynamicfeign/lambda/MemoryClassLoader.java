package com.github.linyuzai.dynamicfeign.lambda;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Load class from byte[] which is compiled in memory.
 *
 * @author michael
 */
class MemoryClassLoader extends URLClassLoader {

    // class name to class bytes:
    private Map<String, byte[]> classBytes = new HashMap<>();

    private Set<String> classNames;

    public MemoryClassLoader(Map<String, byte[]> classBytes) {
        super(new URL[0], MemoryClassLoader.class.getClassLoader());
        this.classBytes.putAll(classBytes);
        this.classNames = classBytes.keySet();
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            Class<?> find = Class.forName(name);
            if (find != null) {
                return find;
            }
        } catch (Exception ignore) {
        }
        byte[] buf = classBytes.get(name);
        if (buf == null) {
            throw new ClassNotFoundException(name);
        }
        classBytes.remove(name);
        return defineClass(name, buf, 0, buf.length);
    }

    public List<Class<?>> getClasses() {
        List<Class<?>> classes = new ArrayList<>();
        for (String className : classNames) {
            try {
                classes.add(findClass(className));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return classes;
    }
}
