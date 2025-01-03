package com.example.demo.Service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import java.util.stream.Collectors;

@Service
public class HolidayService {

    private static final String HOLIDAY_API_URL = "https://date.nager.at/Api/v3/PublicHolidays/{year}/KZ";

    private final RestTemplate restTemplate;

    public HolidayService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Cacheable("holidays")
    public List<Holiday> getPublicHolidays(int year) {
        // Делаем запрос к API
        String url = HOLIDAY_API_URL.replace("{year}", String.valueOf(year));
        Holiday[] holidays = restTemplate.getForObject(url, Holiday[].class);
        return List.of(holidays);
    }

    public boolean isHoliday(LocalDate date) {
        int year = date.getYear();
        List<Holiday> holidays = getPublicHolidays(year);
        return holidays.stream()
                .anyMatch(holiday -> holiday.getDate().equals(date));
    }

    // Метод для проверки доступности API и возврата JSON с результатом
    public ResponseEntity<String> checkApiStatus() {
        try {
            // Проверка на запрос для текущего года
            String url = HOLIDAY_API_URL.replace("{year}", String.valueOf(LocalDate.now().getYear()));
            Holiday[] holidays = restTemplate.getForObject(url, Holiday[].class);

            if (holidays != null && holidays.length > 0) {
                // Формируем список дат
                List<String> holidayDates = List.of(holidays).stream()
                        .map(holiday -> holiday.getDate().toString())  // Преобразуем даты в строку
                        .collect(Collectors.toList());

                // Формируем JSON с датами
                String jsonResponse = "{\"status\": \"success\", \"message\": \"API is working.\", \"holidays\": "
                        + holidays.length + ", \"dates\": " + holidayDates.toString() + "}";
                return ResponseEntity.ok(jsonResponse); // Статус 200 OK
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body("{\"status\": \"fail\", \"message\": \"No holidays found for the year.\"}");
            }
        } catch (Exception e) {
            // Если произошла ошибка, возвращаем ошибку
            String jsonResponse = "{\"status\": \"error\", \"message\": \"API is not available.\", \"error\": \"" + e.getMessage() + "\"}";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(jsonResponse);
        }
    }

    public static class Holiday {
        private String name;
        private LocalDate date;

        public String getName() {
            return name;
        }
        public LocalDate getDate() {
            return date;
        }
    }
}

@Configuration
@EnableCaching
class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

