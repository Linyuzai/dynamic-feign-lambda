package com.github.linyuzai.dynamicfeign.lambda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class LambdaHolderGenerator {

    //private static String CLASSES_PATH = File.separator + "dynamic_feign" + File.separator;
    private static Logger logger = LoggerFactory.getLogger(LambdaHolderGenerator.class);

    private static JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    public static String absoluteProjectPath = null;

    private static List<Class<?>> compile(List<File> files) {
        List<Class<?>> classes = new ArrayList<>();
        for (File file : files) {
            StandardJavaFileManager stdManager = compiler.getStandardFileManager(null, null, null);
            try (MemoryJavaFileManager manager = new MemoryJavaFileManager(stdManager);
                 FileInputStream fis = new FileInputStream(file); InputStreamReader isr = new InputStreamReader(fis);
                 BufferedReader br = new BufferedReader(isr)) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    builder.append(line).append("\n");
                }
                JavaFileObject javaFileObject = manager.makeStringSource(file.getName(), builder.toString());
                JavaCompiler.CompilationTask task = compiler.getTask(null, manager, null, null, null, Collections.singletonList(javaFileObject));
                if (task.call()) {
                    Map<String, byte[]> results = manager.getClassBytes();
                    MemoryClassLoader mcl = new MemoryClassLoader(results);
                    classes.addAll(mcl.getClasses());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return classes;
    }

    public static List<Class<?>> filterClasses(List<Class<?>> classes) {
        List<Class<?>> filterClasses = new ArrayList<>();
        for (Class<?> cls : classes) {
            if (cls.isInterface() && cls.isAnnotationPresent(FeignClient.class)) {
                filterClasses.add(cls);
            }
        }
        return filterClasses;
    }

    public static void generate(String packageName) {
        try {
            String projectPath = absoluteProjectPath == null ? new File("").getCanonicalPath() : absoluteProjectPath;
            String path = projectPath +
                    File.separator + "src" + File.separator + "main" +
                    File.separator + "java" + File.separator + packageName.replaceAll("\\.", Matcher.quoteReplacement(File.separator));
            generateFromSource(path, path, packageName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateFromSource(String generatePath, String scanPath, String packageName) {
        if (fileExists(generatePath)) {
            return;
        }
        List<File> files = new ArrayList<>();
        File scan = new File(scanPath);
        filterFiles(scan, files);
        generateFromClasses(generatePath, packageName, filterClasses(compile(files)));
    }

    private static void filterFiles(File file, List<File> files) {
        if (file == null) {
            return;
        }
        if (file.isDirectory()) {
            File[] fileList = file.listFiles();
            if (fileList != null) {
                for (File f : fileList) {
                    filterFiles(f, files);
                }
            }
        } else {
            if (file.getName().endsWith(".java")) {
                files.add(file);
            }
        }
    }

    public static void generateFromClasses(String generatePath, String packageName, List<Class<?>> classes) {
        StringBuilder content = new StringBuilder("package " + packageName + ";\n" + "public class LambdaHolder {\n");
        for (Class<?> cls : classes) {
            for (Method method : cls.getMethods()) {
                content.append(generateClass(method));
                content.append(generateAddMethod(method, "addMethodUrl", ", String url"));
                content.append(generateRemoveMethod(method, "removeMethodUrl"));
            }
        }
        content.append("}");
        writeJava(generatePath, content.toString());
    }

    public static void writeJava(String generatePath, String content) {
        System.out.println(content);
        File file = getFile(generatePath);
        try (Writer writer = new FileWriter(file)) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String generateClass(Method method) {
        Class<?> returnClass = method.getReturnType();
        String returnClassName = "R";
        if (returnClass == void.class) {
            returnClassName = "void";
        }
        return "    @FunctionalInterface\n"
                + "    public interface " + generateLambdaHolderClassName(method) + "<T" + (returnClass == void.class ? "" : ", R") + "> extends java.io.Serializable {\n"
                + "        " + returnClassName + " apply(T t" + generateParameterTypes(method) + ");\n"
                + "    }\n";
    }

    public static String generateAddMethod(Method method, String methodName, String extraParam) {
        Class<?> returnClass = method.getReturnType();
        return "    public static <T> boolean " + methodName + "(" + generateLambdaHolderClassName(method) + "<T" + (returnClass == void.class ? "" : ", ?") + ">" + " lambda, String url) {\n"
                + "        com.github.linyuzai.dynamicfeign.lambda.SerializedLambda serializedLambda = com.github.linyuzai.dynamicfeign.lambda.SerializedLambda.resolve(lambda);\n"
                + "        return com.github.linyuzai.dynamicfeign.mapper.DynamicFeignClientMapper." + methodName + "(serializedLambda.getImplClass(), serializedLambda.getImplMethodName(), url);\n"
                + "    }\n";
    }

    public static String generateRemoveMethod(Method method, String methodName) {
        Class<?> returnClass = method.getReturnType();
        return "    public static <T> boolean " + methodName + "(" + generateLambdaHolderClassName(method) + "<T" + (returnClass == void.class ? "" : ", ?") + ">" + " lambda) {\n"
                + "        com.github.linyuzai.dynamicfeign.lambda.SerializedLambda serializedLambda = com.github.linyuzai.dynamicfeign.lambda.SerializedLambda.resolve(lambda);\n"
                + "        return com.github.linyuzai.dynamicfeign.mapper.DynamicFeignClientMapper.removeMethodUrl(serializedLambda.getImplClass(), serializedLambda.getImplMethodName());\n"
                + "    }\n";
    }

    public static String generateLambdaHolderClassName(Method method) {
        return method.getDeclaringClass().getSimpleName() + "_" + method.getName() + "_" + "LambdaHolder";
    }

    public static String generateParameterTypes(Method method) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (Class<?> cls : method.getParameterTypes()) {
            builder.append(", ").append(cls.getCanonicalName()).append(" v").append(i++);
        }
        return builder.toString();
    }

    private static boolean fileExists(String generatePath) {
        File file = getFile(generatePath);
        if (file.exists()) {
            logger.warn("LambdaHolder.java exists");
            return true;
        }
        return false;
    }

    private static File getFile(String generatePath) {
        return new File(generatePath, "LambdaHolder.java");
    }
}
