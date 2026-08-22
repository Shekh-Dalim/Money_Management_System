package in.dalim.moneymanager.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController  // TODO used to tell Spring Boot that this class will handle HTTP requests and return data directly as the HTTP response.
@RequestMapping({"/status", "/health", })  // TODO @RequestMapping tells Spring which URL should be handled by a particular Controller or method.
public class HomeController {

    //TODO The healthCheck() method is used to check whether your Spring Boot application is running properly or not.
    @GetMapping
    public String healthCheck(){
        return "Application is running";
    }
}

