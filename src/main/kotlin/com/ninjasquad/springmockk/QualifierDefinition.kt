package com.ninjasquad.springmockk

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.config.DependencyDescriptor
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.core.annotation.AnnotationUtils
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Field
import java.util.*

/**
 * Definition of a Spring [Qualifier](@Qualifier).
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @author JB Nizet
 * @see Definition
 */
class QualifierDefinition(private val field: Field, private val annotations: Set<Annotation>) {

    private val descriptor: DependencyDescriptor

    init {
        this.descriptor = DependencyDescriptor(field, true)
    }

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
        fun forElement(element: AnnotatedElement): QualifierDefinition? {
            if (element is Field) {
                val annotations = getQualifierAnnotations(element)
                if (!annotations.isEmpty()) {
                    return QualifierDefinition(element, annotations)
                }
            }
            return null
        }

        private fun getQualifierAnnotations(field: Field): Set<Annotation> {
            // Assume that any annotations other than @MockkBean/@SpykBean are qualifiers
            val candidates = field.declaredAnnotations
            val annotations = HashSet<Annotation>(candidates.size)
            for (candidate in candidates) {
                if (!isMockOrSpyAnnotation(candidate)) {
                    annotations.add(candidate)
                }
            }
            return annotations
        }

        private fun isMockOrSpyAnnotation(candidate: Annotation): Boolean {
            val annotationClass = candidate.annotationClass
            return (annotationClass == MockkBean::class || annotationClass == SpykBean::class
                || AnnotationUtils.isAnnotationMetaPresent(annotationClass.java, MockkBean::class.java)
                || AnnotationUtils.isAnnotationMetaPresent(annotationClass.java, SpykBean::class.java))
        }
    }
}
