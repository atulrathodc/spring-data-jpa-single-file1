package com.dailycodebuffer.spring.data.jpa.tutorial;

import com.fasterxml.jackson.core.JsonProcessingException;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsys.TestUtilly;
//import com.tsys.TestUtilly1;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.context.event.*;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.support.RequestHandledEvent;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@SpringBootApplication
@RestController
//@EnableKafkaStreams
@AllArgsConstructor
class SpringDataJpaTutorialApplication1 {

	//	public static void main(String[] args) {
//   // Output: Message from Child
//	}
	public static void main(String[] args) throws JsonProcessingException {
		ApplicationContext context = SpringApplication.run(SpringDataJpaTutorialApplication1.class, args);
		context.getBean(MyService.class).printMessage();
	}
}

class LoggingInterceptor implements ClientHttpRequestInterceptor {

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		// Log the Request
		logRequest(request, body);

		// Execute the request
		ClientHttpResponse response = execution.execute(request, body);

		// Buffer the response body
		ClientHttpResponse bufferedResponse = bufferResponse(response);

		// Log the Response
		logResponse(bufferedResponse);

		return bufferedResponse;
	}

	private void logRequest(HttpRequest request, byte[] body) {
		System.out.println("URI         : " + request.getURI());
		System.out.println("Method      : " + request.getMethod());
		System.out.println("Headers     : " + request.getHeaders());
		System.out.println("Request Body: " + new String(body, StandardCharsets.UTF_8));
	}

	private void logResponse(ClientHttpResponse response) throws IOException {
		String responseBody = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))
				.lines()
				.collect(Collectors.joining("\n"));
		System.out.println("Response Status Code: " + response.getStatusCode());
		System.out.println("Response Headers    : " + response.getHeaders());
		System.out.println("Response Body       : " + responseBody);
	}

	private ClientHttpResponse bufferResponse(ClientHttpResponse response) throws IOException {
		// Read the response body
		String responseBody = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))
				.lines()
				.collect(Collectors.joining("\n"));

		// Create a buffered response
		return new BufferedClientHttpResponse(response, responseBody);
	}

	private static class BufferedClientHttpResponse implements ClientHttpResponse {

		private final ClientHttpResponse original;
		private final byte[] body;

		public BufferedClientHttpResponse(ClientHttpResponse original, String body) {
			this.original = original;
			this.body = body.getBytes(StandardCharsets.UTF_8);
		}

		@Override
		public org.springframework.http.HttpStatus getStatusCode() throws IOException {
			return (HttpStatus) original.getStatusCode();
		}

		@Override
		@Deprecated
		public int getRawStatusCode() throws IOException {
			return original.getRawStatusCode();
		}

		@Override
		public String getStatusText() throws IOException {
			return original.getStatusText();
		}

		@Override
		public void close() {
			original.close();
		}

		@Override
		public java.io.InputStream getBody() {
			return new ByteArrayInputStream(body);
		}

		@Override
		public org.springframework.http.HttpHeaders getHeaders() {
			return original.getHeaders();
		}
	}
}
class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

	private final byte[] cachedBody;

	public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
		super(request);
		InputStream requestInputStream = request.getInputStream();
		this.cachedBody = requestInputStream.readAllBytes();
	}

	@Override
	public ServletInputStream getInputStream() {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
		return new ServletInputStream() {
			@Override
			public int read() {
				return byteArrayInputStream.read();
			}

			@Override
			public boolean isFinished() {
				return byteArrayInputStream.available() == 0;
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setReadListener(ReadListener readListener) {
				// No-op
			}
		};
	}

	public String getBody() {
		return new String(cachedBody);
	}
}
 class CachedBodyHttpServletResponse extends HttpServletResponseWrapper {

	private final ByteArrayOutputStream cachedOutputStream = new ByteArrayOutputStream();

	public CachedBodyHttpServletResponse(HttpServletResponse response) {
		super(response);
	}

	@Override
	public ServletOutputStream getOutputStream() {
		return new ServletOutputStream() {
			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setWriteListener(WriteListener writeListener) {
				// No-op
			}

			@Override
			public void write(int b) throws IOException {
				cachedOutputStream.write(b);
			}
		};
	}

	@Override
	public PrintWriter getWriter() {
		return new PrintWriter(cachedOutputStream);
	}

	public String getBody() {
		return cachedOutputStream.toString();
	}
}
@Component
class RequestResponseLoggingFilter implements Filter {

//	@Override
//	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//		HttpServletRequest httpRequest = (HttpServletRequest) request;
//		HttpServletResponse httpResponse = (HttpServletResponse) response;
//
//		// Log the Request
//		System.out.println("Request URI: " + httpRequest.getRequestURI());
//		System.out.println("Request Method: " + httpRequest.getMethod());
//		System.out.println("Request Headers: " + httpRequest.getHeaderNames());
//		System.out.println("Request Body: " + new String(httpRequest.getInputStream().readAllBytes()));
//
//
//		chain.doFilter(request, response);
//
//		// Log the Response
//		System.out.println("Response Status: " + httpResponse.getStatus());
//		System.out.println("Response Body: " + response.toString());
//	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
			CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest((HttpServletRequest) request);
			CachedBodyHttpServletResponse wrappedResponse = new CachedBodyHttpServletResponse((HttpServletResponse) response);

