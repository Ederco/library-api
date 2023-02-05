package com.ederco.libraryapi;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LibraryApiApplication {
//
//	@Autowired
//	private EmailService emailService;

	@Bean
	public ModelMapper modelMapper(){
		return new ModelMapper();
	}
//	@Scheduled(cron = "0 45 19 1/1 * ?")
//	public void testeAgendamentoTarefas(){
//		System.out.println("Agendamento funcionando com sucesso!");
//
//	}
//	@Bean
//	public CommandLineRunner runner(){
//		return args -> {
//			List<String> emails = Arrays.asList("library-api-831326@inbox.mailtrap.io");
//			emailService.sendMails("Testando serviços de email",emails);
//			System.out.println("EMAILS ENVIADOS");
//		};
//	}

	public static void main(String[] args) {
		SpringApplication.run(LibraryApiApplication.class, args);
	}

}
