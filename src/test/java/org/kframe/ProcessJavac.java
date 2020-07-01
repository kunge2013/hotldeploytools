package org.kframe;

import org.kframe.hotldeploy.core.Animal;
import org.kframe.hotldeploy.util.PathUtil;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * 使用Process类来调用命令行javac工具
 *
 * @author fusu
 * @version 2020-05-01
 */
public class ProcessJavac {

    /**
     * 打印进程输出
     *
     * @param process 进程
     */
    private static void readProcessOutput(final Process process) {
        // 将进程的正常输出在 System.out 中打印，进程的错误输出在 System.err 中打印
        read(process.getInputStream(), System.out);
        read(process.getErrorStream(), System.err);
    }

    // 读取输入流
    private static void read(InputStream inputStream, PrintStream out) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {

        //项目所在路径
        String projectPath = PathUtil.getAppHomePath();

        Process process = null;

        String cmd = String.format("javac -cp .:%s/facade/target/facade-1.0.jar -d %s/command-javac/src/main/resources %s/command-javac/src/main/resources/Cat.java", projectPath, projectPath, projectPath);

        System.out.println(cmd);

        process = Runtime.getRuntime().exec(cmd);

        // 打印程序输出
        readProcessOutput(process);

        int exitVal = process.waitFor();
        if (exitVal == 0) {
            System.out.println("javac执行成功！" + exitVal);
        } else {
            System.out.println("javac执行失败" + exitVal);
            return;
        }

        String classFilePath = String.format("%s/command-javac/src/main/resources/Cat.class", projectPath);
        String urlFilePath = String.format("file:%s", classFilePath);
        URL url = new URL(urlFilePath);
        URLClassLoader classLoader = new URLClassLoader(new URL[]{url});

        Class<?> catClass = classLoader.loadClass("Cat");
        Object obj = catClass.newInstance();
        if (obj instanceof Animal) {
            Animal animal = (Animal) obj;
            animal.hello("Kitty");
        }

        //会得到结果:  Hello,Kitty! 我是Cat。
    }

}
