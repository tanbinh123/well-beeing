package com.wellbeeing.wellbeeing.domain.diet;

import lombok.*;
import com.wellbeeing.wellbeeing.domain.SportLabel;
import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ailment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @Column
    private String description;
    @Enumerated(EnumType.STRING)
    @Column
    private EAilmentType type;
    @ManyToMany
    @JoinTable(
            name = "ailment_sportLabel",
            joinColumns = @JoinColumn(name = "ailment_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "sport_label_id", referencedColumnName = "sportLabel_id")
    )
    private List<SportLabel> allowedLabels;
    @Column
    private int changeInCalories;
    @Column
    private int changeInProteinsGramPerKilogram;
    @Column
    private boolean isLowGlycemicIndexRecommended;
}


