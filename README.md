# SpringMockK

[![CircleCI](https://circleci.com/gh/Ninja-Squad/springmockk.svg?style=svg)](https://circleci.com/gh/Ninja-Squad/springmockk)

Support for Spring Boot integration tests written in Kotlin using [MockK](https://mockk.io/) instead of Mockito.
 
Spring provides `@MockitoBean` and `@MockitoSpyBean` annotations for integration tests, which create mock/spy beans using Mockito.

This project provides equivalent annotations `MockkBean` and `MockkSpyBean` to do the exact same thing with MockK.

## Principle

All the Mockito-specific classes of Spring, including the automated tests, have been cloned, translated to Kotlin, and adapted to MockK.

This library thus provides the same functionality as the standard Mockito-based Spring mock beans.

For example:

```kotlin
@ExtendWith(SpringExtension::class)
@WebMvcTest
class GreetingControllerTest {
    @MockkBean
    private lateinit var greetingService: GreetingService
    
    @Autowired
    private lateinit var controller: GreetingController
    
    @Test
    fun `should greet by delegating to the greeting service`() {
        every { greetingService.greet("John") } returns "Hi John"
        
        assertThat(controller.greet("John")).isEqualTo("Hi John")
        verify { greetingService.greet("John") }
    }
}
```

## Usage

### Gradle (Kotlin DSL)

Add this to your dependencies:
```kotlin
testImplementation("com.ninja-squad:springmockk:5.0.1")
```

### Maven

Add this to your dependencies:
```xml
<dependency>
  <groupId>com.ninja-squad</groupId>
  <artifactId>springmockk</artifactId>
  <version>5.0.1</version>
  <scope>test</scope>
</dependency>
```

## Differences with Mockito

 - the MockK defaults are used, which means that mocks created by the annotations are strict (i.e. not relaxed) by default. But [you can configure MockK](https://mockk.io/#settings-file) to use different defaults globally, or you can use `@MockkBean(relaxed = true)` or `@MockkBean(relaxUnitFun = true)`. 
 - the created mocks can't be serializable as they can be with Mockito (AFAIK, MockK doesn't support that feature)
 - When *spying* a bean that is then wrapped in a Spring AOP proxy (for example when using the `@Cacheable` annotation), you must stub and verify using the ultimate target of the bean, rather
  than the bean itself. Use `AopTestUtils.getUltimateTargetObject()` to get the ultimate target.

## Gotchas

### Qualifier annotations

Qualifier annotations are looked up on fields, and not on properties.
This doesn't matter much until you use a custom qualifier annotation.
In that case, make sure that it targets fields and not properties, or use `@field:YourQualifier` to apply it on your beans.

### JDK proxies
In some situations, the beans that need to be spied are JDK proxies. In recent versions of Java (Java 16+ AFAIK),
MockK can't spy JDK proxies unless you pass the argument `--add-opens java.base/java.lang.reflect=ALL-UNNAMED`
to the JVM running the tests.

Not doing that and trying to spy on a JDK proxy will lead to an error such as

```
java.lang.IllegalAccessException: class io.mockk.impl.InternalPlatform cannot access a member of class java.lang.reflect.Proxy (in module java.base) with modifiers "protected"
```

To pass that option to the test JVM with Gradle, configure the test task with

```kotlin
tasks.test {
    // ...
    jvmArgs(
        "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED"
    )
}
```

For Maven users:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
      <argLine>
        --add-opens java.base/java.lang.reflect=ALL-UNNAMED
      </argLine>
    </configuration>
</plugin>
````

## Limitations
 - [this is not an official Spring project](https://github.com/spring-projects/spring-boot/issues/15749), so it might not work out of the box for newest versions if backwards incompatible changes are introduced in Spring.

## Versions compatibility

SpringMockK originally cloned the Mockito annotations provided by Spring Boot (`MockBean` and `SpyBean`).
Spring Boot now doesn't provide these annotations anymore. Similar annotations are now provided
by the Spring Framework itself.

Since version 5.x of SpringMockK, it clones the Mockito annotations from Spring Framework.

 - Version 5.x of SpringMockK: compatible with Spring Framework 7, Java 17+
 - Version 4.x of SpringMockK: compatible with Spring Boot 3.x, Java 17+
 - Version 3.x of SpringMockK: compatible with Spring Boot 2.4.x, 2.5.x and 2.6.x, Java 8+
 - Version 2.x of SpringMockK: compatible with Spring Boot 2.2.x and 2.3.x, Java 8+
 - Version 1.x of SpringMockK: compatible with Spring Boot 2.1.x, Java 8+

## Migrating to version 5.x

Most of the changes have been done to align SpringMockK with the native Mockito support in
Spring framework.

- Replace `@SpykBean` by `@MockkSpyBean`: the annotation has been renamed to be consistent with the naming used by Spring Framework for `@MockitoBean` and `@MockitoSpyBean`
- `@MockkBean` and `@MockkSpyBean` are now written in Kotlin, and are repeatable using Kotlin's native mechanism. If you were using `@MockkBeans` and `@SpykBeans` explicitly, don't do it anymore and repeat the `@MockkBean` and `@MockkSpyBean` annotations.
- The `classes` property has been renamed to `types`. it was previously an alias for `classes`. So if you had `@MockkBean(classes = [SomeService::class])`, it should be rewrteen as `@MockkBean(types = [SomeService::class])`
- The `value` property is now an alias for `name`. it was previously an alias for `classes`. So if you had `@MockkBean([SomeService::class])`, it should be rewrteen as `@MockkBean(types = [SomeService::class])`
- The extension property `com.ninjasquad.springmockk.MockkFunctionsKt.isMock` has been renamed to 
  `isMockOrSpy`.

## How to build

```
  ./gradlew build
```
