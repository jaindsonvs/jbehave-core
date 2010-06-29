package org.jbehave.core.configuration.pico;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.core.annotations.pico.UsingPico;
import org.jbehave.core.configuration.AnnotationBuilder;
import org.jbehave.core.configuration.AnnotationFinder;
import org.jbehave.core.configuration.AnnotationMonitor;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MissingAnnotationException;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.jbehave.core.steps.pico.PicoStepsFactory;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;

public class PicoAnnotationBuilder extends AnnotationBuilder {

    private PicoContainer container;

    public PicoAnnotationBuilder(Class<?> annotatedClass) {
        super(annotatedClass);
    }

    public PicoAnnotationBuilder(Class<?> annotatedClass, AnnotationMonitor annotationMonitor) {
        super(annotatedClass, annotationMonitor);
    }

    @SuppressWarnings({ "rawtypes" })
    public Configuration buildConfiguration() throws MissingAnnotationException {
        AnnotationFinder finder = annotationFinder();
        if (finder.isAnnotationPresent(UsingPico.class)) {
            List<Class> moduleClasses = finder.getAnnotatedValues(UsingPico.class, Class.class, "modules");
            List<PicoModule> modules = new ArrayList<PicoModule>();
            for (Class<PicoModule> moduleClass : moduleClasses) {
                try {
                    modules.add(moduleClass.newInstance());
                } catch (Exception e) {
                    annotationMonitor().elementCreationFailed(moduleClass, e);
                }
            }
            if ( modules.size() > 0 ){
                container = picoContainerFor(modules);                
            }
        } else {
            annotationMonitor().annotationNotFound(UsingPico.class, annotatedClass());
        }
        return super.buildConfiguration();
    }

    @SuppressWarnings("unchecked")
    private PicoContainer picoContainerFor(List<PicoModule> modules) {
        MutablePicoContainer container = instanceOf(MutablePicoContainer.class, annotationFinder().getAnnotatedValue(UsingPico.class, Class.class, "container"));
        for (PicoModule module : modules) {
            module.configure(container);
        }
        return container;
    }

    @Override
    public List<CandidateSteps> buildCandidateSteps() {
        Configuration configuration = buildConfiguration();
        InjectableStepsFactory factory = new InstanceStepsFactory(configuration);
        if (container != null) {
            factory = new PicoStepsFactory(configuration, container);
        }
        return factory.createCandidateSteps();
    }

    @Override
    protected ParameterConverters parameterConverters(AnnotationFinder annotationFinder) {
        if (container != null) {
            List<ParameterConverter> converters = container.getComponents(ParameterConverter.class);
            return new ParameterConverters().addConverters(converters);
        }
        return super.parameterConverters(annotationFinder);
    }

    @Override
    protected <T> T instanceOf(Class<T> type, Class<T> ofClass) {
        if (container != null) {
            T instance = container.getComponent(type);
            if ( instance != null ){
                return instance;
            } else {
                // fall back on default
                // getAnnotationMonitor().elementCreationFailed(type, e);
            }
        }
        return super.instanceOf(type, ofClass);
    }

}
