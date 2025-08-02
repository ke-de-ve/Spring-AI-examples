package academy.spring.springai.songs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SongsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SongsApplication.class, args);
	}

	//TODO: Create new ImageClient Bean
//	@Bean
//	ImageClient imageClient(@Value("${spring.ai.openai.api-key}") String apiKey) {
//		return new OpenAiImageClient(new OpenAiImageApi(apiKey));
//	}
}
