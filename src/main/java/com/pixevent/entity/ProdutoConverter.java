package com.pixevent.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ProdutoConverter implements AttributeConverter<Produto, String> {

    @Override
    public String convertToDatabaseColumn(Produto attribute) {
        return attribute == null ? null : attribute.getValor();
    }

    @Override
    public Produto convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Produto.fromValor(dbData);
    }
}
