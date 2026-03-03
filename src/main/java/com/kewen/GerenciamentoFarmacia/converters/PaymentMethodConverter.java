package com.kewen.GerenciamentoFarmacia.converters;

import com.kewen.GerenciamentoFarmacia.enums.PaymentMethodEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PaymentMethodConverter implements AttributeConverter<PaymentMethodEnum, String> {

    @Override
    public String convertToDatabaseColumn(PaymentMethodEnum attribute) {
        if (attribute == null) return null;
        return attribute.name(); // salva "CREDITCARD", "PIX", etc.
    }

    @Override
    public PaymentMethodEnum convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return PaymentMethodEnum.valueOf(dbData);
    }
}