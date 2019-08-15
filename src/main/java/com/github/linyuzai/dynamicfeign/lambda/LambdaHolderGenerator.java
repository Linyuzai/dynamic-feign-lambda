package com.github.linyuzai.dynamicfeign.lambda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;

public class LambdaHolderGenerator {

    private static Logger logger = LoggerFactory.getLogger(LambdaHolderGenerator.class);

    private static JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    private static List<String> classpath = new ArrayList<>();

    private static List<String> sourcepath = new ArrayList<>();

    public static String absoluteProjectPath = null;

    public static void addClasspath(String... classpath) {
        LambdaHolderGenerator.classpath.addAll(Arrays.asList(classpath));
    }

    public static void addSourcepath(String... sourcepath) {
        LambdaHolderGenerator.sourcepath.addAll(Arrays.asList(sourcepath));
    }

    public static void buildClasspath(File file) {
        if (file == null) {
            return;
        }
        if (file.isDirectory()) {
            addClasspath(file.getAbsolutePath() + File.separator + "*");
            File[] fileList = file.listFiles();
            if (fileList != null) {
                for (File f : fileList) {
                    buildClasspath(f);
                }
            }
        }
    }

    private static List<Class<?>> compile(List<File> files) {
        List<Class<?>> classes = new ArrayList<>();
        //String cp = String.join(File.pathSeparator, classpath);
        //String sp = String.join(File.pathSeparator, sourcepath);
        //Iterable<String> options = Arrays.asList("-classpath", cp, "-sourcepath", sp);
        //System.out.println(options);
        List<File> classpathFiles = new ArrayList<>();
        classpath.stream().map(File::new).forEach(classpathFiles::add);
        for (File file : files) {
            String content = null;
            try (FileInputStream fis = new FileInputStream(file); InputStreamReader isr = new InputStreamReader(fis);
                 BufferedReader br = new BufferedReader(isr)) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    builder.append(line).append("\n");
                }
                content = builder.toString();
                if (!content.contains("@FeignClient(")) {
                    continue;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (content == null) {
                continue;
            }
            try (StandardJavaFileManager stdManager = compiler.getStandardFileManager(null, null, null);
                 MemoryJavaFileManager manager = new MemoryJavaFileManager(stdManager)) {
                stdManager.setLocation(StandardLocation.CLASS_PATH, classpathFiles);
                JavaFileObject javaFileObject = manager.makeStringSource(file.getName(), content);
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
            String sourcePath = projectPath +
                    File.separator + "src" +
                    File.separator + "main" +
                    File.separator + "java";
            //buildClasspath(new File(sourcePath));
            String path = sourcePath +
                    File.separator +
                    packageName.replaceAll("\\.", Matcher.quoteReplacement(File.separator));
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
                content.append(generateAddMethod(method));
                content.append(generateRemoveMethod(method));
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
                + "    }\n\n";
    }

    public static String generateAddMethod(Method method) {
        Class<?> returnClass = method.getReturnType();
        return "    public static <T> boolean addMethodUrl(" + generateLambdaHolderClassName(method) + "<T" + (returnClass == void.class ? "" : ", ?") + ">" + " lambda, String url) {\n"
                + "        com.github.linyuzai.dynamicfeign.lambda.SerializedLambda serializedLambda = com.github.linyuzai.dynamicfeign.lambda.SerializedLambda.resolve(lambda);\n"
                + "        return com.github.linyuzai.dynamicfeign.mapper.DynamicFeignClientMapper.addMethodUrl(serializedLambda.getImplClass(), serializedLambda.getImplMethodName(), url);\n"
                + "    }\n\n";
    }

    public static String generateRemoveMethod(Method method) {
        Class<?> returnClass = method.getReturnType();
        return "    public static <T> boolean removeMethodUrl(" + generateLambdaHolderClassName(method) + "<T" + (returnClass == void.class ? "" : ", ?") + ">" + " lambda) {\n"
                + "        com.github.linyuzai.dynamicfeign.lambda.SerializedLambda serializedLambda = com.github.linyuzai.dynamicfeign.lambda.SerializedLambda.resolve(lambda);\n"
                + "        return com.github.linyuzai.dynamicfeign.mapper.DynamicFeignClientMapper.removeMethodUrl(serializedLambda.getImplClass(), serializedLambda.getImplMethodName());\n"
                + "    }\n\n";
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

    //********************************************************************************

    public static void generateFromBuild() {
        generateFromBuild("build" +
                File.separator + "classes" +
                File.separator + "java" +
                File.separator + "main");
    }

    public static void generateFromBuild(String path) {
        try {
            String projectPath = new File(absoluteProjectPath == null ?
                    new File("").getCanonicalPath() : absoluteProjectPath)
                    .getAbsolutePath();
            String buildPath = projectPath + File.separator + path;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterClasses() {

    }

    //*************************************************************************

    public static void generate(String packageName, Class<?>... classes) {
        try {
            String projectPath = new File(absoluteProjectPath == null ?
                    new File("").getCanonicalPath() : absoluteProjectPath)
                    .getAbsolutePath();
            String sourcePath = projectPath +
                    File.separator + "src" +
                    File.separator + "main" +
                    File.separator + "java";
            String path = sourcePath +
                    File.separator +
                    packageName.replaceAll("\\.", Matcher.quoteReplacement(File.separator));
            generateFromClasses(path, packageName, filterClasses(Arrays.asList(classes)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
