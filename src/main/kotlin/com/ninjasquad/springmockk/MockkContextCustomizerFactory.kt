package com.ninjasquad.springmockk

import org.springframework.test.context.ContextConfigurationAttributes
import org.springframework.test.context.ContextCustomizer
import org.springframework.test.context.ContextCustomizerFactory

/**
 * A {@link ContextCustomizerFactory} to add MockK support.
 *
 * @author Phillip Webb
 * @author JB Nizet
 */
class MockkContextCustomizerFactory : ContextCustomizerFactory {
    override fun createContextCustomizer(
        testClass: Class<*>,
        configAttributes: List<ContextConfigurationAttributes>
    ): ContextCustomizer {
        // We gather the explicit mock definitions here since they form part of the
        // MergedContextConfiguration key. Different mocks need to have a different key.
        val parser = DefinitionsParser()
        parser.parse(testClass)
        return MockkContextCustomizer(parser.parsedDefinitions)
    }
}
