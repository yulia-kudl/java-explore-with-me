package ru.practicum.events.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.events.SortType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchPublicFilterDto {
    private String text;
    private List<Integer> categories;
    private Boolean paid;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeStart;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime rangeEnd;
    private SortType sort;
    private Boolean onlyAvailable;
    private Integer from = 0;
    private Integer size = 10;


    @AssertTrue(message = "Field: range. start должен быть до end")
    public boolean isRangeValid() {
        if (rangeEnd == null || rangeStart == null)
            return true;

        return rangeStart.isBefore(rangeEnd);

    }
}
