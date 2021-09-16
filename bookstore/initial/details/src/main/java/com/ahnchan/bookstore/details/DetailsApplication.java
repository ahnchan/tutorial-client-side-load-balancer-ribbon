package com.ahnchan.bookstore.details;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

//@EnableEurekaClient
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
public class DetailsApplication {
	public static void main(String[] args) {
		SpringApplication.run(DetailsApplication.class, args);
	}
}

@RestController
class DetailsController {

	@Autowired
	private ProductRepository repository;

	@Autowired
	private EurekaClient eurekaClient;

	@GetMapping("/products")
	public Collection<Product> getProduct() {
		return repository.findAll();
	}

	@GetMapping("/products/{id}")
	public Optional<Product> getProductDetail(@PathVariable Integer id) {
		return repository.findById(id);
	}

	@GetMapping("/products/title/{title}")
	public Collection<Product> getProductByTitle(@PathVariable String title) {
		return repository.findByTitleLike("%"+title+"%");
	}

	// Use EurekaClient Information
	@GetMapping("/products/{id}/detailsV1")
	public ProductDetail getProductDetails(@PathVariable Integer id) {

		// RestTemplate 를 이용한 예제 작성 예정 (Load Balancer 튜토리얼에서 작성 예정

		Optional<Product> product = repository.findById(id);
		ProductDetail details = new ProductDetail();
		details.setProduct(repository.findById(id).get());

		return details;
	}

	// Use Spring OpenFeign
	@Autowired
	private ReviewClient reviewClient;

	@GetMapping("/products/{id}/detailsV2")
	public ProductDetail getProductDetailsV2(@PathVariable Integer id) {
		ProductDetail details = new ProductDetail();
		details.setProduct(repository.findById(id).get());
		details.setReviews(reviewClient.getReviews(id));
		return details;
	}
}

@FeignClient("REVIEWS")
interface ReviewClient {
	@RequestMapping(method = RequestMethod.GET, value="/products/{id_product}/reviews")
	List<Review> getReviews(@PathVariable Integer id_product);
}


@Data
class ProductDetail {
	private Product product;
	private Collection<Review> reviews;
}

@Data
class Review {
	private Integer id;
	private Integer id_product;
	private String reviewer;
	private String text;
}

interface ProductRepository extends CrudRepository<Product, Integer> {
	List<Product> findAll();
	List<Product> findByTitleLike(String title);
	Optional<Product> findById(Integer id);
}

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
class Product {
	@Id
	private Integer id;
	private String title;
	private String author;
	private String ISBN;
}