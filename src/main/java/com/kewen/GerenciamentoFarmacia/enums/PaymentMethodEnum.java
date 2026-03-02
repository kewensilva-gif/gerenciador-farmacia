package com.kewen.GerenciamentoFarmacia.enums;

public enum PaymentMethodEnum {
    CREDITCARD("Cartão de Crédito"),
    DEBITCARD("Cartão de Débito"),
    PIX("PIX"),
    CASH("Dinheiro Físico");

    private final String description;

    PaymentMethodEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
