package com.warehouse.util;

import com.warehouse.model.InvoiceType;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates unique, human-readable invoice reference numbers.
 *
 * <p>
 * Format: {@code TYPE-YYYY-NNNNN}
 * <br>
 * Examples: {@code PUR-2023-00001}, {@code SAL-2023-00002}
 * </p>
 *
 * <p>
 * In a multi-user environment the sequence would be stored in the database;
 * for this single-user desktop application an in-memory counter suffices.
 * </p>
 */
public final class InvoiceNumberGenerator {

    private static final AtomicInteger counter = new AtomicInteger(1);

    private InvoiceNumberGenerator() {
    }

    /**
     * Generates the next invoice number for the given type.
     *
     * @param type PURCHASE or SALE
     * @return formatted invoice number string
     */
    public static String next(InvoiceType type) {
        String prefix = (type == InvoiceType.PURCHASE) ? "PUR" : "SAL";
        int seq = counter.getAndIncrement();
        return String.format("%s-%d-%05d", prefix, LocalDate.now().getYear(), seq);
    }
}
