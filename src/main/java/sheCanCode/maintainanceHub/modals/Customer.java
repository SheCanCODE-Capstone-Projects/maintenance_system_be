package sheCanCode.maintainanceHub.modals;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "default_latitude", precision = 10, scale = 8)
    private BigDecimal defaultLatitude;

    @Column(name = "default_longitude", precision = 11, scale = 8)
    private BigDecimal defaultLongitude;
}
