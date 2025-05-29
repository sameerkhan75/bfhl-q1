package com.example.bfhl;

import com.example.bfhl.dto.WebhookResponse;
import com.example.bfhl.dto.FinalQueryRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@SpringBootApplication
public class BfhlQ1Application implements CommandLineRunner {

    private final RestTemplate restTemplate;

    @Value("${api.base}")
    private String baseUrl;
    @Value("${app.name}")
    private String name;
    @Value("${app.regNo}")
    private String regNo;
    @Value("${app.email}")
    private String email;

    public BfhlQ1Application(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(BfhlQ1Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        WebhookResponse gen = restTemplate.postForObject(
            baseUrl + "/generateWebhook/JAVA",
            Map.of("name", name, "regNo", regNo, "email", email),
            WebhookResponse.class
        );
        String token = gen.accessToken();

        int lastTwo = Integer.parseInt(regNo.substring(regNo.length() - 2));
        String finalSql;
        if (lastTwo % 2 == 1) {
            finalSql = """
                SELECT
                    ms.max_sal                          AS SALARY,
                    CONCAT(e.first_name, ' ', e.last_name) AS NAME,
                    TIMESTAMPDIFF(YEAR, e.dob, CURDATE())  AS AGE,
                    d.department_name                   AS DEPARTMENT_NAME
                FROM (
                    SELECT MAX(amount) AS max_sal
                    FROM   payments
                    WHERE  DAY(payment_time) <> 1
                ) ms
                JOIN payments p
                  ON p.amount = ms.max_sal
                 AND DAY(p.payment_time) <> 1
                JOIN employee e
                  ON p.emp_id = e.emp_id
                JOIN department d
                  ON e.department = d.department_id;
                """;
        } else {
            finalSql = """
                /* Q2 branch—no-op for your regNo */
                SELECT NULL;
                """;
        }

HttpHeaders headers = new HttpHeaders();
headers.set("Authorization", token);
headers.setContentType(MediaType.APPLICATION_JSON);


        HttpEntity<FinalQueryRequest> req = new HttpEntity<>(
            new FinalQueryRequest(finalSql), headers
        );
        ResponseEntity<String> resp = restTemplate.postForEntity(
            baseUrl + "/testWebhook/JAVA",
            req,
            String.class
        );

        System.out.println("✅ Submission response: " + resp.getStatusCode());
        System.out.println(resp.getBody());
    }
}
