package academy.spring.springai.songs;


import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.StructuredOutputConverter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/songs")
public class SongsController {

	private final ChatClient chatClient;
	
	/**
	 * Constructor to inject the ChatClient.
	 *
	 * @param chatClientBuilder the ChatClient.Builder to be used for generating responses
	 */
	public SongsController(ChatClient.Builder chatClientBuilder) {
		this.chatClient = chatClientBuilder.build();
	}

	/**
	 * Endpoint to get the top song of the year 2023 using a string prompt.
	 *
	 * @return the response from the chat client
	 */
	@GetMapping("/stringprompt/topSong")
	public String getTopSong() {
		String stringPrompt = "What was the Billboard number one year-end top 100 single for 1984?";

		var chatResponse = chatClient.prompt()
				.system("You are a music expert.")
				.user(stringPrompt)
				.call();

		System.out.println(">> ChatResponse: " + chatResponse.chatResponse());
		return chatResponse.content();

	}	

	/**
	 * Endpoint to get the top song of a specified year using a parameterized prompt.
	 *
	 * @param year the year for which to retrieve the top song
	 * @return the response from the chat client
	 */
	@GetMapping("/stringprompt/topSong/{year}")
	public String getTopSong(@PathVariable String year) {
		// Create a prompt template with a parameter for the year
		PromptTemplate promptTemplate = new PromptTemplate("What was the Billboard number one year-end top 100 single for {year}?");

		// Create a prompt with the specified year
		promptTemplate.add("year", year);

		// Call the chat client with the prompt and return the response
		var chatResponse = chatClient.prompt()
				.system("You are a music expert.")
				.user(promptTemplate.render())
				.call();

		// debugging information
		logResponse(chatResponse);

		return chatResponse.content();
	}

	/**
	 * Endpoint to get the top song of a specified year using a structured output converter.
	 *
	 * @param year the year for which to retrieve the top song
	 * @return a TopSong object containing the title, artist, album, and year
	 */
	@GetMapping("/objectreturn/topsong/{year}")
	public TopSong objectReturnTopSong(@PathVariable("year") int year) {
		// Create a structured output converter for the TopSong class
		// see https://docs.spring.io/spring-ai/reference/api/structured-output-converter.html
		StructuredOutputConverter<TopSong> parser = new BeanOutputConverter<>(TopSong.class);

		String promptText = """
        What was the Billboard number one year-end top 100 single for {year}?
        {format}
        """;

		PromptTemplate promptTemplate = new PromptTemplate(promptText);
		Map<String, Object> promptParams = new HashMap<>();
		promptParams.put("year", year);
		promptParams.put("format", parser.getFormat());

		Prompt prompt = promptTemplate.create(promptParams);

		var chatResponse = chatClient.prompt()
				.system("You are a music expert.")
				.user(promptTemplate.render(promptParams))
				.call();

		// debugging information
		logResponse(chatResponse);

		TopSong topSong = parser.convert(Objects.requireNonNull(chatResponse.content()));
		return topSong;
	}

	/**
	 * Log debugging info about the chat response.
	 */
	private void logResponse(ChatClient.CallResponseSpec callResponse) {
		var response = callResponse.chatResponse();
		System.out.println(">> response: " + response);
        if (response != null) {
			var metadata = response.getMetadata();
            System.out.println(">> >> Metadata: " + metadata);
			System.out.println(">> >> Model: " + metadata.getModel());
			System.out.println(">> >> Usage: " + metadata.getUsage());
			System.out.println(">> >> Prompt Tokens: " + metadata.getUsage().getPromptTokens());
			System.out.println(">> >> Total Tokens: " + metadata.getUsage().getTotalTokens());
		} else {
			System.out.println(">> No response received.");
        }
        System.out.println("====================================================================================================");
	}
}
