package org.kframe;

import org.kframe.hotldeploy.core.Animal;
import org.kframe.hotldeploy.core.FsClassLoader;
import org.kframe.hotldeploy.core.JavaSourceFromString;
import org.kframe.hotldeploy.core.ScriptFileManager;
import org.kframe.hotldeploy.util.PathUtil;

import javax.tools.*;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CodeJavac {

    /**
     * Java类的对应代码
     */
    public static String getJavaCode() {
        return "import com.alibaba.fusu.facade.Animal;\n" +
                "\n" +
                "public class Cat implements Animal {\n" +
                "    @Override\n" +
                "    public String hello(String name) {\n" +
                "        String message = \"Hello,\" + name + \"! 我是Cat212121。\";\n" +
                "        System.out.println(message);\n" +
                "        return message;\n" +
                "    }\n" +
                "}";

    }


    public static void main(String[] args) throws Exception {
       // Thread.currentThread().getContextClassLoader().loadClass(Animal.class.getName());
        //类名
        String className = "Cat";
        //项目所在路径
        String projectPath = PathUtil.getAppHomePath();
        String facadeJarPath = String.format("D:\\github.io\\dynamic-script\\dynamic-script\\facade\\target\\facade-1.0-SNAPSHOT.jar", projectPath);
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
        //使用自定义的ClassLoader加载类
        FsClassLoader fsClassLoader = new FsClassLoader(className, catBytes);
        Class<?> catClass = fsClassLoader.loadClass(className);
        Object obj = catClass.newInstance();
        if (obj instanceof Animal) {
            Animal animal = (Animal) obj;
            animal.hello("Moss");
        }

        //会得到结果:  Hello,Moss! 我是Cat。
    }
}
