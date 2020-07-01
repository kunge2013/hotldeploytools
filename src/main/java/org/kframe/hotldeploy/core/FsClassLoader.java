package org.kframe.hotldeploy.core;

/**
 * 自定义的类加载器，可以动态的加载某个class文件中的类
 *
 * @author fusu
 * @version 2020-05-06
 */
public class FsClassLoader extends ClassLoader {

    private String fullyName;

    private byte[] data;

    public FsClassLoader() {

    }
    public FsClassLoader(String name, byte[] data) {
        this.fullyName = name;
        this.data = data;
    }
    public FsClassLoader(ClassLoader parentClassLoader, String name, byte[] data) {
        super(parentClassLoader);
        this.fullyName = name;
        this.data = data;
    }

    @Override
    protected Class<?> findClass(String name) {
        return defineClass(fullyName, data, 0, data.length);
    }

}
