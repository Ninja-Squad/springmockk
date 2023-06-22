package com.ninjasquad.springmockk

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.config.DependencyDescriptor
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.core.annotation.MergedAnnotations
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Field
import java.lang.reflect.Parameter


/**
 * Definition of a Spring [Qualifier](@Qualifier).
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @author JB Nizet
 * @see Definition
 */
class QualifierDefinition(private val field: Field, private val annotations: Set<Annotation>) {

    private val descriptor = DependencyDescriptor(field, true)

    fun matches(beanFactory: ConfigurableListableBeanFactory, beanName: String): Boolean {
        return beanFactory.isAutowireCandidate(beanName, this.descriptor)
    }

    fun applyTo(definition: RootBeanDefinition) {
        definition.qualifiedElement = this.field
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other == null || !javaClass.isAssignableFrom(other.javaClass)) {
            return false
        }
        other as QualifierDefinition
        return this.annotations == other.annotations
    }

    override fun hashCode(): Int {
        return this.annotations.hashCode()
    }

    companion object {
        fun forElement(
            element: AnnotatedElement,
            source: Class<*>? = null,
        ): QualifierDefinition? {
            val qualifiers = element.declaredAnnotations
                // Assume that any annotations other than @MockkBean/@SpykBean are qualifiers
                .filterNot { isMockOrSpyAnnotation(it.annotationClass.java) }
                .toSet()
                .takeUnless { it.isEmpty() }
                ?: return null

            return when (element) {
                is Field -> QualifierDefinition(element, qualifiers)
                is Parameter -> source?.declaredFields
                    ?.singleOrNull { it.type == element.type }
                    ?.let { QualifierDefinition(it, qualifiers) }
                else -> null
            }
        }

        private fun isMockOrSpyAnnotation(type: Class<out Annotation>): Boolean {
            val annotations = setOf(MockkBean::class.java, SpykBean::class.java)
            if (type in annotations) return true

            val metaAnnotations = MergedAnnotations.from(type)
            return annotations.any { metaAnnotations.isPresent(it) }
        }
    }
}