			// Log the Request
			System.out.println("Request URI: " + wrappedRequest.getRequestURI());
			System.out.println("Request Method: " + wrappedRequest.getMethod());
			System.out.println("Request Body: " + wrappedRequest.getBody());

			chain.doFilter(wrappedRequest, wrappedResponse);

			// Log the Response
			System.out.println("Response Status: " + wrappedResponse.getStatus());
			System.out.println("Response Body: " + wrappedResponse.getBody());
		} else {
			chain.doFilter(request, response);
		}
	}


	private void objectData(Object obj){
		try {
			ObjectMapper mapper = new ObjectMapper();
			String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
			System.out.println(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void init(FilterConfig filterConfig) {
		// No-op
	}

	@Override
	public void destroy() {
		// No-op
	}
}

//@RestController
//@AllArgsConstructor
//class WelcomeController1 {
//
//	@Autowired
//	Humanrpos humanrpos;
//
//	public final FreeMarkerTemplateService freeMarkerTemplateService;
//
//
//	private final PaymentRepo paymentRepo;
//	private final ApplicationEventPublisher applicationEventPublisher;
//
//	private final RestTemplateClient client;
//
//	private final BeanA beanA;
//
//	@Autowired
//	KafkaProducer kafkaProducer;
//
//	public void publishEvent(String message) {
//		CustomEvent event = new CustomEvent(this, message);
//		applicationEventPublisher.publishEvent(event);
//	}
//
//	@GetMapping("/hello")
//	public String hello() {
//		publishEvent("hanling custom event");
//		return client.fetchResource("/hello");
//	}
//	@PostMapping("/addpayment1")
//	public Payment addpayment1(@RequestBody Payment payment){
//		payment.setAmount("1000");
//        return payment;
//	}
//
//	@GetMapping("/addpayment")
//	public String addpayment() throws ExecutionException, InterruptedException, TemplateException, IOException {
//		String payment=client.fetchResource("/getpayment");
//		Payment p=new Payment();
//		p.setAmount(payment);
//		paymentRepo.save(p);
//		String paymentpayload = createPayload("50");
//		kafkaProducer.sendMessage("my-topic", paymentpayload);
//		return payment+"rs payment added";
//	}
//
//	@SneakyThrows
//    public String createPayload(String payment){
//		Map<String, Object> model = new HashMap<>();
//		model.put("id", 1);
//		model.put("message", payment);
//		System.out.println("WelcomeController.welcome");
//		return freeMarkerTemplateService.processTemplate("paymentRequest.ftl", model);
//	}
//
//	@GetMapping("/human")
//	public List<Human> helloa() {
//		return humanrpos.findAll();
//	}
////
////	@PostMapping("/human")
////	public Human saveemp(@RequestBody Human human) {
////		return humanrpos.save(human);
////	}
//}

class FileStorageUtil {

	public static void saveToFile(String fileName, String data) {
		try (FileWriter fileWriter = new FileWriter(fileName)) {
			fileWriter.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

@Configuration
class KafkaProducerConfig {

	@Bean
	public ProducerFactory<String, String> producerFactory() {
		Map<String, Object> config = new HashMap<>();
		config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		return new DefaultKafkaProducerFactory<>(config);
	}

	@Bean
	public KafkaTemplate<String, String> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}
}
@EnableKafka
@Configuration
class KafkaConsumerConfig {

	@Bean
	public ConsumerFactory<String, String> consumerFactory() {
		Map<String, Object> config = new HashMap<>();
		config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		config.put(ConsumerConfig.GROUP_ID_CONFIG, "my-group");
		config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Disable auto commit
		return new DefaultKafkaConsumerFactory<>(config);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		factory.getContainerProperties().setAckMode(org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL);
		return factory;
	}
}

@Service
class KafkaConsumer {

//	@KafkaListener(topics = "my-topic", groupId = "my-group")
//	public void listen(String message) {
//		System.out.println("Received Message: " + message);
//	}
//	bin/kafka-consumer-groups.sh --describe --group my-group --bootstrap-server localhost:9092
//	@KafkaListener(topics = "my-topic", groupId = "my-group")
	public void listen(String message, Acknowledgment acknowledgment) {
		try {
			// Process the message
			System.out.println("Received message: " + message);

			// Acknowledge the message
			acknowledgment.acknowledge();
		} catch (Exception e) {
			System.err.println("Failed to process message: " + message);
			// You can decide not to acknowledge the message here
		}
	}
}

@Service
 class KafkaProducer {

	private final KafkaTemplate<String, String> kafkaTemplate;

	public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	public void sendMessage(String topic, String message) throws ExecutionException, InterruptedException {
		int randomInt = (int)(1 + (Math.random() * (9 - 1)));
//		kafkaTemplate.send(topic,"key"+randomInt, message);
		SendResult<String, String> result = kafkaTemplate.send(topic,"key"+randomInt, message).get();
		System.out.printf("Message sent successfully to topic %s, partition %d, offset %d%n",
				result.getRecordMetadata().topic(),
				result.getRecordMetadata().partition(),
				result.getRecordMetadata().offset());
//		sendMessageWithAcknowledgment( topic,  message);
	}
	public void sendMessageWithAcknowledgment(String topic, String message) {
		Random random = new Random();
		int randomInt = random.nextInt(100); // Random number for key

		String key = "key" + randomInt;

		CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, message);
future.thenRunAsync(() -> {});
//		future(new ListenableFutureCallback<>() {
//
//			@Override
//			public void onSuccess(SendResult<String, String> result) {
//				RecordMetadata metadata = result.getRecordMetadata();
//				System.out.printf(
//						"Message sent successfully to topic %s, partition %d, offset %d, key: %s%n",
//						metadata.topic(), metadata.partition(), metadata.offset(), key
//				);
//			}
//
//			@Override
//			public void onFailure(Throwable ex) {
//				System.err.printf("Failed to send message with key %s: %s%n", key, ex.getMessage());
//			}
//		});
	}
}



@RestController
@AllArgsConstructor
class WelcomeController {
	@Autowired
	KafkaProducer kafkaProducer;

	@GetMapping("/welcome")
	public int welcome()  {

//		kafkaProducer.sendMessage("my-topic", "Hello Kafka!");
		// Return the name of the FreeMarker template file (without .ftl extension)
		return TestUtilly.add(43, 2);
	}

	@GetMapping("/api")
	public String welcome1()  {

//		kafkaProducer.sendMessage("my-topic", "Hello Kafka!");
		// Return the name of the FreeMarker template file (without .ftl extension)
		return "eert";//TestUtill.add(1, 2);
	}
}
//@Component
// class BeanA {
//	public String getMessage() {
//		return "BeanA";
//	}
//}
//
//@Component
// class BeanB {
//	public String getMessage() {
//		return "BeanB";
//	}
//}


//@Component
// // Mark this bean as the preferred one
class BeanA {
	public String getMessage() {
		return "BeanA (Primary)";
	}
}

@Component
@Primary
class BeanBC {
	public String getMessage() {
		return "BeanB";
	}
}

@Component
class BeanC extends BeanBC {
	public String getMessage() {
		return "child";
	}
}
@Configuration
class AppConfig {

	@Bean
	@Primary
	public BeanA beanA() {
		return new BeanA();
	}

	@Bean
	public BeanA beanB() {
		return new BeanA();
	}
}

@Component
class MyService {
	private final BeanBC bean;

	public MyService(BeanBC bean) {
		this.bean = bean;
	}

	public void printMessage() {
		System.out.println(bean.getMessage());
	}
}

@Service
 class RestTemplateClient {

	private final String baseurl="http://localhost:9094";

	private final RestTemplate restTemplate;

	private final WebClient webClient;


	public String getUrl(){
		return baseurl;
	}

//	public Mono<String> fetchPost() {
//		return webClient.get()
//				.uri("/posts/1") // Relative to base URL
//				.retrieve()
//				.bodyToMono(String.class); // Convert response to String
//	}
	public RestTemplateClient(RestTemplate restTemplate,WebClient webClient) {
		this.restTemplate = restTemplate;
		this.webClient = webClient;
	}

	public String fetchResource(String path) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer your-token-value");
//		headers.set("Custom-Header", "CustomHeaderValue");

		// Create an HttpEntity with headers
		HttpEntity<String> entity = new HttpEntity<>(headers);

		// Execute GET request
		ResponseEntity<String> response = restTemplate.exchange(
				getUrl()+path,              // URL
				HttpMethod.GET,   // HTTP method
				entity,           // Request entity (with headers)
				String.class      // Response type
		);

		// Print the response
//		String response = restTemplate.getForObject(getUrl()+path, String.class);
		return response.getBody();
	}

//	public Mono<String> fetchResource(String path) {
//		Mono<String> response = webClient.get()
//				.uri(path) // Relative to base URL
//				.retrieve()
//				.bodyToMono(String.class); // Convert response to String
//		return response;
//	}
}

 class CustomEvent extends ApplicationEvent {
	private String message;

	public CustomEvent(Object source, String message) {
		super(source);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}

// class CustomEvent {
//	private final String message;
//
//	public CustomEvent(String message) {
//		this.message = message;
//	}
//
//	public String getMessage() {
//		return message;
//	}
//}
//@Component
//class EventPublisher {
//	private final ApplicationEventPublisher applicationEventPublisher;
//
//	public EventPublisher(ApplicationEventPublisher applicationEventPublisher) {
//		this.applicationEventPublisher = applicationEventPublisher;
//	}
//
//	public void publishEvent(String message) {
//		CustomEvent event = new CustomEvent( message);
//		applicationEventPublisher.publishEvent(event);
//	}
//}
@Component
class AsyncEventListener {
	private final ApplicationEventPublisher applicationEventPublisher;

	public AsyncEventListener(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	public void publishEvent(String message) {
		CustomEvent event = new CustomEvent(this, message);
//		applicationEventPublisher.publishEvent(event);
	}

	@EventListener
	public void onApplicationEvent(ContextRefreshedEvent event) {
		System.out.println("Context refreshed: " + event);
	}
	@Async
	@EventListener
	public void handleCustomEvent(CustomEvent event) {
		System.out.println("Handling event asynchronously: " + event.getMessage());
	}

	@EventListener
	public void handleContextStarted(ContextStartedEvent event) {
		System.out.println("Context started: " + event);
	}
	@EventListener
	public void handleContextClosed(ContextClosedEvent event) {
		System.out.println("Context closed: " + event);
	}
	@EventListener
	public void handleApplicationStarting(ApplicationStartingEvent event) {
		System.out.println("Application is starting: " + event);
	}
	@EventListener
	public void handleEnvironmentPrepared(ApplicationEnvironmentPreparedEvent event) {
		System.out.println("Environment prepared: " + event);
	}
	@EventListener
	public void handleApplicationPrepared(ApplicationPreparedEvent event) {
		System.out.println("Application prepared: " + event);
	}
	@EventListener
	public void handleApplicationReady(ApplicationReadyEvent event) {
		publishEvent("custom message");
		System.out.println("Application is ready: " + event);
	}
	@EventListener
	public void handleApplicationFailed(ApplicationFailedEvent event) {
		System.err.println("Application failed to start: " + event.getException());
	}
	@EventListener
	public void handleRequestHandled(RequestHandledEvent event) {
		System.out.println("Request handled: " + event);
	}


}

//@Component
 class TransactionEventListener {

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleTransactionCommit(Object event) {
		System.out.println("Transaction committed: " + event);
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
	public void handleTransactionRollback(Object event) {
		System.out.println("Transaction rolled back: " + event);
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
	public void handleTransactionCompletion(Object event) {
		System.out.println("Transaction completed: " + event);
	}
}

//@Component
 class TransactionPhaseListener {

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	public void beforeCommit(Object event) {
		System.out.println("Before commit: " + event);
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void afterCommit(Object event) {
		System.out.println("After commit: " + event);
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
	public void afterRollback(Object event) {
		System.out.println("After rollback: " + event);
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
	public void afterCompletion(Object event) {
		System.out.println("After completion: " + event);
	}
}
@Configuration
 class FreeMarkerConfig {
	@Autowired
	private freemarker.template.Configuration configuration;

	@Component
	@Primary // Mark this bean as the preferred one
	class BeanA {
		public String getMessage() {
			return "BeanA (Primary)";
		}
	}

	@Component
	class BeanB {
		public String getMessage() {
			return "BeanB";
		}
	}
//	@Bean
//	public RestTemplate restTemplate() {
//		return new RestTemplate();
//	}


	@Bean
	public RestTemplate restTemplate1(RestTemplateBuilder builder) {
		return builder
				.interceptors(new LoggingInterceptor()) // Add the logging interceptor
				.build();
	}
	@Bean
	public WebClient webClient(WebClient.Builder builder) {
		return builder.baseUrl("http://localost:9094") // Optional base URL
				.build();
	}
	@Bean(name="emailConfigBean")
	public FreeMarkerConfigurationFactoryBean getFreeMarkerConfiguration(ResourceLoader resourceLoader) {
		FreeMarkerConfigurationFactoryBean bean = new FreeMarkerConfigurationFactoryBean();
		bean.setTemplateLoaderPath("classpath:/templates/");
		return bean;
	}
//	@Bean
//	public Configuration freemarkerConfiguration() {
//		Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);
//
//		// Load templates from /templates directory
//		configuration.setClassForTemplateLoading(this.getClass(), "/templates");
//		configuration.setDefaultEncoding("UTF-8");
//		configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
//
//		return configuration;
//	}
}
@Service
class FreeMarkerTemplateService {

	private final freemarker.template.Configuration freemarkerConfig;

	@Autowired
	public FreeMarkerTemplateService(FreeMarkerConfigurationFactoryBean factoryBean) throws Exception {
		this.freemarkerConfig = factoryBean.getObject();
	}

	public String processTemplate(String templateName, Map<String, Object> model) throws IOException, TemplateException {
		Template template = freemarkerConfig.getTemplate(templateName);
		StringWriter writer = new StringWriter();
		template.process(model, writer);
		return writer.toString();
	}
}
//
@Entity
@Data
@Getter
@Setter
class Human{
	@Id
	@GeneratedValue(
			strategy = GenerationType.AUTO
	)
	int id;
	String name;
	String email;

}

@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@ToString
class Payment{
	@Id
	@GeneratedValue(
			strategy = GenerationType.AUTO
	)
	int id;
	String amount;

}
interface PaymentRepo extends JpaRepository<Payment, Integer> {

}
//
interface Humanrpos extends JpaRepository<Human, Integer> {

}
//@Component
//class CustomFilter implements Filter {
//
//	@Override
//	public void init(FilterConfig filterConfig) throws ServletException {
//		// Initialization logic (optional)
//	}
//
//	@Override
//	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//			throws IOException, ServletException {
//		HttpServletRequest httpRequest = (HttpServletRequest) request;
//		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
//		System.out.println("Request URI: " + httpRequest.getRequestURI());
////		System.out.println("REs body: " + httpServletResponse.getHeader("Content-Type"));
////		System.out.println("REs body: " + httpServletResponse.getOutputStream().);
//
//
//
//		// Continue the filter chain
//		chain.doFilter(request, response);
//	}
//
//	@Override
//	public void destroy() {
//		// Cleanup logic (optional)
//	}
//}
//class JsonUtils {
//
//	public static String getJsonFromRequest(HttpServletRequest request) throws IOException {
//		StringBuilder jsonBuilder = new StringBuilder();
//		try (BufferedReader reader = new BufferedReader(
//				new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
//			String line;
//			while ((line = reader.readLine()) != null) {
//				jsonBuilder.append(line);
//			}
//		}
//		return jsonBuilder.toString();
//	}
//}
////@Component
//class LoggingInterceptor implements HandlerInterceptor {
//
//	@Override
//	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//		System.out.println("Pre Handle method is called. URI: " + request.getRequestURI());
//		return true; // Continue with the next interceptor or controller
//	}
//
//	@Override
//	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, org.springframework.web.servlet.ModelAndView modelAndView) throws Exception {
//		System.out.println("Post Handle method is called.");
//	}
//
//	@Override
//	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//		String jsonPayload = JsonUtils.getJsonFromRequest((HttpServletRequest) request);
//		System.out.println("Received JSON: " + jsonPayload);
//		System.out.println("Request and Response are completed.");
//	}
//}
//@Configuration
// class InterceptorConfig implements WebMvcConfigurer {
//
//
//	@Override
//	public void addInterceptors(InterceptorRegistry registry) {
//		registry.addInterceptor(new LoggingInterceptor())
//				.addPathPatterns("/**") // Apply to all paths
//				.excludePathPatterns("/auth/**"); // Exclude specific paths
//	}
//	@Bean
//	public FilterRegistrationBean<CustomFilter> customFilter() {
//		FilterRegistrationBean<CustomFilter> registrationBean = new FilterRegistrationBean<>();
//		registrationBean.setFilter(new CustomFilter());
////		registrationBean.addUrlPatterns("**"); // Apply to specific URL patterns
//		registrationBean.setOrder(1); // Set the order of the filter
//		return registrationBean;
//	}
//}
//
@Configuration
class KafkaStreamsConfig {


//	@Bean(name = "streamconfig")
//	public StreamsConfig kafkaStreamsConfig() {
//		Map<String, Object> props = new HashMap<>();
//
//		// Kafka Bootstrap Servers
//		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
//
//		// Application ID
//		props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-streams-app-43343");
//
//		// State Directory
//		props.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams");
//
//		// Commit Interval (in milliseconds)
//		props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10);
//
//		// Default Key and Value Serde
//		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
//		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
//
//		return new StreamsConfig(props);
//	}
//
//	@Bean
//	public StreamsBuilder streamsBuilder(StreamsConfig streamsConfig) {
//		return new StreamsBuilder();
//	}

//////	@Bean
//////	public KStream<String, String> wordCountStream(StreamsBuilder streamsBuilder) {
//////		// Define input topic
//////		KStream<String, String> inputStream = streamsBuilder.stream("input-topic");
//////
//////		// Transform the data: perform word count
//////		KTable<String, Long> wordCounts = inputStream
//////				.flatMapValues(value -> List.of(value.toLowerCase().split(" "))) // Split words
//////				.groupBy((key, word) -> word) // Group by word
//////				.count(); // Count occurrences
//////
//////		// Output the results to an output topic
//////		wordCounts.toStream().to("output-topic", Produced.with(org.apache.kafka.common.serialization.Serdes.String(),
//////				org.apache.kafka.common.serialization.Serdes.Long()));
//////
//////		return inputStream;
//////	}
////
////

//@Bean
//public NewTopic topic() {
//	return TopicBuilder.name("input-topic")
//			.partitions(1)
//			.replicas(1)
//			.build();
//}
//
//@Bean
//public NewTopic topic1() {
//	return TopicBuilder.name("output-topic")
//			.partitions(1)
//			.replicas(1)
//			.build();
//}

//@Bean
//public KStream<String, String> streamProcessing(StreamsBuilder builder) {
//	KStream<String, String> stream = builder.stream("input-topic");
//	stream.mapValues(value -> {
//		try {
//			return value.toUpperCase();
//		} catch (Exception e) {
//			System.err.println("Error processing value: " + e.getMessage());
//			return null; // Skip problematic records
//		}
//	});
//	stream.mapValues(value -> value.toUpperCase()).to("output-topic");
//
//	return stream;
//}

//	@Bean
//	public KStream<String, String> wordCountStream(StreamsBuilder streamsBuilder) {
//		// Define input topic
//		KStream<String, String> inputStream = streamsBuilder.stream("input-topic");
//
//		// Transform the data: perform word count
//		KTable<String, Long> wordCounts = inputStream
//				.flatMapValues(value -> List.of(value.toLowerCase().split(" "))) // Split words
//				.groupBy((key, word) -> word) // Group by word
//				.count(); // Count occurrences
//		System.out.println(wordCounts);
//		// Output the results to an output topic
//		wordCounts.toStream().to("output-topic", Produced.with(org.apache.kafka.common.serialization.Serdes.String(),
//				org.apache.kafka.common.serialization.Serdes.Long()));
//
//		return inputStream;
//	}
}