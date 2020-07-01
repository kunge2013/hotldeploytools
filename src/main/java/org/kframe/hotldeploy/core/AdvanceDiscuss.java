package org.kframe.hotldeploy.core;

import org.kframe.hotldeploy.util.PathUtil;

import javax.tools.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author fusu
 * @version 2020-05-06
 */
public class AdvanceDiscuss {

    /**
     * Java类的对应代码
     */
    public static String getJavaCode() {
        return "import com.alibaba.fusu.facade.Animal;\n" +
                "\n" +
                "public class Cat implements Animal {\n" +
                "    @Override\n" +
                "    public String hello(String name) {\n" +
                "        String message = \"Hello,\" + name + \"! 我是Cat。\";\n" +
                "        System.out.println(message);\n" +
                "        return message;\n" +
                "    }\n" +
                "}";
    }


    public static List<String> whiteList() {

        return new ArrayList<String>() {{
            add("java.lang");
            add("java.util");
            add("java.text");
            add("Ljava.lang");
            add("[D");
            add("[F");
            add("[I");
            add("[J");
            add("[C");
            add("[B");
            add("[Z");

            //保证测试进行
            add("java.io.PrintStream");
            add("Cat");
            add("com.alibaba.fusu.facade.Animal");
        }};
    }

    public static List<String> blackList() {

        return new ArrayList<String>() {{
            add("java.lang.Thread");
        }};
    }


    public static void safetyCheck(byte[] catBytes) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(catBytes);
        Set<String> dependencies = JavassistUtil.getDependencies(inputStream);
        inputStream.close();

        //判断是否在白名单中
        List<String> notSupportedPackages = new ArrayList<>();
        for (String d : dependencies) {
            boolean supported = false;
            for (String supportPackage : whiteList()) {
                if (d.startsWith(supportPackage)) {
                    supported = true;
                    break;
                }
            }
            if (!supported) {
                notSupportedPackages.add(d);
            }
        }

        //做黑名单限制
        for (String d : dependencies) {
            boolean unsupported = false;
            for (String supportPackage : blackList()) {
                if (d.startsWith(supportPackage)) {
                    unsupported = true;
                    break;
                }
            }

            if (unsupported) {
                notSupportedPackages.add(d);
            }
        }

        if (!notSupportedPackages.isEmpty()) {
            String message = String.join(",", notSupportedPackages);
            throw new RuntimeException("不支持以下类的使用：" + message);
        }
    }


    public static void compileAndRun() throws Exception {

        //类名
        final String className = "Cat";
        //项目所在路径
        String projectPath = PathUtil.getAppHomePath();
        String facadeJarPath = String.format(".:%s/facade/target/facade-1.0.jar", projectPath);

        //接口的类加载器
        ClassLoader animalClassLoader = Animal.class.getClassLoader();
        //设置当前的线程类加载器
        Thread.currentThread().setContextClassLoader(animalClassLoader);


        //需要进行编译的代码
        Iterable<? extends JavaFileObject> compilationUnits = new ArrayList<JavaFileObject>() {{
            add(new JavaSourceFromString(className, getJavaCode()));
        }};

        //编译的选项，对应于命令行参数
        List<String> options = new ArrayList<>();
        options.add("-classpath");
        options.add(facadeJarPath);

        //使用系统的编译器
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();

        StandardJavaFileManager standardJavaFileManager = javaCompiler.getStandardFileManager(null, null, null);
        ScriptFileManager scriptFileManager = new ScriptFileManager(standardJavaFileManager);

        //使用stringWriter来收集错误。
        StringWriter errorStringWriter = new StringWriter();

        //开始进行编译
        boolean ok = javaCompiler.getTask(errorStringWriter, scriptFileManager, diagnostic -> {
            if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {

                errorStringWriter.append(diagnostic.toString());
            }
        }, options, null, compilationUnits).call();

        if (!ok) {
            String errorMessage = errorStringWriter.toString();
            //编译出错，直接抛错。
            throw new RuntimeException("Compile Error:{}" + errorMessage);
        }

        //获取到编译后的二进制数据。
        final Map<String, byte[]> allBuffers = scriptFileManager.getAllBuffers();
        final byte[] catBytes = allBuffers.get(className);

        //做安全检查，白名单黑名单限制
        safetyCheck(catBytes);

        //使用自定义的ClassLoader加载类
        FsClassLoader fsClassLoader = new FsClassLoader(animalClassLoader, className, catBytes);
        Class<?> catClass = fsClassLoader.findClass(className);
        Object obj = catClass.newInstance();
        if (obj instanceof Animal) {
            Animal animal = (Animal) obj;
            animal.hello("Jerry");
        }

        //会得到结果:  Hello,Jerry! 我是Cat。

    }


    public static void gcTest() throws Exception {

        for (int i = 0; i < 1000000; i++) {
            //编译加载并且执行
            compileAndRun();

            //10000个回收一下
            if (i % 10000 == 0) {
                System.gc();
            }
        }

        //强制进行回收
        System.gc();
        System.out.println("休息10s");
        Thread.currentThread().sleep(10 * 1000);
    }


    public static void main(String[] args) throws Exception {

        //测试垃圾回收状况
        //gcTest();


        compileAndRun();

    }
}
