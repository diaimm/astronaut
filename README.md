# *Astronaut*
[Spring 3.2](https://projects.spring.io/spring-framework/)
[Sonatype Nexus Repository](https://oss.sonatype.org/#nexus-search;quick~astronaut)

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
	
##### Mapping for the API parameters.
The first sample is a simple mapping for an API with the url '/this/is/a/sample/path'.
This API will return some response and we will assume this json response can unmarshalled to a class named SampleResponse.

This API requires four parameters,
1. p1 as an Integer
2. p2 as a String
3. p3 as a Long

For this API...
1. We use *@GetForObject*, and this requires a value for URL you have to use.
2. We use *@Param* annotations to map the arguments of this method to the parameters of the API.
2-1. The value of each *@Param* will be used as each parameter name.
2-2. When you call this method, you will give arguments for this method, and each value will be mapped for each parameter

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
    

# *The MIT License (MIT)*
Copyright (c) 2016 diaimm

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.