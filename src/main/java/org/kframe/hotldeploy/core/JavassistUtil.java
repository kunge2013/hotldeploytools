package org.kframe.hotldeploy.core;

import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * 借助javassist的能力，获取到一个Class的依赖情况
 *
 * @author fusu
 * @version 2019-08-22
 */
public class JavassistUtil {

    /**
     * 通过javassist分析某一个class的依赖情况
     */
    public static Set<String> getDependencies(InputStream is) throws Exception {

        ClassFile cf = new ClassFile(new DataInputStream(is));
        ConstPool constPool = cf.getConstPool();
        HashSet<String> set = new HashSet<>();
        for (int ix = 1, size = constPool.getSize(); ix < size; ix++) {
            int descriptorIndex;
            if (constPool.getTag(ix) == ConstPool.CONST_Class) {
                set.add(constPool.getClassInfo(ix));
            } else if (constPool.getTag(ix) == ConstPool.CONST_NameAndType) {
                descriptorIndex = constPool.getNameAndTypeDescriptor(ix);
                String desc = constPool.getUtf8Info(descriptorIndex);
                for (int p = 0; p < desc.length(); p++) {
                    if (desc.charAt(p) == 'L') {
                        set.add(desc.substring(++p, p = desc.indexOf(';', p)).replace('/', '.'));
                    }
                }
            }
        }
        return set;
    }


}
