package az.example.online.shopping.domain.mapper.concretes;

import az.example.online.shopping.domain.mapper.abstracts.AbstractProductCommandMapper;
import az.example.online.shopping.domain.roots.ProductRoot;
import az.example.online.shopping.domain.valueobjects.Money;
import az.example.online.shopping.infrastructure.dataaccess.entity.ProductEntity;
import az.example.online.shopping.infrastructure.web.dto.request.command.AddProductCommand;
import az.example.online.shopping.infrastructure.web.dto.request.command.UpdateProductCommand;
import az.example.online.shopping.infrastructure.web.dto.response.ProductResponseModel;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class ProductCommandMapper implements AbstractProductCommandMapper {
    @Override
    public ProductRoot toRoot(AddProductCommand command, MultipartFile file) {
        try {
            return ProductRoot
                    .builder()
                    .name(command.getName())
                    .code(command.getCode())
                    .article(command.getArticle())
                    .description(command.getDescription())
                    .category(command.getCategory())
                    .subCategory(command.getSubCategory())
                    .note(command.getNote())
                    .sellPrice(Money.of(command.getSellPrice()))
                    .wholeSalePrice(Money.of(command.getWholeSalePrice()))
                    .quantity(command.getQuantity())
                    .imageName(file.getName())
                    .imageType(file.getContentType())
                    .imageData(file.getBytes())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public ProductEntity toEntity(ProductRoot root) {
        return ProductEntity
                .builder()
                .name(root.getName())
                .description(root.getDescription())
                .code(root.getCode())
                .article(root.getArticle())
                .category(root.getCategory())
                .subCategory(root.getSubCategory())
                .note(root.getNote())
                .sellPrice(root.getSellPrice().getAmount())
                .wholeSalePrice(root.getWholeSalePrice().getAmount())
                .quantity(root.getQuantity())
                .imageName(root.getImageName())
                .imageType(root.getImageType())
                .imageData(root.getImageData())
                .build();
    }

    @Override
    public ProductRoot toRoot(UpdateProductCommand command, MultipartFile file) {
        try {
            return ProductRoot
                    .builder()
                    .name(command.getName())
                    .code(command.getCode())
                    .article(command.getArticle())
                    .description(command.getDescription())
                    .category(command.getCategory())
                    .subCategory(command.getSubCategory())
                    .note(command.getNote())
                    .sellPrice(Money.of(command.getSellPrice()))
                    .wholeSalePrice(Money.of(command.getWholeSalePrice()))
                    .quantity(command.getQuantity())
                    .imageName(file.getName())
                    .imageType(file.getContentType())
                    .imageData(file.getBytes())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ProductResponseModel toResponseModel(ProductEntity entity) {
        return ProductResponseModel
                .builder()
                .name(entity.getName())
                .code(entity.getCode())
                .article(entity.getArticle())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .subCategory(entity.getSubCategory())
                .note(entity.getNote())
                .sellPrice(entity.getSellPrice())
                .wholeSalePrice(entity.getWholeSalePrice())
                .quantity(entity.getQuantity())
                .imageName(entity.getImageName())
                .imageType(entity.getImageType())
                .imageData(entity.getImageData())
                .build();
    }


}
