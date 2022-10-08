package net.spleefx.annotation;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RegistryProcessor extends AbstractProcessor {

    private static final Set<String> TYPES = Stream.of(RegisteredCommand.class).map(Class::getName)
            .collect(Collectors.toSet());

    private Filer filer;
    private TypeSpec.Builder generated;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        filer = processingEnv.getFiler();

        generated = TypeSpec.classBuilder("_GeneratedPluginRegistry")
                .addSuperinterface(ClassName.get("net.spleefx.backend", "PluginRegistry"))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {
        {
            MethodSpec.Builder registerCommands = MethodSpec.methodBuilder("registerCommands")
                    .addAnnotation(Override.class)
                    .addParameter(ClassName.get("net.spleefx", "SpleefX"), "app")
                    .addParameter(ClassName.get("org.bukkit.plugin.java", "JavaPlugin"), "plugin")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class);
            for (Element element : roundEnv.getElementsAnnotatedWith(RegisteredCommand.class)) {
                if (!(element instanceof TypeElement)) continue;
                RegisteredCommand command = element.getAnnotation(RegisteredCommand.class);
                TypeElement type = ((TypeElement) element);
                registerCommands.addStatement("app.getCommandHandler().add(new " + type.getQualifiedName().toString() + command.constructorSignature() + ")");
            }
            generated.addMethod(registerCommands.build());
        }

        {
            MethodSpec.Builder registerListeners = MethodSpec.methodBuilder("registerListeners")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassName.get("net.spleefx", "SpleefX"), "app")
                    .addParameter(ClassName.get("org.bukkit.plugin.java", "JavaPlugin"), "plugin")
                    .returns(void.class);
            for (Element element : roundEnv.getElementsAnnotatedWith(RegisteredListener.class)) {
                if (!(element instanceof TypeElement)) continue;
                RegisteredListener listener = element.getAnnotation(RegisteredListener.class);
                TypeElement type = ((TypeElement) element);
                registerListeners.addStatement("app.addListener(new " + type.getQualifiedName().toString() + listener.parameters() + ")");
            }
            generated.addMethod(registerListeners.build());
        }

        JavaFile javaFile = JavaFile.builder("net.spleefx.backend", generated.build())
                .build();

        try {
            javaFile.writeTo(filer);
        } catch (IOException ignored) {
        }
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return TYPES;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }
}
