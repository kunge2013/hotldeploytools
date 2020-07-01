package org.kframe.hotldeploy.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 通用方法
 *
 * @author fusu
 * @version 2020-05-01
 */
public class PathUtil {

    /**
     * 获取应用的home目录。
     * 例如：/Users/fusu/d/group/alibaba/fusu-share
     */
    public static String getAppHomePath() {
        return Paths.get("").toAbsolutePath().toString();
    }

    /**
     * 获取当前模块名，需要传入一个resources下面的文件作为指南针文件
     * 例如：传入 doc.md
     * 获取到：/Users/fusu/d/group/alibaba/fusu-share/fusu-share-common
     */
    public static String getModuleHomePath(String compassFile) {


        URL url = PathUtil.class.getClassLoader().getResource(compassFile);

        if (url == null) {
            throw new RuntimeException("No " + compassFile + ", please fix it!");
        }


        String inputFilePath = url.getPath();

        //windows环境和其他环境路径有差异
        String osName = System.getProperties().getProperty("os.name");
        if (osName.toLowerCase().startsWith("win")) {
            //win环境下去掉file:/
            inputFilePath = inputFilePath.substring(inputFilePath.indexOf("/") + 1);
        }

        //如果是jar包，那么要求Jar包旁边有个jar直接解压的文件夹。
        int index = inputFilePath.indexOf(".jar!");
        if (index == -1) {
            index = inputFilePath.indexOf("/target/classes");
            if (index == -1) {
                index = inputFilePath.indexOf("/target/test-classes");

            }
        }

        return inputFilePath.substring(0, index);


    }

    /**
     * 获取一个安全的文件夹路径
     * 如果中间某个文件夹不存在，那么就创建文件夹
     */
    public static String getSafeDirectoryPath(String directoryPath) {

        Path path = Paths.get(directoryPath);

        if (!Files.exists(path)) {

            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return directoryPath;

    }


    /**
     * 判断一个文件是否存在
     */
    public static boolean isExist(String filePath) {

        Path path = Paths.get(filePath);

        return Files.exists(path);

    }

    /**
     * 获取resources下面的某个文件内容]
     *
     * @param resourceRelativeName 资源相对路径，相对于resources文件夹，eg: protein/script/PrefixHandlerImpl.java
     * @return 返回资源文件的内容，如果没有找到或者出错，那么就返回 null
     */
    public static String getResourceContent(String resourceRelativeName) {


        ClassLoader classLoader = PathUtil.class.getClassLoader();
        URL resource = classLoader.getResource(resourceRelativeName);
        if (resource == null) {
            return null;
        }

        File file = new File(resource.getFile());

        //Read File Content
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (Throwable throwable) {
            return null;
        }
    }


}
