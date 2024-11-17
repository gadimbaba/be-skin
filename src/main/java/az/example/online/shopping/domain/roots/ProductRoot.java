package az.example.online.shopping.domain.roots;

import az.example.online.shopping.domain.valueobjects.Money;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;


@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
public class ProductRoot extends BaseRoot<UUID> {
    private String name;
    private String description;
    private String category;
    private String subCategory;
    private String note;
    private Money sellPrice;
    private Money wholeSalePrice;
    private Integer quantity;
    private String imageName;
    private String imageType;
    private byte[] imageData;
}
