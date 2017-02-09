package com.example;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@EnableFeignClients
@SpringBootApplication
@EnableConfigurationProperties(MyProperty.class)
@EnableCircuitBreaker
@EnableHystrixDashboard
public class FeignDemeoApplication implements CommandLineRunner{
	private final IFeignClient feignClient;
	private final IFeignClient2 feignClient2;

	public FeignDemeoApplication(IFeignClient feignClient, IFeignClient2 feignClient2) {
		this.feignClient = feignClient;
		this.feignClient2 = feignClient2;
	}

	public static void main(String[] args) {
		SpringApplication.run(FeignDemeoApplication.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {
		feignClient2.getData().stream().forEach(System.out::println);
	}
}
@RestController
class MyController {
	private final IFeignClient2 feignClient2;

	MyController(IFeignClient2 feignClient2) {
		this.feignClient2 = feignClient2;
	}

	@GetMapping("/")
	public List<Tipo> get() {
		return feignClient2.getData();
	}

	public List<Tipo> reliable() {
		return new ArrayList<>();
	}
}

@FeignClient(name = "data",
		url = "http://localhost:8080")
interface IFeignClient{
	@RequestMapping("/data")
	List<Tipo> getData();
}
@FeignClient(name = "data2",
		url = "${myapp.url-access}",
		fallback = FeignClientFallBack.class)
interface IFeignClient2{
	@HystrixCommand(fallbackMethod = "reliable")
	@RequestMapping("/data")
	List<Tipo> getData();
}
@Component
class FeignClientFallBack implements IFeignClient2{
	@Override
	public List<Tipo> getData() {
		return new ArrayList<>();
	}
}

class Tipo {
	private long id;
	private String name;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Tipo{" +
				"id=" + id +
				", name='" + name + '\'' +
				'}';
	}
}
@ConfigurationProperties(prefix = "myapp")
class MyProperty {
	private String urlAccess = "http://localhost:8082";

	public String getUrlAccess() {
		return urlAccess;
	}

	public void setUrlAccess(String urlAccess) {
		this.urlAccess = urlAccess;
	}
}
