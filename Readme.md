<p align="center">
<a href="https://openjdk.java.net/"><img src="https://img.shields.io/badge/Java-17+-green?logo=java&logoColor=white" alt="Java support"></a>
<a href="https://www.apache.org/licenses/LICENSE-2.0.html"><img src="https://img.shields.io/github/license/dk900912/easyexcel-spring-boot?color=4D7A97&logo=apache" alt="License"></a>
<a href="https://search.maven.org/search?q=a:easyexcel-spring-boot-starter"><img src="https://img.shields.io/maven-central/v/io.github.dk900912/easyexcel-spring-boot-starter?logo=apache-maven" alt="Maven Central"></a>
<a href="https://github.com/dk900912/easyexcel-spring-boot/releases"><img src="https://img.shields.io/github/release/dk900912/easyexcel-spring-boot.svg" alt="GitHub release"></a>
<a href="https://github.com/dk900912/easyexcel-spring-boot/stargazers"><img src="https://img.shields.io/github/stars/dk900912/easyexcel-spring-boot" alt="GitHub Stars"></a>
<a href="https://github.com/dk900912/easyexcel-spring-boot/fork"><img src="https://img.shields.io/github/forks/dk900912/easyexcel-spring-boot" alt="GitHub Forks"></a>
<a href="https://github.com/dk900912/easyexcel-spring-boot/issues"><img src="https://img.shields.io/github/issues/dk900912/easyexcel-spring-boot" alt="GitHub issues"></a>
<a href="https://github.com/dk900912/easyexcel-spring-boot/graphs/contributors"><img src="https://img.shields.io/github/contributors/dk900912/easyexcel-spring-boot" alt="GitHub Contributors"></a>
<a href="https://github.com/dk900912/easyexcel-spring-boot"><img src="https://img.shields.io/github/repo-size/dk900912/easyexcel-spring-boot" alt="GitHub repo size"></a>
</p>

---

