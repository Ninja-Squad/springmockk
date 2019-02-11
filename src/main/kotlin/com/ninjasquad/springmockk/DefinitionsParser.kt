package com.ninjasquad.springmockk

import org.springframework.core.ResolvableType
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.util.Assert
import org.springframework.util.ReflectionUtils
import org.springframework.util.StringUtils
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Field
import java.util.*
import kotlin.collections.LinkedHashSet
import kotlin.reflect.KClass

/**
 * Parser to create {@link MockkDefinition} and {@link SpykDefinition} instances from
 * {@link MockkBean @MockkBean} and {@link SpykBean @SpykBean} annotations declared on or in a
 * class.
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @author JB Nizet
 */
class DefinitionsParser(existing: Collection<Definition> = emptySet()) {
    private val definitions = LinkedHashSet<Definition>()
    private val definitionFields = mutableMapOf<Definition, Field>()

    init {
        definitions.addAll(existing)
    }

    val parsedDefinitions: Set<Definition>
        get() = Collections.unmodifiableSet(definitions)

    fun parse(source: Class<*>) {
        parseElement(source)
        ReflectionUtils.doWithFields(source, this::parseElement)
    }

    private fun parseElement(element: AnnotatedElement) {
        for (annotation in AnnotationUtils.getRepeatableAnnotations(
            element,
            MockkBean::class.java,
            MockkBeans::class.java
        )) {
            parseMockkBeanAnnotation(annotation, element)
        }
        for (annotation in AnnotationUtils.getRepeatableAnnotations(
            element,
            SpykBean::class.java,
            SpykBeans::class.java
        )) {
            parseSpykBeanAnnotation(annotation, element)
        }
    }

    private fun parseMockkBeanAnnotation(annotation: MockkBean, element: AnnotatedElement) {
        val typesToMock = getOrDeduceTypes(element, annotation.value)
        check(!typesToMock.isEmpty()) { "Unable to deduce type to mock from $element" }
        if (StringUtils.hasLength(annotation.name)) {
            check(typesToMock.size == 1) { "The name attribute can only be used when mocking a single class" }
        }
        for (typeToMock in typesToMock) {
            val definition = MockkDefinition(
                name = if (annotation.name.isEmpty()) null else annotation.name,
                typeToMock = typeToMock,
                extraInterfaces = annotation.extraInterfaces,
                clear = annotation.clear,
                relaxed = annotation.relaxed,
                relaxUnitFun = annotation.relaxUnitFun,
                qualifier = QualifierDefinition.forElement(element)
            )
            addDefinition(element, definition, "mock")
        }
    }

    private fun parseSpykBeanAnnotation(annotation: SpykBean, element: AnnotatedElement) {
        val typesToSpy = getOrDeduceTypes(element, annotation.value)
        Assert.state(
            !typesToSpy.isEmpty()
        ) { "Unable to deduce type to spy from $element" }
        if (StringUtils.hasLength(annotation.name)) {
            Assert.state(
                typesToSpy.size == 1,
                "The name attribute can only be used when spying a single class"
            )
        }
        for (typeToSpy in typesToSpy) {
            val definition = SpykDefinition(
                name = if (annotation.name.isEmpty()) null else annotation.name,
                typeToSpy = typeToSpy,
                clear = annotation.clear,
                qualifier = QualifierDefinition.forElement(element)
            )
            addDefinition(element, definition, "spy")
        }
    }

    private fun addDefinition(
        element: AnnotatedElement,
        definition: Definition,
        type: String
    ) {
        val isNewDefinition = this.definitions.add(definition)
        Assert.state(
            isNewDefinition
        ) { "Duplicate $type definition $definition" }
        if (element is Field) {
            this.definitionFields[definition] = element
        }
    }

    private fun getOrDeduceTypes(
        element: AnnotatedElement,
        value: Array<out KClass<*>>
    ): Set<ResolvableType> {
        val types = LinkedHashSet<ResolvableType>()
        for (clazz in value) {
            types.add(ResolvableType.forClass(clazz.java))
        }
        if (types.isEmpty() && element is Field) {
            types.add(ResolvableType.forField(element))
        }
        return types
    }

    fun getField(definition: Definition): Field? {
        return this.definitionFields[definition]
    }

}
