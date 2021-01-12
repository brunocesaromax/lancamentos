package com.example.lancamentoapi.repository.projection;

import com.example.lancamentoapi.model.TypeLaunch;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
public class LaunchSummary {

    private Long id;
    private String description;
    private LocalDate dueDate;
    private LocalDate payday;
    private BigDecimal value;
    private TypeLaunch type;
    private String category;
    private String person;

    public LaunchSummary(Long id, String description, LocalDate dueDate, LocalDate payday, BigDecimal value, TypeLaunch type, String category, String person) {
        this.id = id;
        this.description = description;
        this.dueDate = dueDate;
        this.payday = payday;
        this.value = value;
        this.type = type;
        this.category = category;
        this.person = person;
    }
}
