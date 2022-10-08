package net.spleefx.annotation;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PowerupFileProcessor extends AbstractProcessor {

    private static final File POWERUPS = new File("D:\\Java\\SpleefX_\\common\\src\\main\\resources\\power-ups");

    private static final Set<String> TYPES = Stream
            .of(Scan.class)
            .map(Class::getName)
            .collect(Collectors.toSet());

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        Filer filer = processingEnv.getFiler();
        Messager messager = processingEnv.getMessager();
        TypeSpec.Builder generated = TypeSpec.classBuilder("_GeneratedPowerupsFilesList").addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        StringJoiner files = new StringJoiner("\", \"", "\"", "\"").setEmptyValue("");
        for (File file : Objects.requireNonNull(POWERUPS.listFiles())) {
            if (file.getName().endsWith(".yml") || file.getName().endsWith(".schem")) {
                files.add("power-ups/" + file.getName());
                messager.printMessage(Kind.NOTE, file.getAbsolutePath());
            }
        }
        FieldSpec.Builder list = FieldSpec.builder(ClassName.get("com.google.common.collect", "ImmutableSet"), "FILES", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
        list.initializer("ImmutableSet.<String>of(" + files + ")");
        generated.addField(list.build());
        try {
            JavaFile powerupsFile = JavaFile.builder("net.spleefx.powerup.api", generated.build())
                    .build();
            powerupsFile.writeTo(filer);
        } catch (IOException ignored) {
        }
    }

    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return true;
    }

    @Override public Set<String> getSupportedAnnotationTypes() {
        return TYPES;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

}