<!-- TOC -->
  * [1 快速上手](#1-快速上手)
    * [1.1 引入依赖](#11-引入依赖)
    * [1.2 导入与导出](#12-导入与导出)
  * [2 配置项](#2-配置项)
  * [3 Bean Validation](#3-bean-validation)
<!-- TOC -->

EasyExcel 是一款由阿里开源的 Excel 处理工具。相较于原生的`Apache POI`，它可以更优雅、快速地完成 Excel 的读写功能，同时更加地节约内存。即使 EasyExcel 已经很优雅了，但面向 Excel 文档的读写逻辑几乎千篇一律，笔者索性将这些模板化的逻辑抽离出来，该组件已经发布到 maven 中央仓库，感兴趣的朋友可以体验一下。

目前仅支持针对单个 Excel 文档的导入与导出（支持多Sheet），所以由 @RequestExcel 注解修饰的方法参数必须是一个List<List<>>类型，而由 @ResponseExcel 注解修饰的方法返回类型也必须是一个List<List<>>类型，否则将抛出异常。

## 1 快速上手
### 1.1 引入依赖
> easyexcel 组件组要自行引入，强制使用 3.3+ 版本。
```xml
<dependency>
	<groupId>io.github.dk900912</groupId>
	<artifactId>easyexcel-spring-boot-starter</artifactId>
	<version>0.0.9</version>
</dependency>
```
### 1.2 导入与导出
```java
@RestController
@RequestMapping(path = "/easyexcel")
public class ExcelController {

    @PostMapping(path = "/v1/upload")
    public ResponseEntity<String> v1upload(
            @RequestExcel(sheets = {
                    @Sheet(index = 0, headClazz = User.class, headRowNumber = 1),
                    @Sheet(index = 1, headClazz = User.class, headRowNumber = 1),
                    @Sheet(index = 2, headClazz = User.class, headRowNumber = 1)}
            )
            List<List<User>> users) {
        return ResponseEntity.ok("OK");
    }

    @PostMapping(path = "/v2/upload")
    public ResponseEntity<String> v2upload(
            @RequestExcel(sheets = {
                    @Sheet(index = 0, headClazz = User.class, headRowNumber = 1),
                    @Sheet(index = 1, headClazz = Admin.class, headRowNumber = 1)}
            )
            List<List<Object>> data) {
        return ResponseEntity.ok("OK");
    }

    @ResponseExcel(
            name="程序猿",
            sheets = {
                    @Sheet(name = "sheet-0", headClazz = User.class),
                    @Sheet(name = "sheet-1", headClazz = User.class),
                    @Sheet(name = "sheet-2", headClazz = User.class)
            },
            suffix = ExcelTypeEnum.XLSX)
    @GetMapping(path = "/v1/export")
    public List<List<User>> v1export() {
        List<User> userList = Lists.newArrayList();
        for (int i = 0; i < 10000; i++) {
            User user = User.builder().name("暴风赤红" + (i+1))
                    .birth(LocalDate.now()).address("江苏省苏州市科技城昆仑山路58号").sex(Sex.MALE)
                    .build();
            userList.add(user);
        }
        return ImmutableList.of(userList, userList, userList);
    }

    @ResponseExcel(
            name="程序猿",
            sheets = {
                    @Sheet(name = "sheet-0", headClazz = User.class),
                    @Sheet(name = "sheet-1", headClazz = Admin.class)
            },
            suffix = ExcelTypeEnum.XLS)
    @GetMapping(path = "/v2/export")
    public List<List<?>> v2export() {
        List<User> userList = Lists.newArrayList();
        List<Admin> adminList = Lists.newArrayList();
        for (int i = 0; i < 10000; i++) {
            User user = User.builder().name("暴风赤红" + (i+1))
                    .birth(LocalDate.now()).address("江苏省苏州市科技城昆仑山路58号").sex(Sex.MALE)
                    .build();
            userList.add(user);
            Admin admin = Admin.builder().name("擎天柱" + (i+1))
                    .birth(LocalDate.now()).address("江苏省苏州市科技城昆仑山路68号").sex(Sex.MALE)
                    .build();
            adminList.add(admin);
        }

        List<List<?>> responseData = Lists.newArrayList();
        responseData.add(userList);
        responseData.add(adminList);

        return responseData;
    }

    @ResponseExcel(name="templates/tem.xlsx", scene = TEMPLATE)
    @GetMapping(path = "/v1/template")
    public void template() {}
}
```
## 2 配置项
> 一般无需配置
```
spring.easy-excel.enabled=true
spring.easy-excel.converter.media-types[0]=application/octet-stream
spring.easy-excel.template.location=classpath:
```

## 3 导出文件名

在非模板导出场景下，导出文件名可以显式指定，也可以不指定，此时会默认使用基于 UUID 的文件名生成策略，如果不满足大家的需求，可以自行实现`FileNameGenerator`策略接口，然后追加配置项，如下所示：

```java
spring.easy-excel.name.generator=io.github.xiaotou.easyexcel.TimestampFileNameGenerator
```

## 4 Bean Validation

依托于 Spring 内部 `MethodValidationInterceptor`的能力，配合`@Validated`和`@Valid`注解即可实现。

```java
@Validated
@RestController
@RequestMapping(path = "/easyexcel")
public class ExcelController {

    @PostMapping(path = "/v1/upload")
    public ResponseEntity<String> v1upload(
            @RequestExcel(sheets = {
                    @Sheet(index = 0, headClazz = User.class, headRowNumber = 1),
                    @Sheet(index = 1, headClazz = User.class, headRowNumber = 1),
                    @Sheet(index = 2, headClazz = User.class, headRowNumber = 1)}
            )
            List<List<@Valid User>> users, @RequestParam("id") String id) {
        System.out.println(users);
        System.out.println(id);
        return ResponseEntity.ok("OK");
    }
}
```

异常信息如下：
```java
jakarta.validation.ConstraintViolationException: v1upload.users[0].<list element>[0].name: name不能为空
	at org.springframework.validation.beanvalidation.MethodValidationInterceptor.invoke(MethodValidationInterceptor.java:138) ~[spring-context-6.0.9.jar:6.0.9]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184) ~[spring-aop-6.0.9.jar:6.0.9]
	at org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:750) ~[spring-aop-6.0.9.jar:6.0.9]
	at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:702) ~[spring-aop-6.0.9.jar:6.0.9]
	at io.github.xiaotou.easyexcel.ExcelController$$SpringCGLIB$$0.v1upload(<generated>) ~[classes/:na]
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:na]
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77) ~[na:na]
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:568) ~[na:na]
	at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:207) ~[spring-web-6.0.9.jar:6.0.9]
	at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:152) ~[spring-web-6.0.9.jar:6.0.9]
	at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:118) ~[spring-webmvc-6.0.9.jar:6.0.9]
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:884) ~[spring-webmvc-6.0.9.jar:6.0.9]
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:797) ~[spring-webmvc-6.0.9.jar:6.0.9]
	at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87) ~[spring-webmvc-6.0.9.jar:6.0.9]
	at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1081) ~[spring-webmvc-6.0.9.jar:6.0.9]
	at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:974) ~[spring-webmvc-6.0.9.jar:6.0.9]
	at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1011) ~[spring-webmvc-6.0.9.jar:6.0.9]
	at org.springframework.web.servlet.FrameworkServlet.doPost(FrameworkServlet.java:914) ~[spring-webmvc-6.0.9.jar:6.0.9]
	at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:590) ~[tomcat-embed-core-10.1.8.jar:6.0]
	at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:885) ~[spring-webmvc-6.0.9.jar:6.0.9]
	at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:658) ~[tomcat-embed-core-10.1.8.jar:6.0]
```