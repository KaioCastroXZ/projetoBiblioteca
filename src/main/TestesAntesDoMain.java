package main;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class TestesAntesDoMain {

    private TestesAntesDoMain() {
    }

    public static void executar() {
        Path raizProjeto = Path.of(System.getProperty("user.dir"));
        Path pastaTestes = raizProjeto.resolve("test");
        if (!Files.isDirectory(pastaTestes)) {
            return;
        }

        try {
            Path classesTestes = raizProjeto.resolve("target").resolve("test-classes");
            Files.createDirectories(classesTestes);

            List<String> arquivosTeste;
            try (Stream<Path> paths = Files.walk(pastaTestes)) {
                arquivosTeste = paths
                        .filter(path -> path.toString().endsWith(".java"))
                        .map(Path::toString)
                        .toList();
            }

            if (arquivosTeste.isEmpty()) {
                return;
            }

            compilarTestes(classesTestes, arquivosTeste);
            executarTestes(classesTestes);
        } catch (Exception e) {
            throw new IllegalStateException("Os testes falharam. O sistema nao sera iniciado.", e);
        }
    }

    private static void compilarTestes(Path classesTestes, List<String> arquivosTeste) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("Execute o projeto com um JDK, nao apenas com uma JRE.");
        }

        String classpath = System.getProperty("java.class.path")
                + File.pathSeparator
                + Path.of(System.getProperty("user.dir")).resolve("target").resolve("classes");

        List<String> argumentos = Stream.concat(
                Stream.of("-encoding", "UTF-8", "-classpath", classpath, "-d", classesTestes.toString()),
                arquivosTeste.stream()
        ).toList();

        int resultado = compiler.run(null, null, null, argumentos.toArray(new String[0]));
        if (resultado != 0) {
            throw new IllegalStateException("Nao foi possivel compilar os testes.");
        }
    }

    private static void executarTestes(Path classesTestes) throws Exception {
        ClassLoader anterior = Thread.currentThread().getContextClassLoader();
        URLClassLoader classLoader = new URLClassLoader(
                new URL[]{classesTestes.toUri().toURL()},
                TestesAntesDoMain.class.getClassLoader()
        );

        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                    .selectors(DiscoverySelectors.selectClasspathRoots(Set.of(classesTestes)))
                    .build();
            Launcher launcher = LauncherFactory.create();
            SummaryGeneratingListener resumo = new SummaryGeneratingListener();
            launcher.registerTestExecutionListeners(new ImpressoraDeTestes(), resumo);
            launcher.execute(request);

            TestExecutionSummary summary = resumo.getSummary();
            System.out.println("Testes executados: " + summary.getTestsSucceededCount()
                    + "/" + summary.getTestsFoundCount());

            if (summary.getTestsFailedCount() > 0) {
                summary.getFailures().forEach(falha -> System.err.println(
                        falha.getTestIdentifier().getDisplayName() + ": " + falha.getException().getMessage()
                ));
                throw new IllegalStateException("Existem testes falhando.");
            }
        } finally {
            Thread.currentThread().setContextClassLoader(anterior);
            classLoader.close();
        }
    }

    private static class ImpressoraDeTestes implements TestExecutionListener {
        @Override
        public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
            if (testIdentifier.isTest() && testExecutionResult.getStatus() == TestExecutionResult.Status.FAILED) {
                System.err.println("Falhou: " + testIdentifier.getDisplayName());
            }
        }
    }
}
