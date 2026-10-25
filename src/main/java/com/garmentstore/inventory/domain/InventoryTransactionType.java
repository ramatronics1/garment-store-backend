package com.garmentstore.inventory.domain;

public enum InventoryTransactionType {
    RESTOCK,
    MANUAL_CORRECTION,
    ORDER_FULFILLMENT,
    ORDER_CANCELLED_RESTOCK,
    DAMAGED,
    RETURN
}
