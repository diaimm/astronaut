# *Astronaut*
[![Travis CI](https://travis-ci.org/diaimm/astronaut.svg?branch=master)](https://travis-ci.org/diaimm/astronaut "master")
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.esotericsoftware/kryo/badge.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.diaimm%22%20AND%20a%3Aastronaut-abstract)
[[Wiki](https://github.com/diaimm/astronaut/wiki)]

Astronaut is a client builder abstraction for Rest API Call.

I've found many Rest-API projects are making client classes having RestTemplate@Spring directly in its class.
And I'm sure that these duplicated patterns(or classes) are not good, and just as spring framework do, we can **generate** them just like it uses @Repository annotation in **JPA** framework.

Ideally thinking, even though the Rest API Server does not mean a DataBase or persistence Machine, if possible in a project, it must be able to be regarded as one of Repository Layer things. And it is because we normally call such an existences that is in charge of querying or CRUDing of data as a Repository or an Persistence layer.

So, as a repository, an Rest-API Client that is generated by this Astronaut can support Transactions of Repositories by implementing transaction related things of Spring framework for it self. And as it implements the APIs for transaction of Spring, it can be managed by Transaction Manager or be under the control of **@Transactional**.
(If the API server is supporting the APIs for CRUD/Commit/Rollback separately)


# *Start*
### maven dependency
	<dependency>
		<groupId>com.github.diaimm</groupId>
		<artifactId>astronaut-abstract</artifactId>
		<version>0.0.1.RELEASE</version>
	</dependency>
	
### Annotation based.
Annotation based configurations are adopted, just like you make a repository with @Repository of Spring-jpa framework.
With some annotations prepared, you can configure Rest API information.
	
##### @RestAPIRepository
@RestAPIRepository is an annotation that is indicating this interface must be build up as a Rest-API Client.
As a *value* attribute, it requires the name of RestTemplate bean.

The Configurer(you can see the details below) will make a proxied instance and will register this instance into the ApplicationContext so that you can use it with any way Spring supporting (for example, @Autowired).

	@RestAPIRepository("someRestTemplateName")
	public interface SomeAPIClient {
	}
	
##### Mapping for the API parameters with @GetForObject.
The first sample is a simple mapping for an API with the url '/this/is/a/sample/path'.
This API will return some response and we will assume this json response can unmarshalled to a class named SampleResponse.

This API requires four parameters,
1. p1 as an Integer
2. p2 as a String
3. p3 as a Long

For this API...
* We use *@GetForObject*, and this requires a value for URL you have to use.
* We use *@Param* annotations to map the arguments of this method to the parameters of the API.
- The value of each *@Param* will be used as each parameter name.
- When you call this method, you will give arguments for this method, and each value will be mapped for each parameter

So, if you call this method, the RestTemplate will get URL to call of below.

	*http://some.host.name.com/this/is/a/sample/path?p1=your input1&p2=your input2&p3=your input3* 

See the sample.

	@RestAPIRepository("someRestTemplateName")
	public interface SomeAPIClient {
		@GetForObject(url = "/this/is/a/sample/path")
		SampleResponse sampleUrl(@Param("p1") Integer param1, @Param("p2") String param2, @Param("p3") Long param3);
	}
	
In some other class you've got the proxy instance of this, you can call like this.
	
	SampleResponse response = someAPIClient.sampleUrl(1, "test", 3L);
	
Then, it will call the url below through RestTemplate and will map the response json into SampleResponse class. 
	
	http://some.host.name.com/this/is/a/sample/path?p1=1&p2=test&p3=3
	
Available annotations for http methods 
The annotations are made based on the methods of RestTemplate class. It means that each annotation will call matched method of RestTemplate anyhow. 

	@GetForObject
	@PostForObject
	@HeadForHeaders
	@OperationsForAllow
	@PostForLocation
	@Put
	@PutForObject
	@Delete
	
##### Mapping for the API Path variable with @PathParam. #####
With @PathParam, you can adopt @PathVariable value of API server. In other words, if you have to make a dynamic url based on the values of arguments, you can configure it as it requires.

See the Sample first.

	@RestAPIRepository("someRestTemplateName")
	public interface SomeAPIClient {
		@GetForObject(url = "/test/{p1}/ddd/{!p2}/ddd/{!p3}/{ p4 }")
		SampleResponse sampleMethodWithPathParam(@PathParam("p1") String path1, @PathParam("p2") String path2, @PathParam("p3") String path3, @PathParam("p4") String path4);
	}
	
With '{' and '}' you can declare a variable values in Request URI. An for this variables, you have to use @PathParam annotation for each bindings.
In the sample above, for p1, p2, p3, p4, there are @PathParam annotation that has same value for each variables, and with this annotations, the Astronaut can bind real values in your real calling phase.

The thing that you have remember is...
1. the blanks between '{' and '}' will be ignored( it will be trimmed )
2. If you use '!' before the variable than this means that "If the value for this variable is null or empty, then this path will be removed from the URL the Astronaut will make".


For an instance, these can be possble about the sample above.
- below will make this url : *http://some.host.com/test/a/ddd/b/ddd/c/d*

	someAPIClient.sampleMethodWithPathParam("a", "b", "c", "d");
	
- below will make this url : *http://some.host.com/test/a/ddd/ddd/c/d*

	someAPIClient.sampleMethodWithPathParam("a", null, "c", "d");
	
- below will make this url : *http://some.host.com/test/a/ddd/ddd/c/d*

	someAPIClient.sampleMethodWithPathParam("a", "", "c", "d");	
  
##### Using Param DTO to cover complicated arguments of a method #####
 From time to time, you meet such a case that the arguments to call an API is too complicated to list them as the arguments of a method.
 In this case, we normally choose to use a DTO who has all the arguments required or optional.
 
 For this, you can use @Form annotation and same annotations for arguments of methods in fields of that DTO.
 
 This is the sample.
 
		@GetForObject(url = "/test/{path1}/ddd/{ !path2}/ddd/{!path3 }/{ path4 }", dummySupplier = SampleParamSupplier.class)
		SampleResponse sampleMethod3(@Form SampleParam param);
		
		public static class SampleParamSupplier implements Supplier<SampleParam> {
			@Override
			public SampleParam get() {
				return new SampleParam(new StringBuilder("/test/{p1}/ddd/ddd/{ p4 } }"));
			}
		}
		
		public static class SampleParam {
			@PathParam
			private String path1 = "1";
			@PathParam
			private String path2 = "2";
			@PathParam
			private String path3 = "3";
			@PathParam
			private String path4 = "4";
			@Param
			private String value5 = "5";
			@Param
			private String value6 = "6";
			@Param
			private String value7 = "7";
		}
 
In the sample, above, you can see @PathParam and @Param are used for fields in DTO that must be given the method.
And one more thing is *SampleParamSupplier* class. This class is implementing *Supplier<SampleParam>* and is being given as a value for *dummySupplier* of @GetForObject.

The role of SampleParamSupplier is to make the fields and the information for mapping fixed, and by giving the *class* of SampleParamSupplier to *dummySupplier*, the Astronaut can see what it have to bind.

# *The MIT License (MIT)*
Copyright (c) 2016 diaimm

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
