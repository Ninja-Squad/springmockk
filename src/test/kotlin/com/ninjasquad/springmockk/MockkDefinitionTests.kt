package com.ninjasquad.springmockk

import com.ninjasquad.springmockk.example.ExampleExtraInterface
import com.ninjasquad.springmockk.example.ExampleService
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import org.springframework.core.ResolvableType


/**
 * Tests for [MockkDefinition].
 *
 * @author Phillip Webb
 * @author JB Nizet
 */
class MockDefinitionTests {

    @Test
    fun createWithDefaults() {
        val definition = MockkDefinition(typeToMock = EXAMPLE_SERVICE_TYPE)
        assertThat(definition.name).isNull()
        assertThat(definition.typeToMock).isEqualTo(EXAMPLE_SERVICE_TYPE)
        assertThat(definition.extraInterfaces).isEmpty()
        assertThat(definition.relaxed).isFalse()
        assertThat(definition.clear).isEqualTo(MockkClear.AFTER)
        assertThat(definition.qualifier).isNull()
    }

    @Test
    fun createExplicit() {
        val qualifier = mockk<QualifierDefinition>()
        val definition = MockkDefinition(
            name = "name",
            typeToMock = EXAMPLE_SERVICE_TYPE,
            extraInterfaces = arrayOf(ExampleExtraInterface::class),
            relaxed = true,
            clear = MockkClear.BEFORE,
            qualifier = qualifier
        )
        assertThat(definition.name).isEqualTo("name")
        assertThat(definition.typeToMock).isEqualTo(EXAMPLE_SERVICE_TYPE)
        assertThat(definition.extraInterfaces).containsExactly(ExampleExtraInterface::class)
        assertThat(definition.relaxed).isTrue()
        assertThat(definition.clear).isEqualTo(MockkClear.BEFORE)
        assertThat(definition.qualifier).isEqualTo(qualifier)
    }

    @Test
    fun createMock() {
        val definition = MockkDefinition(
            name = "blabla",
            typeToMock = EXAMPLE_SERVICE_TYPE,
            extraInterfaces = arrayOf(ExampleExtraInterface::class),
            relaxed = true,
            clear = MockkClear.BEFORE,
            qualifier = null
        )
        val mock = definition.createMock<Any>()
        assertThat(mock).isInstanceOf(ExampleService::class.java)
        assertThat(mock).isInstanceOf(ExampleExtraInterface::class.java)
        assertThat(mock.toString()).contains("blabla")

        // test that it's indeed relaxed
        assertThatCode { (mock as ExampleService).greeting() }.doesNotThrowAnyException()
        assertThat(MockkClear.get(mock)).isEqualTo(MockkClear.BEFORE)
    }

    companion object {
        private val EXAMPLE_SERVICE_TYPE = ResolvableType.forClass(ExampleService::class.java)
    }

}
